package com.criogas.bulkllenadoentregaapp.Utils;

import android.content.Context;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Sistemas on 06/04/2017.
 */

public class MensajeErrorConexion {
    private Context ctx;

    public MensajeErrorConexion(Context ctx) {
        this.ctx = ctx;
    }

    public void showError(String message) {
        new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("ERROR!")
                .setContentText(message)
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    public void showConectionError() {
        new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("ERROR!")
                .setContentText("Fallo conexión con el servidor")
                .setConfirmText("OK")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }
}
