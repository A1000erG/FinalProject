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
        // 1. Validación inicial: O(1)
        if (paradaAEliminar == null || !adyacencias.containsKey(paradaAEliminar)) {
            System.out.println("La parada no existe en la red.");
            return false;
        }

        // 2. ELIMINACIÓN DE RUTAS ENTRANTES: O(|V| + |E|)
        // Recorremos todas las listas de rutas de las paradas restantes
        // y borramos cualquier flecha que tuviera como destino a 'paradaAEliminar'
        for (List<Ruta> rutas : adyacencias.values()) {
            rutas.removeIf(r -> r.getDestino().equals(paradaAEliminar));
        }

        // 3. ELIMINACIÓN DE LA PARADA Y SUS RUTAS SALIENTES: O(1)
        // Al remover la parada del mapa, desaparecen automáticamente sus rutas salientes
        adyacencias.remove(paradaAEliminar);

        System.out.println("Parada " + paradaAEliminar.getNombre() + " eliminada exitosamente.");
        return true;
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

    public void conectar(Parada origen, Parada destino, Map<Pond, Double> pesos) {
        if (adyacencias.containsKey(origen) && adyacencias.containsKey(destino)) {
            Ruta nuevaRuta = new Ruta(destino, pesos);
            adyacencias.get(origen).add(nuevaRuta);
        } else {
            System.err.println("Error: Una de las paradas no existe en el grafo.");
        }
    }

    public void eliminarRutasDesde(Parada p) {
        if (this.adyacencias.containsKey(p)) {
            this.adyacencias.get(p).clear();
        }
    }
}