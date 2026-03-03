package LogicPack;

import java.util.List;
import java.util.Map;

public class GrafoTransporte {

    //  G=(V,E)
    // V = Paradas/Nodo | E = Rutas/Aristas
    private final Map<Parada, List<Ruta>> adyacencias;

    public GrafoTransporte() {
        this.adyacencias = new java.util.HashMap<>();
    }


    //----------------- METODOS -----------------

    // Complejidad: O(1)
    // Si la parada no exite la agrega
    public void agregarParada(Parada p){
        adyacencias.putIfAbsent(p, new java.util.ArrayList<>());
    }

    // Complejidad: O(1)
    // Si la parada exite, esta agrega una ruta qque salga de ella
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