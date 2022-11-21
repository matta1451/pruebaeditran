package com.editran.variable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public interface IArchivo {
    FileReader getLeerArchivo(File archivo_origen);
    void llenarArchivoRespuestasDesdeLista(File archivo_origen, List<String> lista_elementos) throws IOException;
    boolean esFechaHoyIgualQueFechaArchivo(File archivo_origen) throws IOException;
    void llenarListaDesdeArchivo(File archivo_origen, List<String> lista_elementos) throws IOException;
    void crearArchivo(File archivo_destino) throws IOException;
}
