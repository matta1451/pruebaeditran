package com.editran.service;

import com.editran.variable.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("CorreoService")
public class CorreoService implements ICorreoService {

    private final RestTemplate rest = new RestTemplate();
    private final String to = Util.getPro().getProperty("correo");

    public CorreoService() {

    }

    @Override
    public boolean EnviarCorreoConAdjunto(HttpHeaders httpHeaders, List<Map<String, Object>> adjunto) {
        try {
            HttpHeaders hh = httpHeaders;
            Map<String, Object> mapa = new HashMap<>();
            // Subject es ASUNTO
            mapa.put("to", to);
            mapa.put("multipleTo", "");
            mapa.put("from", null);
            mapa.put("pass", null);
            mapa.put("cc", "");
            mapa.put("bbc", "");
            mapa.put("multipleCc", "");
            mapa.put("multipleBcc", "");
            mapa.put("subject", "Archivos de Respuesta");
            mapa.put("body", "");
            mapa.put("attachment", adjunto);
            mapa.put("ID_OPER", "285");
            HttpEntity<Map<String, Object>> he = new HttpEntity<>(mapa, hh);
            Map<String, Object> resultado = rest.postForObject("http://172.20.3.173/protocolo/CmacCorewebWS/cwServiciosIntegracion.json",
                    he, Map.class);
            if(!resultado.get("status").equals("94")){
                return true;
            }
        } catch (Exception e) {
            
        }
        return false;
    }

    @Override
    public HttpHeaders AsignarAuthenticacionBasica(String usuario, String password) {
        HttpHeaders hh = new HttpHeaders();
        hh.setBasicAuth(usuario, password);
        return hh;
    }

    public Map<String, Object> agregarArchivoComoAdjunto(File file) {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream inputStream;
        String encodedFile;
        Map<String, Object> archivo = new HashMap<>();
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(bytes);
            encodedFile = Base64.getEncoder().encodeToString(bytes);
            archivo = new HashMap<>();
            archivo.put("filename", file.getName() + ".txt");
            archivo.put("file", encodedFile);
            return archivo;
        } catch (IOException e) {
        }
        return archivo;
    }
}
