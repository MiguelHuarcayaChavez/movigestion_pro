# Spec: Módulo de Autenticación (001-auth.spec.md)

## Propósito
Implementar la seguridad base del sistema: el inicio de sesión unificado y el registro exclusivo de Administradores. Este módulo configura Spring Security, el encriptado BCrypt y la emisión de JSON Web Tokens (JWT).

---

## 1. Contrato (API Protocol)

### Endpoint 1: Inicio de Sesión (Login)
* **Verbo y URL:** `POST /api/v1/auth/login`
* **Seguridad:** Público (No requiere cabecera de autorización).
* **Request Body (LoginRequestDTO):**
    ```json
    {
      "usuario": "miguelfleet",
      "contrasena": "Password123!"
    }
    ```
* **Respuestas Esperadas:**
    * `200 OK`: Retorna el payload de éxito.
        ```json
        {
          "id_usuario": 14,
          "usuario": "miguelfleet",
          "rol": "ADMIN",
          "token": "eyJhbGciOiJIUzI1NiIsInR5..."
        }
        ```
    * `400 Bad Request`: Validaciones de campos vacíos o nulos.
    * `401 Unauthorized`: Credenciales incorrectas. (Estructura de error definida en `rules.md`).

### Endpoint 2: Registro de Administradores
* **Verbo y URL:** `POST /api/v1/auth/register-admin`
* **Seguridad:** Privado. Requiere cabecera `Authorization: Bearer <token>` con rol `ROLE_ADMIN`.
* **Request Body (RegisterAdminRequestDTO):**
    ```json
    {
      "dni": "72145639",
      "usuario": "nuevo_admin",
      "contrasena": "AdminSeguro2026",
      "celular": "987654321"
    }
    ```
* **Respuestas Esperadas:**
    * `201 Created`:
        ```json
        {
          "id_usuario": 15,
          "dni": "72145639",
          "usuario": "nuevo_admin",
          "celular": "987654321",
          "rol": "ADMIN",
          "fecha_creacion": "2026-05-30T10:00:00Z"
        }
        ```
    * `400 Bad Request`: Validaciones fallidas de DTO (ej. DNI no tiene 8 caracteres).
    * `403 Forbidden`: El token enviado pertenece a un rol `ROLE_DRIVER`.
    * `409 Conflict`: Violación de integridad de datos (DNI, usuario o celular duplicados).

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* La lógica interactuará exclusivamente con la entidad `Usuario`, mapeada a la tabla `usuario`.
* Al crear el administrador, los campos por defecto deben ser: `estado = 'ACTIVO'`, `rol = 'ADMIN'`, `fotografia_perfil = null` y `id_admin_creador = null`.

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-AUTH-01:** El endpoint de registro asegura por código que el objeto a guardar tenga inyectado el enumerador de rol `ADMIN`.
* **Implementar BR-DATA-01:** El `AuthService` debe capturar `DataIntegrityViolationException` de Hibernate y transformarlo en una excepción personalizada de negocio para devolver un error `409 Conflict` si el DNI, celular o usuario ya existen en la base de datos.
* **Cifrado Obligatorio:** Antes de invocar `usuarioRepository.save()`, el campo de la contraseña debe ser reemplazado por su versión hasheada usando `BCryptPasswordEncoder` (fuerza 10).

---

## 3. Validación (Criterios de Aceptación para Pruebas)

Al generar el código, se deben contemplar (y prever para los Test Unitarios) los siguientes flujos exactos:

### Flujos de `/api/v1/auth/login`
* ✅ **Happy Path:** Si las credenciales coinciden con la base de datos, el sistema genera y devuelve un JWT válido firmado con `HMAC-SHA256` y expiración en 120 minutos. El código HTTP es 200 OK.
* ❌ **Unhappy Path 1 (Fallo de Autenticación):** Si el usuario no existe o la contraseña cifrada no coincide mediante `passwordEncoder.matches()`, el sistema responde `401 Unauthorized` con el mensaje estándar: *"Usuario o contraseña incorrectos"*.
* ❌ **Unhappy Path 2 (Validación de DTO):** Si se envía el body con el campo `usuario` vacío, el framework intercepta la petición (con `@Valid`) y retorna `400 Bad Request` listando el campo en el arreglo `subErrors`.

### Flujos de `/api/v1/auth/register-admin`
* ✅ **Happy Path:** Un usuario con `ROLE_ADMIN` envía un JSON válido y con datos únicos. El sistema encripta la contraseña, guarda el registro en la tabla `usuario` y responde `201 Created` excluyendo la contraseña de la respuesta.
* ❌ **Unhappy Path 1 (Conflicto de Datos):** Un administrador intenta registrar un DNI que ya existe en la base de datos. El servicio rechaza la transacción y retorna `409 Conflict` con el mensaje: *"Los datos proporcionados (DNI, celular o usuario) ya se encuentran registrados en el sistema"*.
* ❌ **Unhappy Path 2 (Seguridad RBAC):** Un usuario autenticado pero con `ROLE_DRIVER` intenta consumir este endpoint. Spring Security bloquea el acceso en la capa de filtros antes de llegar al controlador y retorna `403 Forbidden`.