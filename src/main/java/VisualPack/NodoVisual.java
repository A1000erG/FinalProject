package main.java.VisualPack;

import LogicPack.Parada;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * Representación visual de un vértice en el grafo.
 * Complejidad de creación: O(1)
 */
public class NodoVisual extends StackPane {

    private final Parada paradaLogica;
    private final Circle circulo;

    public NodoVisual(Parada parada) {
        this.paradaLogica = parada;

        // Configuración visual del nodo
        this.circulo = new Circle(20, Color.web("#2b3030")); // Gris oscuro por defecto
        this.circulo.setStroke(Color.WHITE);
        this.circulo.setStrokeWidth(2);

        Text textoNombre = new Text(parada.getId());
        textoNombre.setFill(Color.WHITE);

        // StackPane apila el texto sobre el círculo automáticamente
        this.getChildren().addAll(circulo, textoNombre);

        // Posicionamiento absoluto según las coordenadas de la Parada
        this.setLayoutX(parada.getCoordX() - 20); // Ajuste por el radio
        this.setLayoutY(parada.getCoordY() - 20);
    }

    public Parada getParadaLogica() {
        return paradaLogica;
    }

    // Métodos para cambiar colores al seleccionar (verde) o en ruta óptima (naranja)
    public void setSeleccionado(boolean seleccionado) {
        circulo.setFill(seleccionado ? Color.web("#4caf50") : Color.web("#2b3030"));
    }
}