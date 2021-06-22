package com.criogas.bulkllenadoentregaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.criogas.bulkllenadoentregaapp.Utils.ObtenFecha;
import com.criogas.bulkllenadoentregaapp.model.Cat_Rev_Tanques;
import com.criogas.bulkllenadoentregaapp.model.Operador;
import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.criogas.bulkllenadoentregaapp.rest.CreaLotePipaRest;
import com.criogas.bulkllenadoentregaapp.rest.ICreaLotePipaRest;
import com.criogas.bulkllenadoentregaapp.rest.RestInfoLenadoPipaImplement;
import com.criogas.bulkllenadoentregaapp.rest.RestInvTransfer;
import com.criogas.bulkllenadoentregaapp.rest.RestSalesOrder;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class CreaLoteLLneado extends AppCompatActivity {

    private SweetAlertDialog progressUpdateDialog;
    public String idOrenes;
    public String producto;
    public String descProducto;
    public String cvePipa;
    public static Double pesoNeto = 0.0;

    public Spinner spinnerOperador, spinnerTanque, spinnerUnidadProductora, spinnerTurnos = null;
    public static EditText editTextOVSeleccionadas, editTextFecha, editTextPesoBruto, editTextPesoTara,
                           editTextMermaLlenado, editTextObservaciones,
                           editTextPesoNeto, editTextVolumenNeto = null;
    public Button buttonGeneraLote = null;

    public static List<Operador> listaOperadoresLLenadores = new ArrayList<>();
    public static List<String> listaNombreEmpleado = new ArrayList<>();
    public static List<Cat_Rev_Tanques> listaUnidaProductora_Tanques = new ArrayList<>();
    public static List<String> listaUnidadProductora = new ArrayList<>();
    public static List<String> listaTanques = new ArrayList<>();
    public static ArrayList<OrdenVenta> listaOVSeleccionada = new ArrayList<>();

    String fechaEnvioEpicor = null;
    String tanque = "";

    public String up = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genera_lote_llenado);

        idOrenes = getIntent().getStringExtra("idOrdenes");
        producto = getIntent().getStringExtra("producto");
        listaOVSeleccionada = (ArrayList<OrdenVenta>) getIntent().getSerializableExtra("listaOrdenProductoSeleccionado");
        cvePipa = getIntent().getStringExtra("cvePipa");

        spinnerOperador = (Spinner) findViewById(R.id.spinnerLlenadores);
        spinnerTanque = (Spinner) findViewById(R.id.spinnerTanque);
        spinnerUnidadProductora = (Spinner) findViewById(R.id.spinnerUnidadProductora);
        spinnerTurnos = (Spinner) findViewById(R.id.spinnerTurno);

        editTextFecha = (EditText) findViewById(R.id.editfechaLlenado);
        editTextOVSeleccionadas = (EditText) findViewById(R.id.editTextOVSeleccionadas);
        editTextVolumenNeto = (EditText) findViewById(R.id.textEditVolumenNeto);
        editTextPesoBruto = (EditText) findViewById(R.id.textEditPesoBruto);
        editTextPesoNeto = (EditText) findViewById(R.id.textEditPesoNeto);
        editTextPesoTara = (EditText) findViewById(R.id.textEditPesoTara);
        editTextMermaLlenado = (EditText) findViewById(R.id.textEditMermaLlenado);
        editTextObservaciones = (EditText) findViewById(R.id.textEditObservaciones);

        editTextOVSeleccionadas.setText(idOrenes);
        editTextVolumenNeto.setText("*.*");
        editTextPesoNeto.setText("*.*");

        sincronizaINFO();
        new ObtenFecha(CreaLoteLLneado.this, editTextFecha);

        spinnerUnidadProductora.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null) {
                    up = listaUnidadProductora.get(i);
                    Toast toast = Toast.makeText(CreaLoteLLneado.this,"UP : " + up, Toast.LENGTH_SHORT);
                    toast.show();
                    fillTanque(up);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        editTextPesoBruto.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    startProgressBar();
                    new RestInfoLenadoPipaImplement(CreaLoteLLneado.this, progressUpdateDialog)
                            .getPesoNetoCalculado(producto,editTextPesoBruto.getText().toString(),
                                    editTextPesoTara.getText().toString(),
                                    "LT-KG");
                                    //"KG-LT");
                    Float res = Float.parseFloat(editTextPesoBruto.getText().toString())-
                            Float.parseFloat(editTextPesoTara.getText().toString());
                    editTextPesoNeto.setText(res + "");
                    handled = true;
                }
                return handled;
            }
        });
    }

    public void generaLoteOnClick(View view){
        new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Información llenado: ")
                .setContentText("")
                .setCancelText("Regresar")
                .setConfirmText("Continuar")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sincronizaInfoLLenado();
                        sweetAlertDialog.dismiss();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                }).show();
    }


    public void sincronizaInfoLLenado(){

        progressUpdateDialog.setTitleText("Obteniendo Datos Ingresados...");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = dateFormat.parse(editTextFecha.getText().toString());

            dateFormat.applyPattern("yyyy-MM-dd");
            fechaEnvioEpicor = dateFormat.format(date);
        }catch (Exception e){
            System.out.println(e);
        }

        pesoNeto = Double.parseDouble(editTextVolumenNeto.getText().toString());
        tanque = spinnerTanque.getSelectedItem().toString();

        new GetInfoFromRest().execute();
    }

    public void creaLoteLLenadoCilindros(){
        //MANDAR LOS LITROS
        progressUpdateDialog.setTitleText("Creando lote llenado pipa...");
        Float cantidadPorOV = Float.parseFloat(editTextVolumenNeto.getText().toString())/listaOVSeleccionada.size();
        String [] cveEmpleado = spinnerOperador.getSelectedItem().toString().split(" ");
        ICreaLotePipaRest iCreaLotePipaRest = new CreaLotePipaRest(CreaLoteLLneado.this, progressUpdateDialog);
        iCreaLotePipaRest.creaInfoLLenadoPipa(listaOVSeleccionada,
                editTextFecha.getText().toString(),
                producto,
                cveEmpleado[0],
                spinnerTurnos.getSelectedItem().toString(),
                spinnerUnidadProductora.getSelectedItem().toString(),
                spinnerTanque.getSelectedItem().toString(),
                "",
                cantidadPorOV.toString(),
                cvePipa,
                editTextObservaciones.getText().toString(),
                editTextMermaLlenado.getText().toString()
        );

    }


     private class GetInfoFromRest extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressUpdateDialog.setTitleText("Rest Transfer Inv...");
        }

        protected String doInBackground(Void... params) {

            //progressUpdateDialog.setTitleText("Espere por favor...");

            RestInvTransfer transfer = new RestInvTransfer();
            return transfer.invTransfer(listaOVSeleccionada.get(0).getGas_dp(), pesoNeto, fechaEnvioEpicor,
                    tanque, "BAHIAORI","PIPA AUX","LLENADO PIPA");

        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(String result) {
            if(result.contains("ERROR")){
                new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("¡ERROR AL HACER TRANSFERENCIA DE LLENADO!")
                        .setContentText("" + result + "")
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                progressUpdateDialog.hide();
                                sDialog.dismissWithAnimation();
                            }
                        }).show();
            }else{
                //new UpdateRevision().execute();
                new GetTransferMerma().execute();
            }
            Toast.makeText(CreaLoteLLneado.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private class GetTransferMerma extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressUpdateDialog.setTitleText("Transfer merma...");
        }

        @SuppressLint("WrongThread")
        protected String doInBackground(Void... params) {

            //progressUpdateDialog.setTitleText("Espere por favor...");

            RestInvTransfer transfer = new RestInvTransfer();
            return transfer.invTransfer(listaOVSeleccionada.get(0).getGas_dp(), Double.parseDouble(editTextMermaLlenado.getText().toString()), fechaEnvioEpicor,
                    tanque, "BAHIAORI","PIPA AUX","MERMA LLENADO");

        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(String result) {
            if(result.contains("ERROR")){
                new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("¡ERROR AL ENVIAR MERMA!")
                        .setContentText("" + result + "")
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                progressUpdateDialog.hide();
                                sDialog.dismissWithAnimation();
                            }
                        }).show();
            }else{
                new UpdateRevision().execute();
            }
            Toast.makeText(CreaLoteLLneado.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private class UpdateRevision extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressUpdateDialog.setTitle("Actualizando Revisión...");
        }

        protected String doInBackground(Void... params) {

            String r="";
            RestSalesOrder restSalesOrder = new RestSalesOrder();

            for(OrdenVenta ov: listaOVSeleccionada){
                r=restSalesOrder.actualizaRevision(ov.getFolio(),up);
                if(r.contains("ERROR")){
                    return r;
                }
            }

            return  r;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(String result) {
            //showError(result);
            if(result.contains("ERROR")){
                new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("¡ERROR AL ACTUALIZAR REVISION!")
                        .setContentText("" + result + "")
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                progressUpdateDialog.hide();
                            }
                        }).show();
            }else{
                creaLoteLLenadoCilindros();
            }
            Toast.makeText(CreaLoteLLneado.this, result, Toast.LENGTH_LONG).show();
        }
    }

    public void fillTanque(String up){
            List<String> tanques =new ArrayList<>();

        for(int u = 0; u < listaUnidaProductora_Tanques.size();u++) {
            if(listaUnidaProductora_Tanques.get(u).getUnida_productora().equals(up)){
                tanques.add(listaUnidaProductora_Tanques.get(u).getTanque());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(),R.layout.spinner_item,tanques
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerTanque.setAdapter(adapter);
    }


    public void sincronizaINFO(){
        startProgressBar();
        new RestInfoLenadoPipaImplement(CreaLoteLLneado.this, progressUpdateDialog).fillListaEmpleadosLLenadores(spinnerOperador);
        new RestInfoLenadoPipaImplement(CreaLoteLLneado.this, progressUpdateDialog).fillTanqueUnidadProductora(producto, spinnerUnidadProductora);
    }

    public void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando");
        progressUpdateDialog.setCancelable(false);
    }

    private void showError(String error) {
        progressUpdateDialog.hide();
        new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("¡ERROR!")
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
    public void onBackPressed() {
        Intent intent = new Intent(CreaLoteLLneado.this,SeleccionaOrdenVentaActivity.class);
        startActivity(intent);
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
                        .setTitleText("Generar Lote Pipa")
                        .setContentText("¿Está seguro que desea continuar?")
                        .setConfirmText("Si. Continuar.")
                        .setCancelText("Cancelar")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                                startProgressBar();
                                progressUpdateDialog.show();
                                //new GetInfoFromRest().execute();
                                sincronizaInfoLLenado();
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }
}
