package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

import java.io.Serializable;

public class Pipas implements Serializable {
    String cvepipa;
    String nombre;

    public Pipas(String cvepipa, String nombre) {
        this.cvepipa = cvepipa;
        this.nombre = nombre;
    }

    public Pipas(String nombre) {
        this.nombre = nombre;
    }

    public Pipas(JSONObject obj){
        try {
            this.cvepipa = obj.getString("cvepipa");
            this.nombre = obj.getString("nombre");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String getCvepipa() {
        return cvepipa;
    }

    public void setCvepipa(String cvepipa) {
        this.cvepipa = cvepipa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
