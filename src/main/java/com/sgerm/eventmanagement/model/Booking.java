package com.sgerm.eventmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una reserva de entradas para un evento
 */
@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_code", unique = true, nullable = false)
    private String bookingCode;
    
    @NotNull(message = "La cantidad de entradas es obligatoria")
    @Min(value = 1, message = "Debe reservar al menos 1 entrada")
    @Column(name = "ticket_quantity", nullable = false)
    private Integer ticketQuantity;
    
    @NotNull(message = "El precio total es obligatorio")
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;
    
    @Column(name = "qr_code")
    private String qrCode;
    
    @Column(name = "check_in_date")
    private LocalDateTime checkInDate;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_reference")
    private String paymentReference;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_date")
    private LocalDateTime refundDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    // Constructores
    public Booking() {
        this.bookingCode = generateBookingCode();
        this.bookingDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Booking(User user, Event event, Integer ticketQuantity, BigDecimal totalPrice) {
        this();
        this.user = user;
        this.event = event;
        this.ticketQuantity = ticketQuantity;
        this.totalPrice = totalPrice;
    }
    
    // Métodos de utilidad
    private String generateBookingCode() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public void generateQRCode() {
        this.qrCode = "QR-" + bookingCode + "-" + id;
    }
    
    public boolean canBeCancelled() {
        if (status != BookingStatus.CONFIRMED && status != BookingStatus.PENDING) {
            return false;
        }
        
        if (event.getCancellationDeadline() != null) {
            return LocalDateTime.now().isBefore(event.getCancellationDeadline());
        }
        
        // Por defecto, permitir cancelación hasta 24 horas antes del evento
        return LocalDateTime.now().isBefore(event.getStartDate().minusHours(24));
    }
    
    public boolean canCheckIn() {
        return status == BookingStatus.CONFIRMED && 
               checkInDate == null &&
               LocalDateTime.now().isAfter(event.getStartDate().minusHours(2)) &&
               LocalDateTime.now().isBefore(event.getEndDate());
    }
    
    public boolean isCheckedIn() {
        return checkInDate != null;
    }
    
    public void checkIn() {
        if (!canCheckIn()) {
            throw new IllegalStateException("No se puede realizar el check-in en este momento");
        }
        this.checkInDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
        this.paymentDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        generateQRCode();
    }
    
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Esta reserva no puede ser cancelada");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancellationDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void refund(BigDecimal amount) {
        this.refundAmount = amount;
        this.refundDate = LocalDateTime.now();
        this.status = BookingStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getPricePerTicket() {
        if (ticketQuantity == 0) return BigDecimal.ZERO;
        return totalPrice.divide(BigDecimal.valueOf(ticketQuantity), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    
    public Integer getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(Integer ticketQuantity) { this.ticketQuantity = ticketQuantity; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    public LocalDateTime getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(LocalDateTime cancellationDate) { this.cancellationDate = cancellationDate; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public LocalDateTime getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDateTime checkInDate) { this.checkInDate = checkInDate; }
    
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id) && Objects.equals(bookingCode, booking.bookingCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, bookingCode);
    }
    
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingCode='" + bookingCode + '\'' +
                ", ticketQuantity=" + ticketQuantity +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", bookingDate=" + bookingDate +
                '}';
    }
}