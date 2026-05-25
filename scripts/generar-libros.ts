#!/usr/bin/env node
/**
 * Generador de datos de libros para MongoDB Sharding.
 *
 * Genera 500.000 documentos de libros ficticios usando Faker.
 * Por defecto opera en modo DRY-RUN: genera los datos pero NO los inserta en la base.
 * Para insertar, pasar el flag --insertar.
 *
 * Configuración (archivo .env):
 *     MONGODB_URI=mongodb://localhost:27117
 *     DB_NAME=booklibre
 *     COLLECTION_NAME=books
 *
 * Instalación:
 *     npm install
 *
 * Uso:
 *     # Solo generar y mostrar estadísticas (sin tocar la DB)
 *     npx tsx scripts/generar-libros.ts
 *
 *     # Generar e insertar en el router del cluster shard
 *     npx tsx scripts/generar-libros.ts --insertar
 */
import 'dotenv/config'
import { faker } from '@faker-js/faker'
import { randomUUID } from 'node:crypto'
import { readFileSync, existsSync } from 'node:fs'
import path from 'node:path'
import { Long, MongoClient, type Document } from 'mongodb'

// ─── Valores válidos de los enums del dominio (coinciden con Kotlin) ──────────
const GENDERS = [
    'CLASSIC_LITERATURE', 'DRAMA', 'ROMANCE', 'SCIENCE_FICTION', 'SELF_HELP',
] as const
const LANGUAGES = ['SPANISH', 'ENGLISH', 'FRENCH', 'PORTUGUESE'] as const
const CONDITIONS = ['EXCELLENT', 'VERY_GOOD', 'GOOD', 'REGULAR'] as const
// El owner NO puede ser READER (la entidad valida que un lector no crea libros)
const USER_TYPES = ['PUBLISHER', 'COMBINED'] as const

// Paquete de las clases concretas en Kotlin. Ajustá si tu paquete difiere.
const DOMAIN_PKG = 'ar.edu.unsam.phm.domain'

// bookType (string del @JsonSubTypes) ↔ clase concreta (_class de Spring Data).
// AMBOS deben ser coherentes en cada documento para que la app deserialice bien.
const BOOK_TYPE_MAP = [
    { bookType: 'COMUN',           clazz: `${DOMAIN_PKG}.Common` },
    { bookType: 'CON DEDICATORIA', clazz: `${DOMAIN_PKG}.WithADedication` },
    { bookType: 'COLECCIONABLE',   clazz: `${DOMAIN_PKG}.Collectable` },
] as const

const EDITORIALES = [
    'Alianza Editorial', 'Penguin Random House', 'Planeta', 'Anagrama',
    'Tusquets', 'Cátedra', 'Sudamericana', 'Emecé', 'Seix Barral', 'Salamandra',
]

// ─── Tipos del documento ──────────────────────────────────────────────────────
interface AuthorEmbedded {
    _id: string
    name: string
    avatar: string
}

interface OwnerEmbedded {
    _id: string
    name: string
    bibliokarmas: Long
    userType: typeof USER_TYPES[number]
    img: string
}

interface Book extends Document {
    _class: string
    bookId: string
    title: string
    desc: string
    gender: typeof GENDERS[number]
    author: AuthorEmbedded
    numPages: number
    isbn: string
    language: typeof LANGUAGES[number]
    editorial: string
    publishDate: Date
    condition: typeof CONDITIONS[number]
    owner: OwnerEmbedded
    imageSrc: string
    timestamp: Date
    bookType: string
    deleted: boolean
    ratingAvg: number
    reservationCount: Long
    reservations: unknown[]
    lastTwoReviews: unknown[]
}

interface ResultadoOperacion {
    total: number
    libroEjemplo: Book | null
}

// ─── Generación de un ISBN-13 con guiones (formato 978-X-XXXXX-XXX-X) ─────────
function generarISBN(): string {
    const grupo = faker.number.int({ min: 0, max: 9 })
    const editor = faker.string.numeric(5)
    const titulo = faker.string.numeric(3)
    const verif = faker.number.int({ min: 0, max: 9 })
    return `978-${grupo}-${editor}-${titulo}-${verif}`
}

// ─── Pool de libros REALES (Open Library) ───────────────────────────────────
// Si existe scripts/data/libros-pool.json (creado por fetch-libros-reales.ts),
// usamos esos títulos/autores/ISBN/año para tener distribución alfabética real
// (clave para evaluar el sharding por rango sobre `title`).
// Si no existe, caemos a Faker como en el setup de hash sharding.
interface PoolBook {
    title: string
    author: string
    isbn?: string
    publishYear?: number
    coverId?: number
    gender: typeof GENDERS[number]
}

const POOL_FILE = path.join('scripts', 'data', 'libros-pool.json')

function cargarPool(): PoolBook[] | null {
    if (!existsSync(POOL_FILE)) return null
    try {
        const raw = readFileSync(POOL_FILE, 'utf8')
        const pool = JSON.parse(raw) as PoolBook[]
        if (!Array.isArray(pool) || pool.length === 0) return null
        return pool
    } catch (err) {
        console.warn(`⚠️  No se pudo leer ${POOL_FILE}: ${err}. Uso Faker.`)
        return null
    }
}

const POOL = cargarPool()

function crearAuthor(name?: string): AuthorEmbedded {
    return {
        _id: randomUUID(),
        name: name ?? faker.person.fullName(),
        avatar: 'assets/author_default.jpg',
    }
}

function crearOwner(): OwnerEmbedded {
    return {
        _id: randomUUID(),
        name: faker.person.fullName(),
        bibliokarmas: Long.fromNumber(faker.number.int({ min: 0, max: 5000 })),
        userType: faker.helpers.arrayElement(USER_TYPES),
        img: faker.image.avatar(),
    }
}

function crearLibro(): Book {
    // bookType y _class deben ser coherentes: se eligen juntos del mismo map
    const tipo = faker.helpers.arrayElement(BOOK_TYPE_MAP)

    // Si tenemos pool de libros reales, tomamos uno al azar para usar sus
    // datos "auténticos" (título, autor, año, ISBN, género) en lugar de Faker.
    const real = POOL ? faker.helpers.arrayElement(POOL) : null

    const publishDate = real?.publishYear
        ? new Date(Date.UTC(real.publishYear, faker.number.int({ min: 0, max: 11 }), 1))
        : faker.date.between({ from: '1900-01-01', to: new Date() })

    const imageSrc = real?.coverId
        ? `https://covers.openlibrary.org/b/id/${real.coverId}-M.jpg`
        : faker.image.urlPicsumPhotos({ width: 400, height: 600 })

    return {
        _class: tipo.clazz,
        // book_id propio: UUID de alta cardinalidad. En range sharding sobre
        // { title, bookId } provee splitabilidad cuando hay títulos repetidos.
        bookId: randomUUID(),
        title: real?.title ?? faker.book.title(),
        desc: faker.lorem.paragraph({ min: 3, max: 6 }),
        gender: real?.gender ?? faker.helpers.arrayElement(GENDERS),
        author: crearAuthor(real?.author),
        numPages: faker.number.int({ min: 50, max: 1500 }),
        isbn: real?.isbn ?? generarISBN(),
        language: faker.helpers.arrayElement(LANGUAGES),
        editorial: faker.helpers.arrayElement(EDITORIALES),
        publishDate,
        condition: faker.helpers.arrayElement(CONDITIONS),
        owner: crearOwner(),
        imageSrc,
        timestamp: faker.date.between({ from: '2024-01-01', to: new Date() }),
        bookType: tipo.bookType,
        deleted: faker.datatype.boolean({ probability: 0.05 }),
        ratingAvg: Number(faker.number.float({ min: 0, max: 5, fractionDigits: 2 })),
        reservationCount: Long.fromNumber(faker.number.int({ min: 0, max: 200 })),
        reservations: [],
        lastTwoReviews: [],
    }
}

function* generarLibros(cantidad: number): Generator<Book> {
    for (let i = 0; i < cantidad; i++) {
        yield crearLibro()
    }
}

function obtenerConfiguracion() {
    return {
        insertar: process.argv.includes('--insertar'),
        // 127.0.0.1 fuerza IPv4: con `localhost`, Node a veces resuelve a
        // `[::1]` (IPv6). Incluimos los 2 routers para que el driver alterne
        // heartbeats y haga failover automático si uno se satura bajo carga.
        uri: process.env.MONGODB_URI || 'mongodb://127.0.0.1:27117,127.0.0.1:27118',
        dbName: process.env.DB_NAME || 'book_libre',
        collectionName: process.env.COLLECTION_NAME || 'books',
        total: 500_000,
        // 10k saturaba al router cuando el balancer estaba moviendo chunks
        // (range sharding sobre colección creciendo desde 0). 2k da margen.
        batchSize: 2_000,
    }
}

async function insertarLotes(
    generador: Generator<Book>,
    collection: any,
    config: { batchSize: number }
): Promise<ResultadoOperacion> {
    let total = 0
    let batch: Book[] = []
    let libroEjemplo: Book | null = null
    const inicio = Date.now()

    for (const libro of generador) {
        if (!libroEjemplo) libroEjemplo = libro
        batch.push(libro)

        if (batch.length >= config.batchSize) {
            await collection.insertMany(batch, { ordered: false })
            total += batch.length
            batch = []

            if (total % 100_000 === 0) {
                const segundos = ((Date.now() - inicio) / 1000).toFixed(1)
                console.log(`  ${total.toLocaleString()} insertados... (${segundos}s)`)
            }
            // Backpressure: un respiro corto para que mongos atienda heartbeats
            // sin ahogarse. Sin esto, el monitor del driver timeouteaba bajo
            // carga sostenida.
            await new Promise((r) => setTimeout(r, 25))
        }
    }

    if (batch.length > 0) {
        await collection.insertMany(batch, { ordered: false })
        total += batch.length
    }

    return { total, libroEjemplo }
}

async function contarLibros(generador: Generator<Book>): Promise<ResultadoOperacion> {
    let libroEjemplo: Book | null = null
    let total = 0

    for (const libro of generador) {
        if (!libroEjemplo) libroEjemplo = libro
        total++
    }

    return { total, libroEjemplo }
}

async function main() {
    const config = obtenerConfiguracion()

    console.log(`Generando ${config.total.toLocaleString()} libros ficticios...`)
    console.log(`Router: ${config.uri}`)
    console.log(`Destino: ${config.dbName}.${config.collectionName}`)
    if (POOL) {
        console.log(`Fuente:  pool real de ${POOL.length.toLocaleString()} libros (Open Library)`)
    } else {
        console.log(`Fuente:  Faker (no se encontró ${POOL_FILE})`)
        console.log(`         Para usar datos reales: npx tsx scripts/fetch-libros-reales.ts`)
    }
    console.log(`Modo: ${config.insertar ? 'INSERTAR' : 'DRY-RUN (sin insertar)'}`)
    console.log('')

    const inicio = Date.now()
    const generador = generarLibros(config.total)

    let resultado

    if (config.insertar) {
        // Timeouts generosos: bajo bulk pesado, mongos a veces tarda más de los
        // 10s default en responder al heartbeat → el driver marcaba el server
        // UNKNOWN y abortaba el insertMany en vuelo.
        const client = new MongoClient(config.uri, {
            serverSelectionTimeoutMS: 60_000,
            socketTimeoutMS: 120_000,
            connectTimeoutMS: 60_000,
            heartbeatFrequencyMS: 30_000,
            maxPoolSize: 8,
        })
        await client.connect()
        const collection = client.db(config.dbName).collection<Book>(config.collectionName)

        resultado = await insertarLotes(generador, collection, config)
        await client.close()
    } else {
        resultado = await contarLibros(generador)
    }

    const duracion = ((Date.now() - inicio) / 1000).toFixed(2)

    console.log(`\n✅ Listo: ${resultado.total.toLocaleString()} libros generados en ${duracion}s`)

    if (resultado.libroEjemplo) {
        console.log('\n📄 Ejemplo de documento generado:')
        console.log(JSON.stringify(resultado.libroEjemplo, null, 2))
    }

    if (!config.insertar) {
        console.log('\n⚠️  Se ejecutó en modo DRY-RUN. Los datos NO fueron insertados.')
        console.log(`💡 Para insertar ejecutá:\n   npx tsx scripts/generar-libros.ts --insertar`)
        console.log(`   Editá .env para cambiar la URI de conexión si es necesario.`)
    }
}

/**
 * SHARD KEY ELEGIDA: HASH POR bookId
 * ===================================
 * Justificación:
 * - Las consultas a la colección `books` filtran por criterios variados (título,
 *   género, ISBN, rango de páginas, dueño) con paginación. Ningún campo único se
 *   usa siempre como filtro de rango, así que un sharding por rango no aporta
 *   localidad de datos.
 * - El detalle de un libro individual (búsqueda por bookId) es una consulta
 *   frecuente: con hash, el router va DIRECTO a un solo shard (targeted query).
 * - `bookId` es un UUID de alta cardinalidad → el hash reparte los documentos de
 *   forma uniforme entre los 2 shards, evitando "jumbo chunks" y desbalanceo.
 *
 * Para habilitar el sharding desde el router (router01):
 *   use booklibre
 *   sh.enableSharding('booklibre')
 *   db.books.createIndex({ bookId: "hashed" })
 *   sh.shardCollection('booklibre.books', { bookId: "hashed" })
 *
 * Verificar la distribución luego de insertar:
 *   db.books.getShardDistribution()
 */
main().catch((err) => {
    console.error(err)
    process.exit(1)
})