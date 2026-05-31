# Spec: Módulo de Transportistas (003-driver.spec.md)

## Propósito
Implementar el registro controlado y la consulta del personal de conducción (Transportistas). A diferencia de un sistema tradicional, los conductores no se autoregistran; son dados de alta en el sistema por un Administrador, quien les provee sus credenciales de acceso iniciales.

---

## 1. Contrato (API Protocol)

Todas las rutas requieren la cabecera `Authorization: Bearer <token>`. Las restricciones por rol se detallan en cada endpoint.

### Endpoint 1: Pre-registro de Transportista (Create)
* **Verbo y URL:** `POST /api/v1/drivers`
* **Seguridad:** Privado (Estricto para `ROLE_ADMIN`).
* **Request Body (CreateDriverRequestDTO):**
    ```json
    {
      "dni": "74859612",
      "usuario": "juan_driver",
      "contrasena": "ChoferTemporal2026",
      "celular": "987654321",
      "fotografiaPerfil": "[https://storage.com/profiles/juan.jpg](https://storage.com/profiles/juan.jpg)"
    }
    ```
* **Respuestas Esperadas:**
    * `201 Created`:
        ```json
        {
          "id_usuario": 16,
          "dni": "74859612",
          "usuario": "juan_driver",
          "celular": "987654321",
          "rol": "DRIVER",
          "estado": "ACTIVO",
          "id_admin_creador": 14
        }
        ```
    * `400 Bad Request`: Validaciones de formato fallidas (ej. DNI inválido).
    * `403 Forbidden`: Un chofer intenta crear a otro chofer.
    * `409 Conflict`: DNI, celular o usuario ya existentes.

### Endpoint 2: Listar Transportistas (Read Global)
* **Verbo y URL:** `GET /api/v1/drivers`
* **Seguridad:** Privado (`ROLE_ADMIN`).
* **Query Params (Opcionales):** `?estado=ACTIVO`
* **Respuestas Esperadas:**
    * `200 OK`: Retorna un arreglo `[]` con el listado de conductores registrados en el sistema. Bloqueado para el rol `DRIVER`.

### Endpoint 3: Ver Detalle de Transportista (Read Single)
* **Verbo y URL:** `GET /api/v1/drivers/{id}`
* **Seguridad:** Privado (`ROLE_ADMIN` o `ROLE_DRIVER` con restricción).
* **Respuestas Esperadas:**
    * `200 OK`: Retorna el objeto JSON con el detalle del conductor.
    * `403 Forbidden`: Si el usuario que hace la petición es un `ROLE_DRIVER` y el `{id}` de la URL no coincide con su propio ID del JWT.
    * `404 Not Found`: El ID no existe en la base de datos.

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* Interacción con la entidad `Usuario` (filtrando lógicamente para operar solo con aquellos cuyo `rol = 'DRIVER'`).
* **Auditoría de Creación:** Al ejecutar el `POST`, el backend debe extraer el `id_usuario` del administrador autenticado desde el `SecurityContextHolder` (el JWT) e inyectarlo en el campo `id_admin_creador` del nuevo conductor.

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-AUTH-01 y BR-AUTH-02:** El endpoint garantiza que un administrador crea al conductor. El servicio debe encriptar obligatoriamente la `contrasena` recibida en el body usando `BCryptPasswordEncoder` antes de guardarla. El campo `rol` debe quemarse por código como `DRIVER`.
* **Implementar BR-DATA-01:** Capturar y manejar la unicidad de `dni`, `usuario` y `celular`, retornando un error HTTP `409 Conflict` si se detecta un duplicado.
* **Filtro de Alcance (Scope Enforcement):** Para el endpoint `GET /api/v1/drivers/{id}`, el controlador debe validar explícitamente:
  `if (rol == DRIVER && jwt.getUserId() != path.id) throw AccessDeniedException`.

---

## 3. Validación (Criterios de Aceptación para Pruebas)

El desarrollo y los test unitarios deben contemplar estrictamente los siguientes flujos:

### Flujos de `POST /api/v1/drivers`
* ✅ **Happy Path:** Un Administrador envía datos válidos. El servicio extrae el ID del Admin del contexto de seguridad, encripta la clave temporal, guarda al usuario con rol `DRIVER` y responde `201 Created` excluyendo la contraseña en el DTO de salida.
* ❌ **Unhappy Path 1 (Violación de Seguridad):** Un Transportista con un JWT válido intenta consumir el endpoint de creación. Spring Security bloquea la petición antes del controlador (`403 Forbidden`).
* ❌ **Unhappy Path 2 (Datos Duplicados):** El Administrador intenta registrar a un conductor con un número de celular que ya pertenece a otro usuario en la base de datos. El servicio captura el error y responde `409 Conflict`.

### Flujos de `GET /api/v1/drivers/{id}`
* ✅ **Happy Path (Administrador):** Un Admin solicita el ID de cualquier chofer. El sistema devuelve la información correctamente (`200 OK`).
* ✅ **Happy Path (Transportista):** Un Chofer solicita la información de *su propio ID*. El sistema valida que el ID solicitado coincide con su sesión activa y devuelve los datos (`200 OK`).
* ❌ **Unhappy Path 1 (Ataque IDOR / Referencia Insegura):** Un Chofer autenticado (ej. ID 10) modifica la URL para intentar ver los datos de su compañero (ej. ID 12). El servicio intercepta la discrepancia entre el JWT y la URL, denegando el acceso con un `403 Forbidden` y el mensaje: *"No tiene los permisos necesarios para visualizar el perfil de otro usuario."*
