package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los estados de un evento
 */
public enum EventStatus {
    /**
     * Borrador - El evento está siendo creado/editado
     */
    DRAFT("Borrador", "El evento está siendo creado o editado"),
    
    /**
     * Pendiente de aprobación - Esperando revisión
     */
    PENDING_APPROVAL("Pendiente de Aprobación", "El evento está esperando aprobación"),
    
    /**
     * Publicado - El evento está activo y visible
     */
    PUBLISHED("Publicado", "El evento está activo y disponible para reservas"),
    
    /**
     * Pausado - Temporalmente no disponible
     */
    PAUSED("Pausado", "El evento está temporalmente pausado"),
    
    /**
     * Cancelado - El evento ha sido cancelado
     */
    CANCELLED("Cancelado", "El evento ha sido cancelado"),
    
    /**
     * Completado - El evento ha finalizado
     */
    COMPLETED("Completado", "El evento ha finalizado exitosamente"),
    
    /**
     * Agotado - No hay más entradas disponibles
     */
    SOLD_OUT("Agotado", "No hay más entradas disponibles"),
    
    /**
     * Pospuesto - El evento ha sido pospuesto
     */
    POSTPONED("Pospuesto", "El evento ha sido pospuesto a una nueva fecha");
    
    private final String displayName;
    private final String description;
    
    EventStatus(String displayName, String description) {
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
     * Verifica si el evento está disponible para reservas
     */
    public boolean isBookable() {
        return this == PUBLISHED;
    }
    
    /**
     * Verifica si el evento está activo (visible para usuarios)
     */
    public boolean isActive() {
        return this == PUBLISHED || this == SOLD_OUT;
    }
    
    /**
     * Verifica si el evento puede ser editado
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING_APPROVAL || this == PAUSED;
    }
    
    /**
     * Verifica si el evento está finalizado
     */
    public boolean isFinalized() {
        return this == COMPLETED || this == CANCELLED;
    }
    
    /**
     * Obtiene los estados que pueden ser seleccionados por organizadores
     */
    public static EventStatus[] getOrganizerSelectableStatuses() {
        return new EventStatus[]{DRAFT, PUBLISHED, PAUSED, CANCELLED, POSTPONED};
    }
    
    /**
     * Obtiene los estados que requieren aprobación de administrador
     */
    public static EventStatus[] getAdminOnlyStatuses() {
        return new EventStatus[]{PENDING_APPROVAL};
    }
    
    /**
     * Verifica si se puede cambiar de un estado a otro
     */
    public boolean canTransitionTo(EventStatus newStatus) {
        switch (this) {
            case DRAFT:
                return newStatus == PENDING_APPROVAL || newStatus == PUBLISHED || newStatus == CANCELLED;
            case PENDING_APPROVAL:
                return newStatus == PUBLISHED || newStatus == DRAFT || newStatus == CANCELLED;
            case PUBLISHED:
                return newStatus == PAUSED || newStatus == CANCELLED || newStatus == COMPLETED || newStatus == SOLD_OUT || newStatus == POSTPONED;
            case PAUSED:
                return newStatus == PUBLISHED || newStatus == CANCELLED;
            case SOLD_OUT:
                return newStatus == PUBLISHED || newStatus == CANCELLED || newStatus == COMPLETED;
            case POSTPONED:
                return newStatus == PUBLISHED || newStatus == CANCELLED;
            case CANCELLED:
            case COMPLETED:
                return false; // Estados finales
            default:
                return false;
        }
    }
    
    /**
     * Obtiene el siguiente estado lógico basado en condiciones
     */
    public EventStatus getNextLogicalStatus(boolean isSoldOut, boolean hasEnded) {
        if (this == PUBLISHED) {
            if (hasEnded) {
                return COMPLETED;
            } else if (isSoldOut) {
                return SOLD_OUT;
            }
        } else if (this == SOLD_OUT && !isSoldOut) {
            return PUBLISHED;
        }
        return this;
    }
}