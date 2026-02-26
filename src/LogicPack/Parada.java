package LogicPack;

public class Parada {
    private String id;
    private String nombre;
    private double coordenadaX; // Para visualización en JavaFX
    private double coordenadaY;

    public Parada(String id, String nombre, double x, double y) {
        this.id = id;
        this.nombre = nombre;
        this.coordenadaX = x;
        this.coordenadaY = y;
    }

    // Getters y Setters
    // Es CRUCIAL sobrescribir equals y hashCode para que el HashMap funcione correctamente

}
