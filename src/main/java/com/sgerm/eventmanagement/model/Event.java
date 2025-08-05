package com.sgerm.eventmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un evento en el sistema
 */
@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @NotBlank(message = "La ubicación es obligatoria")
    @Column(nullable = false)
    private String location;
    
    @Column(name = "venue_address")
    private String venueAddress;
    
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Max(value = 100000, message = "La capacidad no puede exceder 100,000")
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(name = "available_tickets")
    private Integer availableTickets;
    
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @Column(name = "max_tickets_per_user")
    private Integer maxTicketsPerUser = 10;
    
    @Column(name = "booking_deadline")
    private LocalDateTime bookingDeadline;
    
    @Column(name = "cancellation_deadline")
    private LocalDateTime cancellationDeadline;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EventResource> resources = new HashSet<>();
    
    // Constructores
    public Event() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Event(String title, String description, LocalDateTime startDate, 
                 LocalDateTime endDate, String location, Integer capacity, 
                 BigDecimal price, User organizer) {
        this();
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.capacity = capacity;
        this.availableTickets = capacity;
        this.price = price;
        this.organizer = organizer;
    }
    
    // Métodos de utilidad
    public boolean isActive() {
        return status == EventStatus.PUBLISHED && 
               LocalDateTime.now().isBefore(endDate);
    }
    
    public boolean canBook() {
        return isActive() && 
               availableTickets > 0 && 
               (bookingDeadline == null || LocalDateTime.now().isBefore(bookingDeadline));
    }
    
    public boolean isFull() {
        return availableTickets <= 0;
    }
    
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startDate);
    }
    
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public int getBookedTickets() {
        return capacity - availableTickets;
    }
    
    public double getOccupancyPercentage() {
        if (capacity == 0) return 0.0;
        return ((double) getBookedTickets() / capacity) * 100;
    }
    
    public void reserveTickets(int quantity) {
        if (quantity > availableTickets) {
            throw new IllegalArgumentException("No hay suficientes entradas disponibles");
        }
        this.availableTickets -= quantity;
    }
    
    public void releaseTickets(int quantity) {
        this.availableTickets = Math.min(capacity, availableTickets + quantity);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public Integer getAvailableTickets() { return availableTickets; }
    public void setAvailableTickets(Integer availableTickets) { this.availableTickets = availableTickets; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    
    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public Integer getMaxTicketsPerUser() { return maxTicketsPerUser; }
    public void setMaxTicketsPerUser(Integer maxTicketsPerUser) { this.maxTicketsPerUser = maxTicketsPerUser; }
    
    public LocalDateTime getBookingDeadline() { return bookingDeadline; }
    public void setBookingDeadline(LocalDateTime bookingDeadline) { this.bookingDeadline = bookingDeadline; }
    
    public LocalDateTime getCancellationDeadline() { return cancellationDeadline; }
    public void setCancellationDeadline(LocalDateTime cancellationDeadline) { this.cancellationDeadline = cancellationDeadline; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
    
    public Set<Booking> getBookings() { return bookings; }
    public void setBookings(Set<Booking> bookings) { this.bookings = bookings; }
    
    public Set<EventResource> getResources() { return resources; }
    public void setResources(Set<EventResource> resources) { this.resources = resources; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", status=" + status +
                '}';
    }
}