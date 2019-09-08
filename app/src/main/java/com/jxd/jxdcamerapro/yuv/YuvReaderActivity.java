package com.jxd.jxdcamerapro.yuv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jxd.jxdcamerapro.R;
import com.jxd.jxdcamerapro.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class YuvReaderActivity extends Activity {
    private Button yuvReaderBtn,yuvTransBtn;
    private static final int TRANS_TO_YUV = 0;
    private static final int YUV_READER = 1;

    private ImageView imageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuv_reader);

        setViews();

    }

    private void setViews() {

        imageView = ((ImageView) findViewById(R.id.yuv_shower_img));

        yuvTransBtn = (Button) findViewById(R.id.trans_to_yuv_btn);
        yuvTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transOnclick();
            }
        });

        yuvReaderBtn = (Button) findViewById(R.id.yuv_reader_btn);
        yuvReaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readerOnclick();
            }
        });
    }

    private void readerOnclick() {
        toResult(YUV_READER);
    }

    private void transOnclick() {
        toResult(TRANS_TO_YUV);
    }

    private void toResult(int code){
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        if(code==TRANS_TO_YUV){
            intent.setType("image/*");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intent,code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("YuvReaderActivityp","requestCode "+requestCode);
        if(data == null || data.getData()==null){
            return;
        }
        String path = FileUtils.getPathFromUri(YuvReaderActivity.this,data.getData());
        switch (requestCode){
            case TRANS_TO_YUV:
                transYuv(path);
                break;

            case YUV_READER:

                yuvReader(path);
                break;
        }
    }

    private void yuvReader(final String path) {
        Log.d("YuvReaderActivityp","yuvReader "+path);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {

                    FileInputStream in = new FileInputStream(path);
                    Log.d("YuvReaderActivityp","readfile "+in.available()/1024);
                    byte[] buffer = new byte[in.available()];//in.available() 表示要读取的文件中的数据长度
                    in.read(buffer);  //将文件中的数据读到buffer中
                    //最后记得，关闭流
                    in.close();
                    onFrame(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void onFrame(byte[] yuvData) {
        int prevSizeW = 1440;
        int prevSizeH = 810;
        Log.d("YuvReaderActivityp","I420ToRGB  ");
        byte[] nv21data=I420Tonv21(yuvData,prevSizeW,prevSizeH);
        YuvImage yuvimage = new YuvImage(
                nv21data,
                ImageFormat.NV21,
                prevSizeW,
                prevSizeH,
                null);//data是onPreviewFrame参数提供

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg( new Rect(0, 0,yuvimage.getWidth(), yuvimage.getHeight()), 100, baos);// 80--JPG图片的质量[0-100],100最高
        byte[] rawImage =baos.toByteArray();

        BitmapFactory.Options options = new BitmapFactory.Options();
//options.inPreferredConfig = Bitmap.Config.RGB_565;   //默认8888
//options.inSampleSize = 8;
        SoftReference<Bitmap> softRef = new SoftReference<Bitmap>(BitmapFactory.decodeByteArray(rawImage, 0,rawImage.length,options));//方便回收
        final Bitmap bmpout = (Bitmap) softRef.get();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("YuvReaderActivityp","imageView show ");
                imageView.setImageBitmap(bmpout);
            }
        });

    }


    private void transYuv(final String path) {
        Log.d("YuvReaderActivityp","transYuv "+path);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    BitmapFactory.Options option = new BitmapFactory.Options();
                    option.inSampleSize = 4;
                    Bitmap bitmap = BitmapFactory.decodeFile(path,option);
                    int inputWidth = bitmap.getWidth();
                    int inputHeight = bitmap.getHeight();
                    int[] argb = new int[inputWidth * inputHeight];
                    // Bitmap 获取 argb
                    bitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
                    byte[] yuv = new byte[inputWidth * inputHeight *3/2];
                    // argb 8888 转 i420
                    conver_argb_to_i420(yuv, argb, inputWidth, inputHeight);
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(path).getParent()+"/"+System.currentTimeMillis()+".yuv");
                    fileOutputStream.write(yuv);
                    fileOutputStream.close();
                    Log.d("YuvReaderActivityp","transYuv success");
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });
    }

    /**
     * I420转nv21
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static byte[] I420Tonv21(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total, total /4);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total + total / 4, total /4);

        bufferY.put(data, 0, total);
        for (int i=0; i<total/4; i+=1) {
            bufferV.put(data[total+i]);
            bufferU.put(data[i+total+total/4]);
        }

        return ret;
    }

    // argb 8888 转 i420
    public static void conver_argb_to_i420(byte[] i420, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;                   // Y start index
        int uIndex = frameSize;           // U statt index
        int vIndex = frameSize*5/4; // V start index: w*h*5/4

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; //  is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // I420(YUV420p) -> YYYYYYYY UU VV
                i420[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && i % 2 == 0) {
                    i420[uIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                    i420[vIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                }
                index ++;
            }
        }
    }


}
