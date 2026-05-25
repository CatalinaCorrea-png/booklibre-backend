# MongoDB Sharding — Range sharding sobre `{ title: 1, bookId: 1 }`

Esta carpeta documenta el experimento de **range sharding** de la colección
`book_libre.books`. Es la segunda iteración del trabajo de sharding del TP: la
primera, en la rama `test/mongo-sharding`, usó **hash sharding** sobre
`{ bookId: "hashed" }`. Esta rama (`test/mongo-sharding-range`) reemplaza esa
estrategia por un **shard key compuesto por rango** y compara los dos enfoques.

## Arquitectura del cluster

El `docker-compose.yml` levanta **11 contenedores Mongo** que conforman un
cluster shardeado:

- **2 routers (`mongos`)** — `router-01` (puerto host `27117`), `router-02`
  (`27118`). La app y los clientes se conectan al router; el router decide
  qué shard contactar.
- **3 config servers** (replica set `rs-config-server`): `mongo-config-01/02/03`.
  Guardan los metadatos del cluster (qué chunk vive en qué shard).
- **Shard 01** (replica set `rs-shard-01`): 3 nodos `shard-01-node-a/b/c`.
- **Shard 02** (replica set `rs-shard-02`): 3 nodos `shard-02-node-a/b/c`.

La app Spring Boot apunta a `mongodb://127.0.0.1:27117/book_libre` (ver
`src/main/resources/application.yml`).

## Elección del shard key: `{ title: 1, bookId: 1 }`

### Por qué range y no hash

La rama hash había validado que `bookId` hasheado es el mejor key para
**consultas puntuales** (`findById`). Pero el caso de uso más visible de la
app es el **listado paginado de libros con filtro/sort por título** — y ahí
el hash no aporta nada, porque docs con títulos contiguos quedan dispersos
aleatoriamente entre los shards.

Con range sharding sobre `title`, los libros con títulos cercanos
alfabéticamente quedan **físicamente juntos** en el mismo chunk (y por ende
en el mismo shard). Eso permite:

- **Targeted queries por título exacto** (`{ title: "Italian Hours" }`) — el
  router calcula a qué chunk pertenece el title y consulta **un solo shard**.
- **Range queries acotadas** (`{ title: { $gte: "I", $lt: "J" } }`) que caen
  dentro de un chunk se enrutan a un solo shard.
- **Sort por título** sin merge de resultados de múltiples shards (cuando se
  acota suficiente).

### Por qué compuesto (`title` + `bookId`) y no solo `title`

Si la cardinalidad del prefijo es baja, MongoDB no puede splittear chunks
que tengan muchos documentos con el mismo valor de shard key — se forman
**jumbo chunks** que no se balancean. En este dominio, varios libros pueden
compartir título (`"1984"`, `"Hamlet"`, etc.), así que `title` solo es
peligroso.

Agregando `bookId` (un UUID con altísima cardinalidad) como segundo campo:

- El shard key compuesto se vuelve **único** (no hay dos docs con el mismo
  `{title, bookId}`).
- MongoDB puede splittear cualquier chunk arbitrariamente.
- Las queries que filtran por `title` solo siguen siendo targeted, porque
  `title` es **prefijo** del shard key.

### Trade-off frente a hash

| Consulta | Hash sobre `bookId` | Range sobre `{title, bookId}` |
|---|---|---|
| `findById(bookId)` | targeted (1 shard) | scatter (`bookId` no es prefijo) |
| `find({ title: "X" })` | scatter | **targeted** (1 shard) |
| `find({ title: /^A/ })` | scatter | targeted o pocos shards |
| Sort por título | merge de N shards | local al shard |
| Distribución automática | sí (hash uniforme) | **NO** (requiere pre-split) |

## Pre-splitting: la diferencia operativa con hash

Con hash, `sh.shardCollection` **pre-splittea automáticamente** y arranca con
varios chunks repartidos. Con range, arranca con **un solo chunk
`[MinKey, MaxKey)` en el primary del DB**. Todos los inserts caen ahí hasta
que ese chunk llegue al threshold de split (~128 MB) y el balancer lo parta.

Eso **satura al shard primary** durante el bulk insert inicial. En nuestro
caso aparecía un timeout consistente al llegar a ~300k documentos: el
mongos quedaba bloqueado esperando acks de writeConcern del único shard que
recibía todo, y el heartbeat SDAM del driver no podía hacerse escuchar.

### Solución

1. Calcular puntos de corte (split keys) basados en la **distribución real**
   de títulos que vamos a insertar.
2. Aplicar `sh.splitAt()` para que la colección vacía tenga N chunks
   pre-creados (en nuestro caso 16).
3. **Mover manualmente** la mitad de los chunks al otro shard con
   `sh.moveChunk()`, **con el balancer apagado**.
4. Insertar.
5. Encender el balancer al final.

### Por qué el balancer tiene que estar apagado durante el split

MongoDB 6+ incluye un **auto-merger** que, cuando el balancer está activo,
mergea chunks vacíos contiguos que viven en el mismo shard. Si encendemos
el balancer antes de mover chunks a shard-01, **el merger nos deshace los
splits**: los 16 chunks vacíos en shard-02 se recombinan en 1.

Lo verificamos en vivo durante el experimento: con balancer encendido,
los 15 `sh.splitAt()` retornaban `ok: 1` pero `db.books.getShardDistribution()`
seguía mostrando 1 chunk. Apagando el balancer, cada `splitAt` quedaba
persistido. Por eso el flow correcto es **split + moveChunk con balancer
apagado**, encender balancer **después** del bulk insert (cuando los chunks
ya tienen datos y no son candidatos al merge).

## Setup paso a paso

### 1. Levantar el cluster

```powershell
docker compose up -d
```

### 2. Inicializar los replica sets internos

```powershell
docker exec -it mongo-config-01 mongosh --port 27017 --file /scripts/init-configserver.js
docker exec -it shard-01-node-a mongosh --port 27017 --file /scripts/init-shard01.js
docker exec -it shard-02-node-a mongosh --port 27017 --file /scripts/init-shard02.js
```

Esperar 10-15 segundos a que cada replica set elija PRIMARY.

### 3. Registrar los shards en el router

```powershell
docker exec -it router-01 mongosh --port 27017 --file /scripts/init-router.js
```

### 4. Calcular puntos de corte (split keys)

Antes de habilitar sharding, calculamos los puntos de corte usando la
distribución real del pool de libros de Open Library:

```powershell
npx tsx scripts/calcular-splits.ts
```

El script lee `scripts/data/libros-pool.json` (76.750 títulos únicos),
ordena lexicográficamente y divide en 16 chunks de tamaño similar. Imprime
los 15 `sh.splitAt(...)` listos para pegar.

### 5. Habilitar sharding + pre-splittear + balancear

```powershell
docker exec -it router-01 mongosh --port 27017
```

Dentro del shell:

```javascript
use book_libre

// Asegurar balancer apagado ANTES de splittear (ver "auto-merger" arriba)
sh.stopBalancer()
sh.getBalancerState()    // false

// Habilitar sharding
sh.enableSharding("book_libre")
sh.shardCollection("book_libre.books", { title: 1, bookId: 1 })

// Pegar acá los 15 sh.splitAt() generados por calcular-splits.ts
sh.splitAt("book_libre.books", { title: "Analecta tragica Graeca", bookId: MinKey })
// ... etc (15 cortes en total)

// Verificar 16 chunks en shard-02 (todos en el primary):
db.books.getShardDistribution()

// Mover 8 chunks pares manualmente a shard-01:
sh.moveChunk("book_libre.books", { title: "Analecta tragica Graeca", bookId: MinKey }, "rs-shard-01")
sh.moveChunk("book_libre.books", { title: "Deal of a Lifetime", bookId: MinKey }, "rs-shard-01")
// ... etc (8 movimientos)

// Verificar 8/8:
db.books.getShardDistribution()
```

### 6. Bulk insert con balancer apagado

```powershell
npx tsx scripts/generar-libros.ts --insertar
```

El script inserta 500.000 docs en batches de 2.000 con backpressure (25 ms
entre batches) para no saturar el router. Tarda ~70-90 segundos. Como los
chunks ya están pre-distribuidos 8/8, los inserts caen ~50/50 desde el
primer batch.

### 7. Encender el balancer al final

```javascript
sh.startBalancer()
```

Con los chunks ya cargados, el auto-merger no los toca (solo mergea chunks
**vacíos** contiguos).

## Resultados experimentales

Sobre un dataset de **452.000 libros** (la corrida cortó a ~90% por OOM del
host de Docker, ver troubleshooting). La distribución relevante para el TP
es la que se logró, no el total absoluto:

### Distribución de documentos (`db.books.getShardDistribution()`)

| Shard | Documentos | % | Chunks | Tamaño |
|---|---|---|---|---|
| `rs-shard-01` | 225.255 | 49,83 % | 8 | 230,49 MiB |
| `rs-shard-02` | 226.745 | 50,16 % | 8 | 231,89 MiB |
| **Total** | **452.000** | **100 %** | **16** | **462,39 MiB** |

Diferencia entre shards: **1.490 documentos** (≈ 0,33 %). La distribución es
casi tan pareja como la de hash sobre UUIDs, pero **lograda sin depender del
balancer durante el insert** — los chunks se distribuyeron a mano antes de
que entraran datos.

### Comportamiento de las queries (con `explain("queryPlanner")`)

| # | Filtro | Shards consultados |
|---|---|---|
| 1 | `{ title: "Italian Hours" }` (prefijo del shard key, valor exacto) | `rs-shard-02` (1) |
| 2 | `{ title: { $gte: "Italian Q", $lt: "Italian Z" } }` (range dentro de **un** chunk) | `rs-shard-02` (1) |
| 3 | `{ title: { $gte: "A", $lt: "C" } }` (range que cruza chunks de ambos shards) | ambos (2) |
| 4 | `{ bookId: "..." }` (segundo campo del shard key, sin prefijo) | ambos (2) — **scatter** |
| 5 | `{ gender: "DRAMA" }` (campo no-key) | ambos (2) — **scatter** |

Lecturas clave para el TP:

- **Caso 1** es el objetivo del diseño: filtrar por título exacto es
  targeted, como debería ser un sistema con sharding por rango.
- **Caso 2** muestra que aún consultas por rango son targeted **si caen
  dentro de un solo chunk**.
- **Caso 3** ilustra el costo de haber distribuido chunks **alternados**
  entre shards (par→01, impar→02) durante el pre-split: chunks
  alfabéticamente adyacentes terminan en shards distintos, así que casi
  cualquier range no-trivial pega a los dos. Es un trade-off consciente del
  paso 5 del setup — mantiene el balance perfecto a costa de la localidad
  de range queries amplias.
- **Caso 4** es el costo principal frente a hash: `findById` (que en hash
  era targeted) acá es scatter, porque `bookId` no es prefijo del shard
  key compuesto.

## Comparación con hash sharding (rama `test/mongo-sharding`)

| Métrica | Hash `{ bookId: "hashed" }` | Range `{ title:1, bookId:1 }` |
|---|---|---|
| Dataset | 1.000.024 docs | 452.000 docs |
| Distribución | 49,99 % / 50,00 % | 49,83 % / 50,16 % |
| Chunks por shard | 1 / 1 | 8 / 8 |
| `findById(bookId)` | **targeted** | scatter |
| `findByTitle(title)` | scatter | **targeted** |
| `find({ title: /^A/ })` | scatter (todos los shards) | depende del rango (1-2 shards) |
| Setup operativo | `shardCollection` y listo | drop + splitAt × 15 + moveChunk × 8 + balancer dance |
| Riesgo de hot shard al insertar | ninguno | alto si NO se pre-splittea |
| Speedup observado (detalle vs listado) | ~68× (51 ms vs 3.450 ms) | medible cuando se hace listado por título acotado |

**Conclusión cualitativa**: hash es más fácil operativamente y mejor para
acceso puntual por id; range premia el patrón de acceso "listado/sort por
prefijo del key" pero exige mantenimiento manual del particionado en el
bulk inicial. Para esta app, donde el flujo más visible para el usuario
es la apertura del detalle (un `findById`), **hash gana en práctica**.
Range queda como ejercicio académico que ilustra los compromisos.

## Verificar comportamiento desde la línea de comandos

```powershell
docker exec router-01 mongosh --port 27017 --eval "load('/scripts/buscar-libros.js')"
```

El script `buscar-libros.js` imprime una targeted query, una scatter-gather
y la distribución por shard. Internamente usa `explain("queryPlanner")` (no
`"executionStats"`) para evitar ejecutar la query completa sobre 452k docs,
y un helper `shardsDe()` que recorre el árbol del plan para reportar los
`shardName` consultados de forma robusta entre versiones de Mongo.

## Archivos en esta carpeta

| Archivo | Propósito |
|---|---|
| `init-configserver.js` | Inicializa el replica set de los config servers. |
| `init-shard01.js` | Inicializa el replica set del shard 1. |
| `init-shard02.js` | Inicializa el replica set del shard 2. |
| `init-router.js` | Registra los dos shards en el router. |
| `fetch-libros-reales.ts` | Descarga 76.750 títulos reales de la Open Library API y los guarda en `data/libros-pool.json`. |
| `calcular-splits.ts` | Lee el pool y calcula 15 `sh.splitAt(...)` que parten la colección en 16 chunks de tamaño similar. |
| `generar-libros.ts` | Genera 500.000 libros tomando títulos del pool, los inserta en batches con backpressure. |
| `buscar-libros.js` | Demuestra targeted vs scatter con `explain()`. |
| `data/libros-pool.json` | Pool de títulos reales (no versionado por tamaño). |

## Configuración del backend Spring (`application.yml`)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27117/book_libre
```

Notas:

- **`127.0.0.1` y no `localhost`**: con `localhost`, Node.js (en
  `generar-libros.ts`) resuelve preferentemente a `[::1]` (IPv6) y algunas
  conexiones IPv6 al router se cuelgan en Windows. Forzar IPv4 evita ese
  problema.
- **Puerto `27117`** es el del router. La app NUNCA debe pegar directo a un
  shard.
- **Sin auth**: el cluster no tiene autenticación habilitada porque un setup
  shardeado con auth requiere `keyFile`, demasiado overhead para un TP.

## Bootstrap idempotente (Spring)

El `ProjectBootstrap.kt` corre cada vez que arranca la app y crea usuarios,
autores, libros curados (24) y reservas iniciales. Detalles relevantes:

1. **No borrar la colección de libros**: `repoBooks.deleteAll()` está
   comentada. Si la dejás activa, el bootstrap te borra los 452k libros
   generados en cada restart.
2. **`createBook` usa `findFirstByTitle`** (no `findByTitle`): Faker puede
   generar libros con título idéntico a los curados. `findByTitle` exige
   resultado único; `findFirstByTitle` tolera duplicados y devuelve
   cualquiera.

## Troubleshooting

### Auto-merger deshace los `splitAt` con balancer encendido

**Síntoma**: aplicás 15 `sh.splitAt(...)`, cada uno devuelve `ok: 1`, pero
`db.books.getShardDistribution()` reporta `chunks: 1`.

**Causa**: MongoDB 6+ tiene un auto-merger que recombina chunks vacíos
contiguos en el mismo shard cuando el balancer está activo.

**Fix**: `sh.stopBalancer()` antes de splittear, mover chunks con
`sh.moveChunk()` manualmente, y encender el balancer **después** del bulk
insert (cuando los chunks ya no están vacíos).

### Timeout del driver durante bulk insert sin pre-split

**Síntoma**: `MongoBulkWriteError: connection to 127.0.0.1:27117 timed out`
después de unos cientos de miles de inserts.

**Causa**: con range sharding sin pre-split, todos los inserts caen al
único chunk del primary shard. Saturan ese shard, mongos se bloquea
esperando acks, el heartbeat SDAM no se procesa, el driver marca topology
UNKNOWN y aborta.

**Fix**: pre-splittear como se describe en la sección 5 del setup, y usar
batchSize chico (2.000) con sleep entre batches en el script generador.

### OOM del host durante bulk insert

**Síntoma**: igual al de arriba, pero pasa a partir del 80-90 % del insert
y `docker ps` deja de responder (`502 Bad Gateway` del pipe del Docker
daemon). Los containers de Mongo siguen vivos cuando vuelve Docker.

**Causa**: 11 contenedores Mongo con WiredTiger cache + replicación 3× +
journals + el Node generador suman más RAM que la asignada a Docker
Desktop.

**Fix**: parar pgadmin (no se usa para nada en este experimento) antes del
insert para liberar RAM, o subir la memoria de Docker Desktop en
Settings → Resources. **No parar `booklibre_sql`**: aunque el experimento
de sharding sea sobre Mongo, la app Spring conecta a Postgres en el
arranque (JPA para users, reviews, reservations) y muere si no lo
encuentra.

```powershell
docker stop pgadmin4_container_booklibre
```

### Bug de Docker Desktop en Windows: port forwarding cruzado

**Pasa con bastante frecuencia** después de reiniciar Docker Desktop o
después de `docker stop`/`docker start` sobre cualquier container del
cluster. El TCP del host se establece, pero termina apuntando al container
equivocado. Tiene dos manifestaciones distintas según qué puerto se cruzó:

#### Caso A: el puerto del router de Mongo (27117) apunta a un shard

**Síntomas posibles**:

- La app Spring loguea `MongoNotPrimaryException: not primary` cuando
  intenta escribir.
- En los logs del cliente Mongo aparece `type=REPLICA_SET_SECONDARY,
  setName='rs-shard-02'` (o `rs-shard-01`, o `rs-config-server`) en vez de
  `setName=null` / `isdbgrid`. Es la pista clave: la app cree que está
  hablando con un mongos pero está hablando con un nodo del replicaset.
- El script `generar-libros.ts` puede tirar timeouts raros que no se
  explican por carga.

**Diagnóstico**:

```powershell
docker exec router-01 mongosh "mongodb://host.docker.internal:27117/admin" --quiet --eval "db.hello().msg"
```

Tiene que devolver `isdbgrid`. Si devuelve string vacío o algo distinto,
el mapeo está roto.

**Fix** — recrear los routers (no toca volumes; metadata y datos
persisten):

```powershell
docker compose stop router01 router02
docker compose rm -f router01 router02
docker compose up -d router01 router02
```

> Ojo con los nombres: los **services** del compose son `router01` /
> `router02` (sin guion), aunque los **containers** se llamen `router-01` /
> `router-02` (con guion). Lo mismo pasa con `db` (service) → `booklibre_sql`
> (container).

#### Caso B: el puerto de Postgres (5433) apunta a otro container

**Síntomas**:

- La app Spring no arranca, falla con
  `org.postgresql.util.PSQLException: El intento de conexión falló` y
  `Caused by: java.io.EOFException` durante el handshake.
- El stack incluye `ConnectionFactoryImpl.doAuthentication` —
  característico: el TCP conecta pero el protocolo de Postgres se rompe en
  el handshake porque del otro lado no hay un Postgres real.

**Diagnóstico** — primero verificar que **adentro** del container Postgres
sí responde:

```powershell
docker exec booklibre_sql pg_isready -U postgres
```

Si dice `accepting connections`, el problema es 100 % de port mapping del
host. Confirmá probando una conexión desde fuera del container.

**Fix**:

```powershell
docker compose stop db
docker compose rm -f db
docker compose up -d db
```

Esperá 5-10 segundos a que Postgres termine de levantar y reintentá
arrancar la app Spring.

> **No parar Postgres a propósito** para liberar RAM aunque el experimento
> sea Mongo-only: la app Spring conecta a Postgres en el arranque para JPA
> (users, reviews, reservations) y muere si no lo encuentra. El único
> container que se puede parar sin consecuencias es `pgadmin4_container_booklibre`.
