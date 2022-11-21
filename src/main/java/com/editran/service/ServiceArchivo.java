package com.editran.service;

import com.editran.Repository.EditranRepo;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceArchivo implements IServiceArchivo {

    @Autowired
    EditranRepo editranRepo;

    public void getTest() {
        editranRepo.getTrama();
    }

    @Override
    public Map<String, Object> spInsertarTrama(String trama, String tipo, Integer cantidad, Integer indicador, String turno, String nombre_archivo) {
        return editranRepo.insertaTrama(trama, tipo, cantidad, indicador, turno, nombre_archivo);
    }

    @Override
    public Map<String, Object> spProcesaEntrantes(String fecha, String turno, String tipo, String nombre_archivo, String validar) {
        return editranRepo.procesaEntrantes(fecha, turno, tipo, nombre_archivo, validar);
    }

    @Override
    public Map<String, Object> spGenerarArchivo(String fecha, String turno, String tipo, String nombre_archivo) {
        return editranRepo.generaPresentado(fecha, turno, tipo, nombre_archivo);
    }

    @Override
    public Map<String, Object> spProcesaRechazadosConfirmados(String fecha, String tipo_archivo, String sesion) {
        return editranRepo.procesaRechazadosConfirmados(fecha, tipo_archivo, sesion);
    }

    @Override
    public Map<String, Object> spProcesaPresentado(String fecha, String tipo_archivo) {
        return editranRepo.procesaPresentado(fecha, tipo_archivo);
    }

    @Override
    public void generarLogsEnBD(String id_registro, String cod_transaccion, String status, String message, String request,
            String descripcion, String metodo, String cod_tipo_componente, int time_proceso, String id_sesion,
            String id_trace, String fecha_origen, String dato_clave, String dato_clave2, String dato_clave3,
            String referencia1, String referencia2, String referencia3, String referencia4, String response,
            String error_interno, String ip_server, String ip_origen, String origen_name, String ip_usuario_red) {
        editranRepo.generarLogsEnBD(id_registro, cod_transaccion, status, message, request, descripcion, metodo,
                cod_tipo_componente, time_proceso, id_sesion, id_trace, fecha_origen, dato_clave, dato_clave2, dato_clave3,
                referencia1, referencia2, referencia3, referencia4, response, error_interno, ip_server, ip_origen, origen_name, ip_usuario_red);
    }
}
