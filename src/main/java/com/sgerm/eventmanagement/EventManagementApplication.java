package com.sgerm.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del Sistema de Gestión de Eventos y Reservas (SGERM)
 * 
 * @author SGERM Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
        System.out.println("\n" +
            "███████╗ ██████╗ ███████╗██████╗ ███╗   ███╗\n" +
            "██╔════╝██╔════╝ ██╔════╝██╔══██╗████╗ ████║\n" +
            "███████╗██║  ███╗█████╗  ██████╔╝██╔████╔██║\n" +
            "╚════██║██║   ██║██╔══╝  ██╔══██╗██║╚██╔╝██║\n" +
            "███████║╚██████╔╝███████╗██║  ██║██║ ╚═╝ ██║\n" +
            "╚══════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝\n" +
            "\n🎉 Sistema de Gestión de Eventos y Reservas iniciado exitosamente!\n" +
            "📊 Dashboard disponible en: http://localhost:8080\n" +
            "📚 API Documentation: http://localhost:8080/swagger-ui.html\n");
    }
}