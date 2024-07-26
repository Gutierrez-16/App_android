# MyApplication

## Descripción

Esta es una aplicación Android que implementa operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para la gestión de usuarios y cursos utilizando SQLite como base de datos local.

## Características

- Registro e inicio de sesión de usuarios.
- Gestión de usuarios: crear, leer, actualizar y eliminar usuarios.
- Gestión de cursos: crear, leer, actualizar y eliminar cursos.
- Relación entre usuarios y cursos: un usuario puede crear varios cursos, y solo el usuario que creó el curso puede actualizarlo o eliminarlo.


## Configuración de la Base de Datos

El archivo **DatabaseHelper.java** contiene la configuración y las operaciones CRUD para las tablas `users` y `courses`.

### Tablas

- **users**:
  - `_id`: ID del usuario (clave primaria).
  - `username`: Nombre de usuario.
  - `password`: Contraseña del usuario.

- **courses**:
  - `_id`: ID del curso (clave primaria).
  - `course_name`: Nombre del curso.
  - `user_id`: ID del usuario que creó el curso (clave foránea).

### Operaciones CRUD

- **Usuarios**:
  - `addUser`: Agregar un nuevo usuario.
  - `updateUser`: Actualizar un usuario existente.
  - `deleteUser`: Eliminar un usuario.
  - `getAllUsers`: Obtener todos los usuarios.
  - `getUser`: Obtener un usuario por nombre de usuario y contraseña.

- **Cursos**:
  - `addCourse`: Agregar un nuevo curso.
  - `updateCourse`: Actualizar un curso existente.
  - `deleteCourse`: Eliminar un curso.
  - `getAllCourses`: Obtener todos los cursos de un usuario específico.
  - `isCourseCreatedByUser`: Verificar si un curso fue creado por un usuario específico.

## Cómo Ejecutar el Proyecto

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Gutierrez-16/App_android.git
