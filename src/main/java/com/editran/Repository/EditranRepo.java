package com.editran.Repository;

import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

@Repository
public class EditranRepo {

    @Autowired
    @Qualifier("conexionCesull")
    private JdbcTemplate jdbc_cesull;
    private final Logger log = LogManager.getLogger(EditranRepo.class);

    public List<String> getTrama() {
        int f = 0;
        try {
            String query = "SELECT COUNT(*) FROM CCESULL.BC_TIN_TRAMAS_ARCHIVO";
            f = jdbc_cesull.queryForObject(query, Integer.class);
            System.out.println("COUNT:" + f);

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        return null;
    }

    public Map<String, Object> insertaTrama(String trama, String tipo, Integer cantidad, Integer indicador, String turno, String nombre_archivo) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbc_cesull)
                .withSchemaName(ApiTransaccionDataBase.BC_TIN_P_INSERTA_TRAMA.getEsquema())
                .withCatalogName(ApiTransaccionDataBase.BC_TIN_P_INSERTA_TRAMA.getPackageDba())
                .withProcedureName(ApiTransaccionDataBase.BC_TIN_P_INSERTA_TRAMA.getStoreProcedure());

        jdbcCall.addDeclaredParameter(new SqlParameter("vi_usuario", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_trama", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("ni_size", OracleTypes.NUMBER));
        jdbcCall.addDeclaredParameter(new SqlParameter("ni_cant", OracleTypes.NUMBER));
        jdbcCall.addDeclaredParameter(new SqlParameter("ni_indicador", OracleTypes.NUMBER));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_sesion", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_tipo", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_file_name", OracleTypes.VARCHAR));

        SqlParameterSource in = new MapSqlParameterSource().
                addValue("vi_usuario", "ROUN").
                addValue("vi_trama", trama).
                addValue("ni_size", 200).
                addValue("ni_cant", cantidad).
                addValue("ni_indicador", indicador).
                addValue("vi_sesion", turno).
                addValue("vi_tipo", tipo).
                addValue("vi_file_name", nombre_archivo);

        return jdbcCall.execute(in);
    }

    public Map<String, Object> procesaEntrantes(String fecha, String turno, String tipo, String nombre_archivo, String validar) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbc_cesull)
                .withSchemaName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ARCHIVOS.getEsquema())
                .withCatalogName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ENTRANTES.getPackageDba())
                .withProcedureName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ENTRANTES.getStoreProcedure());

        jdbcCall.addDeclaredParameter(new SqlParameter("vi_fecha_presentado", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_codigo_usuario", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_tipo_archivo", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_sesion", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_nombre_archivo", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_validar", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlOutParameter("vo_msn", OracleTypes.VARCHAR));
        SqlParameterSource in = new MapSqlParameterSource().
                addValue("vi_fecha_presentado", fecha).
                addValue("vi_codigo_usuario", "ROUN").
                addValue("vi_tipo_archivo", tipo).
                addValue("vi_sesion", turno).
                addValue("vi_nombre_archivo", nombre_archivo).
                addValue("vi_validar", validar);
        return jdbcCall.execute(in);
    }

    public Map<String, Object> procesaRechazadosConfirmados(String fecha, String tipo, String sesion) {
        SimpleJdbcCall jdbc = new SimpleJdbcCall(jdbc_cesull)
                .withSchemaName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ARCHIVOS_RC.getEsquema())
                .withProcedureName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ARCHIVOS_RC.getStoreProcedure());
        jdbc.addDeclaredParameter(new SqlParameter("vi_fecha_presentado", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_codigo_usuario", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_tipo_archivo", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_sesion", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_nombre_archivo", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_ind", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_msn", OracleTypes.VARCHAR));
        SqlParameterSource sql = new MapSqlParameterSource()
                .addValue("vi_fecha_presentado", fecha)
                .addValue("vi_codigo_usuario", "ROUN")
                .addValue("vi_tipo_archivo", tipo)
                .addValue("vi_sesion", sesion);
        return jdbc.execute(sql);
    }

    public Map<String, Object> procesaPresentado(String fecha, String tipo_archivo) {
        SimpleJdbcCall jdbc = new SimpleJdbcCall(jdbc_cesull)
                .withSchemaName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ARCHIVOS.getEsquema())
                .withProcedureName(ApiTransaccionDataBase.BC_TIN_P_PROCESA_ARCHIVOS.getStoreProcedure());
        jdbc.addDeclaredParameter(new SqlParameter("vi_fecha_presentado", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_codigo_usuario", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_tipo_archivo", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlParameter("vi_ind_nulo", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_sesion", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_nombre_archivo", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_ind", OracleTypes.VARCHAR));
        jdbc.addDeclaredParameter(new SqlOutParameter("vo_msn", OracleTypes.VARCHAR));
        SqlParameterSource sql = new MapSqlParameterSource()
                .addValue("vi_fecha_presentado", fecha)
                .addValue("vi_codigo_usuario", "ROUN")
                .addValue("vi_tipo_archivo", tipo_archivo)
                .addValue("vi_ind_nulo", "N");
        return jdbc.execute(sql);
    }

    public Map<String, Object> generaPresentado(String fecha, String turno, String tipo, String nombre_archivo) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbc_cesull)
                .withSchemaName(ApiTransaccionDataBase.BC_TIN_P_GENERA_ARCHIVOS.getEsquema())
                .withCatalogName(ApiTransaccionDataBase.BC_TIN_P_GENERA_ARCHIVOS.getPackageDba())
                .withProcedureName(ApiTransaccionDataBase.BC_TIN_P_GENERA_ARCHIVOS.getStoreProcedure());

        jdbcCall.addDeclaredParameter(new SqlParameter("vi_fecha_presentado", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_sesion", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_tipo_archivo", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlParameter("vi_nombre_archivo", OracleTypes.VARCHAR));
        jdbcCall.addDeclaredParameter(new SqlOutParameter("co_cursor", OracleTypes.CURSOR));

        SqlParameterSource in = new MapSqlParameterSource().
                addValue("vi_fecha_presentado", fecha).
                addValue("vi_sesion", turno).
                addValue("vi_tipo_archivo", tipo).
                addValue("vi_nombre_archivo", nombre_archivo);

        return jdbcCall.execute(in);
    }

    public void generarLogsEnBD(String id_registro, String cod_transaccion,
            String status, String message, String request, String descripcion, String metodo, String cod_tipo_componente,
            int time_proceso, String id_sesion, String id_trace, String fecha_origen, String dato_clave, String dato_clave2,
            String dato_clave3, String referencia1, String referencia2, String referencia3, String referencia4, String response,
            String error_interno, String ip_server, String ip_origen, String origen_name, String ip_usuario_red) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbc_cesull)
                    .withSchemaName(ApiTransaccionDataBase.LOG_P_REGISTRAR.getEsquema())
                    .withProcedureName(ApiTransaccionDataBase.LOG_P_REGISTRAR.getStoreProcedure());
            jdbcCall.addDeclaredParameter(new SqlParameter("ni_cod_empresa", OracleTypes.NUMBER));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_id_registro", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_cod_canal", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_cod_usuario", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_cod_transaccion", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_status", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_message", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_request", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_descripcion", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_metodo", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_cod_tipo_componente", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("ni_time_proceso", OracleTypes.NUMBER));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_id_session", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_id_trace", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_fecha_origen", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_dato_clave_1", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_dato_clave_2", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_dato_clave_3", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_referencia_1", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_referencia_2", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_referencia_3", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_referencia_4", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_response", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_error_interno", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_ip_server", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_ip_origen", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_ip_origen_name", OracleTypes.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlParameter("vi_ip_usuario_red", OracleTypes.VARCHAR));
            SqlParameterSource in = new MapSqlParameterSource().
                    addValue("ni_cod_empresa", 1).
                    addValue("vi_id_registro", id_registro).
                    addValue("vi_cod_canal", "ETRN").
                    addValue("vi_cod_usuario", "ETRN").
                    addValue("vi_cod_transaccion", cod_transaccion).
                    addValue("vi_status", status).
                    addValue("vi_message", message).
                    addValue("vi_request", request).
                    addValue("vi_descripcion", descripcion).
                    addValue("vi_metodo", metodo).
                    addValue("vi_cod_tipo_componente", cod_tipo_componente).
                    addValue("ni_time_proceso", time_proceso).
                    addValue("vi_id_session", id_sesion).
                    addValue("vi_id_trace", id_trace).
                    addValue("vi_fecha_origen", fecha_origen).
                    addValue("vi_dato_clave_1", dato_clave).
                    addValue("vi_dato_clave_2", dato_clave2).
                    addValue("vi_dato_clave_3", dato_clave3).
                    addValue("vi_referencia_1", referencia1).
                    addValue("vi_referencia_2", referencia2).
                    addValue("vi_referencia_3", referencia3).
                    addValue("vi_referencia_4", referencia4).
                    addValue("vi_response", response).
                    addValue("vi_error_interno", error_interno).
                    addValue("vi_ip_server", ip_server).
                    addValue("vi_ip_origen", ip_origen).
                    addValue("vi_ip_origen_name", origen_name).
                    addValue("vi_ip_usuario_red", ip_usuario_red);
            jdbcCall.execute(in);
        } catch (Exception e) {
            log.info("Ha ocurrido un error al generar LOG: " + e.getMessage());
        }
    }

}
