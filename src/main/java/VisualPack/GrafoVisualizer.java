package VisualPack;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Ruta;
import javafx.scene.layout.Pane;
//import main.java.VisualPack.NodoVisual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafoVisualizer {

    private final Pane lienzo;
    private final Map<Parada, NodoVisual> mapaNodosVisuales;

    public GrafoVisualizer(Pane lienzo) {
        this.lienzo = lienzo;
        this.mapaNodosVisuales = new HashMap<>();
    }

    /**
     * Dibuja todo el grafo en el Pane.
     *
     */
    public void dibujarGrafo(GrafoTransporte grafo, List<Parada> todasLasParadas) {
        lienzo.getChildren().clear();
        mapaNodosVisuales.clear();

        for (Parada p : todasLasParadas) {
            NodoVisual nodoV = new NodoVisual(p);
            mapaNodosVisuales.put(p, nodoV);
        }

        // Dibujar las Rutas
        for (Parada origen : todasLasParadas) {
            NodoVisual nodoOrigen = mapaNodosVisuales.get(origen);
            List<Ruta> rutas = grafo.obtenerVecinos(origen);

            for (Ruta ruta : rutas) {
                NodoVisual nodoDestino = mapaNodosVisuales.get(ruta.getDestino());
                if (nodoDestino != null) {
                    RutaVisual rutaV = new RutaVisual(ruta, nodoOrigen, nodoDestino);
                    lienzo.getChildren().add(rutaV);
                }
            }
        }

        // Agregar los Nodos al lienzo (después de las rutas)
        lienzo.getChildren().addAll(mapaNodosVisuales.values());
    }
}