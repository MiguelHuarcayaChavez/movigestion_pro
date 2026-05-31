# Reglas de Desarrollo y Estándares de Código (rules.md)

Este documento define las reglas estrictas de desarrollo, formato de código y estructura de respuestas para el proyecto Gestión de Flotas. **CUALQUIER** código generado por inteligencia artificial o escrito por desarrolladores humanos debe adherirse estrictamente a estas directrices. No se permiten desviaciones.

## 1. Stack Tecnológico Core
- **Lenguaje:** Java 21 LTS. Se deben utilizar las características modernas de Java (ej. `Records` para DTOs si aplica, text blocks, pattern matching).
- **Framework Principal:** Spring Boot 3.4.x.
- **ORM:** Spring Data JPA (Hibernate).
- **Seguridad:** Spring Security con JSON Web Tokens (jjwt).

## 2. Convenciones de Nomenclatura (Naming Conventions)
- **Clases e Interfaces:** `PascalCase` (ej. `VehicleController`, `TripService`).
- **Métodos y Variables:** `camelCase` (ej. `findVehicleById`, `plateNumber`).
- **Constantes:** `UPPER_SNAKE_CASE` (ej. `MAX_LOGIN_ATTEMPTS`).
- **Paquetes:** Minúsculas y en singular (ej. `com.fleetmanager.controller`, no `controllers`).
- **Archivos:** El nombre del archivo debe coincidir exactamente con la clase pública (ej. `VehicleController.java`).

## 3. Estándares de Código y Limpieza (Clean Code)
- **Cero Lógica en Controladores:** Los Controladores (`@RestController`) solo deben recibir la petición HTTP, validar el DTO de entrada y delegar la ejecución inmediatamente a un Servicio (`@Service`).
- **Uso Estricto de DTOs:** Nunca se debe exponer una Entidad JPA (`@Entity`) directamente en el Controlador. Todo dato de entrada o salida debe mapearse mediante clases DTO (`RequestDTO` / `ResponseDTO`).
- **Inyección de Dependencias:** Utilizar inyección por constructor de forma obligatoria (recomendado usar `@RequiredArgsConstructor` de Lombok si está disponible, o constructor explícito). Prohibido el uso de `@Autowired` en los atributos (Field Injection).
- **Comentarios:** Prohibidos los comentarios redundantes que expliquen lo obvio (ej. `// Guarda el usuario`). Solo se permiten comentarios en formato JavaDoc para lógica de negocio altamente compleja o para documentar interfaces de Servicios.

## 4. Contrato Estricto de la API (Gestión de Respuestas y Errores)

La estructura de las respuestas HTTP es inmutable. El backend nunca debe devolver formatos variables.

### 4.1. Respuestas de Éxito (200 OK / 201 Created)
Se debe retornar directamente el objeto JSON o el Array de objetos requeridos, sin envoltorios (wrappers) innecesarios como `{"data": {...}, "success": true}`.

**Ejemplo de Éxito:**
```json
{
  "id_envio": 45,
  "estado": "ASIGNADO"
}
```

### 4.2. Respuestas de Error (4xx y 5xx)
Cualquier error, excepción técnica o violación de regla de negocio debe ser interceptado por un `@RestControllerAdvice}` y formateado OBLIGATORIAMENTE bajo la siguiente estructura JSON:
```json
{
  "timestamp": "YYYY-MM-DDTHH:mm:ssZ",
  "status": <HTTP_STATUS_CODE_INT>,
  "error": "<HTTP_STATUS_TEXT>",
  "message": "<MENSAJE_AMIGABLE_PARA_EL_USUARIO>",
  "path": "<URI_ORIGINAL_DE_LA_PETICION>",
  "subErrors": null
}
```

### 4.3. Errores de Validación (HTTP 400)
Cuando el error provenga de validaciones fallidas en formularios (`@Valid`, `@NotNull`, etc.), el campo `subErrors` debe poblarse con un array de objetos detallando qué campos fallaron.

Formato obligatorio para `subErrors`:
```json
"subErrors": [
  {
    "field": "celular",
    "rejectedValue": "9123",
    "message": "El número de celular debe tener entre 9 y 15 caracteres."
  }
]
```

## 5. Prácticas Prohibidas (Hard Constraints)

1. Borrado Físico: Prohibido usar `repository.delete()`. Se debe implementar un borrado lógico actualizando el campo `estado` a `DE_BAJA` o `INACTIVO`.

2. Contraseñas en Texto Plano: Toda contraseña debe ser encriptada con `BCryptPasswordEncoder` antes de tocar la base de datos.

3. Hardcoding: Ninguna credencial, clave JWT (`JWT_SECRET`), o URL de Google Maps debe estar en el código fuente. Todo debe inyectarse vía `@Value` desde el archivo `application.properties` (y por ende, desde el `.env`).