package com.criogas.bulkllenadoentregaapp.dao;

import com.criogas.bulkllenadoentregaapp.model.ConversionProducto;

import java.util.ArrayList;

public interface DaoConversionesProductos {
    void saveConversionProducto(ArrayList<ConversionProducto> lstConvProductos);
    String getUnidadConverionPeso(String producto, String um);
    String getUnidadConverionVolumen(String producto, String um);
    void eliminaConversionProductos();
    ArrayList<ConversionProducto> getUnidadConverionAll(String converio);
}
