package com.cafeuniv.ppi.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla de bienvenida.
 * Permite elegir entre login de cliente o empleado/administrador.
 */
public class WelcomeController {

    @FXML
    protected void openClienteLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login-cliente.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("JazzCoffee - Login Cliente");
            newStage.centerOnScreen();
            newStage.show();
            // La ventana de bienvenida permanece abierta
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openEmpleadoLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("JazzCoffee - Login Empleado");
            newStage.centerOnScreen();
            newStage.show();
            // La ventana de bienvenida permanece abierta
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

