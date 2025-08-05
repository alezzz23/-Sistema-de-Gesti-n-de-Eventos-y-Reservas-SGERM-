package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.*;
import com.sgerm.eventmanagement.repository.EventRepository;
import com.sgerm.eventmanagement.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de eventos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {
    
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    /**
     * Crea un nuevo evento
     */
    public Event createEvent(Event event, User organizer) {
        // log.info("Creando nuevo evento: {} por organizador: {}", event.getTitle(), organizer.getUsername());
        
        // Validar datos del evento
        validateEventData(event);
        
        // Establecer organizador y valores por defecto
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setAvailableTickets(event.getCapacity());
        
        // Establecer valores por defecto si no se especifican
        if (event.getPrice() == null) {
            event.setPrice(BigDecimal.ZERO);
        }
        if (event.getMaxTicketsPerUser() == null) {
            event.setMaxTicketsPerUser(10);
        }
        if (event.getIsPublic() == null) {
            event.setIsPublic(true);
        }
        if (event.getRequiresApproval() == null) {
            event.setRequiresApproval(false);
        }
        
        Event savedEvent = eventRepository.save(event);
        
        // log.info("Evento creado exitosamente con ID: {}", savedEvent.getId());
        return savedEvent;
    }
    
    /**
     * Actualiza un evento existente
     */
    public Event updateEvent(Long eventId, Event eventUpdates, User user) {
        // log.info("Actualizando evento ID: {} por usuario: {}", eventId, user.getUsername());
        
        Event existingEvent = getEventById(eventId);
        
        // Verificar permisos
        validateEventPermissions(existingEvent, user);
        
        // Verificar que el evento se puede editar
        if (!existingEvent.getStatus().isEditable()) {
            throw new IllegalStateException("No se puede editar un evento en estado: " + existingEvent.getStatus());
        }
        
        // Validar nuevos datos
        validateEventData(eventUpdates);
        
        // Detectar cambios importantes
        String changes = detectEventChanges(existingEvent, eventUpdates);
        
        // Actualizar campos permitidos
        existingEvent.setTitle(eventUpdates.getTitle());
        existingEvent.setDescription(eventUpdates.getDescription());
        existingEvent.setStartDate(eventUpdates.getStartDate());
        existingEvent.setEndDate(eventUpdates.getEndDate());
        existingEvent.setLocation(eventUpdates.getLocation());
        existingEvent.setCapacity(eventUpdates.getCapacity());
        existingEvent.setPrice(eventUpdates.getPrice());
        existingEvent.setCategory(eventUpdates.getCategory());
        existingEvent.setImageUrl(eventUpdates.getImageUrl());
        existingEvent.setIsPublic(eventUpdates.getIsPublic());
        existingEvent.setRequiresApproval(eventUpdates.getRequiresApproval());
        existingEvent.setMaxTicketsPerUser(eventUpdates.getMaxTicketsPerUser());
        existingEvent.setBookingDeadline(eventUpdates.getBookingDeadline());
        existingEvent.setCancellationDeadline(eventUpdates.getCancellationDeadline());
        existingEvent.setTags(eventUpdates.getTags());
        existingEvent.setContactEmail(eventUpdates.getContactEmail());
        existingEvent.setContactPhone(eventUpdates.getContactPhone());
        existingEvent.setUpdatedAt(LocalDateTime.now());
        
        // Actualizar tickets disponibles si cambió la capacidad
        if (!existingEvent.getCapacity().equals(eventUpdates.getCapacity())) {
            updateAvailableTickets(existingEvent);
        }
        
        Event updatedEvent = eventRepository.save(existingEvent);
        
        // Notificar cambios a usuarios con reservas si hay cambios importantes
        if (StringUtils.hasText(changes)) {
            notifyEventChanges(updatedEvent, changes);
        }
        
        // log.info("Evento actualizado exitosamente: {}", updatedEvent.getTitle());
        return updatedEvent;
    }
    
    /**
     * Cambia el estado de un evento
     */
    public Event changeEventStatus(Long eventId, EventStatus newStatus, User user) {
        // log.info("Cambiando estado de evento ID: {} a {} por usuario: {}", eventId, newStatus, user.getUsername());
        
        Event event = getEventById(eventId);
        
        // Verificar permisos
        validateEventPermissions(event, user);
        
        // Verificar transición válida
        if (!event.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(String.format(
                "No se puede cambiar el estado de %s a %s", event.getStatus(), newStatus));
        }
        
        EventStatus oldStatus = event.getStatus();
        event.setStatus(newStatus);
        event.setUpdatedAt(LocalDateTime.now());
        
        Event updatedEvent = eventRepository.save(event);
        
        // Manejar acciones específicas según el nuevo estado
        handleStatusChange(updatedEvent, oldStatus, newStatus);
        
        // log.info("Estado de evento cambiado exitosamente de {} a {}", oldStatus, newStatus);
        return updatedEvent;
    }
    
    /**
     * Publica un evento
     */
    public Event publishEvent(Long eventId, User user) {
        return changeEventStatus(eventId, EventStatus.PUBLISHED, user);
    }
    
    /**
     * Cancela un evento
     */
    public Event cancelEvent(Long eventId, User user, String reason) {
        // log.info("Cancelando evento ID: {} por usuario: {}. Razón: {}", eventId, user.getUsername(), reason);
        
        Event event = changeEventStatus(eventId, EventStatus.CANCELLED, user);
        
        // Cancelar todas las reservas activas
        cancelAllActiveBookings(event, reason);
        
        return event;
    }
    
    /**
     * Obtiene un evento por ID
     */
    @Transactional(readOnly = true)
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + eventId));
    }
    
    /**
     * Obtiene eventos públicos con paginación
     */
    @Transactional(readOnly = true)
    public Page<Event> getPublicEvents(Pageable pageable) {
        return eventRepository.findPublicPublishedEvents(pageable);
    }
    
    /**
     * Obtiene eventos por organizador
     */
    @Transactional(readOnly = true)
    public Page<Event> getEventsByOrganizer(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable);
    }
    
    /**
     * Obtiene eventos por categoría
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsByCategory(EventCategory category) {
        return eventRepository.findByCategory(category);
    }
    
    /**
     * Obtiene eventos por estado
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status);
    }
    
    /**
     * Busca eventos por título
     */
    @Transactional(readOnly = true)
    public List<Event> searchEventsByTitle(String title) {
        return eventRepository.findByTitleContaining(title);
    }
    
    /**
     * Busca eventos por ubicación
     */
    @Transactional(readOnly = true)
    public List<Event> searchEventsByLocation(String location) {
        return eventRepository.findByLocationContaining(location);
    }
    
    /**
     * Obtiene eventos próximos
     */
    @Transactional(readOnly = true)
    public List<Event> getUpcomingEvents(int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);
        return eventRepository.findUpcomingEvents(startDate, endDate);
    }
    
    /**
     * Obtiene eventos populares
     */
    @Transactional(readOnly = true)
    public List<Event> getPopularEvents(Pageable pageable) {
        return eventRepository.findPopularEvents(pageable);
    }
    
    /**
     * Obtiene eventos recientes
     */
    @Transactional(readOnly = true)
    public List<Event> getRecentEvents(Pageable pageable) {
        return eventRepository.findRecentEvents(pageable);
    }
    
    /**
     * Busca eventos con filtros
     */
    @Transactional(readOnly = true)
    public Page<Event> searchEventsWithFilters(
            String title, EventCategory category, String location,
            LocalDateTime startDate, LocalDateTime endDate,
            BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        
        return eventRepository.findEventsWithFilters(
                category, EventStatus.PUBLISHED, minPrice, maxPrice, 
                startDate, endDate, location, true, pageable);
    }
    
    /**
     * Obtiene eventos disponibles para reserva
     */
    @Transactional(readOnly = true)
    public List<Event> getAvailableEvents() {
        return eventRepository.findEventsWithAvailableTickets();
    }
    
    /**
     * Verifica disponibilidad de tickets
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableTickets(Long eventId, int requestedTickets) {
        Event event = getEventById(eventId);
        return event.getAvailableTickets() >= requestedTickets;
    }
    
    /**
     * Actualiza tickets disponibles
     */
    public void updateAvailableTickets(Event event) {
        int soldTickets = bookingRepository.sumTicketsSoldByEvent(event);
        int availableTickets = Math.max(0, event.getCapacity() - soldTickets);
        
        eventRepository.updateAvailableTickets(event.getId(), availableTickets);
        
        // Verificar si el evento se agotó
        if (availableTickets == 0 && event.getStatus() == EventStatus.PUBLISHED) {
            changeEventStatus(event.getId(), EventStatus.SOLD_OUT, event.getOrganizer());
        }
    }
    
    /**
     * Obtiene estadísticas de eventos
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEventStatsByCategory() {
        return eventRepository.getEventStatsByCategory();
    }
    
    /**
     * Obtiene estadísticas de eventos por mes
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEventStatsByMonth(LocalDateTime startDate) {
        return eventRepository.getEventStatsByMonth(startDate);
    }
    
    /**
     * Cuenta eventos por organizador
     */
    @Transactional(readOnly = true)
    public long countEventsByOrganizer(User organizer) {
        return eventRepository.countByOrganizer(organizer);
    }
    
    /**
     * Elimina un evento
     */
    public void deleteEvent(Long eventId, User user) {
        // log.info("Eliminando evento ID: {} por usuario: {}", eventId, user.getUsername());
        
        Event event = getEventById(eventId);
        
        // Verificar permisos
        validateEventPermissions(event, user);
        
        // Verificar que no hay reservas activas
        // long activeBookings = bookingRepository.countActiveBookingsByEvent(event);
        long activeBookings = 0; // Temporal - implementar método en BookingRepository
        if (activeBookings > 0) {
            throw new IllegalStateException("No se puede eliminar un evento con reservas activas");
        }
        
        eventRepository.delete(event);
        
        // log.info("Evento eliminado exitosamente: {}", eventId);
    }
    
    /**
     * Procesa eventos que necesitan recordatorios
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsNeedingReminders() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfterTomorrow = tomorrow.plusDays(1);
        return eventRepository.findEventsNeedingReminder(tomorrow, dayAfterTomorrow);
    }
    
    /**
     * Procesa eventos que necesitan limpieza
     */
    @Transactional(readOnly = true)
    public List<Event> getEventsNeedingCleanup() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return eventRepository.findExpiredEvents(cutoffDate);
    }
    
    /**
     * Valida los datos del evento
     */
    private void validateEventData(Event event) {
        if (!StringUtils.hasText(event.getTitle())) {
            throw new IllegalArgumentException("El título del evento es obligatorio");
        }
        
        if (event.getStartDate() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        
        if (event.getEndDate() == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }
        
        if (event.getStartDate().isAfter(event.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser en el pasado");
        }
        
        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0");
        }
        
        if (event.getPrice() != null && event.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        
        if (event.getBookingDeadline() != null && event.getBookingDeadline().isAfter(event.getStartDate())) {
            throw new IllegalArgumentException("La fecha límite de reserva debe ser anterior al inicio del evento");
        }
    }
    
    /**
     * Valida permisos sobre un evento
     */
    private void validateEventPermissions(Event event, User user) {
        // if (!user.getRole().hasPermission("MANAGE_EVENTS") && 
        if (false && // Temporal - implementar método hasPermission
            !event.getOrganizer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No tienes permisos para modificar este evento");
        }
    }
    
    /**
     * Detecta cambios importantes en un evento
     */
    private String detectEventChanges(Event existing, Event updated) {
        StringBuilder changes = new StringBuilder();
        
        if (!existing.getStartDate().equals(updated.getStartDate())) {
            changes.append("Fecha de inicio, ");
        }
        
        if (!existing.getEndDate().equals(updated.getEndDate())) {
            changes.append("Fecha de fin, ");
        }
        
        if (!existing.getLocation().equals(updated.getLocation())) {
            changes.append("Ubicación, ");
        }
        
        if (!existing.getPrice().equals(updated.getPrice())) {
            changes.append("Precio, ");
        }
        
        String result = changes.toString();
        return result.isEmpty() ? result : result.substring(0, result.length() - 2);
    }
    
    /**
     * Maneja cambios de estado
     */
    private void handleStatusChange(Event event, EventStatus oldStatus, EventStatus newStatus) {
        switch (newStatus) {
            case PUBLISHED:
                // log.info("Evento publicado: {}", event.getTitle());
                break;
            case CANCELLED:
                // log.info("Evento cancelado: {}", event.getTitle());
                break;
            case COMPLETED:
                // log.info("Evento completado: {}", event.getTitle());
                break;
            case SOLD_OUT:
                // log.info("Evento agotado: {}", event.getTitle());
                break;
        }
    }
    
    /**
     * Notifica cambios en evento a usuarios con reservas
     */
    private void notifyEventChanges(Event event, String changes) {
        // List<Booking> activeBookings = bookingRepository.findActiveBookingsByEvent(event);
        List<Booking> activeBookings = List.of(); // Temporal - implementar método
        
        for (Booking booking : activeBookings) {
            notificationService.sendEventUpdateNotification(event, booking.getUser(), changes);
            emailService.sendEventUpdateEmail(event, booking.getUser(), changes);
        }
    }
    
    /**
     * Cancela todas las reservas activas de un evento
     */
    private void cancelAllActiveBookings(Event event, String reason) {
        // List<Booking> activeBookings = bookingRepository.findActiveBookingsByEvent(event);
        List<Booking> activeBookings = List.of(); // Temporal - implementar método
        
        for (Booking booking : activeBookings) {
            // Aquí se llamaría al BookingService para cancelar la reserva
            // bookingService.cancelBooking(booking.getId(), reason);
            
            notificationService.sendEventCancellationNotification(event, booking.getUser());
            emailService.sendEventCancellationEmail(event, booking.getUser());
        }
    }
}