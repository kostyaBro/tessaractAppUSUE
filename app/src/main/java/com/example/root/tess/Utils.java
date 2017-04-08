package com.example.root.tess;

import android.os.Environment;

public class Utils {
    public static final String TESSDATA= "/tessdata";
    public static final String PATH_DATA = Environment.getExternalStorageDirectory().toString() + TESSDATA;
}
