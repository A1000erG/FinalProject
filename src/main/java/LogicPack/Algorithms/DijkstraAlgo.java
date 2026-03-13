package LogicPack.Algorithms;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;

import java.util.*;

import java.util.List;

/**
 * Implementación del algoritmo de Dijkstra para encontrar la ruta más corta.
 * Se utiliza una PriorityQueue para optimizar la selección del nodo con menor distancia.
 */

public class DijkstraAlgo implements Algozed<Parada,Ruta> {
    @Override
    public List<Ruta> buscarRutaOptima(GrafoTransporte grafo, Parada origen, Parada destino, Pond criterio) {

        if (origen == null || destino == null || grafo == null) return new ArrayList<>();
        if (origen.equals(destino)) return new ArrayList<>();

        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Ruta> rutaPrevia = new HashMap<>();
        Map<Parada, Parada> paradaPrevia = new HashMap<>();

        PriorityQueue<ParadaDistancia> colaPrioridad = new PriorityQueue<>(Comparator.comparingDouble(pd -> pd.distancia));

        distancias.put(origen, 0.0);
        colaPrioridad.add(new ParadaDistancia(origen, 0.0));

        while (!colaPrioridad.isEmpty()) {
            Parada actual = colaPrioridad.poll().parada;

            if (actual.equals(destino)) break;

            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                Double pesoRuta = ruta.getPond(criterio);

                if (pesoRuta < 0) {
                    throw new IllegalArgumentException("Dijkstra no admite pesos negativos. Use Bellman-Ford.");
                }

                double nuevaDistancia = distancias.get(actual) + pesoRuta;

                if (nuevaDistancia < distancias.getOrDefault(ruta.getDestino(), Double.MAX_VALUE)) {
                    distancias.put(ruta.getDestino(), nuevaDistancia);
                    paradaPrevia.put(ruta.getDestino(), actual);
                    rutaPrevia.put(ruta.getDestino(), ruta);
                    colaPrioridad.add(new ParadaDistancia(ruta.getDestino(), nuevaDistancia));
                }
            }
        }

        return reconstruirCamino(paradaPrevia, rutaPrevia, destino);
    }

    /**
     * Reconstruye la lista de rutas desde el destino hacia el origen.
     * Complejidad: O(|V|)
     */
    private List<Ruta> reconstruirCamino(Map<Parada, Parada> paradaPrevia, Map<Parada, Ruta> rutaPrevia, Parada destino) {
        LinkedList<Ruta> camino = new LinkedList<>();
        Parada paso = destino;

        if (paradaPrevia.get(paso) == null && rutaPrevia.get(paso) == null) {
            return new ArrayList<>();
        }

        while (paso != null && rutaPrevia.containsKey(paso)) {
            camino.addFirst(rutaPrevia.get(paso));
            paso = paradaPrevia.get(paso);
        }

        return camino;
    }

    /**
     * Clase interna auxiliar para la PriorityQueue.
     */
    private static class ParadaDistancia {
        Parada parada;
        double distancia;

        ParadaDistancia(Parada parada, double distancia) {
            this.parada = parada;
            this.distancia = distancia;
        }
    }
}

