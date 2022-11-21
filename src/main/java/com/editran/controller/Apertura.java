package com.editran.controller;

import com.editran.service.ProcesarArchivo;
import com.editran.variable.Archivo;
import com.editran.variable.Util;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Apertura {

    private final Logger log = LogManager.getLogger(Apertura.class);
    private static boolean ejecutar = false;
    private int hora_tri_inicio;
    private int minuto_tri_inicio;
    private int hora_tri_fin;
    private int minuto_tri_fin;
    private int hora_trm_inicio;
    private int minuto_trm_inicio;
    private int hora_trm_fin;
    private int minuto_trm_fin;
    private int hora_trt_inicio;
    private int minuto_trt_inicio;
    private int hora_trt_fin;
    private int minuto_trt_fin;
    @Autowired
    Cuerpo cuerpo;

    @Autowired
    ProcesarArchivo procesarArchivo;

    public void Iniciar() {
        log.info("Iniciando método INICIAR");
        try {
            establecerHoraMinutos();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    log.info("Iniciando método RUN");
                    int minuto_ahora;
                    int hora_ahora;
                    Calendar calendar;
                    LocalTime hora_actual;
                    LocalTime hora_inicial_tri;
                    LocalTime hora_final_tri;
                    LocalTime hora_inicial_trm;
                    LocalTime hora_final_trm;
                    LocalTime hora_inicial_trt;
                    LocalTime hora_final_trt;
                    log.info("Finalizando método RUN");
                    // Esto se ejecuta en segundo plano una única vez
//                while (ejecutar) {
                    // Ciclo infinito
//                    try {
//                                     En él, hacemos que el hilo duerma
                    while (true) {
                        // Ciclo infinito
                        try {
                            log.info("Iniciando captacion de horas");
                            calendar = Calendar.getInstance();
                            hora_ahora = calendar.get(Calendar.HOUR_OF_DAY);
                            minuto_ahora = calendar.get(Calendar.MINUTE);
                            hora_actual = LocalTime.of(hora_ahora, minuto_ahora);
                            // ===================================================================
                            hora_inicial_tri = LocalTime.of(hora_tri_inicio, minuto_tri_inicio);
                            hora_final_tri = LocalTime.of(hora_tri_fin, minuto_tri_fin);
                            hora_inicial_trm = LocalTime.of(hora_trm_inicio, minuto_trm_inicio);
                            hora_final_trm = LocalTime.of(hora_trm_fin, minuto_trm_fin);
                            hora_inicial_trt = LocalTime.of(hora_trt_inicio, minuto_trt_inicio);
                            hora_final_trt = LocalTime.of(hora_trt_fin, minuto_trt_fin);
                            log.info("Finalizando captacion de horas");
                            log.info(ejecutar);
                            ejecutar = true;
                            while (((hora_actual.isBefore(hora_final_tri) & hora_actual.isAfter(hora_inicial_tri))
                                    | (hora_actual.isBefore(hora_final_trm) & hora_actual.isAfter(hora_inicial_trm)
                                    | (hora_actual.isBefore(hora_final_trt) & hora_actual.isAfter(hora_inicial_trt)))) & ejecutar) {
                                cuerpo.moverArchivos();
                                cuerpo.emitirArchivos();
                                cuerpo.guardarArchivos();
                                procesarArchivo.cargaEntrantes();
                                Thread.sleep(5000);
                                log.info("Valor de ejecutar: " + ejecutar);
                                calendar = Calendar.getInstance();
                                hora_ahora = calendar.get(Calendar.HOUR_OF_DAY);
                                minuto_ahora = calendar.get(Calendar.MINUTE);
                                hora_actual = LocalTime.of(hora_ahora, minuto_ahora);
                            }
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            log.info("OCURRIO UN ERROR");
                        }
                    }
//                        Thread.sleep(5000);
////                        Thread.sleep(1000000);
//                        // Y después realizamos las operaciones
//                        cuerpo.moverArchivos();
//                        cuerpo.emitirArchivos();
//                        cuerpo.guardarArchivos();
//                        procesarArchivo.cargaEntrantes();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
//            }

            };
            Thread hilo = new Thread(runnable);
            hilo.start();

        } catch (Exception e) {
            log.info("Ocurrió un error fuera: " + e.getMessage());
        }

    }

    public void Detener() {
        ejecutar = false;

    }

    // Crear un método para la lectura del archivo de horarios y recopilarlos en un map
    public void establecerHoraMinutos() {
        log.info("Iniciando método ESTABLECERHORAMINUTOS");
        try {
            List<String> horarios = Archivo.recopilarHorarios(new File(Util.getPro().getProperty("ruta_horarios")));
            log.info("Horarios obtenidos: " + horarios);
            int contador = 0;
            for (String s : horarios) {
                log.info("Valor del horario actual es: " + s);
                String[] elementos = s.split(",");
                String[] elementos_inicio = elementos[0].split(":");
                String[] elementos_fin = elementos[1].split(":");
                switch (contador) {
                    case 0: {
                        hora_tri_inicio = Integer.parseInt(elementos_inicio[0]);
                        minuto_tri_inicio = Integer.parseInt(elementos_inicio[1]);
                        hora_tri_fin = Integer.parseInt(elementos_fin[0]);
                        minuto_tri_fin = Integer.parseInt(elementos_fin[1]);
                        log.info(hora_tri_inicio);
                    }
                    break;
                    case 1: {
                        hora_trm_inicio = Integer.parseInt(elementos_inicio[0]);
                        minuto_trm_inicio = Integer.parseInt(elementos_inicio[1]);
                        hora_trm_fin = Integer.parseInt(elementos_fin[0]);
                        minuto_trm_fin = Integer.parseInt(elementos_fin[1]);
                        log.info(hora_trm_inicio);
                    }
                    break;
                    case 2: {
                        hora_trt_inicio = Integer.parseInt(elementos_inicio[0]);
                        minuto_trt_inicio = Integer.parseInt(elementos_inicio[1]);
                        hora_trt_fin = Integer.parseInt(elementos_fin[0]);
                        minuto_trt_fin = Integer.parseInt(elementos_fin[1]);
                        log.info(hora_trt_inicio);
                    }
                    break;
                }
                contador++;
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No hay horas establecidas en el archivo HORARIOS.txt");
        }
        ejecutar = true;
    }
}
