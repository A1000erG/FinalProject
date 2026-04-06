package VisualPack;

import LogicPack.Algorithms.DijkstraAlgo;
import LogicPack.Pond;
import LogicPack.Ruta;
import PersistancePack.DatosRedJSON;
import PersistancePack.RutaJSON;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import PersistancePack.GestorDatosJSON;
import javafx.util.StringConverter;


public class PruebaController {

    // --- ELEMENTOS DE LA INTERFAZ (UI) ---
    @FXML private Pane panelGrafo;           // Capa donde se dibujan paradas y rutas
    @FXML private ScrollPane scrollMapa;     // Contenedor para mover el mapa
    @FXML private AnchorPane panelEdicion;   // El menú lateral deslizable
    @FXML private TextField txtNombreParada; // Campo para el nombre de la parada
    @FXML private AnchorPane contenedorLateral; // El espacio físico en la derecha

    // --- MOTOR LÓGICO Y PERSISTENCIA ---
    private GrafoTransporte grafo = new GrafoTransporte();
    private GestorDatosJSON gestorDatos = new GestorDatosJSON();
    private final String RUTA_ARCHIVO = "src/main/resources/datos/red_transporte.json";
    private Map<String, Parada> mapaParadas = new HashMap<>();
    private Parada paradaEnEdicion = null;

    // --- VARIABLES DE CONTROL Y ESTADO ---
    private double x = 0, y = 0;            // Para el arrastre del mapa
    private boolean fueArrastrado = false;  // Evita crear paradas mientras arrastras
    private double tempX = 0, tempY = 0;    // Coordenadas clicadas
    private Circle marcadorTemporal;        // El punto negro de previsualización
    private Line lineaPrevia;
    private javafx.scene.shape.Polygon flechaPrevia;

    // --- VARIABLES DE CALCULO DE MEJOR RUTA ---
    @FXML private ComboBox<Parada> cbOrigen;
    @FXML private ComboBox<Parada> cbDestino;
    @FXML private RadioButton rbTiempo, rbDistancia, rbCosto, rbTransbordos;

    private DijkstraAlgo dijkstra = new DijkstraAlgo();


    // ==========================================
    //       INICIALIZACIÓN Y CONFIGURACIÓN
    // ==========================================

    @FXML
    public void initialize() {
        setupMapa();

        // --- Cargar datos guardados ---
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

            // Recuperar Rutas (Aquí es donde se conectan realmente en el grafo)
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

        if (cbOrigen != null && cbDestino != null) {
            // Creamos la lista con las paradas que acabamos de cargar
            ObservableList<Parada> listaParadas = FXCollections.observableArrayList(mapaParadas.values());

            cbOrigen.setItems(listaParadas);
            cbDestino.setItems(listaParadas);

            // Configuramos el convertidor para mostrar nombres en lugar de IDs de memoria
            StringConverter<Parada> converter = new StringConverter<Parada>() {
                @Override
                public String toString(Parada p) {
                    return (p == null) ? "" : p.getNombre();
                }

                @Override
                public Parada fromString(String s) {
                    return null;
                }
            };

            cbOrigen.setConverter(converter);
            cbDestino.setConverter(converter);
        } else {
            // Si sale este mensaje, revisa los fx:id en Scene Builder
            System.err.println("¡ALERTA! cbOrigen o cbDestino son NULL. Revisa fx:id en Scene Builder.");
        }

        // Centrar mapa
        javafx.application.Platform.runLater(() -> {
            scrollMapa.setHvalue(0.5);
            scrollMapa.setVvalue(0.5);
        });

        // Esconder panel de edición
        panelEdicion.setTranslateX(4000);
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
        panelGrafo.getChildren().removeIf(n ->
                n.getStyleClass().contains("popup-info") ||
                        (n instanceof Circle && ((Circle) n).getFill() == Color.BLACK)
        );

        if (fueArrastrado) { fueArrastrado = false; return; }

        if (event.getClickCount() == 2) {

            this.paradaEnEdicion = null;
            cargarPanelCreacion();

            this.tempX = event.getX();
            this.tempY = event.getY();
            crearMarcadorTemporal(tempX, tempY);

            animarPanel(true);
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

        panelGrafo.getChildren().removeIf(n -> n.getStyleClass().contains("popup-info"));

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

        // VALIDACIÓN BÁSICA
        if (nombre == null || nombre.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Campos incompletos");
            alert.setContentText("Por favor, rellena el nombre de la parada antes de continuar.");
            alert.showAndWait();
            return;
        }

        Parada paradaAProcesar;

        if (this.paradaEnEdicion != null) {
            // CASO EDICIÓN: Usamos la parada existente
            paradaAProcesar = this.paradaEnEdicion;
            paradaAProcesar.setNombre(nombre); // Actualizamos el nombre por si lo cambió

            grafo.eliminarRutasDesde(paradaAProcesar);

        } else {
            // CASO CREACIÓN: Creamos una nueva instancia
            String id = "P" + (mapaParadas.size() + 1);
            paradaAProcesar = new Parada(id, nombre, tempX, tempY);
            mapaParadas.put(id, paradaAProcesar);
            grafo.agregarParada(paradaAProcesar);
        }

        // LIMPIEZA DE PREVISUALIZACIONES (Líneas punteadas)
        if (lineaPrevia != null) {
            panelGrafo.getChildren().remove(lineaPrevia);
            lineaPrevia = null;
        }
        if (flechaPrevia != null) {
            panelGrafo.getChildren().remove(flechaPrevia);
            flechaPrevia = null;
        }

        for (Node n : previsualizacionesTemporales) {
            panelGrafo.getChildren().remove(n);
        }
        previsualizacionesTemporales.clear();

        // PROCESAR CONEXIONES (Extrae todo lo que hay en el VBox)
        extraerRutasDelPanel(paradaAProcesar);

        // PERSISTENCIA Y GUARDADO
        List<RutaJSON> todasLasRutas = obtenerTodasLasRutasDelGrafo();
        DatosRedJSON datosParaGuardar = new DatosRedJSON(mapaParadas, todasLasRutas);
        gestorDatos.guardarDatos(datosParaGuardar, RUTA_ARCHIVO);


        panelGrafo.getChildren().removeIf(n -> n instanceof Circle && ((Circle) n).getFill() == Color.BLACK);

        // ACTUALIZAR INTERFAZ
        actualizarMarcadorAParadaReal(paradaAProcesar);
        animarPanel(false);

        // RESETEO DE CAMPOS Y VARIABLE DE CONTROL
        txtNombreParada.clear();
        vboxRutas.getChildren().clear();
        this.paradaEnEdicion = null;
        this.tempX = 0;
        this.tempY = 0;
    }

    private void cargarPanelCreacion() {
        try {
            // 1. Verificar si el archivo FXML existe
            var resource = getClass().getResource("/gui/NuevaParada.fxml");
            if (resource == null) {
                System.err.println("ERROR: No se encontró el archivo /gui/NuevaParada.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            AnchorPane view = loader.load();

            // 2. Limpiar e insertar en el contenedor
            if (contenedorLateral != null) {
                contenedorLateral.getChildren().setAll(view);
            } else {
                System.err.println("ERROR: contenedorLateral es NULL");
                return;
            }

            // 3. Vincular componentes con seguridad
            txtNombreParada = (TextField) view.lookup("#txtNombreParada");
            vboxRutas = (VBox) view.lookup("#vboxRutas");

            // Si usas un botón de cerrar en ese panel, vincúlalo aquí también
            Button btnCerrar = (Button) view.lookup("#btnCerrarNueva");
            if (btnCerrar != null) btnCerrar.setOnAction(e -> animarPanel(false));

        } catch (IOException e) {
            System.err.println("FALLO CRÍTICO al cargar el FXML:");
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancelarEdicion() {
        // Limpia las líneas y flechas guardadas en la lista de rastreo
        panelGrafo.getChildren().removeAll(previsualizacionesTemporales);
        previsualizacionesTemporales.clear();

        // Limpia la previsualización principal (la del primer ComboBox)
        if (lineaPrevia != null) panelGrafo.getChildren().remove(lineaPrevia);
        if (flechaPrevia != null) panelGrafo.getChildren().remove(flechaPrevia);
        if (marcadorTemporal != null) panelGrafo.getChildren().remove(marcadorTemporal);

        vboxRutas.getChildren().clear();
        animarPanel(false);
    }


    @FXML private VBox vboxRutas;

    List<javafx.scene.Node> previsualizacionesTemporales = new java.util.ArrayList<>();
    @FXML private void agregarNuevaFilaRuta() {

        System.out.println("Intentando agregar fila...");

        final Line[] miLineaLocal = {null};
        final javafx.scene.shape.Polygon[] miFlechaLocal = {null};

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
                Parada destino = mapaParadas.values().stream()
                        .filter(p -> p.getNombre().equals(nuevo))
                        .findFirst().orElse(null);
                if (destino != null) {
                    if (miLineaLocal[0] != null) panelGrafo.getChildren().remove(miLineaLocal[0]);
                    if (miFlechaLocal[0] != null) panelGrafo.getChildren().remove(miFlechaLocal[0]);

                    double xDest = destino.getCoordX();
                    double yDest = destino.getCoordY();

                    double angulo = Math.atan2(yDest - tempY, xDest - tempX);
                    double offsetLinea = 30.0;

                    double xFinalAjustado = xDest - Math.cos(angulo) * offsetLinea;
                    double yFinalAjustado = yDest - Math.sin(angulo) * offsetLinea;

                    // Línea punteada
                    miLineaLocal[0] = new Line(tempX, tempY, xFinalAjustado, yFinalAjustado);
                    miLineaLocal[0].setStroke(javafx.scene.paint.Color.web("#4A4A4A"));
                    miLineaLocal[0].setStrokeWidth(2.5);
                    miLineaLocal[0].getStrokeDashArray().addAll(10.0, 5.0);
                    miLineaLocal[0].setOpacity(0.8);
                    miLineaLocal[0].setViewOrder(1.0);

                    // Flecha punteada
                    miFlechaLocal[0] = crearPuntaFlecha(tempX, tempY, xDest, yDest);
                    miFlechaLocal[0].setOpacity(0.9);
                    miFlechaLocal[0].setViewOrder(0.0);

                    previsualizacionesTemporales.add(miLineaLocal[0]);
                    previsualizacionesTemporales.add(miFlechaLocal[0]);
                    panelGrafo.getChildren().addAll(miLineaLocal[0], miFlechaLocal[0]);
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

        // 3. BOTÓN BORRAR (También debe limpiar su previsualización)
        StackPane btnBorrar = new StackPane();
        btnBorrar.setPrefSize(30, 30);
        btnBorrar.setMinSize(30, 30);
        btnBorrar.setCursor(javafx.scene.Cursor.HAND);

        // Fondo
        javafx.scene.shape.Circle circuloFondo = new javafx.scene.shape.Circle(15);
        circuloFondo.setFill(javafx.scene.paint.Color.web("#f0f0f0"));

        javafx.scene.shape.Polygon iconoX = new javafx.scene.shape.Polygon();
        // Coordenadas para dibujar una 'X'
        iconoX.getPoints().addAll(new Double[]{
                5.0, 5.0,   2.5, 7.5,   7.5, 12.5,  2.5, 17.5,
                5.0, 20.0,  10.0, 15.0, 15.0, 20.0, 17.5, 17.5,
                12.5, 12.5, 17.5, 7.5,  15.0, 5.0,  10.0, 10.0
        });
        iconoX.setFill(javafx.scene.paint.Color.web("#999999"));
        iconoX.setScaleX(0.6);
        iconoX.setScaleY(0.6);

        btnBorrar.setOnMouseEntered(e -> {
            circuloFondo.setFill(javafx.scene.paint.Color.web("#ffcccc")); // Fondo rosa
            iconoX.setFill(javafx.scene.paint.Color.web("#ff5555"));     // Icono rojo
        });
        btnBorrar.setOnMouseExited(e -> {
            circuloFondo.setFill(javafx.scene.paint.Color.web("#f0f0f0")); // Fondo gris
            iconoX.setFill(javafx.scene.paint.Color.web("#999999"));     // Icono gris
        });

        btnBorrar.setOnMouseClicked(e -> {
            vboxRutas.getChildren().remove(filaRuta);
            if (miLineaLocal[0] != null) {
                panelGrafo.getChildren().remove(miLineaLocal[0]);
                previsualizacionesTemporales.remove(miLineaLocal[0]); // Quitar del rastreo
            }
            if (miFlechaLocal[0] != null) {
                panelGrafo.getChildren().remove(miFlechaLocal[0]);
                previsualizacionesTemporales.remove(miFlechaLocal[0]); // Quitar del rastreo
            }
        });

        btnBorrar.getChildren().addAll(circuloFondo, iconoX);
        filaRuta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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

    private void cargarFilaConDatos(String nombreDestino, double tiempo, double costo) {
        agregarNuevaFilaRuta();
        HBox ultimaFila = (HBox) vboxRutas.getChildren().get(vboxRutas.getChildren().size() - 1);

        // Buscamos los componentes dentro de esa fila (según el orden en que los añadiste)
        ComboBox<String> combo = (ComboBox<String>) ultimaFila.getChildren().get(0);
        TextField txtT = (TextField) ultimaFila.getChildren().get(1);
        TextField txtC = (TextField) ultimaFila.getChildren().get(2);

        combo.setValue(nombreDestino);
        txtT.setText(String.valueOf(tiempo));
        txtC.setText(String.valueOf(costo));
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

        circuloParada.setOnMouseClicked(e -> {
            e.consume(); // Evita que el mapa detecte un clic y quiera crear otra parada

            // Usamos las coordenadas reales del centro del círculo
            mostrarPopup(p, circuloParada.getCenterX(), circuloParada.getCenterY());
        });

        circuloParada.setOnMouseEntered(e -> {
            circuloParada.setRadius(12); // Crece un poquito
            circuloParada.setCursor(javafx.scene.Cursor.HAND);
        });
        circuloParada.setOnMouseExited(e -> {
            circuloParada.setRadius(10); // Vuelve a su tamaño
        });

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

//        double compensacionGrosor = 2.0;
//        double offsetLinea = offsetFlecha + compensacionGrosor;

        double finalX = xDest - Math.cos(angulo) * offsetFlecha;
        double finalY = yDest - Math.sin(angulo) * offsetFlecha;

        lineaPrevia = new Line(xOrigen, yOrigen, finalX, finalY);
        // ESTILO ESTÉTICO
        lineaPrevia.setStroke(javafx.scene.paint.Color.web("#4A4A4A"));
        lineaPrevia.setStrokeWidth(2.5);
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

        double offsetPunta = 12.0;
        double puntaX = x2 - Math.cos(angulo) * offsetPunta;
        double puntaY = y2 - Math.sin(angulo) * offsetPunta;

        // Calculamos las dos esquinas traseras del triángulo
        // Usamos el ángulo + y - 150 grados para que la flecha sea "puntiaguda"
        double anguloA = angulo + Math.toRadians(155);
        double anguloB = angulo - Math.toRadians(155);

        double xA = puntaX + Math.cos(anguloA) * radioFlecha;
        double yA = puntaY + Math.sin(anguloA) * radioFlecha;

        double xB = puntaX + Math.cos(anguloB) * radioFlecha;
        double yB = puntaY + Math.sin(anguloB) * radioFlecha;

        // Crear el triángulo (punta de flecha)
        javafx.scene.shape.Polygon flecha = new javafx.scene.shape.Polygon();
        flecha.getPoints().addAll(new Double[]{
                puntaX, puntaY, // Punta
                xA, yA,         // Esquina trasera 1
                xB, yB          // Esquina trasera 2
        });

        // Color y estilo (usa el mismo que la línea)
        flecha.setFill(javafx.scene.paint.Color.web("#4A4A4A"));
        return flecha;
    }


    private void animarEntradaPanel(javafx.scene.Node panel, boolean mostrar) {
        panel.setVisible(true);

        double posicionOculto = 350;
        double posicionVisible = 0;

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), panel);

        if (mostrar) {
            panel.setTranslateX(posicionOculto);
            tt.setToX(posicionVisible);
        } else {
            tt.setToX(posicionOculto);
            tt.setOnFinished(e -> panel.setVisible(false));
        }

        tt.play();
    }

    // ==========================================
    //                 DETALLES
    // ==========================================

    private void mostrarPopup(Parada p, double x, double y) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/DetallePopup.fxml"));
            VBox popup = loader.load();

            Label lbl = (Label) popup.lookup("#lblNombrePopup");
            Button btn = (Button) popup.lookup("#btnDetallesPopup");

            if (lbl != null) lbl.setText(p.getNombre());
            if (btn != null) {
                btn.setOnAction(e -> {
                    panelGrafo.getChildren().removeIf(n -> n.getStyleClass().contains("popup-info"));
                    cargarPanelDetalle(p);
                });
            }

            popup.getStyleClass().add("popup-info");

            popup.setLayoutX(x - 65);
            popup.setLayoutY(y - 95);

            panelGrafo.getChildren().removeIf(n -> n.getStyleClass().contains("popup-info"));
            panelGrafo.getChildren().add(popup);

        } catch (IOException e) {
            System.err.println("No se pudo cargar DetallePopup.fxml: " + e.getMessage());
        }
    }

    private void poblarRutasEnDetalle(Parada p, VBox contenedor) {
        contenedor.getChildren().clear();
        Parada paradaReal = mapaParadas.get(p.getId());

        List<Ruta> vecinos = grafo.obtenerVecinos(paradaReal != null ? paradaReal : p);

        if (vecinos.isEmpty()) {
            Label lblSencillo = new Label("No hay conexiones salientes.");
            lblSencillo.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            contenedor.getChildren().add(lblSencillo);
            return;
        }

        for (Ruta r : vecinos) {
            HBox fila = new HBox(10);
            fila.setStyle("-fx-padding: 5; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

            Label lblDestino = new Label("A: " + r.getDestino().getNombre());
            Label lblTiempo = new Label(r.getPond(Pond.TIEMPO) + " min");
            Label lblCosto = new Label("$" + r.getPond(Pond.COSTO));

            lblDestino.setPrefWidth(100);
            fila.getChildren().addAll(lblDestino, lblTiempo, lblCosto);
            contenedor.getChildren().add(fila);
        }
    }

    private void cargarPanelDetalle(Parada p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/DetalleParada.fxml"));
            AnchorPane view = loader.load();

            VBox contenedorRutas = (VBox) view.lookup("#vboxRutasDetalle");

            if (contenedorRutas == null) {
                // Buscamos el ScrollPane primero (que es más fácil de hallar)
                ScrollPane scroll = (ScrollPane) view.lookup("ScrollPane");
                if (scroll != null && scroll.getContent() instanceof VBox) {
                    contenedorRutas = (VBox) scroll.getContent();
                }
            }

            if (contenedorRutas != null) {
                poblarRutasEnDetalle(p, contenedorRutas);
            } else {
                System.err.println("ERROR CRÍTICO: No se halló el contenedor ni manualmente.");
            }

            TextField txtNombre = (TextField) view.lookup("#txtNombreDetalle");
            if (txtNombre != null) txtNombre.setText(p.getNombre());

            Button btnEliminar = (Button) view.lookup("#btnEliminar");
            if (btnEliminar != null) {
                btnEliminar.setOnAction(e -> {
                    System.out.println("Abriendo confirmación para eliminar: " + p.getNombre());
                    mostrarConfirmacionEliminar(p);
                });
            }

            Button btnCerrar = (Button) view.lookup("#btnCerrarDetalle");
            if (btnCerrar != null) btnCerrar.setOnAction(e -> animarEntradaPanel(view, false));

            Button btnEditar = (Button) view.lookup("#btnEditar");
            if (btnEditar != null) {
                btnEditar.setOnAction(e -> {
                    System.out.println("Cambiando a modo edición para: " + p.getNombre());
                    abrirEdicionParada(p);
                });
            }

            if (contenedorLateral != null) {
                contenedorLateral.setMouseTransparent(false); // Para que responda a clics
                contenedorLateral.getChildren().setAll(view); // Metemos el panel de detalle dentro del hueco

                // Alineamos el panel a la derecha del contenedor por si acaso
                AnchorPane.setRightAnchor(view, 0.0);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);

                animarEntradaPanel(view, true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ejecutarEliminacionReal(Parada p) {
        grafo.eliminarParada(p);
        mapaParadas.remove(p.getId());

        // Guardar cambios en el JSON
        List<RutaJSON> todasLasRutas = obtenerTodasLasRutasDelGrafo();
        DatosRedJSON datosActualizados = new DatosRedJSON(mapaParadas, todasLasRutas);
        gestorDatos.guardarDatos(datosActualizados, RUTA_ARCHIVO);

        ObservableList<Parada> paradasVivas = FXCollections.observableArrayList(mapaParadas.values());
        cbOrigen.setItems(paradasVivas);
        cbDestino.setItems(paradasVivas);

        // Refrescar la vista
        refrescarMapaVisual();
        if (!contenedorLateral.getChildren().isEmpty()) {
            animarEntradaPanel(contenedorLateral.getChildren().get(0), false);
        }
    }

    private void mostrarConfirmacionEliminar(Parada p) {
        StackPane overlay = new StackPane();
        Pane nodoRaiz = (Pane) panelGrafo.getScene().getRoot();

        overlay.setPrefSize(nodoRaiz.getWidth(), nodoRaiz.getHeight());
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        VBox dialog = new VBox(20);
        dialog.setMaxSize(300, 200);
        dialog.setAlignment(Pos.TOP_CENTER);
        dialog.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        dialog.setPadding(Insets.EMPTY);
        dialog.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.2)));

        // Cabecera Coral
        StackPane header = new StackPane(new Label(p.getNombre().toUpperCase()));
        header.setMinHeight(50);
        header.setPrefWidth(300);
        header.setStyle("-fx-background-color: #E68484; -fx-background-radius: 20 20 0 0;");
        //header.getChildren().get(0).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblNombre = (Label) header.getChildren().get(0);
        lblNombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label mensaje = new Label("¿Seguro que quieres eliminar\nesta parada?");
        mensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        VBox.setMargin(mensaje, new Insets(10, 0, 0, 0));

        HBox botones = new HBox(20, crearBotonConfirmar(true, overlay, p), crearBotonConfirmar(false, overlay, p));
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10));

        dialog.getChildren().addAll(header, mensaje, botones);
        overlay.getChildren().add(dialog);

        nodoRaiz.getChildren().add(overlay);
        overlay.toFront();
    }

    private Button crearBotonConfirmar(boolean esAceptar, StackPane overlay, Parada p) {
        Button btn = new Button(esAceptar ? "✔" : "✘");

        // Estilo según el tipo de botón
        String colorBase = esAceptar ? "#82E082" : "#E68484"; // Verde o Coral
        btn.setStyle("-fx-background-color: " + colorBase + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-min-width: 80; " +
                "-fx-cursor: hand;");

        btn.setOnAction(e -> {
            if (esAceptar) {
                ejecutarEliminacionReal(p); // Llamamos a la lógica de borrado
            } else {
                panelGrafo.getChildren().removeIf(n -> n instanceof Circle && ((Circle) n).getFill() == Color.BLACK);
                animarPanel(false);
            }

            if (overlay.getParent() instanceof Pane) {
                ((Pane) overlay.getParent()).getChildren().remove(overlay);
            }
        });

        return btn;
    }

    private void refrescarMapaVisual() {
        // Guardamos la imagen del mapa
        javafx.scene.Node mapaImagen = panelGrafo.getChildren().get(0);
        panelGrafo.getChildren().clear();
        panelGrafo.getChildren().add(mapaImagen);

        // Redibujar paradas
        for (Parada parada : mapaParadas.values()) {
            dibujarParadaEnMapa(parada);
        }

        // Redibujar todas las rutas
        for (Parada origen : mapaParadas.values()) {
            for (Ruta ruta : grafo.obtenerVecinos(origen)) {
                dibujarLineaRuta(origen, ruta.getDestino());
            }
        }
    }

    private void abrirEdicionParada(Parada p) {

        this.paradaEnEdicion = p;

        txtNombreParada.setText(p.getNombre());
        vboxRutas.getChildren().clear();

        this.tempX = p.getCoordX();
        this.tempY = p.getCoordY();
        List<Ruta> conexiones = grafo.obtenerVecinos(p);

        for (Ruta r : conexiones) {
            cargarFilaConDatos(r.getDestino().getNombre(),
                    r.getPond(Pond.TIEMPO),
                    r.getPond(Pond.COSTO));
        }

        panelEdicion.setVisible(true);
        contenedorLateral.getChildren().setAll(panelEdicion);
        animarEntradaPanel(panelEdicion, true);
    }

    // ==========================================
    //        GESTIÓN Y CALCULO DE MEJOR RUTA
    // ==========================================

    private void mostrarTarjetaResultado(List<Ruta> camino, Parada pOrigen) {
        if (camino == null || camino.isEmpty()){
            System.out.println("DEBUG: El camino está vacío.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/TarjetaRuta.fxml"));
            AnchorPane tarjeta = loader.load();

            // Usamos el origen que pasamos por parámetro y el destino de la última ruta
            Parada destino = camino.get(camino.size() - 1).getDestino();

            double costoTotal = 0;
            double kmTotales = 0;
            double tiempoTotal = 0;

            for (Ruta r : camino) {
                costoTotal += r.getPond(Pond.COSTO);
                kmTotales += r.getPond(Pond.DISTANCIA);
                tiempoTotal += r.getPond(Pond.TIEMPO);
            }

            // Llenado de labels (asegúrate que los IDs coincidan con el FXML anterior)
            Parada origen;
            ((Label) tarjeta.lookup("#lblOrigenNombre")).setText(pOrigen.getNombre());
            ((Label) tarjeta.lookup("#lblDestinoNombre")).setText(destino.getNombre());
            ((Label) tarjeta.lookup("#lblCostoTotal")).setText(String.format("%.0f $ DOP", costoTotal));
            ((Label) tarjeta.lookup("#lblDistanciaTotal")).setText(String.format("%.1f Km", kmTotales));
            ((Label) tarjeta.lookup("#lblTiempoTotal")).setText(String.format("%.0f min", tiempoTotal));
            ((Label) tarjeta.lookup("#lblTransbordosCount")).setText((camino.size() - 1) + " Transbordos");

            // Posicionamiento de la tarjeta
            tarjeta.setLayoutX(50);
            tarjeta.setLayoutY(50);

            // Limpiar tarjeta anterior si existe antes de poner la nueva
            panelGrafo.getChildren().removeIf(n -> n.lookup("#lblOrigenNombre") != null);
            panelGrafo.getChildren().add(tarjeta);
            tarjeta.toFront();

        } catch (IOException e) {
            System.err.println("Error al cargar la tarjeta: " + e.getMessage());
        }
    }

    @FXML
    void onBtnBuscarClicked(ActionEvent event) {
        Parada pOrigen = cbOrigen.getValue(); // El ComboBox de origen de tu imagen
        Parada pDestino = cbDestino.getValue();

        if (pOrigen == null || pDestino == null) return;

        // Tu lógica de búsqueda ya existente
        Pond criterio = obtenerCriterioSeleccionado();
        List<Ruta> resultado = dijkstra.buscarRutaOptima(grafo, pOrigen, pDestino, criterio);

        if (!resultado.isEmpty()) {
            mostrarTarjetaResultado(resultado, pOrigen);
            resaltarRutaEnMapa(resultado);
        }
    }


    private Parada buscarParadaOrigen(Ruta primeraRuta, Parada pOrigenSeleccionado) {
        // Opción A: Si el usuario ya seleccionó el origen en el ComboBox,
        // ese ES el origen, no hay que buscarlo.
        if (pOrigenSeleccionado != null) return pOrigenSeleccionado;

        // Opción B (Seguridad): Buscar en el grafo quién tiene esa ruta
        // (Solo si pOrigenSeleccionado fuera null por alguna razón)
        return null;
    }

    private Pond obtenerCriterioSeleccionado() {
        if (rbTiempo.isSelected()) return Pond.TIEMPO;
        if (rbDistancia.isSelected()) return Pond.DISTANCIA;
        if (rbCosto.isSelected()) return Pond.COSTO;
        if (rbTransbordos.isSelected()) return Pond.TRANSBORDOS;
        return Pond.DISTANCIA; // Por defecto
    }

    private void resaltarRutaEnMapa(List<Ruta> camino) {
        // 1. Limpiar resaltados previos (volver todas las líneas a gris/negro)
        panelGrafo.getChildren().forEach(nodo -> {
            if (nodo instanceof Line) {
                ((Line) nodo).setStroke(Color.BLACK);
                ((Line) nodo).setStrokeWidth(1.0);
            }
        });

        // 2. Resaltar las rutas del camino óptimo
        for (Ruta r : camino) {
            // Aquí deberías tener una forma de identificar la Line física
            // asociada a la ruta 'r' para ponerla en color azul o verde.
            // Ejemplo:
            // Line visual = buscarLineaDeRuta(r);
            // visual.setStroke(Color.CYAN);
            // visual.setStrokeWidth(3.0);
        }
    }

    @FXML
    private void intercambiarSeleccion() {
        Parada temp = cbOrigen.getValue();
        cbOrigen.setValue(cbDestino.getValue());
        cbDestino.setValue(temp);
    }
}