package LogicPack.Algorithms;

<<<<<<< HEAD
public interface Algozed {
=======
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
    /**
     * Calcula la ruta óptima entre dos paradas basándose en un criterio específico.
     * Parámetros:
     * grafo El grafo dirigido representado por listas de adyacencia.
     * origen La parada de inicio.
     * destino La parada final.
     * criterio El atributo a optimizar (TIEMPO, DISTANCIA, COSTO).
     * Retorno:
     * Una lista de aristas que conforman el camino más corto.
     */
    List<E> buscarRutaOptima(GrafoTransporte grafo, V origen, V destino, Pond criterio);
        //Acá dentro se implementará el algoritmo fuerte para búsqueda
>>>>>>> 23ffe8a (Creacion de interfaz, clases, metodo dentro de Ruta)
}
