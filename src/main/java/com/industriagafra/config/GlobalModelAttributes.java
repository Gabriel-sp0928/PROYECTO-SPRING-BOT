package com.industriagafra.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("authenticated")
    public boolean authenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && auth.getPrincipal().equals("anonymousUser"));
    }

    @ModelAttribute("currentRole")
    public String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        var authorities = auth.getAuthorities();
        if (authorities == null || authorities.isEmpty()) return null;
        String a = authorities.iterator().next().getAuthority();
        if (a.startsWith("ROLE_")) return a.substring(5);
        return a;
    }
}
