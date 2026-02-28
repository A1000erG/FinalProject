package LogicPack;

public class Parada {
    private final String id;
    private String nombre;
    private double coordX;
    private double coordY;

    public Parada(String id, String nombre, double coordX, double coordY) {
        this.id = id;
        this.nombre = nombre;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public String getId() {
        return id;
    }
// Conveniente no tener
//    public void setId(String id) {
//        this.id = id;
//    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getCoordX() {
        return coordX;
    }
    public void setCoordX(double coordX) {
        this.coordX = coordX;
    }

    public double getCoordY() {
        return coordY;
    }
    public void setCoordY(double coordY) {
        this.coordY = coordY;
    }


    //----------------- METODOS -----------------

    /*
    Compara esta parada con otro objeto y determina si son iguales.
    */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parada parada = (Parada) o;
        return id.equals(parada.id);
    }

    /*
    Genera un codigo que esta asociado con la parada
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}