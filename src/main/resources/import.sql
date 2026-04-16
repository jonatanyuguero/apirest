INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-04-20 10:00:00', 1, 'Implementar GET y POST', 'Desarrollar los endpoints básicos para listar y crear tareas');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-04-22 12:00:00', 2, 'Añadir PUT y DELETE', 'Completar el CRUD permitiendo editar y borrar registros');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-04-25 18:30:00', 3, 'Configurar Seguridad', 'Implementar autenticación y protección de rutas');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-04-28 09:00:00', 4, 'Gestión de Errores', 'Configurar respuestas personalizadas para excepciones y errores 404');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-01 17:00:00', 5, 'Configuración CORS', 'Permitir peticiones desde diferentes dominios');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-05 20:00:00', 6, 'Documentación API', 'Generar la documentación técnica del proyecto');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-10 11:30:00', 7, 'Subir a GitHub', 'Cargar el enlace del proyecto finalizado en el repositorio');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-15 10:00:00', 8, 'Pruebas de integración', 'Verificar que todos los endpoints (GET, POST, PUT, DELETE) funcionan correctamente en conjunto');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-20 12:00:00', 9, 'Refactorización de código', 'Limpiar el código, eliminar imports no utilizados y mejorar la legibilidad siguiendo las buenas prácticas');
INSERT INTO task (created_at, deadline, id, title, description) VALUES (CURRENT_TIMESTAMP, '2026-05-25 15:00:00', 10, 'Redactar el README.md', 'Escribir las instrucciones paso a paso para que otros puedan ejecutar tu API desde GitHub');

INSERT INTO user_entity (id, email, username, password, is_admin) VALUES (NEXTVAL('user_entity_seq'), 'pepe@openwebinars.net', 'pepe','{noop}12345',false);
UPDATE task SET author_id = CURRVAL('user_entity_seq');