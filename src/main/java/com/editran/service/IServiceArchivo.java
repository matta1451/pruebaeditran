package com.editran.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public interface IServiceArchivo {

    Map<String, Object> spInsertarTrama(String trama, String tipo, Integer cantidad, Integer indicador, String turno, String nombre_archivo);

    Map<String, Object> spProcesaEntrantes(String fecha, String turno, String tipo, String nombre_archivo, String validar);

    Map<String, Object> spGenerarArchivo(String fecha, String turno, String tipo, String nombre_archivo);

    Map<String, Object> spProcesaRechazadosConfirmados(String fecha, String tipo_archivo, String sesion);

    Map<String, Object> spProcesaPresentado(String fecha, String tipo_archivo);

    void generarLogsEnBD(String id_registro, String cod_transaccion,
            String status, String message, String request, String descripcion, String metodo, String cod_tipo_componente,
            int time_proceso, String id_sesion, String id_trace, String fecha_origen, String dato_clave, String dato_clave2,
            String dato_clave3, String referencia1, String referencia2, String referencia3, String referencia4, String response,
            String error_interno, String ip_server, String ip_origen, String origen_name, String ip_usuario_red);
}
