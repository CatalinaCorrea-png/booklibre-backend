// Consultas de ejemplo sobre la colección shardeada `books`.
//
// Ejecutar conectado al router:
//   docker exec -it router-01 mongosh --port 27017
//   load('/scripts/buscar-libros.js')
//
// Notas:
// - Usamos explain("queryPlanner") en lugar de "executionStats" porque solo
//   nos interesa MOSTRAR qué shards consultaría el router. queryPlanner es
//   instantáneo; executionStats ejecuta la query completa (lento sin índices
//   con 1M docs).

db = db.getSiblingDB('book_libre')

// Helper: recorre el explain() y devuelve los nombres de shards encontrados.
// Funciona en distintas versiones de Mongo porque busca cualquier `shardName`
// dentro del árbol del plan (puede estar bajo queryPlanner.winningPlan.shards,
// bajo executionStats.executionStages, etc.).
function shardsDe(cursor) {
    const plan = cursor.explain("queryPlanner")
    const encontrados = new Set()
    const visitar = (obj) => {
        if (!obj || typeof obj !== 'object') return
        if (Array.isArray(obj)) { obj.forEach(visitar); return }
        for (const [k, v] of Object.entries(obj)) {
            if (k === 'shardName' && typeof v === 'string') encontrados.add(v)
            else visitar(v)
        }
    }
    visitar(plan)
    return encontrados.size > 0 ? [...encontrados] : ["(ningún shardName en el plan)"]
}

// Tomamos un bookId real al azar para la targeted query.
const sample = db.books.aggregate([{ $sample: { size: 1 } }]).toArray()[0]
const sampleBookId = sample ? sample.bookId : null

// ─── 1. TARGETED QUERY: por bookId (shard key con hash) ──────────────────────
// El router calcula el hash del bookId y va DIRECTO a un solo shard.
print("\n=== 1. TARGETED QUERY por bookId ===")
print(`bookId de muestra: ${sampleBookId}`)
print("Documento encontrado:")
printjson(db.books.findOne({ bookId: sampleBookId }))
print("\nShards consultados (esperado: 1):")
printjson(shardsDe(db.books.find({ bookId: sampleBookId })))

// ─── 2. SCATTER-GATHER: por género (NO es shard key) ─────────────────────────
// El router no puede deducir en qué shard vive cada match → pregunta a todos.
print("\n=== 2. SCATTER-GATHER por género ===")
print("Shards consultados (esperado: 2):")
printjson(shardsDe(db.books.find({ gender: "DRAMA", deleted: false }).limit(10)))

print("\nPrimeros 3 títulos de género DRAMA:")
db.books.find({ gender: "DRAMA", deleted: false }, { title: 1, _id: 0 })
    .limit(3)
    .forEach(d => print(`  - ${d.title}`))

// ─── 3. Distribución entre shards ────────────────────────────────────────────
// getShardDistribution() solo imprime bien en modo interactivo. Acá calculamos
// el conteo por shard usando $shard del aggregation framework, vía
// $$REMOVE / $$SHARD_KEY no funciona en versiones viejas, así que usamos
// la API de admin sobre la colección config.chunks para mostrar chunks por shard.
print("\n=== 3. Distribución entre shards ===")
print("\nTotal de documentos:")
print(`  ${db.books.countDocuments()}`)

print("\nChunks por shard (config.chunks):")
const collInfo = db.getSiblingDB("config").collections.findOne({ _id: "book_libre.books" })
if (collInfo) {
    db.getSiblingDB("config").chunks.aggregate([
        { $match: { uuid: collInfo.uuid } },
        { $group: { _id: "$shard", chunks: { $sum: 1 } } },
        { $sort: { _id: 1 } }
    ]).forEach(r => print(`  ${r._id}: ${r.chunks} chunk(s)`))
} else {
    print("  (no se encontró metadata de book_libre.books en config)")
}

print("\nPara ver el detalle MiB y docs por shard, correr esto manualmente:")
print("  db.books.getShardDistribution()")
