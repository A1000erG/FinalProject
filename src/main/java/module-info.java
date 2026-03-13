module FinalProject { // Asegúrate de que este nombre sea el mismo que el de tu carpeta raíz
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens VisualPack to javafx.fxml;
    opens LogicPack to com.fasterxml.jackson.databind;

    exports VisualPack;
    exports LogicPack;
    exports PersistancePack; // Añade esto para que el TestPack pueda verlo
}