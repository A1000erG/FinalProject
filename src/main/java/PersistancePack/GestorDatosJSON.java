package PersistancePack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Clase encargada de la persistencia del sistema de transporte.
 * Maneja la lectura y escritura del grafo aplanado G = (V, E) utilizando Jackson.
 */
public class GestorDatosJSON {

    private final ObjectMapper mapper;

    public GestorDatosJSON() {
        this.mapper = new ObjectMapper();
        // Habilitamos la indentación para que el archivo .json sea legible por humanos
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Guarda la estructura de la red en un archivo JSON.
     * Complejidad: O(|V| + |E|), ya que el serializador debe recorrer
     * todos los vértices y aristas una vez para convertirlos a texto.
     *
     * Parametros:
     * - datos Objeto DTO que contiene las paradas y las rutas planas.
     * - rutaArchivo Ruta donde se guardará el archivo ("src/main/resources/datos/red.json").
     */
    public void guardarDatos(DatosRedJSON datos, String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            File directorioPadre = archivo.getParentFile();
            if (directorioPadre != null && !directorioPadre.exists()) {
                directorioPadre.mkdirs();
            }

            // Escribir el objeto DatosRedJSON en el archivo
            mapper.writeValue(archivo, datos);
            System.out.println("Datos de la red guardados exitosamente en: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error crítico al guardar los datos de la red: " + e.getMessage());
        }
    }

    /**
     * Carga la estructura de la red desde un archivo JSON.
     * Complejidad: O(|V| + |E|), el parser lee el archivo secuencialmente e
     * instancia los objetos correspondientes.
     *
     * Parámetros: rutaArchivo Ruta desde donde se leerá el archivo.
     * Retorno: El objeto DatosRedJSON poblado, o null si el archivo no existe o hay un error.
     */
    public DatosRedJSON cargarDatos(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);

            // Si es la primera vez que se ejecuta y no hay archivo, retorna null
            if (!archivo.exists()) {
                System.out.println("No se encontró un archivo de datos previo. Se iniciará una red vacía.");
                return null;
            }

            // Jackson mapea el JSON de vuelta al objeto DatosRedJSON
            return mapper.readValue(archivo, DatosRedJSON.class);

        } catch (IOException e) {
            System.err.println("Error al cargar los datos de la red: " + e.getMessage());
            return null;
        }
    }
}