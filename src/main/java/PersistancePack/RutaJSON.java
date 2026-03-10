package PersistancePack;

import LogicPack.Pond;
import java.util.Map;

/**
 * Data Transfer Object (DTO) para representar una Ruta (Arista) de forma plana.
 * Evita referencias circulares al guardar solo los IDs de los nodos origen y destino.
 */
public class RutaJSON {

    private String idOrigen;
    private String idDestino;

    // Almacena las ponderaciones definidas en el enum Pond (TIEMPO, DISTANCIA, COSTO, TRANSBORDOS)
    private Map<Pond, Double> pesos;

    /**
     * Constructor vacío: Obligatorio para que Jackson pueda deserializar (O(1)).
     */
    public RutaJSON() {
    }

    /**
     * Constructor para inicializar la arista rápidamente durante el guardado.
     */
    public RutaJSON(String idOrigen, String idDestino, Map<Pond, Double> pesos) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.pesos = pesos;
    }

    // --- GETTERS Y SETTERS OBLIGATORIOS PARA JACKSON ---

    public String getIdOrigen() {
        return idOrigen;
    }

    public void setIdOrigen(String idOrigen) {
        this.idOrigen = idOrigen;
    }

    public String getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(String idDestino) {
        this.idDestino = idDestino;
    }

    public Map<Pond, Double> getPesos() {
        return pesos;
    }

    public void setPesos(Map<Pond, Double> pesos) {
        this.pesos = pesos;
    }
}