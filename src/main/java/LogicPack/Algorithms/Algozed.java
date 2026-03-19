package LogicPack.Algorithms;
import LogicPack.GrafoTransporte;
import LogicPack.Pond;

import java.util.List;
/*
* Esta interfaz sirve para la estructura general que implementarán las
* clases de los algoritmos
* V representa las Nodos (parada)
* E representa las Aristas (Ruta)
* */
public interface Algozed<V,E> {

    List<E> buscarRutaOptima(GrafoTransporte grafo, V origen, V destino, Pond criterio);
        //Acá dentro se implementará el algoritmo fuerte para búsqueda
}
