package com.cafeuniv.ppi.controller;

import com.cafeuniv.ppi.domain.Producto;
import com.cafeuniv.ppi.domain.Venta;
import com.cafeuniv.ppi.domain.VentaDetalle;
import com.cafeuniv.ppi.dto.CarritoItem;
import com.cafeuniv.ppi.dto.ClienteCarrito;
import com.cafeuniv.ppi.repository.ProductoRepository;
import com.cafeuniv.ppi.service.CarritoService;
import com.cafeuniv.ppi.service.PosService;
import com.cafeuniv.ppi.service.ReceiptService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import java.text.Normalizer;

/**
 * Controlador de la pantalla POS.
 *
 * Responsabilidades:
 * - Gestionar el carrito, cálculo de totales y confirmación de ventas.
 * - Invocar servicios de dominio y generar recibos.
 * 
 * USO DE COLECCIONES:
 * - ObservableList<VentaDetalle> carrito: Lista del carrito de compras.
 * - Se convierte a ArrayList al confirmar: new ArrayList<>(carrito).
 * - Se usa .stream() para calcular totales.
 */
public class PosController {
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField buscarProductoField;
    @FXML private TextField cantidadField;
    @FXML private TableView<VentaDetalle> carritoTable;
    @FXML private TextField descuentoField;
    @FXML private ComboBox<String> metodoPagoCombo;
    @FXML private Label totalLabel;
    @FXML private TextField fichoField;
    @FXML private Label fichoInfoLabel;
    @FXML private Label notificacionFichosLabel;

    private final ProductoRepository productoRepo = new ProductoRepository();
    private final PosService posService = new PosService();
    private final ReceiptService receiptService = new ReceiptService();
    private final CarritoService carritoService = CarritoService.getInstance();
    private final DateTimeFormatter fichoFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * OBSERVABLELIST del carrito de compras
     * ObservableList es una lista especial de JavaFX que notifica cambios a la UI
     */
    private final ObservableList<VentaDetalle> carrito = FXCollections.observableArrayList();

    // Lista completa y filtrada de productos para el ComboBox (búsqueda por nombre)
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private ClienteCarrito fichoCargado;
    private Timer notificacionTimer;
    private int ultimoConteoFichos = 0;

    /**
     * Normaliza un texto para comparaciones de búsqueda (sin acentos, minúsculas).
     */
    private String normalizeName(String input) {
        if (input == null) return "";
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        n = n.toLowerCase().replaceAll("[^a-z0-9\\s-]", "");
        n = n.trim();
        return n;
    }

    public void initialize() {
        // Cargar lista base de productos y configurar filtro para el ComboBox
        productos.setAll(productoRepo.findAll());
        productosFiltrados = new FilteredList<>(productos, p -> true);
        productoCombo.setItems(productosFiltrados);

        metodoPagoCombo.setItems(FXCollections.observableArrayList("efectivo", "tarjeta", "online"));
        metodoPagoCombo.getSelectionModel().selectFirst();

        TableColumn<VentaDetalle, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getProducto() != null ? c.getValue().getProducto().getNombre() : ""
        ));
        colProd.setPrefWidth(220);

        TableColumn<VentaDetalle, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCant.setPrefWidth(90);

        TableColumn<VentaDetalle, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colPrecio.setPrefWidth(90);

        TableColumn<VentaDetalle, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                (c.getValue().getPrecioUnitario()) * c.getValue().getCantidad()
        ).asObject());
        colSub.setPrefWidth(110);

        carritoTable.getColumns().setAll(java.util.Arrays.asList(colProd, colCant, colPrecio, colSub));
        carritoTable.setItems(carrito);
        updateTotal();
        clearFichoMessage();
        iniciarNotificacionesFichos();
        configurarBusquedaProductos();
        configurarBusquedaProductos();
    }
    
    /**
     * Inicia el sistema de notificaciones para fichos pendientes.
     * Consulta periódicamente si hay nuevos fichos y muestra notificaciones.
     */
    private void iniciarNotificacionesFichos() {
        if (notificacionTimer != null) {
            notificacionTimer.cancel();
        }
        
        notificacionTimer = new Timer("NotificacionesFichos", true);
        // Consultar cada 3 segundos si hay nuevos fichos
        notificacionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> actualizarNotificacionFichos());
            }
        }, 1000, 3000); // Iniciar después de 1 segundo, repetir cada 3 segundos
    }
    
    /**
     * Actualiza la notificación visual de fichos pendientes.
     */
    private void actualizarNotificacionFichos() {
        if (notificacionFichosLabel == null) return;
        
        int conteoActual = carritoService.contarFichosPendientes();
        
        if (conteoActual > 0) {
            // Hay fichos pendientes - mostrar notificación
            notificacionFichosLabel.setText("⚠️ " + conteoActual + " ficho(s) pendiente(s)");
            notificacionFichosLabel.setVisible(true);
            
            // Si hay nuevos fichos (más que antes), hacer parpadear
            if (conteoActual > ultimoConteoFichos) {
                hacerParpadearNotificacion();
                // Mostrar alerta solo si es un nuevo ficho (cuando había fichos antes)
                if (ultimoConteoFichos > 0) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Nuevo Ficho");
                        alert.setHeaderText("¡Nuevo ficho generado!");
                        alert.setContentText("Hay " + conteoActual + " ficho(s) pendiente(s) de atender.");
                        alert.show();
                    });
                }
            }
        } else {
            // No hay fichos pendientes - ocultar notificación
            notificacionFichosLabel.setVisible(false);
        }
        
        ultimoConteoFichos = conteoActual;
    }
    
    /**
     * Efecto de parpadeo para la notificación.
     */
    private void hacerParpadearNotificacion() {
        if (notificacionFichosLabel == null) return;
        
        // Efecto de parpadeo simple
        Timer blinkTimer = new Timer("BlinkTimer", true);
        final int[] blinkCount = {0};
        
        blinkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (notificacionFichosLabel != null) {
                        notificacionFichosLabel.setVisible(blinkCount[0] % 2 == 0);
                        blinkCount[0]++;
                        if (blinkCount[0] >= 6) { // Parpadear 3 veces (6 cambios)
                            notificacionFichosLabel.setVisible(true);
                            blinkTimer.cancel();
                        }
                    }
                });
            }
        }, 0, 300); // Parpadear cada 300ms
    }

    /**
     * Configura el campo de búsqueda para filtrar el ComboBox de productos por nombre.
     * Esto permite que cajero/admin busquen rápido entre muchos productos.
     */
    private void configurarBusquedaProductos() {
        if (buscarProductoField == null) return;

        buscarProductoField.textProperty().addListener((obs, oldValue, newValue) -> {
            String filtro = normalizeName(newValue);
            if (productosFiltrados == null) return;

            productosFiltrados.setPredicate(prod -> {
                if (prod == null) return false;
                if (filtro == null || filtro.isBlank()) return true;
                String nombreNorm = normalizeName(prod.getNombre());
                return nombreNorm.contains(filtro);
            });

            // Si hay resultados, seleccionar el primero para agilizar el flujo del cajero
            if (!productosFiltrados.isEmpty()) {
                productoCombo.getSelectionModel().selectFirst();
            }
        });
    }

    @FXML
    public void onAgregar() {
        Producto p = productoCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        int cantidad = 1;
        try { cantidad = Integer.parseInt(cantidadField.getText()); } catch (Exception ignored) {}
        if (cantidad <= 0) return;

        // Validar stock disponible antes de agregar al carrito
        int stockDisponible = p.getStockActual();
        // Sumar lo que ya está en el carrito para este producto
        int yaEnCarrito = carrito.stream()
                .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(p.getId()))
                .mapToInt(VentaDetalle::getCantidad)
                .sum();
        int totalSolicitado = yaEnCarrito + cantidad;
        if (totalSolicitado > stockDisponible) {
            new Alert(Alert.AlertType.WARNING,
                    "Stock insuficiente para " + p.getNombre() +
                            "\nDisponible: " + stockDisponible +
                            "\nSolicitado (incluyendo carrito): " + totalSolicitado).showAndWait();
            return;
        }

        VentaDetalle d = new VentaDetalle();
        d.setProducto(p);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(p.getPrecio());
        // AÑADIR a la LISTA del carrito
        carrito.add(d);
        cantidadField.clear();
        updateTotal();
    }

    @FXML
    public void onCargarFicho() {
        String codigo = fichoField != null ? fichoField.getText() : null;
        if (codigo == null || codigo.isBlank()) {
            showFichoMessage("Ingresa un código de ficho", true);
            return;
        }

        String normalizado = codigo.trim();
        ClienteCarrito carritoCliente = carritoService.buscarPorFicho(normalizado).orElse(null);
        if (carritoCliente == null) {
            showFichoMessage("Ficho " + normalizado + " no encontrado", true);
            return;
        }
        if (carritoCliente.estaVencido()) {
            carritoService.liberarFicho(normalizado);
            showFichoMessage("Ficho " + normalizado + " vencido", true);
            return;
        }

        carrito.clear();
        List<String> faltantes = new ArrayList<>();
        for (CarritoItem item : carritoCliente.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId());
            if (producto == null) {
                faltantes.add(item.getNombre());
                continue;
            }
            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            carrito.add(detalle);
        }

        if (carrito.isEmpty()) {
            showFichoMessage("El ficho no tiene productos disponibles", true);
            return;
        }

        fichoCargado = carritoCliente;
        updateTotal();
        showFichoMessage("Ficho " + normalizado + " listo (vence " +
                fichoCargado.getExpira().format(fichoFormatter) + ")", false);
        fichoField.clear();

        if (!faltantes.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Productos sin stock o eliminados: " + String.join(", ", faltantes)).showAndWait();
        }
    }

    @FXML
    public void onConfirmar() {
        try {
            double descuento = 0.0;
            try { descuento = Double.parseDouble(descuentoField.getText()); } catch (Exception ignored) {}
            String metodo = metodoPagoCombo.getSelectionModel().getSelectedItem();

            // Validar stock de todos los productos del carrito ANTES de llamar al servicio
            StringBuilder erroresStock = new StringBuilder();
            for (VentaDetalle d : carrito) {
                Producto p = d.getProducto();
                if (p == null) continue;
                // Releer desde BD por si el stock cambió
                Producto desdeDb = productoRepo.findById(p.getId());
                if (desdeDb == null) continue;
                int disponible = desdeDb.getStockActual();
                if (d.getCantidad() > disponible) {
                    erroresStock.append(String.format(
                            "- %s: disponible %d, solicitado %d%n",
                            desdeDb.getNombre(), disponible, d.getCantidad()));
                }
            }
            if (erroresStock.length() > 0) {
                new Alert(Alert.AlertType.ERROR,
                        "Stock insuficiente para los siguientes productos:\n\n" + erroresStock).showAndWait();
                return;
            }
            // CONVERTIR ObservableList a ArrayList para el servicio
            java.util.List<VentaDetalle> copia = new java.util.ArrayList<>(carrito);
            Venta v = posService.confirmarVenta(carrito, descuento, metodo, 1L);
            String content = receiptService.buildReceipt(v, copia, descuento);
            java.nio.file.Path file;
            try {
                file = receiptService.exportToPdf(content, v.getId());
            } catch (Exception ex) {
                // Fallback a TXT si falla el PDF
                file = receiptService.exportToFile(content, v.getId());
            }
            // LIMPIAR la LISTA del carrito
            carrito.clear();
            updateTotal();
            if (fichoCargado != null) {
                carritoService.liberarFicho(fichoCargado.getFicho());
                showFichoMessage("Ficho " + fichoCargado.getFicho() + " atendido", false);
                fichoCargado = null;
            } else {
                clearFichoMessage();
            }
            new Alert(Alert.AlertType.INFORMATION, "Venta #" + v.getId() + " confirmada. Recibo: " + file.toAbsolutePath()).showAndWait();
            productoCombo.setItems(FXCollections.observableArrayList(productoRepo.findAll()));
        } catch (Exception e) {
            // Buscar un mensaje útil, incluso en las causas anidadas
            String msg = e.getMessage();
            Throwable cause = e.getCause();
            while ((msg == null || msg.isBlank()) && cause != null) {
                msg = cause.getMessage();
                cause = cause.getCause();
            }
            if (msg == null || msg.isBlank()) {
                msg = "Ocurrió un error al confirmar la venta. Revisa la consola para más detalles.";
            }
            new Alert(Alert.AlertType.ERROR, msg).showAndWait();
        }
    }

    private void updateTotal() {
        // USAR .stream() para sumar los totales de la LISTA
        double subtotal = carrito.stream().mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad()).sum();
        double descuento = 0.0;
        try { descuento = Double.parseDouble(descuentoField.getText()); } catch (Exception ignored) {}
        double total = Math.max(0.0, subtotal - descuento);
        totalLabel.setText("Total: $" + total);
    }

    private void showFichoMessage(String message, boolean error) {
        if (fichoInfoLabel == null) return;
        fichoInfoLabel.setStyle(error ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
        fichoInfoLabel.setText(message);
    }

    private void clearFichoMessage() {
        if (fichoInfoLabel != null) {
            fichoInfoLabel.setText("");
        }
    }
    
    /**
     * Limpia los recursos cuando se cierra la ventana.
     * Debe llamarse desde el Stage.setOnCloseRequest().
     */
    public void limpiarRecursos() {
        if (notificacionTimer != null) {
            notificacionTimer.cancel();
            notificacionTimer = null;
        }
    }
}


