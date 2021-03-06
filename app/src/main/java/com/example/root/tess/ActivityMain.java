package com.example.root.tess;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    private TextView textView;
    Uri outputFileDir;
    public static final String TESS_DATA = "/tessdata";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";
    private TessBaseAPI tessBaseAPI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        textView = (TextView) this.findViewById(R.id.textView);

        final Activity activity = this;
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 120);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 121);
                }
                startCameraActivity();
            }
        });
    }

    private void startCameraActivity() {
        String imgPath = DATA_PATH + "/img";
        File dir = new File(imgPath);
        if (!dir.exists()) dir.mkdir();
        String imgFilePath = imgPath + "/tess.jpg";
        outputFileDir = Uri.fromFile(new File(imgFilePath));
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode);
        Log.d(TAG, "onActivityResult: " + resultCode + "  " + Activity.RESULT_OK);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            prepareTessData();
            startOCR(outputFileDir);
        } else {
            Toast.makeText(getApplicationContext(), "Image problem", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareTessData() {
        try {
            File dir = new File(DATA_PATH + TESS_DATA);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String fileList[] = getAssets().list("");
            for (String fileName : fileList) {
                String pathToDataFile = DATA_PATH + TESS_DATA + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {
                    InputStream in = getAssets().open(fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void startOCR(Uri imageUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 7;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
            String result = this.getText(bitmap);
            textView.setText(result);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getText(Bitmap bitmap) {
        try {
            tessBaseAPI = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        tessBaseAPI.init(DATA_PATH, "eng");
        tessBaseAPI.setImage(bitmap);
        String retStr = "No result";
        try {
            retStr = tessBaseAPI.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        tessBaseAPI.end();
        return retStr;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 120: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case 121: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Write permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            return;
        }
    }
}