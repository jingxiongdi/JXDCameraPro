package com.jxd.jxdcamerapro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jxd.jxdcamerapro.base.BaseActivity;
import com.jxd.jxdcamerapro.screen.ScreenRecorder;
import com.jxd.jxdcamerapro.utils.ACache;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity{
    private Button screenBtn =null;
    private Button cameraBtn = null;
    private static final int START_RECORD_CODE = 1;
    private ScreenRecorder mRecorder = null;
    private MediaProjectionManager mediaProjectionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenBtn = (Button) findViewById(R.id.screen_btn);

        String key = ACache.get(MainActivity.this).getAsString("ScreenRecorder");
        if(TextUtils.isEmpty(key) || key.equals("false")){
            screenBtn.setText("开始录制");
        }else {
            screenBtn.setText("停止录制");
        }
        screenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String key = ACache.get(MainActivity.this).getAsString("ScreenRecorder");
                    if(TextUtils.isEmpty(key) || key.equals("false")){
                        ACache.get(MainActivity.this).put("ScreenRecorder","true");
                        startScreenRecord();
                        screenBtn.setText("停止录制");
                    }else {
                        ACache.get(MainActivity.this).put("ScreenRecorder","false");
                        screenBtn.setText("开始录制");
                        releaseResource();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    }
                }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenRecord() {
         mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager != null){
            Intent intent = mediaProjectionManager.createScreenCaptureIntent();
            PackageManager packageManager = getPackageManager();
            if (packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY) != null){
                //存在录屏授权的Activity
                startActivityForResult(intent,START_RECORD_CODE);
            }else {
                toastShort("无法录制");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        if (requestCode == START_RECORD_CODE && resultCode == Activity.RESULT_OK){
            try {
                final int width = 360;
                final int height = 640;
                File file = new File(Environment.getExternalStorageDirectory() + "/"
                        + "1ScreenRecorder" + "/ScreenRecorder-" + width + "x" + height + "-"
                        + ".mp4");
                File dirs = new File(file.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//        File file = new File(Environment.getExternalStorageDirectory(),
//                "record-" + width + "x" + height + "-" + System.currentTimeMillis() + ".mp4");
                final int bitrate = 1024*512;
                mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
                mRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            toastShort("拒绝录屏");
        }
    }

    private void releaseResource(){
        if(mRecorder!=null){
            mRecorder.quit();
            mRecorder=null;
        }

        if(mediaProjectionManager!=null){
            mediaProjectionManager = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResource();
    }
}
