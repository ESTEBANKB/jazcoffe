package com.cafeuniv.ppi.controller;

import com.cafeuniv.ppi.domain.Categoria;
import com.cafeuniv.ppi.domain.Producto;
import com.cafeuniv.ppi.repository.CategoriaRepository;
import com.cafeuniv.ppi.repository.ProductoRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador CRUD de productos.
 * 
 * USO DE COLECCIONES:
 * - Tabla usa ObservableList<Producto> para mostrar productos.
 * - ComboBox usa ObservableList<Categoria> para categorías.
 * - NO usa arreglos tradicionales, usa colecciones de JavaFX.
 */
public class ProductosController {
    @FXML private TableView<Producto> tabla;
    @FXML private TextField nombreField;
    @FXML private ComboBox<Categoria> categoriaCombo;
    @FXML private TextField precioField;
    @FXML private TextField costoField;
    @FXML private TextField stockActualField;
    @FXML private TextField stockMinField;

    private final CategoriaRepository categoriaRepo = new CategoriaRepository();
    private final ProductoRepository productoRepo = new ProductoRepository();
    private Producto productoEditando = null; // Para controlar si estamos editando

    public void initialize() {
        TableColumn<Producto, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(180);
        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCategoria() != null ? cell.getValue().getCategoria().getNombre() : ""
        ));
        colCategoria.setPrefWidth(120);
        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(90);
        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStock.setPrefWidth(80);
        colStock.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.valueOf(value));
                Producto p = (Producto) getTableRow().getItem();
                if (p != null && p.getStockMin() != 0 && value <= p.getStockMin()) {
                    setStyle("-fx-background-color: rgba(255,0,0,0.2);");
                } else {
                    setStyle("");
                }
            }
        });
        tabla.getColumns().setAll(java.util.Arrays.asList(colId, colNombre, colCategoria, colPrecio, colStock));
        // CARGAR LISTA de categorías en el combo
        categoriaCombo.setItems(FXCollections.observableArrayList(categoriaRepo.findAll()));
        reload();
    }

    @FXML
    public void onGuardar() {
        try {
            String nombre = nombreField.getText();
            Categoria cat = categoriaCombo.getSelectionModel().getSelectedItem();
            double precio = Double.parseDouble(precioField.getText());
            double costo = Double.parseDouble(costoField.getText());
            int stock = Integer.parseInt(stockActualField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            if (nombre == null || nombre.isBlank()) return;
            
            String nombreTrimmed = nombre.trim();
            
            if (productoEditando != null) {
                // MODO EDICIÓN: Actualizar producto existente
                System.out.println("=== ACTUALIZANDO PRODUCTO ===");
                System.out.println("ID: " + productoEditando.getId());
                System.out.println("Nombre anterior: " + productoEditando.getNombre());
                System.out.println("Nombre nuevo: " + nombreTrimmed);
                
                // Verificar si el nuevo nombre ya existe en otro producto
                Producto productoExistente = productoRepo.findByNombre(nombreTrimmed);
                if (productoExistente != null && !productoExistente.getId().equals(productoEditando.getId())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de validación");
                    alert.setHeaderText("Nombre duplicado");
                    alert.setContentText("Ya existe un producto con el nombre \"" + nombreTrimmed + "\".\n\nPor favor, elige un nombre diferente.");
                    alert.showAndWait();
                    return;
                }
                
                productoEditando.setNombre(nombreTrimmed);
                productoEditando.setCategoria(cat);
                productoEditando.setPrecio(precio);
                productoEditando.setCosto(costo);
                productoEditando.setStockActual(stock);
                productoEditando.setStockMin(stockMin);
                
                productoRepo.saveOrUpdate(productoEditando);
                System.out.println("Producto actualizado exitosamente. ID: " + productoEditando.getId());
                
                productoEditando = null; // Limpiar modo edición
            } else {
                // MODO CREACIÓN: Crear producto nuevo
                System.out.println("=== CREANDO PRODUCTO NUEVO ===");
                System.out.println("Nombre: " + nombreTrimmed);
                System.out.println("Categoría: " + (cat != null ? cat.getNombre() : "null"));
                System.out.println("Precio: " + precio);
                
                // Verificar si ya existe un producto con el mismo nombre
                Producto productoExistente = productoRepo.findByNombre(nombreTrimmed);
                if (productoExistente != null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de validación");
                    alert.setHeaderText("Producto duplicado");
                    alert.setContentText("Ya existe un producto con el nombre \"" + nombreTrimmed + "\".\n\nPor favor, elige un nombre diferente o edita el producto existente.");
                    alert.showAndWait();
                    return;
                }
                
                Producto p = new Producto();
                p.setNombre(nombreTrimmed);
                p.setCategoria(cat);
                p.setPrecio(precio);
                p.setCosto(costo);
                p.setStockActual(stock);
                p.setStockMin(stockMin);
                
                productoRepo.saveOrUpdate(p);
                System.out.println("Producto creado exitosamente. ID: " + p.getId());
            }
            
            clearForm();
            reload();
        } catch (Exception e) {
            System.err.println("ERROR al guardar producto: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al guardar producto");
            alert.setContentText("Ocurrió un error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onEliminar() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un producto para eliminar").showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar producto?");
        confirm.setContentText("¿Estás seguro de eliminar el producto \"" + sel.getNombre() + "\"?\n\nEsta acción no se puede deshacer.");
        
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                productoRepo.delete(sel.getId());
                reload();
                new Alert(Alert.AlertType.INFORMATION, "Producto eliminado exitosamente").showAndWait();
            } catch (Exception e) {
                String errorMsg = "Error al eliminar producto: " + e.getMessage();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMsg += "\n\nCausa: " + e.getCause().getMessage();
                }
                new Alert(Alert.AlertType.ERROR, errorMsg).showAndWait();
                System.err.println("ERROR al eliminar producto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onEditar() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            System.out.println("=== EDITAR: No hay producto seleccionado ===");
            return;
        }
        
        System.out.println("=== EDITANDO PRODUCTO ===");
        System.out.println("ID: " + sel.getId());
        System.out.println("Nombre actual: " + sel.getNombre());
        
        // Guardar referencia al producto que estamos editando
        productoEditando = sel;
        
        // Cargar datos del producto seleccionado en el formulario
        nombreField.setText(sel.getNombre());
        categoriaCombo.setValue(sel.getCategoria());
        precioField.setText(String.valueOf(sel.getPrecio()));
        costoField.setText(String.valueOf(sel.getCosto()));
        stockActualField.setText(String.valueOf(sel.getStockActual()));
        stockMinField.setText(String.valueOf(sel.getStockMin()));
        
        System.out.println("Formulario cargado con datos del producto - MODO EDICIÓN");
    }

    private void clearForm() {
        nombreField.clear();
        categoriaCombo.getSelectionModel().clearSelection();
        precioField.clear();
        costoField.clear();
        stockActualField.clear();
        stockMinField.clear();
        productoEditando = null; // Limpiar modo edición
    }

    private void reload() {
        // CARGAR LISTA de productos en la tabla
        List<Producto> productos = productoRepo.findAll();
        tabla.setItems(FXCollections.observableArrayList(productos));
        
        // Verificar productos con stock bajo y mostrar alertas
        verificarStockBajo(productos);
    }
    
    /**
     * Verifica productos con stock bajo y muestra alertas.
     * @param productos Lista de productos a verificar
     */
    private void verificarStockBajo(List<Producto> productos) {
        List<Producto> productosBajo = new ArrayList<>();
        
        for (Producto p : productos) {
            if (p.getStockMin() > 0 && p.getStockActual() <= p.getStockMin()) {
                productosBajo.add(p);
            }
        }
        
        // Mostrar alertas para productos con stock bajo
        if (!productosBajo.isEmpty()) {
            for (Producto producto : productosBajo) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("⚠️ Stock Agotándose");
                alert.setHeaderText("El producto se está agotando");
                alert.setContentText("Producto: " + producto.getNombre() + 
                        "\n\nStock actual: " + producto.getStockActual() + 
                        "\nStock mínimo: " + producto.getStockMin() +
                        "\n\n¡Surte el producto cuanto antes!");
                alert.showAndWait();
            }
        }
    }
}
