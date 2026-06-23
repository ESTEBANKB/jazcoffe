package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.domain.Usuario;

/**
 * Contexto sencillo de sesión para UI desktop.
 * Mantiene el usuario autenticado en memoria.
 */
public final class SessionContext {
    private static volatile Usuario currentUser;

    private SessionContext() {}

    public static void setCurrentUser(Usuario user) {
        currentUser = user;
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRol() != null && "ADMIN".equalsIgnoreCase(currentUser.getRol().getNombre());
    }
}


