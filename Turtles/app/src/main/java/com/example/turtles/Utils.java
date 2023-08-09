package com.example.turtles;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static int getIndexFromArray(List<String> list, String value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(value)) {
                return i;
            }
        }
        return -1; // Valore predefinito nel caso in cui il valore non sia trovato
    }

    public static File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }



    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public static BitmapDrawable createDynamicTextImage(Context context, String text1, String text2, String text3) {
        // Carica l'immagine di sfondo dall'ImageView (campo)
        if(context==null){
            return null;
        }
        Drawable backgroundDrawable = context.getDrawable(R.drawable.campo2);

        // Imposta le dimensioni desiderate per l'immagine
        int width = backgroundDrawable.getIntrinsicWidth();
        int height = backgroundDrawable.getIntrinsicHeight();

        // Crea un nuovo bitmap vuoto con le dimensioni dell'immagine
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Crea un oggetto Canvas per disegnare sul bitmap
        Canvas canvas = new Canvas(bitmap);

        // Disegna l'immagine di sfondo
        backgroundDrawable.setBounds(0, 0, width, height);
        backgroundDrawable.draw(canvas);

        // Crea un oggetto Paint per impostare le proprietà del testo
        Paint textPaint = new Paint();
        textPaint.setColor(Color.GREEN); // Colore del testo nero
        textPaint.setTextSize(500); // Dimensione del testo in pixel
        textPaint.setTypeface(Typeface.DEFAULT_BOLD); // Stile del testo (in questo caso grassetto)

        // Disegna i testi sui rispettivi posti
        float x1 = width * 0.24f; // 10% della larghezza
        float y1 = height * 0.63f; // 10% dell'altezza
        canvas.drawText(text1, x1, y1, textPaint);

        float x2 = width * 0.68f; // 10% della larghezza
        float y2 = height * 0.26f; // 50% dell'altezza
        canvas.drawText(text2, x2, y2, textPaint);

        float x3 = width * 0.60f; // 10% della larghezza
        float y3 = height * 0.65f; // 90% dell'altezza
        canvas.drawText(text3, x3, y3, textPaint);

        // Crea un oggetto Drawable da utilizzare nell'ImageView
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);

        // Restituisci il Drawable
        return (BitmapDrawable) drawable;
    }

    public static Uri saveImageToGallery(Context context, Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File imageFile = new File(storageDir, imageFileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            // Aggiorna la galleria con la nuova immagine salvata
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, null);
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




}
