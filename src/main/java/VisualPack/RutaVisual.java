package VisualPack;

import LogicPack.Ruta;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/**
 * Representación visual de una arista dirigida.
 */
public class RutaVisual extends Group {

    private final Ruta rutaLogica;
    private final Line linea;
    private final Polygon flecha;

    public RutaVisual(Ruta rutaLogica, NodoVisual origen, NodoVisual destino) {
        this.rutaLogica = rutaLogica;

        double startX = origen.getLayoutX() + 20; // + radio
        double startY = origen.getLayoutY() + 20;
        double endX = destino.getLayoutX() + 20;
        double endY = destino.getLayoutY() + 20;

        this.linea = new Line(startX, startY, endX, endY);
        this.linea.setStrokeWidth(2);
        this.linea.setStroke(Color.BLACK);

        this.flecha = crearPuntaDeFlecha(startX, startY, endX, endY);

        this.getChildren().addAll(linea, flecha);
    }

    // Método auxiliar pa calcular el ángulo de la flecha
    private Polygon crearPuntaDeFlecha(double x1, double y1, double x2, double y2) {
        Polygon triangulo = new Polygon(0, 0, -10, -5, -10, 5); // Forma base

        double angulo = Math.atan2((y2 - y1), (x2 - x1)) * 180 / Math.PI;
        triangulo.setRotate(angulo);

        double ratio = 20.0 / Math.hypot(x2 - x1, y2 - y1);
        double offsetX = (x2 - x1) * ratio;
        double offsetY = (y2 - y1) * ratio;

        triangulo.setTranslateX(x2 - offsetX);
        triangulo.setTranslateY(y2 - offsetY);

        return triangulo;
    }

    public void setResaltado(boolean resaltado) {
        Color color = resaltado ? Color.ORANGE : Color.BLACK;
        linea.setStroke(color);
        flecha.setFill(color);
    }
}