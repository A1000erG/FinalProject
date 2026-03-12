package main.java.VisualPack;

import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import PersistancePack.GestorDatosJSON;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.*;

public class MainController {

    @FXML private ComboBox<Parada> cmbOrigen;
    @FXML private ComboBox<Parada> cmbDestino;
    @FXML private ComboBox<Pond> cmbCriterio;

    @FXML private Button btnCrear;
    @FXML private Button btnEliminar;
    @FXML private Button btnGuardar;
    @FXML private Button btnSalir;

    @FXML private Pane panelGrafo;
    @FXML private AnchorPane panelFlotante;

    private GrafoVisualizer visualizador;
    private GrafoTransporte grafo;
    private GestorDatosJSON gestorDatos;
    private final String RUTA_ARCHIVO = "src/main/resources/datos/red_transporte.json";

    // Variables para el manejo de clics
    private Parada paradaSeleccionada1 = null;
    private Parada paradaSeleccionada2 = null;

    private Map<String, Parada> mapaParadas = new HashMap<>();
    /**
     * Método que se ejecuta automáticamente al cargar el FXML.
     */
    @FXML
    public void initialize() {
        this.grafo = new GrafoTransporte();
        this.gestorDatos = new GestorDatosJSON();
        this.visualizador = new GrafoVisualizer(panelGrafo);
        cargarDatos();

        List<Parada> listaParadas = new ArrayList<>(mapaParadas.values());
        visualizador.dibujarGrafo(grafo,listaParadas);
        cmbCriterio.getItems().addAll(Pond.values());

        configurarEventoEnterRuta();
    }

    @FXML
    private void onGuardarClick() {
        System.out.println("Guardando datos de la red...");
        java.util.List<PersistancePack.RutaJSON> listaAristas = new java.util.ArrayList<>();

        // Recorrer paradas para extraer las rutas
        for (Parada origen : mapaParadas.values()) {
            java.util.List<LogicPack.Ruta> rutas = grafo.obtenerVecinos(origen);

            for (LogicPack.Ruta ruta : rutas) {
                PersistancePack.RutaJSON arista = new PersistancePack.RutaJSON();
                arista.setIdOrigen(origen.getId());
                arista.setIdDestino(ruta.getDestino().getId());

                // Extraer las ponderaciones usando el enum Pond
                java.util.Map<Pond, Double> pesosDTO = new java.util.EnumMap<>(Pond.class);
                for (Pond p : Pond.values()) {
                    Double valor = ruta.getPond(p);
                    if (valor != null) {
                        pesosDTO.put(p, valor);
                    }
                }
                arista.setPesos(pesosDTO);
                listaAristas.add(arista);
            }
        }

        PersistancePack.DatosRedJSON datosFinales = new PersistancePack.DatosRedJSON(mapaParadas, listaAristas);
        gestorDatos.guardarDatos(datosFinales, RUTA_ARCHIVO);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "El grafo se ha guardado exitosamente en el archivo JSON.");
        alert.show();
    }

    @FXML
    private void onSalirClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de Salida");
        alert.setHeaderText("¿Desea guardar los cambios antes de salir?");

        ButtonType btnSi = new ButtonType("Sí, Guardar");
        ButtonType btnNo = new ButtonType("No, Salir");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSi, btnNo, btnCancelar);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnSi) {
                onGuardarClick();
                Platform.exit();
            } else if (result.get() == btnNo) {
                Platform.exit();
            }
        }
    }

    @FXML
    private void onCrearParadaClick() {
        mostrarPanelFlotante("Agregar Parada");
    }

    @FXML
    private void onMapaClicked(MouseEvent event) {
        // Validar doble clic en espacio vacío
        if (event.getClickCount() == 2 && event.getTarget() == panelGrafo) {
            double x = event.getX();
            double y = event.getY();

            System.out.println("Doble clic detectado en coordenadas: X=" + x + ", Y=" + y);
            mostrarPanelFlotante("Agregar Parada");
        }
    }

    private void configurarEventoEnterRuta() {
        // Aquí se agregó un listener global para la tecla Enter
        panelGrafo.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        ejecutarAlgoritmoRuta();
                    }
                });
            }
        });
    }

    private void ejecutarAlgoritmoRuta() {
        Parada origen = cmbOrigen.getValue();
        Parada destino = cmbDestino.getValue();
        Pond criterio = cmbCriterio.getValue();

        if (origen == null || destino == null || criterio == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Debe seleccionar Origen, Destino y Criterio válidos.");
            warning.show();
            return;
        }

        // Decisión de Algoritmo: Bellman-Ford para Costos
        LogicPack.Algorithms.Algozed<Parada, LogicPack.Ruta> algoritmo;

        if (criterio == Pond.COSTO) {
            algoritmo = new LogicPack.Algorithms.BellmanFordAlgo();
            System.out.println("Ejecutando Bellman-Ford");
        } else {
            algoritmo = new LogicPack.Algorithms.DijkstraAlgo();
            System.out.println("Ejecutando Dijkstra O((V+E)logV)");
        }

        try {
            // Ejecución de la búsqueda
            java.util.List<LogicPack.Ruta> caminoOptimo = algoritmo.buscarRutaOptima(grafo, origen, destino, criterio);

            if (caminoOptimo.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION, "No existe una ruta posible entre las paradas seleccionadas.");
                info.show();
            } else {
                // Calcular el valor total de la ruta para mostrarlo
                double totalCriterio = 0.0;
                StringBuilder mensajeRuta = new StringBuilder("Ruta encontrada:\n" + origen.getNombre());

                for (LogicPack.Ruta r : caminoOptimo) {
                    totalCriterio += r.getPond(criterio);
                    mensajeRuta.append(" -> ").append(r.getDestino().getNombre());
                }

                mensajeRuta.append("\n\nTotal (").append(criterio.name()).append("): ").append(totalCriterio);

                // Más adelante esto se dibujará en el lienzo (cambiando el color de las aristas),
                // por ahora lo mostramos en una alerta.
                Alert resultado = new Alert(Alert.AlertType.INFORMATION, mensajeRuta.toString());
                resultado.setHeaderText("¡Ruta Óptima Calculada!");
                resultado.show();
            }
        } catch (IllegalStateException e) {
            // Captura el ciclo negativo de Bellman-Ford
            Alert error = new Alert(Alert.AlertType.ERROR, "Error crítico: " + e.getMessage());
            error.show();
        }
    }

    private void mostrarPanelFlotante(String titulo) {
        panelFlotante.setVisible(true);
        //Limpiar el panelFlotante e inyectarle los TextField correspondientes dinámicamente
    }

    private void cargarDatos() {
        System.out.println("Cargando red desde JSON...");
        PersistancePack.DatosRedJSON datos = gestorDatos.cargarDatos(RUTA_ARCHIVO);

        if (datos == null || datos.getParadas() == null || datos.getParadas().isEmpty()) {
            System.out.println("Iniciando con una red vacía.");
            return;
        }

        //Aquí se reconstruyen los nodos
        for (Parada parada : datos.getParadas().values()) {
            mapaParadas.put(parada.getId(), parada);
            grafo.agregarParada(parada);
        }

        //Esto es para reconstruir las rutas
        if (datos.getRutas() != null) {
            for (PersistancePack.RutaJSON arista : datos.getRutas()) {
                Parada origen = mapaParadas.get(arista.getIdOrigen());
                Parada destino = mapaParadas.get(arista.getIdDestino());

                if (origen != null && destino != null) {
                    LogicPack.Ruta nuevaRuta = new LogicPack.Ruta(destino);

                    // Restaurar los pesos
                    if (arista.getPesos() != null) {
                        for (Map.Entry<Pond, Double> peso : arista.getPesos().entrySet()) {
                            nuevaRuta.setPond(peso.getKey(), peso.getValue());
                        }
                    }
                    grafo.agregarRuta(origen, nuevaRuta);
                }
            }
        }
        System.out.println("Red cargada con éxito: " + mapaParadas.size() + " paradas.");
    }
}