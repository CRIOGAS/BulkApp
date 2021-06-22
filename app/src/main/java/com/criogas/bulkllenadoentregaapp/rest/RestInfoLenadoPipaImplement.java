package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.CreaLoteLLneado;
import com.criogas.bulkllenadoentregaapp.MainActivity;
import com.criogas.bulkllenadoentregaapp.R;
import com.criogas.bulkllenadoentregaapp.SeleccionaOrdenVentaActivity;
import com.criogas.bulkllenadoentregaapp.model.Cat_Rev_Tanques;
import com.criogas.bulkllenadoentregaapp.model.ConfigServer;
import com.criogas.bulkllenadoentregaapp.model.Operador;
import com.criogas.bulkllenadoentregaapp.model.Pipas;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Collator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class RestInfoLenadoPipaImplement implements RestInfoLlenadoPipa {
    private Context ctx;
    public List<String> lstNombrePipas = new ArrayList<>();
    private List<Operador> lstLlenadores = new ArrayList<>();
    private List<String> lstUnidadMedida = new ArrayList<>();
    private List<String> lstUnidadProductora = new ArrayList<>();
    public List<Cat_Rev_Tanques> lstTanque_UnidadProductora = new ArrayList<>();
    private SweetAlertDialog progressUpdateDialog;


    List<Cat_Rev_Tanques> listaUnidaProductora_Tanques = new ArrayList<>();

    public RestInfoLenadoPipaImplement(Context ctx, SweetAlertDialog progressUpdateDialog) {
        this.ctx = ctx;
        this.progressUpdateDialog = progressUpdateDialog;
    }

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando");
        progressUpdateDialog.setCancelable(false);
    }

    private void showConectionError(String error) {
        progressUpdateDialog.hide();
        new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("ERROR DE CONEXIÓN!")
                .setContentText("!!" + error + "¡¡")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    @Override
    public void fillListaPipas(final Spinner spinnerPipas) {
        progressUpdateDialog.setTitleText("Descargando Pipas");
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept","application/json"));
        try {
            RestClient.get(
                    ctx,
                    "api/catalogo_pipa",
                    headers.toArray(new Header[headers.size()]),
                    null,
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            try {

                                SeleccionaOrdenVentaActivity.listaPipas = new ArrayList<Pipas>();
                                for(int i = 0; i < response.length(); i ++){
                                    SeleccionaOrdenVentaActivity.listaPipas.add(new Pipas(response.getJSONObject(i)));
                                    //lista.add(new Pipas(response.getJSONObject(i)));
                                }
                                lstNombrePipas = getNombrePipa(SeleccionaOrdenVentaActivity.listaPipas);
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ctx.getApplicationContext(), R.layout.spinner_item, lstNombrePipas
                                );
                                adapter.setDropDownViewResource(R.layout.spinner_item);
                                spinnerPipas.setAdapter(adapter);

                                progressUpdateDialog.hide();
                                //fillListaEmpleadosLLenadores();

                            }catch (Exception ex){
                                showConectionError(ex.toString());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            showConectionError(throwable.getMessage());
                            Toast toast = Toast.makeText(ctx,
                                    "ERROR AL SINCRONIZAR PIPAS" + throwable.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            if(retryNo == 2) showConectionError("Error de Conexión");
                        }
                    }
                    );
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void fillListaEmpleadosLLenadores(final Spinner spinner) {
        progressUpdateDialog.setTitleText("Sincronizando Operadores...");
        progressUpdateDialog.show();
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept","application/json"));
        try {
            RestClient.get(
                    ctx,
                    "api/operador_llenado_pipa?sucursal=" + ConfigServer.SUCURSAL,
                    headers.toArray(new Header[headers.size()]),
                    null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            try {
                                CreaLoteLLneado.listaOperadoresLLenadores = new ArrayList<>();
                                for (int i = 0; i < response.length(); i++) {
                                    CreaLoteLLneado.listaOperadoresLLenadores.add(new Operador(response.getJSONObject(i)));
                                }
                                for (int i = 0; i < CreaLoteLLneado.listaOperadoresLLenadores.size(); i++) {
                                    CreaLoteLLneado.listaNombreEmpleado.add(CreaLoteLLneado.listaOperadoresLLenadores.get(i).getCveempleado()
                                            + " - " + CreaLoteLLneado.listaOperadoresLLenadores.get(i).getNombre());
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ctx.getApplicationContext(), R.layout.spinner_item, CreaLoteLLneado.listaNombreEmpleado
                                );
                                adapter.setDropDownViewResource(R.layout.spinner_item);
                                spinner.setAdapter(adapter);
                            }catch (Exception ex){
                                ex.printStackTrace();
                                showConectionError(ex.toString());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            showConectionError(throwable.getMessage());
                            Toast toast = Toast.makeText(ctx,
                                    "ERROR AL SINCRONIZAR OPERADORES" + throwable.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            if(retryNo == 2) showConectionError("Error de Conexión");
                        }
                    }

            );
        }catch (Exception ex){
            ex.printStackTrace();
            Toast toast = Toast.makeText(ctx,
                    "ERROR AL SINCRONIZAR TANQUES" + ex.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            progressUpdateDialog.hide();
        }
    }

    @Override
    public  void fillTanqueUnidadProductora(String producto, final Spinner spinner) {
        progressUpdateDialog.setTitleText("Sincronizando Tanque...");
        progressUpdateDialog.show();
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept","application/json"));
        try {
            RestClient.get(
                    ctx,
                    "api/cat_rev_tanques?cveproducto=" + producto + "&sucursal=" + ConfigServer.SUCURSAL,
                    headers.toArray(new Header[headers.size()]),
                    null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            try {
                                CreaLoteLLneado.listaUnidaProductora_Tanques = new ArrayList<>();
                                for (int i = 0; i < response.length(); i++) {
                                    CreaLoteLLneado.listaUnidaProductora_Tanques.add(new Cat_Rev_Tanques (response.getJSONObject(i)));
                                }

                                for(int i=0; i < CreaLoteLLneado.listaUnidaProductora_Tanques.size(); i++){
                                    if(CreaLoteLLneado.listaUnidadProductora.contains(CreaLoteLLneado.listaUnidaProductora_Tanques.get(i).getUnida_productora())==false) {
                                        CreaLoteLLneado.listaUnidadProductora.add(CreaLoteLLneado.listaUnidaProductora_Tanques.get(i).getUnida_productora());
                                    }
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        ctx.getApplicationContext(),
                                        R.layout.spinner_item,
                                        CreaLoteLLneado.listaUnidadProductora);

                                adapter.setDropDownViewResource(R.layout.spinner_item);
                                spinner.setAdapter(adapter);
                                progressUpdateDialog.hide();

                            }catch (Exception ex){
                                ex.printStackTrace();
                                Toast toast = Toast.makeText(ctx,
                                        "ERROR AL SINCRONIZAR TANQUES" + ex.getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                                progressUpdateDialog.hide();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            showConectionError(throwable.getMessage());
                            Toast toast = Toast.makeText(ctx,
                                    "ERROR AL SINCRONIZAR OPERADORES" + throwable.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            if(retryNo == 2) showConectionError("Error de Conexión");
                        }
                    }
            );
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public  void getPesoNetoCalculado(final String cveProducto, final String pesoBruto, final String pesoTara, final String udm) {
        progressUpdateDialog.setTitleText("Calculando Peso Neto...");
        progressUpdateDialog.show();

        final NumberFormat formatter = new DecimalFormat("#0.00");

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept","application/json"));
        try {
            RestClient.get(
                    ctx,
                    "api/factorConversionPipas?producto=" + cveProducto +
                                                            "&pesoBruto=" + pesoBruto +
                                                            "&pesoTara=" + pesoTara +
                                                            "&unidadConversion=" + udm,
                    headers.toArray(new Header[headers.size()]),
                    null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            try {
                                String response = new String(responseBody, "UTF-8");
                                CreaLoteLLneado.pesoNeto = Double.parseDouble(response);
                                CreaLoteLLneado.editTextVolumenNeto.setText(CreaLoteLLneado.pesoNeto.toString());
                                progressUpdateDialog.hide();
                            }catch (Exception ex){
                                ex.printStackTrace();
                                Toast toast = Toast.makeText(ctx,
                                        "ERROR AL CALCULAR PESO NETO" + ex.getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                                progressUpdateDialog.hide();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            progressUpdateDialog.dismissWithAnimation();
                            // new MensajeErrorConexion(ctx).showError();

                            new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error Conexión")
                                    .setContentText("Error al conectar a: " + RestClient.BASE_URL)
                                    .setConfirmText("¡Ok!")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            Intent intent = new Intent(ctx, MainActivity.class);
                                            ctx.startActivity(intent);
                                        }
                                    }).show();
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            if(retryNo == 2) showConectionError("Error de Conexión");
                        }
                    }
            );
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> getNombrePipa(List<Pipas> lstPipas) {
        try {
            ArrayList<String> listDeNombresPipas = new ArrayList<>();
            for (int i = 0; i < lstPipas.size(); i++) {
                listDeNombresPipas.add(lstPipas.get(i).getNombre());
            }
            return listDeNombresPipas;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

}
