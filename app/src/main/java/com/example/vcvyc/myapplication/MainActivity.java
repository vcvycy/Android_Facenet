package com.example.vcvyc.myapplication;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static String model_path="file:///android_asset/20180402-114759.pb";
    //从assets中读取图片
    private  Bitmap readFromAssets(String filename){
        Bitmap bitmap;
        AssetManager asm=getAssets();
        try {
            InputStream is=asm.open(filename);
            bitmap= BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("MainActivity","[*]failed to open "+filename);
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //facenet
        Facenet facenet=new Facenet(getAssets());
        Bitmap bitmap;
        for (int i=1;i<=1;i++){
            String filename=""+(i%3+1)+".jpg";
            long t_start=System.currentTimeMillis();
            bitmap=readFromAssets(filename);
            long t_read=System.currentTimeMillis();
            facenet.recognizeImage(bitmap);
            long t_end=System.currentTimeMillis();
            Log.d("MainActivity","[*]Read Time:"+(t_read-t_start));
            Log.d("MainActivity","[*]Recognize Time:"+(t_end-t_read));
        }
        //Log.d("MainActivity","[*]compare:"+f1.compare(f2));
    }
}
