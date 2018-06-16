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

/**
 *   人脸检测
 */
public class FaceDetect {
    static int MAX_FACES=5;
    private static Rect face2Rect(FaceDetector.Face f, int imgwidth, int imgheight) {
        PointF p=new PointF();
        f.getMidPoint(p);
        double eyesDistance = f.eyesDistance();

        int x, y, width, height;
        x = (int)(Math.floor(p.x-(1.0*eyesDistance)));
        y = (int)(Math.floor(p.y-(1.0*eyesDistance)));
        width = (int)Math.ceil(2.0*eyesDistance);
        height = (int)Math.ceil(3.0f*eyesDistance);

        if(x < 0)
            x = 0;
        if(y < 0)
            y = 0;
        if((y + height) > imgheight)
            height = (int)(imgheight - y);
        if((x + width) > imgwidth)
            width = (int)(imgwidth - x);

        Rect r = new Rect();
        r.set(x, y, x+width, y+height);
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
    //按照rect的大小裁剪出人脸
    public static Bitmap crop(Bitmap bitmap,Rect rect){
        Bitmap cropped=Bitmap.createBitmap(bitmap,rect.left,rect.top,rect.right-rect.left,rect.bottom-rect.top);
        return cropped;
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
                rects[i]=face2Rect(faces[i],bitmap.getWidth(),bitmap.getHeight());
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
