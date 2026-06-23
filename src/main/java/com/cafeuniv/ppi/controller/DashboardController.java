package com.cafeuniv.ppi.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import com.cafeuniv.ppi.service.CarritoService;

import org.hibernate.Session;
import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.service.SessionContext;

/**
 * Controlador del Dashboard principal.
 * Muestra métricas en vivo y abre módulos de mantenimiento.
 * 
 * USO DE COLECCIONES:
 * - XYChart.Series<String, Number> series: Lista de puntos para el gráfico de ventas.
 * - Usa series.getData().add() para agregar datos y .remove(0) para mantener solo 30 puntos.
 * - NO usa arreglos ni listas tradicionales, usa colecciones de JavaFX.
 */
public class DashboardController {
    @FXML private Label ventasHoyLabel;
    @FXML private Label ticketPromedioLabel;
    @FXML private Label topProductoLabel;
    @FXML private LineChart<String, Number> ventasChart;
    @FXML private Label notificacionFichosLabel;

    /**
     * SERIES de datos para el gráfico de línea (lista de puntos X,Y)
     */
    private XYChart.Series<String, Number> series = new XYChart.Series<>();
    private Timer timer;
    private Timer notificacionTimer;
    private int ultimoConteoFichos = 0;
    private final CarritoService carritoService = CarritoService.getInstance();

    public void initialize() {
        refreshMetrics();
        if (ventasChart != null) {
            ventasChart.getData().add(series);
        }
        startLiveFeed();
        iniciarNotificacionesFichos();
    }

    @FXML
    public void openCategorias() {
        if (!com.cafeuniv.ppi.service.PermissionService.getInstance().canAccessModule("productos")) {
            showAccessDenied("Categorías");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/categorias.fxml"));
            Stage stage = new Stage();
            stage.setTitle("JazzCoffee - Categorías");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ignored) {}
    }

    @FXML
    public void openProductos() {
        if (!com.cafeuniv.ppi.service.PermissionService.getInstance().canAccessModule("productos")) {
            showAccessDenied("Productos");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/productos.fxml"));
            Stage stage = new Stage();
            stage.setTitle("JazzCoffee - Productos");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ignored) {}
    }

    @FXML
    public void openMovimientos() {
        if (!com.cafeuniv.ppi.service.PermissionService.getInstance().canAccessModule("inventario")) {
            showAccessDenied("Movimientos de Inventario");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/movimientos.fxml"));
            Stage stage = new Stage();
            stage.setTitle("JazzCoffee - Movimientos de Inventario");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ignored) {}
    }

    @FXML
    public void openPos() {
        if (!com.cafeuniv.ppi.service.PermissionService.getInstance().canAccessModule("pos")) {
            showAccessDenied("Punto de Venta");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/pos.fxml"));
            Stage stage = new Stage();
            stage.setTitle("JazzCoffee - POS / Caja");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ignored) {}
    }

    @FXML
    public void openUsuarios() {
        if (!com.cafeuniv.ppi.service.PermissionService.getInstance().canAccessModule("usuarios")) {
            showAccessDenied("Gestión de Usuarios");
            return;
        }
        try {
            // Cargar el FXML directamente sin usar getClass().getResource()
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/usuarios.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("JazzCoffee - Gestión de Usuarios");
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, 
                "Error abriendo usuarios: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }
    
    private void showAccessDenied(String moduleName) {
        com.cafeuniv.ppi.domain.Usuario currentUser = com.cafeuniv.ppi.service.SessionContext.getCurrentUser();
        String userRole = currentUser != null && currentUser.getRol() != null ? 
            currentUser.getRol().getNombre() : "Sin rol";
        
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
            "Acceso denegado a " + moduleName + "\n\n" +
            "Tu rol actual: " + userRole + "\n" +
            "Contacta al administrador para obtener permisos.").showAndWait();
    }

    private void refreshMetrics() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Double hoy = s.createQuery("select coalesce(sum(v.total),0) from Venta v where date(v.fecha) = current_date", Double.class).uniqueResult();
            Long nVentas = s.createQuery("select count(v) from Venta v where date(v.fecha) = current_date", Long.class).uniqueResult();
            Double avg = (nVentas != null && nVentas > 0) ? (hoy / nVentas) : 0.0;
            Object[] top = s.createQuery(
                    "select d.producto.nombre, sum(d.cantidad) as qty from VentaDetalle d where date(d.venta.fecha) = current_date group by d.producto.nombre order by qty desc",
                    Object[].class).setMaxResults(1).uniqueResult();

            ventasHoyLabel.setText("Ventas hoy: $" + String.format("%.2f", hoy));
            ticketPromedioLabel.setText("Ticket promedio: $" + String.format("%.2f", avg));
            topProductoLabel.setText("Top producto: " + (top != null ? top[0] : "N/A"));
        } catch (Exception e) {
            ventasHoyLabel.setText("Ventas hoy: $0");
            ticketPromedioLabel.setText("Ticket promedio: $0");
            topProductoLabel.setText("Top producto: N/A");
        }
    }

    private void startLiveFeed() {
        if (timer != null) return;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                try (Session s = HibernateUtil.getSessionFactory().openSession()) {
                    java.time.LocalDateTime since = java.time.LocalDateTime.now().minusMinutes(1);
                    Double ultimo = s.createQuery("select coalesce(sum(v.total),0) from Venta v where v.fecha >= :since", Double.class)
                            .setParameter("since", since)
                            .uniqueResult();
                    String label = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    Platform.runLater(() -> {
                        // AGREGAR punto a la SERIES (lista)
                        series.getData().add(new XYChart.Data<>(label, ultimo));
                        // MANTENER solo 30 puntos en la lista (FIFO)
                        if (series.getData().size() > 30) {
                            series.getData().remove(0);
                        }
                        refreshMetrics();
                    });
                } catch (Exception ignored) {}
            }
        }, 1000, 5000);
    }

    @FXML
    public void backupDb() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("backup"));
            java.nio.file.Path src = java.nio.file.Paths.get("data/cafe.mv.db");
            java.nio.file.Path dst = java.nio.file.Paths.get("backup/cafe-" + System.currentTimeMillis() + ".mv.db");
            java.nio.file.Files.copy(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Respaldo creado en " + dst.toAbsolutePath()).showAndWait();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Error al respaldar: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void restoreDb() {
        try {
            // USAR .filter(), .sorted(), .findFirst() sobre un stream de paths
            java.nio.file.Path latest = java.nio.file.Files.list(java.nio.file.Paths.get("backup"))
                    .filter(p -> p.getFileName().toString().endsWith(".mv.db"))
                    .sorted((a,b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                    .findFirst().orElse(null);
            if (latest == null) {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, "No hay backups").showAndWait();
                return;
            }
            try {
                if (HibernateUtil.getSessionFactory().isOpen()) {
                    HibernateUtil.getSessionFactory().close();
                }
            } catch (Exception ignored) {}
            java.nio.file.Files.copy(latest, java.nio.file.Paths.get("data/cafe.mv.db"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Restauración exitosa. Reinicie la app.").showAndWait();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Error al restaurar: " + e.getMessage()).showAndWait();
        }
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
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
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

    @FXML
    public void onCerrarSesion() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (notificacionTimer != null) {
            notificacionTimer.cancel();
            notificacionTimer = null;
        }
        SessionContext.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
            Stage stage = (Stage) ventasHoyLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Bienvenido");
            stage.centerOnScreen();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                    "No se pudo cerrar sesión: " + e.getMessage()).showAndWait();
        }
    }
}


