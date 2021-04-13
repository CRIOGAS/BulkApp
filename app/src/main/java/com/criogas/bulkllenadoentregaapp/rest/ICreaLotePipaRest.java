package com.criogas.bulkllenadoentregaapp.rest;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;

import java.util.ArrayList;

public interface ICreaLotePipaRest {
    String creaInfoLLenadoPipa(ArrayList<OrdenVenta> listaOVSeleccionadas,
                               String fechaLlenado,
                               String producto,
                               String cveLlenador,
                               String turno,
                               String up,
                               String tanque,
                               String presion,
                               String pesoNeto,
                               String pipa);

}
