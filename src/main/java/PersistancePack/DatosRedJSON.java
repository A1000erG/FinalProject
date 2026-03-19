package PersistancePack;

import LogicPack.Parada;
import java.util.List;
import java.util.Map;

/**
 * Clase raíz para la serialización del Grafo de Transporte.
 * Representa matemáticamente el grafo G = (V, E).
 */
public class DatosRedJSON {

    private Map<String, Parada> paradas;
    private List<RutaJSON> rutas;

    /**
     * Este constructor vacío es necesario para la librería Jackson a la hora de cargar los archivos
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