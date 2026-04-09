package LogicPack.Herramientas;

import LogicPack.Pond;
import LogicPack.Ruta;

import java.util.List;


// Su trabajo es recibir la lista de rutas que sale de Dijkstra
// y pre-calcular los totales para que el controlador no tenga que hacer matemáticas.

public class ResultadoRuta {
    private final List<Ruta> rutas;
    private double costoTotal = 0;
    private double tiempoTotal = 0;
    private double distanciaTotal = 0;
    private int transbordos = 0;

    public ResultadoRuta(List<Ruta> rutas) {
        this.rutas = rutas;
        if (rutas != null && !rutas.isEmpty()) {
            calcularTotales();
        }
    }

    private void calcularTotales() {
        for (Ruta r : rutas) {
            costoTotal += r.getPond(Pond.COSTO);
            tiempoTotal += r.getPond(Pond.TIEMPO);
            distanciaTotal += r.getPond(Pond.DISTANCIA);
        }
        // El transbordo suele ser N-1 paradas (si hay 3 rutas, hubo 2 cambios de parada)
        this.transbordos = Math.max(0, rutas.size() - 1);
    }

    // Getters
    public List<Ruta> getRutas() { return rutas; }
    public double getCostoTotal() { return costoTotal; }
    public double getTiempoTotal() { return tiempoTotal; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public int getTransbordos() { return transbordos; }

    public boolean existeRuta() {
        return rutas != null && !rutas.isEmpty();
    }
}