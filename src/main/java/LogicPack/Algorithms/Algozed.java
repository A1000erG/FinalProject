package LogicPack.Algorithms;
import LogicPack.GrafoTransporte;
import LogicPack.Pond;

import java.util.List;

public interface Algozed<V,E> {

    List<E> buscarRutaOptima(GrafoTransporte grafo, V origen, V destino, Pond criterio);
}
