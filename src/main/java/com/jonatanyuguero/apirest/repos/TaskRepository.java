package com.jonatanyuguero.apirest.repos;

import com.jonatanyuguero.apirest.model.Category;
import com.jonatanyuguero.apirest.model.Priority;
import com.jonatanyuguero.apirest.model.Tag;
import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAuthor(User author);

    List<Task> findByAuthorAndTitleContainingIgnoreCase(User author, String title);

    List<Task> findByAuthorAndDescriptionContainingIgnoreCase(User author, String description);

    List<Task> findByAuthorAndCompleted(User author, boolean completed);

    List<Task> findByAuthorAndCategory(User author, Category category);

    List<Task> findByAuthorAndPriority(User author, Priority priority);

    // Búsqueda por el atributo personalizado deadline (tareas con fecha límite anterior a la dada)
    List<Task> findByAuthorAndDeadlineBefore(User author, LocalDateTime deadline);

    // Tareas vencidas: deadline ya pasado y no completadas
    @Query("SELECT t FROM Task t WHERE t.author = :author AND t.deadline < :now AND t.completed = false")
    List<Task> findOverdueByAuthor(@Param("author") User author, @Param("now") LocalDateTime now);

    // Búsqueda por tags seleccionados
    @Query("SELECT DISTINCT t FROM Task t JOIN t.tags tg WHERE t.author = :author AND tg IN :tags")
    List<Task> findByAuthorAndTagsIn(@Param("author") User author, @Param("tags") List<Tag> tags);

    // Para los conteos del dashboard
    long countByAuthor(User author);

    long countByAuthorAndCompleted(User author, boolean completed);

    long countByAuthorAndCategory(User author, Category category);
}
