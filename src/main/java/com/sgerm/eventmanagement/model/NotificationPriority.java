package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define las prioridades de las notificaciones
 */
public enum NotificationPriority {
    /**
     * Prioridad baja - notificaciones informativas
     */
    LOW("Baja", "Notificaciones informativas de baja prioridad", "🔵", "info", 1),
    
    /**
     * Prioridad normal - notificaciones estándar
     */
    NORMAL("Normal", "Notificaciones estándar del sistema", "⚪", "secondary", 2),
    
    /**
     * Prioridad alta - notificaciones importantes
     */
    HIGH("Alta", "Notificaciones importantes que requieren atención", "🟡", "warning", 3),
    
    /**
     * Prioridad urgente - notificaciones críticas
     */
    URGENT("Urgente", "Notificaciones críticas que requieren acción inmediata", "🔴", "danger", 4),
    
    /**
     * Prioridad crítica - notificaciones de emergencia
     */
    CRITICAL("Crítica", "Notificaciones de emergencia del sistema", "🚨", "danger", 5);
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final String cssClass;
    private final int level;
    
    NotificationPriority(String displayName, String description, String icon, String cssClass, int level) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.cssClass = cssClass;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Obtiene el nombre completo con icono
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
    
    /**
     * Verifica si la prioridad es alta o superior
     */
    public boolean isHighPriority() {
        return level >= HIGH.level;
    }
    
    /**
     * Verifica si la prioridad es urgente o superior
     */
    public boolean isUrgent() {
        return level >= URGENT.level;
    }
    
    /**
     * Verifica si la prioridad es crítica
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }
    
    /**
     * Verifica si requiere notificación inmediata
     */
    public boolean requiresImmediateNotification() {
        return level >= URGENT.level;
    }
    
    /**
     * Verifica si debe enviarse por email automáticamente
     */
    public boolean shouldAutoSendEmail() {
        return level >= HIGH.level;
    }
    
    /**
     * Verifica si debe enviarse notificación push automáticamente
     */
    public boolean shouldAutoSendPush() {
        return level >= HIGH.level;
    }
    
    /**
     * Verifica si debe mostrarse como popup
     */
    public boolean shouldShowAsPopup() {
        return level >= URGENT.level;
    }
    
    /**
     * Verifica si debe reproducir sonido
     */
    public boolean shouldPlaySound() {
        return level >= HIGH.level;
    }
    
    /**
     * Obtiene el tiempo de expiración en horas según la prioridad
     */
    public int getExpirationHours() {
        switch (this) {
            case LOW:
                return 168; // 7 días
            case NORMAL:
                return 72;  // 3 días
            case HIGH:
                return 48;  // 2 días
            case URGENT:
                return 24;  // 1 día
            case CRITICAL:
                return 12;  // 12 horas
            default:
                return 72;
        }
    }
    
    /**
     * Obtiene el intervalo de reintento en minutos para notificaciones fallidas
     */
    public int getRetryIntervalMinutes() {
        switch (this) {
            case LOW:
                return 60;  // 1 hora
            case NORMAL:
                return 30;  // 30 minutos
            case HIGH:
                return 15;  // 15 minutos
            case URGENT:
                return 5;   // 5 minutos
            case CRITICAL:
                return 1;   // 1 minuto
            default:
                return 30;
        }
    }
    
    /**
     * Obtiene el número máximo de reintentos
     */
    public int getMaxRetries() {
        switch (this) {
            case LOW:
                return 2;
            case NORMAL:
                return 3;
            case HIGH:
                return 5;
            case URGENT:
                return 10;
            case CRITICAL:
                return 20;
            default:
                return 3;
        }
    }
    
    /**
     * Compara prioridades
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Compara prioridades
     */
    public boolean isLowerThan(NotificationPriority other) {
        return this.level < other.level;
    }
    
    /**
     * Compara prioridades
     */
    public boolean isEqualTo(NotificationPriority other) {
        return this.level == other.level;
    }
    
    /**
     * Obtiene las prioridades altas (HIGH, URGENT, CRITICAL)
     */
    public static NotificationPriority[] getHighPriorities() {
        return new NotificationPriority[]{HIGH, URGENT, CRITICAL};
    }
    
    /**
     * Obtiene las prioridades normales (LOW, NORMAL)
     */
    public static NotificationPriority[] getNormalPriorities() {
        return new NotificationPriority[]{LOW, NORMAL};
    }
    
    /**
     * Obtiene las prioridades urgentes (URGENT, CRITICAL)
     */
    public static NotificationPriority[] getUrgentPriorities() {
        return new NotificationPriority[]{URGENT, CRITICAL};
    }
    
    /**
     * Obtiene una prioridad por su nombre de visualización
     */
    public static NotificationPriority getByDisplayName(String displayName) {
        for (NotificationPriority priority : values()) {
            if (priority.displayName.equalsIgnoreCase(displayName)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("No se encontró la prioridad de notificación: " + displayName);
    }
    
    /**
     * Obtiene una prioridad por su nivel
     */
    public static NotificationPriority getByLevel(int level) {
        for (NotificationPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("No se encontró la prioridad con nivel: " + level);
    }
    
    /**
     * Obtiene la prioridad más alta entre dos
     */
    public static NotificationPriority getHigher(NotificationPriority p1, NotificationPriority p2) {
        return p1.level > p2.level ? p1 : p2;
    }
    
    /**
     * Obtiene la prioridad más baja entre dos
     */
    public static NotificationPriority getLower(NotificationPriority p1, NotificationPriority p2) {
        return p1.level < p2.level ? p1 : p2;
    }
    
    /**
     * Determina la prioridad basada en el tipo de notificación
     */
    public static NotificationPriority determinePriorityByType(NotificationType type) {
        switch (type) {
            case SECURITY_ALERT:
            case PAYMENT_FAILED:
            case EVENT_CANCELLATION:
                return URGENT;
            case BOOKING_CONFIRMATION:
            case PAYMENT_CONFIRMATION:
            case EVENT_REMINDER:
            case REFUND_PROCESSED:
                return HIGH;
            case EMAIL_VERIFICATION:
            case PASSWORD_RESET:
            case EVENT_UPDATE:
            case BOOKING_CANCELLATION:
                return NORMAL;
            case PROMOTION:
            case NEW_EVENT:
            case FEEDBACK_REQUEST:
                return LOW;
            case MAINTENANCE:
                return CRITICAL;
            default:
                return NORMAL;
        }
    }
    
    /**
     * Obtiene el color hexadecimal asociado a la prioridad
     */
    public String getHexColor() {
        switch (this) {
            case LOW:
                return "#17a2b8"; // info
            case NORMAL:
                return "#6c757d"; // secondary
            case HIGH:
                return "#ffc107"; // warning
            case URGENT:
            case CRITICAL:
                return "#dc3545"; // danger
            default:
                return "#6c757d";
        }
    }
}