package com.cafeuniv.ppi.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito confirmado por un cliente con un ficho temporal.
 */
public class ClienteCarrito {
    private final String ficho;
    private final Long usuarioId;
    private final LocalDateTime creado;
    private final LocalDateTime expira;
    private final List<CarritoItem> items;

    public ClienteCarrito(String ficho, Long usuarioId, LocalDateTime creado, LocalDateTime expira, List<CarritoItem> items) {
        this.ficho = ficho;
        this.usuarioId = usuarioId;
        this.creado = creado;
        this.expira = expira;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public String getFicho() {
        return ficho;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getCreado() {
        return creado;
    }

    public LocalDateTime getExpira() {
        return expira;
    }

    public List<CarritoItem> getItems() {
        return items;
    }

    public double getTotal() {
        return items.stream().mapToDouble(CarritoItem::getSubtotal).sum();
    }

    public boolean estaVencido() {
        return LocalDateTime.now().isAfter(expira);
    }
}


