package com.criogas.bulkllenadoentregaapp.rest;

import android.widget.ListView;

public interface RestOrdenVentaI {
    void fillListaOrdenVentaPipas(ListView listView, String clavePipaSeleccionada);
    void sincronizaInfoOrdenesVentaPipa(String idsOrdenesVenta);
}
