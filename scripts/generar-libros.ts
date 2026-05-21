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

function crearAuthor(): AuthorEmbedded {
    return {
        _id: randomUUID(),
        name: faker.person.fullName(),
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

    return {
        _class: tipo.clazz,
        // book_id propio: UUID de alta cardinalidad para que el hash distribuya parejo
        bookId: randomUUID(),
        title: faker.book.title(),
        desc: faker.lorem.paragraph({ min: 3, max: 6 }),
        gender: faker.helpers.arrayElement(GENDERS),
        author: crearAuthor(),
        numPages: faker.number.int({ min: 50, max: 1500 }),
        isbn: generarISBN(),
        language: faker.helpers.arrayElement(LANGUAGES),
        editorial: faker.helpers.arrayElement(EDITORIALES),
        publishDate: faker.date.between({ from: '1900-01-01', to: new Date() }),
        condition: faker.helpers.arrayElement(CONDITIONS),
        owner: crearOwner(),
        imageSrc: faker.image.urlPicsumPhotos({ width: 400, height: 600 }),
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
        uri: process.env.MONGODB_URI || 'mongodb://localhost:27117',
        dbName: process.env.DB_NAME || 'book_libre',
        collectionName: process.env.COLLECTION_NAME || 'books',
        total: 500_000,
        batchSize: 10_000,
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
    console.log(`Modo: ${config.insertar ? 'INSERTAR' : 'DRY-RUN (sin insertar)'}`)
    console.log('')

    const inicio = Date.now()
    const generador = generarLibros(config.total)

    let resultado

    if (config.insertar) {
        const client = new MongoClient(config.uri)
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