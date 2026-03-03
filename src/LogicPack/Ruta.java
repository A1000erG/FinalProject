package LogicPack;
import java.util.EnumMap;
import java.util.Map;

public class Ruta {
    private Parada destino;

    //Este mapa (pesos) permite guardar cada valor de los pesos necesarios con su clave correspondiente
    //Con esta implementación de mapas podemos hacer un solo set y get
    //solo hace falta especificar la clave para obtener el valor o especificar cual es la
    //ponderación que se va modificar
    private Map<Pond, Double> pesos;

    public Ruta(Parada destino){
        this.destino = destino;
        pesos = new EnumMap<>(Pond.class);
    }

    public Ruta() {
        this.destino = null;
        pesos = new EnumMap<>(Pond.class);
    }

    public void setPond(Pond tipo, Double valor){
        pesos.put(tipo, valor);
    }

    public Double getPond(Pond tipo){
        return pesos.get(tipo);
    }

    //private Map<Algozed, Double> atributos;

    /*public Ruta(Parada destino) {
        this.destino = destino;
        this.atributos = new java.util.HashMap<>();
    }*/

    /*public void agregarAtributo(Algozed tipo, Double valor) {
        atributos.put(tipo, valor);
    }*/

    /*public Double getPeso(Algozed tipo) {
        // Retorna el valor o un número muy grande si no existe el atributo
        return atributos.getOrDefault(tipo, Double.MAX_VALUE);
    }*/

    public Parada getDestino() {
        return destino;
    }
}



