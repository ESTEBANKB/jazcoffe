package com.cafeuniv.ppi.controller;

import com.cafeuniv.ppi.domain.MovimientoInventario;
import com.cafeuniv.ppi.domain.Producto;
import com.cafeuniv.ppi.repository.ProductoRepository;
import com.cafeuniv.ppi.repository.MovimientoInventarioRepository;
import com.cafeuniv.ppi.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador de movimientos de inventario.
 * 
 * USO DE COLECCIONES:
 * - Tabla usa ObservableList<MovimientoInventario> para mostrar movimientos.
 * - ComboBox usa ObservableList<Producto> para productos.
 * - Tipo usa ObservableList<String> con valores "entrada", "salida", "ajuste".
 * - NO usa arreglos tradicionales, usa colecciones de JavaFX.
 */
public class MovimientosController {
    @FXML private TableView<MovimientoInventario> tabla;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private ComboBox<String> tipoCombo;
    @FXML private TextField cantidadField;
    @FXML private TextField motivoField;

    private final ProductoRepository productoRepo = new ProductoRepository();
    private final MovimientoInventarioRepository movRepo = new MovimientoInventarioRepository();
    private final InventarioService inventarioService = new InventarioService();

    public void initialize() {
        TableColumn<MovimientoInventario, String> colFecha = new TableColumn<>("fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colFecha.setPrefWidth(140);
        TableColumn<MovimientoInventario, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getProducto() != null ? cell.getValue().getProducto().getNombre() : ""
        ));
        colProd.setPrefWidth(180);
        TableColumn<MovimientoInventario, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(100);
        TableColumn<MovimientoInventario, Integer> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCant.setPrefWidth(100);
        tabla.getColumns().setAll(java.util.Arrays.asList(colFecha, colProd, colTipo, colCant));

        // CARGAR LISTAS en los combos
        productoCombo.setItems(FXCollections.observableArrayList(productoRepo.findAll()));
        tipoCombo.setItems(FXCollections.observableArrayList("entrada", "salida", "ajuste"));
        reload();
    }

    @FXML
    public void onRegistrar() {
        try {
            Producto p = productoCombo.getSelectionModel().getSelectedItem();
            String tipo = tipoCombo.getSelectionModel().getSelectedItem();
            int cantidad = Integer.parseInt(cantidadField.getText());
            String motivo = motivoField.getText();
            if (p == null || tipo == null) return;
            inventarioService.registrarMovimiento(p.getId(), tipo, cantidad, motivo, null);
            cantidadField.clear();
            motivoField.clear();
            reload();
        } catch (Exception ignored) {}
    }

    private void reload() {
        // CARGAR LISTA de los últimos 100 movimientos
        tabla.setItems(FXCollections.observableArrayList(movRepo.findLast(100)));
    }
}
