package com.jonatanyuguero.apirest.service;


import com.jonatanyuguero.apirest.dto.DashboardDto;
import com.jonatanyuguero.apirest.dto.EditTaskCommand;
import com.jonatanyuguero.apirest.error.CategoryNotFoundException;
import com.jonatanyuguero.apirest.error.TagNotFoundException;
import com.jonatanyuguero.apirest.error.TaskNotFoundException;
import com.jonatanyuguero.apirest.model.Category;
import com.jonatanyuguero.apirest.model.Priority;
import com.jonatanyuguero.apirest.model.Tag;
import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.repos.CategoryRepository;
import com.jonatanyuguero.apirest.repos.TagRepository;
import com.jonatanyuguero.apirest.repos.TaskRepository;
import com.jonatanyuguero.apirest.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    // === Operaciones básicas ===
    public List<Task> findAll() {
        List<Task> result = taskRepository.findAll();
        if (result.isEmpty()) throw new TaskNotFoundException();
        return result;
    }

    public List<Task> findByAuthor(User author) {
        List<Task> result = taskRepository.findByAuthor(author);
        if (result.isEmpty()) throw new TaskNotFoundException();
        return result;
    }

    public Task findbyId(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task save(EditTaskCommand cmd, User author) {
        Category category = null;
        if (cmd.categoryId() != null) {
            category = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(cmd.categoryId()));
        }
        return taskRepository.save(
                Task.builder()
                        .title(cmd.title())
                        .description(cmd.description())
                        .deadline(cmd.deadline())
                        .completed(cmd.completed() != null && cmd.completed())
                        .priority(cmd.priority() == null ? Priority.MEDIUM : cmd.priority())
                        .category(category)
                        .author(author)
                        .build()
        );
    }

    public Task edit(EditTaskCommand cmd, Long id) {
        return taskRepository.findById(id)
                .map(t -> {
                    if (cmd.title() != null) t.setTitle(cmd.title());
                    if (cmd.description() != null) t.setDescription(cmd.description());
                    if (cmd.deadline() != null) t.setDeadline(cmd.deadline());
                    if (cmd.completed() != null) t.setCompleted(cmd.completed());
                    if (cmd.priority() != null) t.setPriority(cmd.priority());
                    if (cmd.categoryId() != null) {
                        Category cat = categoryRepository.findById(cmd.categoryId())
                                .orElseThrow(() -> new CategoryNotFoundException(cmd.categoryId()));
                        t.setCategory(cat);
                    }
                    return taskRepository.save(t);
                })
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) throw new TaskNotFoundException(id);
        taskRepository.deleteById(id);
    }

    // === Búsquedas por cada campo ===
    public List<Task> searchByTitle(User author, String title) {
        return taskRepository.findByAuthorAndTitleContainingIgnoreCase(author, title);
    }

    public List<Task> searchByDescription(User author, String description) {
        return taskRepository.findByAuthorAndDescriptionContainingIgnoreCase(author, description);
    }

    public List<Task> searchByCompleted(User author, boolean completed) {
        return taskRepository.findByAuthorAndCompleted(author, completed);
    }

    public List<Task> searchByPriority(User author, Priority priority) {
        return taskRepository.findByAuthorAndPriority(author, priority);
    }

    public List<Task> searchByCategory(User author, Long categoryId) {
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        return taskRepository.findByAuthorAndCategory(author, cat);
    }

    public List<Task> searchByDeadlineBefore(User author, LocalDateTime before) {
        return taskRepository.findByAuthorAndDeadlineBefore(author, before);
    }

    public List<Task> searchOverdue(User author) {
        return taskRepository.findOverdueByAuthor(author, LocalDateTime.now());
    }

    public List<Task> searchByTags(User author, List<Long> tagIds) {
        List<Tag> tags = tagIds.stream()
                .map(id -> tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id)))
                .toList();
        return taskRepository.findByAuthorAndTagsIn(author, tags);
    }

    // === Asignar / quitar tags (verifica que el tag pertenece al usuario) ===
    public Task addTag(Long taskId, Long tagId, User author) {
        Task t = findbyId(taskId);
        Tag tag = tagRepository.findByIdAndAuthor(tagId, author)
                .orElseThrow(() -> new TagNotFoundException(tagId));
        if (!t.getTags().contains(tag)) t.getTags().add(tag);
        return taskRepository.save(t);
    }

    public Task removeTag(Long taskId, Long tagId, User author) {
        Task t = findbyId(taskId);
        Tag tag = tagRepository.findByIdAndAuthor(tagId, author)
                .orElseThrow(() -> new TagNotFoundException(tagId));
        t.getTags().remove(tag);
        return taskRepository.save(t);
    }

    // === Dashboard ===
    public DashboardDto dashboard(User author) {
        List<Task> all = taskRepository.findByAuthor(author);

        long total = all.size();
        long completed = all.stream().filter(Task::isCompleted).count();
        long pending = total - completed;
        long overdue = all.stream()
                .filter(t -> !t.isCompleted()
                        && t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now()))
                .count();

        Map<String, Long> byCategory = all.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getTitle(), Collectors.counting()));

        Map<String, Long> byTag = new HashMap<>();
        for (Task t : all) {
            for (Tag tag : t.getTags()) {
                byTag.merge(tag.getName(), 1L, Long::sum);
            }
        }

        Map<String, Long> byPriority = all.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        return new DashboardDto(total, completed, pending, overdue, byCategory, byTag, byPriority);
    }
}
