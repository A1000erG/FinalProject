package TestPack;
import LogicPack.Algorithms.Algozed;
import LogicPack.Algorithms.BellmanFordAlgo;
import LogicPack.Algorithms.DijkstraAlgo;
import LogicPack.GrafoTransporte;
import LogicPack.Parada;
import LogicPack.Pond;
import LogicPack.Ruta;
import PersistancePack.DatosRedJSON;
import PersistancePack.GestorDatosJSON;
import PersistancePack.RutaJSON;

import java.util.*;

public class Main {
    private static final GestorDatosJSON gestorPersistencia = new GestorDatosJSON();
    public static final String archivo_json="src/main/resources/datos/red_transporte.json";

    private static final Scanner scanner = new Scanner(System.in);
    private static final GrafoTransporte grafo = new GrafoTransporte();
    private static final Map<String, Parada> mapaParadas = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GESTIÓN DE RUTAS (PUCMM) ===");
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1 -> agregarParada();
                case 2 -> eliminarParada(); // Nota: Requiere implementar en Grafo
                case 3 -> gestionarRuta();
                case 4 -> calcularRutaOptima();
                case 5 -> listarDatos();
                case 0 -> {
                    // Guardar datos antes de salir
                    extraerYGuardarGrafo();
                    salir = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    /**
     * Paso 6: Carga el JSON y reconstruye el grafo en memoria.
     * Complejidad: O(|V| + |E|)
     */
    private static void cargarYReconstruirGrafo() {
        DatosRedJSON datos = gestorPersistencia.cargarDatos(archivo_json);

        if (datos == null || datos.getParadas() == null || datos.getParadas().isEmpty()) {
            System.out.println("Iniciando con una red vacía.");
            return;
        }

        // 1. Reconstruir los Nodos O(|V|)
        for (Parada parada : datos.getParadas().values()) {
            mapaParadas.put(parada.getId(), parada);
            grafo.agregarParada(parada);
        }

        // 2. Reconstruir las Aristas O(|E|)
        if (datos.getRutas() != null) {
            for (RutaJSON arista : datos.getRutas()) {
                Parada origen = mapaParadas.get(arista.getIdOrigen());
                Parada destino = mapaParadas.get(arista.getIdDestino());

                if (origen != null && destino != null) {
                    Ruta nuevaRuta = new Ruta(destino);

                    // Restaurar los pesos del JSON a la Ruta
                    if (arista.getPesos() != null) {
                        for (Map.Entry<Pond, Double> peso : arista.getPesos().entrySet()) {
                            nuevaRuta.setPond(peso.getKey(), peso.getValue());
                        }
                    }
                    grafo.agregarRuta(origen, nuevaRuta);
                }
            }
        }
        System.out.println("Red cargada con éxito: " + mapaParadas.size() + " paradas y " +
                (datos.getRutas() != null ? datos.getRutas().size() : 0) + " rutas.");
    }

    /**
     * Paso 5: Extrae las rutas del GrafoTransporte, empaqueta todo y lo guarda.
     * Complejidad: O(|V| + |E|)
     */
    private static void extraerYGuardarGrafo() {
        System.out.println("\nGuardando datos de la red...");
        List<RutaJSON> listaAristas = new ArrayList<>();

        // Recorremos cada parada en el mapa para buscar sus rutas
        for (Parada origen : mapaParadas.values()) {
            List<Ruta> rutas = grafo.obtenerVecinos(origen);

            // Por cada ruta real, creamos un DTO (AristaJSON)
            for (Ruta ruta : rutas) {
                RutaJSON arista = new RutaJSON();
                arista.setIdOrigen(origen.getId());
                arista.setIdDestino(ruta.getDestino().getId());

                // Extraer las ponderaciones usando el enum
                Map<Pond, Double> pesosDTO = new EnumMap<>(Pond.class);
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

        // Empaquetar y guardar
        DatosRedJSON datosFinales = new DatosRedJSON(mapaParadas, listaAristas);
        gestorPersistencia.guardarDatos(datosFinales, archivo_json);
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ DE GESTIÓN ---");
        System.out.println("1. Agregar Parada");
        System.out.println("2. Eliminar Parada");
        System.out.println("3. Agregar/Modificar Ruta");
        System.out.println("4. Calcular Ruta Óptima (Dijkstra/Bellman-Ford)");
        System.out.println("5. Listar Red Actual");
        System.out.println("0. Salir");
    }

    private static void agregarParada() {
        System.out.print("ID de la Parada: ");
        String id = scanner.next();
        System.out.print("Nombre: ");
        String nombre = scanner.next();
        double x = leerDouble("Coordenada X: ");
        double y = leerDouble("Coordenada Y: ");

        Parada nueva = new Parada(id, nombre, x, y);
        grafo.agregarParada(nueva);
        mapaParadas.put(id, nueva);
        System.out.println("Parada agregada exitosamente.");
    }

    private static void gestionarRuta() {
        System.out.print("ID Origen: ");
        Parada p1 = mapaParadas.get(scanner.next());
        System.out.print("ID Destino: ");
        Parada p2 = mapaParadas.get(scanner.next());

        if (p1 == null || p2 == null) {
            System.out.println("Error: Una o ambas paradas no existen.");
            return;
        }

        Ruta nuevaRuta = new Ruta(p2);
        // Validación de ponderaciones según el PDF
        nuevaRuta.setPond(Pond.TIEMPO, leerValidado("Tiempo (min): ", false));
        nuevaRuta.setPond(Pond.DISTANCIA, leerValidado("Distancia (km): ", false));
        nuevaRuta.setPond(Pond.TRANSBORDOS, leerValidado("Transbordos: ", false));
        nuevaRuta.setPond(Pond.COSTO, leerValidado("Costo (puede ser negativo para descuentos): ", true));

        if (grafo.agregarRuta(p1, nuevaRuta)) {
            System.out.println("Ruta establecida de " + p1.getNombre() + " a " + p2.getNombre());
        }
    }

    private static void calcularRutaOptima() {
        System.out.print("ID Origen: ");
        Parada p1 = mapaParadas.get(scanner.next());
        System.out.print("ID Destino: ");
        Parada p2 = mapaParadas.get(scanner.next());

        System.out.println("Optimizar por: 1. Tiempo | 2. Distancia | 3. Costo | 4. Transbordos");
        int c = leerEntero("Criterio: ");
        Pond pond = Pond.values()[c - 1];

        // Decisión de Algoritmo: Si hay costos negativos, usar Bellman-Ford [cite: 41, 141]
        Algozed<Parada, Ruta> algo = (pond == Pond.COSTO) ? new BellmanFordAlgo() : new DijkstraAlgo();

        System.out.println("Ejecutando " + algo.getClass().getSimpleName() + "...");
        List<Ruta> resultado = algo.buscarRutaOptima(grafo, p1, p2, pond);

        if (resultado.isEmpty()) {
            System.out.println("No se encontró una ruta disponible.");
        } else {
            System.out.println("Ruta encontrada:");
            resultado.forEach(r -> System.out.println(" -> " + r.getDestino().getNombre() + " (" + r.getPond(pond) + ")"));
        }
    }

    // --- MÉTODOS AUXILIARES DE VALIDACIÓN ---

    private static double leerValidado(String mensaje, boolean permiteNegativo) {
        while (true) {
            double valor = leerDouble(mensaje);
            if (!permiteNegativo && valor < 0) {
                System.out.println("Error: Este valor no puede ser negativo.");
            } else {
                return valor;
            }
        }
    }

    private static int leerEntero(String m) {
        System.out.print(m);
        while (!scanner.hasNextInt()) { scanner.next(); }
        return scanner.nextInt();
    }

    private static double leerDouble(String m) {
        System.out.print(m);
        while (!scanner.hasNextDouble()) { scanner.next(); }
        return scanner.nextDouble();
    }

    private static void listarDatos() {
        mapaParadas.values().forEach(p -> {
            System.out.print("Parada " + p.getNombre() + " conecta con: ");
            grafo.obtenerVecinos(p).forEach(r -> System.out.print(r.getDestino().getNombre() + " "));
            System.out.println();
        });
    }

    private static void eliminarParada() {
        System.out.println("Funcionalidad pendiente de implementación en GrafoTransporte.");
    }
}