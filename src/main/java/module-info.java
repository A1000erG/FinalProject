module FinalProject {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens VisualPack to javafx.fxml;
    opens LogicPack to com.fasterxml.jackson.databind;

    exports VisualPack;
    exports LogicPack;
    exports PersistancePack;
}