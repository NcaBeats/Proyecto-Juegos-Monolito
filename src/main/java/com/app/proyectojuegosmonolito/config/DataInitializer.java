package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.service.CategoryService;
import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.account.wallet.service.WalletService;
import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@org.springframework.context.annotation.Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GameService gameService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final WalletService walletService;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (gameService.count() > 0) {
            return;
        }

        var action = createCategory("Action");
        var adventure = createCategory("Adventure");
        var rpg = createCategory("RPG");
        var shooter = createCategory("Shooter");
        var platformer = createCategory("Platformer");
        var fighting = createCategory("Fighting");
        var openWorld = createCategory("Open World");
        var sports = createCategory("Sports");
        var indie = createCategory("Indie");
        var stealth = createCategory("Stealth");
        var horror = createCategory("Horror");
        var simulation = createCategory("Simulation");
        var racing = createCategory("Racing");
        var strategy = createCategory("Strategy");

        gameService.create(Game.builder()
                .name("Minecraft")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(0)
                .description("""
                        Minecraft es un videojuego sandbox que te permite construir cualquier cosa que imagines: desde humildes cabañas hasta castillos épicos, granjas automatizadas y ciudades enteras. Con su característico mundo de bloques, podrás recolectar recursos, fabricar herramientas y sobrevivir a la noche mientras los creepers y esqueletos acechan en la oscuridad.

                        El juego ofrece dos modos principales: Supervivencia, donde debes gestionar tu salud, hambre y recursos para prosperar, y Creativo, donde tienes acceso ilimitado a todos los bloques para dar rienda suelta a tu imaginación. Además, el modo multijugador te permite explorar mundos infinitos con amigos, participar en servidores comunitarios y crear experiencias personalizadas con modificaciones.

                        Desde su lanzamiento, Minecraft se ha convertido en uno de los títulos más vendidos de la historia con más de 300 millones de copias, consolidándose como un fenómeno cultural que inspira creatividad en jugadores de todas las edades.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2011, 11, 18))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149050/sjclgg4kv5nvvooh4acf.webp")
                .minimumSpecs("""
                        {"os": "Windows 10", "processor": "Intel Core i3-3210 o AMD A8-7600", "memory": "4 GB RAM", "graphics": "Intel HD Graphics 4000 o AMD Radeon R5", "storage": "1 GB disponible", "additional": "Internet requerido para multijugador"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10", "processor": "Intel Core i5-4690 o AMD A10-7800", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce 700 Series o AMD Radeon Rx 200 Series", "storage": "4 GB disponible"}""")
                .categories(List.of(indie, adventure))
                .build());

        gameService.create(Game.builder()
                .name("Stardew Valley")
                .originalPrice(new BigDecimal("14.99"))
                .discountPercent(0)
                .description("""
                        Stardew Valley es un aclamado simulador de granja con toques de RPG que te invita a escapar del estrés de la ciudad y heredar la antigua granja de tu abuelo en el tranquilo pueblo de Pelican Town. Podrás cultivar cosechas según las estaciones, criar animales, pescar en ríos y lagos, y explorar minas llenas de minerales y criaturas misteriosas.

                        El juego se destaca por su profunda vida comunitaria: podrás interactuar con más de 30 personajes únicos, formar amistades, descubrir secretos y hasta casarte y formar una familia. Cada día presenta nuevas oportunidades, con festivales estacionales, misiones secundarias y un calendario dinámico que hacen que el pueblo se sienta vivo.

                        Además de la agricultura, Stardew Valley ofrece la posibilidad de personalizar tu granja, fabricar objetos, cocinar recetas y participar en una economía local. Es una experiencia relajante pero adictiva que ha cautivado a millones de jugadores por su encanto artesanal y su atención al detalle.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 2, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149138/wrpzkzlrasq3boe9x2vp.webp")
                .minimumSpecs("""
                        {"os": "Windows 7/8/10", "processor": "2 GHz", "memory": "2 GB RAM", "graphics": "256 MB de VRAM compatible", "storage": "500 MB disponible", "additional": "Compatibilidad con gamepad opcional"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10", "processor": "Dual Core a 2.5 GHz", "memory": "4 GB RAM", "graphics": "1 GB de VRAM compatible", "storage": "1 GB disponible"}""")
                .categories(List.of(indie, rpg))
                .build());

        gameService.create(Game.builder()
                .name("Elden Ring")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(0)
                .description("""
                        Elden Ring, desarrollado por FromSoftware y creado en colaboración con George R. R. Martin, es un RPG de mundo abierto ambientado en las Tierras Intermedias. El Círculo de Elden ha sido destruido y su fragmento, el Anillo Mayor, se ha dispersado, corrompiendo a los semidioses que reclaman sus restos. Tú, un Maculado, debes recorrer estas vastas tierras para restaurar el orden.

                        El juego combina la desafiante dificultad característica de FromSoftware con una libertad total de exploración: cabalga sobre Torrentera, un corcel espiritual, a través de campos abiertos, castillos colosales y mazmorras ocultas. Podrás personalizar tu construcción con cientos de armas, hechizos y pactos, y enfrentarte a más de 100 jefes únicos.

                        Su diseño de mundo interconectado, su narrativa fragmentada y sus combates memorables lo han convertido en el juego mejor valorado de su generación, ganando el premio al Juego del Año 2022 y vendiendo más de 25 millones de copias en su primer año.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2022, 2, 25))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149161/q5zkmswvhs13xkkrsbcz.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-8400 o AMD Ryzen 3 3300X", "memory": "12 GB RAM", "graphics": "NVIDIA GTX 1060 3GB o AMD RX 580 4GB", "storage": "60 GB disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i7-8700K o AMD Ryzen 5 3600X", "memory": "16 GB RAM", "graphics": "NVIDIA GTX 1070 8GB o AMD RX VEGA 56 8GB", "storage": "60 GB disponible", "directX": "Version 12"}""")
                .categories(List.of(action, rpg, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Rocket League")
                .originalPrice(new BigDecimal("0.00"))
                .discountPercent(0)
                .description("""
                        Rocket League es una adictiva fusión entre el fútbol y los coches de carreras. Controla vehículos propulsados a reacción en un estadio futurista para golpear una pelota gigante y anotar goles en la portería rival. Su mecánica física única te permite volar, hacer acrobacias y ejecutar jugadas espectaculares que nunca habías visto en un deporte virtual.

                        El juego ofrece modos de 1v1 hasta 3v3, además de modos especiales como baloncesto, hockey y batallas de rumble con objetos locos. Su sistema de progresión incluye cientos de objetos cosméticos, chapas personalizables, y una liga competitiva escalonada con temporadas cada pocos meses.

                        Con una curva de aprendizaje que premia la práctica, Rocket League se ha consolidado como un título esports de élite con torneos mundiales, millones de jugadores activos y una comunidad que no deja de crear contenido. Al ser free-to-play, cualquiera puede unirse a la acción de inmediato.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2015, 7, 7))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149186/jlvjpqerig4jsh4f7ypr.webp")
                .bannerUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788226686/zw94nyucmyjac8jszexj.webp")
                .minimumSpecs("""
                        {"os": "Windows 10", "processor": "Intel Core i5-750 o AMD Phenom II x4 945", "memory": "4 GB RAM", "graphics": "NVIDIA GeForce GTX 460 o AMD Radeon HD 6850", "storage": "20 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10", "processor": "Intel Core i5-4460 o AMD FX-6300", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 760 o AMD Radeon R9 270X", "storage": "20 GB disponible"}""")
                .categories(List.of(sports))
                .build());

        gameService.create(Game.builder()
                .name("Spider-Man Remastered")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(40)
                .description("""
                        Spider-Man Remastered te pone en la piel de Peter Parker, un experto luchador contra el crimen que debe equilibrar su vida personal con la responsabilidad de proteger la ciudad de Nueva York. Recorre Manhattan con un sistema de balanceo realista y fluido, deslízate por los cañones urbanos y combate a enemigos en espectaculares acrobacias.

                        La versión remasterizada de este aclamado título de Insomniac Games añade gráficos mejorados con trazado de rayos, tiempos de carga reducidos al mínimo y mejoras en las animaciones faciales y del traje. Incluye además el caso de los trajes de Spider-Man con más de 30 trajes desbloqueables del universo Marvel y de los cómics.

                        Con una narrativa profunda protagonizada por villanos icónicos como el Doctor Octopus, el Duende Verde y Kingpin, la campaña ofrece horas de acción trepidante. El juego también te permite explorar un Nueva York lleno de vida, detener crímenes aleatorios y completar misiones secundarias que amplían la experiencia mucho más allá de la historia principal.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2022, 8, 12))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149206/xrjf04vgulidbc4lbyf3.avif")
                .bannerUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788226722/y6bb4iesp3jqbvymtaoe.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i3-4160 o AMD ryzen 3 1300X", "memory": "8 GB RAM", "graphics": "NVIDIA GTX 950 o AMD Radeon RX 470", "storage": "75 GB disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-4670 o AMD Ryzen 5 1600", "memory": "16 GB RAM", "graphics": "NVIDIA GTX 1060 6GB o AMD Radeon RX 580 8GB", "storage": "75 GB SSD disponible", "directX": "Version 12"}""")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Grand Theft Auto V")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(50)
                .description("""
                        Grand Theft Auto V te sumerge en el vasto estado de San Andreas, una recreación satírica del sur de California que incluye Los Santos (basada en Los Ángeles), Blaine County y las montañas y desiertos circundantes. La historia sigue a tres protagonistas interconectados: Michael, un ladrón de bancos retirado; Franklin, un joven que busca salir del gueto; y Trevor, un psicópata impredecible. Puedes alternar entre ellos en cualquier momento.

                        El modo para un jugador ofrece decenas de misiones principales que incluyen robos espectaculares, persecuciones a alta velocidad y decisiones morales que afectan el desenlace. Fuera de la historia, el mundo abierto es un enorme parque de juegos donde puedes bucear, jugar al golf, hacer paracaidismo, comprar propiedades y modificarte el coche en talleres.

                        El corazón del juego, sin embargo, es GTA Online, un mundo persistente para hasta 30 jugadores donde puedes formar bandas, asaltar bancos y comprar negocios para construir tu imperio criminal. Con años de contenido añadido constante, sigue siendo en 2026 uno de los juegos más jugados de la historia.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2013, 9, 17))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149231/pfmjtit08yk4aoikwfqv.webp")
                .bannerUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788226489/a1iuiy2siwpbr74n3prx.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core 2 Quad Q6600 a 2.4 GHz o AMD Phenom 9850", "memory": "4 GB RAM", "graphics": "NVIDIA 9800 GT 1GB o AMD HD 4870 1GB", "storage": "110 GB disponible", "sound": "100% compatible DirectX 10"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5 3470 a 3.2 GHz o AMD X8 FX-8350", "memory": "8 GB RAM", "graphics": "NVIDIA GTX 660 2GB o AMD HD 7870 2GB", "storage": "110 GB disponible", "sound": "100% compatible DirectX 10"}""")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Battlefield 1")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(75)
                .description("""
                        Battlefield 1 te transporta al frente de la Primera Guerra Mundial, un conflicto devastador que estremeció al mundo entre 1914 y 1918. Con su ambientación histórica meticulosamente recreada, el juego te permite combatir en campos de batalla que van desde las trincheras de Francia hasta los desiertos de Arabia, pasando por los Alpes italianos y los pantanos de los Balcanes.

                        La campaña para un jugador presenta varias historias entrelazadas conocidas como "Guerra de Novatos", cada una contada desde la perspectiva de un soldado diferente: un piloto británico, una combatiente beduina, un conductor de tanque y un soldado estadounidense en los zelanda. Estas historias humanizan el conflicto y muestran la guerra desde distintos frentes.

                        El multijugador es el corazón del juego, con combates a gran escala que enfrentan a hasta 64 jugadores en modos como Conquista y Operaciones. Podrás pilotar biplanos, manejar tanques, montar caballos con sable y usar armas de época como el lanzallamas y gases tóxicos. Es una experiencia inmersiva y épica que redefine los shooters de guerra.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 10, 21))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149249/yuuilkytoh575uzxosfd.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5 6600K o AMD FX-6350", "memory": "8 GB RAM", "graphics": "NVIDIA GTX 660 2GB o AMD Radeon HD 7850 2GB", "storage": "50 GB disponible", "directX": "Version 11"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7 4790 o AMD FX-8350", "memory": "16 GB RAM", "graphics": "NVIDIA GTX 1060 3GB o AMD RX 480 4GB", "storage": "50 GB disponible", "directX": "Version 11"}""")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Assassin's Creed Shadows")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(20)
                .description("""
                        Assassin's Creed Shadows es la primera entrega de la saga ambientada en el Japón feudal, durante el período Sengoku del siglo XVI. La historia combina dos protagonistas jugables con estilos opuestos: Naoe, una shinobi ágil y sigilosa que se desliza por los tejados y elimina a sus enemigos desde las sombras; y Yasuke, un samurái de origen africano que desata el poder brutal de la espada katana en combates directos.

                        Podrás alternar entre ambos personajes en cualquier momento, lo que ofrece dos maneras radicalmente distintas de abordar cada misión. El mundo abierto es un Japón bellamente recreado con castillos, templos, bosques de bambú y aldeas rurales que cambian con las estaciones del año, afectando al combate y al sigilo.

                        El juego profundiza en la mecánica de ocultación: apaga linternas para sumergirte en la oscuridad, usa shurikens y kunais, y escala cualquier superficie. Incluye además la búsqueda de los legendarios artefactos de los Asesinos y la construcción de tu propio búnker, expandiendo la tradición de la hermandad en una de las eras más fascinantes de la historia japonesa.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2025, 3, 20))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149264/ixpm2q4qxzrddzuxbbla.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-8700K o AMD Ryzen 5 3600", "memory": "16 GB RAM", "graphics": "NVIDIA GTX 1070 8GB o AMD RX 5700 XT 8GB", "storage": "100 GB SSD disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 11 de 64 bits", "processor": "Intel Core i5-11600K o AMD Ryzen 5 5600X", "memory": "16 GB RAM", "graphics": "NVIDIA RTX 2070 8GB o AMD RX 6700 XT 12GB", "storage": "100 GB SSD NVMe disponible", "directX": "Version 12"}""")
                .categories(List.of(action, adventure, rpg, stealth))
                .build());

        gameService.create(Game.builder()
                .name("Cyberpunk 2077")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(60)
                .description("""
                        Cyberpunk 2077 es un RPG de mundo abierto desarrollado por CD Projekt Red, ambientado en Night City, una megalópolis estadounidense obsesionada con el poder, el glamour y la modificación corporal. La historia sigue a V, un mercenario con un chip prototipo llamado Relic que contiene el engrama de Johnny Silverhand, interpretado por Keanu Reeves, un ícono punk que se apodera de su mente.

                        El juego te ofrece total libertad para crear a tu personaje, elegir su historia de fondo y desarrollar habilidades de combate, piratería o cuerpo a cuerpo. Night City es un mundo gigantesco y vertical lleno de distritos diferenciados, barrios de gángsteres, corporaciones despiadadas y zonas de alta tecnología como Japantown y Pacifica.

                        Con la expansión Phantom Liberty añadida, la experiencia se enriquece con una nueva zona, personajes memorables y una trama de espionaje político. El juego combina una narrativa adulta, decisiones con consecuencias y un gameplay frenético que lo han consolidado como uno de los RPGs más ambiciosos de su generación.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2020, 12, 10))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149286/pzyxwb5xndhfrs9quhlr.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-6700 o AMD Ryzen 5 1600", "memory": "12 GB RAM", "graphics": "NVIDIA GTX 1060 6GB o AMD RX 580 8GB", "storage": "70 GB SSD disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-12700 o AMD Ryzen 7 7800X3D", "memory": "16 GB RAM", "graphics": "NVIDIA RTX 2060 SUPER 8GB o AMD RX 6800 XT 16GB", "storage": "70 GB SSD NVMe disponible", "directX": "Version 12", "rayTracing": "Soporte para Ray Tracing mínimo"}""")
                .categories(List.of(rpg, action, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Dragon Ball FighterZ")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(70)
                .description("""
                        Dragon Ball FighterZ es el juego de lucha definitivo de la franquicia, desarrollado por Arc System Works, los maestros del género anime que crearon Guilty Gear. Con un estilo visual que reproduce fielmente la animación del anime, cada golpe, cada transformación y cada Kamehameha se siente como si estuvieras viendo un episodio de la serie.

                        El sistema de combate es de 3 contra 3, inspirado en los juegos de pelea en equipo como Marvel vs. Capcom. Podrás alternar entre tus tres luchadores, ejecutar asistencias, combos espectaculares y desatar ataques finales con los icónicos "Sparking!" y las transformaciones Super Saiyan. Incluye una plantilla de más de 40 personajes del universo Dragon Ball, desde Goku y Vegeta hasta Cell, Buu y los villanos de Super.

                        El juego ofrece un modo historia original para un jugador con combates contra androides y una trama escrita especialmente para el título, además de un completo multijugador online con ranking, torneos y replays. Es accesible para novatos pero con una profundidad competitiva que lo ha convertido en un pilar de la escena esports de juegos de lucha.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 1, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149302/qir5n5rdwxwanuzd4x4u.webp")
                .minimumSpecs("""
                        {"os": "Windows 7/8/10 de 64 bits", "processor": "Intel Core i5-3470 o AMD FX-4350", "memory": "4 GB RAM", "graphics": "NVIDIA GeForce GTX 650 o AMD Radeon HD 7790", "storage": "6 GB disponible", "directX": "Version 11"}""")
                .recommendedSpecs("""
                        {"os": "Windows 7/8/10 de 64 bits", "processor": "Intel Core i5-4460 o AMD FX-6300", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 660 o AMD Radeon HD 7950", "storage": "6 GB disponible", "directX": "Version 11"}""")
                .categories(List.of(fighting, action))
                .build());

        gameService.create(Game.builder()
                .name("Mortal Kombat 11")
                .originalPrice(new BigDecimal("49.99"))
                .discountPercent(80)
                .description("""
                        Mortal Kombat 11 es la entrega más ambiciosa de la legendaria saga de lucha creada por NetherRealm Studios. Con una presentación cinematográfica de primera y un sistema de combate refinado, el juego empuja la violencia y la espectacularidad a nuevos niveles con fatalities, brutalities y friendships desbloqueables que no dejan a nadie indiferente.

                        La historia continua la guerra entre Kronika, la diosa del tiempo, y los campeones de Earthrealm. El argumento juega con los viajes en el tiempo para traer de vuelta a clásicos como Rain, Sindel y el Joker, permitiendo que personajes que murieron en entregas anteriores vuelvan al combate. El modo historia spidodal ofrece una película interactiva de acción con más de 30 capítulos.

                        Además del modo arcade clásico, destaca el Kustomización, que te permite crear combos, equipar accesorios y cambiar el aspecto de cada luchador. El sistema de torres ofrece desafíos infinitos, y el multijugador online incluye temporadas competitivas, kombat league y el icónico sistema de fatality social. Es el juego de lucha más completo y pulido de la franquicia hasta la fecha.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2019, 4, 23))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149323/m9njptc1b0tthgombbgy.webp")
                .minimumSpecs("""
                        {"os": "Windows 7/10 de 64 bits", "processor": "Intel Core i5-750 o AMD Phenom II x4 965", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 670 o AMD Radeon HD 7950", "storage": "60 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-4670 o AMD FX-8350", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 970 o AMD Radeon RX 480", "storage": "60 GB disponible"}""")
                .categories(List.of(fighting, action))
                .build());

        gameService.create(Game.builder()
                .name("Red Dead Redemption 2")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(67)
                .description("""
                        Red Dead Redemption 2 es un western épico ambientado en 1899, en los últimos días del Salvaje Oeste. La historia sigue a Arthur Morgan, un pistolero y miembro de la banda Van der Linde, liderada por el carismático Dutch van der Linde. Tras un robo fallido en la ciudad de Blackwater, la banda huye perseguida por agentes federales mientras lidia con la lealtad, el honor y la supervivencia.

                        El juego presenta un mundo abierto inmenso y minuciosamente detallado: desde las montañas nevadas de Grizzlies West hasta los pantanos de Lemoyne, cada región tiene su propia fauna, clima y comunidades con misiones únicas. Podrás cazar, pescar, jugar al póker, montar a caballo y participar en tiroteos, robos y duelos al atardecer.

                        Con una narrativa adulta, más de 60 horas de campaña principal y un modo online persistente, RDR2 es considerado uno de los mayores logros artísticos de la industria. La atención al detalle en las animaciones faciales, el comportamiento de los caballos y la ambientación sonora te sumerge en un mundo que respira historia en cada rincón.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 10, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149472/y2wcocuowgind8c8jgza.avif")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-2500K o AMD FX-6300", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 770 2GB o AMD Radeon R9 280 3GB", "storage": "150 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-4770K o AMD Ryzen 5 1500X", "memory": "12 GB RAM", "graphics": "NVIDIA GeForce GTX 1060 6GB o AMD Radeon RX 480 4GB", "storage": "150 GB SSD disponible"}""")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Hollow Knight: Silksong")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(0)
                .description("""
                        Hollow Knight: Silksong es la esperada secuela del metroidvania indie Hollow Knight, desarrollado por Team Cherry. La historia sigue a Hornet, la princesa guerrera de Hallownest, quien se despierta en un nuevo reino llamado Pharloom tras ser capturada y transportada junto con otros insectos hilanderos. Su misión: ascender a la cima de la ciudadela de seda y descubrir los secretos que oculta.

                        El juego presenta un mundo interconectado mucho más grande que su predecesor, con múltiples biomas que van desde musgosos bosques hasta ciudadeselas doradas yermas. Hornet se mueve con agilidad nunca vista: puede correr por las paredes, deslizarse con su aguja de seda y ejecutar ataques acrobáticos que cambian radicalmente el enfoque de combate del primer juego.

                        Silksong incluye más de 200 enemigos nuevos, una treintena de jefes y un sistema de misiones secundarias de gran escala. Los fans de los metroidvania encontrarán en él una experiencia desafiante pero profundamente gratificante, con la atmósfera melancólica y la banda sonora orquestal que convirtieron al original en un clásico instantáneo del género indie.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2025, 7, 17))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149493/kdq0puz7vosdiun1vvkt.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core 2 Duo E5200", "memory": "4 GB RAM", "graphics": "GeForce 9800GTX+ (1GB) o equivalente", "storage": "5 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i5-2300 o AMD FX-6350", "memory": "8 GB RAM", "graphics": "GeForce GTX 660 o AMD Radeon HD 7850", "storage": "5 GB disponible"}""")
                .categories(List.of(indie, platformer, adventure))
                .build());

        gameService.create(Game.builder()
                .name("The Witcher 3")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(85)
                .description("""
                        The Witcher 3: Wild Hunt es un RPG de mundo abierto considerado por la crítica como uno de los mejores videojuegos de la historia. La historia sigue a Geralt de Rivia, un brujo mutante que busca a su hija adoptiva Ciri, perseguida por la Cacería Salvaje, una fuerza espectral del mundo élfico. A lo largo del camino, Geralt se ve envuelto en conflictos políticos, guerras y misterios que afectan al destino de los reinos del norte.

                        El mundo es gigantesco y denso en contenido: las regiones de Velen, Novigrado, Skellige y Toussaint ofrecen cientos de horas de exploración, con pueblos por descubrir, monstruos que cazar y decisiones morales que dan forma a la historia. Cada contrato de brujo es una pequeña investigación con múltiples soluciones.

                        The Witcher 3 incluye dos expansiones épicas: Hearts of Stone, una historia autoconclusiva sobre un contrato con un hombre misterioso, y Blood and Wine, que te transporta a Toussaint, un ducado vinícola lleno de vampiros y secretos. Con sus más de 800 horas potenciales de juego, sigue siendo una obra maestra de la narrativa interactiva.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2015, 5, 19))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149509/co60ksh6jnzks1mzhkik.webp")
                .minimumSpecs("""
                        {"os": "Windows 7/8/10 de 64 bits", "processor": "Intel CPU Core i5-2500K 3.3 GHz o AMD CPU Phenom II X4 940", "memory": "6 GB RAM", "graphics": "NVIDIA GPU GeForce GTX 660 o AMD GPU Radeon HD 7870", "storage": "50 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel CPU Core i7 3770 3.4 GHz o AMD CPU AMD FX-8350 4.0 GHz", "memory": "8 GB RAM", "graphics": "NVIDIA GPU GeForce GTX 1060 6GB o AMD GPU Radeon RX 480 4GB", "storage": "50 GB SSD disponible"}""")
                .categories(List.of(rpg, action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("God of War Ragnarök")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(0)
                .description("""
                        God of War Ragnarök continúa la historia de Kratos y su hijo Atreus, ambientada en la mitología nórdica varios años después de los eventos del juego anterior. La profecía del Ragnarök, el fin del mundo nórdico, se acerca, y los dioses del panteón nórdico, incluido Odín, Thor y Freya, se cruzan en el camino del Fantasma de Esparta. Atreus, ahora adolescente, busca descubrir su verdadera identidad como Loki.

                        El juego mantiene el combate visceral y estratégico de la saga, con Kratos utilizando el Hacha Leviatán, las Espadas del Caos y nuevas armas como la lanza Draupnir. La exploración se expande a través de los nueve reinos, cada uno con sus propios biomas y secretos por descubrir. La narrativa entre Kratos y Atreus profundiza en temas de paternidad, identidad y redención.

                        Ragnarök es una carta de amor a la mitología nórdica y al legado de Kratos como dios de la guerra. Con más de 30 horas de campaña, combate desafiante contra dioses y monstruos, y un final que cierra el arco del Fantasma de Esparta en estas tierras, es uno de los títulos más emotivos de la generación.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2024, 9, 19))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788151900/tcixcrqzkgrrrihoiref.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-8400 o AMD Ryzen 5 3600X", "memory": "16 GB RAM", "graphics": "NVIDIA GTX 1070 8GB o AMD RX 580 8GB", "storage": "90 GB SSD disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i5-11600K o AMD Ryzen 7 5800X", "memory": "16 GB RAM", "graphics": "NVIDIA RTX 2070 SUPER 8GB o AMD RX 6800 16GB", "storage": "90 GB SSD NVMe disponible", "directX": "Version 12"}""")
                .categories(List.of(action, adventure, rpg))
                .build());

        gameService.create(Game.builder()
                .name("Resident Evil 4")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(30)
                .description("""
                        Resident Evil 4 es un remake del clásico de 2005 que redefinió los survival horror. La historia sigue a Leon S. Kennedy, ex policía convertido en agente del gobierno, quien es enviado a una remota aldea europea para rescatar a Ashley Graham, la hija del presidente de los Estados Unidos, secuestrada por un culto misterioso.

                        El remake reconstruyó el juego desde cero con gráficos de nueva generación, control moderno en tercera persona y una atmósfera mucho más terrorífica. Los aldeanos ya no son zombis lentos: ahora son enemigos organizados y agresivos que te persiguen en grupo, incluyendo horrores como las Hermanas de la Plaga y el icónico Dr. Salvador con su motosierra.

                        Leon cuenta con un arsenal actualizado que incluye pistolas, escopetas, rifles y ballestas con munición especial. La compra y mejora de armas se hace con el inteligente mercader, un personaje secundario que se ha convertido en uno de los favoritos de los fans. La campaña ofrece aproximadamente 16 horas de acción intensa, con desbloqueo de modos extra como The Mercenaries.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2023, 3, 24))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152018/kqhok1e1hqpwmtkoxpnd.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "AMD Ryzen 5 3600X o Intel Core i7-8700", "memory": "16 GB RAM", "graphics": "AMD Radeon RX 5700 o NVIDIA GeForce GTX 1070", "storage": "60 GB disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "AMD Ryzen 5 7700X o Intel Core i9-11900K", "memory": "16 GB RAM", "graphics": "AMD Radeon RX 6900 XT o NVIDIA GeForce RTX 3080", "storage": "60 GB SSD disponible", "directX": "Version 12"}""")
                .categories(List.of(horror, action))
                .build());

        gameService.create(Game.builder()
                .name("Call of Duty: Modern Warfare III")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(25)
                .description("""
                        Call of Duty: Modern Warfare III es la secuela directa de Modern Warfare II, desarrollada por Sledgehammer Games. La campaña continúa la historia del Capitán Price, Soap MacTavish y la Fuerza Operativa 141 mientras Makarov, el villano más icónico de la saga, ejecuta su plan de desquiciar al mundo occidental desatando el terror en las calles de Verdansk.

                        La campaña incluye 14 misiones cinemáticas que llevan a los jugadores por escenarios globales: desde las calles de Moscú hasta la terminal aérea de Urzikstán, pasando por campos petrolíferos en el desierto. Makarov regresa más cruel que nunca, y la narrativa oscura muestra hasta dónde es capaz de llegar para imponer su visión del orden mundial.

                        El multijugador es el pilar central, con 16 mapas remasterizados de MW2 2009, incluyendo clásicos como Rust, Terminal y Shipment. El modo Zombis, desarrollado en colaboración con Treyarch, ofrece un mapa abierto masivo en el que tienes que sobrevivir contra las hordas en el amenazante Urzikstan. Es un paquete completo para los fans de la franquicia.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2023, 11, 10))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152199/q9wkj9p7pwkp7mfl2il6.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-6600 o AMD Ryzen 5 1400", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 960 o AMD Radeon RX 470", "storage": "125 GB SSD disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i7-10700K o AMD Ryzen 5 3600X", "memory": "16 GB RAM", "graphics": "NVIDIA GeForce RTX 3060 o AMD Radeon RX 6600 XT", "storage": "125 GB SSD NVMe disponible"}""")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Halo Infinite")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(50)
                .description("""
                        Halo Infinite marca el regreso de Master Chief en una aventura que redefine la saga. Tras los eventos de Halo 5, el Jefe Maestro es recogido por un buque de la UNSC y debe enfrentarse a una nueva amenaza: los Desterrados, una facción de ex Covenant que han tomado el control del Instalación 07, un antiguo anillo Halo. Pilotando un Pelican, Master Chief aterriza en el misterioso Zeta Halo para detenerlos.

                        La campaña presenta por primera vez en la saga un mundo abierto semiabierto en el anillo Halo, donde puedes explorar la superficie, descubrir campamentos enemigos, resolver acertijos Forerunner y completar misiones secundarias. El sistema de Grappleshot añade una nueva dimensión de movilidad y combate, permitiéndote balancearte por el terreno y tomar posiciones elevadas.

                        El multijugador es free-to-play y ofrece el regreso del arena shooter clásico de Halo con modos icónicos como Slayer, Capture the Flag y Oddball. La Forge te permite crear tus propios mapas con herramientas sin precedentes. Con temporadas regulares y eventos, Halo Infinite sigue siendo una plataforma en constante evolución para los fans del Jefe.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2021, 12, 8))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152365/y2ha15uhuawclxfqs17g.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "AMD Ryzen 5 1600 o Intel Core i7-4770", "memory": "8 GB RAM", "graphics": "AMD RX 570 o Nvidia GTX 1050 Ti", "storage": "50 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "AMD Ryzen 7 3700X o Intel Core i7-9700K", "memory": "16 GB RAM", "graphics": "AMD RX 5700 XT o Nvidia RTX 2070", "storage": "50 GB SSD disponible"}""")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Forza Horizon 4")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(60)
                .description("""
                        Forza Horizon 4 te transporta a un Reino Unido ficticio y bellamente recreado, con bosques de hoja caduca, montañas nevadas, zonas costeras y ciudades históricas como Edimburgo. Es el festival Horizon anual, una celebración de la cultura automotriz donde los mejores pilotos compiten en una variedad de eventos a lo largo de la isla.

                        La característica más innovadora del juego son las estaciones cambiantes: cada semana, el servidor de Xbox Live cambia dinámicamente entre primavera, verano, otoño e invierno, modificando las condiciones de manejo, la visibilidad y los eventos disponibles. Las carreteras se cubren de barro en primavera, los ríos se congelan en invierno, y los árboles cambian de color en otoño.

                        Con más de 750 coches coleccionables de fabricantes como Ferrari, Lamborghini, Porsche y McLaren, el abanico de personalización es prácticamente infinito. El mundo abierto te permite conducir libremente, completar carreras, hacer acrobacias, participar en derbis y hasta explorar la isla en búnkeres con tu propio avatar. La banda sonora curada con estaciones de radio licenciadas completa la experiencia inmersiva.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 10, 2))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152509/slibpbfpsr8maroiffdi.webp")
                .bannerUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788226771/ql7cef0v5jpuba26rexg.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i3-4170 o Intel Core i5 750", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 650 Ti o AMD Radeon R7 250X", "storage": "80 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-3820 3.6 GHz o Intel Core i5-8400 2.8 GHz", "memory": "12 GB RAM", "graphics": "NVIDIA GTX 1060 3GB o AMD RX 470", "storage": "80 GB disponible"}""")
                .categories(List.of(racing, sports))
                .build());

        gameService.create(Game.builder()
                .name("Civilization VI")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(75)
                .description("""
                        Civilization VI es la última entrega de la legendaria saga de estrategia por turnos de Sid Meier. Partiendo de una pequeña aldea en la Prehistoria, debes guiar a tu civilización a lo largo de miles de años de historia, investigando tecnologías, fundando ciudades, expandiendo tu imperio y compitiendo con otras civilizaciones por la supremacía global.

                        El juego introduce los "Distritos", una nueva mecánica que permite especializar las ciudades colocando barrios específicos en el mapa, como Campus para ciencia, Santuario para fe o Complejo industrial para producción. Esto crea mapas mucho más orgánicos donde la geografía importa de verdad: una ciudad junto a un río tendrá mejor comercio, una junto a montañas tendrá más producción.

                        Con 20 civilizaciones jugables y 18 líderes únicos, cada partida es una experiencia diferente. Puedes jugar como Roma bajo Trajano, Japón bajo Hojo Tokiyori, Francia bajo Catalina de Médicis o el Congo bajo Mvemba a Nzinga, cada uno con bonificaciones y unidades exclusivas. El juego admite partidas de 5 a 30 horas y ofrece multijugador online con hasta 12 jugadores.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 10, 21))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152670/ulge1bf2tbmcexewxnh2.avif")
                .minimumSpecs("""
                        {"os": "Windows 7/8/10/11 de 64 bits", "processor": "Intel Core i3 2.4 Ghz o AMD equivalente", "memory": "4 GB RAM", "graphics": "512 MB ATI 4850 o mejor, 1 GB NVidia 460 o mejor, Intel HD 4000 integrada o mejor", "storage": "17 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 7/8/10/11 de 64 bits", "processor": "Intel Core i5 2.7 Ghz o AMD equivalente", "memory": "8 GB RAM", "graphics": "2 GB ATI 7970 o mejor, 2 GB NVidia 770 o mejor", "storage": "17 GB SSD disponible"}""")
                .categories(List.of(strategy))
                .build());

        gameService.create(Game.builder()
                .name("Silent Hill 2")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(0)
                .description("""
                        Silent Hill 2 es el remake del clásico de survival horror psicológico de 2001, desarrollado por Bloober Team. La historia sigue a James Sunderland, un hombre que recibe una carta de su esposa Mary, fallecida hace tres años, pidiéndole que vaya a buscarla a su lugar especial: el pueblo de Silent Hill. Lo que encuentra allí es mucho más perturbador de lo que esperaba.

                        El remake reconstruye el pueblo de Silent Hill con tecnología moderna: niebla volumétrica, iluminación de nueva generación y un sistema de cámara "over-the-shoulder" más cinematográfico. Los icónicos monstruos como Pyramid Head, las enfermeras sin cara y el Abstract Daddy ahora se sienten más reales y amenazantes que nunca, conservando su simbolismo perturbador.

                        La narrativa se mantiene fiel al original, explorando temas de culpa, pérdida y la dualidad de la mente humana. La banda sonora, compuesta por Akira Yamaoka, sigue siendo uno de los aspectos más memorables del juego. Silent Hill 2 es considerado una de las obras maestras del género y su remake es una oportunidad única para experimentarlo de una forma completamente nueva.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2024, 10, 8))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152834/fdzr9lxwibrx1cmdwt8a.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i7-8700K o AMD Ryzen 5 3600X", "memory": "16 GB RAM", "graphics": "NVIDIA GeForce RTX 2080 o AMD Radeon RX 6800 XT", "storage": "50 GB SSD disponible", "directX": "Version 12"}""")
                .recommendedSpecs("""
                        {"os": "Windows 11 de 64 bits", "processor": "Intel Core i7-12700K o AMD Ryzen 7 7800X3D", "memory": "16 GB RAM", "graphics": "NVIDIA GeForce RTX 3080 o AMD Radeon RX 7900 XT", "storage": "50 GB SSD NVMe disponible", "directX": "Version 12"}""")
                .categories(List.of(horror, adventure))
                .build());

        gameService.create(Game.builder()
                .name("Euro Truck Simulator 2")
                .originalPrice(new BigDecimal("19.99"))
                .discountPercent(0)
                .description("""
                        Euro Truck Simulator 2 es un simulador de conducción de camiones que te permite recorrer más de 60 ciudades europeas a bordo de camiones de marcas reales como Scania, Volvo, Mercedes-Benz y MAN. Tu objetivo es simple pero adictivo: transportar carga de un punto a otro, cumplir con los plazos y ganar dinero para expandir tu propia empresa de transporte.

                        El juego destaca por su atención al detalle: la física del camión es realista, incluyendo el comportamiento de la carga, los cambios de marchas, el consumo de combustible y la gestión de los frenos. Deberás respetar los límites de velocidad, los semáforos y las señales de tráfico para evitar multas, y descansar en las áreas de servicio para no quedarte dormido al volante.

                        Con el dinero ganado podrás comprar más camiones, contratar conductores para que trabajen en tu empresa mientras tú descansas, y eventualmente expandir tu flota con vehículos más potentes. La comunidad de modding es enorme, añadiendo nuevos mapas, camiones y mejoras. Es un juego sorprendentemente relajante, perfecto para sesiones largas de viaje virtual.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2012, 10, 18))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152907/q2jmubh8qbjb0jgsih5c.avif")
                .minimumSpecs("""
                        {"os": "Windows 7/8/10/11 de 64 bits", "processor": "Intel Core i5-6400 o AMD Ryzen 3 1200", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 660 2GB o AMD Radeon R9 270X 2GB", "storage": "12 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i5-9600K o AMD Ryzen 5 3600", "memory": "12 GB RAM", "graphics": "NVIDIA GeForce GTX 1060 6GB o AMD Radeon RX 590", "storage": "12 GB SSD disponible"}""")
                .categories(List.of(simulation, indie))
                .build());

        gameService.create(Game.builder()
                .name("Sekiro: Shadows Die Twice")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(50)
                .description("""
                        Sekiro: Shadows Die Twice es un juego de acción y aventura desarrollado por FromSoftware ambientado en el Japón del final de la era Sengoku, en 1500. La historia sigue a un shinobi conocido como "Lobo" que fue salvado de la muerte por un escultor misterioso. Ahora, con un brazo protésico que le permite usar herramientas letales, Lobo jura proteger a su joven señor Kuro, el heredero divino, de las fuerzas que intentan secuestrarlo.

                        A diferencia de Dark Souls, Sekiro se centra en el combate de postura y el parry: la defensa es tan importante como el ataque, y aprender a desviar los golpes enemigos es crucial para romper su postura y ejecutar un muerte instantánea. La verticalidad se ha potenciado con un gancho que permite escalar árboles, edificios y colinas, abriendo nuevas rutas de exploración.

                        El sistema de resurrección añade una capa estratégica: cuando mueres, puedes revivir en el acto, pero si vuelves a morir antes de regresar a un idolo, la resurrección se agota. El juego incluye combates contra jefes épicos como la Gran Serpiente, el Genichiro Way of Tomoe y el Mono Divino Guardián. Es uno de los juegos más desafiantes y gratificantes de FromSoftware.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2019, 3, 22))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788153006/l2zmofnyufiwvlxerayj.webp")
                .minimumSpecs("""
                        {"os": "Windows 7 SP1/8.1/10 de 64 bits", "processor": "Intel Core i3-2100 o AMD FX-6300", "memory": "4 GB RAM", "graphics": "NVIDIA GeForce GTX 760 6GB o AMD Radeon HD 7950 3GB", "storage": "25 GB disponible", "directX": "Version 11"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core i5-2500K o AMD Ryzen 5 1400", "memory": "8 GB RAM", "graphics": "NVIDIA GeForce GTX 970 4GB o AMD Radeon RX 570 4GB", "storage": "25 GB SSD disponible", "directX": "Version 11"}""")
                .categories(List.of(action, adventure))
                .build());

        gameService.create(Game.builder()
                .name("The Sims 4")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(50)
                .description("""
                        The Sims 4 es el simulador de vida más popular del mundo, donde controlas a personas virtuales llamadas Sims y los guías a lo largo de sus vidas. Desde el primer día, tu Sim puede ser un bebé, un adulto joven, un anciano o cualquier edad intermedia, y vivirás con él las alegrías y tristezas de la vida cotidiana: desde su primer trabajo hasta su jubilación.

                        El juego te permite crear Sims únicos eligiendo su apariencia, personalidad, aspiraciones y caminar. La herramienta Create-a-Sim es increíblemente detallada, permitiéndote modificar cada aspecto del cuerpo y la cara de tu Sim. Luego puedes construir su casa ideal con la herramienta Construir, amueblando cada habitación con detalles como sofás, camas, cocinas, baños y artículos decorativos.

                        Los Sims tienen necesidades físicas y emocionales: deben dormir, comer, socializar y divertirse, y tú decides cómo cubrir esas necesidades. Pueden tener familias, carreras, mascotas, e incluso relacionarse románticamente con otros Sims. The Sims 4 es un juego sandbox sin objetivos fijos, perfecto para jugadores creativos que quieran contar sus propias historias. Numerosos packs de expansión amplían la experiencia con nuevos mundos, profesiones y mecánicas.""")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2014, 9, 2))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788153094/rbqhx5vnpk1crm3antvw.webp")
                .minimumSpecs("""
                        {"os": "Windows 10 de 64 bits", "processor": "Intel Core 2 Duo E4300 o AMD Athlon 64 X2 4000+", "memory": "2 GB RAM", "graphics": "NVIDIA GeForce 6600 o ATI Radeon X1300", "storage": "25 GB disponible"}""")
                .recommendedSpecs("""
                        {"os": "Windows 10/11 de 64 bits", "processor": "Intel Core i5-3470 o AMD FX-8350", "memory": "4 GB RAM", "graphics": "NVIDIA GeForce GTX 650 o AMD Radeon HD 7770", "storage": "25 GB SSD disponible"}""")
                .categories(List.of(simulation))
                .build());

        // Users with full profile data
        var now = Instant.now();

        var player1 = userService.create(
                User.builder()
                        .email("player1@gmail.com")
                        .password("pass123")
                        .role(Role.ADMIN)
                        .build(),
                Profile.builder()
                        .nickname("player1")
                        .run("190110222")
                        .firstName("Juan")
                        .lastName("Pérez")
                        .birthDate(LocalDate.of(2001, 10, 22))
                        .region(Region.METROPOLITANA_DE_SANTIAGO)
                        .comuna(Comuna.SANTIAGO)
                        .address("Av. Libertador 1234")
                        .visibility(Visibility.PUBLIC)
                        .build()
        );
        walletService.updateBalance(player1.getId(), new BigDecimal("200"));

        var player2 = userService.create(
                User.builder()
                        .email("player2@gmail.com")
                        .password("pass123")
                        .build(),
                Profile.builder()
                        .nickname("player2")
                        .run("182345679")
                        .firstName("María")
                        .lastName("García")
                        .birthDate(LocalDate.of(1998, 5, 15))
                        .region(Region.VALPARAISO)
                        .comuna(Comuna.VIÑA_DEL_MAR)
                        .address("Calle Los Olivos 567")
                        .visibility(Visibility.PUBLIC)
                        .build()
        );
        walletService.updateBalance(player2.getId(), new BigDecimal("50"));

        userService.create(
                User.builder()
                        .email("broke@gmail.com")
                        .password("pass123")
                        .build(),
                Profile.builder()
                        .nickname("broke_player")
                        .run("201234565")
                        .firstName("Carlos")
                        .lastName("Rodríguez")
                        .birthDate(LocalDate.of(2005, 3, 10))
                        .region(Region.BIOBIO)
                        .comuna(Comuna.CONCEPCION)
                        .address("Pasaje Las Flores 890")
                        .visibility(Visibility.PUBLIC)
                        .build()
        );
    }

    private Category createCategory(String name) {
        return categoryService.create(Category.builder().name(name).build());
    }
}
