package com.example.admin.cameraapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements RecyclerViewClick {

    private static final int Activity_Start = 0;
    private Button btnTakePic;
    private ImageView pPhotoCaptureImageView;
    private String pImageFileLocation = "";
    private String pgalleryLocation = "Pic's Gallery";
    private File pGalleryFolder;
    // private RecyclerView pRecycleView;
    private static LruCache<String, Bitmap> pMemoryCache;
    private static Set<SoftReference<Bitmap>> pReuseable;
    private RecyclerView pRecycleView;
    private Size pPreviewSize;
    private String pCameraId;
    private static final String IMAGE_FILE_LOCATION = "image_file_location";
    private Button buttonEdit;
    private Button buttonSave;
    private Button buttonUpload;

    ////////////////
    private StorageReference pStorage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog pProgressDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePic = (Button) findViewById(R.id.btnTakePhoto);
        buttonEdit = (Button) findViewById(R.id.btnShare);
        buttonSave = (Button) findViewById(R.id.btnSave);

        pStorage = FirebaseStorage.getInstance().getReference();
        buttonUpload = (Button) findViewById(R.id.btnUpload);
        pProgressDialog = new ProgressDialog(this);

        pPhotoCaptureImageView = (ImageView) findViewById(R.id.capturedPhoto);
        addTakePic();
        createImageGallery();
        pRecycleView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        uploadImage();


        pRecycleView.setLayoutManager(layoutManager);
        RecyclerView.Adapter imageAdapter = new ImageAdapter(sortFilesToLatest(pGalleryFolder), this);
        pRecycleView.setAdapter(imageAdapter);


        final int maxMemorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        final int cacheSize = maxMemorySize / 100;

        pMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    pReuseable.add(new SoftReference<Bitmap>(oldValue));
                }
            }

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            pReuseable = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());

        }


    }



    private void swapImageAdapter() {
        RecyclerView.Adapter newImageAdapter = new ImageAdapter(sortFilesToLatest(pGalleryFolder), this);
        pRecycleView.swapAdapter(newImageAdapter, false);
    }


    public void capturePic( View v) {
        Intent callCameraIntent = new Intent();
        callCameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(callCameraIntent, Activity_Start);
        swapImageAdapter();

    }

   public void addTakePic() {
        btnTakePic.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              capturePic(v);
                                          }
                                      }
        );
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Activity_Start && resultCode == RESULT_OK || requestCode == GALLERY_INTENT && requestCode == RESULT_OK) ;
        {
            RecyclerView.Adapter newImageAdapter = new ImageAdapter(sortFilesToLatest(pGalleryFolder), this);
            pRecycleView.swapAdapter(newImageAdapter, false);



            pProgressDialog.setMessage("Uploading ...");
            pProgressDialog.show();

            Uri uri = data.getData();
            StorageReference filepath = pStorage.child("Photo's").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        pProgressDialog.dismiss();
                }
            });
        }
    }

    private void uploadImage(){
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);

                
            }
        });
    }
    private void createImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        pGalleryFolder = new File(storageDirectory, pgalleryLocation);
        if (!pGalleryFolder.exists()) {
            pGalleryFolder.mkdirs();//file created
        }
    }

    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HH_mmss").format(new Date());
        String imageFileName = "IMAGE" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", pGalleryFolder);
        pImageFileLocation = image.getAbsolutePath();

        return image;
    }

    public static Bitmap getBitmapFromMemoryCache(String key) {
        return pMemoryCache.get(key);

    }

    public static void setBitmapFromMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) != null) {
            pMemoryCache.put(key, bitmap);
        }
    }

    private static int getPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }



    public static boolean reUseBitmap(Bitmap candidate, BitmapFactory.Options options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int width = options.outWidth / options.inSampleSize;
            int height = options.outHeight / options.inSampleSize;
            int bytesCount = width * height * getPixel(candidate.getConfig());
            return bytesCount <= candidate.getAllocationByteCount();
        }
        return candidate.getWidth() == options.outWidth && candidate.getHeight() == options.outHeight && options.inSampleSize == 0;
    }

    public static Bitmap getBitmapReuseable(BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (pReuseable != null && !pReuseable.isEmpty()) {
            synchronized (pReuseable) {
                Bitmap item;
                Iterator<SoftReference<Bitmap>> iterator = pReuseable.iterator();
                while (iterator.hasNext()) {
                    item = iterator.next().get();
                    if (item != null && item.isMutable()) {
                        if (reUseBitmap(item, options)) {
                            bitmap = item;
                            iterator.remove();
                            break;

                        }
                    } else {
                        iterator.remove();
                    }
                }
            }

        }
        return bitmap;
    }

    private File[] sortFilesToLatest(File fileDir) {
        File[] files = fileDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
            }
        });
        return files;
    }


    @Override
    public void getRecyclerViewAdapter(int position) {
        Intent sendFileIntent = new Intent(this, SingleImage.class);
        sendFileIntent.putExtra(IMAGE_FILE_LOCATION, sortFilesToLatest(pGalleryFolder)[position].toString());
        startActivity(sendFileIntent);


    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }





}