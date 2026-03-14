package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.Author
import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookCondition
import ar.edu.unsam.phm.domain.Collectable
import ar.edu.unsam.phm.domain.Common
import ar.edu.unsam.phm.domain.Gender
import ar.edu.unsam.phm.domain.Language
import ar.edu.unsam.phm.repository.Repository
import ar.edu.unsam.phm.domain.Reservation
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.domain.WithADedication
import ar.edu.unsam.phm.domain.*
import java.time.LocalDate

object ApplicationBootstrap {

    // ─── Autores ──────────────────────────────────────────────────────────────

    private val orwell     = Author("George Orwell", "/assets/1_george_orwell.png")
    private val kafka      = Author("Franz Kafka", "/assets/2_franz_kafka.png")
    private val garcia     = Author("Gabriel García Márquez", "/assets/3_gabriel_garcia_marquez.png")
    private val dostoevsky = Author("Fiódor Dostoyevski", "/assets/4_fiodor_dostoyevski.png")
    private val rowling    = Author("J.K. Rowling", "/assets/5_j_k_rowling.png")
    private val asimov     = Author("Isaac Asimov", "/assets/6_isaac_asimov.png")
    private val austen     = Author("Jane Austen", "/assets/7_jane_austen.png")
    private val tolstoy    = Author("León Tolstói", "/assets/8_leon_tolstoi.png")
    private val fitzgerald = Author("F. Scott Fitzgerald", "/assets/9_f_scott_fitzgerald.png")
    private val hemingway  = Author("Ernest Hemingway", "/assets/10_ernest_hemingway.png")
    private val hugo       = Author("Victor Hugo", "/assets/11_victor_hugo.png")
    private val twain      = Author("Mark Twain", "/assets/12_mark_twain.png")
    private val dumas      = Author("Alexandre Dumas", "/assets/13_alexandre_dumas.png")
    private val verne      = Author("Julio Verne", "/assets/14_julio_verne.png")
    private val coelho     = Author("Paulo Coelho", "/assets/15_paulo_coelho.png")
    private val camus      = Author("Albert Camus", "/assets/16_albert_camus.png")
    private val woolf      = Author("Virginia Woolf", "/assets/17_virginia_woolf.png")
    private val poe        = Author("Edgar Allan Poe", "/assets/18_edgar_allan_poe.png")
    private val chekhov    = Author("Antón Chéjov", "/assets/19_anton_chejov.png")
    private val borges     = Author("Jorge Luis Borges", "/assets/20_jorge_luis_borges.png")
    private val cortazar   = Author("Julio Cortázar", "/assets/21_julio_cortazar.png")
    private val saramago   = Author("José Saramago", "/assets/22_jose_saramago.png")
    private val mann       = Author("Thomas Mann", "/assets/23_thomas_mann.png")
    private val proust     = Author("Marcel Proust", "/assets/24_marcel_proust.png")

    // ─── Usuarios ─────────────────────────────────────────────────────────────

    val emiliaRomero = User(
        name = "Emilia Romero",
        description = "Lectora ávida & coleccionista",
        email = "emilia@example.com",
        cel = "+54 11 1234-5678",
        location = "Buenos Aires, AR",
        userType = UserType.COMBINED,
        bibliokarmas = 2345
    )

    val lucianoVega = User(
        name = "Luciano Vega",
        description = "Fanático de la ciencia ficción",
        email = "luciano@example.com",
        cel = "+54 11 8765-4321",
        location = "Rosario, AR",
        userType = UserType.READER,
        bibliokarmas = 980
    )

    val valentinaSosa = User(
        name = "Valentina Sosa",
        description = "Escritora y lectora compulsiva",
        email = "valentina@example.com",
        cel = "+54 11 5555-0000",
        location = "Córdoba, AR",
        userType = UserType.PUBLISHER,
        bibliokarmas = 1500
    )

    val mateoLopez = User(
        name = "Mateo López",
        description = "Lector ocasional, coleccionista serio",
        email = "mateo@example.com",
        cel = "+54 11 3333-7777",
        location = "Mendoza, AR",
        userType = UserType.COMBINED,
        bibliokarmas = 420
    )

    // ─── Libros Comunes (8) ───────────────────────────────────────────────────

    val n1984 = Common().apply {
        title       = "1984"
        desc        = "En un futuro opresivo, Winston Smith vive bajo la mirada del Gran Hermano. El Partido controla la verdad, la memoria y el pensamiento. Una historia sobre la resistencia y la fragilidad de la libertad."
        gender      = Gender.SCIENCE_FICTION
        author      = orwell
        numPages    = 328
        language    = Language.SPANISH
        editorial   = "Secker & Warburg"
        publishDate = LocalDate.of(1949, 6, 8)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
    }

    val elProceso = Common().apply {
        title       = "El Proceso"
        desc        = "Josef K. se despierta un día arrestado sin cargos. Navega una burocracia kafkiana sin salida, donde la culpa parece inevitable y la justicia, inalcanzable. Una pesadilla absurda y profundamente humana."
        gender      = Gender.DRAMA
        author      = kafka
        numPages    = 255
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1925, 4, 26)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
    }

    val crimen = Common().apply {
        title       = "Crimen y Castigo"
        desc        = "Raskolnikov, un estudiante en la miseria, asesina a una usurera creyéndose superior a la moral común. La culpa lo consume lentamente. Una exploración magistral de la psicología criminal y la redención."
        gender      = Gender.DRAMA
        author      = dostoevsky
        numPages    = 545
        language    = Language.SPANISH
        editorial   = "Cátedra"
        publishDate = LocalDate.of(1866, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = valentinaSosa
    }

    val orgullo = Common().apply {
        title       = "Orgullo y Prejuicio"
        desc        = "Elizabeth Bennet, inteligente y sin fortuna, choca con el arrogante Sr. Darcy. Entre malentendidos y presiones sociales, ambos deben superar sus propios prejuicios para encontrar el amor verdadero."
        gender      = Gender.ROMANCE
        author      = austen
        numPages    = 432
        language    = Language.SPANISH
        editorial   = "Penguin Clásicos"
        publishDate = LocalDate.of(1813, 1, 28)
        condition   = BookCondition.EXCELLENT
        owner       = mateoLopez
    }

    val guerraPaz = Common().apply {
        title       = "Guerra y Paz"
        desc        = "A través de varias familias nobles rusas, Tolstói retrata la invasión napoleónica de 1812. Una épica sobre el amor, la guerra y la búsqueda de sentido que abarca toda la condición humana."
        gender      = Gender.CLASSIC_LITERATURE
        author      = tolstoy
        numPages    = 1225
        language    = Language.SPANISH
        editorial   = "Alba Editorial"
        publishDate = LocalDate.of(1869, 1, 1)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
    }

    val losMiserables = Common().apply {
        title       = "Los Miserables"
        desc        = "Jean Valjean, ex convicto, busca redimirse en una Francia desigual. Perseguido por el implacable inspector Javert, su historia entrelaza justicia, misericordia y revolución en el París del siglo XIX."
        gender      = Gender.CLASSIC_LITERATURE
        author      = hugo
        numPages    = 1232
        language    = Language.SPANISH
        editorial   = "Planeta"
        publishDate = LocalDate.of(1862, 1, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
    }

    val alquimista = Common().apply {
        title       = "El Alquimista"
        desc        = "Santiago, un joven pastor andaluz, sueña con un tesoro escondido en Egipto. Su viaje se convierte en una búsqueda espiritual donde aprende a escuchar al universo y seguir su leyenda personal."
        gender      = Gender.SELF_HELP
        author      = coelho
        numPages    = 208
        language    = Language.PORTUGUESE
        editorial   = "Planeta"
        publishDate = LocalDate.of(1988, 1, 1)
        condition   = BookCondition.EXCELLENT
        owner       = valentinaSosa
    }

    val extranjero = Common().apply {
        title       = "El Extranjero"
        desc        = "Meursault no llora en el funeral de su madre y días después mata a un hombre bajo el sol argelino. Su indiferencia ante la vida y la muerte lo convierte en símbolo del absurdo existencial de Camus."
        gender      = Gender.DRAMA
        author      = camus
        numPages    = 159
        language    = Language.FRENCH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1942, 1, 1)
        condition   = BookCondition.GOOD
        owner       = mateoLopez
    }

    // ─── Libros Con Dedicatoria (8) ───────────────────────────────────────────

    val granGatsby = WithADedication().apply {
        title       = "El Gran Gatsby"
        desc        = "Jay Gatsby organiza fiestas opulentas en los años 20 esperando reconquistar a Daisy, su amor perdido. A través de Nick Carraway, Fitzgerald retrata la decadencia detrás del sueño americano."
        gender      = Gender.CLASSIC_LITERATURE
        author      = fitzgerald
        numPages    = 180
        language    = Language.SPANISH
        editorial   = "Scribner"
        publishDate = LocalDate.of(1925, 4, 10)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
    }

    val adiosArmas = WithADedication().apply {
        title       = "Adiós a las Armas"
        desc        = "El teniente Henry se enamora de la enfermera Catherine Barkley en el frente italiano de la Primera Guerra Mundial. Una historia de amor y pérdida narrada con la prosa desnuda y poderosa de Hemingway."
        gender      = Gender.DRAMA
        author      = hemingway
        numPages    = 332
        language    = Language.SPANISH
        editorial   = "Scribner"
        publishDate = LocalDate.of(1929, 9, 27)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
    }

    val monteCristo = WithADedication().apply {
        title       = "El Conde de Montecristo"
        desc        = "Edmond Dantès es encarcelado injustamente. Tras escapar y hallar un tesoro, regresa transformado en el Conde de Montecristo para ejecutar una venganza meticulosa contra quienes arruinaron su vida."
        gender      = Gender.CLASSIC_LITERATURE
        author      = dumas
        numPages    = 1276
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1844, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = valentinaSosa
    }

    val vueltaMundo = WithADedication().apply {
        title       = "La Vuelta al Mundo en 80 Días"
        desc        = "El excéntrico Phileas Fogg apuesta su fortuna a que puede circunnavegar el globo en ochenta días. Con su fiel criado Passepartout, vive aventuras en Asia, América y Europa contra el tiempo."
        gender      = Gender.SCIENCE_FICTION
        author      = verne
        numPages    = 304
        language    = Language.FRENCH
        editorial   = "Hetzel"
        publishDate = LocalDate.of(1872, 1, 1)
        condition   = BookCondition.EXCELLENT
        owner       = mateoLopez
    }

    val senoraDalloway = WithADedication().apply {
        title       = "La Señora Dalloway"
        desc        = "En un solo día londinense, Clarissa Dalloway prepara una fiesta mientras sus recuerdos y los de un veterano de guerra se entrelazan. Woolf explora la memoria, la identidad y el peso invisible del tiempo."
        gender      = Gender.DRAMA
        author      = woolf
        numPages    = 194
        language    = Language.ENGLISH
        editorial   = "Hogarth Press"
        publishDate = LocalDate.of(1925, 5, 14)
        condition   = BookCondition.VERY_GOOD
        owner       = emiliaRomero
    }

    val cuentosMisterio = WithADedication().apply {
        title       = "Cuentos de Misterio e Imaginación"
        desc        = "Poe construye atmósferas de terror y locura en relatos donde la culpa acecha, los muertos regresan y la mente humana se desintegra. Una colección esencial del gótico americano que definió el género."
        gender      = Gender.DRAMA
        author      = poe
        numPages    = 424
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1840, 1, 1)
        condition   = BookCondition.GOOD
        owner       = lucianoVega
    }

    val fundacion = WithADedication().apply {
        title       = "Fundación"
        desc        = "El matemático Hari Seldon usa la psicohistoria para predecir la caída del Imperio Galáctico. Funda una colonia en el fin del universo con el objetivo de reducir milenios de barbarie a tan solo uno."
        gender      = Gender.SCIENCE_FICTION
        author      = asimov
        numPages    = 255
        language    = Language.ENGLISH
        editorial   = "Gnome Press"
        publishDate = LocalDate.of(1951, 5, 1)
        condition   = BookCondition.EXCELLENT
        owner       = valentinaSosa
    }

    val cienAnios = WithADedication().apply {
        title       = "Cien Años de Soledad"
        desc        = "Siete generaciones de la familia Buendía habitan Macondo, un pueblo fundado en la selva colombiana. García Márquez mezcla lo real y lo mágico en una saga sobre el amor, la guerra y la soledad inevitable."
        gender      = Gender.CLASSIC_LITERATURE
        author      = garcia
        numPages    = 471
        language    = Language.SPANISH
        editorial   = "Sudamericana"
        publishDate = LocalDate.of(1967, 5, 30)
        condition   = BookCondition.VERY_GOOD
        owner       = mateoLopez
    }

    // ─── Libros Coleccionables (8) ────────────────────────────────────────────

    val huckFinn = Collectable().apply {
        title       = "Las Aventuras de Huckleberry Finn"
        desc        = "Huck Finn huye de su padre alcohólico y navega el Mississippi junto a Jim, un esclavo fugitivo. Una aventura que cuestiona con humor y ternura la moral y el racismo de la sociedad norteamericana del siglo XIX."
        gender      = Gender.CLASSIC_LITERATURE
        author      = twain
        numPages    = 366
        language    = Language.ENGLISH
        editorial   = "Chatto & Windus"
        publishDate = LocalDate.of(1884, 12, 10)
        condition   = BookCondition.EXCELLENT
        owner       = emiliaRomero
    }

    val ficciones = Collectable().apply {
        title       = "Ficciones"
        desc        = "Borges construye mundos imposibles: una biblioteca infinita, un mapa del tamaño del territorio, un hombre que recuerda cada detalle. Cuentos que desafían la percepción de la realidad, el tiempo y la identidad."
        gender      = Gender.SCIENCE_FICTION
        author      = borges
        numPages    = 174
        language    = Language.SPANISH
        editorial   = "Sur"
        publishDate = LocalDate.of(1944, 1, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
    }

    val rayuela = Collectable().apply {
        title       = "Rayuela"
        desc        = "Horacio Oliveira vaga por París buscando a la Maga y un sentido esquivo. Cortázar propone una novela que puede leerse en múltiples órdenes, rompiendo las convenciones del relato tradicional."
        gender      = Gender.DRAMA
        author      = cortazar
        numPages    = 635
        language    = Language.SPANISH
        editorial   = "Sudamericana"
        publishDate = LocalDate.of(1963, 6, 28)
        condition   = BookCondition.GOOD
        owner       = valentinaSosa
    }

    val ensayoCeguera = Collectable().apply {
        title       = "Ensayo sobre la Ceguera"
        desc        = "Una epidemia de ceguera blanca se propaga sin control. Saramago retrata cómo el orden social colapsa y emerge lo peor del ser humano, en una alegoría brutal sobre la fragilidad de la civilización."
        gender      = Gender.DRAMA
        author      = saramago
        numPages    = 310
        language    = Language.PORTUGUESE
        editorial   = "Caminho"
        publishDate = LocalDate.of(1995, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = mateoLopez
    }

    val montagnaMagica = Collectable().apply {
        title       = "La Montaña Mágica"
        desc        = "Hans Castorp visita a un primo en un sanatorio suizo y termina quedándose siete años. Rodeado de enfermos y pensadores, reflexiona sobre el tiempo, la muerte y las ideas que sacuden a Europa antes de la guerra."
        gender      = Gender.CLASSIC_LITERATURE
        author      = mann
        numPages    = 720
        language    = Language.FRENCH
        editorial   = "S. Fischer Verlag"
        publishDate = LocalDate.of(1924, 11, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = emiliaRomero
    }

    val caminoSwann = Collectable().apply {
        title       = "Por el Camino de Swann"
        desc        = "El narrador rememora su infancia en Combray, evocada por el sabor de una magdalena. También sigue el amor obsesivo de Swann por Odette. El inicio de una obra monumental sobre la memoria y el tiempo perdido."
        gender      = Gender.CLASSIC_LITERATURE
        author      = proust
        numPages    = 512
        language    = Language.FRENCH
        editorial   = "Grasset"
        publishDate = LocalDate.of(1913, 11, 14)
        condition   = BookCondition.EXCELLENT
        owner       = lucianoVega
    }

    val jardinCerezos = Collectable().apply {
        title       = "El Jardín de los Cerezos"
        desc        = "La familia Ranevskaya regresa a su hacienda rusa para descubrir que deberán venderla para saldar deudas, incluido el amado jardín de cerezos. Chéjov retrata el fin de una época con melancolía y humor sutil."
        gender      = Gender.DRAMA
        author      = chekhov
        numPages    = 112
        language    = Language.SPANISH
        editorial   = "Cátedra"
        publishDate = LocalDate.of(1904, 1, 17)
        condition   = BookCondition.GOOD
        owner       = valentinaSosa
    }

    val harryPotter = Collectable().apply {
        title       = "Harry Potter y la Piedra Filosofal"
        desc        = "Harry Potter descubre en su undécimo cumpleaños que es un mago y que el mundo mágico lo espera en Hogwarts. Allí hará amigos, enfrentará enemigos y comenzará a desentrañar el misterio de su propio pasado."
        gender      = Gender.SCIENCE_FICTION
        author      = rowling
        numPages    = 309
        language    = Language.ENGLISH
        editorial   = "Bloomsbury"
        publishDate = LocalDate.of(1997, 6, 26)
        condition   = BookCondition.VERY_GOOD
        owner       = mateoLopez
    }

    // ─── Reservas (2 por usuario) ─────────────────────────────────────────────

    val reservaEmilia1 = Reservation(
        user = emiliaRomero,
        book = n1984,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 15),
        dropOffDate = LocalDate.of(2026, 3, 29)
    )

    val reservaEmilia2 = Reservation(
        user = emiliaRomero,
        book = ficciones,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 1),
        dropOffDate = LocalDate.of(2026, 4, 14)
    )

    val reservaLuciano1 = Reservation(
        user = lucianoVega,
        book = fundacion,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 20),
        dropOffDate = LocalDate.of(2026, 4, 3)
    )

    val reservaLuciano2 = Reservation(
        user = lucianoVega,
        book = rayuela,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 5),
        dropOffDate = LocalDate.of(2026, 4, 19)
    )

    val reservaValentina1 = Reservation(
        user = valentinaSosa,
        book = granGatsby,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 10),
        dropOffDate = LocalDate.of(2026, 3, 24)
    )

    val reservaValentina2 = Reservation(
        user = valentinaSosa,
        book = harryPotter,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 2),
        dropOffDate = LocalDate.of(2026, 4, 16)
    )

    val reservaMateo1 = Reservation(
        user = mateoLopez,
        book = cienAnios,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 18),
        dropOffDate = LocalDate.of(2026, 4, 1)
    )

    val reservaMateo2 = Reservation(
        user = mateoLopez,
        book = ensayoCeguera,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 7),
        dropOffDate = LocalDate.of(2026, 4, 21)
    )

    // ─── Función de inicialización ────────────────────────────────────────────

    fun init(
        userRepository: Repository<User>,
        bookRepository: Repository<Book>,
        reservationRepository: Repository<Reservation>,
        //authorRepository: Repository<Author>
    ) {
        listOf(emiliaRomero, lucianoVega, valentinaSosa, mateoLopez)
            .forEach { userRepository.create(it) }

        listOf(n1984, elProceso, crimen, orgullo, guerraPaz, losMiserables, alquimista, extranjero)
            .forEach { bookRepository.create(it) }

        listOf(granGatsby, adiosArmas, monteCristo, vueltaMundo, senoraDalloway, cuentosMisterio, fundacion, cienAnios)
            .forEach { bookRepository.create(it) }

        listOf(huckFinn, ficciones, rayuela, ensayoCeguera, montagnaMagica, caminoSwann, jardinCerezos, harryPotter)
            .forEach { bookRepository.create(it) }

        listOf(reservaEmilia1, reservaEmilia2, reservaLuciano1, reservaLuciano2, reservaValentina1, reservaValentina2, reservaMateo1, reservaMateo2)
            .forEach { reservationRepository.create(it) }
    }
}