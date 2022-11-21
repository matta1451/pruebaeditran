package com.editran.variable;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AtributosLogBD {

    public static int tiempo_proceso;
    public static long inicio;
    public static long fin;
    public static final SimpleDateFormat FECHAFORMATOID = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    public static final SimpleDateFormat FECHAORIGEN = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static InetAddress ia = null;
    public static InetAddress inet_address = null;
    public static Date hoy = new Date();
    public static String request;
    public static String error_interno = "";
    public static String status = "0";
    public static String mensaje;
    public static String sesion;
    public static String response;
    public static String linea_dato;
    public AtributosLogBD() throws IOException{
        ia = InetAddress.getLocalHost();
        inet_address = InetAddress.getLoopbackAddress();
    }
}
