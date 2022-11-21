package com.editran.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;

public interface ICorreoService {
    boolean EnviarCorreoConAdjunto(HttpHeaders cabecera, List<Map<String, Object>> adjunto);
    HttpHeaders AsignarAuthenticacionBasica(String usuario, String password);
}
