package LogicPack;

import LogicPack.Algorithms.DFS;

import java.util.*;

public class GrafoTransporte {

    //  G=(V,E)
    // V = Paradas/Nodo | E = Rutas/Aristas
    private final Map<Parada, List<Ruta>> adyacencias;

    public GrafoTransporte() {
        this.adyacencias = new java.util.HashMap<>();
    }


    // Complejidad: O(1)
    // Si la parada no exite la agrega
    public void agregarParada(Parada p){
        adyacencias.putIfAbsent(p, new java.util.ArrayList<>());
    }

    // Complejidad: O(1)
    // Si la parada exite, esta agrega una ruta que salga de ella
    public boolean agregarRuta(Parada origen, Ruta ruta){
        if (!adyacencias.containsKey(origen)) {
            return false;
        }

        if (!adyacencias.containsKey(ruta.getDestino())){
            return false;
        }

        adyacencias.get(origen).add(ruta);
        return true;
    }

    //public void eliminarParada(Parada origen, Parada destino){}
    public boolean eliminarParada(Parada paradaAEliminar) {
        // 1. Validaciones iniciales: O(1)
        if (paradaAEliminar == null || !adyacencias.containsKey(paradaAEliminar)) {
            System.out.println("La parada no existe en la red.");
            return false;
        }

        // Caso base: Si la red tiene 1 o 2 paradas, no hay riesgo de fragmentación compleja
        if (adyacencias.size() <= 2) {
            adyacencias.remove(paradaAEliminar);
            for (List<Ruta> rutas : adyacencias.values()) {
                rutas.removeIf(r -> r.getDestino().equals(paradaAEliminar));
            }
            return true;
        }

        // 2. FASE DE BACKUP: O(|V| + |E|)
        // Respaldamos las rutas que salen de la parada
        List<Ruta> rutasSalientes = new ArrayList<>(adyacencias.get(paradaAEliminar));

        // Respaldamos y eliminamos de forma tentativa las rutas que entran a la parada
        Map<Parada, List<Ruta>> rutasEntrantesRespaldo = new HashMap<>();
        for (Map.Entry<Parada, List<Ruta>> entry : adyacencias.entrySet()) {
            Parada origen = entry.getKey();
            if (origen.equals(paradaAEliminar)) continue;

            List<Ruta> rutasHaciaDestino = new ArrayList<>();
            for (Ruta r : entry.getValue()) {
                if (r.getDestino().equals(paradaAEliminar)) {
                    rutasHaciaDestino.add(r);
                }
            }

            if (!rutasHaciaDestino.isEmpty()) {
                rutasEntrantesRespaldo.put(origen, rutasHaciaDestino);
                entry.getValue().removeAll(rutasHaciaDestino); // Eliminación tentativa
            }
        }

        // 3. FASE DE ELIMINACIÓN TENTATIVA: O(1)
        adyacencias.remove(paradaAEliminar);

        // 4. FASE DE VALIDACIÓN CON DFS: O(|V| + |E|) [cite: 290, 304]
        Parada paradaPrueba = adyacencias.keySet().iterator().next(); // Tomamos cualquier parada restante
        DFS dfs = new DFS();
        Set<Parada> nodosAlcanzables = dfs.obtenerNodosAlcanzables(this, paradaPrueba);

        // 5. FASE DE DECISIÓN Y ROLLBACK
        if (nodosAlcanzables.size() == adyacencias.size()) {
            // Éxito: La red sigue conectada
            System.out.println("Parada eliminada con éxito. La red sigue conectada.");
            return true;
        } else {
            // Fallo: Se fragmentó el grafo. Hacemos ROLLBACK: O(|V| + |E|)
            System.out.println("Error: Eliminar esta parada rompería la conectividad de la red.");

            // Restauramos la parada y sus rutas salientes
            adyacencias.put(paradaAEliminar, rutasSalientes);

            // Restauramos las rutas entrantes
            for (Map.Entry<Parada, List<Ruta>> entry : rutasEntrantesRespaldo.entrySet()) {
                adyacencias.get(entry.getKey()).addAll(entry.getValue());
            }
            return false;
        }
    }

    // Complejidad: O(E) donde E es el número de rutas del nodo
    // Busca en la lista de rutas del origen y elimina la que tenga ese destino.
    public void eliminarRuta(Parada origen, Parada destino){
        List<Ruta> rutas = adyacencias.get(origen);
        if (rutas != null) {
            rutas.removeIf(r -> r.getDestino().equals(destino));
        }
    }

    // Complejidad: $O(1)
    // Retorna la todas las tutas que tiene esa parada
    public List<Ruta> obtenerVecinos(Parada p){
        return adyacencias.getOrDefault(p, new java.util.ArrayList<>());
    }
}