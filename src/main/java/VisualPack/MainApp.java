package VisualPack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage escenarioPrincipal) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/gui/VisualStructure.fxml"));
        Scene escena = new Scene(fxmlLoader.load(), 1200, 720);
        escena.getStylesheets().add(getClass().getResource("/gui/estilos.css").toExternalForm());

        escenarioPrincipal.setTitle("Sistema de Gestión de Rutas de Transporte Público - PUCMM");
        escenarioPrincipal.setScene(escena);

        escenarioPrincipal.setMinWidth(1000);
        escenarioPrincipal.setMinHeight(700);

        escenarioPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}