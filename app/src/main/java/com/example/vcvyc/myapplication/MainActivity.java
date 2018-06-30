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
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    //模型地址、facenet类、要比较的两张图片
    public static String model_path="file:///android_asset/20180402-114759.pb";
    public Facenet facenet;
    public Bitmap bitmap1;
    public Bitmap bitmap2;
    //图片显示的空间
    public ImageView imageView1;
    public ImageView imageView2;
    //
    public MTCNN mtcnn;

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
        return Utils.copyBitmap(bitmap);
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
        /*安卓自带人脸检测实现
        Rect rect1 = FaceDetect.detectBiggestFace(bitmap1);
        if (rect1==null) return -1;
        Rect rect2 = FaceDetect.detectBiggestFace(bitmap2);
        if (rect2==null) return -2;*/
        Bitmap bm1=Utils.copyBitmap(bitmap1);
        Bitmap bm2=Utils.copyBitmap(bitmap2);
        Vector<Box> boxes=mtcnn.detectFaces(bitmap1,40);
        Vector<Box> boxes1=mtcnn.detectFaces(bitmap2,40);
        if (boxes.size()==0) return -1;
        if (boxes1.size()==0)return -2;
        for (int i=0;i<boxes.size();i++) Utils.drawBox(bitmap1,boxes.get(i),1+bitmap1.getWidth()/500 );
        for (int i=0;i<boxes1.size();i++) Utils.drawBox(bitmap2,boxes1.get(i),1+bitmap2.getWidth()/500 );
        Log.i("Main","[*]boxNum"+boxes1.size());
        Rect rect1=boxes.get(0).transform2Rect();
        Rect rect2=boxes1.get(0).transform2Rect();
        //MTCNN检测到的人脸框，再上下左右扩展margin个像素点，再放入facenet中。
        int margin=20; //20这个值是facenet中设置的。自己应该可以调整。
        Utils.rectExtend(bitmap1,rect1,margin);
        Utils.rectExtend(bitmap2,rect2,margin);
        //要比较的两个人脸，加厚Rect
        Utils.drawRect(bitmap1,rect1,1+bitmap1.getWidth()/100 );
        Utils.drawRect(bitmap2,rect2,1+bitmap2.getWidth()/100 );
        //(2)裁剪出人脸(只取第一张)
        Bitmap face1=Utils.crop(bitmap1,rect1);
        Bitmap face2=Utils.crop(bitmap2,rect2);
        //(显示人脸)
        imageView1.setImageBitmap(bitmap1);
        imageView2.setImageBitmap(bitmap2);
        //(3)特征提取
        FaceFeature ff1=facenet.recognizeImage(face1);
        FaceFeature ff2=facenet.recognizeImage(face2);
        bitmap1=bm1;
        bitmap2=bm2;
        //(4)比较
        return ff1.compare(ff2);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtcnn=new MTCNN(getAssets());
        imageView1=(ImageView)findViewById(R.id.imageView);
        imageView2=(ImageView)findViewById(R.id.imageView2);
        //载入facenet
        long t_start=System.currentTimeMillis();
        facenet=new Facenet(getAssets());
        long t2=System.currentTimeMillis();
        textviewLog("[*]模型载入成功,Time[ms]:"+(t2-t_start));
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
            if (bm.getWidth()>1000)  bm=Utils.resize(bm,1000);
            if (requestCode == 0x1 && resultCode == RESULT_OK) {
                //imageView1.setImageURI(data.getData());
                bitmap1=Utils.copyBitmap(bm);
                imageView1.setImageBitmap(bitmap1);
            }else {
                //imageView2.setImageURI(data.getData());
                bitmap2=Utils.copyBitmap(bm);
                imageView2.setImageBitmap(bitmap2);
            }
        }catch (Exception e){
            Log.d("MainActivity","[*]"+e);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
