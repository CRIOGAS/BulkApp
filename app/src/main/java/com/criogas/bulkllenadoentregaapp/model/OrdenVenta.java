package com.criogas.bulkllenadoentregaapp.model;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class OrdenVenta implements Serializable {

    private int folio;
    private String cliente;
    private Date creado;
    private String cveOperacion;
    private String promotor;
    private String direccion;
    private String nombre_c;
    private String sucursal;
    private boolean isSeleccionado;
    private String producto;
    private String desccorta;
    private String gas_dp;
    private double qty;
    private String pipa;
    private String udm;
    private String revision;

    public OrdenVenta() {
    }

    public OrdenVenta(int folio, String cliente, Date creado, String cveOperacion, String promotor, String direccion, String nombre_c, String sucursal, boolean isSeleccionado) {
        this.folio = folio;
        this.cliente = cliente;
        this.creado = creado;
        this.cveOperacion = cveOperacion;
        this.promotor = promotor;
        this.direccion = direccion;
        this.nombre_c = nombre_c;
        this.sucursal = sucursal;
        this.isSeleccionado = isSeleccionado;
    }

    public OrdenVenta(int folio, String producto) {
        this.folio = folio;
        this.producto = producto;
    }

    public OrdenVenta(JSONObject obj){
        try {
            this.folio = obj.getInt("folio");
            this.cliente = obj.getString("cliente");

            this.cveOperacion = obj.getString("cveoperacion");
            this.promotor = obj.getString("promotor");
            this.direccion = obj.getString("direccion");
            this.nombre_c = obj.getString("nombre_c");
            this.sucursal = obj.getString("sucursal");
            DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
            this.creado = dateFormat.parse(obj.getString("creado"));
            this.producto = obj.getString("producto");
            this.desccorta = obj.getString("desccorta");
            this.gas_dp = obj.getString("gas_dp");

        }catch (Exception e){

        }

    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Date getCreado() {
        return creado;
    }

    public void setCreado(Date creado) {
        this.creado = creado;
    }

    public String getCveOperacion() {
        return cveOperacion;
    }

    public void setCveOperacion(String cveOperacion) {
        this.cveOperacion = cveOperacion;
    }

    public String getPromotor() {
        return promotor;
    }

    public void setPromotor(String promotor) {
        this.promotor = promotor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombre_c() {
        return nombre_c;
    }

    public void setNombre_c(String nombre_c) {
        this.nombre_c = nombre_c;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public boolean isSeleccionado() {
        return isSeleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        isSeleccionado = seleccionado;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getDesccorta() {
        return desccorta;
    }

    public void setDesccorta(String desccorta) {
        this.desccorta = desccorta;
    }

    public String getGas_dp() {
        return gas_dp;
    }

    public void setGas_dp(String gas_dp) {
        this.gas_dp = gas_dp;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public String getPipa() {
        return pipa;
    }

    public void setPipa(String pipa) {
        this.pipa = pipa;
    }

    public String getUdm() {
        return udm;
    }

    public void setUdm(String udm) {
        this.udm = udm;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
