package com.example.monitoring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNotFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();
        // Для статических ассетов и API возвращаем обычный 404 — не ломаем JS роутер
        if (path.startsWith("/_next") || path.startsWith("/api") || path.contains(".")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Для пользовательских страниц показываем кастомную 404
        request.getRequestDispatcher("/404.html").forward(request, response);
    }
}
