package com.criogas.bulkllenadoentregaapp.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.criogas.bulkllenadoentregaapp.model.ConfigServer;

public class HandheldDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_bulkApp";
    private static final int DB_VERSION = 2;

    public HandheldDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase
                                     db) {
        System.out.println("CREANDO BASE DE DATOS");
        this.createDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        System.out.println("ACTUALIZANDO BASE DE DATOS");
        this.createDatabase(db);
    }

    private void createDatabase(SQLiteDatabase db) {
        try {
            System.out.println("CREANDO BASE DE TABLAS");

            db.execSQL("DROP TABLE IF EXISTS '" + ConfigServer.TABLE_CONFIG_SERVER + "'");
            db.execSQL("CREATE TABLE '" + ConfigServer.TABLE_CONFIG_SERVER + "' (" +
                    "'" + ConfigServer.IP_SERVER + "' TEXT," +
                    "'" + ConfigServer.PORT_SERVER + "' TEXT," +
                    "'" + ConfigServer.COL_SUCURSAL + "' TEXT" +
                    ");"
            );

            db.execSQL("DROP TABLE IF EXISTS 'Conv_Producto'");
            db.execSQL("CREATE TABLE 'Conv_Producto' (" +
                    "'descorta_prod' TEXT," +
                    "'KG' TEXT," +
                    "'L' TEXT," +
                    "'GAL' TEXT," +
                    "'FT3' TEXT," +
                    "'M3' TEXT," +
                    "'LB' TEXT," +
                    "'seq' INT" +
                    ");"
            );

            System.out.println("BASE DE DATOS CREADA!!!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
