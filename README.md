# MoviGestión - Plataforma Logística de Control de Flotas

MoviGestión es un sistema web integral (Monolito distribuido en Frontend y Backend) diseñado para la administración, programación y monitoreo en tiempo real de viajes, vehículos y reportes de incidencias críticas en operaciones logísticas de transporte.

---

## 1. Lógica y Estructura del Proyecto

### Stack Tecnológico
* **Backend:** Java 21 LTS, Spring Boot 4.0.6, Spring Security, JWT (Json Web Tokens), Spring Data JPA, MySQL 8.4.
* **Frontend:** React, Vite, TypeScript, Tailwind CSS v4, Axios, React Router DOM v6.



### Arquitectura del Backend (Spring Boot)
El backend está diseñado bajo un **patrón monolítico tradicional por capas**, garantizando la separación de responsabilidades y la inmutabilidad de las entidades de cara al cliente:

```text
backend/src/main/java/com/movigestion/
│
├── config/             # Configuraciones globales (CORS, Beans de Seguridad, OpenAPI/Swagger)
├── controller/         # Capa de Presentación (Controladores REST que exponen los endpoints)
├── service/            # Capa de Negocio (Interfaces e Implementaciones con las Business Rules)
├── repository/         # Capa de Acceso a Datos (Interfaces que extienden de JpaRepository)
├── entity/             # Capa de Dominio (Entidades ORM autovalidadas mapeadas a MySQL)
└── dto/                # Objetos de Transferencia de Datos (Data Transfer Objects)
    ├── request/        # DTOs de entrada (Validaciones con Jakarta Validation)
    └── response/       # DTOs de salida estructurados para el Frontend
```

### Reglas de Negocio Críticas en el Backend (Core Logic)
* Máquina de Estados de los Viajes: Los envíos siguen un flujo estricto y secuencial controlado por código:
SIN_CONDUCTOR_ASIGNADO ➔ ASIGNADO ➔ EN_CAMINO ➔ COMPLETADO.

* Restricciones de Seguridad Vial (Hard Constraints): El sistema bloquea automáticamente la asignación de un vehículo si su SOAT o Revisión Técnica están vencidos, o si la unidad se encuentra en estado EN_MANTENIMIENTO.

* Exclusividad Operativa 1:1: Un conductor y un vehículo solo pueden estar vinculados a un único viaje activo con estado EN_CAMINO en un mismo instante.

* Inyección de Contexto en Incidencias: El conductor no digita identificadores al reportar un problema mecánico o vial; el backend detecta su identidad desde el JWT, localiza su viaje activo en curso e inyecta los datos de forma inmutable.


### Arquitectura del Frontend (React + TypeScript)
El frontend organiza sus componentes mediante una arquitectura basada en características (Feature-Driven Development), aislando los módulos funcionales para facilitar el mantenimiento:

```text
frontend/src/
│
├── assets/             # Recursos estáticos (Imágenes y SVGs globales)
├── components/         # Componentes transversales
│   ├── layout/         # Estructura visual de la app (Sidebar, Aside, Main View)
│   └── protected/      # Guardias de rutas con persistencia RBAC
├── config/             # Constantes globales del cliente
├── features/           # Módulos encapsulados por contexto de negocio
│   ├── auth/           # Formularios de Login y Registro de Administradores
│   ├── dashboard/      # Vista central de analíticas y KPIs métricos
│   ├── drivers/        # Control y pre-registro de transportistas
│   ├── incidents/      # Consola de monitoreo e historial de reportes
│   └── trips/          # Grilla de programación de rutas y visualización de viajes
├── routes/             # Enrutador centralizado y protección de vistas por roles
├── services/           # Clientes HTTP (Axios) conectados a los endpoints de la API
├── store/              # Estado Global de la aplicación (Context API de Autenticación)
└── types/              # Tipados estrictos de TypeScript e interfaces del modelo
```


### Control de Accesos y Seguridad en el Cliente (RBAC)
La aplicación utiliza un sistema estricto de Control de Acceso Basado en Roles (RBAC) administrado mediante el estado global de React. Las rutas están protegidas en el cliente según los privilegios devueltos por el JSON de autenticación del servidor:

| Rol | Vistas Permitidas | Acciones Permitidas|
| --- | --- | --- |
| ADMIN | Dashboard, Flota, Conductores, Viajes, Incidencias | CRUD de flota, dar de alta transportistas, programar rutas, cancelar envíos, ver KPIs globales.|
| DRIVER | Mis Viajes, Mis Reportes | Iniciar ruta asignada, confirmar entrega de carga, reportar incidencias geolocalizadas en tiempo real.|

---
## 2. Capturas y Funcionalidades

### Autenticación Unificada y Persistencia RBAC
* **Descripción Técnica:** Implementación de un flujo de autenticación *stateless* basado en JSON Web Tokens (JWT) firmado bajo el algoritmo HMAC-SHA256. Al iniciar sesión con credenciales validadas por Spring Security, el servidor responde con un objeto `AuthResponseDTO` que inyecta de forma síncrona el ID, el nombre de usuario y el rol específico (`ADMIN` o `DRIVER`). El frontend almacena el token en el `localStorage` y parsea el rol en la capa global de `AuthContext` para habilitar de manera reactiva las rutas del sistema mediante guardias condicionales (`ProtectedRoute`)


![Inicio de Sesión y Autenticación](/docs/Inicio%20de%20Sesión%20y%20Autenticación.png)

---

### Panel de Control y Analíticas Centralizadas
* **Descripción Técnica:** Módulo de solo lectura optimizado que realiza agregaciones aritméticas eficientes directamente en la base de datos MySQL (evitando la sobrecarga de memoria por traer listas completas a Java). Resuelve consultas calculadas de viajes mensuales, alertas activas y porcentajes de entregas a tiempo. Este endpoint (`/analytics/dashboard`) está diseñado para integrarse con mecanismos de *Short Polling* en el frontend, actualizando el estado de la interfaz periódicamente cada 30 segundos sin romper la consistencia transaccional.


![Panel de Control General](/docs/Panel%20de%20Control%20General.png)

---

### Gestión y Validación Automatizada de Flota
* **Descripción Técnica:** CRUD completo enfocado en la disponibilidad operativa del catálogo de vehículos (`vehiculo`). Utiliza validadores estrictos de la API de fechas de Java 21 (`LocalDate`) para contrastar de manera automática el estado del SOAT y la Revisión Técnica en tiempo real contra el sistema operativo (`CURRENT_DATE`). Si un vehículo presenta documentación vencida o se encuentra bajo el estado `EN_MANTENIMIENTO`, la base de datos levanta una restricción lógica y el servicio bloquea inmediatamente cualquier intento de asignación logística en el flujo de despacho.


![Catálogo de Flota Operativa](/docs/Catálogo%20de%20Flota%20Operativa.png)

---

### Programación Logística y Secuenciación de Viajes
* **Descripción Técnica:** Módulo transaccional (`@Transactional`) que encapsula la máquina de estados rígida para el ciclo de vida de las cargas logísticas (`envio`). Los administradores despachan rutas geolocalizadas vinculando conductores y unidades libres. Una vez asignados, los recursos cambian automáticamente a estado `EN_RUTA`. El backend restringe mediante condicionales estrictas que los choferes solo avancen un estado a la vez (`ASIGNADO` ➔ `EN_CAMINO` ➔ `COMPLETADO`) e impide ataques de referencia insegura (IDOR) asegurando que el conductor logueado solo pueda interactuar con el viaje asociado a su ID de sesión. Al completarse o cancelarse la ruta, el pool logístico se libera de forma automática a estado `DISPONIBLE`.


![Consola de Despacho Logístico](/docs/Consola%20de%20Despacho%20Logístico.png)

---

### Inyección Inmutable de Contexto para Incidencias
* **Descripción Técnica:** Registro inmutable de sucesos críticos en ruta (fallas mecánicas, accidentes, carga comprometida) operativo exclusivamente para el rol `DRIVER`. Para mitigar errores de digitación por parte de los operadores en situaciones de emergencia, el cuerpo de la petición (`CreateIncidentRequestDTO`) no requiere IDs lógicos. El backend intercepta de forma segura el contexto de seguridad (`SecurityContextHolder`), resuelve el ID del transportista logueado, localiza su viaje activo condicionado a estado `EN_CAMINO` y vincula automáticamente la incidencia al envío y camión correspondientes. Diseñado bajo arquitectura inmutable, el endpoint no expone verbos de actualización ni borrado lógico.


![Módulo de Reporte de Incidencias](/docs/Módulo%20de%20Reporte%20de%20Incidencias.png)

---
### Endpoints del proyecto
**Descripción:** Endpoints desarrollados para este proyecto, se muestra a traves de la interfaz swagger  

![Endpoints](/docs/endpoints.png)
