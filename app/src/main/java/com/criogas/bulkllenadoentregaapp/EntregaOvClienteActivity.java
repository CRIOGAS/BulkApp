package com.criogas.bulkllenadoentregaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.criogas.bulkllenadoentregaapp.rest.RestApiPipas;
import com.criogas.bulkllenadoentregaapp.rest.RestSalesOrder;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class EntregaOvClienteActivity extends AppCompatActivity {

    public SweetAlertDialog progressUpdateDialog;
    public EditText editTextOVSeleccionada;
    public EditText editTextInfoOvSeleccionada;
    public OrdenVenta infoOvIngresada;
    int ovIngresada = 0;
    public Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrega_ov_cliente);
        context=EntregaOvClienteActivity.this;
        editTextOVSeleccionada = (EditText) findViewById(R.id.editTextOVIngresa);
        editTextInfoOvSeleccionada = (EditText) findViewById(R.id.editTextInfoOvSeleccionada);

        editTextOVSeleccionada.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && !editTextOVSeleccionada.getText().toString().equals("")) {
                    getOV();
                    //handled = true;
                }else{
                    new SweetAlertDialog(EntregaOvClienteActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("¡¡OV NO VÁLIDA!!")
                            .setContentText("¡Favor de ingresar una OV válida y dar ENTER!")
                            .setConfirmText("¡Ok!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    EntregaOvClienteActivity.this.editTextOVSeleccionada.setText("");
                                    sDialog.dismissWithAnimation();
                                }
                            }).show();
                }
                return handled;
            }
        });


    }

    public void getOV(){
        ovIngresada = Integer.parseInt(editTextOVSeleccionada.getText().toString());
        new GetInfoFromRest().execute();
    }

    private class GetInfoFromRest extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            startProgressBar();
        }

        protected String doInBackground(Void... params) {
            //RestApiPipas apiPipas = new RestApiPipas();
            String dato = "";
            RestSalesOrder restSalesOrder = new RestSalesOrder();
            infoOvIngresada = restSalesOrder.GetByID(ovIngresada);
            if(infoOvIngresada!=null) {
                dato = "Orden Venta : " + ovIngresada + "\n" +
                        "Cliente    : " + infoOvIngresada.getNombre_c() + "\n" +
                        "Cve. Cliente : " + infoOvIngresada.getCliente() + "\n" +
                        "Producto   : " + infoOvIngresada.getDesccorta() + "\n" +
                        "Cve. Prod. :" + infoOvIngresada.getProducto() + "\n" +
                        "Revisión   : " + infoOvIngresada.getRevision() + "\n" +
                        "Pipa       : " + infoOvIngresada.getPipa() + "\n" +
                        "Cantidad   : " + infoOvIngresada.getQty() + "\n" +
                        "Unidad de Medida : " + infoOvIngresada.getUdm();
            }else{
                        dato="¡OV NO VALIDA!";
            }
            return dato;
        }
        protected void onPostExecute(String result) {
            editTextOVSeleccionada.setText("");
            editTextInfoOvSeleccionada.setText("");
            editTextInfoOvSeleccionada.setText(result);
            progressUpdateDialog.hide();
            //Toast.makeText(EntregaOvClienteActivity.this, result, Toast.LENGTH_LONG).show();
        }



        public void startProgressBar() {
            progressUpdateDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressUpdateDialog.setTitleText("Sincronizando información");
            progressUpdateDialog.setCancelable(false);
            progressUpdateDialog.show();
        }
    }

    private class exiteEmpaqueOV extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            startProgressBar();
        }

        protected Boolean doInBackground(Void... params) {
            RestSalesOrder restSalesOrder = new RestSalesOrder();
            return restSalesOrder.existeEmpaque(ovIngresada);
        }
        protected void onPostExecute(Boolean result) {
            /*Intent intent = new Intent(EntregaOvClienteActivity.this, EntregaCliente.class);
            intent.putExtra("infoOvIngresada", infoOvIngresada);
            startActivity(intent);*/
            if(!result) {
                Intent intent = new Intent(EntregaOvClienteActivity.this, EntregaCliente.class);
                intent.putExtra("infoOvIngresada", infoOvIngresada);
                startActivity(intent);
            }else{
                new SweetAlertDialog(EntregaOvClienteActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("¡Orden de venta procesada!")
                        .setContentText("No puedes procesar una OV que ya tiene empaque")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                EntregaOvClienteActivity.this.editTextOVSeleccionada.setText("");
                                sDialog.dismissWithAnimation();
                                progressUpdateDialog.hide();
                            }
                        }).show();
            }
        }
        public void startProgressBar() {
            progressUpdateDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressUpdateDialog.setTitleText("Sincronizando OV seleccionada");
            progressUpdateDialog.setCancelable(false);
            progressUpdateDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.btnNextMenu:
                new exiteEmpaqueOV().execute();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent1 = new Intent(EntregaOvClienteActivity.this,MainActivity.class);
        startActivity(intent1);
    }


}
