package com.jonatanyuguero.apirest.controller;

import com.jonatanyuguero.apirest.dto.DashboardDto;
import com.jonatanyuguero.apirest.dto.EditTaskCommand;
import com.jonatanyuguero.apirest.dto.GetTaskDto;
import com.jonatanyuguero.apirest.model.Priority;
import com.jonatanyuguero.apirest.service.TaskService;
import com.jonatanyuguero.apirest.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService taskService;

    // === CRUD ===

    @Operation(summary = "Obtener todas mis tareas",
            description = "Devuelve todas las tareas del usuario autenticado.")
    @ApiResponse(description = "Listado de tareas del usuario", responseCode = "200",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = GetTaskDto.class))))
    @GetMapping
    public List<GetTaskDto> getAll(@AuthenticationPrincipal User author) {
        return taskService.findByAuthor(author).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Obtener tarea por ID",
            description = "Devuelve los detalles de una tarea. Solo accesible para el propietario.")
    @ApiResponse(responseCode = "200", description = "Detalles de la tarea")
    @PostAuthorize("returnObject.author.username == authentication.principal.username")
    @GetMapping("/{id}")
    public GetTaskDto getById(
            @Parameter(description = "ID de la tarea", example = "1")
            @PathVariable Long id) {
        return GetTaskDto.of(taskService.findbyId(id));
    }

    @Operation(summary = "Crear nueva tarea",
            description = "Crea una nueva tarea asociada al usuario autenticado.")
    @ApiResponse(responseCode = "201", description = "Tarea creada",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetTaskDto.class)))
    @PostMapping
    public ResponseEntity<GetTaskDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la tarea a crear", required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EditTaskCommand.class),
                            examples = @ExampleObject("""
                                    {
                                        "title": "Aprender Spring Boot",
                                        "description": "Hacer todos los cursos",
                                        "deadline": "2025-12-31T23:59:59",
                                        "completed": false,
                                        "priority": "HIGH",
                                        "categoryId": 1
                                    }
                                    """)))
            @RequestBody EditTaskCommand cmd,
            @AuthenticationPrincipal User author) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GetTaskDto.of(taskService.save(cmd, author)));
    }

    @Operation(summary = "Editar tarea",
            description = "Modifica una tarea existente. Solo el propietario puede hacerlo.")
    @ApiResponse(responseCode = "200", description = "Tarea editada")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @PutMapping("/{id}")
    public GetTaskDto edit(
            @RequestBody EditTaskCommand cmd,
            @Parameter(description = "ID de la tarea") @PathVariable Long id) {
        return GetTaskDto.of(taskService.edit(cmd, id));
    }

    @Operation(summary = "Eliminar tarea",
            description = "Elimina la tarea indicada. Solo accesible al propietario.")
    @ApiResponse(responseCode = "204", description = "Tarea eliminada")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la tarea") @PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === Búsquedas por cada campo ===

    @Operation(summary = "Buscar por título",
            description = "Búsqueda parcial e insensible a mayúsculas por el título de la tarea.")
    @GetMapping("/search/title")
    public List<GetTaskDto> byTitle(
            @AuthenticationPrincipal User author,
            @Parameter(description = "Texto a buscar en el título") @RequestParam String q) {
        return taskService.searchByTitle(author, q).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar por descripción",
            description = "Búsqueda parcial insensible a mayúsculas dentro del campo descripción.")
    @GetMapping("/search/description")
    public List<GetTaskDto> byDescription(
            @AuthenticationPrincipal User author,
            @Parameter(description = "Texto a buscar en la descripción") @RequestParam String q) {
        return taskService.searchByDescription(author, q).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar por estado completado",
            description = "Filtra las tareas por su valor completed (true/false).")
    @GetMapping("/search/completed")
    public List<GetTaskDto> byCompleted(
            @AuthenticationPrincipal User author,
            @Parameter(description = "true para completadas, false para pendientes")
            @RequestParam boolean value) {
        return taskService.searchByCompleted(author, value).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar por prioridad",
            description = "Filtra tareas según prioridad LOW, MEDIUM o HIGH (atributo personalizado).")
    @GetMapping("/search/priority")
    public List<GetTaskDto> byPriority(
            @AuthenticationPrincipal User author,
            @Parameter(description = "Prioridad", example = "HIGH") @RequestParam Priority value) {
        return taskService.searchByPriority(author, value).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar por categoría",
            description = "Filtra las tareas por una categoría concreta.")
    @GetMapping("/search/category/{categoryId}")
    public List<GetTaskDto> byCategory(
            @AuthenticationPrincipal User author,
            @Parameter(description = "ID de la categoría") @PathVariable Long categoryId) {
        return taskService.searchByCategory(author, categoryId).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar por deadline (atributo personalizado)",
            description = "Devuelve las tareas con deadline anterior a la fecha indicada.")
    @GetMapping("/search/deadline-before")
    public List<GetTaskDto> byDeadline(
            @AuthenticationPrincipal User author,
            @Parameter(description = "Fecha límite (ISO yyyy-MM-ddTHH:mm:ss)")
            @RequestParam LocalDateTime before) {
        return taskService.searchByDeadlineBefore(author, before).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Listar tareas vencidas",
            description = "Devuelve las tareas no completadas con deadline anterior al momento actual.")
    @GetMapping("/search/overdue")
    public List<GetTaskDto> overdue(@AuthenticationPrincipal User author) {
        return taskService.searchOverdue(author).stream().map(GetTaskDto::of).toList();
    }

    @Operation(summary = "Buscar tareas por tags",
            description = "Devuelve tareas que contengan alguno de los tags indicados.")
    @GetMapping("/search/tags")
    public List<GetTaskDto> byTags(
            @AuthenticationPrincipal User author,
            @Parameter(description = "Lista de IDs de tags") @RequestParam List<Long> ids) {
        return taskService.searchByTags(author, ids).stream().map(GetTaskDto::of).toList();
    }

    // === Asignar / quitar tags ===

    @Operation(summary = "Asignar tag a una tarea",
            description = "Asigna un tag existente a la tarea indicada. Tanto la tarea como el tag deben pertenecer al usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Tag asignado")
    @PreAuthorize("@ownerCheck.check(#taskId, authentication.principal.getId())")
    @PostMapping("/{taskId}/tags/{tagId}")
    public GetTaskDto addTag(
            @Parameter(description = "ID de la tarea") @PathVariable Long taskId,
            @Parameter(description = "ID del tag") @PathVariable Long tagId,
            @AuthenticationPrincipal User user) {
        return GetTaskDto.of(taskService.addTag(taskId, tagId, user));
    }

    @Operation(summary = "Quitar tag de una tarea",
            description = "Elimina un tag de la tarea indicada. Tanto la tarea como el tag deben pertenecer al usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Tag eliminado")
    @PreAuthorize("@ownerCheck.check(#taskId, authentication.principal.getId())")
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public GetTaskDto removeTag(
            @Parameter(description = "ID de la tarea") @PathVariable Long taskId,
            @Parameter(description = "ID del tag") @PathVariable Long tagId,
            @AuthenticationPrincipal User user) {
        return GetTaskDto.of(taskService.removeTag(taskId, tagId, user));
    }

    // === Dashboard ===

    @Operation(summary = "Dashboard del usuario",
            description = "Devuelve estadísticas: totales, completadas, pendientes, vencidas, agrupaciones por categoría, tag y prioridad.")
    @ApiResponse(responseCode = "200", description = "Estadísticas calculadas")
    @GetMapping("/dashboard")
    public DashboardDto dashboard(@AuthenticationPrincipal User author) {
        return taskService.dashboard(author);
    }
}
