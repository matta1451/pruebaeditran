package com.editran.variable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Archivo implements IArchivo {

    public static String carpeta = "";

    @Override
    public FileReader getLeerArchivo(File file) {
        FileReader file_reader = null;
        try {
            file_reader = new FileReader(file);
        } catch (FileNotFoundException f) {
        }
        return file_reader;
    }

    public static void obtenerFechaCarpetaHoy(String ruta_cargar, String formato_fecha) {
        Date hoy = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(formato_fecha);
        String fecha_hoy = sdf.format(hoy);
        String fecha_anio = fecha_hoy.substring(0, 8);
        File file = new File(ruta_cargar);
        for (File archivos : file.listFiles()) {
            if (archivos.getName().startsWith(fecha_anio)) {
                Archivo.carpeta = archivos.getName();
                break;
            }
        }
    }

    public static List<String> recopilarHorarios(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String linea;
        List<String> horarios = new ArrayList<>();
        int contador = 1;
        while ((linea = br.readLine()) != null) {

            switch (contador) {
                case 1: {
                    horarios.add(linea);
                }
                break;
                case 2: {
                    horarios.add(linea);
                }
                break;
                case 3: {
                    horarios.add(linea);
                }
                break;
            }
            contador++;
        }
        return horarios;
    }

    @Override
    public void llenarArchivoRespuestasDesdeLista(File archivo_origen, List<String> lista_elementos) throws IOException {
        FileReader file_reader = new FileReader(archivo_origen);
        BufferedReader br = new BufferedReader(file_reader);
        List<String> elementos_txt = br.lines().collect(Collectors.toList());
        br.close();
        lista_elementos.forEach(e -> {
            if (!elementos_txt.contains(e) & e.length() < 13) {
                elementos_txt.add(e);
            }
        });
        FileWriter fw = new FileWriter(archivo_origen);
        BufferedWriter bw = new BufferedWriter(fw);
        elementos_txt.forEach(e -> {
            try {
                bw.append(e);
                bw.newLine();
            } catch (IOException ex) {
            }
        });
        bw.close();
    }

    @Override
    public boolean esFechaHoyIgualQueFechaArchivo(File archivo) throws IOException {
        BasicFileAttributes bfa = Files.readAttributes(archivo.toPath(), BasicFileAttributes.class);
        FileTime ft = bfa.lastModifiedTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate fecha_hoy = LocalDate.now();
        LocalDate fecha_archivo = LocalDate.parse(sdf.format(new Date(ft.toMillis())));
        return fecha_hoy.isEqual(fecha_archivo);
    }

    @Override
    public void llenarListaDesdeArchivo(File archivo_origen, List<String> lista_elementos) throws IOException {
        FileReader file_reader = new FileReader(archivo_origen);
        BufferedReader validar = new BufferedReader(file_reader);
        if (validar.lines().count() > 0) {
            FileReader fr = new FileReader(archivo_origen);
            BufferedReader lectura = new BufferedReader(fr);
            String linea;
            while ((linea = lectura.readLine()) != null) {
                if (!lista_elementos.contains(linea)) {
                    lista_elementos.add(linea);
                }
            }
            lectura.close();
        }
        validar.close();
    }

    @Override
    public void crearArchivo(File archivo_destino) throws IOException {
        FileWriter fileWriter = new FileWriter(archivo_destino);
        fileWriter.flush();
    }
}
