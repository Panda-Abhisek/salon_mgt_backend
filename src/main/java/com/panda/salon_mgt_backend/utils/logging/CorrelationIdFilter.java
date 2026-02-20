package com.panda.salon_mgt_backend.utils.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = extractOrGenerateId(request);
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        long start = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.info(
                    "HTTP_REQUEST_COMPLETED method={} path={} status={} durationMs={} query={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    safeQuery(request)
            );

            MDC.remove(MDC_KEY);
        }
    }

    private String extractOrGenerateId(HttpServletRequest request) {
        String id = request.getHeader(HEADER);
        return (id == null || id.isBlank())
                ? UUID.randomUUID().toString()
                : id;
    }

    private String safeQuery(HttpServletRequest request) {
        String q = request.getQueryString();
        return q == null ? "" : q;
    }
}