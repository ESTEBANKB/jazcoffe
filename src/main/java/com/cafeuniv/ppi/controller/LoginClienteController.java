package com.cafeuniv.ppi.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.service.AuthService;
import com.cafeuniv.ppi.service.SessionContext;

/**
 * Controlador de la pantalla de Login para Clientes.
 * Valida credenciales y verifica que el usuario tenga rol CLIENTE.
 */
public class LoginClienteController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    private final AuthService authService = new AuthService();

    @FXML
    protected void onLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Email y contraseña requeridos");
            return;
        }
        
        Usuario usuario = authService.authenticate(email, password);
        if (usuario == null) {
            errorLabel.setText("Credenciales inválidas");
            return;
        }
        
        // Verificar que el usuario tenga rol CLIENTE
        if (usuario.getRol() == null || !"CLIENTE".equals(usuario.getRol().getNombre())) {
            errorLabel.setText("Este usuario no tiene permisos de cliente");
            return;
        }
        
        errorLabel.setText("");
        SessionContext.setCurrentUser(usuario);
        loginButton.setText("Entrando...");
        
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard-cliente.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("JazzCoffee - Cliente");
            newStage.centerOnScreen();
            newStage.show();
            // Cerrar la ventana de login
            ((Stage) loginButton.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("Error cargando dashboard");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onForgot(ActionEvent event) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("JazzCoffee - Recuperación de contraseña");
        dlg.setHeaderText(null);
        dlg.setContentText("Ingresa tu email:");
        String email = dlg.showAndWait().orElse(null);
        if (email == null || email.isBlank()) return;
        
        try {
            com.cafeuniv.ppi.repository.UsuarioRepository ur = new com.cafeuniv.ppi.repository.UsuarioRepository();
            com.cafeuniv.ppi.domain.Usuario u = ur.findByEmail(email.trim().toLowerCase());
            
            if (u == null) {
                errorLabel.setText("Email no encontrado: " + email);
                return;
            }
            
            if (!u.isActivo()) {
                errorLabel.setText("Usuario inactivo. Contacta al administrador.");
                return;
            }
            
            // Verificar que sea cliente
            if (u.getRol() == null || !"CLIENTE".equals(u.getRol().getNombre())) {
                errorLabel.setText("Este email no corresponde a un cliente");
                return;
            }
            
            // Generar temporal y actualizar
            String temp = "Tmp" + Long.toHexString(System.currentTimeMillis()).substring(0,6);
            String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(10, temp.toCharArray());
            
            ur.updatePassword(u.getId(), hash);
            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Contraseña temporal generada");
            info.setHeaderText("Nueva clave temporal");
            info.setContentText("Usuario: " + u.getNombre() + "\nEmail: " + u.getEmail() + "\nClave temporal: " + temp + "\n\nUsa esta clave para iniciar sesión.");
            info.showAndWait();
            
            errorLabel.setText("");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onCreateAccount(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/registro-cliente.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Crear cuenta");
            stage.centerOnScreen();
        } catch (Exception e) {
            errorLabel.setText("No se pudo abrir el registro");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onVolver(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Bienvenido");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

