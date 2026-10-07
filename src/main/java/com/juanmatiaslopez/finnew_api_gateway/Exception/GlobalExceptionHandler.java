package com.juanmatiaslopez.finnew_api_gateway.Exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2) //Makes sure that this class acts as a default error handler since the reactive system works differently
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted())
            return Mono.error(ex);//If it's already sent to client, we shouldn't send it again
        HttpStatus httpStatus = determineStatus(ex);

        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", httpStatus.value());
        responseBody.put("error", httpStatus.getReasonPhrase());
        responseBody.put("message", ex.getMessage());
        responseBody.put("path", exchange.getRequest().getPath().value());

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(responseBody))
                .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                .flatMap(dataBuffer -> exchange.getResponse().writeWith(Mono.just(dataBuffer)))
                .onErrorResume(e -> Mono.error(ex));
    }

    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse){
            return (HttpStatus) rse.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
