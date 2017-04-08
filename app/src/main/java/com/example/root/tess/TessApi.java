package com.example.root.tess;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessApi {
    private static final String TAG = TessApi.class.getSimpleName();
    private static volatile TessApi instance;

    TessBaseAPI tessBaseAPI;

    private TessApi() {
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(Utils.TESSDATA, "rus");
        Log.i(TAG, "TessApi: created");
    }

    static TessApi getInstance() {
        TessApi localInstance = instance;
        if (localInstance == null) synchronized (TessApi.class) {
            localInstance = instance;
            if (localInstance == null) instance = localInstance = new TessApi();
        }
        return localInstance;
    }

    public String BitmapToText(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        String extractedText = tessBaseAPI.getUTF8Text();
        return extractedText;
    }

    @Override
    protected void finalize() throws Throwable {
        tessBaseAPI.end();
        instance = null;
        super.finalize();
    }
}
