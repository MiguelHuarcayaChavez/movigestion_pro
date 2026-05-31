# Diccionario de Datos y Mapeo ORM (database.md)

Este documento contiene la estructura exacta de la base de datos relacional (MySQL 8.4 - InnoDB) para el sistema de Gestión de Flotas.

**REGLA ESTRICTA PARA LA IA:** Toda entidad JPA (`@Entity`) generada debe mapear exactamente a estas tablas usando `snake_case` para la base de datos (`@Table`, `@Column`) y `camelCase` para los atributos en Java. Los enumeradores deben mapearse obligatoriamente con `@Enumerated(EnumType.STRING)`. No inventar tablas ni columnas que no existan en este documento.

---

## 1. Tabla: `usuario`
Centraliza la gestión de accesos y perfiles para Administradores y Transportistas.

| Campo / Columna | Tipo SQL | Restricciones DB | Tipo Java Sugerido | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id_usuario` | INT | PK, AUTO_INCREMENT | `Integer` / `Long` | Identificador único. |
| `dni` | VARCHAR(8) | NOT NULL, UNIQUE | `String` | Documento de identidad. |
| `usuario` | VARCHAR(50) | NOT NULL, UNIQUE | `String` | Username para login. |
| `contrasena` | VARCHAR(255)| NOT NULL | `String` | Hash BCrypt. |
| `celular` | VARCHAR(20) | NOT NULL, UNIQUE | `String` | Teléfono de contacto. |
| `rol` | ENUM | NOT NULL ('ADMIN', 'DRIVER') | `RoleEnum` | Rol de Spring Security. |
| `fotografia_perfil`| VARCHAR(255)| NULL | `String` | URL de la imagen del chofer. |
| `estado` | ENUM | NOT NULL, DEFAULT 'ACTIVO' | `UserStatusEnum` | 'ACTIVO' o 'DE_BAJA'. |
| `id_admin_creador` | INT | FK (`usuario.id_usuario`), NULL | `Usuario` (Self-join)| Admin que registró al chofer. |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | `LocalDateTime` | Fecha de alta. |

**Reglas JPA:** La auto-referencia (`id_admin_creador`) debe mapearse como `@ManyToOne(fetch = FetchType.LAZY)`.

---

## 2. Tabla: `vehiculo`
Almacena el catálogo de unidades de transporte y su estado operativo.

| Campo / Columna | Tipo SQL | Restricciones DB | Tipo Java Sugerido | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id_vehiculo` | INT | PK, AUTO_INCREMENT | `Integer` / `Long` | Identificador único. |
| `placa` | VARCHAR(15) | NOT NULL, UNIQUE | `String` | Placa patente única. |
| `estado` | ENUM | NOT NULL, DEFAULT 'DISPONIBLE' | `VehicleStatusEnum` | 'DISPONIBLE', 'EN_RUTA', 'EN_MANTENIMIENTO', 'DE_BAJA'. |
| `fecha_vencimiento_soat`| DATE | NOT NULL | `LocalDate` | Vencimiento del seguro. |
| `fecha_vencimiento_revision`| DATE | NOT NULL | `LocalDate` | Vencimiento de revisión técnica. |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | `LocalDateTime` | Registro de auditoría. |

---

## 3. Tabla: `envio` (Viajes)
Gestiona el ciclo de vida del transporte logístico.

| Campo / Columna | Tipo SQL | Restricciones DB | Tipo Java Sugerido | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id_envio` | INT | PK, AUTO_INCREMENT | `Integer` / `Long` | Identificador único del viaje. |
| `id_administrador` | INT | FK (`usuario`), NOT NULL | `Usuario` | Creador del viaje. |
| `id_transportista` | INT | FK (`usuario`), NULL | `Usuario` | Chofer asignado. |
| `id_vehiculo` | INT | FK (`vehiculo`), NULL | `Vehiculo` | Vehículo asignado. |
| `destino_latitud` | DECIMAL(10,8)| NOT NULL | `BigDecimal` | Coordenada Y (Google Maps). |
| `destino_longitud` | DECIMAL(11,8)| NOT NULL | `BigDecimal` | Coordenada X (Google Maps). |
| `estado` | ENUM | NOT NULL, DEFAULT 'SIN_CONDUCTOR_ASIGNADO' | `TripStatusEnum` | 'SIN_CONDUCTOR_ASIGNADO', 'ASIGNADO', 'EN_CAMINO', 'COMPLETADO', 'CANCELADO'. |
| `fecha_creacion` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | `LocalDateTime` | Momento de la creación. |
| `fecha_inicio` | TIMESTAMP | NULL | `LocalDateTime` | Transición a EN_CAMINO. |
| `fecha_entrega` | TIMESTAMP | NULL | `LocalDateTime` | Transición a COMPLETADO. |

**Reglas JPA:** Todas las relaciones (`@ManyToOne`) hacia `Usuario` y `Vehiculo` deben usar obligatoriamente `fetch = FetchType.LAZY` para evitar N+1 queries.

---

## 4. Tabla: `incidencia`
Registro inmutable de eventualidades reportadas por los choferes durante un envío en curso.

| Campo / Columna | Tipo SQL | Restricciones DB | Tipo Java Sugerido | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id_incidencia` | INT | PK, AUTO_INCREMENT | `Integer` / `Long` | Identificador único. |
| `id_envio` | INT | FK (`envio`), NOT NULL | `Envio` | Viaje afectado. |
| `id_vehiculo` | INT | FK (`vehiculo`), NOT NULL | `Vehiculo` | Vehículo afectado. |
| `id_transportista` | INT | FK (`usuario`), NOT NULL | `Usuario` | Emisor del reporte. |
| `clasificacion` | ENUM | NOT NULL | `IncidentTypeEnum` | 'VEHICULO', 'RUTA', 'CARGA', 'OTRO'. |
| `descripcion` | TEXT | NOT NULL | `String` | Detalle textual. |
| `latitud_reporte` | DECIMAL(10,8)| NOT NULL | `BigDecimal` | Ubicación GPS del suceso. |
| `longitud_reporte`| DECIMAL(11,8)| NOT NULL | `BigDecimal` | Ubicación GPS del suceso. |
| `fecha_reporte` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | `LocalDateTime` | Timestamp inyectado por backend. |

---

## 5. Índices Secundarios (Optimización)
El diseño contempla los siguientes índices en la base de datos para optimizar las consultas masivas. La IA debe considerarlos al crear consultas en los repositorios de Spring Data JPA.

* `idx_envio_estado`: Sobre `envio.estado` (Mejora búsquedas del dashboard).
* `idx_envio_transportista`: Sobre `envio.id_transportista` (Mejora el listado de "Mis Viajes" del chofer).
* `idx_incidencia_fecha`: Sobre `incidencia.fecha_reporte` (Mejora el feed en tiempo real ordenado).
* `idx_vehiculo_estado`: Sobre `vehiculo.estado` (Acelera la búsqueda de vehículos aptos).

## 6. Prohibiciones de Mutación Estructural
1. No alterar nombres de tablas ni convertirlos al plural (ej. usar `vehiculo`, NO `vehiculos`).
2. No usar borrado físico (`DELETE CASCADE`). Toda relación de llaves foráneas para actualizaciones debe ser `ON UPDATE CASCADE` y para borrado `ON DELETE RESTRICT` (o `SET NULL` donde corresponda según el esquema SQL principal).