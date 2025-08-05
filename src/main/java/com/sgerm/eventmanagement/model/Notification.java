package com.sgerm.eventmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa las notificaciones del sistema
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_read", columnList = "is_read"),
    @Index(name = "idx_notification_created", columnList = "created_at"),
    @Index(name = "idx_notification_priority", columnList = "priority")
})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    @Column(nullable = false, length = 1000)
    private String message;
    
    @NotNull(message = "El tipo de notificación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_event_id")
    private Event relatedEvent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_booking_id")
    private Booking relatedBooking;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "action_text", length = 100)
    private String actionText;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;
    
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;
    
    @Column(name = "push_sent", nullable = false)
    private Boolean pushSent = false;
    
    @Column(name = "push_sent_at")
    private LocalDateTime pushSentAt;
    
    @Size(max = 500, message = "Los datos adicionales no pueden exceder 500 caracteres")
    @Column(name = "additional_data", length = 500)
    private String additionalData;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructores
    public Notification() {}
    
    public Notification(String title, String message, NotificationType type, User recipient) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }
    
    public Notification(String title, String message, NotificationType type, 
                       NotificationPriority priority, User recipient, User sender) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.recipient = recipient;
        this.sender = sender;
    }
    
    // Métodos de utilidad
    
    /**
     * Marca la notificación como leída
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Marca la notificación como no leída
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
    
    /**
     * Verifica si la notificación ha expirado
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Verifica si la notificación es urgente
     */
    public boolean isUrgent() {
        return priority == NotificationPriority.HIGH || priority == NotificationPriority.URGENT;
    }
    
    /**
     * Verifica si la notificación tiene una acción asociada
     */
    public boolean hasAction() {
        return actionUrl != null && !actionUrl.trim().isEmpty();
    }
    
    /**
     * Marca el email como enviado
     */
    public void markEmailSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }
    
    /**
     * Marca la notificación push como enviada
     */
    public void markPushSent() {
        this.pushSent = true;
        this.pushSentAt = LocalDateTime.now();
    }
    
    /**
     * Obtiene el icono según el tipo de notificación
     */
    public String getIcon() {
        return type.getIcon();
    }
    
    /**
     * Obtiene la clase CSS según la prioridad
     */
    public String getCssClass() {
        return priority.getCssClass();
    }
    
    /**
     * Verifica si debe enviarse por email
     */
    public boolean shouldSendEmail() {
        return !emailSent && (isUrgent() || type.shouldSendEmail());
    }
    
    /**
     * Verifica si debe enviarse notificación push
     */
    public boolean shouldSendPush() {
        return !pushSent && (isUrgent() || type.shouldSendPush());
    }
    
    /**
     * Obtiene el tiempo transcurrido desde la creación
     */
    public String getTimeAgo() {
        if (createdAt == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (minutes < 1) return "Ahora";
        if (minutes < 60) return minutes + " min";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " h";
        
        long days = hours / 24;
        if (days < 7) return days + " d";
        
        long weeks = days / 7;
        return weeks + " sem";
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public NotificationPriority getPriority() {
        return priority;
    }
    
    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }
    
    public User getRecipient() {
        return recipient;
    }
    
    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }
    
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
    }
    
    public Event getRelatedEvent() {
        return relatedEvent;
    }
    
    public void setRelatedEvent(Event relatedEvent) {
        this.relatedEvent = relatedEvent;
    }
    
    public Booking getRelatedBooking() {
        return relatedBooking;
    }
    
    public void setRelatedBooking(Booking relatedBooking) {
        this.relatedBooking = relatedBooking;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public String getActionText() {
        return actionText;
    }
    
    public void setActionText(String actionText) {
        this.actionText = actionText;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Boolean getEmailSent() {
        return emailSent;
    }
    
    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }
    
    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }
    
    public void setEmailSentAt(LocalDateTime emailSentAt) {
        this.emailSentAt = emailSentAt;
    }
    
    public Boolean getPushSent() {
        return pushSent;
    }
    
    public void setPushSent(Boolean pushSent) {
        this.pushSent = pushSent;
    }
    
    public LocalDateTime getPushSentAt() {
        return pushSentAt;
    }
    
    public void setPushSentAt(LocalDateTime pushSentAt) {
        this.pushSentAt = pushSentAt;
    }
    
    public String getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}