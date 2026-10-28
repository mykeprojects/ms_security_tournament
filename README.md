# MS Security Tournament

Backend REST para administrar usuarios, perfiles, roles y permisos de la
plataforma. Está desarrollado con Java 17, Spring Boot, Spring Security, JWT,
Spring Data JPA y MySQL.

## Requisitos

- JDK 17
- MySQL compatible con el driver MySQL Connector/J
- Windows: puede usarse el wrapper `mvnw.cmd` incluido en el repositorio.
  En otros sistemas, usa `./mvnw`.

## Configuración

1. Crea la base de datos en MySQL:

   ```sql
   CREATE DATABASE security_ms_db;
   ```

2. Configura la conexión a la base de datos mediante variables de entorno de
   Spring Boot:

   ```powershell
   $env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/security_ms_db"
   $env:SPRING_DATASOURCE_USERNAME="root"
   $env:SPRING_DATASOURCE_PASSWORD="<tu-clave-de-MySQL>"
   ```

   En Linux o macOS:

   ```bash
   export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/security_ms_db'
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

Ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

## Primer acceso

Al iniciar, el seeder crea si no existen:

- Usuario administrador: `admin@uc.edu.co`
- Contraseña inicial de desarrollo: `Admin123!`
- Roles `ADMIN` y `CONSULTOR`
- Permisos iniciales para el administrador y permisos de consulta para el
  consultor

Estas credenciales están destinadas únicamente al desarrollo local. Cambia la
contraseña y la estrategia de inicialización antes de desplegar el servicio en
un entorno compartido o de producción. El seeder no sobrescribe la contraseña
si el usuario ya existe.

## Autenticación

El login es el único endpoint público. Envía las credenciales como JSON:

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

Todos los endpoints usan el prefijo `http://localhost:8181`. Salvo el login,
requieren autenticación y autorización.

### Autenticación

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/api/auth/login` | Iniciar sesión y obtener JWT |

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

Para registrar un usuario:

```http
POST /api/users
Content-Type: application/json
Authorization: Bearer <accessToken>
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

Las sesiones también están protegidas por permisos dinámicos; el seeder no
crea permisos para estas rutas.

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
