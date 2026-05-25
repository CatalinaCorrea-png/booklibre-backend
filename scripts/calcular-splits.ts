#!/usr/bin/env node
/**
 * Calcula puntos de corte (split keys) óptimos para pre-splittear la colección
 * `book_libre.books` ANTES del bulk insert, usando la distribución real de
 * títulos del pool de Open Library (`scripts/data/libros-pool.json`).
 *
 * Por qué: con range sharding sobre colección vacía, MongoDB arranca con un
 * único chunk [MinKey..MaxKey] en el shard primario del DB. Todos los inserts
 * van a un solo shard hasta que ese chunk crezca lo suficiente para splittear
 * (~128 MB). Eso satura al mongos y dispara timeouts del driver.
 *
 * Solución: pre-splittear el chunk inicial en N pedazos antes de insertar, y
 * dejar que el balancer reparta los chunks vacíos entre los 2 shards. A partir
 * del primer insert, la carga se reparte ~50/50 igual que con hash.
 *
 * Uso:
 *     npx tsx scripts/calcular-splits.ts                # 16 chunks (default)
 *     npx tsx scripts/calcular-splits.ts --chunks 32
 */
import { readFileSync } from 'node:fs'
import path from 'node:path'

interface PoolBook {
    title: string
    author: string
}

const POOL_FILE = path.join('scripts', 'data', 'libros-pool.json')

function parseArgs() {
    const args = process.argv.slice(2)
    const idx = args.indexOf('--chunks')
    const chunks = idx >= 0 ? parseInt(args[idx + 1], 10) : 16
    if (!Number.isFinite(chunks) || chunks < 2) {
        throw new Error('--chunks debe ser un entero >= 2')
    }
    return { chunks }
}

function main() {
    const { chunks } = parseArgs()

    const raw = readFileSync(POOL_FILE, 'utf8')
    const pool = JSON.parse(raw) as PoolBook[]
    if (!Array.isArray(pool) || pool.length === 0) {
        throw new Error(`Pool vacío o inválido: ${POOL_FILE}`)
    }

    // Orden lexicográfico (mismo que aplica Mongo para range sharding).
    const titulosOrdenados = pool
        .map((b) => b.title)
        .sort((a, b) => (a < b ? -1 : a > b ? 1 : 0))

    const N = titulosOrdenados.length
    const tamPorChunk = N / chunks

    // (chunks - 1) puntos de corte → N chunks
    const splits: { title: string; cumIdx: number; rangeStart: string }[] = []
    let prev = titulosOrdenados[0]
    for (let i = 1; i < chunks; i++) {
        const idx = Math.floor(i * tamPorChunk)
        const titulo = titulosOrdenados[idx]
        splits.push({ title: titulo, cumIdx: idx, rangeStart: prev })
        prev = titulo
    }

    console.log(`Pool:           ${N.toLocaleString()} títulos únicos`)
    console.log(`Chunks objetivo: ${chunks}  (~${Math.round(tamPorChunk).toLocaleString()} títulos/chunk)`)
    console.log(`\n--- Puntos de corte (sh.splitAt) ---\n`)
    console.log(`use book_libre`)
    console.log(`sh.startBalancer()   // para que reparta los chunks vacíos`)
    console.log(``)
    for (const s of splits) {
        // Comilla doble + escape de comillas internas (raro pero por las dudas)
        const safe = s.title.replace(/\\/g, '\\\\').replace(/"/g, '\\"')
        console.log(`sh.splitAt("book_libre.books", { title: "${safe}", bookId: MinKey })`)
    }

    console.log(`\n--- Distribución resultante esperada ---\n`)
    const labels = ['MinKey', ...splits.map((s) => `"${s.title.slice(0, 18)}${s.title.length > 18 ? '…' : ''}"`)]
    const cumIdx = [0, ...splits.map((s) => s.cumIdx), N]
    for (let i = 0; i < chunks; i++) {
        const desde = labels[i] || 'MinKey'
        const hasta = i < chunks - 1 ? labels[i + 1] : 'MaxKey'
        const docs = cumIdx[i + 1] - cumIdx[i]
        const pct = ((docs / N) * 100).toFixed(1)
        console.log(`  chunk ${String(i + 1).padStart(2)}: [${desde}, ${hasta})  →  ${docs.toLocaleString().padStart(6)} títulos  (${pct}%)`)
    }

    console.log(`\nNota: cada "título único" del pool se inserta ~${Math.round(500_000 / N)} veces`)
    console.log(`      en el bulk de 500k → la cantidad de DOCS por chunk será proporcional.`)
}

main()
