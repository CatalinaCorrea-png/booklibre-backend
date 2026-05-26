![Coverage](.github/badges/jacoco.svg)

# 📚 BookLibre - Backend

> "Que la fuerza te acompañe… y que te devuelvan el libro en fecha."

Backend de **BookLibre**, una plataforma para gestionar préstamos de libros entre usuarios. Desarrollado con **Kotlin + Spring Boot**.

---

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Spring Boot | Framework web |
| Spring Data JPA | Persistencia y ORM |
| PostgreSQL | Base de datos relacional |
| Gradle | Gestión de dependencias |
| Docker | Contenedor de base de datos |
| JWT | Autenticación y seguridad |

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
db.book_clicks.aggregate([
    { $group: { _id: "$bookId", count: { $sum: 1 } } },
    { $sort: { count: -1 } },
    { $limit: 1 }
])
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

##  Tutor
- **Foglia, Pablo**

##  Integrantes

- **Andres Bianchimano, Maximiliano**
- **Cernadas, Nicolas**
- **Correa, Catalina**
- **Cossetini Reyes, Dana**
- **Perez, Fernanda**
