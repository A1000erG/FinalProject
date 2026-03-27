package VisualPack;

import LogicPack.Ruta;
import VisualPack.NodoVisual;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/**
 * Representación visual de una arista dirigida.
 *
 */
public class RutaVisual extends Group {

    private final Ruta rutaLogica;
    private final Line linea;
    private final Polygon flecha;
    private static final double RADIO = 20.0;

    public RutaVisual(Ruta rutaLogica, NodoVisual origen, NodoVisual destino) {
        this.rutaLogica = rutaLogica;
        this.linea = new Line();

        this.linea.startXProperty().bind(origen.layoutXProperty().add(RADIO));
        this.linea.startYProperty().bind(origen.layoutYProperty().add(RADIO));
        this.linea.endXProperty().bind(destino.layoutXProperty().add(RADIO));
        this.linea.endYProperty().bind(destino.layoutXProperty().add(RADIO));

        // Coordenadas centrales de los nodos
/*      double startX = origen.getLayoutX() + 20; // + radio
        double startY = origen.getLayoutY() + 20;
        double endX = destino.getLayoutX() + 20;
        double endY = destino.getLayoutY() + 20;
*/

        // 1. Crear la línea conectora
       // this.linea = new Line(startX, startY, endX, endY);
        this.linea.setStrokeWidth(2);
        this.linea.setStroke(Color.BLACK);

        // Crear la punta de flecha (Triángulo)
        this.flecha = new Polygon(0,0,-10,-5,-10,5);
        this.flecha.setFill(Color.BLACK);

        configurarActualizacionFlecha();

        this.getChildren().addAll(linea, flecha);
    }

    private void configurarActualizacionFlecha() {
        // Listener que recalcula la flecha cuando la línea se mueve
        ChangeListener<Number> listener = (obs, oldVal, newVal) -> {
            double x1 = linea.getStartX();
            double y1 = linea.getStartY();
            double x2 = linea.getEndX();
            double y2 = linea.getEndY();

            // Cálculo del ángulo de la ruta [cite: 217]
            double angulo = Math.atan2((y2 - y1), (x2 - x1));

            // Rotar la flecha hacia el destino (en grados) [cite: 218]
            flecha.setRotate(Math.toDegrees(angulo));

            // Posicionar la flecha exactamente en el borde del nodo destino [cite: 218, 219]
            // Restamos el radio del nodo al punto final (x2, y2)
            double cos = Math.cos(angulo);
            double sin = Math.sin(angulo);

            flecha.setTranslateX(x2 - RADIO * cos);
            flecha.setTranslateY(y2 - RADIO * sin);
        };

        // Escuchar cambios en las 4 coordenadas de la línea
        linea.startXProperty().addListener(listener);
        linea.startYProperty().addListener(listener);
        linea.endXProperty().addListener(listener);
        linea.endYProperty().addListener(listener);
    }

    // Método auxiliar pa calcular el ángulo de la flecha
    /*private Polygon crearPuntaDeFlecha(double x1, double y1, double x2, double y2) {
        Polygon triangulo = new Polygon(0, 0, -10, -5, -10, 5); // Forma base

        // Estos cálculos rotan la flecha hacia el destino
        double angulo = Math.atan2((y2 - y1), (x2 - x1)) * 180 / Math.PI;
        triangulo.setRotate(angulo);

        // Con esto se pone la flecha justo en el borde del nodo
        double ratio = 20.0 / Math.hypot(x2 - x1, y2 - y1);
        double offsetX = (x2 - x1) * ratio;
        double offsetY = (y2 - y1) * ratio;

        triangulo.setTranslateX(x2 - offsetX);
        triangulo.setTranslateY(y2 - offsetY);

        return triangulo;
    }*/

    public void setResaltado(boolean resaltado) {
        Color color = resaltado ? Color.ORANGE : Color.BLACK;
        linea.setStroke(color);
        flecha.setFill(color);
    }
}