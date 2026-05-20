package com.jonatanyuguero.apirest.users;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // === Endpoints públicos ===

    @Operation(summary = "Registrar nuevo usuario",
            description = "Crea un usuario con rol USER. Endpoint público (no requiere autenticación).")
    @ApiResponse(responseCode = "201", description = "Usuario creado correctamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = NewUserResponse.class),
                    examples = @ExampleObject("""
                            {
                                "id": 1,
                                "username": "pepe",
                                "email": "pepe@example.com",
                                "fullname": "Pepe García",
                                "role": "USER"
                            }
                            """)))
    @PostMapping("/auth/register")
    public ResponseEntity<NewUserResponse> createUser(@RequestBody NewUserCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NewUserResponse.of(userService.register(cmd)));
    }

    @Operation(summary = "Login",
            description = "Endpoint para autenticarse mediante HTTP Basic. Si las credenciales son correctas devuelve los datos del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/auth/login")
    public NewUserResponse login(@AuthenticationPrincipal User user) {
        return NewUserResponse.of(user);
    }

    // === Endpoints del propio usuario ===

    @Operation(summary = "Obtener mi perfil",
            description = "Devuelve los datos del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario actual")
    @SecurityRequirement(name = "basicAuth")
    @GetMapping("/users/me")
    public NewUserResponse me(@AuthenticationPrincipal User user) {
        return NewUserResponse.of(user);
    }

    @Operation(summary = "Modificar mi perfil",
            description = "Permite al usuario autenticado modificar su email, fullname o password.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @SecurityRequirement(name = "basicAuth")
    @PutMapping("/users/me")
    public NewUserResponse editProfile(@AuthenticationPrincipal User user,
                                       @RequestBody EditProfileCommand cmd) {
        return NewUserResponse.of(userService.editProfile(user, cmd));
    }

    // === Endpoints de ADMIN ===

    @Operation(summary = "Listar todos los usuarios (ADMIN)",
            description = "Devuelve el listado completo de usuarios del sistema. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<NewUserResponse> findAll() {
        return userService.findAll().stream().map(NewUserResponse::of).toList();
    }

    @Operation(summary = "Obtener usuario por ID (ADMIN)",
            description = "Devuelve los detalles de un usuario por su ID. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public NewUserResponse findById(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        return NewUserResponse.of(userService.findById(id));
    }

    @Operation(summary = "Actualizar usuario (ADMIN)",
            description = "Permite al ADMIN modificar los datos de un usuario.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public NewUserResponse adminUpdate(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody NewUserCommand cmd) {
        return NewUserResponse.of(userService.adminUpdateUser(id, cmd));
    }

    @Operation(summary = "Eliminar usuario (ADMIN)",
            description = "Elimina un usuario por ID. Solo ADMIN.")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Promocionar usuario a GESTOR (ADMIN)",
            description = "Cambia el rol de un usuario a GESTOR. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario promocionado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/promote")
    public NewUserResponse promote(
            @Parameter(description = "ID del usuario a promocionar") @PathVariable Long id) {
        return NewUserResponse.of(userService.promoteToGestor(id));
    }

    @Operation(summary = "Degradar GESTOR a USER (ADMIN)",
            description = "Devuelve el rol de un GESTOR a USER. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Usuario degradado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/demote")
    public NewUserResponse demote(
            @Parameter(description = "ID del usuario a degradar") @PathVariable Long id) {
        return NewUserResponse.of(userService.demoteToUser(id));
    }
}
