package LogicPack.Algorithms;
import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;

import java.util.*;

/**
 * Implementación del algoritmo Bellman-Ford.
 * Capaz de manejar pesos negativos y detectar ciclos negativos.
 */
public class BellmanFordAlgo implements Algozed<Parada, Ruta> {

    @Override
    public List<Ruta> buscarRutaOptima(GrafoTransporte grafo, Parada origen, Parada destino, Pond criterio) {
        if (origen == null || destino == null || grafo == null) return new ArrayList<>();

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Ruta> rutaPrevia = new HashMap<>();
        Map<Parada, Parada> paradaPrevia = new HashMap<>();


        List<Parada> todasLasParadas = obtenerTodasLasParadas(grafo);
        for (Parada p : todasLasParadas) {
            distancias.put(p, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        int totalV = todasLasParadas.size();

        for (int i = 1; i < totalV; i++) {
            boolean huboCambio = false;
            for (Parada u : todasLasParadas) {
                for (Ruta ruta : grafo.obtenerVecinos(u)) {
                    Parada v = ruta.getDestino();
                    Double peso = ruta.getPond(criterio);

                    if (distancias.get(u) != Double.MAX_VALUE &&
                            distancias.get(u) + peso < distancias.get(v)) {

                        distancias.put(v, distancias.get(u) + peso);
                        paradaPrevia.put(v, u);
                        rutaPrevia.put(v, ruta);
                        huboCambio = true;
                    }
                }
            }
            if (!huboCambio) break;
        }

        for (Parada u : todasLasParadas) {
            for (Ruta ruta : grafo.obtenerVecinos(u)) {
                if (distancias.get(u) != Double.MAX_VALUE &&
                        distancias.get(u) + ruta.getPond(criterio) < distancias.get(ruta.getDestino())) {
                    throw new IllegalStateException("El sistema detectó un ciclo de costo negativo infinito.");
                }
            }
        }

        return reconstruirCamino(paradaPrevia, rutaPrevia, destino);
    }

    private List<Parada> obtenerTodasLasParadas(GrafoTransporte grafo) {
        Set<Parada> paradas = new HashSet<>();
        return new ArrayList<>();
    }

    private List<Ruta> reconstruirCamino(Map<Parada, Parada> paradaPrevia, Map<Parada, Ruta> rutaPrevia, Parada destino) {
        LinkedList<Ruta> camino = new LinkedList<>();
        Parada paso = destino;

        if (!rutaPrevia.containsKey(paso)) return new ArrayList<>();

        while (paso != null && rutaPrevia.containsKey(paso)) {
            camino.addFirst(rutaPrevia.get(paso));
            paso = paradaPrevia.get(paso);
        }
        return camino;
    }
}