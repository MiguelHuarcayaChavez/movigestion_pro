# Diseño del Sistema y Arquitectura (system_design.md)

Este documento define la infraestructura técnica, el patrón arquitectónico, los mecanismos de comunicación y la distribución física del código para el backend del sistema de Gestión de Flotas. Toda nueva funcionalidad debe acoplarse estrictamente a los lineamientos definidos aquí.

## 1. Stack Tecnológico

El proyecto se ejecuta sobre las siguientes tecnologías y versiones estables:

* **Lenguaje:** Java 21 LTS (Uso de Virtual Threads habilitado si aplica).
* **Framework Principal:** Spring Boot 3.4.x.
* **Gestor de Dependencias:** Maven (pom.xml) o Gradle (build.gradle).
* **Seguridad:** Spring Security + JSON Web Tokens (`io.jsonwebtoken:jjwt`).
* **Persistencia:** Spring Data JPA (Hibernate).
* **Base de Datos:** MySQL 8.4 LTS (Motor InnoDB).
* **Validaciones:** Spring Boot Validation (Hibernate Validator).
* **Mapeo de Objetos:** MapStruct o ModelMapper (para conversión Entity <-> DTO).

## 2. Patrón Arquitectónico (Monolito por Capas)

El backend se estructura como un **Monolito con Arquitectura Tradicional por Capas**. Se imponen las siguientes reglas inquebrantables de flujo de dependencias:

1.  **Capa de Presentación (`controller/`):** Expone los endpoints REST. Solo puede comunicarse con la capa de Servicio. **Prohibido** inyectar repositorios aquí. Recibe y retorna DTOs exclusivamente.
2.  **Capa de Negocio (`service/`):** Contiene las Business Rules. Consume los repositorios y APIs externas. Realiza las validaciones lógicas complejas y transacciones (`@Transactional`).
3.  **Capa de Acceso a Datos (`repository/`):** Interfaces que extienden de `JpaRepository`. Encargadas exclusivas de interactuar con MySQL.
4.  **Capa de Dominio (`entity/`):** Clases mapeadas a tablas de base de datos. No deben exponerse al exterior (se transforman a DTOs en el Service o Controller).

## 3. Distribución de Carpetas (Physical Layout)

Todo el código fuente debe ubicarse dentro de `src/main/java/com/fleetmanager/`. Ninguna clase debe crearse fuera de su paquete designado.

```text
fleet-manager-backend/
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── fleetmanager/
        │           ├── FleetManagerApplication.java
        │           ├── config/         # Configuraciones (Cors, Beans externos)
        │           ├── security/       # Filtros JWT, configuraciones de Spring Security
        │           ├── controller/     # Endpoints HTTP (@RestController)
        │           ├── service/        # Lógica de negocio (@Service)
        │           ├── repository/     # Interfaces de base de datos (@Repository)
        │           ├── entity/         # Mapeo ORM (@Entity, @Table)
        │           ├── dto/            # Objetos de transferencia divididos por contexto
        │           │   ├── request/    # DTOs entrantes
        │           │   └── response/   # DTOs salientes
        │           └── exception/      # Manejo global (@RestControllerAdvice)
        └── resources/
            └── application.properties  # Variables de entorno inyectadas
```

## 4. Mecanismos de Comunicación e Integraciones
### 4.1. Comunicación Cliente-Servidor
* Protocolo: REST síncrono sobre HTTP/HTTPS.
* Formato de Intercambio: `application/json` obligatorio para peticiones y respuestas.
* Tiempo Real (Dashboard): Se implementa mediante Short Polling. El cliente realizará peticiones `GET` periódicas (ej. cada 30 segundos) al endpoint de incidencias activas. No se utilizarán WebSockets.

### 4.2. Integraciones de Terceros (Síncronas Salientes)
* Google Maps Platform: El backend actúa como cliente HTTP (`RestTemplate` o `WebClient`) para consumir la Geocoding API. Se utiliza para convertir direcciones textuales en coordenadas (`latitud`, `longitud`) de forma síncrona antes de guardar un viaje.

## 5. Estrategia de Autenticación y Seguridad
El sistema es stateless (sin estado de sesión en memoria del servidor).
1. Hashing de Contraseñas: Uso obligatorio de `BCryptPasswordEncoder` con factor de costo 10 (`strength = 10`) para almacenar o verificar credenciales en la base de datos.
2. Mecanismo JWT: * Firma criptográfica utilizando el algoritmo HMAC-SHA256.
   * Expiración estricta de 2 horas (120 minutos).
   * El token debe viajar obligatoriamente en la cabecera HTTP: `Authorization: Bearer <token>`.
3. Filtro de Seguridad (`JwtAuthenticationFilter`): Intercepta todas las peticiones (excepto rutas públicas como `/api/v1/auth/login`). Valida la firma del token, extrae el `id_usuario` y `rol` (`ADMIN` o `DRIVER`), y los inyecta en el `SecurityContextHolder` de Spring para que las capas inferiores tengan contexto del usuario en ejecución.

## 6. Configuración y Variables de Entorno
Toda configuración sensible o dependiente del entorno se administra mediante variables de entorno (alojadas en un archivo `.env` en la raíz) e inyectadas a través de `application.properties`. Queda estrictamente prohibido el hardcoding de estas variables.

Variables requeridas:
* `SERVER_PORT`: Puerto de ejecución (ej. 8080).
* `SPRING_PROFILES_ACTIVE`: Entorno activo (`dev`, `prod`).
* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`: Credenciales de MySQL.
* `JWT_SECRET`: Llave de firma en Base64 (mínimo 256 bits).
* `JWT_EXPIRATION_MINUTES`: Tiempo de vida del token (default: 120).
* `Maps_API_KEY`: Credencial para uso de Geocoding API.
* `ALLOWED_ORIGINS`: URLs permitidas para políticas de CORS.