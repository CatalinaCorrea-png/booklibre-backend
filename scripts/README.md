# MongoDB Sharding — Setup y carga de datos

Esta carpeta contiene los scripts para levantar y poblar el cluster MongoDB
shardeado que reemplaza a la base mono-nodo del proyecto.

## Arquitectura del cluster

El `docker-compose.yml` del root levanta **10 contenedores Mongo** que conforman
un cluster shardeado:

- **2 routers (`mongos`)** — `router-01` (puerto host `27117`), `router-02`
  (`27118`). La app y los clientes se conectan acá; el router decide a qué shard
  enrutar cada operación.
- **3 config servers** (replica set `rs-config-server`): `mongo-config-01/02/03`.
  Guardan los metadatos del cluster (qué chunk vive en qué shard).
- **Shard 01** (replica set `rs-shard-01`): 3 nodos `shard-01-node-a/b/c`.
- **Shard 02** (replica set `rs-shard-02`): 3 nodos `shard-02-node-a/b/c`.

La app Spring Boot apunta a `mongodb://127.0.0.1:27117/book_libre` (ver
`src/main/resources/application.yml`).

## Setup paso a paso

### 1. Levantar el cluster

```powershell
docker compose up -d
```

Esperar ~15 segundos a que todos los `mongod` estén escuchando.

### 2. Inicializar los replica sets internos

```powershell
# Config servers
docker exec -it mongo-config-01 mongosh --port 27017 --file /scripts/init-configserver.js

# Shard 01
docker exec -it shard-01-node-a mongosh --port 27017 --file /scripts/init-shard01.js

# Shard 02
docker exec -it shard-02-node-a mongosh --port 27017 --file /scripts/init-shard02.js
```

Esperar otros 10-15 segundos a que cada replica set elija PRIMARY.

### 3. Registrar los shards en el router

```powershell
docker exec -it router-01 mongosh --port 27017 --file /scripts/init-router.js
```

### 4. Verificar el cluster

```powershell
docker exec -it router-01 mongosh --port 27017 --eval "sh.status()"
```

Deberías ver los dos shards listados (`rs-shard-01`, `rs-shard-02`) y dos
routers activos.

**Verificación adicional desde el host de Windows** (importante, ver sección
"Bug de Docker Desktop" más abajo):

```powershell
docker exec -it router-01 mongosh "mongodb://host.docker.internal:27117/admin" --eval "db.hello().msg"
```

Tiene que devolver `isdbgrid`. Si devuelve cualquier otra cosa o vacío, el
port forwarding está cruzado — leé la sección de troubleshooting al final.

### 5. Habilitar sharding en la colección `books`

Conectarse al router y ejecutar:

```javascript
use book_libre
sh.enableSharding('book_libre')
db.books.createIndex({ bookId: "hashed" })
sh.shardCollection('book_libre.books', { bookId: "hashed" })
```

### 6. Levantar la app Spring Boot

El bootstrap inserta los libros curados iniciales (24 docs) en la colección
ya shardeada. Confirmá que ves `Tomcat started on port 8080` y los logs
`Book XXX creado` en consola.

### 7. Generar 500k libros ficticios

```powershell
# Dry-run: solo genera en memoria, NO inserta. Útil para verificar el shape.
npx tsx scripts/generar-libros.ts

# Insertar de verdad (mantené la app corriendo en paralelo)
npx tsx scripts/generar-libros.ts --insertar
```

Tarda ~2 minutos. Para llegar a 1M, ejecutalo dos veces. Con 500k vas a tener
una distribución más limpia (~50/50) para los screenshots; con 1M el
balancer puede tardar más en estabilizar.

## Elección del shard key: `{ bookId: "hashed" }`

### Por qué `bookId`
- Es un **UUID** generado en la app, con altísima cardinalidad. Eso garantiza
  que el hash distribuya parejo entre los shards (no se forman "jumbo chunks").
- El detalle de un libro individual (`findById(bookId)`) es una de las consultas
  más frecuentes. Con hash sobre `bookId`, esa consulta es **targeted**: el
  router calcula el hash, sabe a qué shard ir, y contacta a uno solo.

### Por qué hash y no rango
- Las consultas a `books` filtran por criterios muy variados (título, género,
  ISBN, rango de páginas, dueño) con paginación. **Ningún campo se usa siempre
  como filtro de rango**, así que el sharding por rango no aportaría localidad
  de datos.
- Con UUIDs monotónicos o cuasi-ordenados, un sharding por rango concentraría
  todos los inserts nuevos en el último shard ("hot shard"). El hash evita
  ese problema por construcción.

### Trade-off
- Las consultas que NO filtran por `bookId` (la mayoría: por género, título,
  paginación) son **scatter-gather**: el router pregunta a todos los shards y
  agrega los resultados. Es más costoso que una targeted query pero se ejecuta
  en paralelo, así que con 2 shards se aprovecha el ancho de banda.

## Queries de ejemplo (targeted vs scatter-gather)

El script `buscar-libros.js` ejecuta una targeted query y una scatter-gather
en la misma colección, y para cada una imprime qué shards consultaría el
router.

```powershell
docker exec -it router-01 mongosh --port 27017
```
```javascript
load('/scripts/buscar-libros.js')
```

Detalles de implementación importantes:

- Usa **`explain("queryPlanner")`** en lugar de `"executionStats"`. La
  diferencia es clave: `queryPlanner` **solo planifica** la query (instantáneo),
  mientras que `executionStats` **la ejecuta entera**. Con 1M documentos y sin
  índice secundario sobre `gender` o `title`, ejecutar la scatter-gather
  completa puede tardar minutos.
- El nombre del shard puede aparecer en distintos puntos del plan según la
  versión de Mongo. El helper `shardsDe()` del script recorre el árbol del
  `explain()` y junta todos los `shardName` que encuentra, así funciona en
  Mongo 5/6/7/8.

Lo que vas a ver en la salida:

- **Targeted (`{ bookId: "<uuid>" }`)**: una sola entrada
  (`rs-shard-01` o `rs-shard-02`, según el hash del bookId).
- **Scatter-gather (`{ gender: "DRAMA" }`)**: las dos entradas
  (`rs-shard-01` y `rs-shard-02`) porque el router no puede deducir el shard
  a partir del filtro.

### Salida esperada (ejemplo real)

Esto fue lo que devolvió el script en una corrida con el dataset de 500.024
libros (24 del bootstrap + 500.000 generados). El bookId concreto cambia en
cada ejecución porque se elige al azar con `$sample`.

```
=== 1. TARGETED QUERY por bookId ===
bookId de muestra: c3f0e427-9f2e-425c-b366-3efe163c572d
Documento encontrado:
{
  _id: ObjectId('6a0f51cfce12ffb1d6387b67'),
  _class: 'ar.edu.unsam.phm.domain.Collectable',
  bookId: 'c3f0e427-9f2e-425c-b366-3efe163c572d',
  title: "The Handmaid's Tale",
  ...
}

Shards consultados (esperado: 1):
[ 'rs-shard-02' ]

=== 2. SCATTER-GATHER por género ===
Shards consultados (esperado: 2):
[ 'rs-shard-02', 'rs-shard-01' ]

Primeros 3 títulos de género DRAMA:
  - El Proceso
  - Crimen y Castigo
  - El Extranjero

=== 3. Distribución entre shards ===
Total de documentos:
  500024

Chunks por shard (config.chunks):
  rs-shard-01: 1 chunk(s)
  rs-shard-02: 1 chunk(s)
```

Las claves del resultado para el TP:

1. La **targeted query** consultó **un solo shard** — el hash del `bookId`
   determina inequívocamente dónde está el documento.
2. La **scatter-gather** consultó **los dos shards** — el router no puede
   inferir la ubicación a partir de `gender`.
3. Los **chunks están balanceados** (1 por shard).

Complementariamente, `db.books.getShardDistribution()` devolvió 250.017 docs
en rs-shard-01 (49.99%) y 250.007 en rs-shard-02 (50%) — distribución casi
perfecta gracias al hash sobre UUID.

## Verificación de distribución

El script también imprime, al final, el conteo total y el número de chunks
por shard (leyendo directo de `config.chunks`). Para ver además el espacio
en MiB por shard, hay que correr **manualmente** este comando en mongosh
después del `load()`:

```javascript
db.books.getShardDistribution()
```

Lo corremos a mano porque `getShardDistribution()` formatea su salida vía
side-effect en stdout y desde un script cargado con `load()` solo se ve su
valor de retorno (`true`), no la tabla bonita.

Con hash sobre UUID y volumen grande, la distribución entre shards debería
quedar cercana a **50/50**. Si ves un sesgo (ej. 70/30) recién después de
insertar, esperá 5-10 minutos a que el balancer parta los chunks grandes y
migre. Para confirmar que el balancer está activo:

```javascript
sh.isBalancerRunning()    // true mientras migra chunks
sh.getBalancerState()     // true = habilitado
```

## Resultados experimentales

Esta sección documenta los resultados obtenidos en una corrida real del setup
descrito arriba. Sirve como referencia para validar que el cluster del equipo
está funcionando correctamente y como evidencia para el informe del TP.

### Estado del cluster (`sh.status()`)

- **2 shards registrados**:
  - `rs-shard-01` con 3 nodos (`shard01-a/b/c`)
  - `rs-shard-02` con 3 nodos (`shard02-a/b/c`)
- **2 routers activos** (`router-01` en puerto 27117, `router-02` en 27118)
- **Balancer**: habilitado, idle (sin migraciones pendientes), 1 migración
  exitosa registrada en las últimas 24 horas.
- **Colección `book_libre.books`** shardeada con:
  - `shardKey: { bookId: "hashed" }`
  - 2 chunks (uno por shard), divididos en `bookId: Long(0)`.

### Distribución de documentos (`db.books.getShardDistribution()`)

Sobre un dataset de **500.024 libros** (24 del bootstrap + 500.000 generados
por Faker):

| Shard | Documentos | % | Tamaño |
|---|---|---|---|
| `rs-shard-01` | 250.017 | 49,99 % | 255,98 MiB |
| `rs-shard-02` | 250.007 | 50,00 % | 256,04 MiB |
| **Total** | **500.024** | **100 %** | **512,03 MiB** |

Diferencia entre shards: **10 documentos** (≈ 0,002 %). La distribución es
prácticamente perfecta gracias a la alta cardinalidad de los UUIDs usados
como `bookId` combinada con la función hash.

### Comportamiento de las queries (`buscar-libros.js`)

Salida resumida del script (el bookId concreto cambia en cada ejecución
porque se elige al azar con `$sample`):

```
=== 1. TARGETED QUERY por bookId ===
bookId de muestra: c3f0e427-9f2e-425c-b366-3efe163c572d
Shards consultados (esperado: 1):
[ 'rs-shard-02' ]

=== 2. SCATTER-GATHER por género ===
Shards consultados (esperado: 2):
[ 'rs-shard-02', 'rs-shard-01' ]

Primeros 3 títulos de género DRAMA:
  - El Proceso
  - Crimen y Castigo
  - El Extranjero

=== 3. Distribución entre shards ===
Total de documentos: 500024
Chunks por shard:
  rs-shard-01: 1 chunk(s)
  rs-shard-02: 1 chunk(s)
```

Lecturas:

- Una consulta por **shard key** (`bookId`) le pega a **un solo shard**.
- Una consulta por un campo cualquiera (`gender`) le pega a **los dos**.
- Los títulos retornados (`El Proceso`, `Crimen y Castigo`, `El Extranjero`)
  son del bootstrap curado; conviven sin conflictos con los generados.

### Latencias HTTP end-to-end (frontend → backend → Mongo)

Mediciones tomadas desde el panel Network del navegador (Firefox DevTools),
con el frontend de React/Vite (`localhost:5173`) hablando con el backend
Spring (`localhost:8080`) sobre el cluster shardeado.

**Login y carga del listado:**

| Request | Tipo Mongo | Tiempo | Notas |
|---|---|---|---|
| `POST /api/auth` | (Postgres) | 1,03 s | Login con `emilia@example.com`. Devuelve JWT. |
| `GET /filtered-books?...` | **scatter-gather** | **~3,45 s** | Listado paginado sobre 500k docs. El router consulta los dos shards. |
| `GET /book-genders` | targeted (cacheable) | ~395 ms | Lista de géneros del enum. |

**Navegación al detalle de un libro (`/book-detail/6a0f51c6ce12ffb1d637c248`):**

| Request | Tipo Mongo | Tiempo | Notas |
|---|---|---|---|
| `GET /book-detail/{bookId}?userId=...` | **targeted** | **51 ms** | Detalle del libro. El router calcula hash del `bookId` y va directo al shard. |
| `GET /book-review/{bookId}?page=0&...` | targeted | 73–75 ms | Reviews del libro (mismo `bookId`). |
| `GET /book-detail/{bookId}/bibliokarmas` | targeted | 33–54 ms | Cálculo de bibliokarmas para una reserva. |
| `POST /book-detail/{bookId}/click` | targeted (insert) | 247 ms | Registra el click del usuario. |
| `GET .../dates` | (Postgres) | 60–121 ms | Fechas de reservas existentes. |

### Conclusión cuantitativa

| Operación | Latencia |
|---|---|
| Listado paginado (scatter-gather) | ~3.450 ms |
| Detalle de libro (targeted) | ~51 ms |
| **Speedup del shard key** | **≈ 68x** |

La consulta más sensible a la UX (abrir el detalle de un libro) es ~68 veces
más rápida que el listado, porque el router puede determinar el shard exacto
a partir del `bookId` sin consultar a los demás. Esto valida la elección de
`{ bookId: "hashed" }` como shard key: maximiza el porcentaje de **targeted
queries** sobre el conjunto de operaciones reales del sistema.

## Archivos en esta carpeta

| Archivo | Propósito |
|---|---|
| `init-configserver.js` | Inicializa el replica set de los config servers. |
| `init-shard01.js` | Inicializa el replica set del shard 1. |
| `init-shard02.js` | Inicializa el replica set del shard 2. |
| `init-router.js` | Registra los dos shards en el router. |
| `generar-libros.ts` | Genera (y opcionalmente inserta) 500.000 libros con Faker. |
| `buscar-libros.js` | Demuestra targeted vs scatter-gather con `explain()`. |

## Dependencias del generador (Node)

Instalación local (una sola vez):

```powershell
pnpm add -D tsx typescript @types/node @faker-js/faker mongodb dotenv
```

Variables de entorno opcionales (defaults entre paréntesis):

- `MONGODB_URI` (`mongodb://localhost:27117`)
- `DB_NAME` (`book_libre`)
- `COLLECTION_NAME` (`books`)

## Configuración del backend Spring (`application.yml`)

La app conecta a Postgres y al router de Mongo. Los settings que tienen que
estar en `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/booklibre?sslmode=disable
    username: postgres
    password: postgres
  data:
    mongodb:
      uri: mongodb://127.0.0.1:27117/book_libre
```

Notas:

- **`?sslmode=disable`** en la URL de Postgres: el JDBC driver por defecto
  intenta SSL y el container de Postgres no lo tiene configurado, lo que
  causa un `EOFException` durante el handshake. Disable es lo correcto en
  desarrollo local.
- **Puerto `27117`** en la URI de Mongo: es el puerto del router (no de un
  config server ni de un shard directo). La app NUNCA debe pegarle directo
  a un shard, siempre al router.
- **Sin credenciales en Mongo**: el cluster no tiene auth habilitada (las
  env vars `MONGO_INITDB_ROOT_*` están comentadas en el `docker-compose.yml`
  porque no aplican a un setup shardeado; eso requiere keyFile). Para un TP
  académico se trabaja sin auth.

## Bootstrap idempotente

El `ProjectBootstrap.kt` corre cada vez que arranca la app y crea usuarios,
autores, libros curados (24) y reservas iniciales. Tres detalles que tuvimos
que ajustar para que conviva con un dataset grande generado por Faker:

1. **No borrar la colección de libros**: la línea `repoBooks.deleteAll()` está
   comentada (línea ~1502). Si la dejás activa, el bootstrap te borra los
   500k–1M libros generados en cada restart.
2. **`createBook` usa `findFirstByTitle`** (en lugar de `findByTitle`): Faker
   puede generar libros con título idéntico a los curados (ej. "1984"). El
   `findByTitle` original exige resultado único y rompe; `findFirstByTitle`
   tolera duplicados y devuelve cualquiera.
3. **`MongoBookRepository.findFirstByTitle`** se agregó al repo para soportar
   el punto anterior.

## Troubleshooting

### Bug de Docker Desktop en Windows: port forwarding cruzado

**Síntoma**: la app Spring no puede escribir a Mongo, error
`MongoNotPrimaryException: not primary`. El log muestra que `127.0.0.1:27117`
responde como un secondary de `rs-config-server`, no como un mongos.

**Causa**: tras reiniciar Docker Desktop, ocasionalmente el port forwarding de
host queda mal mapeado y el puerto del router (27117) termina apuntando a un
config server.

**Diagnóstico**: corré esto desde el host (no desde dentro del container,
porque eso bypassea el port forwarding):

```powershell
docker exec -it router-01 mongosh "mongodb://host.docker.internal:27117/admin" --eval "db.hello().msg"
```

Si devuelve `isdbgrid` → todo OK. Si devuelve vacío o cualquier otra cosa
(ej. info de `rs-config-server`), el mapeo está roto.

**Fix**: recrear los routers:

```powershell
docker compose stop router01 router02
docker compose rm -f router01 router02
docker compose up -d router01 router02
```

Esto NO toca volumes; metadata del cluster y data persisten. Re-verificá con
el mismo comando de antes.

### El error `EOFException` durante auth de Postgres

Si la app Spring no puede conectar a Postgres y el stack incluye
`Caused by: java.io.EOFException ... PgStream.receiveChar`, es el handshake
SSL. Fix: `?sslmode=disable` en la URL JDBC (ver sección de configuración).

### El error `NotPrimary` después de inicializar el sharding

Si al ejecutar `init-router.js` o `sh.addShard` ves `not primary`, es que los
replica sets internos todavía no terminaron de elegir PRIMARY. Esperá 10-20
segundos más entre los pasos 2 y 3 del setup y reintentá.
