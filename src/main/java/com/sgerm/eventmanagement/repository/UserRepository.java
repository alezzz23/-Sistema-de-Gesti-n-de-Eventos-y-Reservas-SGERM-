package com.sgerm.eventmanagement.repository;

import com.sgerm.eventmanagement.model.Role;
import com.sgerm.eventmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca un usuario por su nombre de usuario o email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario dado, excluyendo un ID específico
     */
    boolean existsByUsernameAndIdNot(String username, Long id);
    
    /**
     * Verifica si existe un usuario con el email dado, excluyendo un ID específico
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Busca usuarios por rol
     */
    List<User> findByRole(Role role);
    
    /**
     * Busca usuarios activos
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Busca usuarios inactivos
     */
    List<User> findByIsActiveFalse();
    
    /**
     * Busca usuarios con email verificado
     */
    List<User> findByEmailVerifiedTrue();
    
    /**
     * Busca usuarios con email no verificado
     */
    List<User> findByEmailVerifiedFalse();
    
    /**
     * Busca usuarios por rol y estado activo
     */
    List<User> findByRoleAndIsActiveTrue(Role role);
    
    /**
     * Busca usuarios creados después de una fecha específica
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Busca usuarios que han iniciado sesión después de una fecha específica
     */
    List<User> findByLastLoginAfter(LocalDateTime date);
    
    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(u.lastName) LIKE LOWER('%' || :searchTerm || '%')")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Búsqueda general de usuarios (nombre, apellido, username, email)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(u.lastName) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(u.username) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(u.email) LIKE LOWER('%' || :searchTerm || '%')")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Busca usuarios por rol con paginación
     */
    Page<User> findByRole(Role role, Pageable pageable);
    
    /**
     * Busca usuarios activos con paginación
     */
    Page<User> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Busca usuarios por rol y estado activo con paginación
     */
    Page<User> findByRoleAndIsActiveTrue(Role role, Pageable pageable);
    
    /**
     * Cuenta usuarios por rol
     */
    long countByRole(Role role);
    
    /**
     * Cuenta usuarios activos
     */
    long countByIsActiveTrue();
    
    /**
     * Cuenta usuarios con email verificado
     */
    long countByEmailVerifiedTrue();
    
    /**
     * Cuenta usuarios registrados en el último mes
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersRegisteredSince(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Cuenta usuarios activos en el último mes
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :startDate")
    long countActiveUsersSince(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Obtiene los usuarios más activos (con más eventos organizados)
     */
    @Query("SELECT u FROM User u LEFT JOIN u.organizedEvents e " +
           "WHERE u.role = :role " +
           "GROUP BY u " +
           "ORDER BY COUNT(e) DESC")
    List<User> findMostActiveOrganizers(@Param("role") Role role, Pageable pageable);
    
    /**
     * Obtiene los usuarios con más reservas
     */
    @Query("SELECT u FROM User u LEFT JOIN u.bookings b " +
           "GROUP BY u " +
           "ORDER BY COUNT(b) DESC")
    List<User> findMostActiveUsers(Pageable pageable);
    
    /**
     * Busca organizadores disponibles (activos y con rol ORGANIZER)
     */
    @Query("SELECT u FROM User u WHERE u.role = com.sgerm.eventmanagement.model.Role.ORGANIZER AND u.isActive = true")
    List<User> findAvailableOrganizers();
    
    /**
     * Busca usuarios por número de teléfono
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Actualiza la fecha de último login
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * Actualiza el estado de verificación de email
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, @Param("verified") boolean verified);
    
    /**
     * Actualiza el estado activo del usuario
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :userId")
    void updateActiveStatus(@Param("userId") Long userId, @Param("active") boolean active);
    
    /**
     * Busca usuarios inactivos por más de X días
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate OR u.lastLogin IS NULL")
    List<User> findInactiveUsersSince(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Busca usuarios con email no verificado creados antes de una fecha
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersCreatedBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Obtiene estadísticas de usuarios por rol
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatsByRole();
    
    /**
     * Obtiene estadísticas de usuarios por mes de registro
     */
    @Query("SELECT u.createdAt, COUNT(u) " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY u.createdAt " +
           "ORDER BY u.createdAt")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Busca usuarios similares por nombre y apellido
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) = LOWER(:firstName) AND " +
           "LOWER(u.lastName) = LOWER(:lastName) AND " +
           "u.id != :excludeId")
    List<User> findSimilarUsers(@Param("firstName") String firstName, 
                               @Param("lastName") String lastName, 
                               @Param("excludeId") Long excludeId);
    
    /**
     * Busca usuarios por múltiples roles
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoleIn(@Param("roles") List<Role> roles);
    
    /**
     * Busca usuarios por múltiples roles con paginación
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    Page<User> findByRoleIn(@Param("roles") List<Role> roles, Pageable pageable);
}