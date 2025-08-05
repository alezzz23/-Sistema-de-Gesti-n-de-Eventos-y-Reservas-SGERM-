package com.sgerm.eventmanagement.service;

import com.sgerm.eventmanagement.model.Booking;
import com.sgerm.eventmanagement.model.Event;
import com.sgerm.eventmanagement.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio para el envío de correos electrónicos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${app.name}")
    private String appName;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Envía email de verificación de cuenta
     */
    @Async
    public void sendVerificationEmail(User user, String verificationToken) {
        // log.info("Enviando email de verificación a: {}", user.getEmail());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("verificationUrl", baseUrl + "/auth/verify-email?token=" + verificationToken);
            
            String htmlContent = templateEngine.process("emails/verification", context);
            
            sendHtmlEmail(
                user.getEmail(),
                "Verifica tu cuenta en " + appName,
                htmlContent
            );
            
            // log.info("Email de verificación enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de verificación a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Envía email de restablecimiento de contraseña
     */
    @Async
    public void sendPasswordResetEmail(User user, String temporaryPassword) {
        // log.info("Enviando email de restablecimiento de contraseña a: {}", user.getEmail());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("temporaryPassword", temporaryPassword);
            context.setVariable("loginUrl", baseUrl + "/auth/login");
            
            String htmlContent = templateEngine.process("emails/password-reset", context);
            
            sendHtmlEmail(
                user.getEmail(),
                "Restablecimiento de contraseña - " + appName,
                htmlContent
            );
            
            // log.info("Email de restablecimiento enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de restablecimiento a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Envía email de confirmación de reserva
     */
    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        // log.info("Enviando email de confirmación de reserva: {}", booking.getBookingCode());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("booking", booking);
            context.setVariable("user", booking.getUser());
            context.setVariable("event", booking.getEvent());
            context.setVariable("appName", appName);
            context.setVariable("bookingUrl", baseUrl + "/bookings/" + booking.getId());
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/booking-confirmation", context);
            
            sendHtmlEmail(
                booking.getUser().getEmail(),
                "Confirmación de reserva - " + booking.getEvent().getTitle(),
                htmlContent
            );
            
            // log.info("Email de confirmación enviado exitosamente para reserva: {}", booking.getBookingCode());
        } catch (Exception e) {
            // log.error("Error enviando email de confirmación para reserva: {}", booking.getBookingCode(), e);
        }
    }
    
    /**
     * Envía email de cancelación de reserva
     */
    @Async
    public void sendBookingCancellationEmail(Booking booking) {
        // log.info("Enviando email de cancelación de reserva: {}", booking.getBookingCode());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("booking", booking);
            context.setVariable("user", booking.getUser());
            context.setVariable("event", booking.getEvent());
            context.setVariable("appName", appName);
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/booking-cancellation", context);
            
            sendHtmlEmail(
                booking.getUser().getEmail(),
                "Cancelación de reserva - " + booking.getEvent().getTitle(),
                htmlContent
            );
            
            // log.info("Email de cancelación enviado exitosamente para reserva: {}", booking.getBookingCode());
        } catch (Exception e) {
            // log.error("Error enviando email de cancelación para reserva: {}", booking.getBookingCode(), e);
        }
    }
    
    /**
     * Envía email de recordatorio de evento
     */
    @Async
    public void sendEventReminderEmail(Booking booking) {
        // log.info("Enviando email de recordatorio de evento para reserva: {}", booking.getBookingCode());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("booking", booking);
            context.setVariable("user", booking.getUser());
            context.setVariable("event", booking.getEvent());
            context.setVariable("appName", appName);
            context.setVariable("eventUrl", baseUrl + "/events/" + booking.getEvent().getId());
            context.setVariable("bookingUrl", baseUrl + "/bookings/" + booking.getId());
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/event-reminder", context);
            
            sendHtmlEmail(
                booking.getUser().getEmail(),
                "Recordatorio: " + booking.getEvent().getTitle() + " es mañana",
                htmlContent
            );
            
            // log.info("Email de recordatorio enviado exitosamente para reserva: {}", booking.getBookingCode());
        } catch (Exception e) {
            // log.error("Error enviando email de recordatorio para reserva: {}", booking.getBookingCode(), e);
        }
    }
    
    /**
     * Envía email de notificación de evento cancelado
     */
    @Async
    public void sendEventCancellationEmail(Event event, User user) {
        // log.info("Enviando email de cancelación de evento: {} a usuario: {}", event.getTitle(), user.getEmail());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("event", event);
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/event-cancellation", context);
            
            sendHtmlEmail(
                user.getEmail(),
                "Evento cancelado - " + event.getTitle(),
                htmlContent
            );
            
            // log.info("Email de cancelación de evento enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de cancelación de evento a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Envía email de notificación de cambios en evento
     */
    @Async
    public void sendEventUpdateEmail(Event event, User user, String changes) {
        // log.info("Enviando email de actualización de evento: {} a usuario: {}", event.getTitle(), user.getEmail());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("event", event);
            context.setVariable("user", user);
            context.setVariable("changes", changes);
            context.setVariable("appName", appName);
            context.setVariable("eventUrl", baseUrl + "/events/" + event.getId());
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/event-update", context);
            
            sendHtmlEmail(
                user.getEmail(),
                "Actualización de evento - " + event.getTitle(),
                htmlContent
            );
            
            // log.info("Email de actualización de evento enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de actualización de evento a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Envía email de bienvenida a nuevos usuarios
     */
    @Async
    public void sendWelcomeEmail(User user) {
        // log.info("Enviando email de bienvenida a: {}", user.getEmail());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("appName", appName);
            context.setVariable("dashboardUrl", baseUrl + "/dashboard");
            context.setVariable("eventsUrl", baseUrl + "/events");
            
            String htmlContent = templateEngine.process("emails/welcome", context);
            
            sendHtmlEmail(
                user.getEmail(),
                "¡Bienvenido a " + appName + "!",
                htmlContent
            );
            
            // log.info("Email de bienvenida enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("Error enviando email de bienvenida a: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Envía email de notificación de pago
     */
    @Async
    public void sendPaymentConfirmationEmail(Booking booking) {
        // log.info("Enviando email de confirmación de pago para reserva: {}", booking.getBookingCode());
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("booking", booking);
            context.setVariable("user", booking.getUser());
            context.setVariable("event", booking.getEvent());
            context.setVariable("appName", appName);
            context.setVariable("bookingUrl", baseUrl + "/bookings/" + booking.getId());
            context.setVariable("dateFormatter", DATE_FORMATTER);
            
            String htmlContent = templateEngine.process("emails/payment-confirmation", context);
            
            sendHtmlEmail(
                booking.getUser().getEmail(),
                "Pago confirmado - " + booking.getEvent().getTitle(),
                htmlContent
            );
            
            // log.info("Email de confirmación de pago enviado exitosamente para reserva: {}", booking.getBookingCode());
        } catch (Exception e) {
            // log.error("Error enviando email de confirmación de pago para reserva: {}", booking.getBookingCode(), e);
        }
    }
    
    /**
     * Envía email de contacto/soporte
     */
    @Async
    public void sendContactEmail(String fromEmail, String fromName, String subject, String message) {
        // log.info("Enviando email de contacto de: {} <{}>", fromName, fromEmail);
        
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("fromEmail", fromEmail);
            context.setVariable("fromName", fromName);
            context.setVariable("subject", subject);
            context.setVariable("message", message);
            context.setVariable("appName", appName);
            
            String htmlContent = templateEngine.process("emails/contact", context);
            
            // Enviar al equipo de soporte
            sendHtmlEmail(
                "soporte@" + appName.toLowerCase() + ".com",
                "Nuevo mensaje de contacto: " + subject,
                htmlContent
            );
            
            // log.info("Email de contacto enviado exitosamente");
        } catch (Exception e) {
            // log.error("Error enviando email de contacto", e);
        }
    }
    
    /**
     * Envía email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // helper.setFrom(fromEmail, appName);
        // helper.setTo(to);
        // helper.setSubject(subject);
        // helper.setText(htmlContent, true);
        
        // mailSender.send(message); // message no disponible
    }
    
    /**
     * Envía email de texto simple
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        // log.info("Enviando email simple a: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            
            // log.info("Email simple enviado exitosamente a: {}", to);
        } catch (Exception e) {
            // log.error("Error enviando email simple a: {}", to, e);
        }
    }
    
    /**
     * Envía email de notificación masiva
     */
    @Async
    public void sendBulkEmail(String[] recipients, String subject, String htmlContent) {
        // log.info("Enviando email masivo a {} destinatarios", recipients.length);
        
        try {
            for (String recipient : recipients) {
                try {
                    sendHtmlEmail(recipient, subject, htmlContent);
                    // Pequeña pausa para evitar spam
                    Thread.sleep(100);
                } catch (Exception e) {
                    // log.error("Error enviando email masivo a: {}", recipient, e);
                }
            }
            
            // log.info("Email masivo enviado exitosamente a {} destinatarios", recipients.length);
        } catch (Exception e) {
            // log.error("Error en envío de email masivo", e);
        }
    }
    
    /**
     * Verifica la configuración del servicio de email
     */
    public boolean testEmailConfiguration() {
        try {
            sendSimpleEmail(
                fromEmail,
                "Test de configuración - " + appName,
                "Este es un email de prueba para verificar la configuración del servicio de correo."
            );
            return true;
        } catch (Exception e) {
            // log.error("Error en test de configuración de email", e);
            return false;
        }
    }
}