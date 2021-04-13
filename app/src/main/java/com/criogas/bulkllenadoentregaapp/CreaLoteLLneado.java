package com.criogas.bulkllenadoentregaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreaLoteLLneado extends AppCompatActivity {
    Context ctx;
    private SweetAlertDialog progressUpdateDialog;
    public String idOrenes;
    public String producto;
    public String descProducto;
    public String cvePipa;
    public static Double pesoNeto = 0.0;

    public Spinner spinnerOperador, spinnerTanque, spinnerUnidadProductora, spinnerTurnos = null;
    public static EditText editTextOVSeleccionadas, editTextFecha, editTextPresion, editTextPesoBruto, editTextPesoTara, editTextPesoNeto = null;
    public Button buttonGeneraLote = null;

    public static List<Operador> listaOperadoresLLenadores = new ArrayList<>();
    public static List<String> listaNombreEmpleado = new ArrayList<>();
    public static List<Cat_Rev_Tanques> listaUnidaProductora_Tanques = new ArrayList<>();
    public static List<String> listaUnidadProductora = new ArrayList<>();
    public static List<String> listaTanques = new ArrayList<>();
    public static ArrayList<OrdenVenta> listaOVSeleccionada = new ArrayList<>();


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
        editTextPresion = (EditText) findViewById(R.id.textEditPresion);
        editTextPesoBruto = (EditText) findViewById(R.id.textEditPesoBruto);
        editTextPesoNeto = (EditText) findViewById(R.id.textEditPesoNeto);
        editTextPesoTara = (EditText) findViewById(R.id.textEditPesoTara);
        buttonGeneraLote = (Button) findViewById(R.id.btnGeneraLoteLlenado);

        editTextOVSeleccionadas.setText(idOrenes);
        editTextPesoNeto.setText("*.*");

        sincronizaINFO();
        new ObtenFecha(CreaLoteLLneado.this, editTextFecha);

        spinnerUnidadProductora.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if (item != null) {
                    String up = listaUnidadProductora.get(i);
                    Toast toast = Toast.makeText(CreaLoteLLneado.this,"UP : " + up, Toast.LENGTH_SHORT);
                    toast.show();
                    fillTanque(up);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        editTextPesoTara.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    startProgressBar();
                    new RestInfoLenadoPipaImplement(CreaLoteLLneado.this, progressUpdateDialog)
                            .getPesoNetoCalculado(producto,editTextPesoBruto.getText().toString(),
                                    editTextPesoTara.getText().toString(),
                                    "KG-LT");
                    //editTextPesoNeto.setText(pesoNeto.toString());
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

        startProgressBar();
        String ok="";
        //String fechaEnvioEpicor = "0000/00/00";
        String [] cveEmpleado = spinnerOperador.getSelectedItem().toString().split(" ");
        
        Float cantidadPorOV = Float.parseFloat(editTextPesoNeto.getText().toString())/listaOVSeleccionada.size();

        /*try {
            fechaEnvioEpicor = (new SimpleDateFormat("YYYY/MM/DD").parse(editTextFecha.getText().toString()).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/


        ICreaLotePipaRest iCreaLotePipaRest = new CreaLotePipaRest(CreaLoteLLneado.this, progressUpdateDialog);
        iCreaLotePipaRest.creaInfoLLenadoPipa(  listaOVSeleccionada,
                                                editTextFecha.getText().toString(),
                                                producto,
                                                cveEmpleado[0],
                                                spinnerTurnos.getSelectedItem().toString(),
                                                spinnerUnidadProductora.getSelectedItem().toString(),
                                                spinnerTanque.getSelectedItem().toString(),
                                                editTextPresion.getText().toString(),
                                                cantidadPorOV.toString(),
                                                cvePipa
                                                );


        /*ok=restInvTransfer.invTransfer(listaOVSeleccionada.get(1).getGas_dp(),
                                    Double.parseDouble(editTextPesoNeto.getText().toString()),
                fechaEnvioEpicor, spinnerTanque.getSelectedItem().toString(),"BAHIAORI","PIPA AUX","LLENADO PIPA");
    System.out.print(ok);*/
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

    private void startProgressBar() {
        progressUpdateDialog = new SweetAlertDialog(CreaLoteLLneado.this, SweetAlertDialog.PROGRESS_TYPE);
        progressUpdateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressUpdateDialog.setTitleText("Sincronizando");
        progressUpdateDialog.setCancelable(false);
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
                                sincronizaInfoLLenado();
                            }
                        }).show();
                return true;
            default:
                return false;
        }
    }
}
