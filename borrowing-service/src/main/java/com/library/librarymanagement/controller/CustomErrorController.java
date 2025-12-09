package com.library.librarymanagement.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // 403 Forbidden
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error-403";
            }

            // 404 Not Found
            else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", "The page you are looking for does not exist.");
                return "error";
            }
        }

        model.addAttribute("statusCode", status != null ? status.toString() : "Error");
        model.addAttribute("errorMessage", errorMessage != null && !errorMessage.isEmpty() ? errorMessage : "An unexpected error occurred.");
        return "error";
    }
}