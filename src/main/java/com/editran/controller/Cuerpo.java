package com.editran.controller;

import com.editran.variable.Util;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Cuerpo {

    String fecha_hoy = new SimpleDateFormat("yyyyMMdd").format(new Date());

    @Autowired
    private Util util;

    public void moverArchivos() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        fecha_hoy = sdf.format(date);
        try {
            String line = "cmd /C COPY /Y " + Util.getPro().getProperty("cargar") + "\\" + fecha_hoy + " " + Util.getPro().getProperty("salida");
            Process p = Runtime.getRuntime().exec(line);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    public void emitirArchivos() {
        try {
            ArrayList<String> cargar = util.getPresentacion();
            for (String presentacion : cargar) {
                Runtime.getRuntime().exec("cmd /c " + Util.getPro().getProperty("send") + " " + presentacion, null,
                        new File(Util.getPro().getProperty("origen")));
            }
        } catch (IOException e) {
        }
    }

    public void guardarArchivos() {
        try {
            File ruta = new File(Util.getPro().getProperty("cargar") + "\\" + fecha_hoy + "\\");
            if (!ruta.exists()) {
                ruta.mkdir();
            }

            if (ruta.exists()) {
                File entrada = new File(Util.getPro().getProperty("cargar2"));
                List<String> elementos_directorio = Arrays.asList(ruta.listFiles()).stream().map(e -> e.getName()).collect(Collectors.toList());
                for(File archivo : entrada.listFiles()){
                    if(!archivo.getName().equals("temp") & !elementos_directorio.contains(archivo.getName())){
                        FileUtil.copyFile(archivo, ruta);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
