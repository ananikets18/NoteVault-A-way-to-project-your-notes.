package com.notevault.controller;

import com.notevault.dto.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller handling global error routing and redirections.
 * Distinguishes between:
 * - Browser requests (Accept: text/html): forwards to styled 404 or 500 HTML error views.
 * - API requests (Accept: application/json or /api/* paths): returns standard JSON ErrorResponse.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = HttpStatus.NOT_FOUND.value();

        if (statusAttr != null) {
            try {
                statusCode = Integer.parseInt(statusAttr.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path == null) {
            path = request.getRequestURI();
        }

        String acceptHeader = request.getHeader("Accept");
        boolean isHtmlRequest = acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE);
        boolean isApiRequest = path != null && path.startsWith("/api/");

        // For browser navigation (HTML), forward to user-facing themed error pages
        if (isHtmlRequest && !isApiRequest) {
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "forward:/error/404.html";
            } else if (statusCode >= 500) {
                return "forward:/error/500.html";
            }
            return "forward:/error/404.html";
        }

        // For API clients (Postman, curl, AJAX), return clean JSON ErrorResponse
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                httpStatus == HttpStatus.NOT_FOUND ? "The requested resource was not found" : "An unexpected error occurred",
                path
        );

        return ResponseEntity.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
}
