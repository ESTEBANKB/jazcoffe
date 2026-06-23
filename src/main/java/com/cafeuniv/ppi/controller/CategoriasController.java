package com.cafeuniv.ppi.controller;

import com.cafeuniv.ppi.domain.Categoria;
import com.cafeuniv.ppi.repository.CategoriaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador CRUD de categorías.
 * 
 * USO DE COLECCIONES:
 * - Tabla usa ObservableList<Categoria> para mostrar categorías.
 * - NO usa arreglos tradicionales, usa colecciones de JavaFX.
 */
public class CategoriasController {
    @FXML private TableView<Categoria> tabla;
    @FXML private TextField nombreField;

    private final CategoriaRepository repo = new CategoriaRepository();

    public void initialize() {
        TableColumn<Categoria, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        TableColumn<Categoria, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(240);
        tabla.getColumns().setAll(java.util.Arrays.asList(colId, colNombre));
        reload();
    }

    @FXML
    public void onGuardar() {
        String nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) return;
        Categoria c = new Categoria();
        c.setNombre(nombre.trim());
        repo.saveOrUpdate(c);
        nombreField.clear();
        reload();
    }

    @FXML
    public void onEliminar() {
        Categoria sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una categoría para eliminar").showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar categoría?");
        confirm.setContentText("¿Estás seguro de eliminar la categoría \"" + sel.getNombre() + "\"?\n\nNota: Si hay productos asociados, esta operación podría fallar.");
        
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                repo.delete(sel.getId());
                reload();
                new Alert(Alert.AlertType.INFORMATION, "Categoría eliminada exitosamente").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar categoría: " + e.getMessage() + 
                    "\n\nPosible causa: Hay productos asociados a esta categoría.").showAndWait();
                e.printStackTrace();
            }
        }
    }

    private void reload() {
        // CARGAR LISTA de categorías en la tabla
        tabla.setItems(FXCollections.observableArrayList(repo.findAll()));
    }
}


