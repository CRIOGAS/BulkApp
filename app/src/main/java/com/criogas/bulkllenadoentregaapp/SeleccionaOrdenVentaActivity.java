package com.criogas.bulkllenadoentregaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.criogas.bulkllenadoentregaapp.model.Pipas;
import com.criogas.bulkllenadoentregaapp.rest.RestInfoLenadoPipaImplement;
import com.criogas.bulkllenadoentregaapp.rest.RestInfoLlenadoPipa;
import com.criogas.bulkllenadoentregaapp.rest.RestOrdenVentaI;
import com.criogas.bulkllenadoentregaapp.rest.RestOrdenVentaImplement;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;


public class SeleccionaOrdenVentaActivity extends AppCompatActivity {

    private Context ctx;
    public String clavePipa;
    public static String idsOrdenes = "";
    public static String productoSeleccionado = "";
    private SweetAlertDialog progressUpdateDialog;
    public ListView lstViewOrdenVentaPipa;
    public Spinner spinnerPipas = null;
    public static ArrayList<OrdenVenta> lstOrdenesDeVenta = new ArrayList<OrdenVenta>();
    public static List<Pipas> listaPipas = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecciona_orden_venta);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstViewOrdenVentaPipa = (ListView) findViewById(R.id.lstViewOrdenesVenta);
        spinnerPipas = (Spinner) findViewById(R.id.spinnerPipas);

        this.fillListaPipas(spinnerPipas);

        spinnerPipas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null) {
                    clavePipa = listaPipas.get(i).getCvepipa();
                    Toast toast = Toast.makeText(SeleccionaOrdenVentaActivity.this,
                            "PIPA : " + clavePipa, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        fillListaOrdenVentaPipas(clavePipa);
                    }catch (Exception ex){
                        Log.i("ERROR","ERROR");
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(SeleccionaOrdenVentaActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando");
        progressUpdateDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.btnSaveMenu:
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirmación")
                        .setContentText("¿Está seguro que desea continuar?")
                        .setConfirmText("Si. Continuar.")
                        .setCancelText("Cancelar")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                                sincronizaInfoOrdenVentaPipa();
                                /*Intent intent = new Intent(SeleccionaOrdenVentaActivity.this,CreaLoteLLneado.class);
                                startActivity(intent);
                                sDialog.dismissWithAnimation();*/
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SeleccionaOrdenVentaActivity.this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    private void fillListaOrdenVentaPipas(String clavePipaSeleccionada){
        try{
            startProgressBar();
            RestOrdenVentaI restOrdenVentaI = new RestOrdenVentaImplement(SeleccionaOrdenVentaActivity.this,progressUpdateDialog);
            restOrdenVentaI.fillListaOrdenVentaPipas(lstViewOrdenVentaPipa, clavePipaSeleccionada);
        } catch (Exception ex) {
            Log.i("ERROR", "ERRRORRR!!!!");
            ex.printStackTrace();
        }
    }

    private void fillListaPipas(Spinner spinnerPipas){
        try{
            startProgressBar();
            progressUpdateDialog.show();
            RestInfoLlenadoPipa restInfoLlenadoPipa = new RestInfoLenadoPipaImplement(SeleccionaOrdenVentaActivity.this,progressUpdateDialog);
            restInfoLlenadoPipa.fillListaPipas(spinnerPipas);

        } catch (Exception ex) {
            Log.i("ERROR", "ERRRORRR!!!!");
            ex.printStackTrace();
        }
    }



    private void sincronizaInfoOrdenVentaPipa(){
        String idOrdenes = "";
        String gasDpSeleccionado = "";
        String descProductoSeleccionado = "";
        String cveProductoSeleccionado = "";
        int count=0;
        boolean ok = true;
        ArrayList<OrdenVenta> listaOVSeleccionada = new ArrayList<>();

        for(OrdenVenta ov : SeleccionaOrdenVentaActivity.lstOrdenesDeVenta){
            if(ov.isSeleccionado()){
                listaOVSeleccionada.add(ov);
                if(idOrdenes.equals("")){
                    idOrdenes = ov.getFolio() + "";
                }else{
                    idOrdenes += "," + ov.getFolio();

                }

                if(gasDpSeleccionado.equals("")){
                    gasDpSeleccionado=ov.getGas_dp();
                    cveProductoSeleccionado = ov.getProducto();
                }else{
                    if(!gasDpSeleccionado.equals(ov.getGas_dp())){
                        ok = false;
                    }else{
                    }

                }
            }
            count ++;
        }

        if(gasDpSeleccionado.equals("")) ok = false;

        SeleccionaOrdenVentaActivity.idsOrdenes = idOrdenes;
        SeleccionaOrdenVentaActivity.productoSeleccionado = cveProductoSeleccionado;

        if(ok) {
            Intent  intent = new Intent(SeleccionaOrdenVentaActivity.this, CreaLoteLLneado.class);
            intent.putExtra("idOrdenes", idOrdenes);
            intent.putExtra("producto", cveProductoSeleccionado);
            intent.putExtra("listaOrdenProductoSeleccionado", listaOVSeleccionada);
            intent.putExtra("cvePipa",clavePipa);
            startActivity(intent);
        }else{
            MensajeAlerta();
        }
        /*new RestOrdenVentaImplement(
                SeleccionaOrdenVentaActivity.this,
                progressUpdateDialog
        ).sincronizaInfoOrdenesVentaPipa(idOrdenes);*/
    }

    public void MensajeAlerta(){
        progressUpdateDialog.dismiss();
        new SweetAlertDialog(SeleccionaOrdenVentaActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Verifique OV seleccionada")
                .setContentText("Seleccione ordenes de venta del mismo producto o seleccione al menos una OV.")
                //.setCancelText("OK")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Intent intent = new Intent(SeleccionaOrdenVentaActivity.this,SeleccionaOrdenVentaActivity.class);
                        startActivity(intent);
                        progressUpdateDialog.hide();
                    }
                }).show();
    }

}
