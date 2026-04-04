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


    // Si la parada no exite la agrega
    public void agregarParada(Parada p){
        adyacencias.putIfAbsent(p, new java.util.ArrayList<>());
    }

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

    // Busca en la lista de rutas del origen y elimina la que tenga ese destino.
    public void eliminarRuta(Parada origen, Parada destino){
        List<Ruta> rutas = adyacencias.get(origen);
        if (rutas != null) {
            rutas.removeIf(r -> r.getDestino().equals(destino));
        }
    }

    // Retorna todas las rutas que tiene esa parada
    public List<Ruta> obtenerVecinos(Parada p){
        return adyacencias.getOrDefault(p, new java.util.ArrayList<>());
    }


    public void conectar(Parada origen, Parada destino, Map<Pond, Double> pesosMap) {
        // 1. Verificamos que ambas paradas existan en el grafo
        if (adyacencias.containsKey(origen) && adyacencias.containsKey(destino)) {

            // 2. Creamos el objeto Ruta apuntando al destino
            Ruta nuevaRuta = new Ruta(destino);

            // 3. Pasamos todos los pesos del mapa (que viene del JSON o la UI) al objeto Ruta
            if (pesosMap != null) {
                pesosMap.forEach((tipo, valor) -> {
                    nuevaRuta.setPond(tipo, valor);
                });
            }

            // 4. Usamos tu función existente para guardarla en la lista de adyacencia
            this.agregarRuta(origen, nuevaRuta);
        } else {
            System.err.println("Error: No se pudo conectar. Una de las paradas no existe en el grafo.");
        }
    }
}