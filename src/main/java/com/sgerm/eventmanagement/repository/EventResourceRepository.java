package com.sgerm.eventmanagement.repository;

import com.sgerm.eventmanagement.model.Event;
import com.sgerm.eventmanagement.model.EventResource;
import com.sgerm.eventmanagement.model.ResourceStatus;
import com.sgerm.eventmanagement.model.ResourceType;
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
 * Repositorio para la entidad EventResource
 */
@Repository
public interface EventResourceRepository extends JpaRepository<EventResource, Long> {
    
    /**
     * Busca recursos por evento
     */
    List<EventResource> findByEvent(Event event);
    
    /**
     * Busca recursos por evento con paginación
     */
    Page<EventResource> findByEvent(Event event, Pageable pageable);
    
    /**
     * Busca recursos por tipo
     */
    List<EventResource> findByType(ResourceType type);
    
    /**
     * Busca recursos por estado
     */
    List<EventResource> findByStatus(ResourceStatus status);
    
    /**
     * Busca recursos por responsable
     */
    List<EventResource> findByAssignedBy(User assignedBy);
    
    /**
     * Busca recursos por evento y tipo
     */
    List<EventResource> findByEventAndType(Event event, ResourceType type);
    
    /**
     * Busca recursos por evento y estado
     */
    List<EventResource> findByEventAndStatus(Event event, ResourceStatus status);
    
    /**
     * Busca recursos disponibles por tipo
     */
    @Query("SELECT r FROM EventResource r WHERE r.type = :type AND r.status = :status")
    List<EventResource> findAvailableResourcesByType(@Param("type") ResourceType type, @Param("status") ResourceStatus status);
    
    /**
     * Busca recursos por nombre (búsqueda parcial)
     */
    @Query("SELECT r FROM EventResource r WHERE LOWER(r.name) LIKE LOWER('%' || :name || '%')")
    List<EventResource> findByNameContaining(@Param("name") String name);
    
    /**
     * Busca recursos por proveedor
     */
    @Query("SELECT r FROM EventResource r WHERE LOWER(r.supplierName) LIKE LOWER('%' || :supplier || '%')")
    List<EventResource> findBySupplierContaining(@Param("supplier") String supplier);
    
    /**
     * Busca recursos por rango de costos
     */
    @Query("SELECT r FROM EventResource r WHERE r.unitCost BETWEEN :minCost AND :maxCost")
    List<EventResource> findByCostRange(@Param("minCost") BigDecimal minCost, 
                                       @Param("maxCost") BigDecimal maxCost);
    
    /**
     * Busca recursos gratuitos
     */
    @Query("SELECT r FROM EventResource r WHERE r.unitCost = 0 OR r.unitCost IS NULL")
    List<EventResource> findFreeResources();
    
    /**
     * Busca recursos por fecha de entrega
     */
    List<EventResource> findByDeliveryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca recursos por fecha de recogida
     */
    List<EventResource> findByPickupDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca recursos que necesitan entrega hoy
     */
    @Query("SELECT r FROM EventResource r WHERE DATE(r.deliveryDate) = DATE(:today) " +
           "AND r.status IN (com.sgerm.eventmanagement.model.ResourceStatus.RESERVED, com.sgerm.eventmanagement.model.ResourceStatus.IN_TRANSIT)")
    List<EventResource> findResourcesForDeliveryToday(@Param("today") LocalDateTime today);
    
    /**
     * Busca recursos que necesitan recogida hoy
     */
    @Query("SELECT r FROM EventResource r WHERE DATE(r.pickupDate) = DATE(:today) " +
           "AND r.status IN (com.sgerm.eventmanagement.model.ResourceStatus.IN_USE, com.sgerm.eventmanagement.model.ResourceStatus.TEARDOWN)")
    List<EventResource> findResourcesForPickupToday(@Param("today") LocalDateTime today);
    
    /**
     * Busca recursos en mantenimiento
     */
    List<EventResource> findByStatusOrderByUpdatedAtDesc(ResourceStatus status);
    
    /**
     * Busca recursos por múltiples tipos
     */
    @Query("SELECT r FROM EventResource r WHERE r.type IN :types")
    List<EventResource> findByTypeIn(@Param("types") List<ResourceType> types);
    
    /**
     * Busca recursos por múltiples estados
     */
    @Query("SELECT r FROM EventResource r WHERE r.status IN :statuses")
    List<EventResource> findByStatusIn(@Param("statuses") List<ResourceStatus> statuses);
    
    /**
     * Cuenta recursos por evento
     */
    long countByEvent(Event event);
    
    /**
     * Cuenta recursos por tipo
     */
    long countByType(ResourceType type);
    
    /**
     * Cuenta recursos por estado
     */
    long countByStatus(ResourceStatus status);
    
    /**
     * Cuenta recursos por evento y estado
     */
    long countByEventAndStatus(Event event, ResourceStatus status);
    
    /**
     * Suma el costo total de recursos por evento
     */
    @Query("SELECT COALESCE(SUM(r.unitCost * r.quantity), 0) FROM EventResource r WHERE r.event = :event")
    BigDecimal sumTotalCostByEvent(@Param("event") Event event);
    
    /**
     * Suma el costo total de recursos por tipo
     */
    @Query("SELECT COALESCE(SUM(r.unitCost * r.quantity), 0) FROM EventResource r WHERE r.type = :type")
    BigDecimal sumTotalCostByType(@Param("type") ResourceType type);
    
    /**
     * Suma la cantidad total de recursos por tipo
     */
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM EventResource r WHERE r.type = :type")
    Integer sumQuantityByType(@Param("type") ResourceType type);
    
    /**
     * Busca recursos más utilizados
     */
    @Query("SELECT r.type, COUNT(r) FROM EventResource r " +
           "GROUP BY r.type " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findMostUsedResourceTypes();
    
    /**
     * Busca recursos más costosos
     */
    @Query("SELECT r FROM EventResource r ORDER BY (r.unitCost * r.quantity) DESC")
    List<EventResource> findMostExpensiveResources(Pageable pageable);
    
    /**
     * Busca recursos por responsable y estado
     */
    List<EventResource> findByAssignedByAndStatus(User assignedBy, ResourceStatus status);
    
    /**
     * Busca recursos asignados a un responsable
     */
    @Query("SELECT r FROM EventResource r WHERE r.assignedBy = :assignedBy " +
           "AND r.status NOT IN ('RETIRED', 'LOST')")
    List<EventResource> findActiveResourcesByResponsible(@Param("assignedBy") User assignedBy);
    
    /**
     * Busca recursos que requieren atención
     */
    @Query("SELECT r FROM EventResource r WHERE r.status IN ('OUT_OF_SERVICE', 'LOST', 'PENDING_INSPECTION')")
    List<EventResource> findResourcesRequiringAttention();
    
    /**
     * Busca recursos disponibles para un rango de fechas
     */
    @Query("SELECT r FROM EventResource r WHERE r.status = :availableStatus " +
           "AND r.id NOT IN (" +
           "  SELECT er.id FROM EventResource er " +
           "  WHERE er.status IN (:busyStatuses) " +
           "  AND ((er.deliveryDate <= :endDate AND er.pickupDate >= :startDate))" +
           ")")
    List<EventResource> findAvailableResourcesForDateRange(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("availableStatus") ResourceStatus availableStatus,
                                                          @Param("busyStatuses") List<ResourceStatus> busyStatuses);
    
    /**
     * Verifica disponibilidad de un recurso específico para fechas
     */
    @Query("SELECT COUNT(r) = 0 FROM EventResource r WHERE r.id = :resourceId " +
           "AND r.status IN (:busyStatuses) " +
           "AND ((r.deliveryDate <= :endDate AND r.pickupDate >= :startDate))")
    boolean isResourceAvailableForDates(@Param("resourceId") Long resourceId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("busyStatuses") List<ResourceStatus> busyStatuses);
    
    /**
     * Actualiza el estado de un recurso
     */
    @Modifying
    @Query("UPDATE EventResource r SET r.status = :status WHERE r.id = :resourceId")
    void updateResourceStatus(@Param("resourceId") Long resourceId, @Param("status") ResourceStatus status);
    
    /**
     * Actualiza las fechas de entrega y recogida
     */
    @Modifying
    @Query("UPDATE EventResource r SET r.deliveryDate = :deliveryDate, r.pickupDate = :pickupDate " +
           "WHERE r.id = :resourceId")
    void updateDeliveryDates(@Param("resourceId") Long resourceId,
                            @Param("deliveryDate") LocalDateTime deliveryDate,
                            @Param("pickupDate") LocalDateTime pickupDate);
    
    /**
     * Actualiza el responsable de un recurso
     */
    @Modifying
    @Query("UPDATE EventResource r SET r.assignedBy = :assignedBy WHERE r.id = :resourceId")
    void updateResponsible(@Param("resourceId") Long resourceId, @Param("assignedBy") User assignedBy);
    
    /**
     * Busca recursos con conflictos de programación
     */
    @Query("SELECT r FROM EventResource r WHERE r.status IN (com.sgerm.eventmanagement.model.ResourceStatus.RESERVED, com.sgerm.eventmanagement.model.ResourceStatus.IN_USE) " +
           "AND r.deliveryDate > r.pickupDate")
    List<EventResource> findResourcesWithSchedulingConflicts();
    
    /**
     * Busca recursos próximos a vencer (entrega/recogida)
     */
    @Query("SELECT r FROM EventResource r WHERE " +
           "(r.deliveryDate BETWEEN :now AND :futureDate) OR " +
           "(r.pickupDate BETWEEN :now AND :futureDate)")
    List<EventResource> findResourcesWithUpcomingDates(@Param("now") LocalDateTime now,
                                                      @Param("futureDate") LocalDateTime futureDate);
    
    /**
     * Obtiene estadísticas de recursos por tipo
     */
    @Query("SELECT r.type, COUNT(r), SUM(r.quantity), SUM(r.unitCost * r.quantity) " +
           "FROM EventResource r " +
           "GROUP BY r.type")
    List<Object[]> getResourceStatsByType();
    
    /**
     * Obtiene estadísticas de recursos por estado
     */
    @Query("SELECT r.status, COUNT(r) FROM EventResource r GROUP BY r.status")
    List<Object[]> getResourceStatsByStatus();
    
    /**
     * Obtiene estadísticas de uso de recursos por mes
     */
    @Query("SELECT r.deliveryDate, COUNT(r) " +
           "FROM EventResource r " +
           "WHERE r.deliveryDate >= :startDate " +
           "GROUP BY r.deliveryDate " +
           "ORDER BY r.deliveryDate")
    List<Object[]> getResourceUsageStatsByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Busca recursos con filtros avanzados
     */
    @Query("SELECT r FROM EventResource r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:eventId IS NULL OR r.event.id = :eventId) AND " +
           "(:assignedById IS NULL OR r.assignedBy.id = :assignedById) AND " +
           "(:minCost IS NULL OR r.unitCost >= :minCost) AND " +
           "(:maxCost IS NULL OR r.unitCost <= :maxCost) AND " +
           "(:supplier IS NULL OR LOWER(r.supplierName) LIKE LOWER('%' || :supplier || '%'))")
    Page<EventResource> findResourcesWithFilters(@Param("type") ResourceType type,
                                                @Param("status") ResourceStatus status,
                                                @Param("eventId") Long eventId,
                                                @Param("assignedById") Long assignedById,
                                                @Param("minCost") BigDecimal minCost,
                                                @Param("maxCost") BigDecimal maxCost,
                                                @Param("supplier") String supplier,
                                                Pageable pageable);
    
    /**
     * Busca recursos duplicados (mismo nombre y tipo)
     */
    @Query("SELECT r FROM EventResource r WHERE " +
           "LOWER(r.name) = LOWER(:name) AND r.type = :type AND r.id != :excludeId")
    List<EventResource> findDuplicateResources(@Param("name") String name,
                                              @Param("type") ResourceType type,
                                              @Param("excludeId") Long excludeId);
    
    /**
     * Busca los proveedores más utilizados
     */
    @Query("SELECT r.supplierName, COUNT(r) FROM EventResource r " +
           "WHERE r.supplierName IS NOT NULL " +
           "GROUP BY r.supplierName " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findMostUsedSuppliers(Pageable pageable);
}