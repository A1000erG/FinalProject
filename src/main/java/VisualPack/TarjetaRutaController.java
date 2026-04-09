package VisualPack;

import LogicPack.Herramientas.ResultadoRuta;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class TarjetaRutaController {

    @FXML private AnchorPane contenedorPrincipal; // El panel base de tu diseño
    @FXML private Button btnCerrar;               // El botón con la "X"
    @FXML private Label lblOrigen, lblDestino;    // Tus labels actuales
    @FXML private Label lblDistancia, lblTiempo, lblCosto, lblTransbordos;

    private PruebaController mainController;


    @FXML
    public void initialize() {
        System.out.println("Inicializando TarjetaRuta... ejecutando animación.");
        ejecutarAnimacionDeEntrada();
    }

    private void ejecutarAnimacionDeEntrada() {
        // 1. Animación de Opacidad (Desvanecimiento)
        FadeTransition fade = new FadeTransition(Duration.millis(600), contenedorPrincipal);
        fade.setFromValue(0.0); // Comienza invisible
        fade.setToValue(1.0);   // Termina totalmente visible

        // 2. Animación de Posición (Subida)
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), contenedorPrincipal);
        slide.setFromY(50.0);   // Comienza 50 pixeles abajo
        slide.setToY(0.0);      // Termina en su posición original

        // 3. Ejecutamos ambas animaciones al mismo tiempo
        fade.play();
        slide.play();
    }
    /**
     * Este es el método mágico.
     * Recibe el objeto que ya calculamos y llena la interfaz.
     */
    public void configurarDatos(ResultadoRuta resultado, String nombreOrigen, String nombreDestino) {
        if (resultado == null || !resultado.existeRuta()) {
            mostrarSinRuta();
            return;
        }

        lblOrigen.setText(nombreOrigen);
        lblDestino.setText(nombreDestino);

        lblDistancia.setText(String.format("%.2f km", resultado.getDistanciaTotal()));
        lblTiempo.setText(String.format("%.0f min", resultado.getTiempoTotal()));
        lblCosto.setText(String.format("$%.2f", resultado.getCostoTotal()));
        lblTransbordos.setText(String.valueOf(resultado.getTransbordos()));
    }

    private void mostrarSinRuta() {
        lblOrigen.setText("Sin ruta disponible");
        lblDestino.setText("---");
        lblDistancia.setText("N/A");
        lblTiempo.setText("N/A");
        lblCosto.setText("N/A");
        lblTransbordos.setText("0");
    }

    @FXML
    private void onCerrarClicked() {
        AnchorPane panelContenedor = (AnchorPane) contenedorPrincipal.getParent();
        if (panelContenedor != null) {
            System.out.println("Cerrando tarjeta...");
            panelContenedor.getChildren().remove(contenedorPrincipal);
        }
    }
}