package PersistancePack;

import LogicPack.Parada;
import java.util.List;
import java.util.Map;

/**
 * Clase raíz para la serialización del Grafo de Transporte.
 * Representa matemáticamente el grafo G = (V, E).
 */
public class DatosRedJSON {

    // Representa el conjunto de Vértices (V)
    private Map<String, Parada> paradas;

    // Representa el conjunto de Aristas (E)
    private List<RutaJSON> rutas;

    /**
     * Constructor vacío: Obligatorio para la librería Jackson (O(1)).
     */
    public DatosRedJSON() {
    }

    /**
     * Constructor para empaquetar los datos antes de guardarlos.
     */
    public DatosRedJSON(Map<String, Parada> paradas, List<RutaJSON> rutas) {
        this.paradas = paradas;
        this.rutas = rutas;
    }

    // --- GETTERS Y SETTERS OBLIGATORIOS PARA JACKSON ---

    public Map<String, Parada> getParadas() {
        return paradas;
    }

    public void setParadas(Map<String, Parada> paradas) {
        this.paradas = paradas;
    }

    public List<RutaJSON> getRutas() {
        return rutas;
    }

    public void setRutas(List<RutaJSON> rutas) {
        this.rutas = rutas;
    }
}