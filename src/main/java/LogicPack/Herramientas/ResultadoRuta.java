package LogicPack;

import java.util.List;


// Su trabajo es recibir la lista de rutas que sale de Dijkstra
// y pre-calcular los totales para que el controlador no tenga que hacer matemáticas.

public class ResultadoRuta {
    private final List<Ruta> camino;
    private double costoTotal = 0;
    private double tiempoTotal = 0;
    private double distanciaTotal = 0;
    private int transbordos = 0;

    public ResultadoRuta(List<Ruta> camino) {
        this.camino = camino;
        if (camino != null && !camino.isEmpty()) {
            calcularTotales();
        }
    }

    private void calcularTotales() {
        for (Ruta r : camino) {
            costoTotal += r.getPond(Pond.COSTO);
            tiempoTotal += r.getPond(Pond.TIEMPO);
            distanciaTotal += r.getPond(Pond.DISTANCIA);
        }
        // El transbordo suele ser N-1 paradas (si hay 3 rutas, hubo 2 cambios de parada)
        this.transbordos = Math.max(0, camino.size() - 1);
    }

    // Getters
    public List<Ruta> getCamino() { return camino; }
    public double getCostoTotal() { return costoTotal; }
    public double getTiempoTotal() { return tiempoTotal; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public int getTransbordos() { return transbordos; }

    public boolean existeRuta() {
        return camino != null && !camino.isEmpty();
    }
}