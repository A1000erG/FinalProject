package VisualPack;

import PersistancePack.DatosRedJSON;
import PersistancePack.RutaJSON;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import PersistancePack.GestorDatosJSON;


public class PruebaController {

    // --- ELEMENTOS DE LA INTERFAZ (UI) ---
    @FXML private Pane panelGrafo;           // Capa donde se dibujan paradas y rutas
    @FXML private ScrollPane scrollMapa;     // Contenedor para mover el mapa
    @FXML private AnchorPane panelEdicion;   // El menú lateral deslizable
    @FXML private TextField txtNombreParada; // Campo para el nombre de la parada

    // --- MOTOR LÓGICO Y PERSISTENCIA ---
    private GrafoTransporte grafo = new GrafoTransporte();
    private GestorDatosJSON gestorDatos = new GestorDatosJSON();
    private final String RUTA_ARCHIVO = "src/main/resources/datos/red_transporte.json";
    private Map<String, Parada> mapaParadas = new HashMap<>();

    // --- VARIABLES DE CONTROL Y ESTADO ---
    private double x = 0, y = 0;            // Para el arrastre del mapa
    private boolean fueArrastrado = false;  // Evita crear paradas mientras arrastras
    private double tempX = 0, tempY = 0;    // Coordenadas clicadas
    private Circle marcadorTemporal;        // El punto negro de previsualización
    private Line lineaPrevia;
    private javafx.scene.shape.Polygon flechaPrevia;

    // --- VARIABLES DE LA RUTA ---
    @FXML private TextField txtTiempoFijo;
    @FXML private TextField txtCostoFijo;

    // ==========================================
    //       INICIALIZACIÓN Y CONFIGURACIÓN
    // ==========================================

    @FXML
    public void initialize() {
        setupMapa();


        // --- NUEVO: Cargar datos guardados ---
        DatosRedJSON datosCargados = gestorDatos.cargarDatos(RUTA_ARCHIVO);

        if (datosCargados != null) {
            // 1. Recuperar Paradas
            if (datosCargados.getParadas() != null) {
                mapaParadas.putAll(datosCargados.getParadas());
                for (Parada p : mapaParadas.values()) {
                    grafo.agregarParada(p);
                    dibujarParadaEnMapa(p);
                }
            }

            // 2. Recuperar Rutas (Aquí es donde se conectan realmente en el grafo)
            if (datosCargados.getRutas() != null) {
                for (RutaJSON r : datosCargados.getRutas()) {
                    Parada origen = mapaParadas.get(r.getIdOrigen());
                    Parada destino = mapaParadas.get(r.getIdDestino());
                    if (origen != null && destino != null) {
                        grafo.conectar(origen, destino, r.getPesos());
                        dibujarLineaRuta(origen, destino);
                    }
                }
            }
        }


        javafx.application.Platform.runLater(() -> {
            scrollMapa.setHvalue(0.5);
            scrollMapa.setVvalue(0.5);
        });

        panelEdicion.setTranslateX(4000);

        // RESTRICCIÓN PARA EL CAMPO TIEMPO
        txtTiempoFijo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtTiempoFijo.setText(oldValue); // Si no es número, vuelve al valor anterior
            }
        });

        // RESTRICCIÓN PARA EL CAMPO COSTO
        txtCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtCostoFijo.setText(oldValue);
            }
        });
    }

    private void setupMapa() {
        try {
            double anchoDeseado = 1500;

            Image img = new Image(getClass().getResourceAsStream("/visual/mapa.png"),
                        anchoDeseado, 0, true, true);
            ImageView mapaView = new ImageView(img);
            mapaView.setMouseTransparent(true);     // El mapa no bloquea los clics al panel

            mapaView.setViewOrder(10.0);

            panelGrafo.getChildren().clear();
            panelGrafo.getChildren().add(mapaView);

            // Ajustar el tamaño del panel al tamaño real de la imagen
            panelGrafo.setPrefSize(img.getWidth(), img.getHeight());
            panelGrafo.setMinSize(img.getWidth(), img.getHeight());

            panelGrafo.setScaleX(1.0);
            panelGrafo.setScaleY(1.0);


        } catch (Exception e) {
            System.err.println("Error al cargar el mapa visual: " + e.getMessage());
        }
    }

    // ==========================================
    //        GESTIÓN DE EVENTOS DEL MAPA
    // ==========================================

    @FXML
    void onMapaClicked(MouseEvent event) {
        if (fueArrastrado) { fueArrastrado = false; return; }

        if (event.getClickCount() == 2) {
            this.tempX = event.getX();
            this.tempY = event.getY();

            crearMarcadorTemporal(tempX, tempY);

            if (comboConectarFijo != null) {
                comboConectarFijo.getItems().clear(); // Limpiar opciones viejas
                for (Parada p : mapaParadas.values()) {
                    comboConectarFijo.getItems().add(p.getNombre());
                }

                comboConectarFijo.setOnAction(e -> {
                    String seleccionado = comboConectarFijo.getValue();
                    System.out.println("Seleccionado en combo: " + seleccionado);
                    if (seleccionado != null) {
                        Parada destino = mapaParadas.values().stream()
                                .filter(p -> p.getNombre().equals(seleccionado))
                                .findFirst().orElse(null);

                        if (destino != null) {
                            // Dibujamos una línea temporal que se borra si cambias de opinión
                            dibujarLineaTemporal(tempX, tempY, destino);
                        }
                    }
                });
            }

            animarPanel(true);
            txtNombreParada.requestFocus();
        }
    }

    @FXML
    void onMousePressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML
    void onMouseDragged(MouseEvent event) {
        fueArrastrado = true;

        // Lógica de desplazamiento (Panning) del ScrollPane
        double deltaX = event.getSceneX() - x;
        double deltaY = event.getSceneY() - y;

        double hValue = scrollMapa.getHvalue();
        double vValue = scrollMapa.getVvalue();

        double contentWidth = panelGrafo.getWidth();
        double contentHeight = panelGrafo.getHeight();
        double viewportWidth = scrollMapa.getViewportBounds().getWidth();
        double viewportHeight = scrollMapa.getViewportBounds().getHeight();

        if (contentWidth > viewportWidth) {
            double hDelta = deltaX / (contentWidth - viewportWidth);
            scrollMapa.setHvalue(hValue - hDelta);
        }

        if (contentHeight > viewportHeight) {
            double vDelta = deltaY / (contentHeight - viewportHeight);
            scrollMapa.setVvalue(vValue - vDelta);
        }

        x = event.getSceneX();
        y = event.getSceneY();
    }


    // ==========================================
    //          LÓGICA DEL PANEL CRUD
    // ==========================================

    @FXML
    private void confirmarNuevaParada() {

        String nombre = txtNombreParada.getText();
        if (nombre == null || nombre.isEmpty()) return;

        String tiempoRaw = txtTiempoFijo.getText();
        String costoRaw = txtCostoFijo.getText();

        // VALIDACIÓN BÁSICA
        if (nombre.isEmpty() || tiempoRaw.isEmpty() || costoRaw.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Campos incompletos");
            alert.setContentText("Por favor, rellena el nombre, tiempo y costo antes de continuar.");
            alert.showAndWait();
            return;
        }

        // Si todo está bien, procedes con el Double.parseDouble y el guardado...
        double tiempo = Double.parseDouble(tiempoRaw);
        double costo = Double.parseDouble(costoRaw);



        // Crear objeto Parada
        String id = "P" + (mapaParadas.size() + 1);
        Parada nueva = new Parada(id, nombre, tempX, tempY);

        mapaParadas.put(id, nueva);
        grafo.agregarParada(nueva);

        if (lineaPrevia != null) {
            panelGrafo.getChildren().remove(lineaPrevia);
            lineaPrevia = null;
        }
        if (flechaPrevia != null) {
            panelGrafo.getChildren().remove(flechaPrevia);
            flechaPrevia = null;
        }

        String nombreDestinoFijo = comboConectarFijo.getValue();
        if (nombreDestinoFijo != null) {
            Parada destinoFijo = mapaParadas.values().stream()
                    .filter(p -> p.getNombre().equals(nombreDestinoFijo))
                    .findFirst().orElse(null);

            if (destinoFijo != null) {
                // Creamos pesos por defecto (puedes ajustarlos)
                Map<LogicPack.Pond, Double> pesosFijos = new HashMap<>();
                pesosFijos.put(LogicPack.Pond.TIEMPO, 10.0);
                pesosFijos.put(LogicPack.Pond.COSTO, 5.0);

                grafo.conectar(nueva, destinoFijo, pesosFijos);
                dibujarLineaRuta(nueva, destinoFijo); // <--- AQUÍ SE DIBUJA LA LÍNEA
            }
        }

        extraerRutasDelPanel(nueva);

        List<RutaJSON> todasLasRutas = obtenerTodasLasRutasDelGrafo();
        DatosRedJSON datosParaGuardar = new DatosRedJSON(mapaParadas, todasLasRutas);
        gestorDatos.guardarDatos(datosParaGuardar, RUTA_ARCHIVO);

        // Actualizar Visual: de marcador negro a círculo coral
        actualizarMarcadorAParadaReal(nueva);
        animarPanel(false);

        txtNombreParada.clear();
        comboConectarFijo.getSelectionModel().clearSelection();
        vboxRutas.getChildren().clear();
    }

    @FXML
    private void onCancelarEdicion() {
        if (marcadorTemporal != null) {
            panelGrafo.getChildren().remove(marcadorTemporal);
            marcadorTemporal = null;
        }
        if (flechaPrevia != null) {
            panelGrafo.getChildren().remove(flechaPrevia);
            flechaPrevia = null;
        }
        animarPanel(false);
    }

    @FXML private ComboBox<String> comboConectarFijo;

    @FXML private VBox vboxRutas;

    @FXML
    private void agregarNuevaFilaRuta() {

        System.out.println("Intentando agregar fila...");


        // 1. VALIDACIÓN CON MENSAJE VISUAL
        if (!vboxRutas.getChildren().isEmpty()) {
            HBox ultimaFila = (HBox) vboxRutas.getChildren().get(vboxRutas.getChildren().size() - 1);
            ComboBox<String> comboAnterior = (ComboBox<String>) ultimaFila.getChildren().get(0);
            if (comboAnterior.getValue() == null) return;
        }

        // --- LA CLAVE PARA LAS FLECHAS MÚLTIPLES ---
        // Creamos un contenedor para la flecha específica de ESTA fila
        final javafx.scene.Group[] miFlechaPersonal = {null};

        HBox filaRuta = new HBox(10);
        filaRuta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        filaRuta.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));

        ComboBox<String> comboConexion = new ComboBox<>();
        comboConexion.setPromptText("Parada");
        comboConexion.getStyleClass().add("campo-minimalista");
        comboConexion.setPrefWidth(110);

        // Llenar combo (Solo una vez)
        for (Parada p : mapaParadas.values()) {
            comboConexion.getItems().add(p.getNombre());
        }

        // 2. ESCUCHADOR DE LA COMBOBOX (DIBUJO INDEPENDIENTE)
        comboConexion.valueProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                Parada destino = mapaParadas.get(nuevo);
                if (destino != null) {
                    // Si esta fila ya tenía una flecha, la borramos antes de poner la nueva
                    if (miFlechaPersonal[0] != null) {
                        panelGrafo.getChildren().remove(miFlechaPersonal[0]);
                    }

                    // Dibujamos la nueva y la guardamos EN LA FILA, no en la variable global
                    // Nota: Asegúrate que dibujarLineaTemporal devuelva el Group o Line creado
                    dibujarLineaTemporal(tempX, tempY, destino);
                }
            }
        });

        // 4. TEXTFIELDS (TIEMPO Y COSTO)
        TextField txtT = new TextField();
        txtT.setPromptText("Min");
        txtT.setPrefWidth(60);
        txtT.getStyleClass().add("campo-minimalista");
        //configurarValidacionNumerica(txtT);

        TextField txtC = new TextField();
        txtC.setPromptText("$$");
        txtC.setPrefWidth(60);
        txtC.getStyleClass().add("campo-minimalista");
        //configurarValidacionNumerica(txtC);

        Button btnBorrar = new Button("✕");
        //btnBorrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5f56;");
        btnBorrar.setOnAction(e -> {
            vboxRutas.getChildren().remove(filaRuta);
            // Opcional: borrar la flecha temporal si se elimina la fila
            if (flechaPrevia != null) panelGrafo.getChildren().remove(flechaPrevia);
        });

        filaRuta.getChildren().addAll(comboConexion, txtT, txtC, btnBorrar);
        vboxRutas.getChildren().add(filaRuta);
        System.out.println("Fila añadida con éxito. Total hijos: " + vboxRutas.getChildren().size());
    }

    private List<RutaJSON> extraerRutasDelPanel(Parada origen) {
        List<RutaJSON> rutasExtraidas = new java.util.ArrayList<>();

        for (javafx.scene.Node nodo : vboxRutas.getChildren()) {
            if (!(nodo instanceof HBox)) {
                continue; // Si el nodo no es una fila (HBox), ignóralo y sigue con el siguiente
            }

            try {
                HBox fila = (HBox) nodo;

                // Verificamos que la fila tenga los elementos esperados antes de acceder por índice
                if (fila.getChildren().size() < 3) continue;

                ComboBox<String> combo = (ComboBox<String>) fila.getChildren().get(0);
                TextField txtT = (TextField) fila.getChildren().get(1);
                TextField txtC = (TextField) fila.getChildren().get(2);

                String nombreDestino = combo.getValue();
                if (nombreDestino != null && !txtT.getText().isEmpty() && !txtC.getText().isEmpty()) {
                    // Buscar la parada destino por nombre
                    Parada destino = mapaParadas.values().stream()
                            .filter(p -> p.getNombre().equals(nombreDestino))
                            .findFirst().orElse(null);

                    if (destino != null) {
                        Map<LogicPack.Pond, Double> pesos = new HashMap<>();
                        pesos.put(LogicPack.Pond.TIEMPO, Double.parseDouble(txtT.getText()));
                        pesos.put(LogicPack.Pond.COSTO, Double.parseDouble(txtC.getText()));

                        rutasExtraidas.add(new RutaJSON(origen.getId(), destino.getId(), pesos));
                        // También agregar al grafo en memoria
                        grafo.conectar(origen, destino, pesos);

                        dibujarLineaRuta(origen, destino);
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Se ingresó un valor no numérico en tiempo o costo.");
            }
        }
        return rutasExtraidas;
    }

    private List<RutaJSON> obtenerTodasLasRutasDelGrafo() {
        List<RutaJSON> todas = new java.util.ArrayList<>();

        // Recorremos cada parada del mapa de paradas para pedirle sus rutas al grafo
        for (Parada origen : mapaParadas.values()) {
            List<LogicPack.Ruta> rutasDesdeOrigen = grafo.obtenerVecinos(origen);

            for (LogicPack.Ruta ruta : rutasDesdeOrigen) {
                // Convertimos la Ruta lógica a RutaJSON (DTO)
                // Necesitamos extraer los pesos de la ruta
                Map<LogicPack.Pond, Double> pesos = new HashMap<>();
                for (LogicPack.Pond p : LogicPack.Pond.values()) {
                    Double valor = ruta.getPond(p);
                    if (valor != null) pesos.put(p, valor);
                }

                todas.add(new RutaJSON(origen.getId(), ruta.getDestino().getId(), pesos));
            }
        }
        return todas;
    }


    // ==========================================
    //          AUXILIARES VISUALES
    // ==========================================

    private void animarPanel(boolean mostrar) {
        panelEdicion.setVisible(true);

        TranslateTransition tt = new TranslateTransition(javafx.util.Duration.millis(350), panelEdicion);

        if (mostrar) {
            tt.setToX(0); // Vuelve a su posición original (dentro)
        } else {
            tt.setToX(500); // Se va a la derecha (fuera)
            tt.setOnFinished(e -> panelEdicion.setVisible(false)); // Se apaga al terminar
        }
        tt.play();
    }


    private void crearMarcadorTemporal(double x, double y) {
        // Si ya existe un marcador de un clic anterior que no se guardó, lo borramos
        if (marcadorTemporal != null) {
            panelGrafo.getChildren().remove(marcadorTemporal);
        }

        marcadorTemporal = new Circle(8, javafx.scene.paint.Color.BLACK);
        marcadorTemporal.setCenterX(x);
        marcadorTemporal.setCenterY(y);
        panelGrafo.getChildren().add(marcadorTemporal);
    }


    private void dibujarParadaEnMapa(Parada p) {
        Circle circuloParada = new Circle(10);
        circuloParada.setCenterX(p.getCoordX());
        circuloParada.setCenterY(p.getCoordY());
        circuloParada.setFill(javafx.scene.paint.Color.web("#eb8e83")); // El color de tu UI
        circuloParada.setStroke(javafx.scene.paint.Color.WHITE);
        circuloParada.setStrokeWidth(2);

        // 3. (Opcional) Añadir un efecto de sombra para que resalte
        circuloParada.setEffect(new javafx.scene.effect.DropShadow(5, javafx.scene.paint.Color.GRAY));
        circuloParada.setViewOrder(0.0); // Al frente de todo

        // 4. MUY IMPORTANTE: Añadirlo al panel visual
        panelGrafo.getChildren().add(circuloParada);
    }


    private void actualizarMarcadorAParadaReal(Parada p) {
        if (marcadorTemporal != null) {
            panelGrafo.getChildren().remove(marcadorTemporal);
            marcadorTemporal = null;
        }
        dibujarParadaEnMapa(p);
    }


    private void dibujarLineaRuta(Parada origen, Parada destino) {

        if (lineaPrevia != null) {
            panelGrafo.getChildren().remove(lineaPrevia);
            lineaPrevia = null;
        }

        double x1 = origen.getCoordX();
        double y1 = origen.getCoordY();
        double x2 = destino.getCoordX();
        double y2 = destino.getCoordY();

        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double offsetLinea = 14.0;

        double finalX = x2 - Math.cos(angulo) * offsetLinea;
        double finalY = y2 - Math.sin(angulo) * offsetLinea;

        Line linea = new Line(x1, y1, finalX, finalY);
        linea.setStroke(javafx.scene.paint.Color.web("#4A4A4A"));
        linea.setStrokeWidth(2.5);
        linea.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.BUTT);
        linea.setOpacity(1.0);

        javafx.scene.shape.Polygon flecha = crearPuntaFlecha(x1, y1, x2, y2);

        flecha.setOpacity(1.0);
        linea.setViewOrder(1.0);
        flecha.setViewOrder(1.0);

        panelGrafo.getChildren().addAll(linea, flecha);
    }


    private void dibujarLineaTemporal(double xOrigen, double yOrigen, Parada destino) {
        if (lineaPrevia != null) panelGrafo.getChildren().remove(lineaPrevia);
        if (flechaPrevia != null) panelGrafo.getChildren().remove(flechaPrevia);

        double xDest = destino.getCoordX();
        double yDest = destino.getCoordY();

        double angulo = Math.atan2(yDest - yOrigen, xDest - xOrigen);
        double offsetFlecha = 12.0;

        double compensacionGrosor = 2.0;
        double offsetLinea = offsetFlecha + compensacionGrosor;

        double finalX = xDest - Math.cos(angulo) * offsetLinea;
        double finalY = yDest - Math.sin(angulo) * offsetLinea;

        lineaPrevia = new Line(xOrigen, yOrigen, finalX, finalY);
        // ESTILO ESTÉTICO
        lineaPrevia.setStroke(javafx.scene.paint.Color.web("#4A4A4A"));
        lineaPrevia.setStrokeWidth(3);
        lineaPrevia.getStrokeDashArray().addAll(10.0, 5.0); // Línea punteada para indicar "previsualización"
        lineaPrevia.setOpacity(0.8);
        lineaPrevia.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.BUTT);

        // Flecha temporal
        flechaPrevia = crearPuntaFlecha(xOrigen, yOrigen, xDest, yDest);
        flechaPrevia.setFill(javafx.scene.paint.Color.web("#4A4A4A"));
        flechaPrevia.setOpacity(0.9);

        panelGrafo.getChildren().addAll(lineaPrevia, flechaPrevia);
    }


    private javafx.scene.shape.Polygon crearPuntaFlecha(double x1, double y1, double x2, double y2) {
        double radioFlecha = 15.0; // Tamaño de la flecha

        // Calcular el ángulo de la línea
        double angulo = Math.atan2(y2 - y1, x2 - x1);

        // Crear el triángulo (punta de flecha)
        javafx.scene.shape.Polygon flecha = new javafx.scene.shape.Polygon();
        flecha.getPoints().addAll(new Double[]{
                0.0, 0.0,
                -radioFlecha, radioFlecha / 1.5,
                -radioFlecha, -radioFlecha / 1.5
        });

        // Color y estilo (usa el mismo que la línea)
        flecha.setFill(javafx.scene.paint.Color.web("#4A4A4A"));
        //flecha.setStroke(javafx.scene.paint.Color.WHITE);
        //flecha.setStrokeWidth(0.5);

        // Rotar y posicionar la flecha al final (x2, y2)
        flecha.setRotate(Math.toDegrees(angulo));

        double offset = 12.0;
        flecha.setLayoutX(x2 - Math.cos(angulo) * offset);
        flecha.setLayoutY(y2 - Math.sin(angulo) * offset);

        return flecha;
    }

}