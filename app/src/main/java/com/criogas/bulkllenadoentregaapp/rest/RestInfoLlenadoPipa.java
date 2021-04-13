package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;
import android.widget.Spinner;

import com.criogas.bulkllenadoentregaapp.model.Pipas;
import java.util.List;
import java.util.ArrayList;

public interface RestInfoLlenadoPipa {
    void fillListaPipas(Spinner spinnerPipas);
    ArrayList<String> getNombrePipa(List<Pipas> listaDePipas);
    void fillListaEmpleadosLLenadores(Spinner spinner);
    void fillTanqueUnidadProductora(String producto, Spinner spinner);
    void getPesoNetoCalculado(String cveProducto, String pesoBruto, String pesoTara, String udm);
}
