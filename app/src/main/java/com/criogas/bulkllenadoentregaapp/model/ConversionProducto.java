package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

public class ConversionProducto {
    public String desCorta;
    public String lb;
    public String kg;
    public String lt;
    public String gal;
    public String pie3;
    public String mt3;
    public String seq;

    public ConversionProducto(String desCorta, String lb, String kg, String lt, String gal, String pie3, String mt3, String seq) {
        this.desCorta = desCorta;
        this.lb = lb;
        this.kg = kg;
        this.lt = lt;
        this.gal = gal;
        this.pie3 = pie3;
        this.mt3 = mt3;
        this.seq = seq;
    }

    public ConversionProducto(JSONObject object) {
        try {
            this.desCorta = object.getString("descorta_prod");
            this.lb = object.getString("LB");
            this.kg = object.getString("KG");
            this.lt = object.getString("L");
            this.gal = object.getString("GAL");
            this.pie3 = object.getString("FT3");
            this.mt3 = object.getString("M3");
            this.seq = object.getString("seq");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    public String getDesCorta() {
        return desCorta;
    }

    public void setDesCorta(String desCorta) {
        this.desCorta = desCorta;
    }

    public String getLb() {
        return lb;
    }

    public void setLb(String lb) {
        this.lb = lb;
    }

    public String getKg() {
        return kg;
    }

    public void setKg(String kg) {
        this.kg = kg;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public String getGal() {
        return gal;
    }

    public void setGal(String gal) {
        this.gal = gal;
    }

    public String getPie3() {
        return pie3;
    }

    public void setPie3(String pie3) {
        this.pie3 = pie3;
    }

    public String getMt3() {
        return mt3;
    }

    public void setMt3(String mt3) {
        this.mt3 = mt3;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }
}
