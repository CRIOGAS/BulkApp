package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

public class UnidadProductora {

    private String tanque;

    public UnidadProductora(JSONObject obj) {
        try {
            this.tanque = obj.getString("tanque");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getTanque() {
        return tanque;
    }

    public void setTanque(String tanque) {
        this.tanque = tanque;
    }
}
