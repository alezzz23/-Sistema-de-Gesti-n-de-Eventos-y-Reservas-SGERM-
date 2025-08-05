# 🎯 Ejemplos de Uso - SGERM Expandido

## 📱 Casos de Uso Prácticos

### **Escenario 1: Conferencia Tecnológica**

#### **Configuración del Evento:**
```json
POST /api/events
{
  "name": "TechConf 2024",
  "description": "Conferencia anual de tecnología",
  "category": "CONFERENCE",
  "startDate": "2024-06-15T09:00:00",
  "endDate": "2024-06-15T18:00:00",
  "location": {
    "name": "Centro de Convenciones",
    "address": "Av. Principal 123, Ciudad",
    "coordinates": {
      "latitude": -12.0464,
      "longitude": -77.0428
    }
  },
  "capacity": 500,
  "price": 150.00,
  "requiresApproval": false,
  "tags": ["tecnología", "programación", "innovación"]
}
```

#### **Reserva con Pago:**
```json
// 1. Crear intención de pago
POST /api/payments/create-intent
{
  "bookingId": 123,
  "amount": 150.00,
  "currency": "USD"
}

// Respuesta:
{
  "paymentIntentId": "pi_1234567890",
  "clientSecret": "pi_1234567890_secret_xyz",
  "status": "requires_payment_method"
}

// 2. Procesar pago (después de confirmación del cliente)
POST /api/payments/process
{
  "bookingId": 123,
  "paymentIntentId": "pi_1234567890",
  "method": "CREDIT_CARD",
  "amount": 150.00
}
```

#### **Analytics del Evento:**
```json
GET /api/analytics/dashboard?startDate=2024-06-01&endDate=2024-06-30

// Respuesta:
{
  "totalRevenue": 75000.00,
  "totalBookings": 500,
  "activeEvents": 1,
  "conversionRate": 85.5,
  "popularEvents": [
    {
      "eventId": 1,
      "eventName": "TechConf 2024",
      "bookings": 500,
      "revenue": 75000.00,
      "averageRating": 4.8
    }
  ],
  "dailyRevenue": [
    {
      "date": "2024-06-15",
      "revenue": 75000.00,
      "bookings": 500
    }
  ]
}
```

---

### **Escenario 2: Restaurante con Reservas**

#### **Configuración de Recursos:**
```json
POST /api/resources
{
  "name": "Mesa para 4 personas - Ventana",
  "type": "TABLE",
  "capacity": 4,
  "location": "Zona ventana",
  "amenities": ["vista", "silenciosa"],
  "pricePerHour": 0.00,
  "isAvailable": true,
  "metadata": {
    "tableNumber": "W-04",
    "hasView": true,
    "isAccessible": true
  }
}
```

#### **Sistema de Reservas por Horarios:**
```json
POST /api/bookings
{
  "eventId": null,
  "resourceId": 15,
  "startTime": "2024-06-20T19:00:00",
  "endTime": "2024-06-20T21:00:00",
  "guestCount": 4,
  "specialRequests": "Celebración de aniversario",
  "contactInfo": {
    "phone": "+51987654321",
    "email": "cliente@email.com"
  }
}
```

#### **Notificaciones Automáticas:**
```json
// Configuración de notificaciones
POST /api/notifications/templates
{
  "type": "BOOKING_REMINDER",
  "trigger": "2_HOURS_BEFORE",
  "channels": ["EMAIL", "SMS"],
  "template": {
    "subject": "Recordatorio: Tu reserva en {{restaurant_name}}",
    "body": "Hola {{customer_name}}, te recordamos tu reserva para {{guest_count}} personas el {{date}} a las {{time}}. Mesa: {{table_number}}."
  }
}
```

---

### **Escenario 3: Gimnasio con Clases**

#### **Evento Recurrente:**
```json
POST /api/events
{
  "name": "Yoga Matutino",
  "description": "Clase de yoga para principiantes",
  "category": "FITNESS",
  "isRecurring": true,
  "recurrencePattern": {
    "type": "WEEKLY",
    "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
    "startTime": "07:00:00",
    "duration": 60,
    "endDate": "2024-12-31"
  },
  "capacity": 20,
  "price": 25.00,
  "resourceId": 5, // Sala de yoga
  "instructorId": 10
}
```

#### **Sistema de Membresías:**
```json
POST /api/memberships
{
  "userId": 123,
  "type": "MONTHLY_UNLIMITED",
  "price": 80.00,
  "startDate": "2024-06-01",
  "benefits": [
    "Acceso ilimitado a clases grupales",
    "Descuento 10% en clases personales",
    "Acceso a área de pesas"
  ],
  "autoRenew": true
}
```

#### **Check-in con QR:**
```json
// Generar QR para la clase
GET /api/bookings/123/qr-code

// Respuesta:
{
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "checkInUrl": "https://sgerm.app/checkin/abc123def456",
  "expiresAt": "2024-06-20T08:00:00"
}

// Check-in
POST /api/checkin/abc123def456
{
  "timestamp": "2024-06-20T06:55:00",
  "location": {
    "latitude": -12.0464,
    "longitude": -77.0428
  }
}
```

---

### **Escenario 4: Centro de Eventos Corporativos**

#### **Evento con Múltiples Recursos:**
```json
POST /api/events
{
  "name": "Reunión Anual de Accionistas",
  "description": "Presentación de resultados anuales",
  "category": "CORPORATE",
  "startDate": "2024-07-10T14:00:00",
  "endDate": "2024-07-10T18:00:00",
  "capacity": 200,
  "resources": [
    {
      "resourceId": 1, // Auditorio principal
      "quantity": 1,
      "startTime": "2024-07-10T14:00:00",
      "endTime": "2024-07-10T18:00:00"
    },
    {
      "resourceId": 5, // Sistema de sonido
      "quantity": 1,
      "startTime": "2024-07-10T13:30:00",
      "endTime": "2024-07-10T18:30:00"
    },
    {
      "resourceId": 8, // Servicio de catering
      "quantity": 200,
      "startTime": "2024-07-10T16:00:00",
      "endTime": "2024-07-10T17:00:00"
    }
  ],
  "requiresApproval": true,
  "organizerId": 15
}
```

#### **Gestión de Inventario:**
```json
// Verificar disponibilidad antes del evento
GET /api/inventory/check?date=2024-07-10&resources=1,5,8

// Respuesta:
{
  "available": true,
  "conflicts": [],
  "suggestions": [],
  "inventory": [
    {
      "resourceId": 1,
      "name": "Auditorio Principal",
      "available": true,
      "currentStock": 1,
      "reservedStock": 0
    },
    {
      "resourceId": 5,
      "name": "Sistema de Sonido Premium",
      "available": true,
      "currentStock": 2,
      "reservedStock": 1
    },
    {
      "resourceId": 8,
      "name": "Servicio de Catering",
      "available": true,
      "currentStock": 500,
      "reservedStock": 200,
      "lowStockAlert": false
    }
  ]
}
```

---

### **Escenario 5: Sistema de Reviews y Reputación**

#### **Crear Review después del Evento:**
```json
POST /api/reviews
{
  "eventId": 123,
  "rating": 5,
  "comment": "Excelente conferencia, muy bien organizada. Los speakers fueron de primer nivel y la logística impecable.",
  "aspects": {
    "organization": 5,
    "content": 5,
    "venue": 4,
    "catering": 4,
    "networking": 5
  }
}
```

#### **Estadísticas de Reviews:**
```json
GET /api/reviews/event/123/statistics

// Respuesta:
{
  "averageRating": 4.6,
  "totalReviews": 127,
  "ratingDistribution": {
    "5": 78,
    "4": 32,
    "3": 12,
    "2": 3,
    "1": 2
  },
  "aspectRatings": {
    "organization": 4.8,
    "content": 4.7,
    "venue": 4.4,
    "catering": 4.2,
    "networking": 4.6
  },
  "sentimentAnalysis": {
    "positive": 85.5,
    "neutral": 12.0,
    "negative": 2.5
  }
}
```

---

## 🔄 **Flujos de Trabajo Automatizados**

### **Flujo de Reserva Completo:**

1. **Usuario busca evento** → `GET /api/events/search`
2. **Verifica disponibilidad** → `GET /api/events/{id}/availability`
3. **Crea reserva** → `POST /api/bookings`
4. **Procesa pago** → `POST /api/payments/process`
5. **Confirma reserva** → Webhook de Stripe
6. **Envía confirmación** → Notificación automática
7. **Recordatorio** → 24h antes del evento
8. **Check-in** → QR code en el evento
9. **Solicita review** → 24h después del evento

### **Flujo de Gestión de Inventario:**

1. **Reserva requiere recursos** → Verificación automática
2. **Stock bajo detectado** → Alerta al administrador
3. **Reposición automática** → Orden a proveedores
4. **Evento finaliza** → Liberación de recursos
5. **Inventario actualizado** → Estado en tiempo real

### **Flujo de Analytics:**

1. **Evento creado** → Tracking de métricas
2. **Reservas realizadas** → Análisis de conversión
3. **Pagos procesados** → Cálculo de ingresos
4. **Evento finalizado** → Reporte de resultados
5. **Reviews recibidas** → Análisis de satisfacción
6. **Dashboard actualizado** → Métricas en tiempo real

---

## 📊 **Métricas de Rendimiento Esperadas**

### **KPIs del Sistema:**
- **Tiempo de respuesta API**: < 200ms promedio
- **Disponibilidad**: 99.9% uptime
- **Tasa de conversión**: 15-25% (búsqueda → reserva)
- **Satisfacción del cliente**: > 4.5/5 promedio
- **Procesamiento de pagos**: 99.5% éxito

### **Métricas de Negocio:**
- **Ingresos por evento**: Tracking en tiempo real
- **Ocupación promedio**: 75-85% objetivo
- **Tiempo de setup**: < 5 minutos por evento
- **ROI de marketing**: Medición por canal
- **Retención de clientes**: > 60% eventos recurrentes

¡SGERM está listo para revolucionar la gestión de eventos! 🚀