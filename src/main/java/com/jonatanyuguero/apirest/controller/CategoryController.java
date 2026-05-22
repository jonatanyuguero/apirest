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

    @Operation(summary = "Listar categorias disponibles",
            description = "Devuelve todas las categorias. Disponible para cualquier usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de categorias")
    @GetMapping
    public List<CategoryDto> findAll() {
        return categoryService.findAll().stream().map(CategoryDto::of).toList();
    }

    @Operation(summary = "Obtener categoria por ID",
            description = "Devuelve los detalles de una categoria a partir de su identificador.")
    @ApiResponse(responseCode = "200", description = "Categoria encontrada")
    @GetMapping("/{id}")
    public CategoryDto findById(
            @Parameter(description = "Identificador de la categoria", example = "1")
            @PathVariable Long id) {
        return CategoryDto.of(categoryService.findById(id));
    }

    @Operation(summary = "Crear categoria",
            description = "Permite crear una nueva categoria. Solo ADMIN o GESTOR.")
    @ApiResponse(responseCode = "201", description = "Categoria creada correctamente")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryDto.of(categoryService.save(cmd)));
    }

    @Operation(summary = "Editar categoria",
            description = "Modifica el titulo de una categoria existente. Solo ADMIN o GESTOR.")
    @ApiResponse(responseCode = "200", description = "Categoria editada")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping("/{id}")
    public CategoryDto edit(
            @Parameter(description = "ID de la categoria a editar") @PathVariable Long id,
            @RequestBody CategoryCommand cmd) {
        return CategoryDto.of(categoryService.edit(id, cmd));
    }

    @Operation(summary = "Eliminar categoria",
            description = "Elimina una categoria existente. Solo ADMIN o GESTOR.")
    @ApiResponse(responseCode = "204", description = "Categoria eliminada")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la categoria a eliminar") @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}