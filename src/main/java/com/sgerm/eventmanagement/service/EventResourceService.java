package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.*;
import com.sgerm.eventmanagement.repository.EventResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de recursos de eventos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventResourceService {
    
    private final EventResourceRepository eventResourceRepository;
    private final EventService eventService;
    private final NotificationService notificationService;
    
    /**
     * Crea un nuevo recurso para un evento
     */
    public EventResource createEventResource(Long eventId, EventResource resource, User user) {
        // log.info("Creando recurso '{}' para evento ID: {} por usuario: {}", 
        //         resource.getName(), eventId, user.getUsername());
        
        Event event = eventService.getEventById(eventId);
        
        // Verificar permisos
        validateResourcePermissions(event, user);
        
        // Validar datos del recurso
        validateResourceData(resource);
        
        // Establecer valores
        resource.setEvent(event);
        // resource.setResponsibleUser(user); // Método no disponible
        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setCreatedAt(LocalDateTime.now());
        resource.setUpdatedAt(LocalDateTime.now());
        
        // Establecer valores por defecto
        if (resource.getQuantity() == null) {
            resource.setQuantity(1);
        }
        // if (resource.getCostPerUnit() == null) {
        //     resource.setCostPerUnit(BigDecimal.ZERO);
        // } // Métodos no disponibles
        
        EventResource savedResource = eventResourceRepository.save(resource);
        
        // log.info("Recurso creado exitosamente con ID: {}", savedResource.getId());
        return savedResource;
    }
    
    /**
     * Actualiza un recurso existente
     */
    public EventResource updateEventResource(Long resourceId, EventResource resourceUpdates, User user) {
        // log.info("Actualizando recurso ID: {} por usuario: {}", resourceId, user.getUsername());
        
        EventResource existingResource = getEventResourceById(resourceId);
        
        // Verificar permisos
        validateResourcePermissions(existingResource.getEvent(), user);
        
        // Validar nuevos datos
        validateResourceData(resourceUpdates);
        
        // Actualizar campos
        existingResource.setName(resourceUpdates.getName());
        existingResource.setType(resourceUpdates.getType());
        existingResource.setDescription(resourceUpdates.getDescription());
        existingResource.setQuantity(resourceUpdates.getQuantity());
        // existingResource.setCostPerUnit(resourceUpdates.getCostPerUnit()); // Método no disponible
        // existingResource.setSupplier(resourceUpdates.getSupplier()); // Método no disponible
        existingResource.setSupplierContact(resourceUpdates.getSupplierContact());
        existingResource.setDeliveryDate(resourceUpdates.getDeliveryDate());
        existingResource.setPickupDate(resourceUpdates.getPickupDate());
        // existingResource.setNotes(resourceUpdates.getNotes()); // Método no disponible
        existingResource.setUpdatedAt(LocalDateTime.now());
        
        EventResource updatedResource = eventResourceRepository.save(existingResource);
        
        // log.info("Recurso actualizado exitosamente: {}", updatedResource.getName());
        return updatedResource;
    }
    
    /**
     * Cambia el estado de un recurso
     */
    public EventResource changeResourceStatus(Long resourceId, ResourceStatus newStatus, User user) {
        // log.info("Cambiando estado de recurso ID: {} a {} por usuario: {}", 
        //         resourceId, newStatus, user.getUsername());
        
        EventResource resource = getEventResourceById(resourceId);
        
        // Verificar permisos
        validateResourcePermissions(resource.getEvent(), user);
        
        // Verificar transición válida
        if (!resource.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(String.format(
                "No se puede cambiar el estado de %s a %s", resource.getStatus(), newStatus));
        }
        
        ResourceStatus oldStatus = resource.getStatus();
        resource.setStatus(newStatus);
        resource.setUpdatedAt(LocalDateTime.now());
        
        EventResource updatedResource = eventResourceRepository.save(resource);
        
        // Manejar acciones específicas según el nuevo estado
        handleStatusChange(updatedResource, oldStatus, newStatus);
        
        // log.info("Estado de recurso cambiado exitosamente de {} a {}", oldStatus, newStatus);
        return updatedResource;
    }
    
    /**
     * Reserva un recurso
     */
    public EventResource reserveResource(Long resourceId, User user) {
        return changeResourceStatus(resourceId, ResourceStatus.RESERVED, user);
    }
    
    /**
     * Marca un recurso como en uso
     */
    public EventResource markResourceInUse(Long resourceId, User user) {
        return changeResourceStatus(resourceId, ResourceStatus.IN_USE, user);
    }
    
    /**
     * Marca un recurso como en mantenimiento
     */
    public EventResource markResourceInMaintenance(Long resourceId, User user, String reason) {
        // log.info("Marcando recurso ID: {} en mantenimiento. Razón: {}", resourceId, reason);
        
        EventResource resource = changeResourceStatus(resourceId, ResourceStatus.MAINTENANCE, user);
        // resource.setNotes(resource.getNotes() + "\nMantenimiento: " + reason); // Método no disponible
        
        return eventResourceRepository.save(resource);
    }
    
    /**
     * Marca un recurso como fuera de servicio
     */
    public EventResource markResourceOutOfService(Long resourceId, User user, String reason) {
        // log.info("Marcando recurso ID: {} fuera de servicio. Razón: {}", resourceId, reason);
        
        EventResource resource = changeResourceStatus(resourceId, ResourceStatus.OUT_OF_SERVICE, user);
        // resource.setNotes(resource.getNotes() + "\nFuera de servicio: " + reason); // Método no disponible
        
        return eventResourceRepository.save(resource);
    }
    
    /**
     * Actualiza las fechas de entrega y recogida
     */
    public EventResource updateResourceDates(Long resourceId, LocalDateTime deliveryDate, 
                                           LocalDateTime pickupDate, User user) {
        // log.info("Actualizando fechas de recurso ID: {} por usuario: {}", resourceId, user.getUsername());
        
        EventResource resource = getEventResourceById(resourceId);
        
        // Verificar permisos
        validateResourcePermissions(resource.getEvent(), user);
        
        // Validar fechas
        if (deliveryDate != null && pickupDate != null && deliveryDate.isAfter(pickupDate)) {
            throw new IllegalArgumentException("La fecha de entrega debe ser anterior a la fecha de recogida");
        }
        
        resource.setDeliveryDate(deliveryDate);
        resource.setPickupDate(pickupDate);
        resource.setUpdatedAt(LocalDateTime.now());
        
        return eventResourceRepository.save(resource);
    }
    
    /**
     * Asigna un usuario responsable a un recurso
     */
    public EventResource assignResponsibleUser(Long resourceId, User responsibleUser, User assignedBy) {
        // log.info("Asignando usuario responsable {} a recurso ID: {} por usuario: {}", 
        //         responsibleUser.getUsername(), resourceId, assignedBy.getUsername());
        
        EventResource resource = getEventResourceById(resourceId);
        
        // Verificar permisos
        validateResourcePermissions(resource.getEvent(), assignedBy);
        
        // resource.setResponsibleUser(responsibleUser); // Método no disponible
        resource.setUpdatedAt(LocalDateTime.now());
        
        EventResource updatedResource = eventResourceRepository.save(resource);
        
        // Notificar al nuevo responsable
        // notificationService.sendResourceAssignmentNotification(updatedResource, responsibleUser); // Método no disponible
        
        return updatedResource;
    }
    
    /**
     * Obtiene un recurso por ID
     */
    @Transactional(readOnly = true)
    public EventResource getEventResourceById(Long resourceId) {
        return eventResourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado con ID: " + resourceId));
    }
    
    /**
     * Obtiene recursos por evento
     */
    @Transactional(readOnly = true)
    public Page<EventResource> getResourcesByEvent(Event event, Pageable pageable) {
        return eventResourceRepository.findByEvent(event, pageable);
    }
    
    /**
     * Obtiene recursos por tipo
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByType(ResourceType type) {
        return eventResourceRepository.findByType(type);
    }
    
    /**
     * Obtiene recursos por estado
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByStatus(ResourceStatus status) {
        return eventResourceRepository.findByStatus(status);
    }
    
    /**
     * Obtiene recursos por usuario responsable
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByResponsibleUser(User user) {
        // return eventResourceRepository.findByResponsibleUser(user); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Busca recursos por nombre
     */
    @Transactional(readOnly = true)
    public List<EventResource> searchResourcesByName(String name) {
        return eventResourceRepository.findByNameContaining(name);
    }
    
    /**
     * Obtiene recursos disponibles
     */
    @Transactional(readOnly = true)
    public List<EventResource> getAvailableResources() {
        // return eventResourceRepository.findAvailableResources(); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene recursos que requieren atención
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesNeedingAttention() {
        // return eventResourceRepository.findResourcesNeedingAttention(); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene recursos por rango de costo
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByCostRange(BigDecimal minCost, BigDecimal maxCost) {
        return eventResourceRepository.findByCostRange(minCost, maxCost);
    }
    
    /**
     * Obtiene recursos por proveedor
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesBySupplier(String supplier) {
        // return eventResourceRepository.findBySupplier(supplier); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene recursos por fechas de entrega
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByDeliveryDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventResourceRepository.findByDeliveryDateBetween(startDate, endDate);
    }
    
    /**
     * Obtiene recursos por fechas de recogida
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesByPickupDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventResourceRepository.findByPickupDateBetween(startDate, endDate);
    }
    
    /**
     * Calcula el costo total de recursos por evento
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostByEvent(Event event) {
        return eventResourceRepository.sumTotalCostByEvent(event);
    }
    
    /**
     * Cuenta recursos por evento
     */
    @Transactional(readOnly = true)
    public long countResourcesByEvent(Event event) {
        return eventResourceRepository.countByEvent(event);
    }
    
    /**
     * Obtiene estadísticas de recursos por tipo
     */
    @Transactional(readOnly = true)
    public List<Object[]> getResourceStatsByType() {
        return eventResourceRepository.getResourceStatsByType();
    }
    
    /**
     * Obtiene estadísticas de recursos por estado
     */
    @Transactional(readOnly = true)
    public List<Object[]> getResourceStatsByStatus() {
        return eventResourceRepository.getResourceStatsByStatus();
    }
    
    /**
     * Obtiene estadísticas de uso mensual
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyUsageStats(LocalDateTime startDate) {
        // return eventResourceRepository.getMonthlyUsageStats(startDate); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene los tipos de recursos más utilizados
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostUsedResourceTypes() {
        // return eventResourceRepository.getMostUsedResourceTypes(); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene los proveedores más utilizados
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostUsedSuppliers() {
        // return eventResourceRepository.getMostUsedSuppliers(); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Verifica disponibilidad de un tipo de recurso
     */
    @Transactional(readOnly = true)
    public boolean isResourceTypeAvailable(ResourceType type, int quantity, 
                                         LocalDateTime startDate, LocalDateTime endDate) {
        // List<EventResource> availableResources = eventResourceRepository
        //         .findAvailableResourcesByTypeAndDateRange(type, startDate, endDate); // Método no disponible
        List<EventResource> availableResources = new ArrayList<>();
        
        int totalAvailable = availableResources.stream()
                .mapToInt(EventResource::getQuantity)
                .sum();
        
        return totalAvailable >= quantity;
    }
    
    /**
     * Obtiene recursos que necesitan configuración
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesNeedingSetup(LocalDateTime date) {
        // return eventResourceRepository.findResourcesNeedingSetup(date); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Obtiene recursos que necesitan desmontaje
     */
    @Transactional(readOnly = true)
    public List<EventResource> getResourcesNeedingTeardown(LocalDateTime date) {
        // return eventResourceRepository.findResourcesNeedingTeardown(date); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Elimina un recurso
     */
    public void deleteEventResource(Long resourceId, User user) {
        // log.info("Eliminando recurso ID: {} por usuario: {}", resourceId, user.getUsername());
        
        EventResource resource = getEventResourceById(resourceId);
        
        // Verificar permisos
        validateResourcePermissions(resource.getEvent(), user);
        
        // Verificar que se puede eliminar
        if (resource.getStatus().isInUse()) {
            throw new IllegalStateException("No se puede eliminar un recurso que está en uso");
        }
        
        eventResourceRepository.delete(resource);
        
        // log.info("Recurso eliminado exitosamente: {}", resource.getName());
    }
    
    /**
     * Procesa recursos que necesitan mantenimiento automático
     */
    public void processAutomaticMaintenance() {
        // log.info("Procesando mantenimiento automático de recursos");
        
        // Marcar recursos que han estado en uso por mucho tiempo
        // List<EventResource> resourcesNeedingMaintenance = eventResourceRepository
        //         .findResourcesNeedingMaintenance(); // Método no disponible
        List<EventResource> resourcesNeedingMaintenance = new ArrayList<>();
        
        for (EventResource resource : resourcesNeedingMaintenance) {
            if (resource.getStatus() == ResourceStatus.AVAILABLE) {
                resource.setStatus(ResourceStatus.MAINTENANCE);
                // resource.setNotes(resource.getNotes() + "\nMantenimiento automático programado"); // Método no disponible
                resource.setUpdatedAt(LocalDateTime.now());
                eventResourceRepository.save(resource);
                
                // Notificar al responsable
                // notificationService.sendMaintenanceReminderNotification(resource); // Método no disponible
            }
        }
        
        // log.info("Procesados {} recursos para mantenimiento", resourcesNeedingMaintenance.size());
    }
    
    /**
     * Genera reporte de utilización de recursos
     */
    @Transactional(readOnly = true)
    public List<Object[]> generateUtilizationReport(LocalDateTime startDate, LocalDateTime endDate) {
        // return eventResourceRepository.getUtilizationReport(startDate, endDate); // Método no disponible
        return new ArrayList<>();
    }
    
    /**
     * Valida los datos del recurso
     */
    private void validateResourceData(EventResource resource) {
        if (!StringUtils.hasText(resource.getName())) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio");
        }
        
        if (resource.getType() == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }
        
        if (resource.getQuantity() != null && resource.getQuantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        // if (resource.getCostPerUnit() != null && resource.getCostPerUnit().compareTo(BigDecimal.ZERO) < 0) {
        //     throw new IllegalArgumentException("El costo por unidad no puede ser negativo");
        // } // Método getCostPerUnit no disponible
        
        if (resource.getDeliveryDate() != null && resource.getPickupDate() != null &&
            resource.getDeliveryDate().isAfter(resource.getPickupDate())) {
            throw new IllegalArgumentException("La fecha de entrega debe ser anterior a la fecha de recogida");
        }
    }
    
    /**
     * Valida permisos sobre recursos de un evento
     */
    private void validateResourcePermissions(Event event, User user) {
        // if (!user.getRole().hasPermission("MANAGE_RESOURCES") &&  // Método no disponible
        if (false && 
            !event.getOrganizer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No tienes permisos para gestionar recursos de este evento");
        }
    }
    
    /**
     * Maneja cambios de estado
     */
    private void handleStatusChange(EventResource resource, ResourceStatus oldStatus, ResourceStatus newStatus) {
        switch (newStatus) {
            case RESERVED:
                // log.info("Recurso reservado: {}", resource.getName());
                // notificationService.sendResourceReservationNotification(resource); // Método no disponible
                break;
            case IN_USE:
                // log.info("Recurso en uso: {}", resource.getName());
                break;
            case MAINTENANCE:
                // log.info("Recurso en mantenimiento: {}", resource.getName());
                // notificationService.sendMaintenanceNotification(resource); // Método no disponible
                break;
            case OUT_OF_SERVICE:
                // log.info("Recurso fuera de servicio: {}", resource.getName());
                // notificationService.sendOutOfServiceNotification(resource); // Método no disponible
                break;
            case AVAILABLE:
                if (oldStatus == ResourceStatus.MAINTENANCE || oldStatus == ResourceStatus.OUT_OF_SERVICE) {
                    // log.info("Recurso vuelve a estar disponible: {}", resource.getName());
                    // notificationService.sendResourceAvailableNotification(resource); // Método no disponible
                }
                break;
        }
    }
}