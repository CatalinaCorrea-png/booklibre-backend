#!/usr/bin/env node
/**
 * Descarga un pool de libros REALES desde la API pública de Open Library
 * (https://openlibrary.org) y lo guarda en `scripts/data/libros-pool.json`.
 *
 * El pool después es consumido por `generar-libros.ts` para reemplazar a
 * Faker en los campos que importan para el range sharding por título
 * (título, autor, año, ISBN). Los demás campos siguen siendo generados.
 *
 * Usage:
 *     npx tsx scripts/fetch-libros-reales.ts
 *     npx tsx scripts/fetch-libros-reales.ts --total 10000
 */
import fs from 'node:fs/promises'
import path from 'node:path'

const OUT_DIR = path.join('scripts', 'data')
const OUT_FILE = path.join(OUT_DIR, 'libros-pool.json')

// Open Library "subject" → nuestro enum Gender del backend.
// Usamos varios subjects por género para spread la carga (offset bajo en cada
// uno) y para no chocar con la deep-pagination de la Search API (>~10k offset
// empieza a devolver resultados vacíos o duplicados).
const SUBJECT_TO_GENDER = [
    // Classic literature
    { subject: 'classics',           gender: 'CLASSIC_LITERATURE' },
    { subject: 'classic_literature', gender: 'CLASSIC_LITERATURE' },
    { subject: 'literature',         gender: 'CLASSIC_LITERATURE' },
    // Drama
    { subject: 'drama',              gender: 'DRAMA' },
    { subject: 'plays',              gender: 'DRAMA' },
    { subject: 'tragedy',            gender: 'DRAMA' },
    // Romance
    { subject: 'romance',            gender: 'ROMANCE' },
    { subject: 'love_stories',       gender: 'ROMANCE' },
    { subject: 'historical_romance', gender: 'ROMANCE' },
    // Science fiction
    { subject: 'science_fiction',    gender: 'SCIENCE_FICTION' },
    { subject: 'fantasy',            gender: 'SCIENCE_FICTION' },
    { subject: 'dystopias',          gender: 'SCIENCE_FICTION' },
    // Self-help
    { subject: 'self-help',          gender: 'SELF_HELP' },
    { subject: 'personal_development', gender: 'SELF_HELP' },
    { subject: 'motivation',         gender: 'SELF_HELP' },
] as const

const PAGE_SIZE = 100

interface BookFromAPI {
    title: string
    author: string
    isbn?: string
    publishYear?: number
    coverId?: number
    gender: typeof SUBJECT_TO_GENDER[number]['gender']
}

interface OLSearchDoc {
    title?: string
    author_name?: string[]
    isbn?: string[]
    first_publish_year?: number
    cover_i?: number
}

interface OLSearchResponse {
    docs: OLSearchDoc[]
    numFound: number
}

function parseArgs() {
    const args = process.argv.slice(2)
    const totalIdx = args.indexOf('--total')
    const total = totalIdx >= 0 ? parseInt(args[totalIdx + 1], 10) : 50000
    const perSubject = Math.ceil(total / SUBJECT_TO_GENDER.length)
    return { total, perSubject }
}

async function fetchPage(subject: string, offset: number): Promise<OLSearchResponse> {
    const url = new URL('https://openlibrary.org/search.json')
    url.searchParams.set('subject', subject)
    url.searchParams.set('limit', String(PAGE_SIZE))
    url.searchParams.set('offset', String(offset))
    url.searchParams.set('fields', 'title,author_name,isbn,first_publish_year,cover_i')

    const res = await fetch(url, {
        headers: { 'User-Agent': 'phm-unsam-tp-mongo-sharding/1.0' },
    })
    if (!res.ok) {
        throw new Error(`Open Library API devolvió ${res.status}: ${await res.text()}`)
    }
    return res.json() as Promise<OLSearchResponse>
}

async function fetchSubject(
    subject: string,
    gender: BookFromAPI['gender'],
    target: number,
): Promise<BookFromAPI[]> {
    const books: BookFromAPI[] = []
    let offset = 0
    let totalEstimado: number | null = null

    console.log(`\n📚 Subject "${subject}" → género ${gender}`)

    while (books.length < target) {
        const data = await fetchPage(subject, offset)
        if (totalEstimado === null) {
            totalEstimado = data.numFound
            console.log(`  Open Library reporta ${totalEstimado.toLocaleString()} libros para "${subject}"`)
        }

        if (data.docs.length === 0) {
            console.log(`  Sin más resultados en offset ${offset}, paro.`)
            break
        }

        for (const doc of data.docs) {
            if (!doc.title || !doc.author_name?.length) continue
            books.push({
                title: doc.title,
                author: doc.author_name[0],
                isbn: doc.isbn?.[0],
                publishYear: doc.first_publish_year,
                coverId: doc.cover_i,
                gender,
            })
            if (books.length >= target) break
        }

        offset += PAGE_SIZE
        process.stdout.write(`  ${books.length}/${target}\r`)

        // Cortesía: una pausa chica entre requests para no estresar la API
        await new Promise((r) => setTimeout(r, 150))
    }

    console.log(`  ✓ ${books.length} libros recolectados de "${subject}"`)
    return books
}

async function main() {
    const { total, perSubject } = parseArgs()
    console.log(`Objetivo: ${total.toLocaleString()} libros únicos (${perSubject} por subject)`)
    console.log(`Subjects: ${SUBJECT_TO_GENDER.length} (cubriendo 5 géneros)`)
    console.log(`Salida:   ${OUT_FILE}\n`)

    const inicio = Date.now()
    // Dedupe por (title, author): un libro suele aparecer en varios subjects.
    // El primero que lo trae gana el género (orden de SUBJECT_TO_GENDER importa).
    const vistos = new Set<string>()
    const pool: BookFromAPI[] = []

    for (const { subject, gender } of SUBJECT_TO_GENDER) {
        try {
            const libros = await fetchSubject(subject, gender, perSubject)
            let nuevos = 0
            for (const libro of libros) {
                const key = `${libro.title}|${libro.author}`.toLowerCase()
                if (vistos.has(key)) continue
                vistos.add(key)
                pool.push(libro)
                nuevos++
            }
            console.log(`  → ${nuevos} nuevos (total acumulado: ${pool.length.toLocaleString()})`)
        } catch (err) {
            console.error(`✗ Falló fetch de "${subject}":`, err)
            console.error('  Sigo con los demás subjects.')
        }
    }

    if (pool.length === 0) {
        console.error('\n✗ No se pudo descargar ningún libro. Revisá la conexión.')
        process.exit(1)
    }

    await fs.mkdir(OUT_DIR, { recursive: true })
    await fs.writeFile(OUT_FILE, JSON.stringify(pool, null, 2), 'utf8')

    const duracion = ((Date.now() - inicio) / 1000).toFixed(1)
    console.log(`\n✅ Listo: ${pool.length.toLocaleString()} libros únicos guardados en ${OUT_FILE} (${duracion}s)`)

    // Reporte rápido de distribución alfabética del pool
    const buckets: Record<string, number> = {}
    for (const b of pool) {
        const inicial = (b.title[0] || '?').toUpperCase()
        buckets[inicial] = (buckets[inicial] || 0) + 1
    }
    const inicialesOrdenadas = Object.keys(buckets).sort()
    console.log('\n📊 Distribución alfabética del pool (relevante para el range sharding):')
    for (const ini of inicialesOrdenadas) {
        const pct = ((buckets[ini] / pool.length) * 100).toFixed(1)
        console.log(`  ${ini}: ${buckets[ini].toString().padStart(5)} (${pct}%)`)
    }
}

main().catch((err) => {
    console.error(err)
    process.exit(1)
})
