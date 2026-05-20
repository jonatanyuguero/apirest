package com.jonatanyuguero.apirest.controller;

import com.jonatanyuguero.apirest.dto.CategoryCommand;
import com.jonatanyuguero.apirest.dto.CategoryDto;
import com.jonatanyuguero.apirest.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Listar categorías disponibles",
            description = "Devuelve todas las categorías. Disponible para cualquier usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de categorías")
    @GetMapping
    public List<CategoryDto> findAll() {
        return categoryService.findAll().stream().map(CategoryDto::of).toList();
    }

    @Operation(summary = "Obtener categoría por ID",
            description = "Devuelve los detalles de una categoría a partir de su identificador.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @GetMapping("/{id}")
    public CategoryDto findById(
            @Parameter(description = "Identificador de la categoría", example = "1")
            @PathVariable Long id) {
        return CategoryDto.of(categoryService.findById(id));
    }

    @Operation(summary = "Crear categoría",
            description = "Permite crear una nueva categoría. Solo accesible para roles ADMIN o GESTOR.")
    @ApiResponse(responseCode = "201", description = "Categoría creada correctamente")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryDto.of(categoryService.save(cmd)));
    }

    @Operation(summary = "Editar categoría",
            description = "Modifica el título de una categoría existente. Solo ADMIN o GESTOR.")
    @ApiResponse(responseCode = "200", description = "Categoría editada")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping("/{id}")
    public CategoryDto edit(
            @Parameter(description = "ID de la categoría a editar") @PathVariable Long id,
            @RequestBody CategoryCommand cmd) {
        return CategoryDto.of(categoryService.edit(id, cmd));
    }

    @Operation(summary = "Eliminar categoría",
            description = "Elimina una categoría existente. Solo ADMIN o GESTOR.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la categoría a eliminar") @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
