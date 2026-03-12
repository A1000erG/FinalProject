package LogicPack;

public class GeneradorIdParada {
    private static GeneradorIdParada instancia;
    private int contador = 1;
    private GeneradorIdParada() {}

    public static GeneradorIdParada getInstancia() {
        if (instancia == null) {
            instancia = new GeneradorIdParada();
        }
        return instancia;
    }

    public String generarId() {
        return String.format("P-%03d", contador++);
    }

    // Esta función es para chequear el id más alto dentro del archivo JSON
    public void sincronizarConIdExistente(String idCargado) {
        try {
            int numeroCargado = Integer.parseInt(idCargado.substring(2));
            if (numeroCargado >= contador) {
                contador = numeroCargado + 1;
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {

        }
    }
}