package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define las categorías de eventos
 */
public enum EventCategory {
    /**
     * Conferencias y seminarios profesionales
     */
    CONFERENCE("Conferencia", "Conferencias, seminarios y eventos profesionales", "🎤"),
    
    /**
     * Talleres y cursos educativos
     */
    WORKSHOP("Taller", "Talleres, cursos y actividades educativas", "🛠️"),
    
    /**
     * Conciertos y espectáculos musicales
     */
    CONCERT("Concierto", "Conciertos, recitales y espectáculos musicales", "🎵"),
    
    /**
     * Obras de teatro y espectáculos
     */
    THEATER("Teatro", "Obras de teatro, monólogos y espectáculos teatrales", "🎭"),
    
    /**
     * Eventos deportivos
     */
    SPORTS("Deportes", "Competencias, torneos y eventos deportivos", "⚽"),
    
    /**
     * Exposiciones y ferias
     */
    EXHIBITION("Exposición", "Exposiciones, ferias y muestras", "🖼️"),
    
    /**
     * Fiestas y celebraciones
     */
    PARTY("Fiesta", "Fiestas, celebraciones y eventos sociales", "🎉"),
    
    /**
     * Eventos corporativos
     */
    CORPORATE("Corporativo", "Eventos empresariales y corporativos", "💼"),
    
    /**
     * Eventos gastronómicos
     */
    FOOD("Gastronomía", "Eventos gastronómicos, cenas y degustaciones", "🍽️"),
    
    /**
     * Eventos de tecnología
     */
    TECHNOLOGY("Tecnología", "Eventos de tecnología, hackathons y meetups tech", "💻"),
    
    /**
     * Eventos de arte y cultura
     */
    ART_CULTURE("Arte y Cultura", "Eventos artísticos y culturales", "🎨"),
    
    /**
     * Eventos familiares
     */
    FAMILY("Familiar", "Eventos familiares y para niños", "👨‍👩‍👧‍👦"),
    
    /**
     * Eventos de salud y bienestar
     */
    HEALTH_WELLNESS("Salud y Bienestar", "Eventos de salud, fitness y bienestar", "🧘‍♀️"),
    
    /**
     * Eventos de networking
     */
    NETWORKING("Networking", "Eventos de networking y conexiones profesionales", "🤝"),
    
    /**
     * Eventos virtuales/online
     */
    VIRTUAL("Virtual", "Eventos virtuales y online", "💻"),
    
    /**
     * Otros eventos no clasificados
     */
    OTHER("Otros", "Otros tipos de eventos", "📅");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    EventCategory(String displayName, String description, String icon) {
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
     * Verifica si la categoría es para eventos presenciales típicamente
     */
    public boolean isTypicallyInPerson() {
        return this != VIRTUAL;
    }
    
    /**
     * Verifica si la categoría suele requerir equipamiento especial
     */
    public boolean requiresSpecialEquipment() {
        return this == CONCERT || this == THEATER || this == CONFERENCE || 
               this == TECHNOLOGY || this == SPORTS;
    }
    
    /**
     * Verifica si la categoría es típicamente para audiencias grandes
     */
    public boolean isTypicallyLargeAudience() {
        return this == CONCERT || this == SPORTS || this == CONFERENCE || 
               this == EXHIBITION || this == PARTY;
    }
    
    /**
     * Obtiene las categorías más populares
     */
    public static EventCategory[] getPopularCategories() {
        return new EventCategory[]{
            CONFERENCE, WORKSHOP, CONCERT, PARTY, CORPORATE, NETWORKING
        };
    }
    
    /**
     * Obtiene las categorías de entretenimiento
     */
    public static EventCategory[] getEntertainmentCategories() {
        return new EventCategory[]{
            CONCERT, THEATER, PARTY, ART_CULTURE, SPORTS
        };
    }
    
    /**
     * Obtiene las categorías profesionales
     */
    public static EventCategory[] getProfessionalCategories() {
        return new EventCategory[]{
            CONFERENCE, WORKSHOP, CORPORATE, NETWORKING, TECHNOLOGY
        };
    }
    
    /**
     * Obtiene una categoría por su nombre de visualización
     */
    public static EventCategory getByDisplayName(String displayName) {
        for (EventCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No se encontró la categoría: " + displayName);
    }
    
    /**
     * Obtiene categorías relacionadas basadas en similitudes
     */
    public EventCategory[] getRelatedCategories() {
        switch (this) {
            case CONFERENCE:
                return new EventCategory[]{WORKSHOP, CORPORATE, NETWORKING, TECHNOLOGY};
            case WORKSHOP:
                return new EventCategory[]{CONFERENCE, TECHNOLOGY, HEALTH_WELLNESS};
            case CONCERT:
                return new EventCategory[]{THEATER, ART_CULTURE, PARTY};
            case THEATER:
                return new EventCategory[]{CONCERT, ART_CULTURE};
            case SPORTS:
                return new EventCategory[]{HEALTH_WELLNESS, FAMILY};
            case PARTY:
                return new EventCategory[]{CONCERT, FAMILY, FOOD};
            case CORPORATE:
                return new EventCategory[]{CONFERENCE, NETWORKING};
            case TECHNOLOGY:
                return new EventCategory[]{CONFERENCE, WORKSHOP, NETWORKING};
            case NETWORKING:
                return new EventCategory[]{CONFERENCE, CORPORATE, TECHNOLOGY};
            default:
                return new EventCategory[]{OTHER};
        }
    }
}