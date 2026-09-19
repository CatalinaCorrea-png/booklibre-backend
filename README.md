![Coverage](.github/badges/jacoco.svg)

# 📚 BookLibre - Backend

> "Que la fuerza te acompañe… y que te devuelvan el libro en fecha."

Backend de **BookLibre**, una plataforma para gestionar préstamos de libros entre usuarios. Desarrollado con **Kotlin + Spring Boot**.

> Proyecto grupal de la materia PHM (UNSAM, 2026). Este repositorio es una copia del
> original de la cátedra, con todo el historial. [Equipo](#integrantes) al final.

---

## 🛠️ Tecnologías

![Kotlin](https://img.shields.io/badge/Kotlin_1.9-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![GraphQL](https://img.shields.io/badge/GraphQL_(Netflix_DGS)-E10098?style=flat-square&logo=graphql&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![Java](https://img.shields.io/badge/JDK_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)

---

## ✨ Lo más interesante del proyecto

**Autenticación JWT, sin sesión en el servidor**
- **Dos tokens.** El *access token* (JWT firmado con HMAC) lleva el rol y el email del usuario y
  viaja en el header `Authorization`. El *refresh token* va en una cookie `httpOnly` +
  `SameSite=Strict` y **rota en cada uso**: el anterior queda invalidado.
- **Filtro JWT propio** (`OncePerRequestFilter`) que responde `401` con
  `WWW-Authenticate: Bearer error="invalid_token"` y distingue un token **vencido** de uno
  **inválido**. El frontend usa esa respuesta para saber cuándo renovar el token.
- **CSRF desactivado a propósito:** el access token no viaja en una cookie y la del refresh es
  `SameSite=Strict`, así que el navegador no la manda desde otros sitios.
- **Permisos por rol en cada endpoint**, con una
  [jerarquía de roles](#seguridad--jerarquía-de-roles-rolehierarchy) para que el admin herede
  los permisos del resto.

**Persistencia en tres motores**
- **PostgreSQL** (JPA) para usuarios, reservas y reseñas, con
  [funciones, un trigger y una vista](#componentes-en-la-base-de-datos) en la propia base.
- **MongoDB** para el catálogo de libros y los clics.
- **Redis** para el [ranking de los 10 libros más clickeados](#redis--home-top-10-más-clickeados-redis)
  y una caché por libro.

**GraphQL (Netflix DGS)**
- KPIs para el panel de administración. La tasa de conversión, por ejemplo, **cruza los clics de
  Redis con las reservas de PostgreSQL**.
- [*Schema stitching* con OpenLibrary](#graphql--schema-stitching-openlibrary): la API externa se
  consulta solo si el cliente pide ese campo.

**Sharding de MongoDB** (ramas `test/mongo-sharding-hashed` y `test/mongo-sharding-range`)
- Cluster con 2 shards, cada uno un *replica set* de 3 nodos, más config servers y router.
- **Hash** sobre `{ bookId: "hashed" }` con 500.024 libros → reparto de 49,99 % / 50,00 %.
- **Rango** sobre `{ title: 1, bookId: 1 }` con 452.000 libros, pre-split y `moveChunk` manual con
  el balancer apagado → 49,83 % / 50,16 %, y las búsquedas por título van a **un solo shard**.
- Cada rama documenta el experimento en `scripts/README.md`.

**Deploy**
- `Dockerfile` en dos etapas (Gradle + JDK 21 → JRE 21), configuración por variables de entorno
  (ver `.env.example`) y deploy en Render. Cobertura medida con JaCoCo (badge arriba).

---

## 🚀 Cómo correr el proyecto

### Prerequisitos
- JDK 17+
- Docker y Docker Compose

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/booklibre-backend.git
cd booklibre-backend

# 2. Levantar la base de datos con Docker
docker-compose up -d

# 3. Correr la aplicación
./gradlew bootRun
```

El servidor levanta por defecto en `http://localhost:8080`.

---

##  Dominio

### Usuario
Representa a una persona registrada en la plataforma.

### Libro
Representa un libro disponible para prestar.


### Reserva
Representa la reserva de un libro por parte de un usuario lector.


---

##  BiblioKarmas

Los BiblioKarmas son el sistema de puntaje de la plataforma. Se calculan al momento de realizar una reserva.

La fórmula base es:

```
bibliokarmas = 5 * días de reserva + plus por tipo de libro
```

El plus varía según el tipo de libro:

- **Libro común:** `páginas * 5` si el usuario tiene menos de 1000 bibliokarmas, o `páginas * 2` en caso contrario.
- **Libro con dedicatoria:** `200 + 10 * cantidad de reservas del libro`
- **Libro coleccionable:** `ceil(bibliokarmas del usuario / 5) + páginas del libro`

---

##  Estructura del proyecto

```
src/
└── main/
    └── kotlin/
            ├── controller/    # Endpoints REST
            ├── service/       # Lógica de negocio
            ├── domain/         # Entidades del dominio
            ├── errorrs/       # Errores
            ├── repository/    # Repositorio
            └── dto/           # Objetos de transferencia
```

---

##  Endpoints principales
TODO

---

## Componentes en la Base de Datos

### 1. Conocer los Libros que reservó un determinado usuario en el corriente año.

```sql
--  Query function
CREATE OR REPLACE FUNCTION get_user_reservations(p_user_id INT)
RETURNS TABLE (
	name VARCHAR,
	title VARCHAR,
	pick_up_date DATE,
	drop_off_date DATE
)

LANGUAGE plpgsql
AS $$
BEGIN
    IF p_user_id <= 0
        THEN RAISE EXCEPTION 'El ID no puede ser cero o negativo: %', p_user_id;
    END IF;
    
    IF NOT EXISTS( SELECT 1 FROM app_user u WHERE u.id = p_user_id )
        THEN RAISE EXCEPTION 'No existe un usuario con ese ID: %', p_user_id;
    END IF;

    RETURN QUERY
        SELECT u.name, b.title, r.pick_up_date, r.drop_off_date
        FROM Reservation r
        INNER JOIN app_user u ON u.id = r.user_id
        INNER JOIN book b ON b.id = r.book_id
        WHERE EXTRACT( YEAR FROM r.pick_up_date ) = EXTRACT( YEAR FROM CURRENT_DATE )
        AND u.id = p_user_id;
END;
$$;

-- Function call
SELECT * get_user_reservations_current_year(1)
```

### 2. Llevar un control de las veces que un libro actualizó su puntaje, de manera de saber: a) la fecha en la que se actualizó, b) el nuevo valor y el anterior.

```sql
-- CREAR TABLA PARA GUARDAR ACTUALIZACIONES
DROP TABLE IF EXISTS historial_puntaje_libro;
CREATE TABLE historial_puntaje_libro (
     id SERIAL PRIMARY KEY,
     id_libro INT NOT NULL,
     fecha_actualizacion TIMESTAMP,
     valor_viejo DECIMAL(3,2),
     valor_nuevo DECIMAL(3,2),
     veces_actualizado INT
);

-- LA FUNCION DE INSERT AL HISTORIAL
CREATE OR REPLACE FUNCTION registrar_cambio_puntaje()
RETURNS TRIGGER AS $$
DECLARE
    acc NUMERIC;
BEGIN
    SELECT COUNT(*)
    INTO acc
    FROM historial_puntaje_libro
    WHERE id_libro = NEW.id;
    
    -- Guardo todo en el historial con timestamp
    INSERT INTO historial_puntaje_libro (id_libro, fecha_actualizacion, valor_viejo, valor_nuevo, veces_actualizado)
    VALUES (NEW.id, NOW(), OLD.rating_avg, NEW.rating_avg, acc+1);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- EL TRIGGER ESCUCHA A UN UPDATE DE AVGRATING EN BOOK
CREATE TRIGGER trg_puntaje_libro
    AFTER UPDATE OF rating_avg ON book
    FOR EACH ROW
    EXECUTE FUNCTION registrar_cambio_puntaje();

```

### 3. Saber qué usuarios tienen más de N reservas.
``` sql
CREATE OR REPLACE FUNCTION obtener_usuarios_con_n_reservas(n INT)  
RETURNS TABLE ( 
	id INT, 
	name TEXT 
) 
AS $$ 
BEGIN 
	RETURN QUERY  
	SELECT u.id, u.name 
	FROM app_user u 
	JOIN reservation r ON r.user_id = u.id -- La reserva conoce al usuario
	GROUP BY u.id, u.name 
	HAVING COUNT(r.id) > n 
END; 
$$ LANGUAGE plpgsql;
```

### 4. Evitar que los bibliokarmas de un usuario tomen un valor nulo en la base (por fuera de la interfaz de usuario).

``` sql
ALTER TABLE app_user
    ALTER COLUMN bibliokarmas SET NOT NULL,
    ALTER COLUMN bibliokarmas SET DEFAULT 0;

-- Si ya existen filas con NULL se lo saco
UPDATE app_user SET bibliokarmas = 0 WHERE bibliokarmas IS NULL;
```

### 5. Listar los usuarios que tengan más de 2 reservas devueltas.
``` sql

CREATE VIEW users_with_more_than_2_returned_reservations AS
SELECT 
    u.id,
    u.name,
    u.email,
    u.user_type,
    COUNT(r.id) AS returned_reservations
FROM app_user u
JOIN reservation r ON r.user_id = u.id
WHERE r.drop_off_date < CURRENT_DATE
GROUP BY u.id, u.name, u.email, u.user_type
HAVING COUNT(r.id) > 2
ORDER BY returned_reservations DESC;

SELECT * FROM users_with_more_than_2_returned_reservations;

```

---

## Consultas MongoDB

### 1. Saber qué libro es el más clickeado.
```js
db.books.find().sort({ bookClicks: -1 }).limit(1)
```

### 2. Saber cuantos libros son del tipo coleccionable
```js
db["books"].find({ "bookType" : "COLECCIONABLE" }).count()
```


### 3. Saber qué libros tienen más de 4 puntos de calificación.

```js
db.books.find({ratingAvg:{$gt:4}})
```

### 4. Saber qué libros tienen al menos 3 reservas activas.

```js
db.books.aggregate([
    { $match: { "reservations": { $exists: true } } },
    { $project: { title: 1, activeReservations: { $filter: { input: "$reservations", as: "r", 
                    cond: { $and: [{ $lte: ["$$r.pickUpDate", new Date()] }, 
                            { $gte: ["$$r.dropOffDate", new Date()] }] } } } } },
    { $match: { "activeReservations.2": { $exists: true } } }
])

```

### 5. Saber qué libros tienen todos las reservas cumplidas (ya devolvieron los libros)

```js
db["books"].find({
  "reservations": {
    $not: {
      $elemMatch: { "dropOffDate": { $gte: ISODate() } }
    }
  }
})
```
---

## Redis — Home Top 10 más clickeados (Redis)

La primera página del Home (page 0, sin filtros) se sirve desde Redis:

1. **ZSET `books-ranking:clicks`** → top 10 `bookId` por clicks (`ZREVRANGE`). Se siembra desde `Book.bookClicks` en el bootstrap y sube `+1` con cada click.
2. **Cache por-libro `cached-books:<bookId>`** (TTL 10 min) → trae el JSON de esos libros en un solo `MGET`.
3. Si falta alguno en cache → fallback a Mongo (`findTop10ByOrderByBookClicksDesc`) y se re-cachea.
4. Se quedan los primeros 6 (`HOME_PAGE_SIZE`), se les calcula bibliokarmas y se devuelve. El total se cuenta en Mongo con el **mismo criterio per-usuario** que `searchBooks` (`countByCriteria(byCriteriaMongo)`), para que la cantidad de páginas sea consistente entre la página 0 y las siguientes.

El resto (page 1+ o búsquedas con filtros) va directo a Mongo (`searchBooks`).

---

## GraphQL — Schema stitching (OpenLibrary)

El tipo `BookGql` se compone de dos fuentes: **MongoDB** para los campos base y **OpenLibrary** (por ISBN) para el campo `externalMetadata`. Ese campo se resuelve de forma **lazy**: la API externa se consulta solo si el cliente pide ese campo. Si OpenLibrary falla o no tiene el ISBN, `cover` cae al `imageSrc` guardado en Mongo. El panel de KPIs es conceptualmente solo para administradores (el front bloquea la ruta); el endpoint `/graphql` queda abierto en el back (`permitAll`) como simplificación del TP.

**Query sin `externalMetadata`** → no hay llamada a OpenLibrary:
```graphql
query {
  book(isbn: "978-0-452-28423-4") {
    title
    imageSrc
  }
}
```
```json
{
  "data": {
    "book": {
      "title": "1984",
      "imageSrc": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz9gIAgf5hTagXaQZl8ayY6FF26n2qirXQMg&s"
    }
  }
}
```

**Query con `externalMetadata`** → se consulta OpenLibrary por ISBN:
```graphql
query {
  book(isbn: "978-0-452-28423-4") {
    title
    externalMetadata {
      title
      cover
      pageCount
      publishDate
    }
  }
}
```
```json
{
  "data": {
    "book": {
      "title": "1984",
      "externalMetadata": {
        "title": "Nineteen eighty-four",
        "cover": "https://covers.openlibrary.org/b/id/7898938-L.jpg",
        "pageCount": 339,
        "publishDate": "2003"
      }
    }
  }
}
```

---

## Seguridad — Jerarquía de roles (`RoleHierarchy`)

Un `@Bean RoleHierarchy` en `SecurityConfiguration` define que `ADMIN` está por encima del resto de los roles, de modo que un admin **hereda** las authorities `READER`, `PUBLISHER` y `COMBINED`. Así el admin pasa todos los `requestMatcher` protegidos por rol sin tener que listar `ADMIN` en cada uno.

```kotlin
@Bean
fun roleHierarchy(): RoleHierarchy =
    RoleHierarchyImpl.fromHierarchy(
        """
        ADMIN > PUBLISHER
        ADMIN > COMBINED
        ADMIN > READER
        """.trimIndent()
    )
```

Por qué hace falta: la autorización es **first-match-wins** por orden de los matchers (no "gana la más permisiva"). Sin la jerarquía, un admin que pega a un endpoint listado con otro rol (ej. `/filtered-books` → `READER`/`COMBINED`) sería rechazado antes de llegar a cualquier regla general. En Spring Boot 3.3 el bean se aplica automáticamente, sin cablearlo dentro de `authorizeHttpRequests`.

---

##  Tutor
- **Foglia, Pablo**

##  Integrantes

- **Andres Bianchimano, Maximiliano**
- **Cernadas, Nicolas**
- **Correa, Catalina**
- **Cossetini Reyes, Dana**
- **Perez, Fernanda**
