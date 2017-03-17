/*
package com.example.admin.cameraapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

*/
/**
 * Created by admin on 2017/03/16.
 *//*


public class ScreenShot extends AppCompatActivity {

    Button takeBtn;
    private File pGalleryFolder;
    private String pgalleryLocation = "Pic's Gallery";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        takeBtn=(Button) findViewById(R.id.btnSave);
        takeScreen();

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
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), pgalleryLocation);
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

private void takeScreen(){
    takeBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);

            layout.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap pic = takeScreenShot(layout);

                    try{
                        if(pic != null){
                            saveScreenShot(pic);
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
*/
