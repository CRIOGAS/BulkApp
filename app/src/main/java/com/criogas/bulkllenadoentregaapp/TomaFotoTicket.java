package com.criogas.bulkllenadoentregaapp;

        import android.Manifest;
        import android.app.Activity;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.media.MediaScannerConnection;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.content.Context;
        import android.widget.Toast;

        import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;

public class TomaFotoTicket extends AppCompatActivity implements View.OnClickListener {

    private SweetAlertDialog progressUpdateDialog;
    private static Context context;

    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int TAKE_PICTURE = 101;

    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 200;

    private static final String CARPETA_PRINCIPAL = "imagenAppBulk";
    private static final String CARPETA_IMAGEN = "ticket";
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;
    private String path; //almacena ruta de imagen
    File fileImagen;
    Bitmap bitmap;

    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;

    ImageView imgFoto;
    Button btnTakeImg, btnSaveImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecciona_imagen);

        initUI();

        imgFoto.setOnClickListener(this);
        btnTakeImg.setOnClickListener(this);
        btnTakeImg.setOnClickListener(this);
    }

    private void initUI(){
        btnSaveImg = (Button) findViewById(R.id.btnSaveImg);
        btnTakeImg = (Button) findViewById(R.id.btnTakeImg);
        imgFoto = (ImageView) findViewById(R.id.imgViewFoto);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.btnTakeImg){
            checkPermissionCamera();
        }else if(id == R.id.btnSaveImg){
            checkPermissionStorage();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TAKE_PICTURE){
            if(resultCode == Activity.RESULT_OK && data != null){
                bitmap = (Bitmap) data.getExtras().get("data");
                imgFoto.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            if(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePicture();
            }
        }else if(requestCode == REQUEST_PERMISSION_WRITE_STORAGE){
            if(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermissionCamera(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicture();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION_CAMERA);
            }

        }else {
            takePicture();
        }
    }

    private void checkPermissionStorage() {
        if(Build.VERSION.SDK_INT <= 28){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    saveImage();
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_WRITE_STORAGE);
                }

            }else {
                saveImage();
            }
        }
    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, TAKE_PICTURE);
        }
    }

    private void saveImage(){
        OutputStream fos = null;
        File file = null;

        if(Build.VERSION.SDK_INT >= 28){
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();

            String fileName = System.currentTimeMillis() + "ticket_ov";

            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            //values.put(MediaStore.Images.Media., fileName);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);

            //Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            //Uri imageURI = resolver.insert(collection,values);
        }else{
            String imgDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            String fileName = System.currentTimeMillis() + ".jpg";
            file = new File(imgDirectory,fileName);

            try {
                fos = new FileOutputStream(file);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
       boolean isSave = bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);

        if(isSave){
            Toast.makeText(this, "Imagen guardada exitosamete",Toast.LENGTH_SHORT).show();
        }

        if(fos!=null){
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MediaScannerConnection.scanFile(this, new String[]{file.toString()},null,null);
    }
}

