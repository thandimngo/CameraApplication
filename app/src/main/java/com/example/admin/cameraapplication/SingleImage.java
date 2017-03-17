package com.example.admin.cameraapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.data;

/**
 * Created by admin on 2017/03/07.
 */

public class SingleImage extends AppCompatActivity {

    private static final String IMAGE_FILE_LOCATION = "image_file_location";
    private ImageView singleImageViewer;
    private Button buttonWatermark;
    private Button buttonSave;
    private Button buttonTimestamp;
    private Button buttonShare;

    private String userInputValue = "";
   private TextView myTextView;
    private TextView myTimeStamp;



    private static final int GALLERY_INTENT = 2;
    private File pGalleryFolder;
    private String pgalleryLocation = "Pic's Gallery";
    private String pImageFileLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        singleImageViewer = (ImageView) findViewById(R.id.singleImage);
        buttonWatermark = (Button) findViewById(R.id.btnWatermark);
        buttonSave = (Button) findViewById(R.id.btnSave);
        buttonTimestamp = (Button) findViewById(R.id.btnTimeStamp);
        myTextView =(TextView) findViewById(R.id.myImageViewText);
        myTimeStamp=(TextView) findViewById(R.id.myTimeStamp);



        textIT();
        timeStampIT();
        takeScreen();



        File imageFile = new File(
                getIntent().getStringExtra(IMAGE_FILE_LOCATION)
        );

        SingleBitmapWorkerTask workerTask = new SingleBitmapWorkerTask(singleImageViewer, width, height);
        workerTask.execute(imageFile);
        singleImageViewer.setRotation((float) 90);


        imageLongClick();




    }

    public void imageLongClick() {
        singleImageViewer.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(SingleImage.this);
                alert.setMessage("Do you want to delete this picture?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
/*

                        File file = new File(imagePath);
                        file.delete();
*/

                        Intent objIntent = new Intent(SingleImage.this, MainActivity.class);
                        startActivity(objIntent);

                              dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();

                return true;
            }
        });
    }

    private void displayTextBox(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_text, null);
        dialogBuilder.setView(dialogView);
        final EditText textContent = (EditText) dialogView.findViewById(R.id.add_text_on_image);
        dialogBuilder.setTitle("");
        dialogBuilder.setMessage("");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                userInputValue = textContent.getText().toString();
                if(!userInputValue.equals("") || !userInputValue.isEmpty()){

                    myTextView.setText(userInputValue);
                    myTextView.setRotation((float) 70);

                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }



    private void textIT(){
       buttonWatermark.setOnClickListener(new View.OnClickListener(){

           @Override
           public void onClick(View v) {
               displayTextBox();
           }
       });
    }


    private void timeStamp(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH_mmss").format(new Date());

        myTimeStamp.setText(timeStamp);



    }

    private void timeStampIT(){
        buttonTimestamp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                timeStamp();
            }
        });
    }

    private Bitmap takeScreenShot(View v){
        Bitmap screenShot = null;

        try{
            int widthV= v.getMeasuredWidth();
            int heightV=v.getMeasuredHeight();

            screenShot=Bitmap.createBitmap(widthV,heightV, Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(screenShot);
            v.draw(c);
        } catch (Exception e){
            e.printStackTrace();
        }


        return screenShot;
    }

    private void saveScreenShot(Bitmap bm){
        ByteArrayOutputStream save = null;
        File file = null;

        try{
            save = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,40,save);

            // file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ File.separator + pgalleryLocation);
            file.createNewFile();
            if (!pGalleryFolder.exists()) {
                pGalleryFolder.mkdirs();//file created

            }


            FileOutputStream show = new FileOutputStream(file);
            show.write(save.toByteArray());
            show.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH_mmss").format(new Date());
        String imageFileName = "IMAGES" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", pGalleryFolder);
        pImageFileLocation = image.getAbsolutePath();

        return image;
    }

    private void takeScreen(){
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout1);
                try {
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                layout.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap pic = takeScreenShot(layout);

                        try{
                            if(pic != null){
                                saveScreenShot(pic);
                                Toast.makeText(SingleImage.this,
                                        "Saved", Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}