# Android_Facenet
Facenet for Android.
先通过MTCNN截取人脸【多张人脸我是随机选择一张的】，
然后通过FACENET提取特征，人脸相似度通过比较欧几里得距离得到。

* 编译环境：Android 3.1.2
* 人脸检测:MTCNN类用法 (MTCNN.Java)
  * 类实例化 MTCNN mtcnn=new MTCNN(getAssets())
  * 只有1个API：public Vector<Box> detectFaces(Bitmap bitmap,int minFaceSize)
    * 参数bitmap：要处理的图片
    * 参数minFaceSize：最小的脸像素值，一般>=40。越大则检测速度越快，但会忽略掉较小的脸
    * 返回值:所有的脸的Box，包括left/right/top/bottom/landmark(一共5个点，嘴巴鼻子眼)


主要移植自:https://github.com/davidsandberg/facenet
# 运行效果：
![Alt text](Screenshot_20180627-203742.png)
