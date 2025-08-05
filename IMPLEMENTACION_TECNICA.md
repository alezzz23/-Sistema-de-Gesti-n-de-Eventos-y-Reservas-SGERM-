# 🛠️ Guía de Implementación Técnica - SGERM Expandido

## 📋 Implementación Prioritaria - Fase 1

### 💳 **1. Sistema de Pagos con Stripe**

#### **Dependencias Maven:**
```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.16.0</version>
</dependency>
```

#### **Modelo Payment:**
```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // getters, setters, constructors
}

enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, DIGITAL_WALLET
}

enum PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED
}
```

#### **PaymentService:**
```java
@Service
@Transactional
public class PaymentService {
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    public PaymentIntent createPaymentIntent(Long bookingId, BigDecimal amount) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // centavos
                .setCurrency("usd")
                .putMetadata("booking_id", bookingId.toString())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
            
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error creating payment intent", e);
        }
    }
    
    public Payment processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setBooking(bookingService.findById(request.getBookingId()));
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PROCESSING);
        
        try {
            PaymentIntent intent = createPaymentIntent(request.getBookingId(), request.getAmount());
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStatus(PaymentStatus.PENDING);
            
            return paymentRepository.save(payment);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Payment processing failed", e);
        }
    }
    
    public void confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
            
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(intent.getId());
                paymentRepository.save(payment);
                
                // Confirmar la reserva
                bookingService.confirmBooking(payment.getBooking().getId());
                
                // Enviar notificación
                notificationService.sendPaymentConfirmation(payment);
            }
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error confirming payment", e);
        }
    }
}
```

#### **PaymentController:**
```java
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request) {
        
        PaymentIntent intent = paymentService.createPaymentIntent(
            request.getBookingId(), request.getAmount());
            
        PaymentIntentResponse response = new PaymentIntentResponse(
            intent.getId(), intent.getClientSecret());
            
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        Payment payment = paymentService.processPayment(request);
        PaymentResponse response = PaymentResponse.fromPayment(payment);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        // Verificar webhook y procesar eventos
        paymentService.handleWebhookEvent(payload, sigHeader);
        
        return ResponseEntity.ok("Success");
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @PathVariable Long userId) {
        
        List<Payment> payments = paymentService.getPaymentsByUser(userId);
        List<PaymentResponse> response = payments.stream()
            .map(PaymentResponse::fromPayment)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }
}
```

---

### 📊 **2. Sistema de Analytics**

#### **AnalyticsService:**
```java
@Service
public class AnalyticsService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    public DashboardMetrics getDashboardMetrics(LocalDate startDate, LocalDate endDate) {
        DashboardMetrics metrics = new DashboardMetrics();
        
        // Ingresos totales
        BigDecimal totalRevenue = paymentRepository.getTotalRevenueByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        metrics.setTotalRevenue(totalRevenue);
        
        // Número de reservas
        Long totalBookings = bookingRepository.countByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        metrics.setTotalBookings(totalBookings);
        
        // Eventos activos
        Long activeEvents = eventRepository.countActiveEventsByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        metrics.setActiveEvents(activeEvents);
        
        // Tasa de conversión
        Double conversionRate = calculateConversionRate(startDate, endDate);
        metrics.setConversionRate(conversionRate);
        
        // Eventos más populares
        List<EventPopularity> popularEvents = getPopularEvents(startDate, endDate, 5);
        metrics.setPopularEvents(popularEvents);
        
        // Ingresos por día
        List<DailyRevenue> dailyRevenue = getDailyRevenue(startDate, endDate);
        metrics.setDailyRevenue(dailyRevenue);
        
        return metrics;
    }
    
    public List<EventPopularity> getPopularEvents(LocalDate startDate, LocalDate endDate, int limit) {
        return eventRepository.findMostPopularEvents(startDate, endDate, PageRequest.of(0, limit));
    }
    
    public RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        RevenueReport report = new RevenueReport();
        
        // Ingresos por categoría de evento
        List<CategoryRevenue> categoryRevenue = paymentRepository.getRevenueByCategory(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        report.setCategoryRevenue(categoryRevenue);
        
        // Ingresos por organizador
        List<OrganizerRevenue> organizerRevenue = paymentRepository.getRevenueByOrganizer(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        report.setOrganizerRevenue(organizerRevenue);
        
        // Comparación con período anterior
        LocalDate previousStart = startDate.minusDays(ChronoUnit.DAYS.between(startDate, endDate));
        BigDecimal previousRevenue = paymentRepository.getTotalRevenueByDateRange(
            previousStart.atStartOfDay(), startDate.minusDays(1).atTime(23, 59, 59));
        report.setPreviousPeriodRevenue(previousRevenue);
        
        return report;
    }
    
    private Double calculateConversionRate(LocalDate startDate, LocalDate endDate) {
        Long totalViews = eventRepository.getTotalViewsByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        Long totalBookings = bookingRepository.countByDateRange(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            
        if (totalViews == 0) return 0.0;
        return (totalBookings.doubleValue() / totalViews.doubleValue()) * 100;
    }
}
```

#### **AnalyticsController:**
```java
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetrics> getDashboardMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        DashboardMetrics metrics = analyticsService.getDashboardMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/revenue-report")
    public ResponseEntity<RevenueReport> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        RevenueReport report = analyticsService.generateRevenueReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/popular-events")
    public ResponseEntity<List<EventPopularity>> getPopularEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<EventPopularity> events = analyticsService.getPopularEvents(startDate, endDate, limit);
        return ResponseEntity.ok(events);
    }
}
```

---

### ⭐ **3. Sistema de Reviews**

#### **Modelo Review:**
```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;
    
    @Column(length = 1000)
    private String comment;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_moderated")
    private Boolean isModerated = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // getters, setters, constructors
}
```

#### **ReviewService:**
```java
@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    public Review createReview(CreateReviewRequest request, Long userId) {
        // Verificar que el usuario asistió al evento
        boolean hasAttended = bookingRepository.existsByUserIdAndEventIdAndStatus(
            userId, request.getEventId(), BookingStatus.COMPLETED);
            
        if (!hasAttended) {
            throw new UnauthorizedReviewException(
                "Solo puedes reseñar eventos a los que hayas asistido");
        }
        
        // Verificar que no haya reseñado antes
        boolean hasReviewed = reviewRepository.existsByUserIdAndEventId(
            userId, request.getEventId());
            
        if (hasReviewed) {
            throw new DuplicateReviewException(
                "Ya has reseñado este evento");
        }
        
        Review review = new Review();
        review.setEvent(eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new EventNotFoundException("Evento no encontrado")));
        review.setUser(userService.findById(userId));
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsVerified(true); // Verificado porque asistió
        
        // Moderación automática básica
        review.setIsModerated(moderateContent(request.getComment()));
        
        Review savedReview = reviewRepository.save(review);
        
        // Actualizar rating promedio del evento
        updateEventAverageRating(request.getEventId());
        
        return savedReview;
    }
    
    public List<Review> getEventReviews(Long eventId, Pageable pageable) {
        return reviewRepository.findByEventIdAndIsModeratedTrueOrderByCreatedAtDesc(
            eventId, pageable);
    }
    
    public ReviewStatistics getEventReviewStatistics(Long eventId) {
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistribution(eventId);
        Double averageRating = reviewRepository.getAverageRating(eventId);
        Long totalReviews = reviewRepository.countByEventId(eventId);
        
        ReviewStatistics stats = new ReviewStatistics();
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        stats.setTotalReviews(totalReviews);
        stats.setRatingDistribution(convertToRatingDistribution(ratingDistribution));
        
        return stats;
    }
    
    private void updateEventAverageRating(Long eventId) {
        Double averageRating = reviewRepository.getAverageRating(eventId);
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException("Evento no encontrado"));
        
        event.setAverageRating(averageRating != null ? averageRating : 0.0);
        eventRepository.save(event);
    }
    
    private boolean moderateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }
        
        // Lista de palabras prohibidas (simplificado)
        List<String> bannedWords = Arrays.asList("spam", "fake", "scam");
        
        String lowerContent = content.toLowerCase();
        return bannedWords.stream().noneMatch(lowerContent::contains);
    }
}
```

---

### 🔧 **Configuración de Aplicación**

#### **application.yml actualizado:**
```yaml
spring:
  application:
    name: sgerm-expanded
  
  datasource:
    url: jdbc:postgresql://localhost:5432/sgerm_db
    username: ${DB_USERNAME:sgerm_user}
    password: ${DB_PASSWORD:sgerm_password}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# Configuración de Stripe
stripe:
  public:
    key: ${STRIPE_PUBLIC_KEY}
  secret:
    key: ${STRIPE_SECRET_KEY}
  webhook:
    secret: ${STRIPE_WEBHOOK_SECRET}

# Configuración de APIs externas
external:
  apis:
    google:
      maps:
        key: ${GOOGLE_MAPS_API_KEY}
    twilio:
      account:
        sid: ${TWILIO_ACCOUNT_SID}
      auth:
        token: ${TWILIO_AUTH_TOKEN}
      phone:
        number: ${TWILIO_PHONE_NUMBER}

# Configuración de la aplicación
app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey}
    expiration: 86400000 # 24 horas
  
  file:
    upload:
      dir: ${FILE_UPLOAD_DIR:./uploads}
      max-size: 10MB
  
  analytics:
    cache:
      ttl: 300 # 5 minutos
  
  review:
    moderation:
      enabled: true
      auto-approve: false

logging:
  level:
    com.sgerm: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/sgerm.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

---

## 🚀 **Próximos Pasos**

1. **Implementar las funcionalidades de Fase 1**
2. **Crear tests unitarios e integración**
3. **Configurar CI/CD pipeline**
4. **Documentar APIs con Swagger/OpenAPI**
5. **Implementar monitoreo y logging**
6. **Preparar para Fase 2 del roadmap**

¡El futuro de SGERM se ve prometedor! 🌟