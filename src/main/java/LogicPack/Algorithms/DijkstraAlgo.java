package LogicPack.Algorithms;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;

import java.util.*;

import java.util.List;

/**
 * Implementación del algoritmo de Dijkstra para encontrar la ruta más corta.
 * Se utiliza una PriorityQueue para optimizar la selección del nodo con menor ponderacion.
 */

public class DijkstraAlgo implements Algozed<Parada,Ruta> {
    @Override
    public List<Ruta> buscarRutaOptima(GrafoTransporte grafo, Parada origen, Parada destino, Pond criterio) {
        // Validaciones iniciales
        if (origen == null || destino == null || grafo == null) return new ArrayList<>();
        if (origen.equals(destino)) return new ArrayList<>();

        // Mapas para rastrear ponderaciones mínimas y el camino recorrido
        Map<Parada, Double> ponderaciones = new HashMap<>();
        Map<Parada, Ruta> rutaPrevia = new HashMap<>(); // Guarda la arista que llevó a la parada
        Map<Parada, Parada> paradaPrevia = new HashMap<>(); // Guarda el nodo anterior

        // PriorityQueue para seleccionar la parada con la distancia más corta acumulada
        PriorityQueue<ParadaDistancia> colaPrioridad = new PriorityQueue<>(Comparator.comparingDouble(pd -> pd.ponderacion));

        // Inicializar ponderaciones
        ponderaciones.put(origen, 0.0);
        colaPrioridad.add(new ParadaDistancia(origen, 0.0));

        while (!colaPrioridad.isEmpty()) {
            Parada actual = colaPrioridad.poll().parada;

            // Si llegamos al destino, podemos detener la búsqueda
            if (actual.equals(destino)) break;

            // Explorar vecinos
            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                Double pesoRuta = ruta.getPond(criterio);

                // Validación de pesos negativos (Dijkstra no los soporta)
                if (pesoRuta < 0) {
                    throw new IllegalArgumentException("Dijkstra no admite pesos negativos. Use Bellman-Ford.");
                }

                double nuevaDistancia = ponderaciones.get(actual) + pesoRuta;

                if (nuevaDistancia < ponderaciones.getOrDefault(ruta.getDestino(), Double.MAX_VALUE)) {
                    ponderaciones.put(ruta.getDestino(), nuevaDistancia);
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
     *
     */
    private List<Ruta> reconstruirCamino(Map<Parada, Parada> paradaPrevia, Map<Parada, Ruta> rutaPrevia, Parada destino) {
        LinkedList<Ruta> camino = new LinkedList<>();
        Parada paso = destino;

        // Si no hay rastro del destino en el mapa de previos, no hay ruta posible
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
        double ponderacion;

        ParadaDistancia(Parada parada, double ponderacion) {
            this.parada = parada;
            this.ponderacion = ponderacion;
        }
    }
}

