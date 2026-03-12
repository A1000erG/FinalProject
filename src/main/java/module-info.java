module SistemaGestionTransporte {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;
    opens VisualPack to javafx.fxml;

    exports VisualPack;
}