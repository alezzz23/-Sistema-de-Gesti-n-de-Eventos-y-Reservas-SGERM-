package com.sgerm.eventmanagement.controller;

import com.sgerm.eventmanagement.model.User;
import com.sgerm.eventmanagement.model.Role;
import com.sgerm.eventmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * Obtiene el perfil del usuario autenticado
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile(Authentication authentication) {
        // log.info("Obteniendo perfil de usuario: {}", authentication.getName());
        
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * Actualiza el perfil del usuario autenticado
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateCurrentUserProfile(
            @Valid @RequestBody User userUpdates,
            Authentication authentication) {
        
        // log.info("Actualizando perfil de usuario: {}", authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        User updatedUser = userService.updateUser(currentUser.getId(), userUpdates);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Cambia la contraseña del usuario autenticado
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        
        // log.info("Cambiando contraseña de usuario: {}", authentication.getName());
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Contraseña actual y nueva son requeridas"));
        }
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        userService.changePassword(currentUser.getId(), currentPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Contraseña cambiada exitosamente"));
    }
    
    /**
     * Solicita restablecimiento de contraseña
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> emailData) {
        
        String email = emailData.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email es requerido"));
        }
        
        // log.info("Solicitud de restablecimiento de contraseña para email: {}", email);
        
        userService.resetPassword(email);
        
        return ResponseEntity.ok(Map.of("message", "Si el email existe, se enviará un enlace de restablecimiento"));
    }
    
    /**
     * Restablece la contraseña con token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> resetData) {
        
        String token = resetData.get("token");
        String newPassword = resetData.get("newPassword");
        
        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token y nueva contraseña son requeridos"));
        }
        
        // log.info("Restableciendo contraseña con token");
        
        // userService.resetPassword(token, newPassword); // Método no implementado para tokens
         // Por ahora, solo devolvemos un mensaje de error
         return ResponseEntity.badRequest()
                 .body(Map.of("error", "Funcionalidad de reset con token no implementada"));
    }
    
    /**
     * Verifica email con token
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestBody Map<String, String> verificationData) {
        
        String token = verificationData.get("token");
        
        if (token == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token de verificación es requerido"));
        }
        
        // log.info("Verificando email con token");
        
        userService.verifyEmail(token);
        
        return ResponseEntity.ok(Map.of("message", "Email verificado exitosamente"));
    }
    
    /**
     * Reenvía email de verificación
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            Authentication authentication) {
        
        // log.info("Reenviando email de verificación para usuario: {}", authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // if (currentUser.isEmailVerified()) {
        //     return ResponseEntity.badRequest()
        //             .body(Map.of("error", "El email ya está verificado"));
        // }
        
        // userService.sendEmailVerification(currentUser); // Método no implementado
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Funcionalidad de verificación de email no implementada"));
    }
    
    /**
     * Obtiene todos los usuarios (solo administradores)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        // log.info("Obteniendo todos los usuarios con paginación");
        
        Page<User> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Obtiene un usuario por ID (solo administradores)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        // log.info("Obteniendo usuario por ID: {}", userId);
        
        User user = userService.getUserById(userId);
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * Crea un nuevo usuario (solo administradores)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody User user,
            Authentication authentication) {
        
        // log.info("Creando nuevo usuario: {} por admin: {}", user.getUsername(), authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        User createdUser = userService.createUser(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    /**
     * Actualiza un usuario (solo administradores)
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody User userUpdates,
            Authentication authentication) {
        
        // log.info("Actualizando usuario ID: {} por admin: {}", userId, authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        User updatedUser = userService.updateUser(userId, userUpdates);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Cambia el rol de un usuario (solo administradores)
     */
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleData,
            Authentication authentication) {
        
        String newRole = roleData.get("role");
        
        if (newRole == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // log.info("Cambiando rol de usuario ID: {} a {} por admin: {}", 
        //         userId, newRole, authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // UserRole role = UserRole.valueOf(newRole.toUpperCase()); // Enum no definido
        // User updatedUser = userService.changeUserRole(userId, role, currentUser); // Método no implementado
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Funcionalidad de cambio de rol no implementada"));
    }
    
    /**
     * Activa o desactiva un usuario (solo administradores)
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long userId,
            Authentication authentication) {
        
        // log.info("Cambiando estado de usuario ID: {} por admin: {}", userId, authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // User updatedUser = userService.toggleUserStatus(userId, currentUser); // Método no implementado
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Funcionalidad de cambio de estado no implementada"));
    }
    
    /**
     * Busca usuarios por nombre de usuario
     */
    @GetMapping("/search/username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchByUsername(@RequestParam String username) {
        // log.info("Buscando usuarios por username: {}", username);
        
        // List<User> users = userService.searchByUsername(username); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Busca usuarios por email
     */
    @GetMapping("/search/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchByEmail(@RequestParam String email) {
        // log.info("Buscando usuarios por email: {}", email);
        
        // List<User> users = userService.searchByEmail(email); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Busca usuarios por nombre
     */
    @GetMapping("/search/name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> searchByName(@RequestParam String name) {
        // log.info("Buscando usuarios por nombre: {}", name);
        
        // List<User> users = userService.searchByName(name); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Obtiene usuarios por rol
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        // log.info("Obteniendo usuarios por rol: {}", role);
        
        // UserRole userRole = UserRole.valueOf(role.toUpperCase()); // Enum no definido
        // List<User> users = userService.getUsersByRole(userRole); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Obtiene usuarios activos
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getActiveUsers() {
        // log.info("Obteniendo usuarios activos");
        
        // List<User> users = userService.getActiveUsers(); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Obtiene usuarios inactivos
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getInactiveUsers() {
        // log.info("Obteniendo usuarios inactivos");
        
        // List<User> users = userService.getInactiveUsers(); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Obtiene usuarios registrados recientemente
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getRecentUsers(@RequestParam(defaultValue = "7") int days) {
        // log.info("Obteniendo usuarios registrados en los últimos {} días", days);
        
        // LocalDateTime since = LocalDateTime.now().minusDays(days);
        // List<User> users = userService.getUsersRegisteredSince(since); // Método no implementado
        return ResponseEntity.badRequest()
                .body(List.of()); // Retorna lista vacía como placeholder
    }
    
    /**
     * Obtiene estadísticas de usuarios
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        // log.info("Obteniendo estadísticas de usuarios");
        
        // Map<String, Object> stats = Map.of(
        //         "totalUsers", userService.getTotalUserCount(),
        //         "activeUsers", userService.getActiveUserCount(),
        //         "usersByRole", userService.getUserStatsByRole(),
        //         "usersByMonth", userService.getUserStatsByMonth(LocalDateTime.now().minusYears(1))
        // ); // Métodos no implementados
        Map<String, Object> stats = Map.of(
                "error", "Funcionalidad de estadísticas no implementada"
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Elimina un usuario (solo administradores)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {
        
        // log.info("Eliminando usuario ID: {} por admin: {}", userId, authentication.getName());
        
        User currentUser = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        userService.deleteUser(userId);
        
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado exitosamente"));
    }
    
    /**
     * Manejo de errores
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        // log.error("Error de argumento: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        // log.error("Error de estado: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        // log.error("Error interno del servidor: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
    }
}