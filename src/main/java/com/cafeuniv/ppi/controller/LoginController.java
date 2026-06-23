package com.cafeuniv.ppi.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.service.AuthService;
import com.cafeuniv.ppi.service.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla de Login.
 * Valida credenciales y navega al Dashboard.
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    private final AuthService authService = new AuthService();

    @FXML
    protected void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Usuario y contraseña requeridos");
            return;
        }
        Usuario usuario = authService.authenticate(username, password);
        if (usuario == null) {
            errorLabel.setText("Credenciales inválidas");
            return;
        }
        errorLabel.setText("");
        SessionContext.setCurrentUser(usuario);
        loginButton.setText("Entrando...");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("JazzCoffee - Dashboard");
            newStage.centerOnScreen();
            newStage.show();
            // Cerrar la ventana de login
            ((Stage) loginButton.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("Error cargando dashboard");
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


