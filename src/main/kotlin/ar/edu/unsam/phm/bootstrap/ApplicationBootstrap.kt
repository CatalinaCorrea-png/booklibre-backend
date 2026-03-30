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
        cel = "1112345678",
        location = "Buenos Aires, AR",
        timestamp = "27/10/2021",
        userType = UserTypes.COMBINED,
        password = "123456",
        bibliokarmas = 110,
        img = "/assets/emilia_romero_avatar.png"
    )

    val lucianoVega = User(
        name = "Luciano Vega",
        description = "Fanático de la ciencia ficción",
        email = "luciano@example.com",
        cel = "1187654321",
        location = "Rosario, AR",
        userType = UserTypes.READER,
        bibliokarmas = 980,
        password = "123456",
        timestamp = "14/02/2016",
        img = "/assets/luciano_vega_avatar.png"
    )

    val valentinaSosa = User(
        name = "Valentina Sosa",
        description = "Escritora y lectora compulsiva",
        email = "valentina@example.com",
        cel = "1155550000",
        location = "Córdoba, AR",
        userType = UserTypes.PUBLISHER,
        bibliokarmas = 1500,
        timestamp = "10/01/2023",
        img = "/assets/valentina_sosa_avatar.png",
        password = "123456",
    )

    val mateoLopez = User(
        name = "Mateo López",
        description = "Lector ocasional, coleccionista serio",
        email = "mateo@example.com",
        cel = "1133337777",
        location = "Mendoza, AR",
        userType = UserTypes.COMBINED,
        bibliokarmas = 420,
        password = "123456",
        timestamp = "01/02/2024",
        img = "/assets/mateo_lopez_avatar.png"
    )

    // ─── Libros Comunes (8) ───────────────────────────────────────────────────
    // Owners: emilia→ n1984, guerraPaz | luciano→ elProceso, losMiserables
    //         valentina→ crimen, alquimista | mateo→ orgullo, extranjero

    val n1984 = Common().apply {
        title       = "1984"
        isbn        = "978-0-452-28423-4"
        desc        = "En un futuro totalitario, Winston Smith vive bajo la vigilancia omnipresente del Gran Hermano, una figura cuyo rostro aparece en carteles por toda Oceanía. El Partido controla no solo las acciones de sus ciudadanos, sino también sus pensamientos, reescribiendo el pasado para que la historia siempre justifique el presente. Winston trabaja en el Ministerio de la Verdad, donde su tarea consiste precisamente en falsificar registros históricos. En secreto, comienza a cuestionar el sistema y se enamora de Julia, una joven rebelde con quien comparte una relación clandestina y peligrosa. Juntos buscan contactar a la resistencia, pero en un mundo donde hasta los sueños pueden ser traicionados, la libertad tiene un precio devastador. Una de las novelas más influyentes del siglo XX sobre el poder, la vigilancia y la resistencia humana."
        gender      = Gender.SCIENCE_FICTION
        author      = orwell
        numPages    = 328
        language    = Language.SPANISH
        editorial   = "Secker & Warburg"
        publishDate = LocalDate.of(1949, 6, 8)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
        imageSrc    = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz9gIAgf5hTagXaQZl8ayY6FF26n2qirXQMg&s"
        timestamp   = LocalDate.of(2026, 1, 21)
    }

    val elProceso = Common().apply {
        title       = "El Proceso"
        isbn        = "978-84-206-3667-2"
        desc        = "Josef K. se despierta una mañana para descubrir que ha sido arrestado, aunque nadie le dice de qué se le acusa. A partir de ese momento, su vida entera queda subordinada a un proceso judicial absurdo e incomprensible, manejado por una burocracia laberíntica que nunca muestra su verdadero rostro. Josef intenta encontrar abogados, contactar jueces, entender las reglas de un sistema que parece no tenerlas, mientras su trabajo y sus relaciones personales se deterioran lentamente. Kafka construye una pesadilla lógica donde la culpa no necesita causa y la inocencia no ofrece protección. Una obra cumbre del absurdo existencial que, publicada póstumamente, sigue siendo una de las alegorías más poderosas sobre la opresión institucional y la fragilidad del individuo frente al poder."
        gender      = Gender.DRAMA
        author      = kafka
        numPages    = 255
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1925, 4, 26)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
        imageSrc    = "https://acdn-us.mitiendanube.com/stores/001/168/109/products/el-proceso1-d60e6b26de70d743d015882612962062-1024-1024.webp?w=1920"
        timestamp   = LocalDate.of(2024, 3, 1)
    }

    val crimen = Common().apply {
        title       = "Crimen y Castigo"
        isbn        = "978-84-376-0494-7"
        desc        = "Rodión Raskolnikov es un estudiante pobre de San Petersburgo que ha desarrollado una teoría según la cual ciertos hombres extraordinarios tienen el derecho moral de transgredir la ley en pos de un bien mayor. Convencido de pertenecer a esa categoría, asesina a una anciana usurera y a su hermana, que resulta ser testigo inesperado del crimen. Lejos de liberarlo, el acto lo condena a un tormento psicológico devastador. Dostoyevski narra con una precisión brutal el deterioro mental de Raskolnikov, su enfrentamiento con el astuto inspector Porfiry, y su gradual acercamiento a Sonia, una joven que sobrevive en la miseria con una fe inquebrantable. Una exploración sin igual de la culpa, la redención y las contradicciones del alma humana."
        gender      = Gender.DRAMA
        author      = dostoevsky
        numPages    = 545
        language    = Language.SPANISH
        editorial   = "Cátedra"
        publishDate = LocalDate.of(1866, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = valentinaSosa
        imageSrc    = "https://acdn-us.mitiendanube.com/stores/004/008/965/products/img_8468-dfbcfc91acd4498ad217537263442873-480-0.webp"
        timestamp   = LocalDate.of(2021, 2, 9)
    }

    val orgullo = Common().apply {
        title       = "Orgullo y Prejuicio"
        isbn        = "978-0-439-70818-8"
        desc        = "La familia Bennet tiene cinco hijas y una madre empeñada en casarlas bien antes de que la fortuna familiar desaparezca. Cuando el rico y apuesto Mr. Bingley llega al vecindario acompañado de su aún más rico pero distante amigo Mr. Darcy, el escenario queda listo para una serie de malentendidos, orgullos heridos y prejuicios difíciles de superar. Elizabeth Bennet, la segunda hija, es inteligente, ingeniosa y poco dispuesta a casarse sin amor, lo que la convierte en un personaje revolucionario para su época. Austen teje con ironía fina una crítica a las convenciones sociales del siglo XIX, mostrando cómo tanto el orgullo de Darcy como los prejuicios de Elizabeth deben ceder ante una verdad más profunda: que el amor genuino requiere conocerse a uno mismo antes de conocer al otro."
        gender      = Gender.ROMANCE
        author      = austen
        numPages    = 432
        language    = Language.SPANISH
        editorial   = "Penguin Clásicos"
        publishDate = LocalDate.of(1813, 1, 28)
        condition   = BookCondition.EXCELLENT
        owner       = mateoLopez
        imageSrc    = "https://images.cdn2.buscalibre.com/fit-in/360x360/5f/b0/5fb0cb647320eede167a469ee4b648bf.jpg"
        timestamp   = LocalDate.of(2019, 6, 1)
    }

    val guerraPaz = Common().apply {
        title       = "Guerra y Paz"
        isbn        = "978-84-9107-186-3"
        desc        = "Considerada una de las novelas más grandes jamás escritas, Guerra y Paz sigue a varias familias de la aristocracia rusa durante las guerras napoleónicas de principios del siglo XIX. A través de personajes como el idealista Pierre Bezukhov, el ambicioso Andréi Bolkonsky y la luminosa Natasha Rostova, Tolstói explora el amor, la muerte, la guerra, la fe y el sentido de la existencia humana a una escala épica sin precedentes. Las batallas de Austerlitz y Borodinó cobran vida con una precisión histórica asombrosa, mientras la vida cotidiana de las familias nobles transcurre con una intimidad que hace que cada personaje resulte profundamente real. Una obra que exige entrega pero recompensa al lector con una visión del mundo incomparable."
        gender      = Gender.CLASSIC_LITERATURE
        author      = tolstoy
        numPages    = 1225
        language    = Language.SPANISH
        editorial   = "Alba Editorial"
        publishDate = LocalDate.of(1869, 1, 1)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
        imageSrc    = "https://http2.mlstatic.com/D_NQ_NP_689496-MLA78230208406_082024-O.webp"
        timestamp   = LocalDate.of(2024, 12, 1)
    }

    val losMiserables = Common().apply {
        title       = "Los Miserables"
        isbn        = "978-84-8428-019-7"
        desc        = "Jean Valjean pasa diecinueve años en prisión por robar un pan para alimentar a su familia. Al salir, marcado como ex convicto, la sociedad le cierra todas las puertas hasta que un obispo le ofrece misericordia en lugar de condena. Ese acto de bondad transforma su vida y lo convierte en un hombre justo que, sin embargo, siempre será perseguido por el inflexible inspector Javert, para quien la ley no admite redención. A lo largo de décadas, Valjean protege a Cosette, la hija de la desdichada Fantine, mientras París bulle con tensiones sociales que desembocarán en la revolución de 1832. Hugo retrata con compasión y grandeza a los olvidados de la sociedad, construyendo una de las historias más emotivas y universales de la literatura occidental."
        gender      = Gender.CLASSIC_LITERATURE
        author      = hugo
        numPages    = 1232
        language    = Language.SPANISH
        editorial   = "Planeta"
        publishDate = LocalDate.of(1862, 1, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
        imageSrc    = "https://http2.mlstatic.com/D_NQ_NP_762363-MLM49917565139_052022-O.webp"
        timestamp   = LocalDate.of(2025, 10, 21)
    }

    val alquimista = Common().apply {
        title       = "El Alquimista"
        isbn        = "978-84-08-04325-6"
        desc        = "Santiago es un joven pastor andaluz que sueña repetidamente con un tesoro escondido junto a las pirámides de Egipto. Dejando atrás su rebaño y sus certezas, emprende un viaje que lo llevará a cruzar el estrecho de Gibraltar, adentrarse en los mercados de Tánger, atravesar el desierto del Sahara y vivir en un oasis donde conocerá el amor. A lo largo del camino, encuentra a personajes que le enseñan a escuchar el lenguaje del universo y a confiar en su Leyenda Personal, ese destino único que cada ser humano tiene el deber y el derecho de cumplir. Coelho construye una fábula filosófica sobre la valentía de perseguir los sueños, el valor del presente y la idea de que cuando alguien desea algo con todo su ser, el universo conspira para ayudarlo a lograrlo."
        gender      = Gender.SELF_HELP
        author      = coelho
        numPages    = 208
        language    = Language.PORTUGUESE
        editorial   = "Planeta"
        publishDate = LocalDate.of(1988, 1, 1)
        condition   = BookCondition.EXCELLENT
        owner       = valentinaSosa
        imageSrc    = "https://tienda.planetadelibros.com.ar/cdn/shop/files/ElalquimistaBK_Fte.jpg?v=1730985825"
        timestamp   = LocalDate.of(2016, 6, 6)
    }

    val extranjero = Common().apply {
        title       = "El Extranjero"
        isbn        = "978-84-08-04999-9"
        desc        = "Meursault, un empleado francés que vive en Argelia, no llora en el funeral de su madre. Días después, en una playa deslumbrante bajo el sol africano, mata a un árabe de manera casi accidental. Lo que sigue no es exactamente un juicio por el crimen, sino un proceso donde la sociedad lo condena por su incapacidad de fingir emociones que no siente. Camus construye con una prosa seca y solar una de las grandes novelas del absurdo: la historia de un hombre que vive al margen de las convenciones morales no por crueldad sino por una especie de honestidad radical. El Extranjero es una meditación sobre la indiferencia del universo, la libertad ante la muerte y el absurdo como condición fundamental de la existencia humana."
        gender      = Gender.DRAMA
        author      = camus
        numPages    = 159
        language    = Language.FRENCH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1942, 1, 1)
        condition   = BookCondition.GOOD
        owner       = mateoLopez
        imageSrc    = "https://m.media-amazon.com/images/I/71mLWMj0sQL._AC_UF1000,1000_QL80_.jpg"
        timestamp   = LocalDate.of(2025, 7, 8)
    }

    // ─── Libros Con Dedicatoria (8) ───────────────────────────────────────────
    // Owners: emilia→ granGatsby, senoraDalloway | luciano→ adiosArmas, cuentosMisterio
    //         valentina→ monteCristo, fundacion | mateo→ vueltaMundo, cienAnios

    val granGatsby = WithADedication().apply {
        title       = "El Gran Gatsby"
        isbn        = "978-84-206-8256-3"
        desc        = "En los dorados años veinte, Jay Gatsby organiza fiestas legendarias en su mansión de Long Island, pero nadie sabe realmente quién es ni de dónde viene su fortuna. Nick Carraway, su vecino y primo de Daisy Buchanan, se convierte en testigo privilegiado de la obsesión de Gatsby por reconquistar a Daisy, el amor que perdió años atrás cuando era pobre y ella eligió casarse con el poderoso Tom Buchanan. Fitzgerald usa esta historia de amor imposible para diseccionar el sueño americano: la ilusión de que el dinero puede comprar el pasado, de que la riqueza garantiza la felicidad, de que el esfuerzo siempre es recompensado. Debajo del brillo de las fiestas y los automóviles lujosos, late una melancolía profunda y una crítica mordaz a la hipocresía de la clase alta estadounidense."
        gender      = Gender.CLASSIC_LITERATURE
        author      = fitzgerald
        numPages    = 180
        language    = Language.SPANISH
        editorial   = "Scribner"
        publishDate = LocalDate.of(1925, 4, 10)
        condition   = BookCondition.GOOD
        owner       = emiliaRomero
        imageSrc    = "https://http2.mlstatic.com/D_NQ_NP_980687-MLU78007366453_072024-O.webp"
        timestamp   = LocalDate.of(2026, 3, 17)
    }

    val adiosArmas = WithADedication().apply {
        title       = "Adiós a las Armas"
        isbn        = "978-0-7432-7356-5"
        desc        = "El teniente Frederic Henry es un oficial estadounidense que sirve como conductor de ambulancias en el frente italiano durante la Primera Guerra Mundial. En un hospital de campaña conoce a Catherine Barkley, una enfermera británica con quien desarrolla una relación que pasa del flirteo a un amor profundo y desesperado. La guerra los rodea con su violencia absurda, la retirada de Caporetto los separa de todo lo conocido, y juntos intentan construir una vida al margen del conflicto que los amenaza. Hemingway escribe con su característica prosa desnuda y directa, donde lo que no se dice pesa tanto como lo que se dice. Una historia de amor y pérdida que es también un retrato despiadado de la guerra como máquina trituradora de vidas y esperanzas."
        gender      = Gender.DRAMA
        author      = hemingway
        numPages    = 332
        language    = Language.SPANISH
        editorial   = "Scribner"
        publishDate = LocalDate.of(1929, 9, 27)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
        imageSrc    = "https://www.penguinlibros.com/ar/1595223/adios-a-las-armas.jpg"
        timestamp   = LocalDate.of(2024, 10, 9)
    }

    val monteCristo = WithADedication().apply {
        title       = "El Conde de Montecristo"
        isbn        = "978-0-684-80146-2"
        desc        = "Edmond Dantès es un joven marino marsellés a punto de casarse con la mujer que ama y de convertirse en capitán de su barco, cuando una conspiración de envidia y ambición lo arroja injustamente a la prisión del Castillo de If. Allí pasa catorce años de encierro, aprende todo lo que hay que saber del mundo de la mano del abate Faria, y descubre la ubicación de un tesoro legendario en la isla de Montecristo. Al escapar, regresa al mundo transformado en el misterioso y riquísimo Conde de Montecristo, con un solo objetivo: ejecutar una venganza meticulosa, paciente y elaborada contra cada uno de los hombres que destruyeron su vida. Dumas construye un torbellino narrativo de intriga, justicia poética y acción que no da tregua al lector."
        gender      = Gender.CLASSIC_LITERATURE
        author      = dumas
        numPages    = 1276
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1844, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = valentinaSosa
        imageSrc    = "https://www.penguinlibros.com/ar/6234239-large_default/el-conde-de-montecristo.webp"
        timestamp   = LocalDate.of(2022, 1, 6)
    }

    val vueltaMundo = WithADedication().apply {
        title       = "La Vuelta al Mundo en 80 Días"
        isbn        = "978-84-206-9942-4"
        desc        = "Phileas Fogg es un caballero inglés de hábitos tan rígidos e inexplicables que sus vecinos sospechan que es un autómata. En el Reform Club de Londres, apuesta toda su fortuna a que puede dar la vuelta al mundo en exactamente ochenta días, algo que los periódicos de la época sugieren como posible gracias a los avances del ferrocarril y el barco de vapor. Acompañado de su nuevo sirviente francés Passepartout, emprende una carrera contra el tiempo a través de Europa, India, China, el Pacífico, Estados Unidos y el Atlántico, mientras el detective Fix lo persigue convencido de que es un ladrón de banco. Verne combina la aventura, el humor y la fascinación por la tecnología en una novela que sigue siendo imposible de soltar más de ciento cincuenta años después."
        gender      = Gender.SCIENCE_FICTION
        author      = verne
        numPages    = 304
        language    = Language.FRENCH
        editorial   = "Hetzel"
        publishDate = LocalDate.of(1872, 1, 1)
        condition   = BookCondition.EXCELLENT
        owner       = mateoLopez
        imageSrc    = "https://images.cdn2.buscalibre.com/fit-in/360x360/1f/cb/1fcbcd4165d3c7eababb3e92dff6972c.jpg"
        timestamp   = LocalDate.of(2022, 1, 1)
    }

    val senoraDalloway = WithADedication().apply {
        title       = "La Señora Dalloway"
        isbn        = "978-84-670-2347-8"
        desc        = "En un único día de junio en el Londres de posguerra, Clarissa Dalloway prepara una fiesta que dará esa noche. Mientras compra flores, saluda a conocidos y arregla los últimos detalles, su mente viaja constantemente al pasado: a su juventud en Bourton, a Peter Walsh, el hombre al que amó y rechazó, a las elecciones que definieron su vida. Paralelamente, Septimus Warren Smith, un veterano de guerra que sufre lo que hoy llamaríamos estrés postraumático, recorre la ciudad con su esposa italiana, acechado por visiones y voces. Woolf entreteje ambas historias con una técnica de flujo de conciencia revolucionaria para su época, creando una meditación poética y profunda sobre el tiempo, la memoria, la identidad, la locura y el precio invisible que cobra la vida en sociedad."
        gender      = Gender.DRAMA
        author      = woolf
        numPages    = 194
        language    = Language.ENGLISH
        editorial   = "Hogarth Press"
        publishDate = LocalDate.of(1925, 5, 14)
        condition   = BookCondition.VERY_GOOD
        owner       = emiliaRomero
        imageSrc    = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSnPl2enENU9OdvIh58PC0QuIJ_g0-wYbc3XQ&s"
        timestamp   = LocalDate.of(2018, 2, 12)
    }

    val cuentosMisterio = WithADedication().apply {
        title       = "Cuentos de Misterio e Imaginación"
        isbn        = "978-0-156-62870-9"
        desc        = "Edgar Allan Poe es el maestro indiscutido del cuento de terror psicológico, y esta colección reúne sus relatos más perturbadores e influyentes. En El corazón delator, un asesino es traicionado por el latido que cree escuchar bajo el suelo donde enterró a su víctima. En La caída de la casa Usher, una mansión decadente parece respirar junto a sus últimos ocupantes. En El pozo y el péndulo, un prisionero de la Inquisición enfrenta torturas de una crueldad mecánica y perfecta. Poe construye atmósferas densas y opresivas donde el horror no proviene de criaturas sobrenaturales sino de la mente humana desintegrándose bajo el peso de la culpa, la obsesión y el miedo. Una colección que fundó géneros enteros y que todavía hoy resulta absolutamente perturbadora."
        gender      = Gender.DRAMA
        author      = poe
        numPages    = 424
        language    = Language.SPANISH
        editorial   = "Alianza Editorial"
        publishDate = LocalDate.of(1840, 1, 1)
        condition   = BookCondition.GOOD
        owner       = lucianoVega
        imageSrc    = "https://panamericana.vtexassets.com/arquivos/ids/525902/cuentos-de-misterio-e-imaginacion-2-9788418211997.jpg?v=638407572538400000"
        timestamp   = LocalDate.of(2025, 5, 14)
    }

    val fundacion = WithADedication().apply {
        title       = "Fundación"
        isbn        = "978-84-206-1326-4"
        desc        = "El matemático Hari Seldon ha desarrollado la psicohistoria, una ciencia capaz de predecir el comportamiento de grandes masas humanas con precisión estadística. Sus cálculos revelan algo aterrador: el Imperio Galáctico, que lleva doce mil años en pie, está condenado a colapsar en menos de un siglo, inaugurando treinta mil años de barbarie y oscuridad. Para reducir ese período a tan solo uno, Seldon funda en el extremo del universo una colonia de científicos y enciclopedistas llamada la Fundación. A lo largo de generaciones, los habitantes de ese planeta deberán navegar crisis cuidadosamente sembradas por Seldon, donde siempre habrá exactamente una salida. Asimov construye una space opera de ideas que es también una reflexión sobre el determinismo histórico, el conocimiento como poder y la fragilidad de las civilizaciones."
        gender      = Gender.SCIENCE_FICTION
        author      = asimov
        numPages    = 255
        language    = Language.ENGLISH
        editorial   = "Gnome Press"
        publishDate = LocalDate.of(1951, 5, 1)
        condition   = BookCondition.EXCELLENT
        owner       = valentinaSosa
        imageSrc    = "https://m.media-amazon.com/images/S/compressed.photo.goodreads.com/books/1170429948i/53687.jpg"
        timestamp   = LocalDate.of(2023, 7, 25)
    }

    val cienAnios = WithADedication().apply {
        title       = "Cien Años de Soledad"
        isbn        = "978-0-553-29335-7"
        desc        = "José Arcadio Buendía funda Macondo en medio de la selva colombiana con un grupo de familias que buscan un lugar nuevo para vivir. A lo largo de siete generaciones, los Buendía repiten patrones de amor, guerra, locura y soledad que parecen escritos de antemano. García Márquez mezcla sin esfuerzo aparente lo cotidiano y lo sobrenatural: los muertos conviven con los vivos, llueven flores amarillas, una gitana levita, y un hombre arrastra su sombra de culpa hasta la muerte. La novela es al mismo tiempo la historia de una familia, de un pueblo, de un continente y de la condición humana. Publicada en 1967, se convirtió en el texto fundacional del realismo mágico latinoamericano y en una de las novelas más leídas y celebradas de todos los tiempos. Ganó el Premio Nobel para su autor en 1982."
        gender      = Gender.CLASSIC_LITERATURE
        author      = garcia
        numPages    = 471
        language    = Language.SPANISH
        editorial   = "Sudamericana"
        publishDate = LocalDate.of(1967, 5, 30)
        condition   = BookCondition.VERY_GOOD
        owner       = mateoLopez
        imageSrc    = "https://assets.lectulandia.co/b/ab/Gabriel%20Garcia%20Marquez/Cien%20anos%20de%20soledad%20Edicion%20conmemorativa%20(1)/big.jpg"
        timestamp   = LocalDate.of(2025, 10, 3)
    }

    // ─── Libros Coleccionables (8) ────────────────────────────────────────────
    // Owners: emilia→ huckFinn, montagnaMagica | luciano→ ficciones, caminoSwann
    //         valentina→ rayuela, jardinCerezos | mateo→ ensayoCeguera, harryPotter

    val huckFinn = Collectable().apply {
        title       = "Las Aventuras de Huckleberry Finn"
        isbn        = "978-84-397-2077-5"
        desc        = "Huck Finn es el hijo de un borracho violento que escapa de su padre y de la vida civilizada que la viuda Douglas intenta imponerle. En su huida, se une a Jim, un esclavo que también huye buscando la libertad, y juntos navegan el río Mississippi en una balsa. Lo que comienza como una aventura se convierte en un viaje moral: Huck debe decidir entre obedecer las leyes de una sociedad que considera a Jim una propiedad, o seguir su conciencia y proteger a su amigo. Twain escribe con el dialecto del sur profundo y un humor que deja al descubierto la hipocresía y la crueldad del racismo de su época. Considerada la gran novela americana por muchos críticos, es también una de las primeras obras de la literatura mundial narradas en la voz auténtica de un niño."
        gender      = Gender.CLASSIC_LITERATURE
        author      = twain
        numPages    = 366
        language    = Language.ENGLISH
        editorial   = "Chatto & Windus"
        publishDate = LocalDate.of(1884, 12, 10)
        condition   = BookCondition.EXCELLENT
        owner       = emiliaRomero
        imageSrc    = "https://www.edicontinente.com.ar/image/titulos/9788426141057.jpg"
        timestamp   = LocalDate.of(2025, 3, 30)
    }

    val ficciones = Collectable().apply {
        title       = "Ficciones"
        isbn        = "978-0-486-28061-3"
        desc        = "Jorge Luis Borges construye en este libro una serie de mundos imposibles con la precisión de un matemático y la imaginación de un mago. En La Biblioteca de Babel, el universo entero es una biblioteca infinita que contiene todos los libros posibles. En El jardín de senderos que se bifurcan, el tiempo es un laberinto de caminos paralelos. En Tlön, Uqbar, Orbis Tertius, una sociedad secreta inventa un mundo tan detallado que comienza a reemplazar al real. En Funes el memorioso, un hombre que recuerda cada detalle de cada instante descubre que la perfección de la memoria puede ser una condena. Cada cuento es un ejercicio deslumbrante de inteligencia literaria que cuestiona la naturaleza de la realidad, el tiempo, la identidad y el conocimiento. Ficciones es una de las obras más influyentes del siglo XX en cualquier idioma."
        gender      = Gender.SCIENCE_FICTION
        author      = borges
        numPages    = 174
        language    = Language.SPANISH
        editorial   = "Sur"
        publishDate = LocalDate.of(1944, 1, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = lucianoVega
        imageSrc    = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSm6k93G1ce4FkEE8FYXOsApKJfGO-_xD5-tQ&s"
        timestamp   = LocalDate.of(2025, 2, 3)
    }

    val rayuela = Collectable().apply {
        title       = "Rayuela"
        isbn        = "978-84-206-9550-1"
        desc        = "Horacio Oliveira, un intelectual argentino, vaga por el París de los años cincuenta en busca de algo que no sabe nombrar, acompañado por la enigmática y luminosa Maga, con quien comparte una relación hecha de amor y malentendidos. Tras una tragedia que rompe ese círculo bohemio, Oliveira regresa a Buenos Aires, donde continúa su búsqueda errática entre amigos y situaciones absurdas. Pero Rayuela no es solo una novela sobre personajes: es una revolución formal. Cortázar incluye un tablero de instrucciones que propone dos formas de lectura completamente distintas, convirtiendo al lector en coautor del libro. Los capítulos prescindibles, las digresiones filosóficas y los juegos de lenguaje transforman cada lectura en una experiencia única. Una obra que cambió para siempre la idea de lo que puede ser una novela."
        gender      = Gender.DRAMA
        author      = cortazar
        numPages    = 635
        language    = Language.SPANISH
        editorial   = "Sudamericana"
        publishDate = LocalDate.of(1963, 6, 28)
        condition   = BookCondition.GOOD
        owner       = valentinaSosa
        imageSrc    = "https://images.cdn3.buscalibre.com/fit-in/360x360/90/53/905322d10841b36aa311dbd5c90d92ed.jpg"
        timestamp   = LocalDate.of(2025, 5, 29)
    }

    val ensayoCeguera = Collectable().apply {
        title       = "Ensayo sobre la Ceguera"
        isbn        = "978-84-322-3802-6"
        desc        = "En una ciudad sin nombre, un hombre se queda ciego de repente mientras espera en un semáforo. En pocas horas, la ceguera se propaga como una epidemia: una ceguera blanca, luminosa, que lo invade todo. Las autoridades recluyen a los afectados en un manicomio abandonado, donde sin organización ni recursos, el orden social colapsa rápidamente y emerge lo peor del ser humano: la violencia, el abuso y la brutalidad. Solo una mujer, la esposa de un médico, conserva la vista en secreto y guía a un pequeño grupo hacia la supervivencia. Saramago escribe sin nombres propios, sin puntos aparte y con una prosa que fluye como un río oscuro, construyendo una alegoría devastadora sobre la fragilidad de la civilización y nuestra ceguera moral colectiva ante el sufrimiento ajeno."
        gender      = Gender.DRAMA
        author      = saramago
        numPages    = 310
        language    = Language.PORTUGUESE
        editorial   = "Caminho"
        publishDate = LocalDate.of(1995, 1, 1)
        condition   = BookCondition.REGULAR
        owner       = mateoLopez
        imageSrc    = "https://www.penguinlibros.com/ar/3537745-large_default/ensayo-sobre-la-ceguera.webp"
        timestamp   = LocalDate.of(2023, 1, 12)
    }

    val montagnaMagica = Collectable().apply {
        title       = "La Montaña Mágica"
        isbn        = "978-84-306-0360-9"
        desc        = "Hans Castorp viaja a los Alpes suizos para visitar a su primo enfermo en el Sanatorio Internacional Berghof, donde los tuberculosos de la alta sociedad europea pasan meses o años en reposo. Lo que iba a ser una visita de tres semanas se convierte en una estancia de siete años. Atrapado en ese mundo suspendido entre la vida y la muerte, Hans entabla conversaciones filosóficas interminables con Settembrini, un humanista italiano defensor de la razón y el progreso, y Naphta, un jesuita oscuro que defiende la fe y la autoridad. Mann convierte este sanatorio en una alegoría de Europa antes de la Primera Guerra Mundial, donde las ideas compiten con la misma urgencia que los pacientes luchan contra la enfermedad. Una novela monumental sobre el tiempo, la enfermedad, la política y la búsqueda de sentido."
        gender      = Gender.CLASSIC_LITERATURE
        author      = mann
        numPages    = 720
        language    = Language.FRENCH
        editorial   = "S. Fischer Verlag"
        publishDate = LocalDate.of(1924, 11, 1)
        condition   = BookCondition.VERY_GOOD
        owner       = emiliaRomero
        imageSrc    = "https://images.cdn3.buscalibre.com/fit-in/360x360/75/56/7556ee308c4a24d1a4ea1be13b9ee928.jpg"
        timestamp   = LocalDate.of(2023, 2, 1)
    }

    val caminoSwann = Collectable().apply {
        title       = "Por el Camino de Swann"
        isbn        = "978-84-350-0185-4"
        desc        = "El narrador moja una magdalena en una taza de té y, con ese gesto involuntario, el pasado entero regresa con una vividez abrumadora. Así comienza el primer volumen de En busca del tiempo perdido, la obra monumental de Marcel Proust. El libro se divide en dos grandes partes: en la primera, el narrador recuerda su infancia en el pueblo de Combray, los veranos en casa de los tíos, los paseos por el jardín y las visitas del señor Swann. En la segunda, reconstruye la historia de amor de Swann por Odette, una cortesana que no es su tipo pero de quien se enamora con una obsesión devastadora. Proust escribe oraciones que pueden durar páginas enteras, explorando los mecanismos de la memoria, el deseo y el tiempo con una profundidad y una belleza sin parangón en la literatura universal."
        gender      = Gender.CLASSIC_LITERATURE
        author      = proust
        numPages    = 512
        language    = Language.FRENCH
        editorial   = "Grasset"
        publishDate = LocalDate.of(1913, 11, 14)
        condition   = BookCondition.EXCELLENT
        owner       = lucianoVega
        imageSrc    = "https://upload.wikimedia.org/wikipedia/commons/e/ee/Por_el_camino_de_Swann-Espasa-Calpe1920-01.jpg"
        timestamp   = LocalDate.of(2023, 5, 21)
    }

    val jardinCerezos = Collectable().apply {
        title       = "El Jardín de los Cerezos"
        isbn        = "978-84-663-0012-7"
        desc        = "Liubov Ranevskaya regresa a Rusia después de años en Francia, donde huyó tras una serie de tragedias personales. La espera en su hacienda familiar, con su magnífico jardín de cerezos en flor, no puede durar: las deudas son insostenibles y la propiedad debe venderse. Lopajin, un empresario de origen campesino que de niño trabajó en esa misma casa, propone cortar los cerezos y construir dachas para turistas. La familia, incapaz de aceptar el fin de una época, se niega a ver la realidad hasta que es demasiado tarde. Chéjov construye una tragicomedia donde nadie es villano y todos son víctimas de su propia incapacidad para adaptarse al cambio. Con diálogos que parecen hablar de una cosa y dicen otra, retrata el ocaso de la aristocracia rusa con melancolía, humor y una ternura infinita."
        gender      = Gender.DRAMA
        author      = chekhov
        numPages    = 112
        language    = Language.SPANISH
        editorial   = "Cátedra"
        publishDate = LocalDate.of(1904, 1, 17)
        condition   = BookCondition.GOOD
        owner       = valentinaSosa
        imageSrc    = "https://images.cdn2.buscalibre.com/fit-in/360x360/4b/33/4b3304f77876c25cd3e8babde159401d.jpg"
        timestamp   = LocalDate.of(2024, 1, 10)
    }

    val harryPotter = Collectable().apply {
        title       = "Harry Potter y la Piedra Filosofal"
        isbn        = "978-84-376-0233-2"
        desc        = "Harry Potter tiene once años y vive en un armario debajo de la escalera de la casa de sus tíos, quienes lo tratan como un estorbo y le ocultan un secreto fundamental: que sus padres no murieron en un accidente de tráfico, sino a manos del mago más oscuro que el mundo mágico haya conocido, y que Harry, de algún modo inexplicable, sobrevivió a ese ataque siendo un bebé. Cuando las cartas de Hogwarts comienzan a llegar, se abre una puerta a un mundo paralelo de hechizos, varitas, fantasmas y criaturas mágicas. En la escuela, Harry hace sus primeros amigos verdaderos, Ron y Hermione, descubre que es un talentoso jugador de Quidditch y se enfrenta por primera vez a fuerzas que buscan resucitar al que no debe ser nombrado. Rowling construyó un universo tan detallado y coherente que generaciones enteras crecieron deseando recibir su carta de Hogwarts."
        gender      = Gender.SCIENCE_FICTION
        author      = rowling
        numPages    = 309
        language    = Language.ENGLISH
        editorial   = "Bloomsbury"
        publishDate = LocalDate.of(1997, 6, 26)
        condition   = BookCondition.VERY_GOOD
        owner       = mateoLopez
        imageSrc    = "https://images.cdn2.buscalibre.com/fit-in/360x360/e6/5f/e65f54742ad7bbc41903d17f75b77d78.jpg"
        timestamp   = LocalDate.of(2026, 1, 10)
    }

    // ─── Reservas pasadas (finalizadas — libros leídos) ───────────────────────

    val reservaEmiliaPasada1 = Reservation(
        user = emiliaRomero, book = elProceso,
        review = Review(reviewerName = emiliaRomero.name, rating = 4,
            review = "Kafkiano en el mejor sentido. La burocracia como pesadilla existencial, muy bien logrado.",
            timestamp = LocalDate.of(2025, 8, 10)),
        pickUpDate = LocalDate.of(2025, 7, 20), dropOffDate = LocalDate.of(2025, 8, 9),
    )

    val reservaEmiliaPasada2 = Reservation(
        user = emiliaRomero, book = adiosArmas,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "Hemingway en su máxima expresión. El final me dejó sin palabras.",
            timestamp = LocalDate.of(2025, 10, 15)),
        pickUpDate = LocalDate.of(2025, 9, 25), dropOffDate = LocalDate.of(2025, 10, 14),
    )

    val reservaEmiliaPasada3 = Reservation(
        user = emiliaRomero, book = rayuela,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "Una experiencia única. Lo leí en orden lineal y luego saltando capítulos, totalmente diferente.",
            timestamp = LocalDate.of(2026, 1, 20)),
        pickUpDate = LocalDate.of(2025, 12, 28), dropOffDate = LocalDate.of(2026, 1, 19),
    )

    val reservaLucianoPasada1 = Reservation(
        user = lucianoVega, book = granGatsby,
        review = Review(reviewerName = lucianoVega.name, rating = 3,
            review = "Bella prosa, pero el protagonista me resultó difícil de empatizar. Vale la pena igual.",
            timestamp = LocalDate.of(2025, 6, 5)),
        pickUpDate = LocalDate.of(2025, 5, 15), dropOffDate = LocalDate.of(2025, 6, 4),
    )

    val reservaLucianoPasada2 = Reservation(
        user = lucianoVega, book = crimen,
        review = Review(reviewerName = lucianoVega.name, rating = 5,
            review = "Dostoyevski entiende la psicología humana como nadie. Raskolnikov es aterrador y fascinante.",
            timestamp = LocalDate.of(2025, 9, 3)),
        pickUpDate = LocalDate.of(2025, 8, 12), dropOffDate = LocalDate.of(2025, 9, 2),
    )

    val reservaLucianoPasada3 = Reservation(
        user = lucianoVega, book = harryPotter,
        review = Review(reviewerName = lucianoVega.name, rating = 4,
            review = "Lo releí de adulto y sigue siendo mágico. Rowling construyó un mundo increíble.",
            timestamp = LocalDate.of(2026, 2, 1)),
        pickUpDate = LocalDate.of(2026, 4, 10), dropOffDate = LocalDate.of(2026, 4, 29),
    )

    val reservaValentinaPasada1 = Reservation(
        user = valentinaSosa, book = huckFinn,
        review = Review(reviewerName = valentinaSosa.name, rating = 4,
            review = "Una aventura atemporal. Twain critica la sociedad con humor fino.",
            timestamp = LocalDate.of(2025, 5, 20)),
        pickUpDate = LocalDate.of(2025, 4, 28), dropOffDate = LocalDate.of(2025, 5, 19),
    )

    val reservaValentinaPasada2 = Reservation(
        user = valentinaSosa, book = n1984,
        review = Review(reviewerName = valentinaSosa.name, rating = 5,
            review = "Imprescindible. Cada vez más vigente. Orwell era un visionario.",
            timestamp = LocalDate.of(2025, 11, 8)),
        pickUpDate = LocalDate.of(2025, 10, 18), dropOffDate = LocalDate.of(2025, 11, 7),
    )

    val reservaValentinaPasada3 = Reservation(
        user = valentinaSosa, book = caminoSwann,
        review = Review(reviewerName = valentinaSosa.name, rating = 4,
            review = "Proust exige paciencia pero recompensa con una belleza literaria incomparable.",
            timestamp = LocalDate.of(2026, 1, 5)),
        pickUpDate = LocalDate.of(2025, 12, 10), dropOffDate = LocalDate.of(2026, 1, 4),
    )

    val reservaMateoPasada1 = Reservation(
        user = mateoLopez, book = losMiserables,
        review = Review(reviewerName = mateoLopez.name, rating = 5,
            review = "Monumental. Victor Hugo logra que te importen profundamente personajes de hace dos siglos.",
            timestamp = LocalDate.of(2025, 7, 14)),
        pickUpDate = LocalDate.of(2025, 6, 10), dropOffDate = LocalDate.of(2025, 7, 13),
    )

    val reservaMateoPasada2 = Reservation(
        user = mateoLopez, book = montagnaMagica,
        review = Review(reviewerName = mateoLopez.name, rating = 3,
            review = "Filosóficamente rico pero denso. Hay que entrar con paciencia y tiempo.",
            timestamp = LocalDate.of(2025, 10, 29)),
        pickUpDate = LocalDate.of(2025, 9, 20), dropOffDate = LocalDate.of(2025, 10, 28),
    )

    val reservaMateoPasada3 = Reservation(
        user = mateoLopez, book = monteCristo,
        review = Review(reviewerName = mateoLopez.name, rating = 5,
            review = "La mejor historia de venganza jamás escrita. No pude soltarlo.",
            timestamp = LocalDate.of(2026, 2, 15)),
        pickUpDate = LocalDate.of(2026, 1, 22), dropOffDate = LocalDate.of(2026, 2, 14),
    )

    val reservaElProceso2 = Reservation(
        user = valentinaSosa, book = elProceso,
        review = Review(reviewerName = valentinaSosa.name, rating = 5,
            review = "Una obra que te deja paralizado. La burocracia como metáfora de la existencia.",
            timestamp = LocalDate.of(2025, 9, 12)),
        pickUpDate = LocalDate.of(2025, 8, 22), dropOffDate = LocalDate.of(2025, 9, 11),
    )

    val reservaElProceso3 = Reservation(
        user = mateoLopez, book = elProceso,
        review = Review(reviewerName = mateoLopez.name, rating = 4,
            review = "Kafka logra que te sientas atrapado junto al protagonista. Incómodo pero brillante.",
            timestamp = LocalDate.of(2025, 11, 30)),
        pickUpDate = LocalDate.of(2025, 11, 10), dropOffDate = LocalDate.of(2025, 11, 29),
    )

    val reservaElProceso4 = Reservation(
        user = emiliaRomero, book = elProceso,
        review = Review(reviewerName = emiliaRomero.name, rating = 3,
            review = "Me costó entrar pero una vez adentro no pude parar. La angustia de K. se siente real.",
            timestamp = LocalDate.of(2025, 6, 5)),
        pickUpDate = LocalDate.of(2025, 5, 15), dropOffDate = LocalDate.of(2025, 6, 4),
    )

    val reservaElProceso5 = Reservation(
        user = lucianoVega, book = elProceso,
        review = Review(reviewerName = lucianoVega.name, rating = 5,
            review = "El absurdo kafkiano en estado puro. Una pesadilla que no podés dejar de leer.",
            timestamp = LocalDate.of(2025, 7, 20)),
        pickUpDate = LocalDate.of(2025, 6, 30), dropOffDate = LocalDate.of(2025, 7, 19),
    )

    val reservaElProceso6 = Reservation(
        user = mateoLopez, book = elProceso,
        review = Review(reviewerName = mateoLopez.name, rating = 4,
            review = "La culpa sin causa explicada, qué incómodo y qué genial.",
            timestamp = LocalDate.of(2025, 10, 1)),
        pickUpDate = LocalDate.of(2025, 9, 11), dropOffDate = LocalDate.of(2025, 9, 30),
    )

    val reservaAdiosArmas2 = Reservation(
        user = valentinaSosa, book = adiosArmas,
        review = Review(reviewerName = valentinaSosa.name, rating = 4,
            review = "La guerra contada sin heroísmo, con una honestidad brutal. Hemingway no decepciona.",
            timestamp = LocalDate.of(2025, 7, 8)),
        pickUpDate = LocalDate.of(2025, 6, 18), dropOffDate = LocalDate.of(2025, 7, 7),
    )

    val reservaAdiosArmas3 = Reservation(
        user = mateoLopez, book = adiosArmas,
        review = Review(reviewerName = mateoLopez.name, rating = 3,
            review = "Buena prosa, aunque el ritmo se me hizo lento en el medio. El final salva todo.",
            timestamp = LocalDate.of(2026, 1, 14)),
        pickUpDate = LocalDate.of(2025, 12, 25), dropOffDate = LocalDate.of(2026, 1, 13),
    )

    val reservaRayuela2 = Reservation(
        user = lucianoVega, book = rayuela,
        review = Review(reviewerName = lucianoVega.name, rating = 4,
            review = "Cortázar rompe todo y lo reconstruye mejor. Exige concentración pero vale cada página.",
            timestamp = LocalDate.of(2025, 8, 28)),
        pickUpDate = LocalDate.of(2025, 8, 8), dropOffDate = LocalDate.of(2025, 8, 27),
    )

    val reservaRayuela3 = Reservation(
        user = mateoLopez, book = rayuela,
        review = Review(reviewerName = mateoLopez.name, rating = 5,
            review = "La mejor novela latinoamericana que leí. La estructura no lineal es un viaje mental.",
            timestamp = LocalDate.of(2026, 2, 20)),
        pickUpDate = LocalDate.of(2026, 1, 30), dropOffDate = LocalDate.of(2026, 2, 19),
    )

    val reservaGranGatsby2 = Reservation(
        user = valentinaSosa, book = granGatsby,
        review = Review(reviewerName = valentinaSosa.name, rating = 5,
            review = "El sueño americano desnudo. Fitzgerald escribe con una elegancia que duele.",
            timestamp = LocalDate.of(2025, 7, 2)),
        pickUpDate = LocalDate.of(2025, 6, 12), dropOffDate = LocalDate.of(2025, 7, 1),
    )

    val reservaGranGatsby3 = Reservation(
        user = mateoLopez, book = granGatsby,
        review = Review(reviewerName = mateoLopez.name, rating = 4,
            review = "Corto e intenso. La fiesta como fachada del vacío, muy bien retratado.",
            timestamp = LocalDate.of(2025, 10, 5)),
        pickUpDate = LocalDate.of(2025, 9, 15), dropOffDate = LocalDate.of(2025, 10, 4),
    )

    val reservaCrimen2 = Reservation(
        user = emiliaRomero, book = crimen,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "La culpa narrada desde adentro. Dostoyevski te mete en la cabeza de Raskolnikov sin escapatoria.",
            timestamp = LocalDate.of(2025, 6, 20)),
        pickUpDate = LocalDate.of(2025, 5, 30), dropOffDate = LocalDate.of(2025, 6, 19),
    )

    val reservaCrimen3 = Reservation(
        user = mateoLopez, book = crimen,
        review = Review(reviewerName = mateoLopez.name, rating = 4,
            review = "Denso pero absorbente. El juicio final es magistral.",
            timestamp = LocalDate.of(2025, 12, 10)),
        pickUpDate = LocalDate.of(2025, 11, 20), dropOffDate = LocalDate.of(2025, 12, 9),
    )

    val reservaHarryPotter2 = Reservation(
        user = emiliaRomero, book = harryPotter,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "Un clásico moderno. La magia de Hogwarts no envejece nunca.",
            timestamp = LocalDate.of(2025, 5, 10)),
        pickUpDate = LocalDate.of(2025, 4, 20), dropOffDate = LocalDate.of(2025, 5, 9),
    )

    val reservaHarryPotter3 = Reservation(
        user = valentinaSosa, book = harryPotter,
        review = Review(reviewerName = valentinaSosa.name, rating = 4,
            review = "Lo leí por primera vez de adulta y entendí por qué marcó a toda una generación.",
            timestamp = LocalDate.of(2025, 8, 5)),
        pickUpDate = LocalDate.of(2025, 7, 16), dropOffDate = LocalDate.of(2025, 8, 4),

    )

    val reservaHuckFinn2 = Reservation(
        user = lucianoVega, book = huckFinn,
        review = Review(reviewerName = lucianoVega.name, rating = 4,
            review = "Twain disfraza la crítica social de aventura infantil con una habilidad increíble.",
            timestamp = LocalDate.of(2025, 9, 22)),
        pickUpDate = LocalDate.of(2025, 9, 2), dropOffDate = LocalDate.of(2025, 9, 21),

    )

    val reservaHuckFinn3 = Reservation(
        user = mateoLopez, book = huckFinn,
        review = Review(reviewerName = mateoLopez.name, rating = 3,
            review = "Entretenido, aunque algunos pasajes se sienten datados. El vínculo Huck-Jim es lo mejor.",
            timestamp = LocalDate.of(2026, 1, 28)),
        pickUpDate = LocalDate.of(2026, 1, 8), dropOffDate = LocalDate.of(2026, 1, 27),

    )

    val reservaN19842 = Reservation(
        user = lucianoVega, book = n1984,
        review = Review(reviewerName = lucianoVega.name, rating = 5,
            review = "Perturbador y necesario. Lo releí y cada vez me parece más actual.",
            timestamp = LocalDate.of(2025, 6, 30)),
        pickUpDate = LocalDate.of(2025, 6, 10), dropOffDate = LocalDate.of(2025, 6, 29),

    )

    val reservaN19843 = Reservation(
        user = mateoLopez, book = n1984,
        review = Review(reviewerName = mateoLopez.name, rating = 5,
            review = "El Gran Hermano ya existe. Orwell lo supo antes que todos.",
            timestamp = LocalDate.of(2025, 12, 22)),
        pickUpDate = LocalDate.of(2025, 12, 2), dropOffDate = LocalDate.of(2025, 12, 21),

    )

    val reservaLosMiserables2 = Reservation(
        user = emiliaRomero, book = losMiserables,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "Jean Valjean es uno de los personajes más conmovedores de la literatura universal.",
            timestamp = LocalDate.of(2025, 8, 18)),
        pickUpDate = LocalDate.of(2025, 7, 28), dropOffDate = LocalDate.of(2025, 8, 17),

    )

    val reservaLosMiserables3 = Reservation(
        user = valentinaSosa, book = losMiserables,
        review = Review(reviewerName = valentinaSosa.name, rating = 4,
            review = "Largo pero cada página tiene peso. Hugo no desperdicia ni un capítulo.",
            timestamp = LocalDate.of(2026, 2, 10)),
        pickUpDate = LocalDate.of(2026, 1, 20), dropOffDate = LocalDate.of(2026, 2, 9),

    )

    val reservaMontagnaMagica2 = Reservation(
        user = lucianoVega, book = montagnaMagica,
        review = Review(reviewerName = lucianoVega.name, rating = 4,
            review = "Mann logra que el tiempo del sanatorio se sienta tan eterno como para el protagonista.",
            timestamp = LocalDate.of(2025, 11, 15)),
        pickUpDate = LocalDate.of(2025, 10, 25), dropOffDate = LocalDate.of(2025, 11, 14),
            )

    val reservaMontagnaMagica3 = Reservation(
        user = valentinaSosa, book = montagnaMagica,
        review = Review(reviewerName = valentinaSosa.name, rating = 3,
            review = "Muy filosófica, quizás demasiado. Los diálogos entre Naphta y Settembrini son brillantes.",
            timestamp = LocalDate.of(2026, 1, 10)),
        pickUpDate = LocalDate.of(2025, 12, 20), dropOffDate = LocalDate.of(2026, 1, 9),

    )

    val reservaMonteCristo2 = Reservation(
        user = emiliaRomero, book = monteCristo,
        review = Review(reviewerName = emiliaRomero.name, rating = 5,
            review = "Imposible soltar. La venganza de Dantès es satisfactoria en cada nivel.",
            timestamp = LocalDate.of(2025, 7, 25)),
        pickUpDate = LocalDate.of(2025, 7, 5), dropOffDate = LocalDate.of(2025, 7, 24),

    )

    val reservaMonteCristo3 = Reservation(
        user = lucianoVega, book = monteCristo,
        review = Review(reviewerName = lucianoVega.name, rating = 5,
            review = "Dumas teje una trama perfecta. Cada detalle de los primeros capítulos vuelve al final.",
            timestamp = LocalDate.of(2025, 10, 18)),
        pickUpDate = LocalDate.of(2025, 9, 28), dropOffDate = LocalDate.of(2025, 10, 17),

    )

    val reservaSinCalificar = Reservation(
        user = emiliaRomero,
        book = elProceso,
        review = Review(), // sin rating
        pickUpDate = LocalDate.of(2026, 1, 1),
        dropOffDate = LocalDate.of(2026, 2, 1),
    )

// ─── Reservas activas/futuras ─────────────────────────────────────────────

    val reservaEmilia1 = Reservation(
        user = emiliaRomero, book = jardinCerezos,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 15), dropOffDate = LocalDate.of(2026, 3, 29),

    )

    val reservaEmilia2 = Reservation(
        user = emiliaRomero, book = ficciones,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 1), dropOffDate = LocalDate.of(2026, 4, 14),

    )

    val reservaLuciano1 = Reservation(
        user = lucianoVega, book = fundacion,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 20), dropOffDate = LocalDate.of(2026, 4, 3),

    )

    val reservaLuciano2 = Reservation(
        user = lucianoVega, book = vueltaMundo,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 5), dropOffDate = LocalDate.of(2026, 4, 19),

    )

    val reservaValentina1 = Reservation(
        user = valentinaSosa, book = cienAnios,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 10), dropOffDate = LocalDate.of(2026, 3, 24),

    )

    val reservaValentina2 = Reservation(
        user = valentinaSosa, book = guerraPaz,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 2), dropOffDate = LocalDate.of(2026, 4, 16),

    )

    val reservaMateo1 = Reservation(
        user = mateoLopez, book = cuentosMisterio,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 3, 18), dropOffDate = LocalDate.of(2026, 4, 1),

    )

    val reservaMateo2 = Reservation(
        user = mateoLopez, book = senoraDalloway,
        review = Review(),
        pickUpDate = LocalDate.of(2026, 4, 7), dropOffDate = LocalDate.of(2026, 4, 21),

    )

    // ─── Función de inicialización ────────────────────────────────────────────

    fun init(
        userRepository: Repository<User>,
        bookRepository: Repository<Book>,
        reservationRepository: Repository<Reservation>,
    ) {
        listOf(emiliaRomero, lucianoVega, valentinaSosa, mateoLopez)
            .forEach { userRepository.create(it) }

        listOf(n1984, elProceso, crimen, orgullo, guerraPaz, losMiserables, alquimista, extranjero)
            .forEach { bookRepository.create(it) }

        listOf(granGatsby, adiosArmas, monteCristo, vueltaMundo, senoraDalloway, cuentosMisterio, fundacion, cienAnios)
            .forEach { bookRepository.create(it) }

        listOf(huckFinn, ficciones, rayuela, ensayoCeguera, montagnaMagica, caminoSwann, jardinCerezos, harryPotter)
            .forEach { bookRepository.create(it) }

        listOf(
            reservaEmiliaPasada1, reservaEmiliaPasada2, reservaEmiliaPasada3,
            //reservaLucianoPasada1, reservaLucianoPasada2, reservaLucianoPasada3,
            reservaLucianoPasada1, reservaLucianoPasada2,
            reservaValentinaPasada1, reservaValentinaPasada2, reservaValentinaPasada3,
            reservaMateoPasada1, reservaMateoPasada2, reservaMateoPasada3,
            reservaElProceso2, reservaElProceso3,
            reservaAdiosArmas2, reservaAdiosArmas3,
            reservaRayuela2, reservaRayuela3,
            reservaGranGatsby2, reservaGranGatsby3,
            reservaCrimen2, reservaCrimen3,
            reservaHarryPotter2, reservaHarryPotter3,
            reservaHuckFinn2, reservaHuckFinn3,
            reservaN19842, reservaN19843,
            reservaLosMiserables2, reservaLosMiserables3,
            reservaMontagnaMagica2, reservaMontagnaMagica3,
            reservaMonteCristo2, reservaMonteCristo3, reservaSinCalificar,
            reservaElProceso4, reservaElProceso5, reservaElProceso6,
        ).forEach { reservationRepository.create(it) }

        listOf(
            reservaEmilia1, reservaEmilia2,
            reservaLuciano1, reservaLuciano2,
            reservaValentina1, reservaValentina2,
            reservaMateo1, reservaMateo2,
        ).forEach { reservationRepository.create(it) }
    }

}
