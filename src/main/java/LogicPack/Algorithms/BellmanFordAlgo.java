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

        // Estructuras para distancias y reconstrucción de camino
        Map<Parada, Double> distancias = new HashMap<>();
        Map<Parada, Ruta> rutaPrevia = new HashMap<>();
        Map<Parada, Parada> paradaPrevia = new HashMap<>();

        // Paso 1: Inicialización
        // Obtenemos todas las paradas para inicializar distancias a infinito
        // Nota: En un grafo real, esto se basaría en el conjunto de vértices V
        List<Parada> todasLasParadas = obtenerTodasLasParadas(grafo);
        for (Parada p : todasLasParadas) {
            distancias.put(p, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        int totalV = todasLasParadas.size();

        // Paso 2: Relajación de aristas (V - 1) veces
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
            // Optimización: si en una iteración no hay cambios, terminamos antes
            if (!huboCambio) break;
        }

        // Paso 3: Detección de ciclos de peso negativo
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
        // En una implementación real, GrafoTransporte debería tener un método getVertices()
        // Por ahora, usamos un Set para identificar todas las paradas únicas (orígenes y destinos)
        Set<Parada> paradas = new HashSet<>();
        // Asumiendo que podemos acceder a la estructura o iterar sobre ella
        // Aquí podrías necesitar un método en GrafoTransporte para retornar el keyset del mapa de adyacencia
        return new ArrayList<>(); // Implementar lógica de extracción de nodos
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