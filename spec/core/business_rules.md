# Reglas de Negocio y Lógica de Dominio (business_rule.md)

Este documento centraliza las leyes operativas, los flujos de estado y las restricciones del sistema de Gestión de Flotas. Todo código generado debe validar estrictamente estas reglas antes de procesar cualquier transacción. Estas leyes operan independientemente del framework o la base de datos subyacente.

## 1. Definición de Roles y Autorización (BR-AUTH)

El sistema opera bajo un modelo estricto de Control de Acceso Basado en Roles (RBAC) y Propiedad de Recursos:

* **Administrador de Flotas (`ROLE_ADMIN`):** Tiene control absoluto sobre la creación, modificación y lectura global de todos los recursos (Flota, Conductores, Envíos, Incidencias, Analíticas). **Único rol con capacidad para cancelar viajes y registrar/dar de baja choferes.**
* **Transportista (`ROLE_DRIVER`):** Rol puramente operativo. **Solo puede** leer la información de sus propios viajes asignados, modificar el estado de su viaje activo y reportar incidencias vinculadas a su persona.
* **Exclusividad y Separación:** Los roles son mutuamente excluyentes. Un Transportista no tiene acceso a endpoints de configuración global, y un Administrador no puede simular iniciar rutas ni crear incidencias de campo.
* **Autenticación Inicial:** Los transportistas no pueden registrarse por sí mismos; un Administrador debe proveer obligatoriamente sus credenciales iniciales.
* **Excepción de Aprovisionamiento Inicial:** El endpoint `/api/v1/auth/register-admin` se expone de forma pública únicamente para permitir el registro del primer usuario administrador del sistema en entornos con bases de datos limpias. Una vez creado el primer registro, el login protegerá el resto del ciclo operativo.

## 2. Reglas de Integridad General y Auditoría (BR-DATA)

* **Unicidad Global:** No pueden existir duplicados en el sistema bajo ninguna circunstancia para los siguientes campos operativos:
    * `DNI`, `Usuario` y `Celular` (aplicable tanto para Administradores como Transportistas).
    * `Placa` (aplicable para todos los vehículos).
* **Inmutabilidad Histórica (Borrado Lógico):** Queda estrictamente prohibida la eliminación física de registros. Si un conductor renuncia o un vehículo es vendido, su estado debe cambiar a `DE_BAJA` o `INACTIVO`. Esto preserva el historial de viajes y auditorías vinculadas a esos identificadores.

## 3. Lógica de Asignación de Flota (BR-FLEET)

La asignación de recursos logísticos está protegida por restricciones de seguridad vial y disponibilidad:

* **Restricción de Operatividad Vial (Hard Constraint):** El sistema **rechazará** la asignación de cualquier vehículo a un viaje si se cumple alguna de estas condiciones:
    1.  El vehículo está en estado `EN_MANTENIMIENTO`.
    2.  La fecha de vencimiento del SOAT es menor o igual a la fecha actual (`fecha_vencimiento_soat <= CURRENT_DATE`).
    3.  La fecha de vencimiento de la Revisión Técnica es menor o igual a la fecha actual.
* **Exclusividad 1:1 en Ruta:** Un vehículo y un conductor solo pueden estar vinculados a un (1) viaje en estado `EN_CAMINO` de manera simultánea. No se permiten asignaciones paralelas activas.

## 4. Máquina de Estados del Viaje / Envío (BR-TRIP)

El ciclo de vida de un viaje (Envío) es rígido y debe validarse en cada petición de cambio de estado.

### 4.1. Flujo Permitido
1.  **`SIN_CONDUCTOR_ASIGNADO`**: Estado inicial. El Administrador crea la ruta con el destino geolocalizado pero sin definir los recursos.
2.  **`ASIGNADO`**: El Administrador vincula un Conductor y un Vehículo aptos.
3.  **`EN_CAMINO`**: El Conductor (y solo el conductor asignado) notifica que ha iniciado el traslado.
4.  **`COMPLETADO`**: El Conductor notifica la llegada exitosa al destino.
5.  **`CANCELADO`**: Solo el Administrador puede forzar este estado por causa de fuerza mayor.

### 4.2. Restricciones de Transición
* No se puede pasar directamente de `ASIGNADO` a `COMPLETADO`.
* El estado `CANCELADO` puede ejecutarse desde cualquier punto, **excepto** si el viaje ya está en estado `COMPLETADO`.
* **Liberación de Recursos:** En el instante en que un viaje transiciona a `COMPLETADO` o `CANCELADO`, el sistema debe liberar automáticamente al Vehículo y al Conductor, devolviendo sus estados individuales a `DISPONIBLE`, para que puedan recibir nuevas asignaciones.

## 5. Reglas de Emisión de Incidencias (BR-INC)

El reporte de problemas en ruta (mecánicos, tráfico, accidentes) debe ser lo más sencillo y seguro posible para el conductor:

* **Inyección Automática de Contexto:** Cuando un conductor emite una alerta, el sistema no debe confiar ni pedir que el conductor digite el ID del viaje o del vehículo. El backend debe identificar al usuario, buscar su viaje activo en estado `EN_CAMINO`, y vincular automáticamente la incidencia a esos identificadores.
* **Bloqueo de Falsos Reportes:** Si el conductor intenta reportar una incidencia pero no tiene ningún viaje activo (`EN_CAMINO`), la operación debe ser rechazada.
* **Inmutabilidad de Reportes:** Una vez enviada a la base de datos, una incidencia es de **solo lectura**. Ni el transportista ni el administrador pueden editar su contenido, fecha, o ubicación GPS, garantizando su validez legal y de auditoría.