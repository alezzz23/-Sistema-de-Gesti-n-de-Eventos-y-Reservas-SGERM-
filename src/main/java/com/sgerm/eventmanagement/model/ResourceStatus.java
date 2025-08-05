package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los estados de los recursos para eventos
 */
public enum ResourceStatus {
    /**
     * Recurso disponible para reservar
     */
    AVAILABLE("Disponible", "El recurso está disponible para ser reservado", "✅", "success"),
    
    /**
     * Recurso reservado para un evento
     */
    RESERVED("Reservado", "El recurso está reservado para un evento específico", "📅", "warning"),
    
    /**
     * Recurso en uso actualmente
     */
    IN_USE("En Uso", "El recurso está siendo utilizado actualmente", "🔄", "info"),
    
    /**
     * Recurso en mantenimiento
     */
    MAINTENANCE("Mantenimiento", "El recurso está en mantenimiento y no disponible", "🔧", "secondary"),
    
    /**
     * Recurso dañado o fuera de servicio
     */
    OUT_OF_SERVICE("Fuera de Servicio", "El recurso está dañado o fuera de servicio", "❌", "danger"),
    
    /**
     * Recurso en tránsito o siendo transportado
     */
    IN_TRANSIT("En Tránsito", "El recurso está siendo transportado", "🚛", "info"),
    
    /**
     * Recurso siendo configurado o instalado
     */
    SETUP("Configuración", "El recurso está siendo configurado o instalado", "⚙️", "warning"),
    
    /**
     * Recurso siendo desmontado después del evento
     */
    TEARDOWN("Desmontaje", "El recurso está siendo desmontado", "📦", "warning"),
    
    /**
     * Recurso perdido o extraviado
     */
    LOST("Perdido", "El recurso se ha perdido o extraviado", "❓", "danger"),
    
    /**
     * Recurso retirado permanentemente
     */
    RETIRED("Retirado", "El recurso ha sido retirado permanentemente del inventario", "🗑️", "dark"),
    
    /**
     * Recurso pendiente de inspección
     */
    PENDING_INSPECTION("Pendiente Inspección", "El recurso está pendiente de inspección", "🔍", "secondary"),
    
    /**
     * Recurso en proceso de limpieza
     */
    CLEANING("Limpieza", "El recurso está siendo limpiado", "🧹", "info");
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final String cssClass;
    
    ResourceStatus(String displayName, String description, String icon, String cssClass) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.cssClass = cssClass;
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
    
    /**
     * Obtiene el nombre completo con icono
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
    
    /**
     * Verifica si el recurso está disponible para reservar
     */
    public boolean isAvailableForBooking() {
        return this == AVAILABLE;
    }
    
    /**
     * Verifica si el recurso está activo (no retirado, perdido o fuera de servicio)
     */
    public boolean isActive() {
        return this != RETIRED && this != LOST && this != OUT_OF_SERVICE;
    }
    
    /**
     * Verifica si el recurso está en un estado temporal
     */
    public boolean isTemporaryStatus() {
        return this == MAINTENANCE || this == IN_TRANSIT || this == SETUP || 
               this == TEARDOWN || this == PENDING_INSPECTION || this == CLEANING;
    }
    
    /**
     * Verifica si el recurso está siendo utilizado
     */
    public boolean isInUse() {
        return this == RESERVED || this == IN_USE || this == SETUP || this == TEARDOWN;
    }
    
    /**
     * Verifica si el recurso requiere atención
     */
    public boolean requiresAttention() {
        return this == OUT_OF_SERVICE || this == LOST || this == PENDING_INSPECTION;
    }
    
    /**
     * Verifica si el estado es final (no puede cambiar automáticamente)
     */
    public boolean isFinalStatus() {
        return this == RETIRED || this == LOST;
    }
    
    /**
     * Obtiene los estados que pueden ser seleccionados por organizadores
     */
    public static ResourceStatus[] getOrganizerSelectableStatuses() {
        return new ResourceStatus[]{
            AVAILABLE, RESERVED, IN_USE, MAINTENANCE
        };
    }
    
    /**
     * Obtiene los estados que solo pueden ser seleccionados por administradores
     */
    public static ResourceStatus[] getAdminOnlyStatuses() {
        return new ResourceStatus[]{
            OUT_OF_SERVICE, LOST, RETIRED, PENDING_INSPECTION
        };
    }
    
    /**
     * Obtiene los estados del sistema (automáticos)
     */
    public static ResourceStatus[] getSystemStatuses() {
        return new ResourceStatus[]{
            IN_TRANSIT, SETUP, TEARDOWN, CLEANING
        };
    }
    
    /**
     * Obtiene las transiciones válidas desde este estado
     */
    public ResourceStatus[] getValidTransitions() {
        switch (this) {
            case AVAILABLE:
                return new ResourceStatus[]{RESERVED, MAINTENANCE, OUT_OF_SERVICE, PENDING_INSPECTION};
            case RESERVED:
                return new ResourceStatus[]{AVAILABLE, IN_USE, SETUP, IN_TRANSIT};
            case IN_USE:
                return new ResourceStatus[]{TEARDOWN, AVAILABLE, MAINTENANCE};
            case MAINTENANCE:
                return new ResourceStatus[]{AVAILABLE, OUT_OF_SERVICE, PENDING_INSPECTION};
            case OUT_OF_SERVICE:
                return new ResourceStatus[]{MAINTENANCE, RETIRED, PENDING_INSPECTION};
            case IN_TRANSIT:
                return new ResourceStatus[]{AVAILABLE, SETUP, LOST};
            case SETUP:
                return new ResourceStatus[]{IN_USE, AVAILABLE, MAINTENANCE};
            case TEARDOWN:
                return new ResourceStatus[]{AVAILABLE, CLEANING, MAINTENANCE};
            case LOST:
                return new ResourceStatus[]{AVAILABLE, RETIRED}; // Si se encuentra
            case RETIRED:
                return new ResourceStatus[]{}; // Estado final
            case PENDING_INSPECTION:
                return new ResourceStatus[]{AVAILABLE, MAINTENANCE, OUT_OF_SERVICE};
            case CLEANING:
                return new ResourceStatus[]{AVAILABLE, MAINTENANCE};
            default:
                return new ResourceStatus[]{};
        }
    }
    
    /**
     * Verifica si es válida la transición a otro estado
     */
    public boolean canTransitionTo(ResourceStatus newStatus) {
        ResourceStatus[] validTransitions = getValidTransitions();
        for (ResourceStatus status : validTransitions) {
            if (status == newStatus) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene un estado por su nombre de visualización
     */
    public static ResourceStatus getByDisplayName(String displayName) {
        for (ResourceStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No se encontró el estado del recurso: " + displayName);
    }
    
    /**
     * Obtiene los estados activos (excluyendo finales)
     */
    public static ResourceStatus[] getActiveStatuses() {
        return new ResourceStatus[]{
            AVAILABLE, RESERVED, IN_USE, MAINTENANCE, IN_TRANSIT, 
            SETUP, TEARDOWN, PENDING_INSPECTION, CLEANING
        };
    }
    
    /**
     * Obtiene los estados problemáticos que requieren atención
     */
    public static ResourceStatus[] getProblematicStatuses() {
        return new ResourceStatus[]{
            OUT_OF_SERVICE, LOST, PENDING_INSPECTION
        };
    }
}