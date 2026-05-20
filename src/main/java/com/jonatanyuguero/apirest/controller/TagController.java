package com.jonatanyuguero.apirest.controller;

import com.jonatanyuguero.apirest.dto.TagCommand;
import com.jonatanyuguero.apirest.dto.TagDto;
import com.jonatanyuguero.apirest.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Listar tags",
            description = "Devuelve todos los tags disponibles para el usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de tags")
    @GetMapping
    public List<TagDto> findAll() {
        return tagService.findAll().stream().map(TagDto::of).toList();
    }

    @Operation(summary = "Obtener tag por ID",
            description = "Devuelve los detalles de un tag concreto.")
    @ApiResponse(responseCode = "200", description = "Tag encontrado")
    @GetMapping("/{id}")
    public TagDto findById(
            @Parameter(description = "ID del tag", example = "1")
            @PathVariable Long id) {
        return TagDto.of(tagService.findById(id));
    }

    @Operation(summary = "Crear tag",
            description = "Crea un nuevo tag. Accesible para cualquier usuario autenticado.")
    @ApiResponse(responseCode = "201", description = "Tag creado correctamente")
    @PostMapping
    public ResponseEntity<TagDto> create(@RequestBody TagCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TagDto.of(tagService.save(cmd)));
    }

    @Operation(summary = "Editar tag",
            description = "Modifica el nombre de un tag existente.")
    @ApiResponse(responseCode = "200", description = "Tag editado")
    @PutMapping("/{id}")
    public TagDto edit(
            @Parameter(description = "ID del tag") @PathVariable Long id,
            @RequestBody TagCommand cmd) {
        return TagDto.of(tagService.edit(id, cmd));
    }

    @Operation(summary = "Eliminar tag",
            description = "Elimina un tag por su ID.")
    @ApiResponse(responseCode = "204", description = "Tag eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del tag") @PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
