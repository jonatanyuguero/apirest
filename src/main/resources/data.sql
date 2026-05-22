INSERT IGNORE INTO user_entity (id, email, username, password, fullname, role) VALUES (1, 'admin@todolist.com', 'admin', '{noop}admin', 'Administrador', 'ADMIN');
INSERT IGNORE INTO user_entity (id, email, username, password, fullname, role) VALUES (2, 'gestor@todolist.com', 'gestor', '{noop}gestor', 'Gestor Principal', 'GESTOR');
INSERT IGNORE INTO user_entity (id, email, username, password, fullname, role) VALUES (3, 'pepe@todolist.com', 'pepe', '{noop}12345', 'Pepe Garcia', 'USER');
INSERT IGNORE INTO user_entity (id, email, username, password, fullname, role) VALUES (4, 'ana@todolist.com', 'ana', '{noop}12345', 'Ana Lopez', 'USER');

INSERT IGNORE INTO category (id, title) VALUES (1, 'Trabajo');
INSERT IGNORE INTO category (id, title) VALUES (2, 'Personal');
INSERT IGNORE INTO category (id, title) VALUES (3, 'Estudio');
INSERT IGNORE INTO category (id, title) VALUES (4, 'Salud');

INSERT IGNORE INTO tag (id, name, author_id) VALUES (1, 'urgente', 3);
INSERT IGNORE INTO tag (id, name, author_id) VALUES (2, 'spring', 3);
INSERT IGNORE INTO tag (id, name, author_id) VALUES (3, 'backend', 3);

INSERT IGNORE INTO task (id, created_at, updated_at, deadline, title, description, completed, priority, author_id, category_id) VALUES (1, NOW(), NOW(), '2026-06-01 10:00:00', 'Implementar GET y POST', 'Desarrollar los endpoints basicos', false, 'HIGH', 3, 3);
INSERT IGNORE INTO task (id, created_at, updated_at, deadline, title, description, completed, priority, author_id, category_id) VALUES (2, NOW(), NOW(), '2026-06-05 12:00:00', 'Anadir PUT y DELETE', 'Completar el CRUD', false, 'HIGH', 3, 3);
INSERT IGNORE INTO task (id, created_at, updated_at, deadline, title, description, completed, priority, author_id, category_id) VALUES (3, NOW(), NOW(), '2026-05-25 18:30:00', 'Configurar Seguridad', 'Implementar autenticacion', true, 'MEDIUM', 3, 1);
INSERT IGNORE INTO task (id, created_at, updated_at, deadline, title, description, completed, priority, author_id, category_id) VALUES (4, NOW(), NOW(), '2026-05-20 09:00:00', 'Revision medica', 'Cita anual con el medico', false, 'LOW', 3, 4);
INSERT IGNORE INTO task (id, created_at, updated_at, deadline, title, description, completed, priority, author_id, category_id) VALUES (5, NOW(), NOW(), '2026-06-10 17:00:00', 'Documentacion API', 'Generar la documentacion tecnica', false, 'MEDIUM', 3, 3);

INSERT IGNORE INTO task_tag (task_id, tag_id) VALUES (1, 2);
INSERT IGNORE INTO task_tag (task_id, tag_id) VALUES (1, 3);
INSERT IGNORE INTO task_tag (task_id, tag_id) VALUES (2, 1);
INSERT IGNORE INTO task_tag (task_id, tag_id) VALUES (5, 2);
