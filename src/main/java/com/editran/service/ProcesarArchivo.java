package com.editran.service;

import com.editran.controller.Cuerpo;
import com.editran.variable.Archivo;
import com.editran.variable.AtributosLogBD;
import com.editran.variable.IArchivo;
import com.editran.variable.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

@Service
public class ProcesarArchivo {

    public static final String RUTA = Util.getPro().getProperty("cargar");
    private final Logger log = LogManager.getLogger(ProcesarArchivo.class);
    private final String fecha_hoy = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private final String fecha_hoy_normal = this.fecha_hoy.substring(6) + this.fecha_hoy.substring(4, 6) + this.fecha_hoy.substring(0, 4);
    private final Map<Character, String> mapa = new HashMap<>();
    private String archivo_lect = "";
    private int intentos_enviar_correo = 0;
    private String fecha_reves = "";
    private String fecha_normal = "";
    private String turno = "";
    private String contenido_fichero = "";
    @Autowired
    private IServiceArchivo iservice_archivo;
    @Autowired
    private Cuerpo cuerpo;
    @Autowired
    private CorreoService correoService;
    private final List<String> nombres_entrada = new ArrayList<>();
    private final IArchivo archivo = new Archivo();
    private boolean ejecutar_log = true;

    public ProcesarArchivo() {
        mapa.put('1', "P");
        mapa.put('2', "R");
        mapa.put('7', "C");
    }

    @Scheduled(cron = "0/20 * * * * *")
    public void cargaEntrantes() {
        AtributosLogBD.inicio = System.nanoTime();
        cuerpo.guardarArchivos();
        String ruta_final = RUTA + "\\" + fecha_hoy + "\\";
        File documento = new File(ruta_final);
        log.info("La ruta para procesar el archivo es:" + ruta_final);
        log.info("El nombre de la carpeta es:" + documento.getName());
        AtributosLogBD.request = "Datos -> Fecha: " + this.fecha_normal + ", turnos: TRI-TRT-TRM, tipos: P-T-D";
        Map<String, Object> entrantes = new HashMap<>();
        try {
            AtributosLogBD.ia = InetAddress.getLocalHost();
            AtributosLogBD.inet_address = InetAddress.getLoopbackAddress();
            File reserva = new File(Util.getPro().getProperty("reserva_respuestas"));
            if (!reserva.exists()) {
                AtributosLogBD.linea_dato = "91";
                archivo.crearArchivo(reserva);
            }
            if (reserva.exists()) {
                if (archivo.esFechaHoyIgualQueFechaArchivo(reserva)) {
                    archivo.llenarListaDesdeArchivo(reserva, nombres_entrada);
                } else {
                    reserva.delete();
                    log.info("Se eliminó el archivo para crear uno nuevo este día...");
                    AtributosLogBD.linea_dato = "99";
                    archivo.crearArchivo(reserva);
                }
            }
            List<Map<String, Object>> adjunto = new ArrayList<>();
            for (File files : Objects.requireNonNull(documento.listFiles())) {
                this.archivo_lect = files.getName();
                log.info("Archivo actual:" + files.getName());
                if (!this.nombres_entrada.contains(files.getName()) & files.getName().length() > 11) {
                    FileReader file_reader = archivo.getLeerArchivo(files);
                    FileReader file_reader2 = archivo.getLeerArchivo(files);
                    BufferedReader tipo_reader = new BufferedReader(file_reader);
                    BufferedReader trama = new BufferedReader(file_reader2);
                    String tipo = "";
                    try {
                        List<Character> chars = tipo_reader.lines().filter(e -> e.charAt(0) == '1').map(e -> e.charAt(2)).collect(Collectors.toList());
                        tipo = mapa.get(chars.get(chars.size() - 1));
                        AtributosLogBD.linea_dato = "118";
                        this.insertarTrama(trama, files, tipo);
                        tipo_reader.close();
                        trama.close();
                    } catch (IOException e) {
                        AtributosLogBD.error_interno = e.getMessage();
                        log.error("Error al procesar carpeta: " + e.getMessage());
                    }
                    this.fecha_normal = this.fecha_reves.substring(6) + this.fecha_reves.substring(4, 6) + this.fecha_reves.substring(0, 4);
                    log.info("Iniciando proceso de validación de la trama...");
                    String indice = "0";
                    AtributosLogBD.linea_dato = "129";
                    entrantes = iservice_archivo.spProcesaEntrantes(this.fecha_normal, this.turno, tipo, files.getName(), indice);
                    log.info("Finalizando: " + entrantes);
                    if (!((String) entrantes.get("VO_IND")).equals("1")) {
                        indice = "1";
                        AtributosLogBD.linea_dato = "134";
                        iservice_archivo.spProcesaEntrantes(this.fecha_normal, this.turno, tipo, files.getName(), indice);
                        log.info("Iniciando guardado en la tabla histórica...");
                    }
                    AtributosLogBD.mensaje = "PROCESO CORRECTO";
                    this.nombres_entrada.add(files.getName());
                    log.info("Elemento añadido a la lista de nombres para evitar lecturas redundantes...");
                    log.info("Elementos de la lista de nombres: " + this.nombres_entrada);
                    log.info("Lectura finalizada");
                    this.ejecutar_log = true;
                } else if (files.getName().length() < 13 & !this.nombres_entrada.contains(files.getName())) {
                    AtributosLogBD.mensaje = "PROCESO CORRECTO";
                    log.info("SE ESTAN AGREGANDO ADJUNTOS...");
                    this.nombres_entrada.add(files.getName());
                    adjunto.add(this.correoService.agregarArchivoComoAdjunto(files));
                    this.ejecutar_log = true;
                }
            }
            if (!adjunto.isEmpty()) {
                AtributosLogBD.linea_dato = "157";
                HttpHeaders httpHeaders = this.correoService.AsignarAuthenticacionBasica("WBANKS", "167f43f30f6cf891747582ca74697c0f");
                String response;
                AtributosLogBD.linea_dato = "160";
                if (!this.correoService.EnviarCorreoConAdjunto(httpHeaders, adjunto)) {
                    AtributosLogBD.fin = System.nanoTime();
                    AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
                    AtributosLogBD.status = "90";
                    AtributosLogBD.mensaje = "PROCESO INCORRECTO DE ENVIO DE CORREOS";
                    response = "{vo_msn=Error inesperado del Sistema., message=Error inesperado "
                            + "del Sistema., vo_ind=1, status=96}";
                    AtributosLogBD.error_interno = "Error inesperado del Sistema.";
                    if (intentos_enviar_correo < 3) {
                        reserva.delete();
                        this.nombres_entrada.removeIf(e -> e.length() < 13);
                        archivo.crearArchivo(reserva);
                        JOptionPane.showMessageDialog(null, "Error. Se procesó correctamente el conjunto de archivos, pero no se ha podido "
                                + "enviar un correo con los archivos de respuesta. \nEl sistema intentará ejecutar este proceso nuevamente. \nSi el problema "
                                + "persiste, verifique que el correo ingresado sea correcto y que haya una conexión a internet. \nMáximo número de intentos: 3."
                                + " \nIntento N°: " + (intentos_enviar_correo + 1));
                        intentos_enviar_correo++;
                    } else {
                        JOptionPane.showMessageDialog(null, "Todos los intentos fueron consumidos. Por favor, verifique el correo \n"
                                + " y reinicie la aplicación para cargar los archivos al correo.");
                    }
                } else {
                    AtributosLogBD.fin = System.nanoTime();
                    AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
                    AtributosLogBD.mensaje = "PROCESO CORRECTO DE ENVIO DE CORREOS";
                    AtributosLogBD.error_interno = "SIN ERRORES";
                    AtributosLogBD.status = "0";
                    response = "msg: Proceso Exitoso., status: 00";
                }
                if (!this.nombres_entrada.isEmpty()) {
                    archivo.llenarArchivoRespuestasDesdeLista(reserva, nombres_entrada);
                }
                iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "91", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                        "com.editran.service.ProcesarArchivo.cargaEntrantes", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Usuario: WBANKS",
                        "Contraseña: Protegida", "DATOS DEL CORREO", "Ejecución de EnviarCorreoConAdjunto",
                        "Línea: " + AtributosLogBD.linea_dato, "Envío de Correo...", "", response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(), AtributosLogBD.ia.getHostAddress(),
                        AtributosLogBD.inet_address.getHostAddress(), "");
                AtributosLogBD.mensaje = "PROCESO CORRECTO.";
                log.info("Log del correo enviado...");
            }
            AtributosLogBD.linea_dato = "202";
//            cuerpo.moverArchivos();
        } catch (Exception e) {
            AtributosLogBD.request = "Datos Erróneos...";
            AtributosLogBD.error_interno = e.getMessage();
            AtributosLogBD.mensaje = "PROCESO INCORRECTO";
            AtributosLogBD.status = "90";
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error al procesarEntrantes. Puede que la conexión para enviar correos haya sido saturada.");
        }
        if (this.ejecutar_log) {
            AtributosLogBD.fin = System.nanoTime();
            AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
            iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "91", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                    "com.editran.service.ProcesarArchivo.cargaEntrantes", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), this.fecha_normal,
                    this.turno, this.archivo_lect, "Validación this.ejecutar_log", "Ejecución hasta la línea: " + AtributosLogBD.linea_dato, "", "", entrantes.toString(), AtributosLogBD.error_interno,
                    AtributosLogBD.ia.getHostName(), AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            this.ejecutar_log = false;
        }
    }

//    @Scheduled(cron = "0 01/05 12 * * *")
//    @Scheduled(cron = "0 0 13 * * *")
//    @Scheduled(cron = "0 31-59/5 14 * * *")
//    @Scheduled(cron = "0 0 15 * * *")
//    @Scheduled(cron = "0 30 18-23 * * *")
//    @Scheduled(cron = "0 30 00-06 * * *")
    @Scheduled(cron = "0/20 * * * * *")
    public void generarArchivoRechazado() {
        AtributosLogBD.inicio = System.nanoTime();
        AtributosLogBD.request = "Datos -> Fecha: " + this.fecha_hoy_normal + ", Tipo: R" + ", Sesión: TRT-TRM-TRI";
        try {
            AtributosLogBD.ia = InetAddress.getLocalHost();
            AtributosLogBD.inet_address = InetAddress.getLoopbackAddress();
            LocalTime local_time = LocalTime.now();
            // Sesión Intermedia
            LocalTime hora_inicioI = LocalTime.of(12, 00);
            LocalTime hora_finI = LocalTime.of(13, 0);
            // Sesión Mañana
            LocalTime hora_inicioM = LocalTime.of(14, 30);
            LocalTime hora_finM = LocalTime.of(15, 0);

            if (local_time.isAfter(hora_inicioI) & local_time.isBefore(hora_finI)) {
                AtributosLogBD.sesion = "TRI";
            } else if (local_time.isAfter(hora_inicioM) & local_time.isBefore(hora_finM)) {
                AtributosLogBD.sesion = "TRM";
            } else {
                AtributosLogBD.sesion = "TRT";
            }
            AtributosLogBD.linea_dato = "255";
            Map<String, Object> procesaR = iservice_archivo.spProcesaRechazadosConfirmados(this.fecha_hoy_normal, "R", AtributosLogBD.sesion);
            log.info("Procesando rechazado: " + procesaR);
            String nombre_archivo = procesaR.get("vo_nombre_archivo").toString();
            File existe_archivo = new File(Util.getPro().getProperty("salida") + "\\" + nombre_archivo);
            if (!existe_archivo.exists()) {
                AtributosLogBD.response = procesaR.toString();
                AtributosLogBD.mensaje = "PROCESO CORRECTO.";
                AtributosLogBD.linea_dato = "263";
                Map<String, Object> genera_archivo = iservice_archivo.spGenerarArchivo(this.fecha_hoy_normal, AtributosLogBD.sesion, "R", nombre_archivo);
                log.info("Archivo generado del método generarRechazado: " + genera_archivo);
                List<LinkedCaseInsensitiveMap> elementos_trama = (ArrayList<LinkedCaseInsensitiveMap>) genera_archivo.get("co_cursor");
                List<String> elementos_nuevo_txt = new ArrayList<>();
                elementos_trama.forEach(e -> {
                    elementos_nuevo_txt.add(e.get("DES_TRAMA").toString());
                });
                File salida = new File(Util.getPro().getProperty("salida") + "\\" + nombre_archivo);
                log.info("Nombre del archivo SALIDA en el método generaRechazados: " + salida.getName());
                FileWriter file_writer = new FileWriter(salida);
                try (BufferedWriter bw = new BufferedWriter(file_writer)) {
                    elementos_nuevo_txt.forEach(e -> {
                        try {
                            bw.append(e);
                            bw.newLine();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "La ruta del archivo de Salida no fue ubicada");
                        }
                    });
                    bw.flush();
                    bw.close();
                }
                AtributosLogBD.fin = System.nanoTime();
                AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
                log.info("Nuevo archivo generado...");
//            cuerpo.emitirArchivos();
                AtributosLogBD.linea_dato = "290";
                iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                        "com.editran.service.ProcesarArchivo.generarArchivoRechazado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                        "Fecha: " + this.fecha_hoy_normal, "Tipo: R", "Método de la clase ProcesarArchivo", "Ejecucion hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                        AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            }
        } catch (Exception e) {
            AtributosLogBD.fin = System.nanoTime();
            AtributosLogBD.error_interno = e.getMessage();
            AtributosLogBD.status = "90";
            AtributosLogBD.mensaje = "ERROR EN PROCESO";
            AtributosLogBD.response = "{msg: Archivo Inválido}";
            AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
            iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                    "com.editran.service.ProcesarArchivo.generarArchivoRechazado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                    "Fecha: " + this.fecha_hoy_normal, "Tipo: R", "Método de la clase ProcesarArchivo", "Ejecucion hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                    AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error inesperado al intentar generar un archivo rechazado. Las causas "
                    + "podrían ser: Se ha movido el directorio principal de carga, el archivo no contiene el formato común o "
                    + "posiblemente haya una caída de red. Por favor, inténtelo de nuevo...");
        }
    }

//    @Scheduled(cron = "0/20 * 10-18 * * *") 
//    @Scheduled(cron = "0 30/5 10-11 * * *")
//    @Scheduled(cron = "0 0 12 * * *")
//    @Scheduled(cron = "0 15-45/5 15 * * *")
//    @Scheduled(cron = "0 45/15 17-18 * * *")
    @Scheduled(cron = "0/20 * * * * *")
    public void generarArchivoConfirmado() {
        AtributosLogBD.inicio = System.nanoTime();
        AtributosLogBD.request = "Datos -> Fecha: " + this.fecha_hoy_normal + ", Tipo: C" + ", Sesión: TRT-TRM-TRI";
        try {
            AtributosLogBD.ia = InetAddress.getLocalHost();
            AtributosLogBD.inet_address = InetAddress.getLoopbackAddress();
            LocalTime local_time = LocalTime.now();
            // Sesión Tarde
            LocalTime hora_inicioT = LocalTime.of(10, 29);
            LocalTime hora_finT = LocalTime.of(12, 0);
            // Sesión Mañana
            LocalTime hora_inicioI = LocalTime.of(15, 14);
            LocalTime hora_finI = LocalTime.of(15, 45);

            if (local_time.isAfter(hora_inicioT) & local_time.isBefore(hora_finT)) {
                AtributosLogBD.sesion = "TRT";
            } else if (local_time.isAfter(hora_inicioI) & local_time.isBefore(hora_finI)) {
                AtributosLogBD.sesion = "TRI";
            } else {
                AtributosLogBD.sesion = "TRM";
            }
            log.info("Leyendo los directorios para procesar y generar confirmados...");
//            File file = new File(RUTA + "\\" + fecha_hoy + "\\");
            // Validar que sea rechazado o confirmado y verificar el turno.
            AtributosLogBD.linea_dato = "356";
            Map<String, Object> procesaC = iservice_archivo.spProcesaRechazadosConfirmados(this.fecha_hoy_normal, "C", AtributosLogBD.sesion);
            log.info("Procesando confirmado: " + procesaC);
            String nombre_archivo = procesaC.get("vo_nombre_archivo").toString();
            File file = new File(Util.getPro().getProperty("salida") + "\\" + nombre_archivo);
            if (!file.exists()) {
                AtributosLogBD.response = procesaC.toString();
                AtributosLogBD.mensaje = "PROCESO CORRECTO.";
                AtributosLogBD.linea_dato = "364";
                Map<String, Object> genera_archivo = iservice_archivo.spGenerarArchivo(this.fecha_hoy_normal, AtributosLogBD.sesion, "C", nombre_archivo);
                log.info("Archivo generado del método generarConfirmado: " + genera_archivo);
                List<LinkedCaseInsensitiveMap> elementos_trama = (ArrayList<LinkedCaseInsensitiveMap>) genera_archivo.get("co_cursor");
                List<String> elementos_nuevo_txt = new ArrayList<>();
                elementos_trama.forEach(e -> {
                    elementos_nuevo_txt.add(e.get("DES_TRAMA").toString());
                });
                File salida = new File(Util.getPro().getProperty("salida") + "\\" + nombre_archivo);
                log.info("Nombre del archivo SALIDA en el método generaConfirmados: " + salida.getName());
                AtributosLogBD.linea_dato = "374";
                FileWriter file_writer = new FileWriter(salida);
                BufferedWriter bw = new BufferedWriter(file_writer);
                elementos_nuevo_txt.forEach(e -> {
                    try {
                        bw.append(e);
                        bw.newLine();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "La ruta del archivo de Salida no fue ubicada");
                    }
                });
                bw.flush();
                bw.close();
                AtributosLogBD.fin = System.nanoTime();
                AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
                log.info("Nuevo archivo generado...");
//            cuerpo.emitirArchivos();
                AtributosLogBD.linea_dato = "391";
                iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                        "com.editran.service.ProcesarArchivo.generarArchivoConfirmado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                        "Fecha: " + this.fecha_hoy_normal, "Tipo: C",
                        "Método de la clase ProcesarArchivo", "Ejecución hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                        AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            }
        } catch (Exception e) {
            AtributosLogBD.fin = System.nanoTime();
            AtributosLogBD.error_interno = e.getMessage();
            AtributosLogBD.status = "90";
            AtributosLogBD.mensaje = "ERROR EN PROCESO";
            AtributosLogBD.response = "{msg: Archivo Inválido}";
            AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
            iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                    "com.editran.service.ProcesarArchivo.generarArchivoConfirmado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                    "Fecha: " + this.fecha_hoy_normal, "Tipo: C",
                    "Método de la clase ProcesarArchivo", "Ejecución hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                    AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error inesperado. Las causas podrían ser: Se ha movido el directorio principal de carga, "
                    + "el archivo no contiene el formato común o posiblemente haya una caída de red. Por favor, inténtelo de nuevo...");
        }
    }

//    @Scheduled(cron = "* 01/05 12 * * *")
//    @Scheduled(cron = "* 0 13 * * *")
//    @Scheduled(cron = "0/20 * 10-18 * * *")
    @Scheduled(cron = "0/20 * * * * *")
    public void generarPresentado() {
        AtributosLogBD.inicio = System.nanoTime();
        AtributosLogBD.request = "Datos -> Fecha: " + this.fecha_hoy_normal + ", Tipo: P" + ", Sesión: TRT-TRM-TRI";
        try {
            AtributosLogBD.ia = InetAddress.getLocalHost();
            AtributosLogBD.inet_address = InetAddress.getLoopbackAddress();
            // El siguiente método me retorna el nombre del archivo, un mensaje, una sesión y un índice.
            AtributosLogBD.linea_dato = "439";
            Map<String, Object> procesa_archivo = iservice_archivo.spProcesaPresentado(this.fecha_hoy_normal, "P");
            log.info("Procesando presentado: " + procesa_archivo);
            AtributosLogBD.sesion = procesa_archivo.get("vo_sesion").toString();
            String nombre_archivo = procesa_archivo.get("vo_nombre_archivo").toString();
            File file = new File(Util.getPro().getProperty("salida") + "\\" + nombre_archivo);
            if (!file.exists()) {
                AtributosLogBD.mensaje = "PROCESO CORRECTO...";
                AtributosLogBD.linea_dato = "447";
                Map<String, Object> genera_archivo = iservice_archivo.spGenerarArchivo(this.fecha_hoy_normal, AtributosLogBD.sesion, "P", nombre_archivo);
//                    Map<String, Object> genera_archivo = iservice_archivo.spGenerarArchivo("01072022", "TRI", "P", "ECTRIP_01.805");
                log.info("Procesando genera_archivo: " + genera_archivo);
                // Luego, lo que devuelva el procedimiento más lo que tenga la variable procesa_archivo, se deben crear en un nuevo archivo.
                String ruta = Util.getPro().getProperty("salida");
//                    String ruta = "D:\\Usuarios\\acosme\\Desktop\\editran_mas\\20221004\\";
                ArrayList<LinkedCaseInsensitiveMap> lista = (ArrayList<LinkedCaseInsensitiveMap>) genera_archivo.get("co_cursor");
                ArrayList<String> lista_convertida = new ArrayList<>();
                lista.forEach(e -> {
                    lista_convertida.add(e.get("DES_TRAMA").toString());
                });
                // Genera un nuevo archivo con el contenido correspondiente.
                File salida = new File(ruta + "\\" + nombre_archivo);
                FileWriter file_writer = new FileWriter(salida);
                try (BufferedWriter bw = new BufferedWriter(file_writer)) {
                    lista_convertida.forEach(e -> {
                        try {
                            bw.append(e);
                            bw.newLine();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "La ruta del archivo de Salida no fue ubicada");
                        }
                    });
                    bw.flush();
                    bw.close();
                }
                // Ejecutar procedimiento LOG aquí.
                log.info("Nuevo archivo generado...");
//                cuerpo.emitirArchivos();
                AtributosLogBD.linea_dato = "479";
                AtributosLogBD.fin = System.nanoTime();
                AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
                iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                        "com.editran.service.ProcesarArchivo.generarPresentado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                        "Fecha: " + this.fecha_hoy_normal, "Tipo: P",
                        "Método de la clase ProcesarArchivo", "Ejecución hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                        AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
            }
        } catch (Exception e) {
            AtributosLogBD.fin = System.nanoTime();
            AtributosLogBD.error_interno = e.getMessage();
            AtributosLogBD.status = "90";
            AtributosLogBD.mensaje = "ERROR EN PROCESO";
            AtributosLogBD.response = "{msg: Archivo Inválido}";
            AtributosLogBD.tiempo_proceso = (int) ((AtributosLogBD.fin - AtributosLogBD.inicio) / 1e9);
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error al intentar generar archivos Presentados");
            iservice_archivo.generarLogsEnBD(AtributosLogBD.FECHAFORMATOID.format(AtributosLogBD.hoy), "90", AtributosLogBD.status, AtributosLogBD.mensaje, AtributosLogBD.request, AtributosLogBD.mensaje,
                    "com.editran.service.ProcesarArchivo.generarPresentado", "21", AtributosLogBD.tiempo_proceso, "", "", AtributosLogBD.FECHAORIGEN.format(AtributosLogBD.hoy), "Sesión: " + AtributosLogBD.sesion,
                    "Fecha: " + this.fecha_hoy_normal, "Tipo: P",
                    "", "Ejecución hasta la línea: " + AtributosLogBD.linea_dato, "", "", AtributosLogBD.response, AtributosLogBD.error_interno, AtributosLogBD.ia.getHostName(),
                    AtributosLogBD.ia.getHostAddress(), AtributosLogBD.inet_address.getHostAddress(), "");
        }
    }

    private void insertarTrama(BufferedReader reader, File files, String tipo) throws IOException {
        String linea;
        int contador_trama = 0;
        int num_envio = 1;
        log.info("Iniciando ejecución de InsertarTrama...");
        while ((linea = reader.readLine()) != null) {
            contador_trama++;
            this.contenido_fichero += linea;
            if (linea.charAt(0) == '1') {
                this.turno = linea.substring(3, 6);
                this.fecha_reves = linea.substring(22, 30);
            }
            if (contador_trama == 4) {
                iservice_archivo.spInsertarTrama(this.contenido_fichero, tipo, contador_trama, num_envio, turno, files.getName());
                log.info("Se insertó una trama de 4 ...");
                this.contenido_fichero = "";
                contador_trama = 0;
                num_envio++;
            }
        }
        if (!this.contenido_fichero.equals("")) {
            Map<String, Object> mapa = iservice_archivo.spInsertarTrama(this.contenido_fichero, tipo, contador_trama, num_envio, turno, files.getName());
            log.info("Se insertó el restante de la trama: " + mapa);
        }
    }
}
