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
CREATE OR REPLACE FUNCTION registrar_cambio()
RETURNS TRIGGER AS $$
DECLARE
    viejo_rating NUMERIC;
    nuevo_rating NUMERIC;
BEGIN
	-- Calculo el puntaje viejo
    SELECT AVG(rev.rating)
    INTO viejo_rating
    FROM review rev
    INNER JOIN reservation res
    ON res.review_id = rev.id
    WHERE rev.rating > 0
    AND NEW.id != rev.id -- sin la review nueva
    AND NEW.id = res.review_id;

    -- Calculo puntaje nuevo
    SELECT AVG(rev.rating)
    INTO nuevo_rating
    FROM review rev
    INNER JOIN reservation res
    ON res.review_id = rev.id
    WHERE rev.rating > 0
    AND NEW.id = res.review_id;
    
    -- Guardo todo en el historial con timestamp
    INSERT INTO historial_puntaje_libro (id_libro, fecha_actualizacion, valor_viejo, valor_nuevo)
    SELECT res.book_id, NOW(), viejo_rating, nuevo_rating
    FROM reservation res
    WHERE NEW.id = res.review_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- EL TRIGGER ESCUCHA A UN UPDATE DE RATING EN REVIEW
   -- ESTO CAMBIARA A UN INSERT EN REVIEW
CREATE TRIGGER trg_puntaje_libro
    AFTER UPDATE OF rating ON review
    FOR EACH ROW
    EXECUTE FUNCTION registrar_cambio();

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