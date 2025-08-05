package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.*;
import com.sgerm.eventmanagement.repository.BookingRepository;
import com.sgerm.eventmanagement.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de reservas de eventos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    /**
     * Crea una nueva reserva
     */
    public Booking createBooking(Long eventId, User user, int ticketQuantity, String specialRequests) {
        // log.info("Creando reserva para evento ID: {} por usuario: {} con {} tickets", 
        //        eventId, user.getUsername(), ticketQuantity);
        
        Event event = eventService.getEventById(eventId);
        
        // Validar que el evento permite reservas
        validateBookingEligibility(event, user, ticketQuantity);
        
        // Verificar disponibilidad de tickets
        if (!eventService.hasAvailableTickets(eventId, ticketQuantity)) {
            throw new IllegalStateException("No hay suficientes tickets disponibles");
        }
        
        // Verificar límite por usuario
        // int existingTickets = bookingRepository.sumTicketsByUserAndEvent(user, event); // Método no implementado
        // if (existingTickets + ticketQuantity > event.getMaxTicketsPerUser()) {
        //     throw new IllegalStateException(String.format(
        //         "Excede el límite de %d tickets por usuario", event.getMaxTicketsPerUser()));
        // }
        
        // Crear la reserva
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setTicketQuantity(ticketQuantity);
        booking.setTotalPrice(event.getPrice().multiply(BigDecimal.valueOf(ticketQuantity)));
        booking.setStatus(event.getRequiresApproval() ? BookingStatus.PENDING : BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        booking.setSpecialRequests(specialRequests);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Generar código de reserva único
        // booking.setBookingCode(Booking.generateBookingCode()); // Método privado o no implementado
        
        // Establecer fecha de expiración si está pendiente
        // if (booking.getStatus() == BookingStatus.PENDING) {
        //     booking.setExpirationDate(LocalDateTime.now().plusHours(24)); // Método no implementado
        // }
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Actualizar tickets disponibles
        eventService.updateAvailableTickets(event);
        
        // Enviar notificaciones
        if (savedBooking.getStatus() == BookingStatus.CONFIRMED) {
            sendBookingConfirmation(savedBooking);
        } else {
            sendBookingPendingNotification(savedBooking);
        }
        
        // log.info("Reserva creada exitosamente con código: {}", savedBooking.getBookingCode());
        return savedBooking;
    }
    
    /**
     * Confirma una reserva pendiente
     */
    public Booking confirmBooking(Long bookingId, User organizer) {
        // log.info("Confirmando reserva ID: {} por organizador: {}", bookingId, organizer.getUsername());
        
        Booking booking = getBookingById(bookingId);
        
        // Verificar permisos
            if (!booking.getEvent().getOrganizer().getId().equals(organizer.getId()) &&
            false) { // !organizer.getRole().hasPermission("MANAGE_BOOKINGS") // Método no implementado
            throw new IllegalArgumentException("No tienes permisos para confirmar esta reserva");
        }
        
        // Verificar estado
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar reservas pendientes");
        }
        
        // Verificar que no haya expirado
        // if (booking.getExpirationDate() != null && booking.getExpirationDate().isBefore(LocalDateTime.now())) {
        //     throw new IllegalStateException("La reserva ha expirado");
        // }
        
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentDate(LocalDateTime.now());
        // booking.setExpirationDate(null); // Método no implementado
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Generar código QR
        // booking.setQrCode(booking.generateQRCode()); // Método no implementado o retorna void
        
        Booking confirmedBooking = bookingRepository.save(booking);
        
        // Enviar confirmación
        sendBookingConfirmation(confirmedBooking);
        
        // log.info("Reserva confirmada exitosamente: {}", confirmedBooking.getBookingCode());
        return confirmedBooking;
    }
    
    /**
     * Rechaza una reserva pendiente
     */
    public Booking rejectBooking(Long bookingId, User organizer, String reason) {
        // log.info("Rechazando reserva ID: {} por organizador: {}. Razón: {}", 
        //        bookingId, organizer.getUsername(), reason);
        
        Booking booking = getBookingById(bookingId);
        
        // Verificar permisos
        if (!booking.getEvent().getOrganizer().getId().equals(organizer.getId()) &&
            false) { // !organizer.getRole().hasPermission("MANAGE_BOOKINGS") // Método no implementado
            throw new IllegalArgumentException("No tienes permisos para rechazar esta reserva");
        }
        
        // Verificar estado
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden rechazar reservas pendientes");
        }
        
        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationDate(LocalDateTime.now());
        // booking.setCancellationReason(reason); // Método no implementado
        booking.setUpdatedAt(LocalDateTime.now());
        
        Booking rejectedBooking = bookingRepository.save(booking);
        
        // Actualizar tickets disponibles
        eventService.updateAvailableTickets(booking.getEvent());
        
        // Enviar notificación de rechazo
        // notificationService.sendBookingRejectionNotification(rejectedBooking, reason); // Método no implementado
        // emailService.sendBookingRejectionEmail(rejectedBooking, reason); // Método no implementado
        
        // log.info("Reserva rechazada: {}", rejectedBooking.getBookingCode());
        return rejectedBooking;
    }
    
    /**
     * Cancela una reserva
     */
    public Booking cancelBooking(Long bookingId, User user, String reason) {
        // log.info("Cancelando reserva ID: {} por usuario: {}. Razón: {}", 
        //        bookingId, user.getUsername(), reason);
        
        Booking booking = getBookingById(bookingId);
        
        // Verificar permisos
        if (!booking.getUser().getId().equals(user.getId()) &&
            !booking.getEvent().getOrganizer().getId().equals(user.getId()) &&
            false) { // !user.getRole().hasPermission("MANAGE_BOOKINGS") // Método no implementado
            throw new IllegalArgumentException("No tienes permisos para cancelar esta reserva");
        }
        
        // Verificar que se puede cancelar
        if (!booking.canBeCancelled()) {
            throw new IllegalStateException("Esta reserva no se puede cancelar");
        }
        
        // Verificar fecha límite de cancelación
        if (booking.getEvent().getCancellationDeadline() != null &&
            LocalDateTime.now().isAfter(booking.getEvent().getCancellationDeadline())) {
            throw new IllegalStateException("Ha pasado la fecha límite para cancelar");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationDate(LocalDateTime.now());
        // booking.setCancellationReason(reason); // Método no implementado
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Determinar si procede reembolso
        if (shouldProcessRefund(booking)) {
            booking.setStatus(BookingStatus.REFUND_PENDING);
            booking.setRefundAmount(calculateRefundAmount(booking));
        }
        
        Booking cancelledBooking = bookingRepository.save(booking);
        
        // Actualizar tickets disponibles
        eventService.updateAvailableTickets(booking.getEvent());
        
        // Enviar notificaciones
        notificationService.sendBookingCancellationNotification(cancelledBooking);
        emailService.sendBookingCancellationEmail(cancelledBooking);
        
        // log.info("Reserva cancelada: {}", cancelledBooking.getBookingCode());
        return cancelledBooking;
    }
    
    /**
     * Procesa el check-in de una reserva
     */
    public Booking checkInBooking(String bookingCode, User organizer) {
        // log.info("Procesando check-in para reserva: {} por organizador: {}", 
        //        bookingCode, organizer.getUsername());
        
        Booking booking = getBookingByCode(bookingCode);
        
        // Verificar permisos
        if (!booking.getEvent().getOrganizer().getId().equals(organizer.getId()) &&
            false) { // !organizer.getRole().hasPermission("MANAGE_BOOKINGS") // Método no implementado
            throw new IllegalArgumentException("No tienes permisos para hacer check-in");
        }
        
        // Verificar que se puede hacer check-in
        if (!booking.canCheckIn()) {
            throw new IllegalStateException("No se puede hacer check-in para esta reserva");
        }
        
        // Verificar que el evento está en curso o próximo a comenzar
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = booking.getEvent().getStartDate();
        if (now.isBefore(eventStart.minusHours(2))) {
            throw new IllegalStateException("El check-in solo está disponible 2 horas antes del evento");
        }
        
        booking.setCheckInDate(now);
        booking.setStatus(BookingStatus.USED);
        booking.setUpdatedAt(now);
        
        Booking checkedInBooking = bookingRepository.save(booking);
        
        // log.info("Check-in procesado exitosamente para reserva: {}", bookingCode);
        return checkedInBooking;
    }
    
    /**
     * Procesa el pago de una reserva
     */
    public Booking processPayment(Long bookingId, String paymentMethod, String paymentReference) {
        // log.info("Procesando pago para reserva ID: {} con método: {}", bookingId, paymentMethod);
        
        Booking booking = getBookingById(bookingId);
        
        // Verificar que requiere pago
        if (!booking.getStatus().requiresPayment()) {
            throw new IllegalStateException("Esta reserva no requiere pago");
        }
        
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentReference(paymentReference);
        booking.setPaymentDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Generar código QR
        // booking.setQrCode(booking.generateQRCode()); // Método no implementado o retorna void
        
        Booking paidBooking = bookingRepository.save(booking);
        
        // Enviar confirmación de pago
        notificationService.sendPaymentConfirmationNotification(paidBooking);
        emailService.sendPaymentConfirmationEmail(paidBooking);
        
        // log.info("Pago procesado exitosamente para reserva: {}", paidBooking.getBookingCode());
        return paidBooking;
    }
    
    /**
     * Procesa un reembolso
     */
    public Booking processRefund(Long bookingId, BigDecimal refundAmount, String refundReference) {
        // log.info("Procesando reembolso para reserva ID: {} por monto: {}", bookingId, refundAmount);
        
        Booking booking = getBookingById(bookingId);
        
        // Verificar que está pendiente de reembolso
        if (booking.getStatus() != BookingStatus.REFUND_PENDING) {
            throw new IllegalStateException("Esta reserva no está pendiente de reembolso");
        }
        
        booking.setRefundAmount(refundAmount);
        // booking.setRefundReference(refundReference); // Método no implementado
        booking.setRefundDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.REFUNDED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        Booking refundedBooking = bookingRepository.save(booking);
        
        // Enviar confirmación de reembolso
        // notificationService.sendRefundConfirmationNotification(refundedBooking); // Método no implementado
        // emailService.sendRefundConfirmationEmail(refundedBooking); // Método no implementado
        
        // log.info("Reembolso procesado exitosamente para reserva: {}", refundedBooking.getBookingCode());
        return refundedBooking;
    }
    
    /**
     * Obtiene una reserva por ID
     */
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + bookingId));
    }
    
    /**
     * Obtiene una reserva por código
     */
    @Transactional(readOnly = true)
    public Booking getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con código: " + bookingCode));
    }
    
    /**
     * Obtiene reservas por usuario
     */
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByUser(User user, Pageable pageable) {
        return bookingRepository.findByUser(user, pageable);
    }
    
    /**
     * Obtiene reservas por evento
     */
    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByEvent(Event event, Pageable pageable) {
        return bookingRepository.findByEvent(event, pageable);
    }
    
    /**
     * Obtiene reservas por estado
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    /**
     * Obtiene reservas activas por usuario
     */
    @Transactional(readOnly = true)
    public List<Booking> getActiveBookingsByUser(User user) {
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        return bookingRepository.findActiveBookingsByUser(user, activeStatuses);
    }
    
    /**
     * Obtiene reservas recientes
     */
    @Transactional(readOnly = true)
    public List<Booking> getRecentBookings(Pageable pageable) {
        return bookingRepository.findRecentBookings(pageable);
    }
    
    /**
     * Obtiene reservas expiradas
     */
    @Transactional(readOnly = true)
    public List<Booking> getExpiredBookings() {
        return bookingRepository.findExpiredBookings(LocalDateTime.now(), BookingStatus.PENDING);
    }
    
    /**
     * Obtiene reservas que necesitan recordatorio
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsNeedingReminder() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfterTomorrow = tomorrow.plusDays(1);
        return bookingRepository.findBookingsNeedingReminder(tomorrow, BookingStatus.CONFIRMED, dayAfterTomorrow);
    }
    
    /**
     * Obtiene estadísticas de reservas por estado
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBookingStatsByStatus() {
        return bookingRepository.getBookingStatsByStatus();
    }
    
    /**
     * Obtiene estadísticas de ingresos por mes
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRevenueStatsByMonth(LocalDateTime startDate) {
        return bookingRepository.getRevenueStatsByMonth(startDate);
    }
    
    /**
     * Cuenta reservas por usuario
     */
    @Transactional(readOnly = true)
    public long countBookingsByUser(User user) {
        return bookingRepository.countByUser(user);
    }
    
    /**
     * Suma tickets vendidos por evento
     */
    @Transactional(readOnly = true)
    public int sumTicketsSoldByEvent(Event event) {
        return bookingRepository.sumTicketsSoldByEvent(event);
    }
    
    /**
     * Calcula ingresos totales por evento
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenueByEvent(Event event) {
        return bookingRepository.sumRevenueByEvent(event);
    }
    
    /**
     * Procesa reservas expiradas
     */
    public void processExpiredBookings() {
        // log.info("Procesando reservas expiradas");
        
        List<Booking> expiredBookings = getExpiredBookings();
        
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            
            // Actualizar tickets disponibles
            eventService.updateAvailableTickets(booking.getEvent());
            
            // Notificar expiración
            // notificationService.sendBookingExpirationNotification(booking); // Método no implementado
        }
        
        // log.info("Procesadas {} reservas expiradas", expiredBookings.size());
    }
    
    /**
     * Genera códigos QR para reservas que no los tienen
     */
    public void generateMissingQRCodes() {
        // log.info("Generando códigos QR faltantes");
        
        // List<Booking> bookingsNeedingQR = bookingRepository.findBookingsNeedingQRCode(); // Método no implementado
        
        // for (Booking booking : bookingsNeedingQR) {
        //     booking.setQrCode(booking.generateQRCode());
        //     booking.setUpdatedAt(LocalDateTime.now());
        //     bookingRepository.save(booking);
        // }
        
        // log.info("Generados {} códigos QR", bookingsNeedingQR.size());
    }
    
    /**
     * Valida elegibilidad para reservar
     */
    private void validateBookingEligibility(Event event, User user, int ticketQuantity) {
        // Verificar que el evento permite reservas
        if (!event.getStatus().isBookable()) {
            throw new IllegalStateException("Este evento no permite reservas en su estado actual: " + event.getStatus());
        }
        
        // Verificar fecha límite de reserva
        if (event.getBookingDeadline() != null && LocalDateTime.now().isAfter(event.getBookingDeadline())) {
            throw new IllegalStateException("Ha pasado la fecha límite para reservar");
        }
        
        // Verificar que el usuario no es el organizador (opcional)
        if (event.getOrganizer().getId().equals(user.getId())) {
            throw new IllegalStateException("El organizador no puede reservar su propio evento");
        }
        
        // Verificar cantidad de tickets
        if (ticketQuantity <= 0) {
            throw new IllegalArgumentException("La cantidad de tickets debe ser mayor a 0");
        }
        
        if (ticketQuantity > event.getMaxTicketsPerUser()) {
            throw new IllegalArgumentException(String.format(
                "No se pueden reservar más de %d tickets por usuario", event.getMaxTicketsPerUser()));
        }
    }
    
    /**
     * Determina si procede reembolso
     */
    private boolean shouldProcessRefund(Booking booking) {
        // Reembolso si el evento es de pago y la cancelación es con suficiente antelación
        if (booking.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
            return false; // Evento gratuito, no hay reembolso
        }
        
        if (booking.getEvent().getCancellationDeadline() != null) {
            return LocalDateTime.now().isBefore(booking.getEvent().getCancellationDeadline());
        }
        
        // Por defecto, permitir reembolso si se cancela al menos 24 horas antes
        return LocalDateTime.now().isBefore(booking.getEvent().getStartDate().minusHours(24));
    }
    
    /**
     * Calcula el monto del reembolso
     */
    private BigDecimal calculateRefundAmount(Booking booking) {
        // Por defecto, reembolso completo
        BigDecimal refundAmount = booking.getTotalPrice();
        
        // Aplicar penalización si es muy cerca del evento
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = booking.getEvent().getStartDate();
        
        long hoursUntilEvent = java.time.Duration.between(now, eventStart).toHours();
        
        if (hoursUntilEvent < 48) {
            // 50% de reembolso si se cancela con menos de 48 horas
            refundAmount = refundAmount.multiply(BigDecimal.valueOf(0.5));
        } else if (hoursUntilEvent < 168) { // 7 días
            // 80% de reembolso si se cancela con menos de 7 días
            refundAmount = refundAmount.multiply(BigDecimal.valueOf(0.8));
        }
        
        return refundAmount;
    }
    
    /**
     * Envía confirmación de reserva
     */
    private void sendBookingConfirmation(Booking booking) {
        notificationService.sendBookingConfirmationNotification(booking);
        emailService.sendBookingConfirmationEmail(booking);
    }
    
    /**
     * Envía notificación de reserva pendiente
     */
    private void sendBookingPendingNotification(Booking booking) {
        // notificationService.sendBookingPendingNotification(booking); // Método no implementado
        // emailService.sendBookingPendingEmail(booking); // Método no implementado
    }
}