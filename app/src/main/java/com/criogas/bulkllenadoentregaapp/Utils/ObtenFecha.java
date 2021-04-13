package com.criogas.bulkllenadoentregaapp.Utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.criogas.bulkllenadoentregaapp.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ObtenFecha implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    EditText _editText;
    private int dia;
    private int mes;
    private int ano;
    private Context context;

    public ObtenFecha(Context context, EditText editTextViewID) {
        Activity activity = (Activity)context;
        this._editText = (EditText)activity.findViewById(R.id.editfechaLlenado);
        this._editText.setOnClickListener(this);
        this.context = context;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        ano = year;
        mes = monthOfYear + 1;
        dia = dayOfMonth;
        updateDisplay();
    }

    @Override
    public void onClick(View view) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        System.out.print("####################################" + calendar.getTime() + "############################");

        DatePickerDialog datePicker = new DatePickerDialog(context, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePicker.show();
    }

    private void updateDisplay(){
        _editText.setText(new StringBuilder().append(twoDigits(dia)).append("/").append(twoDigits(mes)).append("/").append(ano));
    }

    private String twoDigits(int n){
        return (n<9)? ("0"+n) : String.valueOf(n);
    }
}
