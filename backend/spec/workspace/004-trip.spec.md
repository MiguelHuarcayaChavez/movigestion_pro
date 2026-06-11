# Spec: Módulo de Envíos y Viajes (004-trip.spec.md)

## Propósito
Gestionar la planificación, asignación y el ciclo de vida de los viajes. Este módulo implementa la máquina de estados estricta y las restricciones operativas que impiden que un vehículo o conductor sean sobreasignados o salgan a ruta con documentación vencida.

---

## 1. Contrato (API Protocol)

Todas las rutas requieren la cabecera `Authorization: Bearer <token>`.

### Endpoint 1: Crear y Asignar Viaje (Create)
* **Verbo y URL:** `POST /api/v1/trips`
* **Seguridad:** Privado (`ROLE_ADMIN`).
* **Request Body (CreateTripRequestDTO):**
    ```json
    {
      "idTransportista": 5, 
      "idVehiculo": 2,      
      "destinoLatitud": -12.046374,
      "destinoLongitud": -77.042754
    }
    ```
  *(Nota: `idTransportista` e `idVehiculo` pueden viajar como `null` si la ruta se crea vacía).*
* **Respuestas Esperadas:**
    * `201 Created`: Retorna el ID del viaje y su estado calculado (`SIN_CONDUCTOR_ASIGNADO` o `ASIGNADO`).
    * `400 Bad Request`: Coordenadas faltantes o fuera de rango.
    * `422 Unprocessable Entity`: Restricción de flota (SOAT vencido, vehículo en mantenimiento, o conductor ya asignado a otra ruta).

### Endpoint 2: Listar Viajes Globales (Read - Admin)
* **Verbo y URL:** `GET /api/v1/trips`
* **Seguridad:** Privado (`ROLE_ADMIN`).
* **Query Params:** `?estado=EN_CAMINO` (Opcional).
* **Respuestas Esperadas:**
    * `200 OK`: Arreglo de viajes con sus detalles (incluye datos anidados básicos de conductor y vehículo). Bloqueado para `ROLE_DRIVER`.

### Endpoint 3: Mis Viajes (Read - Driver)
* **Verbo y URL:** `GET /api/v1/trips/my-trips`
* **Seguridad:** Privado (`ROLE_DRIVER`).
* **Query Params:** `?scope=active` (Retorna el viaje actual en ruta) o `?scope=pending` (viajes asignados por iniciar).
* **Respuestas Esperadas:**
    * `200 OK`: Arreglo de viajes filtrados por el ID del JWT del conductor.

### Endpoint 4: Actualizar Estado del Viaje (State Machine)
* **Verbo y URL:** `PATCH /api/v1/trips/{id}/status`
* **Seguridad:** Privado (`ROLE_ADMIN` y `ROLE_DRIVER`).
* **Request Body (UpdateTripStatusDTO):**
    ```json
    {
      "nuevoEstado": "EN_CAMINO"
    }
    ```
* **Respuestas Esperadas:**
    * `200 OK`: El viaje transitó correctamente de estado.
    * `422 Unprocessable Entity`: Violación de la secuencia de estados (ej. saltar de ASIGNADO a COMPLETADO).
    * `403 Forbidden`: Un chofer intenta poner estado CANCELADO (solo Admin puede hacerlo) o intenta modificar un viaje que no le pertenece.

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* Este servicio coordina de forma transaccional (`@Transactional`) las entidades `Envio`, `Vehiculo` y `Usuario`.
* Al crear el viaje, el `id_administrador` se extrae automáticamente del token (JWT).

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-FLEET-01:** Antes de guardar o actualizar a estado `ASIGNADO`, el servicio debe consultar si el conductor y el vehículo tienen otro viaje con estado `EN_CAMINO`. Si es así, lanza excepción.
* **Implementar BR-FLEET-02:** Si el DTO incluye un `idVehiculo`, el servicio debe verificar la tabla `Vehiculo`. Si `estado == EN_MANTENIMIENTO`, `fecha_vencimiento_soat <= CURRENT_DATE` o `fecha_vencimiento_revision <= CURRENT_DATE`, la asignación se rechaza.
* **Implementar BR-TRIP-01:** El servicio debe validar mediante sentencias `if/switch` que el `nuevoEstado` solicitado sigue exactamente el flujo: `SIN_CONDUCTOR_ASIGNADO` -> `ASIGNADO` -> `EN_CAMINO` -> `COMPLETADO`. El estado `CANCELADO` es exclusivo del Administrador.
* **Implementar BR-TRIP-02:** Si un viaje transiciona a `COMPLETADO` o `CANCELADO`, el mismo método debe localizar al `Vehiculo` y `Usuario` asociados y setear el estado de ambos a `DISPONIBLE` para devolverlos al pool logístico.

---

## 3. Validación (Criterios de Aceptación para Pruebas)

El código generado debe poder superar los siguientes escenarios de test unitario:

### Flujos de `POST /api/v1/trips`
* ✅ **Happy Path:** Un Administrador crea un viaje enviando un ID de conductor libre y un vehículo con SOAT válido. El servicio crea la entrada, fija el estado a `ASIGNADO`, cambia el estado del vehículo a `EN_RUTA`, y responde `201 Created`.
* ❌ **Unhappy Path 1 (Restricción Vial Operativa):** Se intenta crear o asignar un viaje con un vehículo cuyo SOAT venció ayer. El servicio aborta la transacción y responde `422 Unprocessable Entity` con el mensaje: *"El vehículo no puede ser asignado porque tiene el SOAT o la Revisión Técnica vencidos."*
* ❌ **Unhappy Path 2 (Sobre-asignación 1:1):** Se intenta asignar a un conductor que ya figura como `id_transportista` en otro viaje en estado `EN_CAMINO`. El servicio responde `422 Unprocessable Entity` notificando la colisión operativa.

### Flujos de `PATCH /api/v1/trips/{id}/status`
* ✅ **Happy Path (Liberación de Recursos):** El Conductor autenticado cambia su viaje activo de `EN_CAMINO` a `COMPLETADO`. El servicio actualiza el viaje, marca `fecha_entrega` con el timestamp actual y automáticamente actualiza al conductor y su vehículo a estado `DISPONIBLE`. Retorna `200 OK`.
* ❌ **Unhappy Path 1 (Violación Máquina de Estados):** El conductor recibe un viaje en estado `ASIGNADO` e intenta mandar un PATCH directo a `COMPLETADO` sin haber pasado por `EN_CAMINO`. El servicio rechaza la petición con `422 Unprocessable Entity` indicando: *"Transición de estado inválida."*
* ❌ **Unhappy Path 2 (Violación de Autorización por Rol):** Un Transportista intenta enviar un estado `CANCELADO`. El servicio detecta el rol desde el `SecurityContext` y bloquea la operación retornando `403 Forbidden` indicando que esa transición es exclusiva de administración.