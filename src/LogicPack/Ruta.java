package LogicPack;
import LogicPack.Algorithms.Algozed;

import java.util.Map;

public class Ruta {

    private Parada destino;
    private Map<Algozed, Double> atributos;

    public Ruta(Parada destino) {
        this.destino = destino;
        this.atributos = new java.util.HashMap<>();
    }

    public void agregarAtributo(Algozed tipo, Double valor) {
        atributos.put(tipo, valor);
    }

    public Double getPeso(Algozed tipo) {
        // Retorna el valor o un número muy grande si no existe el atributo
        return atributos.getOrDefault(tipo, Double.MAX_VALUE);
    }

    public Parada getDestino() {
        return destino;
    }
}
