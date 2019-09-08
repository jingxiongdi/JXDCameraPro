package com.jxd.jxdcamerapro.player;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jxd.jxdcamerapro.R;
import com.jxd.jxdcamerapro.base.BaseActivity;

public class NetPlayerActivity extends BaseActivity {
    private VideoView videoView = null;
    private EditText editText = null;
    private Button playBtn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_player);

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setMediaController(new MediaController(this));

        editText = (EditText) findViewById(R.id.edit_text);
        playBtn = (Button) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(editText.getText())){
                    videoView.setVideoPath(editText.getText().toString() );
                    videoView.start();
                }

            }
        });

    }
}
