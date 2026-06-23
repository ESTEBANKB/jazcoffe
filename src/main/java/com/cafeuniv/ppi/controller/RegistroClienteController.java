package com.cafeuniv.ppi.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.cafeuniv.ppi.domain.Rol;
import com.cafeuniv.ppi.domain.Usuario;
import com.cafeuniv.ppi.repository.RolRepository;
import com.cafeuniv.ppi.repository.UsuarioRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador para la creación de cuentas de clientes.
 */
public class RegistroClienteController {

    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label mensajeLabel;

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();

    @FXML
    protected void onCrearCuenta(ActionEvent event) {
        mensajeLabel.setStyle("-fx-text-fill: red;");
        mensajeLabel.setText("");

        String nombre = nombreField.getText() != null ? nombreField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String confirm = confirmField.getText() != null ? confirmField.getText() : "";

        if (nombre.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            mensajeLabel.setText("Todos los campos son obligatorios");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            mensajeLabel.setText("Email no válido");
            return;
        }

        if (password.length() < 6) {
            mensajeLabel.setText("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirm)) {
            mensajeLabel.setText("Las contraseñas no coinciden");
            return;
        }

        if (usuarioRepository.findByEmail(email) != null) {
            mensajeLabel.setText("Ya existe una cuenta con este email");
            return;
        }

        Rol clienteRol = rolRepository.findByNombre("CLIENTE");
        if (clienteRol == null) {
            clienteRol = rolRepository.createIfNotExists("CLIENTE");
        }

        if (clienteRol == null) {
            mensajeLabel.setText("No fue posible asignar el rol CLIENTE");
            return;
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setEmail(email);
        nuevo.setHash(BCrypt.withDefaults().hashToString(10, password.toCharArray()));
        nuevo.setRol(clienteRol);
        nuevo.setActivo(true);

        try {
            usuarioRepository.save(nuevo);
        } catch (Exception e) {
            mensajeLabel.setText("Error creando la cuenta: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        mensajeLabel.setStyle("-fx-text-fill: #2e7d32;");
        mensajeLabel.setText("Cuenta creada correctamente. Redirigiendo...");

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login-cliente.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Login Cliente");
            stage.centerOnScreen();
        } catch (Exception e) {
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setText("Cuenta creada pero no se pudo abrir el login");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onVolverLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login-cliente.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Login Cliente");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
