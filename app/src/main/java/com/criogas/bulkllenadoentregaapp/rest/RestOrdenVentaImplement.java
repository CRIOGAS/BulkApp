package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceActivity;
import android.widget.ListView;

import com.criogas.bulkllenadoentregaapp.MainActivity;
import com.criogas.bulkllenadoentregaapp.R;
import com.criogas.bulkllenadoentregaapp.SeleccionaOrdenVentaActivity;
import com.criogas.bulkllenadoentregaapp.Utils.MensajeErrorConexion;
import com.criogas.bulkllenadoentregaapp.Utils.OrdenVentaAdapter;
import com.criogas.bulkllenadoentregaapp.model.ConfigServer;
import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class RestOrdenVentaImplement implements RestOrdenVentaI {

    private SweetAlertDialog sweetAlertDialog;
    private Context context;
    private OrdenVentaAdapter ordenVentaAdapter;
    private SweetAlertDialog progressUpdateDialog;

    public RestOrdenVentaImplement(Context context, SweetAlertDialog sweetAlertDialog) {
        this.context=context;
        this.sweetAlertDialog=sweetAlertDialog;
    }

    @Override
    public void fillListaOrdenVentaPipas(final ListView listViewOrdeVenta, String clavePipaSeleccionada) {

        RestClient restClient =  new RestClient();
        this.ordenVentaAdapter = ordenVentaAdapter;
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Acept","application/json"));

        try{
            restClient.get(
                    context,
                    "api/PedidoPipa?cvePipa=" + clavePipaSeleccionada + "&sucursal=" + ConfigServer.SUCURSAL,
                    headers.toArray(new Header[headers.size()]),
                    null,
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                            try {
                                SeleccionaOrdenVentaActivity.lstOrdenesDeVenta = new ArrayList<OrdenVenta>();
                                for (int i = 0; i < response.length(); i++) {
                                    SeleccionaOrdenVentaActivity.lstOrdenesDeVenta.add(
                                            new OrdenVenta(response.getJSONObject(i))
                                    );
                                }

                                OrdenVentaAdapter ordenEmbarqueAdapter = new OrdenVentaAdapter(
                                        context, R.layout.row_ordenes_venta, SeleccionaOrdenVentaActivity.lstOrdenesDeVenta);
                                listViewOrdeVenta.setAdapter(ordenEmbarqueAdapter);

                                //sweetAlertDialog.hide();
                            }catch (Exception e){
                                e.printStackTrace();
                                new MensajeErrorConexion(context).showError("Exception: " + e.getMessage());
                            }
                        }

                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                            sweetAlertDialog.dismissWithAnimation();
                            // new MensajeErrorConexion(ctx).showError();

                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error Conexión")
                                    .setContentText("Error al conectar a: " + RestClient.BASE_URL + "\nFavor de acercarse al receptor")
                                    .setConfirmText("¡Ok!")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            Intent intent = new Intent(context, MainActivity.class);
                                            context.startActivity(intent);
                                        }
                                    }).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            sweetAlertDialog.dismissWithAnimation();
                            // new MensajeErrorConexion(ctx).showError();

                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error Conexión")
                                    .setContentText("Error al conectar a: " + RestClient.BASE_URL)
                                    .setConfirmText("¡Ok!")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            Intent intent = new Intent(context, MainActivity.class);
                                            context.startActivity(intent);
                                        }
                                    }).show();
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            sweetAlertDialog.dismissWithAnimation();
                            //  new MensajeErrorConexion(ctx).showError();

                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error Conexión")
                                    .setContentText("Error al conectar a: " + RestClient.BASE_URL)
                                    .setConfirmText("¡Ok!")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            Intent intent = new Intent(context, MainActivity.class);
                                            context.startActivity(intent);
                                        }
                                    }).show();
                        }
                    }
            );


        }catch (Exception ex) {
            sweetAlertDialog.hide();
            new MensajeErrorConexion(context).showError("Error de Conexión: " + ex.getMessage());
        }

    }

    @Override
    public void sincronizaInfoOrdenesVentaPipa(String idsOrdenesVenta) {
        startProgressBar();
        RestClient restClient =  new RestClient();
        progressUpdateDialog.setTitleText("Descargando Informacion de OV");

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept","application/json"));
        try {

            restClient.get(context,"api/getInfoOvByLlenado?idOrdenes=" + idsOrdenesVenta,
                    headers.toArray(new Header[headers.size()]),
                    null,
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            progressUpdateDialog.hide();
                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("ERROR!")
                                    .setContentText("Fallo conexión con el servidor\n" + "Info. OV Para Lote de llenado")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                        }
                                    }).show();
                        }
                    });

        }catch (Exception e){
           e.printStackTrace();
        }
    }

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando");
        progressUpdateDialog.setContentText("");
        progressUpdateDialog.setCancelable(false);
        progressUpdateDialog.show();
    }
}
