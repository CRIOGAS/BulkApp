package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

public class Cat_Rev_Tanques {
    private String unida_productora;
    private String tanque;

    public Cat_Rev_Tanques(JSONObject obj) {
        try {
            this.unida_productora = obj.getString("unida_productora");
            this.tanque = obj.getString("tanque");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getUnida_productora() {
        return unida_productora;
    }

    public void setUnida_productora(String unida_productora) {
        this.unida_productora = unida_productora;
    }

    public String getTanque() {
        return tanque;
    }

    public void setTanque(String tanque) {
        this.tanque = tanque;
    }
}
