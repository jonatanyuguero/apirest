package com.jonatanyuguero.apirest.controller;

import com.jonatanyuguero.apirest.dto.EditTaskCommand;
import com.jonatanyuguero.apirest.dto.GetTaskDto;
import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.service.TaskService;
import com.jonatanyuguero.apirest.users.User;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@RestController
@RequestMapping("/task/")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService taskService;


    @Operation(
            summary = "Obtener todas las tareas del usuario",
            description = "Permite obtener todas las tareas de un usuario"
    )

    @ApiResponse(description = "Listado de tareas del usuario",
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = GetTaskDto.class)),
                    examples = {
                            @ExampleObject("""
                                {
                                    "id": 1,
                                    "title": "Comprar alimentos",
                                    "description": "Hacer una lista de compras para la semana",
                                    "createdAt": "2025-01-13T16:12:11.295172",
                                    "deadline": "2025-01-20T16:12:11.295172",
                                    "author": {
                                        "id": 1,
                                        "username": "pepe",
                                        "email": "pepe@openwebinars.net"
                                    }
                                }
                                """)
                    }
            )
    )
    @GetMapping
    public List<GetTaskDto> getAll(
            @AuthenticationPrincipal User author
    ){

        return //taskService.findAll()
                taskService.findByAuthor(author)
                .stream()
                .map(GetTaskDto::of)
                .toList();
    }




    @Operation(
            summary = "Obtener una tarea concreta",
            description = "Permite obtener los detalles de una tarea específica. Solo el propietario de la tarea puede verla."
    )
    @ApiResponse(
            description = "Detalles de la tarea solicitada",
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetTaskDto.class),
                    examples = {
                            @ExampleObject("""
                            {
                                "id": 1,
                                "title": "Comprar alimentos",
                                "description": "Hacer una lista de compras para la semana",
                                "createdAt": "2025-01-13T16:12:11.295172",
                                "deadline": "2025-01-20T16:12:11.295172",
                                "author": {
                                    "id": 1,
                                    "username": "pepe",
                                    "email": "pepe@openwebinars.net"
                                }
                            }
                            """)
                    }
            )
    )
    @PostAuthorize("""
            returnObject.author.username == authentication.principal.username
            """)
    @GetMapping("/{id}")
    public GetTaskDto getById(@PathVariable Long id){
        return GetTaskDto.of(taskService.findbyId(id));
    }



    @PostMapping
    public ResponseEntity<GetTaskDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Tarea a crear", required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EditTaskCommand.class),
                            examples = @ExampleObject("""
                                {
                                    "title": "Aprender Spring Boot",
                                    "description": "Hacer todos los cursos de Spring Boot",
                                    "deadline": "2025-12-31T23:59:59"
                                }
                                """)
                    )
            )
            @RequestBody EditTaskCommand cmd,
            @AuthenticationPrincipal User author
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GetTaskDto.of(taskService.save(cmd, author)));
    }





    @Operation(
            summary = "Editar una tarea",
            description = "Permite modificar los detalles de una tarea existente"
    )
    @ApiResponse(description = "Tarea editada correctamente",
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetTaskDto.class),
                    examples = {
                            @ExampleObject("""
                            {
                                "id": 1,
                                "title": "Aprender Spring Boot",
                                "description": "Hacer todos los cursos de Spring Boot",
                                "createdAt": "2025-01-13T16:12:11.295172",
                                "deadline": "2025-12-31T23:59:59",
                                "author": {
                                    "id": 1,
                                    "username": "pepe",
                                    "email": "pepe@openwebinars.net"
                                }
                            }
                            """)
                    }
            )
    )
    @PreAuthorize("""
            @ownerCheck.check(#id, authentication.principal.getId())
            """)
    @PutMapping("/{id}")
    public GetTaskDto edit(@RequestBody EditTaskCommand cmd,
                     @PathVariable Long id){
        return GetTaskDto.of(taskService.edit(cmd,id));
    }



    @Operation(
            summary = "Eliminar una tarea",
            description = "Permite eliminar una tarea asociada al usuario autenticado si se proporciona su ID"
    )
    @ApiResponse(description = "Respuesta correcta de tarea eliminada",
            responseCode = "204",
            content = @Content(schema = @Schema(implementation = Void.class)))

    @PreAuthorize("""
            @ownerCheck.check(#id, authentication.principal.getId())
            """)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        taskService.delete(id);
        return  ResponseEntity.noContent().build();
    }


}
