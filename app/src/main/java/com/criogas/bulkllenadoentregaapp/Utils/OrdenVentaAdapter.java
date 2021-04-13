package com.criogas.bulkllenadoentregaapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.criogas.bulkllenadoentregaapp.R;
import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrdenVentaAdapter extends ArrayAdapter<OrdenVenta> {
    private ArrayList<OrdenVenta> items;
    private Context currentContext;

    TextView txtOrdenVentaAndDate;
    TextView txtOrdenVentaPromotor;
    TextView txtOrdenVentaProducto;
    TextView txtOrdenVentaCliente;
    CheckBox checkOrdenVenta;

    public OrdenVentaAdapter(@NonNull Context context, int textViewResourceId, ArrayList<OrdenVenta> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        currentContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        View v = convertView;

        if (v==null){
            LayoutInflater vi = (LayoutInflater)currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row_ordenes_venta, null);
        }

        OrdenVenta ov = items.get(position);

        if(ov != null){
            txtOrdenVentaAndDate = (TextView)v.findViewById(R.id.txtOrdenVentaAndDate);
            txtOrdenVentaPromotor = (TextView)v.findViewById(R.id.txtOrdenVentaChofer);
            txtOrdenVentaProducto = (TextView)v.findViewById(R.id.txtOrdenVentaProducto);
            txtOrdenVentaCliente = (TextView)v.findViewById(R.id.txtOrdenVentaCliente);
            checkOrdenVenta = (CheckBox)v.findViewById(R.id.chkOrdenVenta);

            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

            txtOrdenVentaAndDate.setText(ov.getFolio() + " - " + dt.format(ov.getCreado()));
            txtOrdenVentaCliente.setText(ov.getNombre_c());
            txtOrdenVentaPromotor.setText(ov.getPromotor());
            txtOrdenVentaProducto.setText(ov.getDesccorta());
        }

        checkOrdenVenta.setTag(position);

        checkOrdenVenta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int getPosistion = (Integer)compoundButton.getTag();
                items.get(getPosistion).setSeleccionado(compoundButton.isChecked());
            }
        });

        checkOrdenVenta.setChecked(items.get(position).isSeleccionado());

        return v;
    }
}
