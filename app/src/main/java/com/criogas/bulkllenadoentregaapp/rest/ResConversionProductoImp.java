package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.MainActivity;
import com.criogas.bulkllenadoentregaapp.dao.DaoConversionesProductos;
import com.criogas.bulkllenadoentregaapp.dao.DaoConversionesProductosImplement;
import com.criogas.bulkllenadoentregaapp.dao.HandheldDatabaseHelper;
import com.criogas.bulkllenadoentregaapp.model.ConversionProducto;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;


public class ResConversionProductoImp implements ResConversionProducto {
    private SweetAlertDialog progressUpdateDialog;
    private Context ctx;
    private SQLiteOpenHelper dbHelper = null;
    private DaoConversionesProductos daoConversionesProductos;
    private RestClient RestClient = new RestClient();
    private ArrayList<ConversionProducto> lstConversionProductos = new ArrayList<>();

    public ResConversionProductoImp(Context ctx, SweetAlertDialog progressUpdateDialog) {
        this.ctx = ctx;
        this.progressUpdateDialog = progressUpdateDialog;
        daoConversionesProductos = new DaoConversionesProductosImplement(new HandheldDatabaseHelper(ctx));
        this.dbHelper = dbHelper = new HandheldDatabaseHelper(ctx);
    }

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando Datos");
        progressUpdateDialog.setContentText("");
        progressUpdateDialog.show();
    }

    private void showConectionError(String texto) {
        progressUpdateDialog.hide();
        new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("ERROR!")
                .setContentText("Fallo conexión con el servidor\n" + texto)
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    @Override
    public void sincronizaConversionProducto() {
        startProgressBar();
        RestClient restClient = new RestClient();

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "application/json"));

        try {
            restClient.get(
                    ctx,
                    "api/conversiones_productos",
                    headers.toArray(new Header[headers.size()]),
                    null,
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    lstConversionProductos.add(new ConversionProducto(response.getJSONObject(i)));
                                    System.out.println(new ConversionProducto(response.getJSONObject(i)));
                                }
                                new SaveConversionProductoInDbTask().execute();

                            }catch (Exception ex){
                                System.out.println(ex);
                                Toast toast = Toast.makeText(ctx,
                                        "ERROR ON SUCCESS: " + ex.toString(), Toast.LENGTH_SHORT);
                                toast.show();
                                progressUpdateDialog.hide();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            showConectionError("Error al sincronizar unidad de conversión");
                        }

                        @Override
                        public void onRetry(int retryNo) {
                            showConectionError("Error al sincronizar unidad de conversión");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class SaveConversionProductoInDbTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressUpdateDialog.setTitleText("Guardando Información");
        }

        @Override
        protected String doInBackground(Void... voids) {

            daoConversionesProductos.eliminaConversionProductos();
            daoConversionesProductos.saveConversionProducto(lstConversionProductos);

            return "";
        }

        @Override
        protected void onPostExecute(String success) {
            progressUpdateDialog.hide();

            new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("¡Exito!")
                    .setContentText("La sincronización finalizó exitosamente")
                    .setConfirmText("OK")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(ctx, MainActivity.class);
                            ctx.startActivity(intent);
                        }
                    }).show();
        }
    }
}
