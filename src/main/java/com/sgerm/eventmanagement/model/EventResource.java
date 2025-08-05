package com.sgerm.eventmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un recurso asignado a un evento
 */
@Entity
@Table(name = "event_resources")
public class EventResource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre del recurso es obligatorio")
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;
    
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status = ResourceStatus.RESERVED; // REQUESTED no existe, usando RESERVED
    
    @Column(name = "supplier_name")
    private String supplierName;
    
    @Column(name = "supplier_contact")
    private String supplierContact;
    
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;
    
    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;
    
    @Column(name = "setup_time")
    private LocalDateTime setupTime;
    
    @Column(name = "breakdown_time")
    private LocalDateTime breakdownTime;
    
    @Column(name = "location_notes", columnDefinition = "TEXT")
    private String locationNotes;
    
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
    
    @Column(name = "is_critical")
    private Boolean isCritical = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    // Constructores
    public EventResource() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public EventResource(String name, ResourceType type, Integer quantity, Event event) {
        this();
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.event = event;
        calculateTotalCost();
    }
    
    // Métodos de utilidad
    public void calculateTotalCost() {
        if (unitCost != null && quantity != null) {
            this.totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public boolean isAvailable() {
        return status == ResourceStatus.IN_USE || status == ResourceStatus.SETUP; // CONFIRMED y DELIVERED no existen
    }
    
    public boolean requiresDelivery() {
        return type == ResourceType.EQUIPMENT || type == ResourceType.FURNITURE || 
               type == ResourceType.DECORATION;
    }
    
    public boolean isSetupRequired() {
        return type == ResourceType.EQUIPMENT || type == ResourceType.AUDIO_VISUAL || 
               type == ResourceType.LIGHTING || type == ResourceType.STAGE;
    }
    
    public void markAsDelivered() {
        this.status = ResourceStatus.IN_USE; // DELIVERED no existe, usando IN_USE
        this.deliveryDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsSetup() {
        this.status = ResourceStatus.SETUP;
        this.setupTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsReturned() {
        this.status = ResourceStatus.AVAILABLE; // RETURNED no existe, usando AVAILABLE
        this.pickupDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isOverdue() {
        if (pickupDate == null) return false;
        return LocalDateTime.now().isAfter(pickupDate) && 
               (status != ResourceStatus.AVAILABLE && status != ResourceStatus.OUT_OF_SERVICE); // RETURNED y CANCELLED no existen
    }
    
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }
    
    public String getTypeDisplayName() {
        return type.getDisplayName();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalCost();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        calculateTotalCost();
    }
    
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { 
        this.unitCost = unitCost;
        calculateTotalCost();
    }
    
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    
    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public LocalDateTime getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDateTime pickupDate) { this.pickupDate = pickupDate; }
    
    public LocalDateTime getSetupTime() { return setupTime; }
    public void setSetupTime(LocalDateTime setupTime) { this.setupTime = setupTime; }
    
    public LocalDateTime getBreakdownTime() { return breakdownTime; }
    public void setBreakdownTime(LocalDateTime breakdownTime) { this.breakdownTime = breakdownTime; }
    
    public String getLocationNotes() { return locationNotes; }
    public void setLocationNotes(String locationNotes) { this.locationNotes = locationNotes; }
    
    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
    
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    
    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventResource)) return false;
        EventResource that = (EventResource) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "EventResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", quantity=" + quantity +
                ", status=" + status +
                ", totalCost=" + totalCost +
                '}';
    }
}