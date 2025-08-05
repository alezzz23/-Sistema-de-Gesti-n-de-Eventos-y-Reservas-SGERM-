package com.sgerm.eventmanagement.repository;

import com.sgerm.eventmanagement.model.Booking;
import com.sgerm.eventmanagement.model.Event;
import com.sgerm.eventmanagement.model.Notification;
import com.sgerm.eventmanagement.model.NotificationPriority;
import com.sgerm.eventmanagement.model.NotificationType;
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
 * Repositorio para la entidad Notification
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Busca notificaciones por destinatario
     */
    List<Notification> findByRecipient(User recipient);
    
    /**
     * Busca notificaciones por destinatario con paginación
     */
    Page<Notification> findByRecipient(User recipient, Pageable pageable);
    
    /**
     * Busca notificaciones por remitente
     */
    List<Notification> findBySender(User sender);
    
    /**
     * Busca notificaciones por tipo
     */
    List<Notification> findByType(NotificationType type);
    
    /**
     * Busca notificaciones por prioridad
     */
    List<Notification> findByPriority(NotificationPriority priority);
    
    /**
     * Busca notificaciones por evento relacionado
     */
    List<Notification> findByRelatedEvent(Event event);
    
    /**
     * Busca notificaciones por reserva relacionada
     */
    List<Notification> findByRelatedBooking(Booking booking);
    
    /**
     * Busca notificaciones no leídas por destinatario
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    List<Notification> findUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Busca notificaciones no leídas por destinatario con paginación
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    Page<Notification> findUnreadByRecipient(@Param("recipient") User recipient, Pageable pageable);
    
    /**
     * Busca notificaciones leídas por destinatario
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = true")
    List<Notification> findReadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Busca notificaciones por destinatario y tipo
     */
    List<Notification> findByRecipientAndType(User recipient, NotificationType type);
    
    /**
     * Busca notificaciones por destinatario y prioridad
     */
    List<Notification> findByRecipientAndPriority(User recipient, NotificationPriority priority);
    
    /**
     * Busca notificaciones por destinatario, tipo y estado de lectura
     */
    List<Notification> findByRecipientAndTypeAndIsRead(User recipient, NotificationType type, boolean isRead);
    
    /**
     * Busca notificaciones por rango de fechas
     */
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca notificaciones por título (búsqueda parcial)
     */
    @Query("SELECT n FROM Notification n WHERE LOWER(n.title) LIKE LOWER('%' || :title || '%')")
    List<Notification> findByTitleContaining(@Param("title") String title);
    
    /**
     * Busca notificaciones por mensaje (búsqueda parcial)
     */
    @Query("SELECT n FROM Notification n WHERE LOWER(n.message) LIKE LOWER('%' || :message || '%')")
    List<Notification> findByMessageContaining(@Param("message") String message);
    
    /**
     * Busca notificaciones expiradas
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Busca notificaciones próximas a expirar
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL " +
           "AND n.expiresAt BETWEEN :now AND :futureDate")
    List<Notification> findNotificationsExpiringBetween(@Param("now") LocalDateTime now,
                                                       @Param("futureDate") LocalDateTime futureDate);
    
    /**
     * Busca notificaciones pendientes de envío por email
     */
    @Query("SELECT n FROM Notification n WHERE n.emailSent = false AND n.type IN :emailTypes")
    List<Notification> findPendingEmailNotifications(@Param("emailTypes") List<NotificationType> emailTypes);
    
    /**
     * Busca notificaciones pendientes de envío push
     */
    @Query("SELECT n FROM Notification n WHERE n.pushSent = false AND n.type IN :pushTypes")
    List<Notification> findPendingPushNotifications(@Param("pushTypes") List<NotificationType> pushTypes);
    
    /**
     * Busca notificaciones por múltiples tipos
     */
    @Query("SELECT n FROM Notification n WHERE n.type IN :types")
    List<Notification> findByTypeIn(@Param("types") List<NotificationType> types);
    
    /**
     * Busca notificaciones por múltiples prioridades
     */
    @Query("SELECT n FROM Notification n WHERE n.priority IN :priorities")
    List<Notification> findByPriorityIn(@Param("priorities") List<NotificationPriority> priorities);
    
    /**
     * Busca notificaciones de alta prioridad no leídas
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient " +
           "AND n.isRead = false AND n.priority IN ('HIGH', 'URGENT', 'CRITICAL')")
    List<Notification> findHighPriorityUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Busca notificaciones recientes por destinatario
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient " +
           "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByRecipient(@Param("recipient") User recipient,
                                           @Param("since") LocalDateTime since);
    
    /**
     * Busca notificaciones del sistema (sin remitente)
     */
    @Query("SELECT n FROM Notification n WHERE n.sender IS NULL")
    List<Notification> findSystemNotifications();
    
    /**
     * Busca notificaciones de usuario (con remitente)
     */
    @Query("SELECT n FROM Notification n WHERE n.sender IS NOT NULL")
    List<Notification> findUserNotifications();
    
    /**
     * Cuenta notificaciones no leídas por destinatario
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    long countUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Cuenta notificaciones por tipo
     */
    long countByType(NotificationType type);
    
    /**
     * Cuenta notificaciones por prioridad
     */
    long countByPriority(NotificationPriority priority);
    
    /**
     * Cuenta notificaciones por destinatario y tipo
     */
    long countByRecipientAndType(User recipient, NotificationType type);
    
    /**
     * Cuenta notificaciones de alta prioridad no leídas por destinatario
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient " +
           "AND n.isRead = false AND n.priority IN ('HIGH', 'URGENT', 'CRITICAL')")
    long countHighPriorityUnreadByRecipient(@Param("recipient") User recipient);
    
    /**
     * Busca notificaciones más recientes por destinatario
     */
    List<Notification> findTop10ByRecipientOrderByCreatedAtDesc(User recipient);
    
    /**
     * Busca notificaciones más recientes del sistema
     */
    @Query("SELECT n FROM Notification n WHERE n.sender IS NULL " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentSystemNotifications(Pageable pageable);
    
    /**
     * Busca notificaciones por evento y tipo
     */
    List<Notification> findByRelatedEventAndType(Event event, NotificationType type);
    
    /**
     * Busca notificaciones por reserva y tipo
     */
    List<Notification> findByRelatedBookingAndType(Booking booking, NotificationType type);
    
    /**
     * Busca notificaciones con URL de acción
     */
    @Query("SELECT n FROM Notification n WHERE n.actionUrl IS NOT NULL")
    List<Notification> findNotificationsWithAction();
    
    /**
     * Busca notificaciones sin URL de acción
     */
    @Query("SELECT n FROM Notification n WHERE n.actionUrl IS NULL")
    List<Notification> findNotificationsWithoutAction();
    
    /**
     * Marca una notificación como leída
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") User recipient, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Marca notificaciones por tipo como leídas
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.recipient = :recipient AND n.type = :type AND n.isRead = false")
    void markAsReadByRecipientAndType(@Param("recipient") User recipient,
                                     @Param("type") NotificationType type,
                                     @Param("readAt") LocalDateTime readAt);
    
    /**
     * Actualiza el estado de envío de email
     */
    @Modifying
    @Query("UPDATE Notification n SET n.emailSent = :sent, n.emailSentAt = :sentAt " +
           "WHERE n.id = :notificationId")
    void updateEmailSentStatus(@Param("notificationId") Long notificationId,
                              @Param("sent") boolean sent,
                              @Param("sentAt") LocalDateTime sentAt);
    
    /**
     * Actualiza el estado de envío push
     */
    @Modifying
    @Query("UPDATE Notification n SET n.pushSent = :sent, n.pushSentAt = :sentAt " +
           "WHERE n.id = :notificationId")
    void updatePushSentStatus(@Param("notificationId") Long notificationId,
                             @Param("sent") boolean sent,
                             @Param("sentAt") LocalDateTime sentAt);
    
    /**
     * Elimina notificaciones expiradas
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Elimina notificaciones leídas antiguas
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Busca notificaciones con filtros avanzados
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "(:recipientId IS NULL OR n.recipient.id = :recipientId) AND " +
           "(:senderId IS NULL OR n.sender.id = :senderId) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:eventId IS NULL OR n.relatedEvent.id = :eventId) AND " +
           "(:bookingId IS NULL OR n.relatedBooking.id = :bookingId) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate)")
    Page<Notification> findNotificationsWithFilters(@Param("recipientId") Long recipientId,
                                                   @Param("senderId") Long senderId,
                                                   @Param("type") NotificationType type,
                                                   @Param("priority") NotificationPriority priority,
                                                   @Param("isRead") Boolean isRead,
                                                   @Param("eventId") Long eventId,
                                                   @Param("bookingId") Long bookingId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);
    
    /**
     * Obtiene estadísticas de notificaciones por tipo
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> getNotificationStatsByType();
    
    /**
     * Obtiene estadísticas de notificaciones por prioridad
     */
    @Query("SELECT n.priority, COUNT(n) FROM Notification n GROUP BY n.priority")
    List<Object[]> getNotificationStatsByPriority();
    
    /**
     * Obtiene estadísticas de notificaciones por mes
     */
    @Query("SELECT n.createdAt, COUNT(n) " +
           "FROM Notification n " +
           "WHERE n.createdAt >= :startDate " +
           "GROUP BY n.createdAt " +
           "ORDER BY n.createdAt")
    List<Object[]> getNotificationStatsByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Obtiene estadísticas de lectura de notificaciones
     */
    @Query("SELECT n.isRead, COUNT(n) FROM Notification n GROUP BY n.isRead")
    List<Object[]> getNotificationReadStats();
    
    /**
     * Obtiene usuarios más activos en notificaciones
     */
    @Query("SELECT n.recipient, COUNT(n) FROM Notification n " +
           "GROUP BY n.recipient " +
           "ORDER BY COUNT(n) DESC")
    List<Object[]> getMostNotifiedUsers(Pageable pageable);
    
    /**
     * Obtiene tipos de notificación más enviados
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :startDate " +
           "GROUP BY n.type " +
           "ORDER BY COUNT(n) DESC")
    List<Object[]> getMostSentNotificationTypes(@Param("startDate") LocalDateTime startDate,
                                               Pageable pageable);
    
    /**
     * Busca notificaciones duplicadas
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "n.recipient = :recipient AND n.type = :type " +
           "AND n.relatedEvent = :event AND n.relatedBooking = :booking " +
           "AND n.createdAt >= :since")
    List<Notification> findDuplicateNotifications(@Param("recipient") User recipient,
                                                 @Param("type") NotificationType type,
                                                 @Param("event") Event event,
                                                 @Param("booking") Booking booking,
                                                 @Param("since") LocalDateTime since);
    
    /**
     * Busca notificaciones que requieren reenvío
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "((n.emailSent = false AND n.type IN :emailTypes) OR " +
           "(n.pushSent = false AND n.type IN :pushTypes)) AND " +
           "n.createdAt >= :retryAfter")
    List<Notification> findNotificationsForRetry(@Param("emailTypes") List<NotificationType> emailTypes,
                                                @Param("pushTypes") List<NotificationType> pushTypes,
                                                @Param("retryAfter") LocalDateTime retryAfter);
}