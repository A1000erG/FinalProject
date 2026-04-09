package LogicPack.Herramientas;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;
import LogicPack.Algorithms.*;
import java.util.List;


public class RutaService {
    private final GrafoTransporte grafo;
    private final Algozed<Parada, Ruta> dijkstra;
    private final Algozed<Parada, Ruta> bellmanFord;

    public RutaService(GrafoTransporte grafo) {
        this.grafo = grafo;

        this.dijkstra = new DijkstraAlgo();
        this.bellmanFord = new BellmanFordAlgo();
    }

    public ResultadoRuta obtenerRuta(Parada origen, Parada destino, Pond criterio) {
        if (origen == null || destino == null) return new ResultadoRuta(null);

        List<Ruta> listaRutas;

        // LÓGICA DE DECISIÓN:
        // Si el criterio es COSTO, usamos Bellman-Ford por si hay porcentajes negativos
        if (criterio == Pond.COSTO) {
            System.out.println("Criterio: Costo. Ejecutando Bellman-Ford...");
            listaRutas = bellmanFord.buscarRutaOptima(grafo, origen, destino, criterio);
        } else {
            // Para tiempo, distancia o transbordos usamos Dijkstra (más rápido)
            System.out.println("Criterio estándar. Ejecutando Dijkstra...");
            listaRutas = dijkstra.buscarRutaOptima(grafo, origen, destino, criterio);
        }

        return new ResultadoRuta(listaRutas);
    }

    // Aquí más adelante agregaremos el método para la ruta Alterna
}