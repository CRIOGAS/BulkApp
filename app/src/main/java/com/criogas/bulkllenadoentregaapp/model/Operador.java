package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

public class Operador {
    String cveempleado;
    String nombre;

    public Operador(String cveempleado, String nombre) {
        this.cveempleado = cveempleado;
        this.nombre = nombre;
    }

    public Operador(JSONObject obj) {
        try {
            this.cveempleado = obj.getString("clave_pv");
            this.nombre = obj.getString("nombre_pv");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getCveempleado() {
        return cveempleado;
    }

    public void setCveempleado(String cveempleado) {
        this.cveempleado = cveempleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
