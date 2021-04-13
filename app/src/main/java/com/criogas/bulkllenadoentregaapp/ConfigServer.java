package com.criogas.bulkllenadoentregaapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class ConfigServer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_server);
    }

    public void saveServerConfig(View v) {
        /*daoConfigServer.saveServerAddress(
                txtIpServidor.getText().toString(),
                txtPortServidor.getText().toString(),
                txtSucursal.getText().toString()
        );*/

        new SweetAlertDialog(ConfigServer.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Datos Guardados Exitosamente")
                .setContentText("¡Dirección Server Registrada!")
                .setConfirmText("¡Ok!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        //RestClient.BASE_URL = "http://" + txtIpServidor.getText().toString() + ":" + txtPortServidor.getText().toString() + "/";
                        Intent intent = new Intent(ConfigServer.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }
}
