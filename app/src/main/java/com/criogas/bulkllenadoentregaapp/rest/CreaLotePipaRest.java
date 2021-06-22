package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.MainActivity;
import com.criogas.bulkllenadoentregaapp.model.ConfigServer;
import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CreaLotePipaRest implements ICreaLotePipaRest {
    Context context;
    SweetAlertDialog progressUpdateDialog;
    public String foliosLlenado = "";

    public CreaLotePipaRest(Context context, SweetAlertDialog progressUpdateDialog) {
        this.context = context;
        this.progressUpdateDialog = progressUpdateDialog;
    }

    @Override
    public String creaInfoLLenadoPipa(ArrayList<OrdenVenta> listaOVSeleccionadas, String fechaLlenado, String producto, String cveLlenador, String turno, String up, String tanque, String presion, String pesoNeto, String pipa, String observaciones, String mermaLlenado) {
        progressUpdateDialog.setTitleText("Creando Lote");
        progressUpdateDialog.setContentText("espere...");
        //progressUpdateDialog.show();
        try {
            JSONObject jsonObjectGeneral = new JSONObject();

            JSONObject jsonObjecOrdenesVenta = new JSONObject();
            JSONArray jsonArrayOrndesVenta = new JSONArray();
            for (int i = 0; i < listaOVSeleccionadas.size(); i++) {
                jsonObjecOrdenesVenta = new JSONObject();
                jsonObjecOrdenesVenta.put("folio", listaOVSeleccionadas.get(i).getFolio());
                jsonObjecOrdenesVenta.put("producto", listaOVSeleccionadas.get(i).getProducto());
                jsonObjecOrdenesVenta.put("sucursal", ConfigServer.SUCURSAL);
                jsonArrayOrndesVenta.put(jsonObjecOrdenesVenta);
            }
            jsonObjectGeneral.put("orden_venta_pipa", jsonArrayOrndesVenta);

            JSONObject jsonObjectInfoLlenado = new JSONObject();
            JSONArray jsonArrayInfoLlenado = new JSONArray();
            jsonObjectInfoLlenado.put("cvellenador", cveLlenador);
            jsonObjectInfoLlenado.put("fhllenado",fechaLlenado);
            jsonObjectInfoLlenado.put("usuario","SYS_HH");
            jsonObjectInfoLlenado.put("cveproducto",producto);
            jsonObjectInfoLlenado.put("turno",turno);
            jsonObjectInfoLlenado.put("planta",up);
            jsonObjectInfoLlenado.put("tanque",tanque);
            jsonObjectInfoLlenado.put("observaciones",observaciones);
            jsonObjectInfoLlenado.put("merma_llenado",mermaLlenado);
            jsonObjectInfoLlenado.put("sitio", ConfigServer.SUCURSAL);
            jsonArrayInfoLlenado.put(jsonObjectInfoLlenado);
            jsonObjectGeneral.put("infollenado_pipas",jsonArrayInfoLlenado);

            JSONObject jsonObjectDetInfoLlenado = new JSONObject();
            JSONArray jsonArrayDetInfoLLenado = new JSONArray();
            jsonObjectDetInfoLlenado.put("foliocamion","");
            jsonObjectDetInfoLlenado.put("observaciones","");
            jsonObjectDetInfoLlenado.put("numpipa",pipa);
            jsonObjectDetInfoLlenado.put("presion",presion);
            jsonObjectDetInfoLlenado.put("unidadmed","L");
            jsonObjectDetInfoLlenado.put("valor",pesoNeto);
            jsonObjectDetInfoLlenado.put("sucursal",ConfigServer.SUCURSAL);
            jsonObjectDetInfoLlenado.put("cveproducto",producto);
            jsonArrayDetInfoLLenado.put(jsonObjectDetInfoLlenado);
            jsonObjectGeneral.put("det_infollenado_pipas",jsonArrayDetInfoLLenado);

            StringEntity entity = new StringEntity(jsonObjectGeneral.toString(),"UTF8");

            RestClient.post(context, "api/crealotellenado_pipas", entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String response = new String(responseBody,"UTF8");
                        foliosLlenado = response;
                        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText("¡Respuesta del Servidor!")
                                .setContentText("Folio de llenado : " + foliosLlenado + " ")
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        progressUpdateDialog.hide();
                                        sDialog.dismissWithAnimation();
                                        Intent intent = new Intent(context, MainActivity.class);
                                        context.startActivity(intent);
                                    }
                                }).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(context,
                                "ERROR AL CREAR LOTE DE LLENADO = " + e.getMessage(), Toast.LENGTH_SHORT);
                        toast.show();
                        progressUpdateDialog.hide();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progressUpdateDialog.dismissWithAnimation();
                    // new MensajeErrorConexion(ctx).showError();

                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error Conexión")
                            .setContentText("Error " + error + "al conectar a: " + RestClient.BASE_URL)
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
                    progressUpdateDialog.dismissWithAnimation();
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
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return foliosLlenado;
    }
}
