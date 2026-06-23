package com.cafeuniv.ppi.service;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Simulación de proveedor de pagos.
 * Devuelve aprobaciones o rechazos probabilísticos por método.
 */
public class PaymentService {
    private final SecureRandom random = new SecureRandom();

    public static class PaymentResult {
        public final boolean approved;
        public final String reference;
        public final String message;

        public PaymentResult(boolean approved, String reference, String message) {
            this.approved = approved;
            this.reference = reference;
            this.message = message;
        }
    }

    public PaymentResult pay(String method, double amount) {
        if (amount < 0.0) {
            return new PaymentResult(false, null, "Monto inválido");
        }
        String m = method == null ? "" : method.toLowerCase(Locale.ROOT).trim();
        switch (m) {
            case "efectivo":
                return approve("CASH" + token());
            case "tarjeta":
                // 90% éxito
                return random.nextDouble() < 0.9 ? approve("CARD" + token()) : decline("Transacción rechazada por emisor");
            case "online":
                // 85% éxito
                return random.nextDouble() < 0.85 ? approve("ONL" + token()) : decline("Pago online fallido");
            default:
                return new PaymentResult(false, null, "Método de pago no soportado");
        }
    }

    private PaymentResult approve(String ref) {
        return new PaymentResult(true, ref, "Aprobado");
    }

    private PaymentResult decline(String msg) {
        return new PaymentResult(false, null, msg);
    }

    private String token() {
        return Long.toHexString(Math.abs(random.nextLong())).substring(0, 8).toUpperCase(Locale.ROOT);
    }
}


