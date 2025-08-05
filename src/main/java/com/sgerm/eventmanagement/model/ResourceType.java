package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los tipos de recursos para eventos
 */
public enum ResourceType {
    /**
     * Equipos audiovisuales
     */
    AUDIO_VISUAL("Audiovisual", "Equipos de sonido, micrófonos, proyectores, pantallas", "🎤"),
    
    /**
     * Equipos de iluminación
     */
    LIGHTING("Iluminación", "Luces, focos, sistemas de iluminación", "💡"),
    
    /**
     * Mobiliario
     */
    FURNITURE("Mobiliario", "Mesas, sillas, tarimas, stands", "🪑"),
    
    /**
     * Decoración
     */
    DECORATION("Decoración", "Flores, centros de mesa, banners, decoración temática", "🎨"),
    
    /**
     * Catering y bebidas
     */
    CATERING("Catering", "Comida, bebidas, servicio de catering", "🍽️"),
    
    /**
     * Equipos técnicos
     */
    EQUIPMENT("Equipos", "Equipos técnicos, herramientas, maquinaria", "⚙️"),
    
    /**
     * Transporte
     */
    TRANSPORT("Transporte", "Vehículos, transporte de equipos, logística", "🚛"),
    
    /**
     * Personal de apoyo
     */
    STAFF("Personal", "Personal de apoyo, seguridad, técnicos", "👥"),
    
    /**
     * Materiales de construcción/montaje
     */
    CONSTRUCTION("Construcción", "Materiales para montaje, estructuras temporales", "🔨"),
    
    /**
     * Escenario y tarima
     */
    STAGE("Escenario", "Tarimas, escenarios, plataformas", "🎭"),
    
    /**
     * Seguridad
     */
    SECURITY("Seguridad", "Personal de seguridad, sistemas de seguridad", "🛡️"),
    
    /**
     * Tecnología
     */
    TECHNOLOGY("Tecnología", "Equipos informáticos, software, conectividad", "💻"),
    
    /**
     * Limpieza
     */
    CLEANING("Limpieza", "Servicios de limpieza, materiales de limpieza", "🧹"),
    
    /**
     * Entretenimiento
     */
    ENTERTAINMENT("Entretenimiento", "Artistas, músicos, animadores", "🎪"),
    
    /**
     * Otros recursos no clasificados
     */
    OTHER("Otros", "Otros recursos no clasificados", "📦");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    ResourceType(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
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
    
    /**
     * Obtiene el nombre completo con icono
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
    
    /**
     * Verifica si el recurso requiere instalación/montaje
     */
    public boolean requiresSetup() {
        return this == AUDIO_VISUAL || this == LIGHTING || this == STAGE || 
               this == EQUIPMENT || this == CONSTRUCTION || this == TECHNOLOGY;
    }
    
    /**
     * Verifica si el recurso requiere personal especializado
     */
    public boolean requiresSpecializedStaff() {
        return this == AUDIO_VISUAL || this == LIGHTING || this == TECHNOLOGY || 
               this == SECURITY || this == ENTERTAINMENT;
    }
    
    /**
     * Verifica si el recurso es reutilizable
     */
    public boolean isReusable() {
        return this != CATERING && this != CLEANING && this != DECORATION;
    }
    
    /**
     * Verifica si el recurso requiere transporte especial
     */
    public boolean requiresSpecialTransport() {
        return this == AUDIO_VISUAL || this == LIGHTING || this == FURNITURE || 
               this == EQUIPMENT || this == STAGE || this == CONSTRUCTION;
    }
    
    /**
     * Obtiene el tiempo estimado de setup en horas
     */
    public int getEstimatedSetupHours() {
        switch (this) {
            case AUDIO_VISUAL:
            case LIGHTING:
                return 4;
            case STAGE:
            case CONSTRUCTION:
                return 8;
            case FURNITURE:
            case DECORATION:
                return 2;
            case TECHNOLOGY:
                return 3;
            case EQUIPMENT:
                return 2;
            default:
                return 1;
        }
    }
    
    /**
     * Obtiene los tipos de recursos más comunes
     */
    public static ResourceType[] getCommonTypes() {
        return new ResourceType[]{
            AUDIO_VISUAL, FURNITURE, CATERING, DECORATION, STAFF
        };
    }
    
    /**
     * Obtiene los tipos de recursos técnicos
     */
    public static ResourceType[] getTechnicalTypes() {
        return new ResourceType[]{
            AUDIO_VISUAL, LIGHTING, EQUIPMENT, TECHNOLOGY, STAGE
        };
    }
    
    /**
     * Obtiene los tipos de recursos de servicios
     */
    public static ResourceType[] getServiceTypes() {
        return new ResourceType[]{
            CATERING, STAFF, SECURITY, CLEANING, ENTERTAINMENT, TRANSPORT
        };
    }
    
    /**
     * Obtiene un tipo de recurso por su nombre de visualización
     */
    public static ResourceType getByDisplayName(String displayName) {
        for (ResourceType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No se encontró el tipo de recurso: " + displayName);
    }
    
    /**
     * Obtiene tipos de recursos relacionados
     */
    public ResourceType[] getRelatedTypes() {
        switch (this) {
            case AUDIO_VISUAL:
                return new ResourceType[]{LIGHTING, TECHNOLOGY, STAGE};
            case LIGHTING:
                return new ResourceType[]{AUDIO_VISUAL, STAGE, EQUIPMENT};
            case FURNITURE:
                return new ResourceType[]{DECORATION, STAGE};
            case CATERING:
                return new ResourceType[]{STAFF, CLEANING};
            case STAGE:
                return new ResourceType[]{AUDIO_VISUAL, LIGHTING, CONSTRUCTION};
            case SECURITY:
                return new ResourceType[]{STAFF, TECHNOLOGY};
            default:
                return new ResourceType[]{OTHER};
        }
    }
}