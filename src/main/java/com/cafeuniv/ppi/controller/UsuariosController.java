package com.cafeuniv.ppi.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.cafeuniv.ppi.domain.Rol;
import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.repository.UsuarioRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Administración de usuarios (solo ADMIN).
 */
public class UsuariosController {
    @FXML private TableView<Usuario> tabla;
    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> rolCombo;
    @FXML private CheckBox activoCheck;
    @FXML private ListView<String> permisosList;

    private final UsuarioRepository repo = new UsuarioRepository();

    public void initialize() {
        try {
            // Configurar columnas de la tabla
            TableColumn<Usuario, Long> colId = new TableColumn<>("ID");
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            
            TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            
            TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            
            TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
            colRol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    c.getValue().getRol() != null ? c.getValue().getRol().getNombre() : ""
            ));
            
            TableColumn<Usuario, Boolean> colActivo = new TableColumn<>("Activo");
            colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
            
            // Agregar columnas a la tabla
            tabla.getColumns().addAll(colId, colNombre, colEmail, colRol, colActivo);
            
            // Configurar combo de roles
            rolCombo.setItems(FXCollections.observableArrayList("ADMIN", "CAJERO"));
            
            // Cuando seleccionas un usuario, cargar sus datos en el formulario
            tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    nombreField.setText(newVal.getNombre());
                    emailField.setText(newVal.getEmail());
                    rolCombo.setValue(newVal.getRol() != null ? newVal.getRol().getNombre() : "CAJERO");
                    activoCheck.setSelected(newVal.isActivo());
                }
            });
            
            // Cargar datos
            reload();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error inicializando usuarios: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML public void onNuevo() {
        nombreField.clear();
        emailField.clear();
        rolCombo.getSelectionModel().clearSelection();
        activoCheck.setSelected(true);
        tabla.getSelectionModel().clearSelection();
    }

    @FXML public void onGuardar() {
        try {
            String nombre = nombreField.getText();
            String email = emailField.getText();
            String rol = rolCombo.getSelectionModel().getSelectedItem();
            boolean activo = activoCheck.isSelected();
            
            if (nombre == null || nombre.isBlank() || email == null || email.isBlank() || rol == null) {
                new Alert(Alert.AlertType.WARNING, "Todos los campos son obligatorios").showAndWait();
                return;
            }
            
            // Validar email único
            Usuario existente = repo.findByEmail(email.trim().toLowerCase());
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (existente != null && (u == null || !existente.getId().equals(u.getId()))) {
                new Alert(Alert.AlertType.WARNING, "El email ya está en uso").showAndWait();
                return;
            }
            
            if (u == null) {
                u = new Usuario();
                // Generar contraseña temporal para nuevos usuarios
                String temp = "Tmp" + Long.toHexString(System.currentTimeMillis()).substring(0,6);
                String hash = BCrypt.withDefaults().hashToString(10, temp.toCharArray());
                u.setHash(hash);
            }
            
            u.setNombre(nombre.trim());
            u.setEmail(email.trim().toLowerCase());
            Rol r = new Rol(); 
            r.setNombre(rol);
            u.setRol(r);
            u.setActivo(activo);
            
            repo.saveOrUpdate(u);
            reload();
            
            if (u.getId() == null) {
                new Alert(Alert.AlertType.INFORMATION, "Usuario creado. Clave temporal: " + 
                    (u.getHash() != null ? "Tmp" + Long.toHexString(System.currentTimeMillis()).substring(0,6) : "Generada")).showAndWait();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Usuario actualizado").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
        }
    }

    @FXML public void onEliminar() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario para eliminar").showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar usuario?");
        confirm.setContentText("¿Estás seguro de eliminar a " + sel.getNombre() + "?");
        
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                repo.delete(sel.getId());
                reload();
                new Alert(Alert.AlertType.INFORMATION, "Usuario eliminado").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML public void onCambiarRol() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String nuevo = new ChoiceDialog<>(sel.getRol() != null ? sel.getRol().getNombre() : "CAJERO",
                FXCollections.observableArrayList("ADMIN", "CAJERO")).showAndWait().orElse(null);
        if (nuevo == null) return;
        repo.updateRole(sel.getId(), nuevo);
        reload();
    }

    @FXML public void onResetPass() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario para resetear su contraseña").showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Resetear contraseña");
        confirm.setHeaderText("¿Resetear contraseña?");
        confirm.setContentText("Se generará una nueva contraseña temporal para " + sel.getNombre());
        
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                String temp = "Tmp" + Long.toHexString(System.currentTimeMillis()).substring(0,6);
                String hash = BCrypt.withDefaults().hashToString(10, temp.toCharArray());
                repo.updatePassword(sel.getId(), hash);
                
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Contraseña reseteada");
                info.setHeaderText("Nueva contraseña temporal generada");
                info.setContentText("Usuario: " + sel.getNombre() + "\nNueva clave: " + temp + "\n\nEntrégale esta clave al usuario para que inicie sesión.");
                info.showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error al resetear: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void onGestionarPermisos() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario").showAndWait();
            return;
        }
        
        try {
            // Obtener lista de TODOS los permisos disponibles en el sistema
            java.util.List<String> allPermisos = com.cafeuniv.ppi.service.PermissionService.getInstance().getAllPermissions();
            
            // IMPORTANTE: Obtener permisos PERSONALIZADOS del usuario desde la base de datos
            // Esto solo retorna los permisos que se guardaron explícitamente en permisos_json
            java.util.Set<String> currentPermisos = com.cafeuniv.ppi.service.PermissionService.getInstance().getUserPermissions(sel.getId());
            
            // Crear ventana de gestión de permisos
            javafx.stage.Stage permisoStage = new javafx.stage.Stage();
            permisoStage.setTitle("Gestionar Permisos - " + sel.getNombre());
            permisoStage.setWidth(400);
            permisoStage.setHeight(500);
            permisoStage.setResizable(false);
            
            // Crear layout principal
            javafx.scene.layout.VBox mainVBox = new javafx.scene.layout.VBox(10);
            mainVBox.setPadding(new javafx.geometry.Insets(20));
            
            // Título
            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Permisos para: " + sel.getNombre());
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            mainVBox.getChildren().add(titleLabel);
            
            // Información del rol
            String rolNombre = sel.getRol() != null ? sel.getRol().getNombre() : "Sin rol";
            javafx.scene.control.Label rolLabel = new javafx.scene.control.Label("Rol: " + rolNombre);
            rolLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
            mainVBox.getChildren().add(rolLabel);
            
            // Separador
            javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
            mainVBox.getChildren().add(separator);
            
            // ScrollPane para los checkboxes
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
            javafx.scene.layout.VBox checkBoxVBox = new javafx.scene.layout.VBox(5);
            checkBoxVBox.setPadding(new javafx.geometry.Insets(10));
            
            // Mapeo de permisos a descripciones más amigables
            java.util.Map<String, String> permisoDescriptions = new java.util.HashMap<>();
            permisoDescriptions.put("DASHBOARD_VIEW", "Ver Dashboard Principal");
            permisoDescriptions.put("POS_ACCESS", "Acceso al Punto de Venta");
            permisoDescriptions.put("INVENTARIO_VIEW", "Ver Inventario");
            permisoDescriptions.put("INVENTARIO_EDIT", "Editar Inventario");
            permisoDescriptions.put("PRODUCTOS_VIEW", "Ver Productos");
            permisoDescriptions.put("PRODUCTOS_EDIT", "Editar Productos");
            permisoDescriptions.put("USUARIOS_VIEW", "Ver Usuarios");
            permisoDescriptions.put("USUARIOS_EDIT", "Editar Usuarios");
            permisoDescriptions.put("VENTAS_VIEW", "Ver Ventas");
            permisoDescriptions.put("VENTAS_EDIT", "Editar Ventas");
            
            java.util.List<javafx.scene.control.CheckBox> checkboxes = new java.util.ArrayList<>();
            
            for (String permiso : allPermisos) {
                javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
                String description = permisoDescriptions.getOrDefault(permiso, permiso);
                checkBox.setText(description);
                checkBox.setUserData(permiso);
                checkBox.setSelected(currentPermisos.contains(permiso));
                checkboxes.add(checkBox);
                checkBoxVBox.getChildren().add(checkBox);
            }
            
            scrollPane.setContent(checkBoxVBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setMaxHeight(300);
            mainVBox.getChildren().add(scrollPane);
            
            // Botones
            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            
            javafx.scene.control.Button saveButton = new javafx.scene.control.Button("Guardar");
            saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            saveButton.setOnAction(e -> {
                // Recoger los checkboxes seleccionados en un Set
                java.util.Set<String> selectedPermisos = new java.util.HashSet<>();
                for (javafx.scene.control.CheckBox cb : checkboxes) {
                    if (cb.isSelected()) {
                        selectedPermisos.add((String) cb.getUserData());
                    }
                }
                
                // GUARDAR en la base de datos: convierte Set a JSON y persiste
                // Esto SOBRESCRIBE los permisos anteriores del usuario
                boolean success = com.cafeuniv.ppi.service.PermissionService.getInstance()
                    .updateUserPermissions(sel.getId(), selectedPermisos);
                
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Permisos actualizados correctamente").showAndWait();
                    permisoStage.close();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Error al actualizar permisos").showAndWait();
                }
            });
            
            javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Cancelar");
            cancelButton.setOnAction(e -> permisoStage.close());
            
            buttonBox.getChildren().addAll(cancelButton, saveButton);
            mainVBox.getChildren().add(buttonBox);
            
            // Configurar la escena
            javafx.scene.Scene scene = new javafx.scene.Scene(mainVBox);
            permisoStage.setScene(scene);
            permisoStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            permisoStage.show();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error gestionando permisos: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }
    
    @FXML
    public void onVerPermisos() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un usuario").showAndWait();
            return;
        }
        
        try {
            // Obtener los permisos PERSONALIZADOS desde la BD del usuario seleccionado
            // Nota: Si el usuario es ADMIN o CAJERO sin permisos personalizados,
            // esto retornará vacío y se usan los permisos por defecto del rol
            java.util.Set<String> permisos = com.cafeuniv.ppi.service.PermissionService.getInstance().getUserPermissions(sel.getId());
            
            StringBuilder sb = new StringBuilder();
            sb.append("Permisos de: ").append(sel.getNombre()).append("\n");
            sb.append("Rol: ").append(sel.getRol().getNombre()).append("\n\n");
            sb.append("Permisos personalizados guardados:\n");
            
            if (permisos.isEmpty()) {
                sb.append("Sin permisos personalizados asignados\n");
                sb.append("\n(Usará los permisos por defecto del rol)");
            } else {
                for (String permiso : permisos) {
                    sb.append("• ").append(permiso).append("\n");
                }
            }
            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Permisos del Usuario");
            info.setHeaderText("Información de Permisos");
            info.setContentText(sb.toString());
            info.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    private void reload() {
        try {
            java.util.List<Usuario> usuarios = repo.findAllWithRole();
            tabla.setItems(FXCollections.observableArrayList(usuarios));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error cargando usuarios: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }
}


