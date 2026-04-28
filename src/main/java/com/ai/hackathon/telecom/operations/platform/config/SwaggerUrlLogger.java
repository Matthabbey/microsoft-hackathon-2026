package com.ai.hackathon.telecom.operations.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwaggerUrlLogger {

    @Value("${server.port:8080}")
    private int port;

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerPath;

    @EventListener(ApplicationReadyEvent.class)
    public void logSwaggerUrl() {
        log.info("Swagger UI available at: http://localhost:{}{}", port, swaggerPath);
        log.info("OpenAPI docs available at: http://localhost:{}/v3/api-docs", port);
    }
}
