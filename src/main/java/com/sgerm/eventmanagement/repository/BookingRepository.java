package com.sgerm.eventmanagement.repository;

import com.sgerm.eventmanagement.model.Booking;
import com.sgerm.eventmanagement.model.BookingStatus;
import com.sgerm.eventmanagement.model.Event;
import com.sgerm.eventmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Booking
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Busca una reserva por su código único
     */
    Optional<Booking> findByBookingCode(String bookingCode);
    
    /**
     * Busca reservas por usuario
     */
    List<Booking> findByUser(User user);
    
    /**
     * Busca reservas por usuario con paginación
     */
    Page<Booking> findByUser(User user, Pageable pageable);
    
    /**
     * Busca reservas por evento
     */
    List<Booking> findByEvent(Event event);
    
    /**
     * Busca reservas por evento con paginación
     */
    Page<Booking> findByEvent(Event event, Pageable pageable);
    
    /**
     * Busca reservas por estado
     */
    List<Booking> findByStatus(BookingStatus status);
    
    /**
     * Busca reservas por estado con paginación
     */
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    /**
     * Busca reservas por usuario y estado
     */
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    
    /**
     * Busca reservas por evento y estado
     */
    List<Booking> findByEventAndStatus(Event event, BookingStatus status);
    
    /**
     * Busca reservas confirmadas por evento
     */
    @Query("SELECT b FROM Booking b WHERE b.event = :event AND b.status = :status")
    List<Booking> findConfirmedBookingsByEvent(@Param("event") Event event, @Param("status") BookingStatus status);
    
    /**
     * Busca reservas activas por usuario
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status IN (:statuses)")
    List<Booking> findActiveBookingsByUser(@Param("user") User user, @Param("statuses") List<BookingStatus> statuses);
    
    /**
     * Busca reservas por rango de fechas de creación
     */
    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca reservas por rango de fechas de pago
     */
    List<Booking> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Busca reservas pendientes de pago
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.paymentDate IS NULL")
    List<Booking> findPendingPaymentBookings(@Param("status") BookingStatus status);
    
    /**
     * Busca reservas expiradas (pendientes por más de X tiempo)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.bookingDate < :cutoffDate")
    List<Booking> findExpiredBookings(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("status") BookingStatus status);
    
    /**
     * Busca reservas que necesitan recordatorio
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status " +
           "AND b.event.startDate BETWEEN :startTime AND :endTime")
    List<Booking> findBookingsNeedingReminder(@Param("startTime") LocalDateTime startTime, @Param("status") BookingStatus status, 
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * Busca reservas por método de pago
     */
    List<Booking> findByPaymentMethod(String paymentMethod);
    
    /**
     * Busca reservas con check-in realizado
     */
    List<Booking> findByCheckInDateIsNotNull();
    
    /**
     * Busca reservas sin check-in para un evento específico
     */
    @Query("SELECT b FROM Booking b WHERE b.event = :event " +
           "AND b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED " +
           "AND b.checkInDate IS NULL")
    List<Booking> findBookingsWithoutCheckIn(@Param("event") Event event);
    
    /**
     * Busca reservas por referencia de pago
     */
    Optional<Booking> findByPaymentReference(String paymentReference);
    
    /**
     * Cuenta reservas por evento
     */
    long countByEvent(Event event);
    
    /**
     * Cuenta reservas por evento y estado
     */
    long countByEventAndStatus(Event event, BookingStatus status);
    
    /**
     * Cuenta reservas por usuario
     */
    long countByUser(User user);
    
    /**
     * Cuenta reservas por usuario y estado
     */
    long countByUserAndStatus(User user, BookingStatus status);
    
    /**
     * Cuenta reservas por estado
     */
    long countByStatus(BookingStatus status);
    
    /**
     * Suma total de entradas vendidas por evento
     */
    @Query("SELECT COALESCE(SUM(b.ticketQuantity), 0) FROM Booking b " +
           "WHERE b.event = :event AND b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED")
    Integer sumTicketsSoldByEvent(@Param("event") Event event);
    
    /**
     * Suma total de ingresos por evento
     */
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
           "WHERE b.event = :event AND b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED")
    BigDecimal sumRevenueByEvent(@Param("event") Event event);
    
    /**
     * Suma total de ingresos por usuario (organizador)
     */
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
           "WHERE b.event.organizer = :organizer AND b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED")
    BigDecimal sumRevenueByOrganizer(@Param("organizer") User organizer);
    
    /**
     * Obtiene las reservas más recientes
     */
    @Query("SELECT b FROM Booking b ORDER BY b.bookingDate DESC")
    List<Booking> findRecentBookings(Pageable pageable);
    
    /**
     * Obtiene las reservas más recientes por usuario
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.bookingDate DESC")
    List<Booking> findRecentBookingsByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Obtiene las reservas más recientes por evento
     */
    @Query("SELECT b FROM Booking b WHERE b.event = :event ORDER BY b.bookingDate DESC")
    List<Booking> findRecentBookingsByEvent(@Param("event") Event event, Pageable pageable);
    
    /**
     * Busca reservas duplicadas (mismo usuario y evento)
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.event = :event " +
           "AND b.status IN (com.sgerm.eventmanagement.model.BookingStatus.PENDING, com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED)")
    List<Booking> findDuplicateBookings(@Param("user") User user, @Param("event") Event event);
    
    /**
     * Verifica si un usuario ya tiene una reserva activa para un evento
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.user = :user AND b.event = :event " +
           "AND b.status IN (com.sgerm.eventmanagement.model.BookingStatus.PENDING, com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED)")
    boolean existsActiveBookingByUserAndEvent(@Param("user") User user, @Param("event") Event event);
    
    /**
     * Actualiza el estado de una reserva
     */
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id = :bookingId")
    void updateBookingStatus(@Param("bookingId") Long bookingId, @Param("status") BookingStatus status);
    
    /**
     * Actualiza la fecha de pago
     */
    @Modifying
    @Query("UPDATE Booking b SET b.paymentDate = :paymentDate, b.paymentReference = :paymentReference " +
           "WHERE b.id = :bookingId")
    void updatePaymentInfo(@Param("bookingId") Long bookingId, 
                          @Param("paymentDate") LocalDateTime paymentDate,
                          @Param("paymentReference") String paymentReference);
    
    /**
     * Actualiza la fecha de check-in
     */
    @Modifying
    @Query("UPDATE Booking b SET b.checkInDate = :checkInDate WHERE b.id = :bookingId")
    void updateCheckInDate(@Param("bookingId") Long bookingId, @Param("checkInDate") LocalDateTime checkInDate);
    
    /**
     * Actualiza el código QR
     */
    @Modifying
    @Query("UPDATE Booking b SET b.qrCode = :qrCode WHERE b.id = :bookingId")
    void updateQrCode(@Param("bookingId") Long bookingId, @Param("qrCode") String qrCode);
    
    /**
     * Busca reservas que necesitan generar QR
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND (b.qrCode IS NULL OR b.qrCode = '')")
    List<Booking> findBookingsNeedingQrCode(@Param("status") BookingStatus status);
    
    /**
     * Busca reservas cancelables (dentro del plazo de cancelación)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status " +
           "AND (b.event.cancellationDeadline IS NULL OR :now <= b.event.cancellationDeadline)")
    List<Booking> findCancelableBookings(@Param("now") LocalDateTime now, @Param("status") BookingStatus status);
    
    /**
     * Busca reservas por usuario en un rango de fechas
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user " +
           "AND b.event.startDate BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsByUserInDateRange(@Param("user") User user,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Obtiene estadísticas de reservas por estado
     */
    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> getBookingStatsByStatus();
    
    /**
     * Obtiene estadísticas de reservas por método de pago
     */
    @Query("SELECT b.paymentMethod, COUNT(b) FROM Booking b " +
           "WHERE b.paymentMethod IS NOT NULL " +
           "GROUP BY b.paymentMethod")
    List<Object[]> getBookingStatsByPaymentMethod();
    
    /**
     * Obtiene estadísticas de reservas por mes
     */
    @Query("SELECT b.bookingDate, COUNT(b) " +
           "FROM Booking b " +
           "WHERE b.bookingDate >= :startDate " +
           "GROUP BY b.bookingDate " +
           "ORDER BY b.bookingDate")
    List<Object[]> getBookingStatsByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Obtiene ingresos por mes
     */
    @Query("SELECT b.paymentDate, SUM(b.totalPrice) " +
           "FROM Booking b " +
           "WHERE b.paymentDate >= :startDate AND b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED " +
           "GROUP BY b.paymentDate " +
           "ORDER BY b.paymentDate")
    List<Object[]> getRevenueStatsByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Busca reservas con filtros avanzados
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:userId IS NULL OR b.user.id = :userId) AND " +
           "(:eventId IS NULL OR b.event.id = :eventId) AND " +
           "(:startDate IS NULL OR b.bookingDate >= :startDate) AND " +
           "(:endDate IS NULL OR b.bookingDate <= :endDate) AND " +
           "(:paymentMethod IS NULL OR b.paymentMethod = :paymentMethod)")
    Page<Booking> findBookingsWithFilters(@Param("status") BookingStatus status,
                                         @Param("userId") Long userId,
                                         @Param("eventId") Long eventId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("paymentMethod") String paymentMethod,
                                         Pageable pageable);
    
    /**
     * Busca los usuarios más activos (con más reservas)
     */
    @Query("SELECT b.user, COUNT(b) FROM Booking b " +
           "WHERE b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED " +
           "GROUP BY b.user " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);
    
    /**
     * Busca los eventos más populares (con más reservas)
     */
    @Query("SELECT b.event, COUNT(b) FROM Booking b " +
           "WHERE b.status = com.sgerm.eventmanagement.model.BookingStatus.CONFIRMED " +
           "GROUP BY b.event " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostPopularEvents(Pageable pageable);
}