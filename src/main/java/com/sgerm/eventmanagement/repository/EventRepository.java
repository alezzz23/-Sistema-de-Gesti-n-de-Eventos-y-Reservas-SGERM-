package com.sgerm.eventmanagement.repository;

import com.sgerm.eventmanagement.model.Event;
import com.sgerm.eventmanagement.model.EventCategory;
import com.sgerm.eventmanagement.model.EventStatus;
import com.sgerm.eventmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Event
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    /**
     * Busca eventos por organizador
     */
    List<Event> findByOrganizer(User organizer);
    
    /**
     * Busca eventos por organizador con paginación
     */
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    
    /**
     * Busca eventos por estado
     */
    List<Event> findByStatus(EventStatus status);
    
    /**
     * Busca eventos por estado con paginación
     */
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    
    /**
     * Busca eventos por categoría
     */
    List<Event> findByCategory(EventCategory category);
    
    /**
     * Busca eventos por categoría con paginación
     */
    Page<Event> findByCategory(EventCategory category, Pageable pageable);
    
    /**
     * Busca eventos públicos
     */
    List<Event> findByIsPublicTrue();
    
    /**
     * Busca eventos públicos con paginación
     */
    Page<Event> findByIsPublicTrue(Pageable pageable);
    
    /**
     * Busca eventos públicos y publicados
     */
    @Query("SELECT e FROM Event e WHERE e.isPublic = true AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED")
    List<Event> findPublicPublishedEvents();
    
    /**
     * Busca eventos públicos y publicados con paginación
     */
    @Query("SELECT e FROM Event e WHERE e.isPublic = true AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED")
    Page<Event> findPublicPublishedEvents(Pageable pageable);
    
    /**
     * Busca eventos por rango de fechas
     */
    @Query("SELECT e FROM Event e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
    List<Event> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Busca eventos que comienzan después de una fecha
     */
    List<Event> findByStartDateAfter(LocalDateTime date);
    
    /**
     * Busca eventos que terminan antes de una fecha
     */
    List<Event> findByEndDateBefore(LocalDateTime date);
    
    /**
     * Busca eventos próximos (que comienzan en las próximas X horas)
     */
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :now AND :futureDate AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now, 
                                  @Param("futureDate") LocalDateTime futureDate);
    
    /**
     * Busca eventos por título (búsqueda parcial)
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER('%' || :title || '%')")
    List<Event> findByTitleContaining(@Param("title") String title);
    
    /**
     * Búsqueda general de eventos (título, descripción, ubicación)
     */
    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(e.description) LIKE LOWER('%' || :searchTerm || '%') OR " +
           "LOWER(e.location) LIKE LOWER('%' || :searchTerm || '%')")
    Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Busca eventos por ubicación
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) LIKE LOWER('%' || :location || '%')")
    List<Event> findByLocationContaining(@Param("location") String location);
    
    /**
     * Busca eventos por rango de precios
     */
    @Query("SELECT e FROM Event e WHERE e.price BETWEEN :minPrice AND :maxPrice")
    List<Event> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                 @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * Busca eventos gratuitos
     */
    @Query("SELECT e FROM Event e WHERE e.price = 0 OR e.price IS NULL")
    List<Event> findFreeEvents();
    
    /**
     * Busca eventos con entradas disponibles
     */
    @Query("SELECT e FROM Event e WHERE e.availableTickets > 0")
    List<Event> findEventsWithAvailableTickets();
    
    /**
     * Busca eventos agotados
     */
    @Query("SELECT e FROM Event e WHERE e.availableTickets = 0 OR e.status = :status")
    List<Event> findSoldOutEvents(@Param("status") EventStatus status);
    
    /**
     * Busca eventos por organizador y estado
     */
    List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);
    
    /**
     * Busca eventos por categoría y estado
     */
    List<Event> findByCategoryAndStatus(EventCategory category, EventStatus status);
    
    /**
     * Busca eventos por múltiples categorías
     */
    @Query("SELECT e FROM Event e WHERE e.category IN :categories")
    List<Event> findByCategoryIn(@Param("categories") List<EventCategory> categories);
    
    /**
     * Busca eventos por múltiples estados
     */
    @Query("SELECT e FROM Event e WHERE e.status IN :statuses")
    List<Event> findByStatusIn(@Param("statuses") List<EventStatus> statuses);
    
    /**
     * Busca eventos que requieren aprobación
     */
    List<Event> findByRequiresApprovalTrue();
    
    /**
     * Busca eventos pendientes de aprobación
     */
    @Query("SELECT e FROM Event e WHERE e.requiresApproval = true AND e.status = com.sgerm.eventmanagement.model.EventStatus.DRAFT")
    List<Event> findPendingApprovalEvents();
    
    /**
     * Busca eventos por tags
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.tags) LIKE LOWER('%' || :tag || '%')")
    List<Event> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * Cuenta eventos por organizador
     */
    long countByOrganizer(User organizer);
    
    /**
     * Cuenta eventos por estado
     */
    long countByStatus(EventStatus status);
    
    /**
     * Cuenta eventos por categoría
     */
    long countByCategory(EventCategory category);
    
    /**
     * Cuenta eventos públicos
     */
    long countByIsPublicTrue();
    
    /**
     * Cuenta eventos creados después de una fecha
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Obtiene eventos populares (con más reservas)
     */
    @Query("SELECT e FROM Event e LEFT JOIN e.bookings b " +
           "WHERE e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED " +
           "GROUP BY e " +
           "ORDER BY COUNT(b) DESC")
    List<Event> findPopularEvents(Pageable pageable);
    
    /**
     * Obtiene eventos recientes
     */
    @Query("SELECT e FROM Event e WHERE e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED ORDER BY e.createdAt DESC")
    List<Event> findRecentEvents(Pageable pageable);
    
    /**
     * Obtiene eventos próximos por categoría
     */
    @Query("SELECT e FROM Event e WHERE e.category = :category " +
           "AND e.startDate > :now " +
           "AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED " +
           "ORDER BY e.startDate ASC")
    List<Event> findUpcomingEventsByCategory(@Param("category") EventCategory category, 
                                           @Param("now") LocalDateTime now, 
                                           Pageable pageable);
    
    /**
     * Busca eventos similares por categoría y ubicación
     */
    @Query("SELECT e FROM Event e WHERE e.category = :category " +
           "AND LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "AND e.id != :excludeId " +
           "AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED")
    List<Event> findSimilarEvents(@Param("category") EventCategory category, 
                                 @Param("location") String location, 
                                 @Param("excludeId") Long excludeId, 
                                 Pageable pageable);
    
    /**
     * Actualiza el estado de un evento
     */
    @Modifying
    @Query("UPDATE Event e SET e.status = :status WHERE e.id = :eventId")
    void updateEventStatus(@Param("eventId") Long eventId, @Param("status") EventStatus status);
    
    /**
     * Actualiza las entradas disponibles
     */
    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = :availableTickets WHERE e.id = :eventId")
    void updateAvailableTickets(@Param("eventId") Long eventId, @Param("availableTickets") Integer availableTickets);
    
    /**
     * Incrementa las entradas disponibles
     */
    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets + :increment WHERE e.id = :eventId")
    void incrementAvailableTickets(@Param("eventId") Long eventId, @Param("increment") Integer increment);
    
    /**
     * Decrementa las entradas disponibles
     */
    @Modifying
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - :decrement WHERE e.id = :eventId AND e.availableTickets >= :decrement")
    int decrementAvailableTickets(@Param("eventId") Long eventId, @Param("decrement") Integer decrement);
    
    /**
     * Busca eventos que necesitan recordatorio
     */
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startTime AND :endTime " +
           "AND e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED")
    List<Event> findEventsNeedingReminder(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Busca eventos expirados que necesitan limpieza
     */
    @Query("SELECT e FROM Event e WHERE e.endDate < :cutoffDate " +
           "AND e.status NOT IN (com.sgerm.eventmanagement.model.EventStatus.COMPLETED, com.sgerm.eventmanagement.model.EventStatus.CANCELLED)")
    List<Event> findExpiredEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Obtiene estadísticas de eventos por categoría
     */
    @Query("SELECT e.category, COUNT(e) FROM Event e GROUP BY e.category")
    List<Object[]> getEventStatsByCategory();
    
    /**
     * Obtiene estadísticas de eventos por estado
     */
    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    List<Object[]> getEventStatsByStatus();
    
    /**
     * Obtiene estadísticas de eventos por mes
     */
    @Query("SELECT e.startDate, COUNT(e) " +
           "FROM Event e " +
           "WHERE e.startDate >= :startDate " +
           "GROUP BY e.startDate " +
           "ORDER BY e.startDate")
    List<Object[]> getEventStatsByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Obtiene ingresos totales por organizador
     */
    @Query("SELECT e.organizer, SUM(e.price * (e.capacity - e.availableTickets)) " +
           "FROM Event e " +
           "WHERE e.status = com.sgerm.eventmanagement.model.EventStatus.PUBLISHED " +
           "GROUP BY e.organizer " +
           "ORDER BY SUM(e.price * (e.capacity - e.availableTickets)) DESC")
    List<Object[]> getRevenueByOrganizer();
    
    /**
     * Busca eventos con filtros avanzados
     */
    @Query("SELECT e FROM Event e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR e.price <= :maxPrice) AND " +
           "(:startDate IS NULL OR e.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.endDate <= :endDate) AND " +
           "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:isPublic IS NULL OR e.isPublic = :isPublic)")
    Page<Event> findEventsWithFilters(@Param("category") EventCategory category,
                                     @Param("status") EventStatus status,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("location") String location,
                                     @Param("isPublic") Boolean isPublic,
                                     Pageable pageable);
}