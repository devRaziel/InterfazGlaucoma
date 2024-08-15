package com.example.interfaz;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class popup_glaucoma extends AppCompatActivity {
    private ImageView imageViewProcessed;
    private Button buttonSaveImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popup_glaucoma);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        //setTitle("Segmentacion Copa y disco");
        DisplayMetrics medidadventana = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(medidadventana);
        int ancho = medidadventana.widthPixels;
        int alto = medidadventana.heightPixels;
        getWindow().setLayout((int)(ancho*0.95),(int)(alto * 0.65));
        imageViewProcessed = findViewById(R.id.imageView_processed);

        buttonSaveImage = findViewById(R.id.button_save_image);
        buttonSaveImage.setOnClickListener(view -> saveImage());
        // Establecer el tamaño máximo para el ImageView
        //imageViewProcessed.setMaxWidth(100); // Tamaño máximo en píxeles
        //mageViewProcessed.setMaxHeight(100); // Tamaño máximo en píxeles
        // Solicitar la imagen automáticamente al crear la actividad
        requestImage();
    }
    private void requestImage() {
        OkHttpClient client = new OkHttpClient();

        // Construir la solicitud GET para obtener la imagen procesada del servidor Flask
        Request request = new Request.Builder()
                .url("http://192.168.100.13:5000/get_processed_image") // Cambia la IP según tu configuración
                .build();

        // Enviar la solicitud al servidor
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(popup_glaucoma.this, "Error al solicitar la imagen", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Verificar que la respuesta sea exitosa (código 200)
                if (response.isSuccessful()) {
                    // Obtener la imagen procesada como un byte array
                    byte[] imageData = Objects.requireNonNull(response.body()).bytes();


                    // Convertir el byte array en un Bitmap para mostrarlo en ImageView
                    Bitmap processedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                    // Obtener las dimensiones del bitmap original
                    int originalWidth = processedBitmap.getWidth();
                    int originalHeight = processedBitmap.getHeight();

                    // Obtener el doble de las dimensiones originales
                    int scaledWidth = originalWidth * 2;
                    int scaledHeight = originalHeight * 2;

                    // Escalar el bitmap a las nuevas dimensiones
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(processedBitmap, scaledWidth, scaledHeight, true);

                    // Convertir el bitmap escalado en un BitmapDrawable
                    BitmapDrawable scaledDrawable = new BitmapDrawable(getResources(), scaledBitmap);

                    // Mostrar la imagen procesada en ImageView con el tamaño escalado
                    runOnUiThread(() -> imageViewProcessed.setImageDrawable(scaledDrawable));
                } else {
                    runOnUiThread(() -> Toast.makeText(popup_glaucoma.this, "Error al obtener la imagen procesada", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void saveImageToGallery(Bitmap bitmap) {
        // Crear un nombre único para el archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        // Obtener la ruta del directorio público donde se guardarán las imágenes
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try {
            // Crear un flujo de salida para guardar el archivo de imagen
            FileOutputStream fos = new FileOutputStream(imageFile);

            // Comprimir y guardar el bitmap como un archivo JPEG en el directorio de imágenes
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // Cerrar el flujo de salida
            fos.flush();
            fos.close();

            // Escanear el archivo para que aparezca en la galería de imágenes
            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null,
                    (path, uri) -> {
                        // Notificar al usuario que la imagen se ha guardado exitosamente
                        Toast.makeText(popup_glaucoma.this, "Imagen guardada en Galería", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            e.printStackTrace();
            // Notificar al usuario si ocurre un error al guardar la imagen
            Toast.makeText(popup_glaucoma.this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveImage() {
        // Obtener el bitmap del ImageView
        BitmapDrawable drawable = (BitmapDrawable) imageViewProcessed.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        // Llamar a la función para guardar la imagen
        saveImageToGallery(bitmap);
    }
}