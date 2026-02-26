package LogicPack;

public class Ruta {
    private Parada destino;
    private double tiempo;      // Peso 1 [cite: 37]
    private double distancia;   // Peso 2 [cite: 37]
    private double costo;       // Peso 3 [cite: 37]
    private int transbordos;    // Peso 4 [cite: 37]

    public Ruta(Parada destino, double tiempo, double distancia, double costo, int transbordos) {
        this.destino = destino;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.costo = costo;
        this.transbordos = transbordos;
    }
}
