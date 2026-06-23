package com.cafeuniv.ppi;

import com.cafeuniv.ppi.service.PerformanceMonitor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación JavaFX principal.
 *
 * Responsabilidades:
 * - Inicializar el entorno de la app llamando a {@link com.cafeuniv.ppi.Bootstrap#init()}.
 * - Cargar la vista inicial (login.fxml) y mostrar la ventana principal.
 * - Iniciar el monitor de rendimiento integrado.
 * 
 * NOTA SOBRE COLECCIONES:
 * Este archivo no usa directamente arreglos o listas, pero los controladores
 * que carga usan listas ObservableList de JavaFX para gestionar datos.
 */
public class App extends Application {

    private static PerformanceMonitor performanceMonitor;

    @Override
    /**
     * Punto de entrada JavaFX. Configura e inicia la UI.
     * @param stage ventana principal
     * @throws IOException si falla la carga del FXML
     */
    public void start(Stage stage) throws IOException {
        // Iniciar monitor de rendimiento
        // Puedes desactivar esto comentando las siguientes líneas si no quieres monitoreo
        try {
            performanceMonitor = PerformanceMonitor.getInstance();
            // Monitorear cada 5 segundos, guardar en rendimiento_report.csv
            performanceMonitor.startMonitoring(5, "rendimiento_report.csv");
            
            // Registrar shutdown hook para detener el monitor al cerrar
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (performanceMonitor != null) {
                    performanceMonitor.shutdown();
                }
            }));
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo iniciar el monitor de rendimiento: " + e.getMessage());
        }
        
        try {
            Bootstrap.init();
        } catch (Exception e) {
            System.err.println("Error inicializando Bootstrap: " + e.getMessage());
            e.printStackTrace();
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/welcome.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("JazzCoffee - Bienvenido");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Punto de entrada del proceso Java. Lanza JavaFX.
     */
    public static void main(String[] args) {
        launch();
    }
}


