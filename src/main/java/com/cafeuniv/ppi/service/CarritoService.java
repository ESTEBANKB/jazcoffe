package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.dto.CarritoItem;
import com.cafeuniv.ppi.dto.ClienteCarrito;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio en memoria para gestionar fichos de carritos de clientes.
 * Próxima etapa: persistir en BD para integrarlo con el POS.
 */
public class CarritoService {
    private static final CarritoService INSTANCE = new CarritoService();
    private final Map<String, ClienteCarrito> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger contador = new AtomicInteger(1);
    private final Duration vigencia = Duration.ofHours(1);

    private CarritoService() {}

    public static CarritoService getInstance() {
        return INSTANCE;
    }

    public ClienteCarrito crearFicho(Long usuarioId, List<CarritoItem> items) {
        limpiarVencidos();
        String codigo = generarCodigo();
        LocalDateTime ahora = LocalDateTime.now();
        ClienteCarrito carrito = new ClienteCarrito(codigo, usuarioId, ahora, ahora.plus(vigencia), new ArrayList<>(items));
        tickets.put(codigo, carrito);
        return carrito;
    }

    public Optional<ClienteCarrito> buscarPorFicho(String ficho) {
        limpiarVencidos();
        return Optional.ofNullable(tickets.get(ficho));
    }

    public void liberarFicho(String ficho) {
        tickets.remove(ficho);
    }

    /**
     * Obtiene todos los fichos pendientes (no vencidos).
     * Útil para mostrar notificaciones en el POS o Admin.
     */
    public List<ClienteCarrito> obtenerFichosPendientes() {
        limpiarVencidos();
        return new ArrayList<>(tickets.values());
    }

    /**
     * Obtiene el número de fichos pendientes.
     * Útil para mostrar contador de notificaciones.
     */
    public int contarFichosPendientes() {
        limpiarVencidos();
        return tickets.size();
    }

    private String generarCodigo() {
        return String.format("%04d", contador.getAndIncrement());
    }

    private void limpiarVencidos() {
        tickets.values().removeIf(ClienteCarrito::estaVencido);
    }
}


