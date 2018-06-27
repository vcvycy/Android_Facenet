package com.example.vcvyc.myapplication;
/* by cjf 1801615 352871242@qq.com*/

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.util.Log;

import java.math.BigDecimal;

import static java.lang.Math.*;

/**  此文件弃用。
 *   Android自带人脸检测，效果太差弃用。现在使用MTCNN。
 */
public class FaceDetect {
    //检测到人脸后，长宽各扩展44个像素
    static int margin=44;
    static int MAX_FACES=5;
    private static Rect face2Rect(FaceDetector.Face f, int imgwidth, int imgheight,int margin) {
        PointF p=new PointF();
        f.getMidPoint(p);
        double eyesDistance = f.eyesDistance();

        int x, y, width, height;
        x = (int)(Math.floor(p.x-(1.0*eyesDistance)));
        y = (int)(Math.floor(p.y-(1.0*eyesDistance)));
        width = (int)Math.ceil(2.0*eyesDistance);
        height = (int)Math.ceil(3.0f*eyesDistance);
        //margin
        int x1=max(x-margin/2,0);
        int y1=max(y-margin/2,0);
        int x2=min(x+width+margin/2,imgwidth-1);
        int y2=min(y+height+margin/2,imgheight-1);
        Rect r = new Rect();
        r.set(x1,y1,x2,y2);
        //r.set(x, y, x+width, y+height);
        Log.d("FaceDetectWH","width:"+imgwidth+" height:"+imgheight);
        Log.d("FaceDetect++","x:"+x+" y:"+y+ "  x2:"+(x+width)+"y2:"+(y+height));
        Log.d("FaceDetect--","x:"+x1+" y:"+y1+ "  x2:"+x2+"y2:"+y2);
        return r;
    }


    //android.media.FaceDetector 暂时只支持RGB_565格式图片
    private static Bitmap convert2rgb565(Bitmap bitmap) {
        Bitmap.Config config=Bitmap.Config.RGB_565;
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }
    public static Rect  detectBiggestFace(Bitmap _bitmap){
        Rect[] rects=FaceDetect.detectFaces(_bitmap);
        if (rects.length==0) return null;
        Rect biggestRect=rects[0];
        for (int i=1;i<rects.length;i++){
            int area1=(rects[i].right-rects[i].left)*(rects[i].bottom-rects[i].top);
            int area=(biggestRect.right-biggestRect.left) * (biggestRect.bottom-biggestRect.top);
            if (area1>area)
                biggestRect=rects[i];
        }
        return biggestRect;
    }
    public static Rect[] detectFaces(Bitmap _bitmap){
        long t_start=System.currentTimeMillis();
        //
        try {
            Bitmap bitmap=convert2rgb565(_bitmap);
            //showImage(bitmap);
            FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_FACES);
            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
            //Log.d("MainActivity","[*]Time_tmp:"+(System.currentTimeMillis()-t_start));

            int face_num = faceDetector.findFaces(bitmap, faces);
            Rect[] rects=new Rect[face_num];
            for (int i=0;i<face_num;i++){
                rects[i]=face2Rect(faces[i],bitmap.getWidth(),bitmap.getHeight(),margin);
                Rect rect=rects[i];
                //Log.d("MainActivity","[*]"+rect.left+" "+rect.top+" "+rect.right+" "+rect.bottom);
            }
            //Log.d("MainActivity","[*]facenum:"+face_num);
            //Log.d("MainActivity","[*]Time:"+(System.currentTimeMillis()-t_start));
            return rects;
        }catch (Exception e){
            Log.d("MainActivity","[*]error "+e);
            return null;
        }
    }
}
