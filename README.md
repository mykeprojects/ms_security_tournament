# MS Security Tournament

Backend REST para administrar usuarios, perfiles, roles y permisos de la
plataforma. Está desarrollado con Java 25, Spring Boot, Spring Security, JWT,
Spring Data JPA y MySQL.

## Requisitos

- JDK 25 (LTS)
- MySQL compatible con el driver MySQL Connector/J
- Windows: puede usarse el wrapper `mvnw.cmd` incluido en el repositorio.
  En otros sistemas, usa `./mvnw`.

## Configuración

1. Crea la base de datos en MySQL:

   ```sql
   CREATE DATABASE sports_system_db;
   ```

2. Configura la conexión a la base de datos mediante variables de entorno de
   Spring Boot:

   ```powershell
   $env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/sports_system_db"
   $env:SPRING_DATASOURCE_USERNAME="root"
   $env:SPRING_DATASOURCE_PASSWORD="<tu-clave-de-MySQL>"
   ```

   En Linux o macOS:

   ```bash
   export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/sports_system_db'
   export SPRING_DATASOURCE_USERNAME='root'
   export SPRING_DATASOURCE_PASSWORD='<tu-clave-de-MySQL>'
   ```

3. Define una clave JWT de al menos 32 bytes. No uses la clave de desarrollo
   predeterminada en entornos compartidos o de producción:

   ```powershell
   $env:JWT_SECRET="<clave-segura-de-al-menos-32-bytes>"
   ```

4. Si el frontend no está en `localhost` o `127.0.0.1`, configura los orígenes
   que podrá usar:

   ```powershell
   $env:CORS_ALLOWED_ORIGIN_PATTERNS="http://localhost:[*],https://frontend.ejemplo.com"
   ```

   El valor predeterminado permite frontends locales en cualquier puerto.
   CORS permite los métodos `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS`, y las
   cabeceras `Authorization` y `Content-Type`.

5. Para validar los inicios de sesión con Firebase, configura el ID del
   proyecto usado también por el frontend:

   ```powershell
   $env:FIREBASE_PROJECT_ID="sports-system-uc-2026"
   ```

   El `FIREBASE_PROJECT_ID` debe coincidir con `VITE_FIREBASE_PROJECT_ID` del
   frontend. El archivo local `application.properties` usa este mismo valor
   como predeterminado; define la variable de entorno para reemplazarlo en
   otros entornos. El backend valida la firma y el proyecto del ID token con
   las claves públicas de Firebase, por lo que no necesita una cuenta de
   servicio.

La aplicación escucha en `http://localhost:8181`. Hibernate actualiza el
esquema automáticamente (`spring.jpa.hibernate.ddl-auto=update`); para
producción, se recomienda administrar los cambios de esquema mediante
migraciones.

## Ejecutar y probar

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

### IntelliJ IDEA

Abre el proyecto importando `pom.xml` como proyecto Maven y verifica:

1. En **File > Project Structure > Project**, selecciona JDK 25 como **Project
   SDK** y **Language level**.
2. En **Settings > Build, Execution, Deployment > Build Tools > Maven >
   Importing**, selecciona JDK 25 como **JDK for importer**.
3. En la ventana **Maven**, pulsa **Reload All Maven Projects** para sincronizar
   el nivel de compilación definido en `pom.xml`.

El proyecto configura el bytecode de Java 25 mediante Maven. Si IntelliJ aún
indica que el módulo está configurado para JVM target 5, revisa **File >
Project Structure > Modules > Sources** y establece **Language level** en 25;
después recarga el proyecto Maven.

Ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

## Primer acceso

Al iniciar, el seeder crea si no existen:

- Usuario administrador: `admin@uc.edu.co`
- Contraseña inicial de desarrollo: `Admin123!`
<<<<<<< HEAD
- Roles `ADMIN`, `CONSULTOR` y `AFICIONADO`
- Permisos iniciales para el administrador, permisos de consulta para el
  consultor y permisos mínimos de operación para el aficionado
=======
- Roles `ADMIN`, `CONSULTOR`, `ADMINISTRADOR DEPORTIVO` y `FAN`
- Permisos de las rutas existentes de ambos backends, con `ADMIN` asociado a
  todos los permisos
- Permisos de consulta de usuarios para `CONSULTOR`, permisos de gestión
  deportiva para `ADMINISTRADOR DEPORTIVO` y permisos de lectura/apuestas
  propias para `FAN`
>>>>>>> 25f618ded9d43fd85c4dfd0de6e357650955a440

Estas credenciales están destinadas únicamente al desarrollo local. Cambia la
contraseña y la estrategia de inicialización antes de desplegar el servicio en
un entorno compartido o de producción. El seeder no sobrescribe la contraseña
si el usuario ya existe. Los otros tres roles se crean sin usuarios de
demostración; asígnalos a cuentas registradas desde la gestión de usuarios.
Las asignaciones son idempotentes: al reiniciar, solo se agregan los roles,
permisos o relaciones que falten.

## Autenticación

El registro, el login y la validación del proveedor externo son endpoints
públicos. Para iniciar sesión, envía las
credenciales como JSON:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@uc.edu.co",
  "password": "Admin123!"
}
```

La respuesta contiene un token JWT Bearer válido durante 15 minutos:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresAt": "2026-10-28T03:00:00Z"
}
```

Envía el token en las llamadas protegidas:

```http
Authorization: Bearer <accessToken>
```

El acceso se evalúa dinámicamente a partir de los permisos asociados al usuario
por sus roles. La mayoría de las operaciones requieren un permiso registrado
para la combinación exacta de método HTTP y ruta. Por ejemplo, la ruta
`/api/users/8` se compara con el permiso `/api/users/{id}`.

Un usuario autenticado puede consultar, crear y actualizar su propio perfil
sin tener permisos adicionales. Esta excepción solo aplica a las rutas de
perfil propio `GET`, `POST` y `PUT`; no permite acceder al perfil de otro
usuario ni borrar un perfil.

## API disponible

Todos los endpoints usan el prefijo `http://localhost:8181`. Login, registro,
OAuth, verificación de segundo factor y recuperación de contraseña son públicos.
El cierre de sesión requiere autenticación; las demás rutas protegidas usan
autorización dinámica por permisos.

### Autenticación

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/auth/register` | Registrar una cuenta mediante credenciales |
| `POST` | `/api/auth/login` | Iniciar sesión y obtener JWT |
| `POST` | `/api/auth/oauth` | Validar un ID token de Firebase y obtener JWT |
| `POST` | `/api/auth/logout` | Revocar la sesión JWT actual |

El frontend conserva el JWT en una cookie de sesión y lo envía como cabecera
`Authorization: Bearer` mediante su interceptor HTTP. El backend registra cada
JWT en `sessions`; una sesión revocada o expirada deja de autorizar operaciones
protegidas.

Para Firebase, el backend valida el ID token usando el ID de proyecto
`security.firebase.project-id`, que debe coincidir con
`VITE_FIREBASE_PROJECT_ID` en el frontend. La validación obtiene las claves
públicas de Firebase; no requiere guardar credenciales de servicio. El flujo
depende de la interfaz `ExternalIdentityProvider`, por lo que otro proveedor
puede incorporarse sin cambiar la autenticación ni la autorización.
### Usuarios — HU-SE-01

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/users` | Crear usuario |
| `GET` | `/api/users` | Listar usuarios |
| `GET` | `/api/users/{id}` | Consultar usuario |
| `GET` | `/api/users/{id}/detail` | Consultar usuario y perfil |
| `GET` | `/api/users/{id}/detail-with-sessions` | Consultar usuario y sesiones |
| `GET` | `/api/users/{id}/detail-with-roles` | Consultar usuario y roles |
| `PUT` | `/api/users/{id}` | Actualizar nombre, email y opcionalmente contraseña |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |

El registro público se realiza mediante `/api/auth/register` y no requiere
token:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Ana Pérez",
  "email": "ana@example.com",
  "password": "ClaveSegura1!"
}
```

El correo debe ser válido y único. La contraseña debe tener entre 8 y 72
caracteres, al menos una mayúscula, una minúscula, un número y un carácter
especial de `@$!%*?&`, sin espacios. Se almacena con BCrypt. Las respuestas de
usuario no incluyen la contraseña. La eliminación de un usuario es física;
elimina sus asociaciones y datos dependientes, pero no borra los roles ni los
permisos del sistema.

Cada registro nuevo con credenciales, y el primer inicio de sesión mediante
OAuth, recibe automáticamente el rol `AFICIONADO`. Después de crear la cuenta,
el microservicio de seguridad solicita al sistema deportivo el registro del
aficionado usando el `id` del usuario como `externalUserId`. Si el sistema
deportivo no responde, la cuenta recién creada se elimina como compensación y el
registro responde `503`.

La integración requiere:

```properties
security.sports.base-url=${SPORTS_SYSTEM_URL:http://localhost:3000}
security.sports.internal-api-key=${INTERNAL_API_KEY:dev-internal-key}
```

El valor de `INTERNAL_API_KEY` debe coincidir con `INTERNAL_API_KEY` en el
`.env` de `ms_sport_system`. Si no se define en el entorno de
`ms_security_tournament`, se usa el valor de desarrollo `dev-internal-key`.

### Perfiles — HU-SE-02

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/users/{userId}/profile` | Crear perfil |
| `GET` | `/api/users/{userId}/profile` | Consultar perfil |
| `PUT` | `/api/users/{userId}/profile` | Actualizar perfil |
| `DELETE` | `/api/users/{userId}/profile` | Eliminar perfil (requiere permiso) |

El usuario debe existir y puede tener como máximo un perfil. Tanto la creación
como la actualización esperan:

```json
{
  "phone": "+57 300 1234567",
  "photoUrl": "https://ejemplo.com/fotos/ana.jpg",
  "birthDate": "2000-05-21"
}
```

`phone` es obligatorio (máximo 30 caracteres), `birthDate` es obligatoria y
debe ser una fecha pasada. `photoUrl` es opcional (máximo 2048 caracteres).
El backend almacena la URL de la fotografía; no recibe ni almacena el archivo
de imagen.

### Roles

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/roles` | Crear rol |
| `GET` | `/api/roles` | Listar roles |
| `GET` | `/api/roles/{id}` | Consultar rol |
| `PUT` | `/api/roles/{id}` | Actualizar rol |
| `DELETE` | `/api/roles/{id}` | Eliminar rol |

Los roles se crean con `name` y `description`.

### Asignaciones de roles a usuarios

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/user-roles` | Asignar rol a usuario |
| `GET` | `/api/user-roles` | Listar asignaciones |
| `GET` | `/api/user-roles/{id}` | Consultar asignación |
| `GET` | `/api/user-roles/users/{userId}` | Consultar roles de un usuario |
| `GET` | `/api/user-roles/roles/{roleId}` | Consultar usuarios de un rol |
| `DELETE` | `/api/user-roles/{userId}/{roleId}` | Retirar rol de usuario |

Para asignar un rol:

```json
{
  "userId": 2,
  "roleId": 1
}
```

### Permisos

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/permissions` | Crear permiso |
| `GET` | `/api/permissions` | Listar permisos |
| `GET` | `/api/permissions/{id}` | Consultar permiso |
| `PUT` | `/api/permissions/{id}` | Actualizar permiso |
| `DELETE` | `/api/permissions/{id}` | Eliminar permiso |

Un permiso contiene `url`, `method` y `model`. Para restringir una ruta con
parámetros numéricos, registra la ruta con `{id}`, por ejemplo
`/api/users/{id}/profile`.

El seeder registra las rutas de seguridad protegidas y las rutas existentes de
gestión deportiva, partidos, anotaciones, seguimiento, métodos de pago y
apuestas. `ADMIN` recibe todos los permisos presentes en la tabla. Los demás
roles reciben únicamente el conjunto descrito arriba; las operaciones públicas
de autenticación no necesitan permisos dinámicos. `POST /api/auth/logout`
requiere autenticación, pero no una fila de permiso; la validación interna de
permisos también está excluida de la autorización dinámica.

### Asignaciones de permisos a roles

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/role-permissions` | Asignar permiso a rol |
| `GET` | `/api/role-permissions` | Listar asignaciones |
| `GET` | `/api/role-permissions/{id}` | Consultar asignación |
| `DELETE` | `/api/role-permissions/{id}` | Retirar permiso de rol |

### Sesiones

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/users/{userId}/sessions` | Crear sesión |
| `GET` | `/api/users/{userId}/sessions` | Listar sesiones del usuario |
| `GET` | `/api/users/{userId}/sessions/{sessionId}` | Consultar sesión |
| `PUT` | `/api/users/{userId}/sessions/{sessionId}` | Actualizar sesión |
| `DELETE` | `/api/users/{userId}/sessions/{sessionId}` | Eliminar sesión |

Las sesiones también están protegidas por permisos dinámicos; sus permisos se
crean para `ADMIN`.

## Respuestas de error

- `400 Bad Request`: datos que no pasan validación o una operación inválida.
- `401 Unauthorized`: credenciales inválidas o token ausente/no válido.
- `403 Forbidden`: usuario autenticado sin permiso para la ruta.
- `404 Not Found`: usuario, perfil u otro recurso inexistente.
- `409 Conflict`: correo, rol, permiso o asignación duplicados/conflictivos.
- `500 Internal Server Error`: error inesperado; la respuesta incluye un
  `errorId` para localizarlo en los logs.

Los errores de negocio suelen responder con `errorCase` y `message`; los
errores de validación devuelven un objeto cuyas claves son los campos
inválidos.

## Guías del proyecto

Las guías de relaciones, manejo de excepciones, BCrypt, JWT y autorización
dinámica están en la carpeta [`guias`](./guias).
