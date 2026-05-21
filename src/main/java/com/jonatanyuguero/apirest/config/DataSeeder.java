package com.jonatanyuguero.apirest.config;

/**
 * DataSeeder deshabilitado.
 *
 * Los datos iniciales (usuarios admin/gestor/pepe/ana, categorías, tags y tareas)
 * se cargan desde src/main/resources/import.sql, que Hibernate ejecuta automáticamente
 * tras crear el esquema cuando ddl-auto está en 'create' o 'create-drop', y manualmente
 * (mediante spring.jpa.defer-datasource-initialization=true junto a sql.init) en otros casos.
 *
 * La anotación @Component se ha retirado para que Spring no instancie esta clase
 * ni ejecute ningún CommandLineRunner. Se conserva el archivo como referencia.
 */
public class DataSeeder {
}
