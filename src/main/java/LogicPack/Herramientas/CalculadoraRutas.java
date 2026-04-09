package LogicPack;

public class CalculadoraRutas {

    private static final double FACTOR_KM = 0.05;
    private static final double VELOCIDAD_PROMEDIO = 40.0;  // km/h

    public static double calcularDistancia(Parada p1, Parada p2) {
        double dx = p2.getCoordX() - p1.getCoordX();
        double dy = p2.getCoordY() - p1.getCoordY();
        double pixeles = Math.sqrt(dx * dx + dy * dy);
        return pixeles * FACTOR_KM;
    }

    public static double calcularTiempo(double distanciaKM) {
        // Tiempo en minutos: (Distancia / Velocidad) * 60
        return (distanciaKM / VELOCIDAD_PROMEDIO) * 60.0;
    }
}
