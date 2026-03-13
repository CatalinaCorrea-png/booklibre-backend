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

##  Tutor
- **Foglia, Pablo**

##  Integrantes

- **Andres Bianchimano, Maximiliano**
- **Cernadas, Nicolas**
- **Correa, Catalina**
- **Cossetini Reyes, Dana**
- **Perez, Fernanda**