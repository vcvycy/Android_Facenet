package com.example.vcvyc.myapplication;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
 /*
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
*/
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.nio.IntBuffer;

/**功能：人脸转换为512维特征向量
 */
public class Facenet{
    private static final String MODEL_FILE  = "file:///android_asset/20180402-114759.pb";
    private static final String INPUT_NAME  = "input:0";
    private static final String OUTPUT_NAME = "embeddings:0";
    private static final String PHASE_NAME  = "phase_train:0";
    private static final String[] outputNames = new String[] {OUTPUT_NAME};
    //输入图片大小.(图片非此大小，会rescale)
    private static final int INPUT_SIZE=160;
    private float[] floatValues;  //保存input的值
    private int[] intValues;      //像素值
    private AssetManager assetManager;
    private TensorFlowInferenceInterface inferenceInterface;
    Facenet(AssetManager mgr){
        assetManager=mgr;
        loadModel();
        floatValues=new float[INPUT_SIZE*INPUT_SIZE*3];
        intValues = new int[INPUT_SIZE * INPUT_SIZE];
    }
    private boolean loadModel(){
        //AssetManager
        try {
            inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
            Log.d("Facenet","[*]load model success");
        }catch(Exception e){
            Log.e("Facenet","[*]load model failed"+e);
            return false;
        }
        return true;
    }
    //Bitmap to floatValues
    private int normalizeImage(final Bitmap _bitmap){
        // (0) bitmap缩放到INPUT_SIZE*INPUT_SIZE
        float scale_width=((float)INPUT_SIZE)/_bitmap.getWidth();
        float scale_height=((float)INPUT_SIZE)/_bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_width,scale_height);
        Bitmap bitmap = Bitmap.createBitmap(_bitmap,0,0,_bitmap.getWidth(),_bitmap.getHeight(),matrix,true);
        //Log.d("Facenet","[*]bitmap size:"+bitmap.getHeight()+"x"+bitmap.getWidth());
        // (1) 将像素映射到[-1,1]区间内
        float imageMean=127.5f;
        float imageStd=128;
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        for (int i=0;i<intValues.length;i++){
            final int val=intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
        }
        //Log.d("Facenet","[*]normalizeImage");
        //Log.d("Facenet","[*]normalizeImage"+intValues.length);
        return 0;
    }
    public FaceFeature recognizeImage(final Bitmap bitmap){
        //Log.d("Facenet","[*]recognizeImage");
        //(0)图片预处理，normailize
        normalizeImage(bitmap);
        //(1)Feed
        try {
            inferenceInterface.feed(INPUT_NAME, floatValues, 1, INPUT_SIZE, INPUT_SIZE, 3);
            boolean []phase=new boolean[1];
            phase[0]=false;
            inferenceInterface.feed(PHASE_NAME,phase);
        }catch (Exception e){
            Log.e("Facenet","[*] feed Error\n"+e);
            return null;
        }
        //(2)run
       // Log.d("Facenet","[*]Feed:"+INPUT_NAME);
        try {
            inferenceInterface.run(outputNames, false);
        }catch (Exception e){
            Log.e("Facenet","[*] run error\n"+e);
            return null;
        }
        //(3)fetch
        FaceFeature faceFeature=new FaceFeature();
        float[] outputs=faceFeature.getFeature();
        try {
            inferenceInterface.fetch(OUTPUT_NAME, outputs);
        }catch (Exception e){
            Log.e("Facenet","[*] fetch error\n"+e);
            return null;
        }
        return faceFeature;
    }
}
