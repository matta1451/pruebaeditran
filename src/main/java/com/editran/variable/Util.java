package com.editran.variable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import org.springframework.stereotype.Controller;

@Controller
public class Util {
    
    public static Properties getPro(){
    
    Properties propiedades = new Properties();
    
        try {
            propiedades.load(new FileReader("C:\\Users\\csumanager\\Desktop\\Editran_Rutas\\application.properties"));
            if(propiedades.isEmpty()){
                System.out.println("No se encontro el archivo");
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
             ex.printStackTrace();
        }
        return propiedades;
    }
     
          
    public ArrayList<String> getPresentacion() {
        ArrayList<String> presentacion = new ArrayList<>();
        
        String todo = Util.getPro().getProperty("presentaciones");
        String[] presentaciones = todo.split(",");
     
        for(String rota:presentaciones) {
            presentacion.add(rota);
        }
        
        return presentacion;
        
    }
}
