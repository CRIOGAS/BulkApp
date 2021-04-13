package com.criogas.bulkllenadoentregaapp.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.criogas.bulkllenadoentregaapp.R;
import com.criogas.bulkllenadoentregaapp.model.Pipas;

import java.util.ArrayList;
import java.util.List;

public class PipasAdapter extends ArrayAdapter<Pipas> {
    private ArrayList<Pipas> item;
    private Context context;
    Spinner spinnerPipas;

    public PipasAdapter(@NonNull Context context, int resource, ArrayList<Pipas> item) {
        super(context, resource, item);
        this.item = item;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v==null){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.spinner_item, null);
        }

        Pipas ov = item.get(position);

        if(ov != null){
            spinnerPipas = (Spinner) v.findViewById(R.id.spinnerPipas);

            //spinnerPipas.se(ov.getPromotor());
        }

        /*checkOrdenVenta.setTag(position);

        checkOrdenVenta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int getPosistion = (Integer)compoundButton.getTag();
                items.get(getPosistion).setSeleccionado(compoundButton.isChecked());
            }
        });

        checkOrdenVenta.setChecked(items.get(position).isSeleccionado());*/

        return v;
    }
}
