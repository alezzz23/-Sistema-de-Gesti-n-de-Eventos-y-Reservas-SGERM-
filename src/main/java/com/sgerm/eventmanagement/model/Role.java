package com.sgerm.eventmanagement.model;

/**
 * Enumeración que define los roles de usuario en el sistema
 */
public enum Role {
    /**
     * Administrador del sistema - Acceso completo
     */
    ADMIN("Administrador", "Acceso completo al sistema"),
    
    /**
     * Organizador de eventos - Puede crear y gestionar eventos
     */
    ORGANIZER("Organizador", "Puede crear y gestionar eventos"),
    
    /**
     * Cliente - Puede reservar y comprar entradas
     */
    CLIENT("Cliente", "Puede reservar y comprar entradas"),
    
    /**
     * Staff - Personal de apoyo para eventos
     */
    STAFF("Personal", "Personal de apoyo para eventos"),
    
    /**
     * Moderador - Puede moderar contenido y usuarios
     */
    MODERATOR("Moderador", "Puede moderar contenido y usuarios");
    
    private final String displayName;
    private final String description;
    
    Role(String displayName, String description) {
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
     * Verifica si el rol tiene permisos de administrador
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Verifica si el rol puede organizar eventos
     */
    public boolean canOrganizeEvents() {
        return this == ADMIN || this == ORGANIZER;
    }
    
    /**
     * Verifica si el rol puede moderar contenido
     */
    public boolean canModerate() {
        return this == ADMIN || this == MODERATOR;
    }
    
    /**
     * Verifica si el rol puede acceder al panel de administración
     */
    public boolean canAccessAdminPanel() {
        return this == ADMIN || this == MODERATOR || this == ORGANIZER;
    }
    
    /**
     * Obtiene todos los roles disponibles como array de strings
     */
    public static String[] getAllRoleNames() {
        Role[] roles = values();
        String[] roleNames = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].name();
        }
        return roleNames;
    }
    
    /**
     * Obtiene un rol por su nombre de visualización
     */
    public static Role getByDisplayName(String displayName) {
        for (Role role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No se encontró el rol: " + displayName);
    }
}