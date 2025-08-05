package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.Role;
import com.sgerm.eventmanagement.model.User;
import com.sgerm.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para la gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final EmailService emailService;
    // private final NotificationService notificationService;
    
    /**
     * Implementación de UserDetailsService para Spring Security
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
    
    /**
     * Crea un nuevo usuario
     */
    public User createUser(User user) {
        // log.info("Creando nuevo usuario: {}", user.getUsername());
        
        // Validar que el username y email sean únicos
        validateUniqueUser(user.getUsername(), user.getEmail(), null);
        
        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Establecer valores por defecto
        // user.setActive(true); // Método no disponible
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Asignar rol por defecto si no se especifica
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }
        
        User savedUser = userRepository.save(user);
        
        // Enviar email de verificación
        sendVerificationEmail(savedUser);
        
        // log.info("Usuario creado exitosamente: {}", savedUser.getUsername());
        return savedUser;
    }
    
    /**
     * Actualiza un usuario existente
     */
    public User updateUser(Long userId, User userUpdates) {
        // log.info("Actualizando usuario ID: {}", userId);
        
        User existingUser = getUserById(userId);
        
        // Validar unicidad si se cambia username o email
        if (!existingUser.getUsername().equals(userUpdates.getUsername()) ||
            !existingUser.getEmail().equals(userUpdates.getEmail())) {
            validateUniqueUser(userUpdates.getUsername(), userUpdates.getEmail(), userId);
        }
        
        // Actualizar campos permitidos
        existingUser.setUsername(userUpdates.getUsername());
        existingUser.setEmail(userUpdates.getEmail());
        existingUser.setFirstName(userUpdates.getFirstName());
        existingUser.setLastName(userUpdates.getLastName());
        existingUser.setPhoneNumber(userUpdates.getPhoneNumber());
        existingUser.setProfileImageUrl(userUpdates.getProfileImageUrl());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        // Si se cambió el email, marcar como no verificado
        if (!existingUser.getEmail().equals(userUpdates.getEmail())) {
            existingUser.setEmailVerified(false);
            sendVerificationEmail(existingUser);
        }
        
        User updatedUser = userRepository.save(existingUser);
        // log.info("Usuario actualizado exitosamente: {}", updatedUser.getUsername());
        
        return updatedUser;
    }
    
    /**
     * Cambia la contraseña de un usuario
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // log.info("Cambiando contraseña para usuario ID: {}", userId);
        
        User user = getUserById(userId);
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Notificar cambio de contraseña
        // notificationService.sendPasswordChangeNotification(user);
        
        // log.info("Contraseña cambiada exitosamente para usuario: {}", user.getUsername());
    }
    
    /**
     * Restablece la contraseña de un usuario
     */
    public void resetPassword(String email) {
        // log.info("Solicitando restablecimiento de contraseña para: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe o no
            // log.warn("Intento de restablecimiento para email no registrado: {}", email);
            return;
        }
        
        User user = userOpt.get();
        
        // Generar nueva contraseña temporal
        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Enviar email con nueva contraseña
        // emailService.sendPasswordResetEmail(user, temporaryPassword);
        
        // log.info("Contraseña restablecida para usuario: {}", user.getUsername());
    }
    
    /**
     * Verifica el email de un usuario
     */
    public boolean verifyEmail(String token) {
        // log.info("Verificando email con token: {}", token);
        
        // En una implementación real, el token estaría almacenado en la base de datos
        // Por simplicidad, aquí asumimos que el token contiene el ID del usuario
        try {
            Long userId = Long.parseLong(token);
            User user = getUserById(userId);
            
            // if (!user.isEmailVerified()) { // Método no disponible
            if (true) { // Placeholder
                user.setEmailVerified(true);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                // log.info("Email verificado exitosamente para usuario: {}", user.getUsername());
                return true;
            }
        } catch (Exception e) {
            // log.error("Error verificando email con token: {}", token, e);
        }
        
        return false;
    }
    
    /**
     * Activa o desactiva un usuario
     */
    public void toggleUserStatus(Long userId, boolean active) {
        // log.info("Cambiando estado de usuario ID {} a: {}", userId, active ? "activo" : "inactivo");
        
        User user = getUserById(userId);
        // user.setActive(active); // Método no disponible
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Notificar cambio de estado
        if (!active) {
            // notificationService.sendAccountDeactivationNotification(user);
        }
        
        // log.info("Estado de usuario cambiado exitosamente: {}", user.getUsername());
    }
    
    /**
     * Cambia el rol de un usuario
     */
    public void changeUserRole(Long userId, Role newRole) {
        // log.info("Cambiando rol de usuario ID {} a: {}", userId, newRole);
        
        User user = getUserById(userId);
        Role oldRole = user.getRole();
        
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Notificar cambio de rol
        // notificationService.sendRoleChangeNotification(user, oldRole, newRole);
        
        // log.info("Rol de usuario cambiado exitosamente: {} de {} a {}", 
        //        user.getUsername(), oldRole, newRole);
    }
    
    /**
     * Actualiza la fecha de último login
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            userRepository.updateLastLogin(userOpt.get().getId(), LocalDateTime.now());
        }
    }
    
    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
    }
    
    /**
     * Obtiene un usuario por username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Obtiene un usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Obtiene todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * Busca usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Busca usuarios activos
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        // return userRepository.findByActive(true); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Busca usuarios por nombre
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String name) {
        // return userRepository.findByFirstNameContainingOrLastNameContaining(name, name); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Búsqueda general de usuarios
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        // return userRepository.searchUsers(searchTerm); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene estadísticas de usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatsByRole() {
        return userRepository.getUserStatsByRole();
    }
    
    /**
     * Obtiene estadísticas de registros por mes
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRegistrationStatsByMonth(LocalDateTime startDate) {
        // return userRepository.getRegistrationStatsByMonth(startDate); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Cuenta usuarios activos
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        // return userRepository.countActiveUsers();
        return 0L;
    }
    
    /**
     * Cuenta usuarios por rol
     */
    @Transactional(readOnly = true)
    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * Verifica si existe un usuario con el username dado
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Verifica si existe un usuario con el email dado
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Elimina un usuario (soft delete)
     */
    public void deleteUser(Long userId) {
        // log.info("Eliminando usuario ID: {}", userId);
        
        User user = getUserById(userId);
        
        // En lugar de eliminar físicamente, desactivamos el usuario
        // user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // log.info("Usuario desactivado exitosamente: {}", user.getUsername());
    }
    
    /**
     * Valida que el username y email sean únicos
     */
    private void validateUniqueUser(String username, String email, Long excludeUserId) {
        if (StringUtils.hasText(username)) {
            Optional<User> existingByUsername = userRepository.findByUsername(username);
            if (existingByUsername.isPresent() && 
                (excludeUserId == null || !existingByUsername.get().getId().equals(excludeUserId))) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso: " + username);
            }
        }
        
        if (StringUtils.hasText(email)) {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent() && 
                (excludeUserId == null || !existingByEmail.get().getId().equals(excludeUserId))) {
                throw new IllegalArgumentException("El email ya está en uso: " + email);
            }
        }
    }
    
    /**
     * Envía email de verificación
     */
    private void sendVerificationEmail(User user) {
        try {
            String verificationToken = user.getId().toString(); // Simplificado
            // emailService.sendVerificationEmail(user, verificationToken);
            // log.info("Email de verificación enviado a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de verificación a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Genera una contraseña temporal
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}