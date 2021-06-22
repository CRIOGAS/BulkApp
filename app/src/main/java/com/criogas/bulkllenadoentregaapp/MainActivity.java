package com.criogas.bulkllenadoentregaapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.criogas.bulkllenadoentregaapp.rest.ResConversionProducto;
import com.criogas.bulkllenadoentregaapp.rest.ResConversionProductoImp;
import com.criogas.bulkllenadoentregaapp.rest.RestSalesOrder;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    public SweetAlertDialog progressUpdateDialog;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.initBaseActivity();

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if(!addressRestServer.equals("")) {
                if (position == 0) {
                    //if (!daoRemision.isRemisionProcesada()) {
                    Intent intent = new Intent(MainActivity.this, SeleccionaOrdenVentaActivity.class);
                    startActivity(intent);
                        /*} else {
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("RUTA EN PROCESO")
                                    .setContentText("Cerrar ruta en proceso")
                                    .setConfirmText("OK").show();
                        }*/
                } else if (position == 1) {
                    Intent intent = new Intent(MainActivity.this, EntregaOvClienteActivity.class);
                    //intent.putExtra(Remision.PROPIETARIO_FIELD, Remision.REMISION_CRIOGAS);
                    startActivity(intent);
                    finish();
                } else if (position == 2) {
                    //startProgressBar();
                    ResConversionProducto resConversionProducto = new ResConversionProductoImp(MainActivity.this, progressUpdateDialog);
                    resConversionProducto.sincronizaConversionProducto();

                } else if (position == 3) {

                    Intent intent = new Intent(MainActivity.this, TomaFotoTicket.class);
                    startActivity(intent);
                }
                /*} else {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Configurar Servidor")
                            .setContentText("¡Favor de configurar servidor!")
                            .setConfirmText("¡Ok!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            }).show();
                }*/
            }
        };

        ListView listView = (ListView)findViewById(R.id.listViewMainMenu);
        listView.setOnItemClickListener(itemClickListener);

        //new GetInfoFromRest().execute();

        super.onCreate(savedInstanceState);
    }

    /**
     *
     * Ejemplo de llamada del REST por AsyncTask
     **/
    private class GetInfoFromRest extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            RestSalesOrder restSalesOrder = new RestSalesOrder();
            OrdenVenta ov = restSalesOrder.GetByID(1484);
            String dato = ov.getCliente() + " - " + ov.getProducto() + " - " + ov.getDesccorta() + " - " + ov.getRevision() + " - " + ov.getPipa() + " - " + ov.getQty() + " - " + ov.getUdm();
            return dato;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando Datos");
        progressUpdateDialog.setContentText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.btnConfigMenu:
                Intent intent = new Intent(MainActivity.this, ConfigServer.class);
                startActivity(intent);
                return true;
            case R.id.btnInfoWifi:
                //getWiffiInfo();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}
