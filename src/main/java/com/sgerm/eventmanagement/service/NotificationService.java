package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.*;
import com.sgerm.eventmanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de notificaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    /**
     * Crea una nueva notificación
     */
    public Notification createNotification(Notification notification) {
        // log.info("Creando nueva notificación para usuario: {}", notification.getRecipient().getUsername());
        
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setEmailSent(false);
        notification.setPushSent(false);
        
        // Establecer fecha de expiración si no se especifica
        if (notification.getExpiresAt() == null) {
            notification.setExpiresAt(calculateExpirationDate(notification.getPriority()));
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Enviar notificación por email si es necesario
        if (shouldSendEmail(notification.getType())) {
            sendEmailNotification(savedNotification);
        }
        
        // Enviar notificación push si es necesario
        if (shouldSendPush(notification.getType())) {
            sendPushNotification(savedNotification);
        }
        
        // log.info("Notificación creada exitosamente con ID: {}", savedNotification.getId());
        return savedNotification;
    }
    
    /**
     * Envía notificación de confirmación de reserva
     */
    @Async
    public void sendBookingConfirmationNotification(Booking booking) {
        // log.info("Enviando notificación de confirmación de reserva: {}", booking.getBookingCode());
        
        Notification notification = new Notification(
                "Reserva confirmada",
                String.format("Tu reserva para '%s' ha sido confirmada. Código: %s", 
                        booking.getEvent().getTitle(), booking.getBookingCode()),
                NotificationType.BOOKING_CONFIRMATION,
                NotificationPriority.HIGH,
                booking.getUser(),
                null);
        notification.setRelatedEvent(booking.getEvent());
        notification.setRelatedBooking(booking);
        notification.setActionUrl("/bookings/" + booking.getId());
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de cancelación de reserva
     */
    @Async
    public void sendBookingCancellationNotification(Booking booking) {
        // log.info("Enviando notificación de cancelación de reserva: {}", booking.getBookingCode());
        
        Notification notification = new Notification(
                "Reserva cancelada",
                String.format("Tu reserva para '%s' ha sido cancelada. Código: %s", 
                        booking.getEvent().getTitle(), booking.getBookingCode()),
                NotificationType.BOOKING_CANCELLATION,
                NotificationPriority.NORMAL,
                booking.getUser(),
                null);
        notification.setRelatedEvent(booking.getEvent());
        notification.setRelatedBooking(booking);
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de recordatorio de evento
     */
    @Async
    public void sendEventReminderNotification(Booking booking) {
        // log.info("Enviando notificación de recordatorio para evento: {}", booking.getEvent().getTitle());
        
        Notification notification = new Notification(
                "Recordatorio de evento",
                String.format("No olvides que mañana tienes el evento '%s'. ¡Te esperamos!", 
                        booking.getEvent().getTitle()),
                NotificationType.EVENT_REMINDER,
                NotificationPriority.HIGH,
                booking.getUser(),
                null);
        notification.setRelatedEvent(booking.getEvent());
        notification.setRelatedBooking(booking);
        notification.setActionUrl("/events/" + booking.getEvent().getId());
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de confirmación de pago
     */
    @Async
    public void sendPaymentConfirmationNotification(Booking booking) {
        // log.info("Enviando notificación de confirmación de pago para reserva: {}", booking.getBookingCode());
        
        Notification notification = new Notification(
                "Pago confirmado",
                String.format("Tu pago de $%.2f para '%s' ha sido procesado exitosamente.", 
                        booking.getTotalPrice(), booking.getEvent().getTitle()),
                NotificationType.PAYMENT_CONFIRMATION,
                NotificationPriority.HIGH,
                booking.getUser(),
                null);
        notification.setRelatedEvent(booking.getEvent());
        notification.setRelatedBooking(booking);
        notification.setActionUrl("/bookings/" + booking.getId());
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de cambio de contraseña
     */
    @Async
    public void sendPasswordChangeNotification(User user) {
        // log.info("Enviando notificación de cambio de contraseña para usuario: {}", user.getUsername());
        
        Notification notification = new Notification(
                "Contraseña actualizada",
                "Tu contraseña ha sido cambiada exitosamente. Si no fuiste tú, contacta al soporte.",
                NotificationType.SECURITY_ALERT,
                NotificationPriority.HIGH,
                user,
                null);
        notification.setActionUrl("/profile/security");
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de desactivación de cuenta
     */
    @Async
    public void sendAccountDeactivationNotification(User user) {
        // log.info("Enviando notificación de desactivación de cuenta para usuario: {}", user.getUsername());
        
        Notification notification = new Notification(
                "Cuenta desactivada",
                "Tu cuenta ha sido desactivada. Contacta al soporte si crees que es un error.",
                NotificationType.SECURITY_ALERT,
                NotificationPriority.URGENT,
                user,
                null);
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de cambio de rol
     */
    @Async
    public void sendRoleChangeNotification(User user, Role oldRole, Role newRole) {
        // log.info("Enviando notificación de cambio de rol para usuario: {} de {} a {}", 
        //        user.getUsername(), oldRole, newRole);
        
        Notification notification = new Notification(
                "Rol actualizado",
                String.format("Tu rol ha sido cambiado de %s a %s.", 
                        oldRole.getDisplayName(), newRole.getDisplayName()),
                NotificationType.SECURITY_ALERT,
                NotificationPriority.HIGH,
                user,
                null);
        notification.setActionUrl("/profile");
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de evento cancelado
     */
    @Async
    public void sendEventCancellationNotification(Event event, User user) {
        // log.info("Enviando notificación de cancelación de evento: {} a usuario: {}", 
        //        event.getTitle(), user.getUsername());
        
        Notification notification = new Notification(
                "Evento cancelado",
                String.format("El evento '%s' ha sido cancelado. Se procesará el reembolso automáticamente.", 
                        event.getTitle()),
                NotificationType.EVENT_CANCELLATION,
                NotificationPriority.URGENT,
                user,
                null);
        notification.setRelatedEvent(event);
        notification.setActionUrl("/events/" + event.getId());
        
        createNotification(notification);
    }
    
    /**
     * Envía notificación de actualización de evento
     */
    @Async
    public void sendEventUpdateNotification(Event event, User user, String changes) {
        // log.info("Enviando notificación de actualización de evento: {} a usuario: {}", 
        //        event.getTitle(), user.getUsername());
        
        Notification notification = new Notification(
                "Evento actualizado",
                String.format("El evento '%s' ha sido actualizado. Cambios: %s", 
                        event.getTitle(), changes),
                NotificationType.EVENT_UPDATE,
                NotificationPriority.NORMAL,
                user,
                null);
        notification.setRelatedEvent(event);
        notification.setActionUrl("/events/" + event.getId());
        
        createNotification(notification);
    }
    
    /**
     * Marca una notificación como leída
     */
    public void markAsRead(Long notificationId, User user) {
        // log.info("Marcando notificación {} como leída por usuario: {}", notificationId, user.getUsername());
        
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            
            // Verificar que la notificación pertenece al usuario
            if (!notification.getRecipient().getId().equals(user.getId())) {
                throw new IllegalArgumentException("No tienes permiso para marcar esta notificación como leída");
            }
            
            if (!notification.getIsRead()) {
                notificationRepository.markAsRead(notificationId, LocalDateTime.now());
                // log.info("Notificación {} marcada como leída exitosamente", notificationId);
            }
        }
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    public void markAllAsRead(User user) {
        // log.info("Marcando todas las notificaciones como leídas para usuario: {}", user.getUsername());
        
        notificationRepository.markAllAsReadByRecipient(user, LocalDateTime.now());
        
        // log.info("Todas las notificaciones marcadas como leídas para usuario: {}", user.getUsername());
    }
    
    /**
     * Obtiene notificaciones de un usuario
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByRecipient(user, pageable);
    }
    
    /**
     * Obtiene notificaciones no leídas de un usuario
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findUnreadByRecipient(user);
    }
    
    /**
     * Cuenta notificaciones no leídas de un usuario
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(User user) {
        return notificationRepository.countUnreadByRecipient(user);
    }
    
    /**
     * Obtiene notificaciones recientes de un usuario
     */
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(User user, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return notificationRepository.findRecentByRecipient(user, since);
    }
    
    /**
     * Obtiene notificaciones de alta prioridad no leídas
     */
    @Transactional(readOnly = true)
    public List<Notification> getHighPriorityUnreadNotifications(User user) {
        return notificationRepository.findHighPriorityUnreadByRecipient(user);
    }
    
    /**
     * Elimina notificaciones expiradas
     */
    @Async
    public void cleanupExpiredNotifications() {
        // log.info("Iniciando limpieza de notificaciones expiradas");
        
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.deleteExpiredNotifications(now);
        
        // log.info("Limpieza de notificaciones expiradas completada");
    }
    
    /**
     * Elimina notificaciones leídas antiguas
     */
    @Async
    public void cleanupOldReadNotifications(int daysOld) {
        // log.info("Iniciando limpieza de notificaciones leídas antiguas (más de {} días)", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldReadNotifications(cutoffDate);
        
        // log.info("Limpieza de notificaciones leídas antiguas completada");
    }
    
    /**
     * Envía notificación por email
     */
    @Async
    private void sendEmailNotification(Notification notification) {
        try {
            // Aquí se podría personalizar el email según el tipo de notificación
            emailService.sendSimpleEmail(
                notification.getRecipient().getEmail(),
                notification.getTitle(),
                notification.getMessage()
            );
            
            // Marcar como enviado
            notificationRepository.updateEmailSentStatus(
                notification.getId(), true, LocalDateTime.now()
            );
            
            // log.info("Email de notificación enviado exitosamente para notificación ID: {}", 
            //        notification.getId());
        } catch (Exception e) {
            // log.error("Error enviando email de notificación ID: {}", notification.getId(), e);
        }
    }
    
    /**
     * Envía notificación push
     */
    @Async
    private void sendPushNotification(Notification notification) {
        try {
            // Aquí se implementaría el envío de notificaciones push
            // Por ahora solo simulamos el envío
            // log.info("Enviando notificación push para notificación ID: {}", notification.getId());
            
            // Marcar como enviado
            notificationRepository.updatePushSentStatus(
                notification.getId(), true, LocalDateTime.now()
            );
            
            // log.info("Notificación push enviada exitosamente para notificación ID: {}", 
            //        notification.getId());
        } catch (Exception e) {
            // log.error("Error enviando notificación push ID: {}", notification.getId(), e);
        }
    }
    
    /**
     * Determina si se debe enviar email para un tipo de notificación
     */
    private boolean shouldSendEmail(NotificationType type) {
        // Simplificado: enviar email para todos los tipos
        return true;
    }
    
    /**
     * Determina si se debe enviar push para un tipo de notificación
     */
    private boolean shouldSendPush(NotificationType type) {
        // Simplificado: enviar push para todos los tipos
        return true;
    }
    
    /**
     * Calcula la fecha de expiración basada en la prioridad
     */
    private LocalDateTime calculateExpirationDate(NotificationPriority priority) {
        return LocalDateTime.now().plusHours(priority.getExpirationHours());
    }
    
    /**
     * Obtiene estadísticas de notificaciones
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNotificationStatsByType() {
        return notificationRepository.getNotificationStatsByType();
    }
    
    /**
     * Obtiene estadísticas de notificaciones por prioridad
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNotificationStatsByPriority() {
        return notificationRepository.getNotificationStatsByPriority();
    }
    
    /**
     * Reenvía notificaciones fallidas
     */
    @Async
    public void retryFailedNotifications() {
        // log.info("Iniciando reenvío de notificaciones fallidas");
        
        LocalDateTime retryAfter = LocalDateTime.now().minusHours(1);
        // Simplificado: usar todos los tipos de notificación
        List<NotificationType> emailTypes = Arrays.asList(NotificationType.values());
        List<NotificationType> pushTypes = Arrays.asList(NotificationType.values());
        
        List<Notification> failedNotifications = notificationRepository
                .findNotificationsForRetry(emailTypes, pushTypes, retryAfter);
        
        for (Notification notification : failedNotifications) {
            if (!notification.getEmailSent() && shouldSendEmail(notification.getType())) {
                sendEmailNotification(notification);
            }
            
            if (!notification.getPushSent() && shouldSendPush(notification.getType())) {
                sendPushNotification(notification);
            }
        }
        
        // log.info("Reenvío de notificaciones fallidas completado. Procesadas: {}", 
        //        failedNotifications.size());
    }
}