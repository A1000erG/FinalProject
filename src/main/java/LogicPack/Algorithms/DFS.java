/*package LogicPack.Algorithms;

import LogicPack.GrafoTransporte;
import LogicPack.Pond;

import java.util.List;

public class DFS implements Algozed{
    @Override
    public List buscarRutaOptima(GrafoTransporte grafo, Object origen, Object destino, Pond criterio) {
        //IMplementación
        return List.of();
    }
}
*/
package LogicPack.Algorithms;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;

import java.util.*;

/**
 * Implementación de Búsqueda en Profundidad (DFS).
 * Implementa Algozed para buscar una ruta válida.
 * Además, provee herramientas para validar la conectividad del grafo (alcanzabilidad).
 */
public class DFS implements Algozed<Parada, Ruta> {

    @Override
    public List<Ruta> buscarRutaOptima(GrafoTransporte grafo, Parada origen, Parada destino, Pond criterio) {
        // Validación inicial preventiva
        if (origen == null || destino == null || grafo == null) return new ArrayList<>();
        if (origen.equals(destino)) return new ArrayList<>();

        // Estructuras de control
        Set<Parada> visitados = new HashSet<>();
        Map<Parada, Ruta> rutaPrevia = new HashMap<>();
        Map<Parada, Parada> paradaPrevia = new HashMap<>();

        // Usamos una Pila (Stack) para simular la recursividad de DFS de manera iterativa
        Stack<Parada> pila = new Stack<>();

        pila.push(origen);
        visitados.add(origen);

        boolean encontrado = false;

        // Bucle principal DFS: O(|V| + |E|)
        while (!pila.isEmpty()) {
            Parada actual = pila.pop();

            // Si llegamos al destino, cortamos la búsqueda temprana
            if (actual.equals(destino)) {
                encontrado = true;
                break;
            }

            // Exploramos los vecinos usando la lista de adyacencia
            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                Parada vecino = ruta.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    paradaPrevia.put(vecino, actual);
                    rutaPrevia.put(vecino, ruta);
                    pila.push(vecino); // Apilamos para profundizar en el siguiente paso
                }
            }
        }

        if (!encontrado) return new ArrayList<>();
        return reconstruirCamino(paradaPrevia, rutaPrevia, destino);
    }

    /**
     * Reconstruye el camino desde el destino hacia el origen de forma secuencial.
     * Complejidad: O(|V|) en el peor de los casos.
     */
    private List<Ruta> reconstruirCamino(Map<Parada, Parada> paradaPrevia, Map<Parada, Ruta> rutaPrevia, Parada destino) {
        LinkedList<Ruta> camino = new LinkedList<>();
        Parada paso = destino;

        while (paso != null && rutaPrevia.containsKey(paso)) {
            camino.addFirst(rutaPrevia.get(paso));
            paso = paradaPrevia.get(paso);
        }
        return camino;
    }

    /**
     * MÉTODO PARA VALIDACIÓN DE CONECTIVIDAD
     * Retorna un conjunto con todas las paradas alcanzables desde un origen dado.
     * Utilízalo antes de eliminar un nodo para verificar si el grafo sigue siendo conexo.
     * * Complejidad: O(|V| + |E|)
     */
    public Set<Parada> obtenerNodosAlcanzables(GrafoTransporte grafo, Parada origen) {
        Set<Parada> visitados = new HashSet<>();
        if (origen == null || grafo == null) return visitados;

        Stack<Parada> pila = new Stack<>();
        pila.push(origen);
        visitados.add(origen);

        while (!pila.isEmpty()) {
            Parada actual = pila.pop();
            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                Parada vecino = ruta.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    pila.push(vecino);
                }
            }
        }
        return visitados;
    }
}

