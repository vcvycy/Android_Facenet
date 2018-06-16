package com.example.vcvyc.myapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.Image;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    //模型地址、facenet类、要比较的两张图片
    public static String model_path="file:///android_asset/20180402-114759.pb";
    public Facenet facenet;
    public Bitmap bitmap1;
    public Bitmap bitmap2;
    //图片显示的空间
    public ImageView imageView1;
    public ImageView imageView2;

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
    public void textviewLog(String msg){
        TextView textView=(TextView)findViewById(R.id.textView);
        textView.append("\n"+msg);
    }
    public void showScore(double score,long time){
        TextView textView=(TextView)findViewById(R.id.textView2);
        textView.setText("[*]人脸检测+识别 运行时间:"+time+"\n");
        if (score<=0){
            if (score<-1.5)textView.append("[*]图二检测不到人脸");
            else textView.append("[*]图一检测不到人脸");
        }else{
            textView.append("[*]二者相似度为:"+score+" [可设为小于1.1为同一个人]");
        }
    }
    //比较bitmap1和bitmap2(会先切割人脸在比较)
    public double compareFaces(){
        //(1)圈出人脸，人脸检测(可能会有多个人脸)
        Rect rect1 = FaceDetect.detectBiggestFace(bitmap1);
        if (rect1==null) return -1;
        Rect rect2 = FaceDetect.detectBiggestFace(bitmap2);
        if (rect2==null) return -2;
        //(2)裁剪出人脸(只取第一张)
        Bitmap face1=FaceDetect.crop(bitmap1,rect1);
        Bitmap face2=FaceDetect.crop(bitmap2,rect2);
        //(显示人脸)
        imageView1.setImageBitmap(face1);
        imageView2.setImageBitmap(face2);
        //(3)特征提取
        FaceFeature ff1=facenet.recognizeImage(face1);
        FaceFeature ff2=facenet.recognizeImage(face2);
        //(4)比较
        double score=ff1.compare(ff2);
        return score;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView1=(ImageView)findViewById(R.id.imageView);
        imageView2=(ImageView)findViewById(R.id.imageView2);
        //载入facenet
        long t_start=System.currentTimeMillis();
        facenet=new Facenet(getAssets());
        long t2=System.currentTimeMillis();
        textviewLog("[*]模型载入成功,Time[ms]:"+(t2-t_start));
        textviewLog("[*]人脸检测用的是android自带的，可能会比较弱一点");
        textviewLog("[*]第一次比较会慢3秒左右");
        //先从assets中读取图片
        bitmap1=readFromAssets("trump1.jpg");
        bitmap2=readFromAssets("trump2.jpg");
        long t1=System.currentTimeMillis();
        double score=compareFaces();
        showScore(score,System.currentTimeMillis()-t1);
        //Log.d("MainActivity","[*] end,score="+score);


        //以下是控件事件绑定之类；添加自己上传图片的功能



        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity","[*]you click me ");
                Intent intent= new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent, 0x1);
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent, 0x2);
            }
        });
        Button btn=(Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long t1=System.currentTimeMillis();
                double score=compareFaces();
                showScore(score,System.currentTimeMillis()-t1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(data==null)return;
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            if (requestCode == 0x1 && resultCode == RESULT_OK) {
                //imageView1.setImageURI(data.getData());
                bitmap1=bm;
                imageView1.setImageBitmap(bitmap1);
            }else {
                //imageView2.setImageURI(data.getData());
                bitmap2=bm;
                imageView2.setImageBitmap(bitmap2);
            }
        }catch (Exception e){
            Log.d("MainActivity","[*]"+e);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
