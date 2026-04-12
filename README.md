# 📚 BookLibre - Backend

> "Que la fuerza te acompañe… y que te devuelvan el libro en fecha."

Backend de la aplicación BookLibre, una plataforma para gestionar préstamos de libros entre usuarios. Desarrollado con **Kotlin + Spring Boot**.

---

## Tecnologías

- **Kotlin**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL** (base de datos)
- **Maven / Gradle**

---

## Cómo correr el proyecto

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/booklibre-backend.git
cd booklibre-backend

# Correr la aplicación
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
     valor_nuevo DECIMAL(3,2)
);

-- LA FUNCION DE INSERT AL HISTORIAL
CREATE OR REPLACE FUNCTION registrar_cambio_puntaje()
RETURNS TRIGGER AS $$
BEGIN
    -- Guardo todo en el historial con timestamp
    INSERT INTO historial_puntaje_libro (id_libro, fecha_actualizacion, valor_viejo, valor_nuevo)
    VALUES (NEW.id, NOW(), OLD.ratingAvg, NEW.ratingAvg);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- EL TRIGGER ESCUCHA A UN UPDATE DE RATING EN REVIEW
   -- ESTO CAMBIARA A UN INSERT EN REVIEW
CREATE TRIGGER trg_puntaje_libro
    AFTER UPDATE OF ratingAvg ON book
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
---

##  Tutor
- **Foglia, Pablo**

##  Integrantes

- **Andres Bianchimano, Maximiliano**
- **Cernadas, Nicolas**
- **Correa, Catalina**
- **Cossetini Reyes, Dana**
- **Perez, Fernanda**
