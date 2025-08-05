package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los estados de una reserva
 */
public enum BookingStatus {
    /**
     * Pendiente - Reserva creada pero no confirmada
     */
    PENDING("Pendiente", "Reserva creada, esperando confirmación de pago"),
    
    /**
     * Confirmada - Reserva pagada y confirmada
     */
    CONFIRMED("Confirmada", "Reserva confirmada y pagada"),
    
    /**
     * Cancelada - Reserva cancelada por el usuario
     */
    CANCELLED("Cancelada", "Reserva cancelada por el usuario"),
    
    /**
     * Expirada - Reserva expirada por falta de pago
     */
    EXPIRED("Expirada", "Reserva expirada por falta de pago en el tiempo límite"),
    
    /**
     * Reembolsada - Reserva cancelada con reembolso
     */
    REFUNDED("Reembolsada", "Reserva cancelada y reembolsada"),
    
    /**
     * Usada - El usuario ya asistió al evento
     */
    USED("Usada", "El usuario ya hizo check-in en el evento"),
    
    /**
     * No presentado - El usuario no asistió al evento
     */
    NO_SHOW("No Presentado", "El usuario no asistió al evento"),
    
    /**
     * En proceso de reembolso
     */
    REFUND_PENDING("Reembolso Pendiente", "Reembolso en proceso"),
    
    /**
     * Rechazada - Reserva rechazada por el organizador
     */
    REJECTED("Rechazada", "Reserva rechazada por el organizador");
    
    private final String displayName;
    private final String description;
    
    BookingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica si la reserva está activa (válida para el evento)
     */
    public boolean isActive() {
        return this == CONFIRMED || this == PENDING;
    }
    
    /**
     * Verifica si la reserva puede ser cancelada
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Verifica si la reserva puede ser reembolsada
     */
    public boolean isRefundable() {
        return this == CONFIRMED || this == CANCELLED;
    }
    
    /**
     * Verifica si la reserva está finalizada (no puede cambiar)
     */
    public boolean isFinal() {
        return this == USED || this == NO_SHOW || this == REFUNDED || 
               this == EXPIRED || this == REJECTED;
    }
    
    /**
     * Verifica si la reserva requiere pago
     */
    public boolean requiresPayment() {
        return this == PENDING;
    }
    
    /**
     * Verifica si la reserva permite check-in
     */
    public boolean allowsCheckIn() {
        return this == CONFIRMED;
    }
    
    /**
     * Obtiene el color CSS asociado al estado para la UI
     */
    public String getCssClass() {
        switch (this) {
            case PENDING:
                return "warning";
            case CONFIRMED:
                return "success";
            case CANCELLED:
            case REJECTED:
                return "danger";
            case EXPIRED:
                return "secondary";
            case REFUNDED:
            case REFUND_PENDING:
                return "info";
            case USED:
                return "primary";
            case NO_SHOW:
                return "dark";
            default:
                return "secondary";
        }
    }
    
    /**
     * Obtiene el icono asociado al estado
     */
    public String getIcon() {
        switch (this) {
            case PENDING:
                return "⏳";
            case CONFIRMED:
                return "✅";
            case CANCELLED:
                return "❌";
            case EXPIRED:
                return "⏰";
            case REFUNDED:
                return "💰";
            case USED:
                return "🎫";
            case NO_SHOW:
                return "👻";
            case REFUND_PENDING:
                return "🔄";
            case REJECTED:
                return "🚫";
            default:
                return "❓";
        }
    }
    
    /**
     * Obtiene los estados que pueden ser establecidos por usuarios
     */
    public static BookingStatus[] getUserSelectableStatuses() {
        return new BookingStatus[]{CANCELLED};
    }
    
    /**
     * Obtiene los estados que pueden ser establecidos por organizadores
     */
    public static BookingStatus[] getOrganizerSelectableStatuses() {
        return new BookingStatus[]{CONFIRMED, CANCELLED, REJECTED, REFUNDED};
    }
    
    /**
     * Obtiene los estados que pueden ser establecidos por el sistema
     */
    public static BookingStatus[] getSystemStatuses() {
        return new BookingStatus[]{EXPIRED, USED, NO_SHOW, REFUND_PENDING};
    }
    
    /**
     * Verifica si se puede cambiar de un estado a otro
     */
    public boolean canTransitionTo(BookingStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED || newStatus == CANCELLED || 
                       newStatus == EXPIRED || newStatus == REJECTED;
            case CONFIRMED:
                return newStatus == CANCELLED || newStatus == USED || 
                       newStatus == NO_SHOW || newStatus == REFUND_PENDING;
            case CANCELLED:
                return newStatus == REFUNDED || newStatus == REFUND_PENDING;
            case REFUND_PENDING:
                return newStatus == REFUNDED;
            case EXPIRED:
            case REFUNDED:
            case USED:
            case NO_SHOW:
            case REJECTED:
                return false; // Estados finales
            default:
                return false;
        }
    }
    
    /**
     * Obtiene el siguiente estado lógico basado en condiciones
     */
    public BookingStatus getNextLogicalStatus(boolean eventEnded, boolean userCheckedIn) {
        if (this == CONFIRMED && eventEnded) {
            return userCheckedIn ? USED : NO_SHOW;
        }
        return this;
    }
}