package LogicPack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Parada {
    private final String id;
    private String nombre;
    private double coordX;
    private double coordY;
    @JsonCreator
    //Constructor pa JSON
    public Parada(@JsonProperty("id") String id,
                  @JsonProperty("nombre") String nombre,
                  @JsonProperty("coordX") double coordX,
                  @JsonProperty("coordY") double coordY) {
        this.id = id;
        this.nombre = nombre;
        this.coordX = coordX;
        this.coordY = coordY;
    }
    //Constructor normal
    public Parada(String nombre, double coordX, double coordY) {
        this.id = LogicPack.GeneradorIdParada.getInstancia().generarId();
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

}