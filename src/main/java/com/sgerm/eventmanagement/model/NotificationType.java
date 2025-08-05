package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los tipos de notificaciones del sistema
 */
public enum NotificationType {
    /**
     * Confirmación de reserva
     */
    BOOKING_CONFIRMATION("Confirmación de Reserva", "Confirmación de una nueva reserva", "✅", true, true),
    
    /**
     * Cancelación de reserva
     */
    BOOKING_CANCELLATION("Cancelación de Reserva", "Notificación de cancelación de reserva", "❌", true, true),
    
    /**
     * Recordatorio de evento
     */
    EVENT_REMINDER("Recordatorio de Evento", "Recordatorio de evento próximo", "⏰", true, true),
    
    /**
     * Cambio en evento
     */
    EVENT_UPDATE("Actualización de Evento", "Cambios en la información del evento", "📝", true, true),
    
    /**
     * Cancelación de evento
     */
    EVENT_CANCELLATION("Cancelación de Evento", "El evento ha sido cancelado", "🚫", true, true),
    
    /**
     * Nuevo evento publicado
     */
    NEW_EVENT("Nuevo Evento", "Se ha publicado un nuevo evento", "🎉", false, true),
    
    /**
     * Confirmación de pago
     */
    PAYMENT_CONFIRMATION("Pago Confirmado", "Confirmación de pago recibido", "💳", true, true),
    
    /**
     * Fallo en el pago
     */
    PAYMENT_FAILED("Pago Fallido", "Error en el procesamiento del pago", "💸", true, true),
    
    /**
     * Reembolso procesado
     */
    REFUND_PROCESSED("Reembolso Procesado", "Su reembolso ha sido procesado", "💰", true, true),
    
    /**
     * Verificación de email
     */
    EMAIL_VERIFICATION("Verificación de Email", "Verificación de dirección de correo", "📧", true, false),
    
    /**
     * Restablecimiento de contraseña
     */
    PASSWORD_RESET("Restablecer Contraseña", "Solicitud de restablecimiento de contraseña", "🔑", true, false),
    
    /**
     * Bienvenida al sistema
     */
    WELCOME("Bienvenido", "Mensaje de bienvenida al sistema", "👋", true, false),
    
    /**
     * Notificación del sistema
     */
    SYSTEM_NOTIFICATION("Notificación del Sistema", "Notificación general del sistema", "🔔", false, true),
    
    /**
     * Mantenimiento programado
     */
    MAINTENANCE("Mantenimiento", "Notificación de mantenimiento programado", "🔧", true, true),
    
    /**
     * Promoción o oferta especial
     */
    PROMOTION("Promoción", "Ofertas y promociones especiales", "🎁", false, true),
    
    /**
     * Invitación a evento
     */
    EVENT_INVITATION("Invitación", "Invitación a participar en un evento", "💌", true, true),
    
    /**
     * Solicitud de feedback
     */
    FEEDBACK_REQUEST("Solicitud de Opinión", "Solicitud de feedback sobre un evento", "⭐", true, false),
    
    /**
     * Notificación de seguridad
     */
    SECURITY_ALERT("Alerta de Seguridad", "Notificación de seguridad importante", "🛡️", true, true),
    
    /**
     * Actualización de perfil
     */
    PROFILE_UPDATE("Perfil Actualizado", "Confirmación de actualización de perfil", "👤", false, false),
    
    /**
     * Notificación de chat o mensaje
     */
    MESSAGE("Mensaje", "Nuevo mensaje recibido", "💬", false, true),
    
    /**
     * Notificación de recurso
     */
    RESOURCE_UPDATE("Recurso Actualizado", "Cambios en recursos del evento", "📦", true, true),
    
    /**
     * Notificación de capacidad
     */
    CAPACITY_WARNING("Advertencia de Capacidad", "El evento está cerca de su capacidad máxima", "⚠️", true, true),
    
    /**
     * Evento agotado
     */
    SOLD_OUT("Evento Agotado", "El evento ha agotado todas sus entradas", "🎫", true, true),
    
    /**
     * Check-in exitoso
     */
    CHECK_IN_SUCCESS("Check-in Exitoso", "Check-in realizado correctamente", "✅", false, true),
    
    /**
     * Otros tipos de notificación
     */
    OTHER("Otros", "Otros tipos de notificación", "📋", false, false);
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final boolean sendEmail;
    private final boolean sendPush;
    
    NotificationType(String displayName, String description, String icon, boolean sendEmail, boolean sendPush) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.sendEmail = sendEmail;
        this.sendPush = sendPush;
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
    
    public boolean shouldSendEmail() {
        return sendEmail;
    }
    
    public boolean shouldSendPush() {
        return sendPush;
    }
    
    /**
     * Obtiene el nombre completo con icono
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
    
    /**
     * Verifica si es una notificación relacionada con eventos
     */
    public boolean isEventRelated() {
        return this == BOOKING_CONFIRMATION || this == BOOKING_CANCELLATION || 
               this == EVENT_REMINDER || this == EVENT_UPDATE || this == EVENT_CANCELLATION ||
               this == NEW_EVENT || this == EVENT_INVITATION || this == RESOURCE_UPDATE ||
               this == CAPACITY_WARNING || this == SOLD_OUT || this == CHECK_IN_SUCCESS;
    }
    
    /**
     * Verifica si es una notificación relacionada con pagos
     */
    public boolean isPaymentRelated() {
        return this == PAYMENT_CONFIRMATION || this == PAYMENT_FAILED || this == REFUND_PROCESSED;
    }
    
    /**
     * Verifica si es una notificación de seguridad
     */
    public boolean isSecurityRelated() {
        return this == EMAIL_VERIFICATION || this == PASSWORD_RESET || this == SECURITY_ALERT;
    }
    
    /**
     * Verifica si es una notificación del sistema
     */
    public boolean isSystemNotification() {
        return this == SYSTEM_NOTIFICATION || this == MAINTENANCE || this == WELCOME;
    }
    
    /**
     * Verifica si es una notificación promocional
     */
    public boolean isPromotional() {
        return this == PROMOTION || this == NEW_EVENT;
    }
    
    /**
     * Verifica si requiere acción del usuario
     */
    public boolean requiresUserAction() {
        return this == EMAIL_VERIFICATION || this == PASSWORD_RESET || 
               this == EVENT_INVITATION || this == FEEDBACK_REQUEST;
    }
    
    /**
     * Obtiene los tipos de notificación para eventos
     */
    public static NotificationType[] getEventTypes() {
        return new NotificationType[]{
            BOOKING_CONFIRMATION, BOOKING_CANCELLATION, EVENT_REMINDER, 
            EVENT_UPDATE, EVENT_CANCELLATION, NEW_EVENT, EVENT_INVITATION,
            RESOURCE_UPDATE, CAPACITY_WARNING, SOLD_OUT, CHECK_IN_SUCCESS
        };
    }
    
    /**
     * Obtiene los tipos de notificación para pagos
     */
    public static NotificationType[] getPaymentTypes() {
        return new NotificationType[]{
            PAYMENT_CONFIRMATION, PAYMENT_FAILED, REFUND_PROCESSED
        };
    }
    
    /**
     * Obtiene los tipos de notificación de seguridad
     */
    public static NotificationType[] getSecurityTypes() {
        return new NotificationType[]{
            EMAIL_VERIFICATION, PASSWORD_RESET, SECURITY_ALERT
        };
    }
    
    /**
     * Obtiene los tipos de notificación del sistema
     */
    public static NotificationType[] getSystemTypes() {
        return new NotificationType[]{
            SYSTEM_NOTIFICATION, MAINTENANCE, WELCOME
        };
    }
    
    /**
     * Obtiene los tipos de notificación promocionales
     */
    public static NotificationType[] getPromotionalTypes() {
        return new NotificationType[]{
            PROMOTION, NEW_EVENT
        };
    }
    
    /**
     * Obtiene un tipo de notificación por su nombre de visualización
     */
    public static NotificationType getByDisplayName(String displayName) {
        for (NotificationType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No se encontró el tipo de notificación: " + displayName);
    }
    
    /**
     * Obtiene los tipos que requieren email
     */
    public static NotificationType[] getEmailRequiredTypes() {
        return java.util.Arrays.stream(values())
                .filter(NotificationType::shouldSendEmail)
                .toArray(NotificationType[]::new);
    }
    
    /**
     * Obtiene los tipos que requieren push
     */
    public static NotificationType[] getPushRequiredTypes() {
        return java.util.Arrays.stream(values())
                .filter(NotificationType::shouldSendPush)
                .toArray(NotificationType[]::new);
    }
}