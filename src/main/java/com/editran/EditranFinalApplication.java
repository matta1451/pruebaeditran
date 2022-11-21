package com.editran;

import com.editran.service.IServiceArchivo;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import org.jboss.jandex.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EditranFinalApplication implements CommandLineRunner {

    @Autowired
    private IServiceArchivo iservice_archivo;

    public static void main(String[] args) {
        ApplicationContext contexto = new SpringApplicationBuilder(Main.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        SpringApplication.run(EditranFinalApplication.class, args);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run(String... args) throws Exception {
        int seleccion = 0;
        do {
            SimpleDateFormat fechaFormatoId = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            SimpleDateFormat origen = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            InetAddress ia = InetAddress.getLocalHost();
            InetAddress inet_address = InetAddress.getLoopbackAddress();
            Date hoy = new Date();
            String id_registro = fechaFormatoId.format(hoy);
            String cod_transaccion = "92";
            String status = "0";
            String message = "PROCESO CORRECTO.";
            String metodo = "com.editran.EditranFinalApplication.run";
            String cod_tipo_componente = "21";
            String descripcion = "PROCESO CORRECTO.";
            String fecha_origen = origen.format(hoy);
            String referencia1 = "Implementación de Interfaz CommandLineRunner.";
            String ip_server = ia.getHostName();
            String ip_origen = ia.getHostAddress();
            String origen_name = inet_address.getHostAddress();
            String request = "RequestBasico [codEmpresa=1, codUsuario=ETRN, codCanal=ETRN, "
                    + "fechaTraceId= " + fecha_origen + ", ipOrigen=" + ip_origen;
            
            String response = "{errorInterno:\"\",message:PROCESO CORRECTO.,status:0,fechaResponse:" + fecha_origen + "}";
            iservice_archivo.generarLogsEnBD(id_registro, cod_transaccion, status, message, request, descripcion, metodo, cod_tipo_componente,
                    0, "", "", fecha_origen, "", "", "", referencia1, "", "", "", response, "", ip_server, ip_origen, origen_name, "");
            seleccion = JOptionPane.showOptionDialog(
                    null,
                    "Aplicación Inicializada!!!. La detección de horarios fue recepcionada correctamente. El programa iniciará su ejecución automáticamente.",
                    "Selector de opciones",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, // null para icono por defecto.
                    new Object[]{"Salir"}, // null para YES, NO y CANCEL
                    "Salir");
            if (seleccion != -1) {
                switch (seleccion) {
                    case 0:
                        System.out.println("Saliendo...");
                        System.exit(0);
                        break;
                }
            }
        } while (seleccion != 4);
    }
}
