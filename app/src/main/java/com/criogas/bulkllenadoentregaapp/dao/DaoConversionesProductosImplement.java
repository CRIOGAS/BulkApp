package com.criogas.bulkllenadoentregaapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.criogas.bulkllenadoentregaapp.model.ConversionProducto;

import java.util.ArrayList;

public class DaoConversionesProductosImplement implements DaoConversionesProductos {

    private SQLiteDatabase db;
    private SQLiteOpenHelper dbHelper;
    private Cursor cursor;

    public DaoConversionesProductosImplement(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public void saveConversionProducto(ArrayList<ConversionProducto> lstConvProductos) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        for(ConversionProducto cp : lstConvProductos){
            values.put("descorta_prod",cp.desCorta);
            values.put("LB",cp.lb);
            values.put("KG",cp.kg);
            values.put("L",cp.lt);
            values.put("GAL",cp.gal);
            values.put("FT3",cp.pie3);
            values.put("M3",cp.mt3);
            values.put("seq",cp.seq);
            db.insert("Conv_Producto",null,values);
        }
        db.close();
    }

    @Override
    public String getUnidadConverionPeso(String converion, String um) {
        String res="1";
        db = dbHelper.getReadableDatabase();

        String query = ("select " + um + " from Conv_Producto where '" + converion + "' like '%' || descorta_prod || '%' and KG = 1.0");
        cursor = db.rawQuery(query,new String[] {});

        while (cursor.moveToNext()){
            res = cursor.getString(cursor.getColumnIndex(um));
        }

        cursor.close();
        db.close();
        return res;

    }

    @Override
    public String getUnidadConverionVolumen(String converion, String um) {
        String res="1";
        db = dbHelper.getReadableDatabase();

        String query = ("select " + um + " from Conv_Producto where '" + converion + "' like '%' || descorta_prod || '%' and L = 1.0");
        cursor = db.rawQuery(query,new String[] {});

        while (cursor.moveToNext()){
            res = cursor.getString(cursor.getColumnIndex(um));
        }

        cursor.close();
        db.close();
        return res;

    }

    @Override
    public ArrayList<ConversionProducto> getUnidadConverionAll(String converion) {
        ArrayList<ConversionProducto> lstConvProd = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        String query = ("select * from Conv_Producto where '" + converion + "' like '%' || descorta_prod || '%'");
        //String query = ("select kg from Conv_Producto where lt = 1 ");
        cursor = db.rawQuery(query,new String[] {});

        while (cursor.moveToNext()){
            lstConvProd.add(new ConversionProducto(
                    cursor.getString(cursor.getColumnIndex("descorta_prod")),
                    cursor.getString(cursor.getColumnIndex("LB")),
                    cursor.getString(cursor.getColumnIndex("KG")),
                    cursor.getString(cursor.getColumnIndex("L")),
                    cursor.getString(cursor.getColumnIndex("GAL")),
                    cursor.getString(cursor.getColumnIndex("FT3")),
                    cursor.getString(cursor.getColumnIndex("M3")),
                    cursor.getString(cursor.getColumnIndex("seq"))
            ));
        }

        cursor.close();
        db.close();
        return lstConvProd;

    }

    @Override
    public void eliminaConversionProductos() {
        db = dbHelper.getWritableDatabase();
        db.execSQL("delete from Conv_Producto");
        db.close();
    }
}
