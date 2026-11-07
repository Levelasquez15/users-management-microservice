# Users Management — Microservicio Spring Boot, Arquitectura Hexagonal y DDD

Microservicio backend desarrollado con **Java 17+** y **Spring Boot 3.3.5** para la gestión de usuarios, autenticación y control de acceso basado en roles. Implementa **Arquitectura Hexagonal (Puertos y Adaptadores)**, principios de **Domain-Driven Design (DDD)**, persistencia relacional en **MySQL**, seguridad mediante **Tokens JWT** y notificaciones transaccionales vía **SMTP (JavaMail)**.

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue una estricta separación de responsabilidades en tres capas desacopladas del framework:

```text
src/main/java/com/jcaa/usersmanagement/
├── domain/                      # NÚCLEO: Entidades, Value Objects y Reglas de Negocio puras
│   ├── model/                   # Modelos de dominio (UserModel, EmailDestinationModel)
│   ├── valueobject/             # Objetos de valor inmutables (UserId, UserEmail, UserPassword, UserRole, UserStatus)
│   └── exception/               # Excepciones de dominio
│
├── application/                 # CASOS DE USO: Orquestación de flujos de negocio
│   ├── port/
│   │   ├── in/                  # Puertos de Entrada (RegisterUserUseCase, AuthenticateUserUseCase, etc.)
│   │   └── out/                 # Puertos de Salida (UserRepositoryPort, EmailSenderPort, TokenGeneratorPort)
│   └── service/                 # Implementación de casos de uso y notificaciones asíncronas
│
└── infrastructure/              # ADAPTADORES: Frameworks, integraciones y tecnologías externas
    ├── adapter/
    │   ├── in/rest/             # Controladores REST y DTOs de transporte
    │   ├── out/persistence/     # Adaptador de persistencia JDBC hacia MySQL
    │   └── email/               # Adaptador de correo JavaMail para servidor SMTP
    ├── config/                  # Configuraciones Spring (DataSource, JavaMail, OpenAPI)
    └── security/                # Spring Security, filtros JWT y criptografía BCrypt
```

---

## 📋 Prerrequisitos

- **Java JDK:** Versión 17 o superior (compatible con OpenJDK 17 a 23).
- **Apache Maven:** 3.9+ (incluido a través del wrapper `./mvnw` y `mvnw.cmd`).
- **Base de Datos:** MySQL Server 8.0 o superior / MySQL Server 26.x.
- **Git:** Para control de versiones.

---

## ⚙️ Configuración y Puesta en Marcha Local

### 1. Clonar el Repositorio
```bash
git clone https://github.com/Levelasquez15/users-management-microservice.git
cd users-management-microservice
```

### 2. Configurar Variables de Entorno (`.env`)
Copie la plantilla de variables de entorno y ajuste las credenciales según su entorno local:
```bash
cp .env.example .env
```

Contenido del archivo `.env`:
```properties
# Base de datos MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=crud_usuarios
DB_USERNAME=root
DB_PASSWORD=su_contraseña_mysql
DB_SSL_MODE=DISABLED

# Servidor de Correo SMTP (Gmail / Brevo)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=su_cuenta@gmail.com
SMTP_PASSWORD=su_clave_de_aplicacion
SMTP_FROM_ADDRESS=su_cuenta@gmail.com
SMTP_FROM_NAME=Gestion de Usuarios
```

### 3. Inicializar la Base de Datos
Ejecute el script de estructura inicial ubicado en `src/main/resources/schema.sql` en su servidor MySQL:
```bash
mysql -u root -p < src/main/resources/schema.sql
```
Este script crea:
- La base de datos `crud_usuarios`.
- La tabla relacional `users`.
- El usuario Administrador inicial por defecto (`admin@example.com` / contraseña: `Admin1234!`).

### 4. Compilación y Ejecución
Compile el proyecto y ejecute los tests unitarios con Maven Wrapper:
```bash
# En Windows:
mvnw.cmd clean install -U
mvnw.cmd spring-boot:run

# En Linux/macOS:
./mvnw clean install -U
./mvnw spring-boot:run
```
El microservicio quedará escuchando en el puerto por defecto: `http://localhost:8080`.

---

## 📖 Documentación y Swagger UI

La API expone su documentación interactiva OpenAPI 3.0 accesible desde el navegador:
👉 **`http://localhost:8080/swagger-ui/index.html`**

---

## 📡 Catálogo de Endpoints REST

| Método | Ruta | Seguridad | Descripción | Códigos HTTP |
| :---: | :--- | :---: | :--- | :---: |
| **GET** | `/health` | Público | Verificación del estado del microservicio y metadatos de build. | `200` |
| **POST** | `/api/auth/login` | Público | Autenticación de usuario activo; retorna token Bearer JWT (15 min vigencia). | `200`, `401` |
| **POST** | `/api/users` | Público | Registro de usuario. Asigna estado inicial `PENDING` y envía correo de bienvenida. | `201`, `400`, `409` |
| **GET** | `/api/users` | Token JWT (`ADMIN`, `REVIEWER`) | Listado alfabético completo de usuarios. | `200`, `401`, `403` |
| **GET** | `/api/users/{id}` | Token JWT (`ADMIN`, `REVIEWER`) | Obtener detalle de usuario por identificador único. | `200`, `401`, `404` |
| **PUT** | `/api/users/{id}` | Token JWT (`ADMIN`) | Actualizar usuario (ej. activar cuenta a estado `ACTIVE`). Envía notificación. | `200`, `400`, `401`, `404` |
| **DELETE**| `/api/users/{id}` | Token JWT (`ADMIN`) | Eliminación permanente de un usuario por su ID. | `204`, `401`, `404` |

---

## 🔒 Reglas de Negocio y Seguridad

1. **Ciclo de Estados del Usuario:**
   - Todo usuario creado ingresa con estado inicial **`PENDING`**.
   - Los usuarios con estado `PENDING` **no pueden autenticarse**; el endpoint `/api/auth/login` responde con `401 Unauthorized` (*"Tu cuenta no está activa. contacta al administrador."*).
   - Un usuario con rol `ADMIN` debe invocar `PUT /api/users/{id}` para actualizar el estado a **`ACTIVE`**.
2. **Notificaciones Transaccionales Asíncronas:**
   - **Registro:** Se despacha un correo HTML de bienvenida con credenciales temporales.
   - **Activación:** Se envía un correo automático confirmando el cambio a estado activo.
3. **Control de Acceso basado en Roles (RBAC):**
   - Rutas de consulta (`GET /api/users/**`): roles `ADMIN` y `REVIEWER`.
   - Rutas de mutación (`PUT`, `DELETE`): exclusivo rol `ADMIN`.
   - Autenticación mediante cabecera estándar HTTP:
     ```http
     Authorization: Bearer <accessToken>
     ```

---

## 🧪 Pruebas con Cliente HTTP (Boomerang / Postman)

El repositorio incluye la colección completa lista para importar en **Postman**, **Insomnia** o la extensión **Boomerang**:
📁 **`collections/Taller de MicroServicios Spring Boot Hexagonal.json`**

La colección contiene las peticiones preconfiguradas para probar el flujo completo:
1. `POST /api/auth/login (Admin)`
2. `POST /api/users (Crear Usuario)`
3. `GET /api/users (Listar Usuarios)`
4. `POST /api/auth/login (Validar Bloqueo Usuario Inactivo)`
5. `PUT /api/users/{id} (Activar Usuario a ACTIVE)`
6. `GET /api/users/{id} (Consulta por ID)`
7. `DELETE /api/users/{id} (Eliminación por ID)`
