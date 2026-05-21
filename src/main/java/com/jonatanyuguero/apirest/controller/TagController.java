package com.jonatanyuguero.apirest.controller;

import com.jonatanyuguero.apirest.dto.TagCommand;
import com.jonatanyuguero.apirest.dto.TagDto;
import com.jonatanyuguero.apirest.service.TagService;
import com.jonatanyuguero.apirest.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Listar mis tags",
            description = "Devuelve los tags creados por el usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de tags del usuario")
    @GetMapping
    public List<TagDto> findAll(@AuthenticationPrincipal User user) {
        return tagService.findAll(user).stream().map(TagDto::of).toList();
    }

    @Operation(summary = "Obtener tag por ID",
            description = "Devuelve un tag del usuario autenticado. 404 si el tag no existe o pertenece a otro usuario.")
    @ApiResponse(responseCode = "200", description = "Tag encontrado")
    @GetMapping("/{id}")
    public TagDto findById(
            @Parameter(description = "ID del tag", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return TagDto.of(tagService.findById(id, user));
    }

    @Operation(summary = "Crear tag",
            description = "Crea un tag asociado al usuario autenticado.")
    @ApiResponse(responseCode = "201", description = "Tag creado correctamente")
    @PostMapping
    public ResponseEntity<TagDto> create(
            @RequestBody TagCommand cmd,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TagDto.of(tagService.save(cmd, user)));
    }

    @Operation(summary = "Editar tag",
            description = "Modifica el nombre de un tag del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Tag editado")
    @PutMapping("/{id}")
    public TagDto edit(
            @Parameter(description = "ID del tag") @PathVariable Long id,
            @RequestBody TagCommand cmd,
            @AuthenticationPrincipal User user) {
        return TagDto.of(tagService.edit(id, cmd, user));
    }

    @Operation(summary = "Eliminar tag",
            description = "Elimina un tag del usuario autenticado.")
    @ApiResponse(responseCode = "204", description = "Tag eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del tag") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        tagService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
