# Feed de Actividad Reciente (GraphQL) — Integrante 5

KPI para el tablero del administrador. Unifica en una sola lista **los últimos libros dados de alta**
y **las últimas reservas confirmadas**, ordenadas cronológicamente (descendente), mostrando el **top 5**.

La lista es **heterogénea**: combina dos tipos de evento distintos. Se modela con una **interface de GraphQL**
(`ActivityEvent`) con dos implementaciones: `NewBookEvent` y `NewReservationEvent`.

Convive con la API REST y con los demás KPIs GraphQL (conversión, calificaciones) — no reemplaza nada.

---

## 1. Flujo de punta a punta (resumen)

```
                 ┌─────────────── MongoDB (libros) ───────────────┐
                 │  findTop5ByDeletedFalseOrderByRegisteredAtDesc │
   GraphQL       │  → 5 libros más nuevos por registeredAt        │
 recentActivity ─┤                                                │
   (resolver)    │  ┌──────────── PostgreSQL (reservas) ─────────┐│
                 │  │ findTop5ByOrderByCreatedAtDesc (@EntityGraph)│
                 └──┴ → 5 reservas más nuevas por createdAt ──────┘
                          │
                          ▼
        unir (≤10) → ordenar por fecha desc (desempate por título) → take(5)
                          │
                          ▼
     [ NewBookEvent | NewReservationEvent ]  (date ISO con offset, user, bookTitle)
                          │
                          ▼  HTTP POST /graphql
                  ControlPanel.tsx  →  timeAgo(date)  →  card "Actividad reciente"
```

Cada fuente trae **a lo sumo 5** filas: como el feed final son 5, los 5 globales siempre
salen de la unión de "top 5 de cada lado". Nunca se traen colecciones enteras a memoria.

---

## 2. Decisión de diseño clave: dos campos de fecha distintos

El dominio ya tenía una fecha de "alta del libro". Para el feed hizo falta **otra** con más precisión.
Por eso conviven dos campos, y es la decisión que explica casi todo el comportamiento:

| Campo | Tipo | Para qué |
|---|---|---|
| `Book.createdAt` | `LocalDate` | Fecha de alta del libro (día). La usan **Home (caché), perfil "Mis libros" y el ordenamiento**. Es el campo de siempre. |
| `Book.registeredAt` | `LocalDateTime?` | **Exclusivo del feed.** Momento exacto del alta (con hora). Permite mostrar "recién" y ordenar altas del mismo día. |
| `Reservation.createdAt` | `LocalDateTime` | Momento en que se **confirmó** la reserva (distinto de `pickUpDate`, que es cuándo se retira). |

**¿Por qué un campo aparte (`registeredAt`) y no reutilizar `createdAt`?**
- `createdAt` es `LocalDate` (sin hora) y lo consumen otras pantallas (Home/perfil) vía el caché de Redis.
  Cambiarle el tipo rompía la deserialización del caché. Agregar un campo nuevo es **aditivo** y no toca nada de eso.
- El feed necesita **hora** para dos cosas: mostrar "recién" y ordenar dos altas del mismo día por el momento real.

**¿Por qué `registeredAt` es nullable (`LocalDateTime?`)?**
- Un libro viejo guardado **antes** de que existiera el campo no lo tiene. Si el default fuera `now()`,
  al leerlo aparecería como "recién dado de alta" en cada arranque (bug que tuvimos).
- Con default `null`: el libro viejo queda en `null`, y en Mongo los `null` **ordenan al final** del
  `OrderByRegisteredAtDesc` → no se cuela arriba del feed. Además el resolver hace fallback a `createdAt` por las dudas.

**¿Por qué `Reservation.createdAt` y no `pickUpDate`?**
- La consigna pide "reservas **confirmadas**". `pickUpDate` es cuándo se retira el libro (puede ser futuro);
  una reserva confirmada hoy para retirar el mes que viene es "actividad reciente" **hoy**, no en el futuro.

---

## 3. Modelo de datos (qué se agregó)

**`domain/Book.kt`**
```kotlin
var createdAt: LocalDate = LocalDate.now(),         // alta (día) — Home/perfil/orden
var registeredAt: LocalDateTime? = null,            // alta (con hora) — SOLO feed, nullable a propósito
```

**`domain/Reservation.kt`** (entidad Postgres)
```kotlin
var createdAt: LocalDateTime = LocalDateTime.now(), // cuándo se confirmó la reserva
```
> En Postgres (`ddl-auto: create-drop`) la columna se crea fresca en cada arranque, así que no hay filas legacy.

---

## 4. Schema GraphQL (`resources/schema/schema.graphqls`)

```graphql
type Query {
    # ...
    recentActivity: [ActivityEvent!]!
}

enum ActivityEventType { BOOK_REGISTERED, RESERVATION }

interface ActivityEvent {
    date: String!                  # ISO-8601 con offset; el front calcula el "hace X"
    typeEvent: ActivityEventType!
    user: String!
    bookTitle: String!
}

type NewBookEvent implements ActivityEvent { ... }          # user = owner que publicó
type NewReservationEvent implements ActivityEvent { ... }   # user = lector que reservó
```

`date` viaja como **String** (no se registra un scalar `DateTime`), consistente con el resto del schema.

**`graphql/ActivityEvent.kt`** — tipos Kotlin (`sealed interface` + 2 `data class`) y el
`@DgsTypeResolver` que resuelve el `__typename` de la interface (sin eso DGS no sabe qué tipo concreto devolver).

---

## 5. El resolver (`graphql/RecentActivityDataFetcher.kt`)

```kotlin
@DgsQuery
fun recentActivity(): List<ActivityEvent> {
    val bookEvents = bookRepository.findTop5ByDeletedFalseOrderByRegisteredAtDesc()
        .map { book ->
            val instant = book.registeredAt ?: book.createdAt.atStartOfDay()  // fallback libros viejos
            instant to NewBookEvent(date = instant.toIsoWithOffset(), user = book.owner.name, ...)
        }

    val reservationEvents = reservationRepository.findTop5ByOrderByCreatedAtDesc()
        .map { res -> res.createdAt to NewReservationEvent(date = ..., user = res.user.name, ...) }

    return (bookEvents + reservationEvents)
        .sortedWith(compareByDescending<Pair<LocalDateTime, ActivityEvent>> { it.first }
            .thenBy { it.second.bookTitle })     // desempate determinístico
        .take(FEED_SIZE)                          // 5
        .map { it.second }
}
```

Puntos finos:
- **Se ordena por `LocalDateTime` real, no por el String.** Mezclar fechas como texto fallaría.
- **Desempate por título**: si un libro y una reserva comparten fecha, el orden no queda librado a lo que devuelva la base.
- **`toIsoWithOffset()`**: serializa la fecha con offset de zona (`...-03:00`). Un `LocalDateTime.toString()`
  no lleva zona y el navegador lo interpretaría con **su** zona → fecha corrida. Con el offset, `new Date()` del
  front resuelve el instante exacto. **Esto arregla el problema de horario.**

---

## 6. Repositorios — los finders "top 5 más nuevos"

**`MongoBookRepository`**
```kotlin
fun findTop5ByDeletedFalseOrderByRegisteredAtDesc(): List<Book>
```

**`CrudReservationRepository`**
```kotlin
@EntityGraph(attributePaths = ["user"])
fun findTop5ByOrderByCreatedAtDesc(): List<Reservation>
```
> El `@EntityGraph` es **obligatorio**: `Reservation.user` es `LAZY` y el resolver lee `user.name`.
> Sin el fetch-join sería `LazyInitializationException`. (El `owner` del libro no lo necesita: es un `OwnerDTO`
> embebido en el documento de Mongo.)

---

## 7. Seed / Bootstrap (`bootstrap/ProjectBootstrap.kt`)

El bootstrap **no borra** la colección de Mongo (los libros creados por el usuario **persisten**). En cada
arranque mantiene el seed consistente:

- **`registeredAt` de los libros del seed**: se deriva de su fecha de alta histórica
  (`book.createdAt.atStartOfDay()`), así tienen una distribución temporal realista y distinta.
- **Dedup de duplicados del seed**: `createBook` usa `findAllByTitle`, se queda con una copia y borra las
  repetidas del **mismo título del seed**. Arregla los libros repetidos en el Home **sin tocar** los libros
  del usuario (que tienen otro título).
- **Reset de reservas embebidas**: cada libro del seed limpia sus reservas embebidas
  (`existing.reservations.clear()`) y `initBookReservationCount` las reconstruye desde Postgres. Sin esto se
  acumulaban en cada arranque y el libro nunca volvía a "disponible".
- **`createdAt` de las reservas del seed**: las reservas pasadas se confirman el día de retiro; las de pickUp
  futuro se mapean a una fecha **pasada y distinta** (para no empatar y para que cualquier reserva nueva real
  con `now()` quede siempre por encima en el feed).

Para los datos **creados desde la app** no hace falta nada: `BookService.createBook` estampa
`registeredAt = LocalDateTime.now()` y `ReservationService.createReservation` deja `createdAt = now()` por default.

---

## 8. Frontend (`frontend-2026-grupo2`)

La card "Actividad reciente" de `ControlPanel.tsx` ya existía con datos hardcodeados. Solo se cableó:

- **`src/domain/graphql/ActivityEvent.ts`** — tipo de la respuesta (`__typename`, `date`, `typeEvent`, `user`, `bookTitle`).
- **`src/services/graphql/graphqlService.ts`** — `getRecentActivity()` (mismo patrón que `getConversionRate`,
  pidiendo `__typename` para distinguir el tipo de evento).
- **`src/utils/formatDate.ts`** — helper `timeAgo(iso)` que convierte la fecha ISO en "recién" / "hace X min/h/d".
- **`src/pages/ControlPanel.tsx`** — se reemplazó el hardcode por la query y se mapea cada evento al shape
  `Activity` que ya renderiza `ActivityItem` (`alta` vs `reserva`).

No hizo falta tocar `ActivityItem` ni el render de la card.

---

## 9. Cómo se ve el feed según el dato

| Evento | Fecha que usa | Se ve como |
|---|---|---|
| Libro **nuevo** (creado en la app) | `registeredAt = now()` | "recién" / "hace X min", ordenado bien |
| Libro **del seed** | `registeredAt` histórico | "hace X días/meses" |
| Libro **viejo** sin `registeredAt` | `null` → ordena al final (fallback a `createdAt`) | no aparece como "nuevo" |
| Reserva **nueva** | `createdAt = now()` | "recién" |
| Reserva **del seed** | `createdAt` espejado al pasado | "hace X días" |

---

## 10. Cómo verificar

1. **Reiniciá el backend** (el bootstrap deja Mongo consistente y Postgres se resembra solo).
2. En el front, andá a **Control Panel** → card **Actividad reciente**.
3. Pruebas:
   - Creá un libro → aparece **arriba como "recién"**.
   - Reservá un libro del seed y reiniciá → vuelve a estar **disponible**.
   - Los **duplicados** del Home (ej. "Cien Años de Soledad" x2) desaparecen.

No hay cambios de configuración ni en el front para correrlo (GraphQL/CORS ya estaban andando por los otros KPIs).

---

## 11. Limitaciones conocidas

- El **reset de reservas** aplica a los libros del **seed**. Si reservás un libro que **vos mismo creaste**
  (libro de usuario) y reiniciás, esa reserva embebida no se limpia, porque el bootstrap solo procesa los del seed.
- El **dedup** es por **título exacto**. Dos libros con títulos casi iguales (ej. `holaa` vs `holaaa`) se tratan
  como distintos (se asume que son libros del usuario, se conservan).
- La fecha usa la **zona del servidor** (`ZoneId.systemDefault()`) para calcular el offset. En local (server y
  navegador en la misma máquina) es correcto; en un deploy conviene fijar la zona del servidor.

---

## 12. Archivos tocados

**Backend**
- `domain/Book.kt` — campos `createdAt` / `registeredAt`
- `domain/Reservation.kt` — campo `createdAt`
- `resources/schema/schema.graphqls` — query + interface + tipos + enum
- `graphql/ActivityEvent.kt` — tipos Kotlin + `@DgsTypeResolver`
- `graphql/RecentActivityDataFetcher.kt` — el resolver
- `repository/MongoBookRepository.kt` — `findTop5...OrderByRegisteredAtDesc`, `findAllByTitle`
- `repository/CrudReservationRepository.kt` — `findTop5...OrderByCreatedAtDesc` (`@EntityGraph`)
- `bootstrap/ProjectBootstrap.kt` — seed de `registeredAt`/`createdAt`, dedup, reset de reservas
- `services/BookService.kt` — estampa `registeredAt` al crear
- `test/.../RecentActivityDataFetcherTest.kt` — tests del resolver (mockk)

**Frontend**
- `domain/graphql/ActivityEvent.ts`
- `services/graphql/graphqlService.ts`
- `utils/formatDate.ts` (`timeAgo`)
- `pages/ControlPanel.tsx`

<br>

---
---

# PARTE II — Teoría: GraphQL desde cero

> Esta parte es para **aprender**. No hace falta saber nada de GraphQL para leerla.
> Cada concepto se aterriza en el código real de este proyecto.

## 1. Primero, ¿qué es una API?

Una **API** (Application Programming Interface) es un **contrato** para que dos programas se hablen.
En esta app, el **frontend** (React) le pide datos al **backend** (Spring Boot) a través de una API por HTTP.

Hay dos estilos de API conviviendo en este proyecto:
- **REST** (la de siempre): un montón de endpoints (`/books`, `/reservations`, `/book-detail/{id}`...).
- **GraphQL** (la nueva): **un solo endpoint** (`/graphql`) para el tablero de KPIs.

## 2. REST y sus dos dolores

En REST, cada endpoint devuelve una **forma fija** de datos. Eso trae dos problemas típicos:

- **Over-fetching** (te trae de más): pedís `/book-detail/5` y te llega el libro completo (50 campos)
  aunque solo querías el título.
- **Under-fetching** (te trae de menos → varias llamadas): para armar una pantalla necesitás
  el libro **y** sus reservas **y** el dueño → 3 requests a 3 endpoints.

## 3. ¿Qué es GraphQL?

GraphQL es **un lenguaje de consulta para APIs** + un **motor** que las resuelve.
Lo creó Facebook (2012, open source 2015). Ideas centrales:

1. **Un solo endpoint** (`POST /graphql`). No hay una URL por recurso.
2. **El cliente pide exactamente lo que necesita** — ni más ni menos. Adiós over/under-fetching.
3. **Fuertemente tipado**: existe un **Schema** que describe TODO lo que se puede pedir. Si pedís algo
   que no existe o con el tipo equivocado, el server lo rechaza antes de ejecutar.
4. **La respuesta tiene la misma forma que la pregunta.**

Mini-ejemplo con este proyecto. El cliente manda:

```graphql
query {
  recentActivity {
    typeEvent
    bookTitle
  }
}
```

y el server responde **exactamente** esa forma (nada más):

```json
{ "data": { "recentActivity": [
  { "typeEvent": "BOOK_REGISTERED", "bookTitle": "Cien Años de Soledad" },
  { "typeEvent": "RESERVATION",     "bookTitle": "El Aleph" }
] } }
```

## 4. Conceptos centrales (con su lugar en el código)

### 4.1 Schema
Es **el contrato**. Se escribe en **SDL** (Schema Definition Language) y vive en
`src/main/resources/schema/schema.graphqls`. Define qué tipos hay y qué se puede consultar.

### 4.2 Tipos
- **Scalars** (valores simples): `Int`, `Float`, `String`, `Boolean`, `ID`.
- **Object types** (objetos con campos): `type NewBookEvent { date: String! ... }`.
- **Enum** (valores cerrados): `enum ActivityEventType { BOOK_REGISTERED, RESERVATION }`.
- **Interface** (campos comunes que varios tipos comparten): nuestro `interface ActivityEvent`.
- **Union** (un valor que es "uno de varios tipos", sin campos comunes obligatorios).
- **Input types** (para pasar argumentos complejos a una operación).

Modificadores de tipo:
- `!` = **non-null** (obligatorio). `String!` nunca es `null`.
- `[Tipo]` = **lista**. `[ActivityEvent!]!` = lista no-nula, de elementos no-nulos.

### 4.3 Operaciones raíz
Son los 3 puntos de entrada posibles:
- **Query** → leer datos (lo que usamos: `recentActivity`, `conversionRate`...).
- **Mutation** → crear/modificar (este proyecto no las usa en GraphQL; eso va por REST).
- **Subscription** → datos en tiempo real (push). No se usa acá.

### 4.4 Campos, argumentos y selección
Cada tipo tiene **campos**. Un campo puede recibir **argumentos**: `book(isbn: String!): BookGql`.
El cliente arma una **selección** (qué campos quiere); GraphQL devuelve solo esos.

### 4.5 Resolver (Data Fetcher) — la pieza clave del backend
Por cada campo, el motor necesita saber **de dónde sacar el dato**. Esa función es el **resolver**
(o *data fetcher*). El resolver del campo raíz `recentActivity` es nuestra clase
`RecentActivityDataFetcher`.

## 5. ¿Cómo resuelve el server una query? (el "árbol de resolución")

GraphQL resuelve la query **campo por campo, de la raíz hacia abajo**:

```
query { recentActivity { date user } }
         │                 │     │
         │                 │     └─ campo 'user' → resolver por defecto: toma la prop 'user' del objeto
         │                 └─────── campo 'date' → resolver por defecto: toma la prop 'date' del objeto
         └───────────────────────── campo raíz → RecentActivityDataFetcher.recentActivity() devuelve la lista
```

- **Resolver explícito**: el de `recentActivity` (lo escribimos nosotros, va a Mongo + Postgres).
- **Resolver por defecto**: para `date`, `user`, etc., GraphQL **no necesita código**: si el objeto
  devuelto tiene una propiedad con **ese mismo nombre**, la usa. Por eso nuestras `data class`
  (`NewBookEvent(val date, val user, ...)`) "matchean solas" con el schema: **mapeo por nombre de campo**.

> 🔑 **Regla de oro**: los nombres de los campos en la query y en los objetos del resolver tienen que
> coincidir EXACTAMENTE con los del schema. Si el schema dice `date` y pedís `fecha`, GraphQL responde error.

## 6. Errores en GraphQL

GraphQL casi siempre responde **HTTP 200**, incluso cuando algo falla. Los errores vienen en un array
`errors` dentro del JSON:

```json
{ "errors": [ { "message": "Field 'fecha' in type 'ActivityEvent' is undefined" } ] }
```

Por eso el front, después de cada query, hace:
```ts
if (response.data.errors) { throw new Error(...) }
```

## 7. Introspección y `__typename`

El schema es **auto-descriptivo**: se puede consultar a sí mismo (eso permite herramientas como GraphiQL).
Un meta-campo especial es **`__typename`**: devuelve el **nombre del tipo concreto** de un objeto.
Es imprescindible cuando trabajás con **interfaces/uniones** (sección siguiente).

## 8. Interfaces y listas heterogéneas — el corazón de este feature

El feed devuelve **una lista con dos tipos de cosa distintos** (altas de libro y reservas). Eso se modela
con una **interface**:

```graphql
interface ActivityEvent { date: String!  typeEvent: ActivityEventType!  user: String!  bookTitle: String! }
type NewBookEvent        implements ActivityEvent { ... }
type NewReservationEvent implements ActivityEvent { ... }

type Query { recentActivity: [ActivityEvent!]! }   # lista de la INTERFACE → heterogénea
```

Cuando el cliente recibe una lista de `ActivityEvent`, usa `__typename` para saber qué es cada elemento.
Si quisiera campos **específicos** de un tipo, usaría un **inline fragment**:

```graphql
query {
  recentActivity {
    __typename            # "NewBookEvent" | "NewReservationEvent"
    date
    ... on NewBookEvent { user }        # campos solo si es alta
    ... on NewReservationEvent { user } # campos solo si es reserva
  }
}
```

(En este feature los 4 campos son comunes a la interface, así que no hacen falta fragments; alcanza con
pedirlos directo + `__typename`.)

**Del lado del server**, para que GraphQL sepa convertir cada objeto Kotlin en su tipo concreto, hace falta
un **type resolver** (`@DgsTypeResolver`), que es justo lo que hace `ActivityEventTypeResolver`:
```kotlin
@DgsTypeResolver(name = "ActivityEvent")
fun resolveType(event: ActivityEvent): String = when (event) {
    is NewBookEvent        -> "NewBookEvent"
    is NewReservationEvent -> "NewReservationEvent"
}
```
Sin esto, GraphQL no puede resolver el `__typename` de la interface y la query falla.

## 9. Netflix DGS sobre Spring Boot (el framework que usamos)

**DGS** (Domain Graph Service) es el framework de Netflix para hacer GraphQL en Spring Boot. Es
**schema-first**: vos escribís el `.graphqls` y DGS lo cablea con tus clases por anotaciones.

| Anotación | Qué hace | En este feature |
|---|---|---|
| `@DgsComponent` | Marca una clase como proveedora de resolvers (como un `@Component`). | `RecentActivityDataFetcher`, `ActivityEventTypeResolver` |
| `@DgsQuery` | Mapea un método a un campo de `type Query` (por nombre). | `fun recentActivity()` ↔ `recentActivity` |
| `@DgsData` | Mapea un método a un campo de cualquier tipo (resolver de campo). | (no lo usamos acá; sí en otros KPIs para campos lazy) |
| `@DgsTypeResolver` | Resuelve el tipo concreto de una interface/union (`__typename`). | `ActivityEventTypeResolver` |

DGS encuentra el schema en `resources/schema/*.graphqls`, levanta el endpoint **`/graphql`** y enruta cada
campo al método anotado correspondiente.

## 10. Mapa concepto GraphQL → archivo del proyecto

| Concepto | Dónde verlo |
|---|---|
| Schema (SDL) | `resources/schema/schema.graphqls` |
| `type Query` + campo `recentActivity` | mismo archivo |
| Interface + tipos concretos + enum | mismo archivo |
| Resolver del campo raíz (`@DgsQuery`) | `graphql/RecentActivityDataFetcher.kt` |
| Tipos de retorno (mapeo por nombre) | `graphql/ActivityEvent.kt` (`NewBookEvent`, `NewReservationEvent`) |
| Type resolver de la interface | `graphql/ActivityEvent.kt` (`ActivityEventTypeResolver`) |
| Cliente que arma la query | `services/graphql/graphqlService.ts` |
| Manejo de `errors` | mismo archivo (chequeo `response.data.errors`) |

## 11. Probar la API a mano

Con `curl` (o Postman), apuntando al endpoint único:

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ recentActivity { __typename date typeEvent user bookTitle } }"}'
```

Respuesta esperada (resumida):
```json
{ "data": { "recentActivity": [
  { "__typename": "NewBookEvent", "date": "2026-06-11T20:00:00-03:00",
    "typeEvent": "BOOK_REGISTERED", "user": "Valentina Sosa", "bookTitle": "Cien Años de Soledad" }
] } }
```

> Si DGS tiene habilitado el playground, también podés explorar el schema y autocompletar queries en
> **`http://localhost:8080/graphiql`** (interfaz visual con introspección).

## 12. REST vs GraphQL (resumen)

| | REST | GraphQL |
|---|---|---|
| Endpoints | muchos (uno por recurso) | uno (`/graphql`) |
| Forma de la respuesta | fija (la decide el server) | la decide el **cliente** |
| Over/under-fetching | común | se evita |
| Tipado del contrato | informal (docs aparte) | **schema** fuerte y autodescriptivo |
| Varios recursos en una llamada | difícil | natural |
| Caché HTTP | simple (por URL) | requiere más trabajo |
| Cuándo conviene | CRUD simple, archivos, caché por URL | pantallas que cruzan varios datos (¡como este tablero!) |

Por eso el tablero de KPIs usa GraphQL: cada métrica cruza fuentes distintas (Redis, Postgres, Mongo) y el
front pide en **una sola query** justo lo que cada card necesita.

## 13. Glosario rápido

- **Schema / SDL**: el contrato tipado de la API.
- **Query**: operación de lectura. **Mutation**: de escritura. **Subscription**: tiempo real.
- **Resolver / Data Fetcher**: función que produce el valor de un campo.
- **Scalar**: tipo simple (Int, String, Boolean, ID...).
- **Interface / Union**: permiten respuestas **polimórficas** (varios tipos posibles).
- **`__typename`**: meta-campo con el nombre del tipo concreto.
- **Inline fragment** (`... on Tipo { }`): pedir campos solo cuando el objeto es de cierto tipo.
- **Introspección**: consultar el schema a sí mismo.
- **DGS**: framework de Netflix para GraphQL en Spring Boot.

## 14. Para seguir aprendiendo

- Documentación oficial: <https://graphql.org/learn/>
- Netflix DGS: <https://netflix.github.io/dgs/>
- "How to GraphQL": <https://www.howtographql.com/>
