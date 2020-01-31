package com.example.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.interfaces.IPlayerControl;
import com.example.musicplayer.interfaces.IPlayerViewControl;
import com.example.musicplayer.services.PlayerService;

import static com.example.musicplayer.interfaces.IPlayerControl.PLAY_STATE_PAUSE;
import static com.example.musicplayer.interfaces.IPlayerControl.PLAY_STATE_PLAY;
import static com.example.musicplayer.interfaces.IPlayerControl.PLAY_STATE_STOP;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SeekBar seekBar;
    private Button playOrPause;
    private Button stop;
    private IPlayerControl playerControl;
    private boolean isUserTouchProgressBar = false;

    private IPlayerViewControl playerViewControl = new IPlayerViewControl() {
        @Override
        public void onPlayerStateChange(int state) {
            //根据播放状态来修改UI
            switch (state){
                case PLAY_STATE_PLAY:
                    playOrPause.setText("pause");
                    break;
                case PLAY_STATE_PAUSE:
                case PLAY_STATE_STOP:
                    playOrPause.setText("play");
                    break;
            }
        }

        @Override
        public void onSeekChange(int seek) {
            //改变播放进度，当手触摸到进度条时不更新(避免抖动)
            if (!isUserTouchProgressBar) {
                seekBar.setProgress(seek);
            }
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定服务成功以后，注册UI
            playerControl = (IPlayerControl) service;
            playerControl.registerViewController(playerViewControl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerControl = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();

        //为了能后台长期运行，这里要先开启服务
        initService();
        //绑定服务
        initBindService();
    }

    private void initService() {
        startService(new Intent(this, PlayerService.class));
    }

    private void initBindService() {
        Intent intent = new Intent(this, PlayerService.class);

        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止拖动时
                isUserTouchProgressBar = false;
                int touchProgress = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch: " + touchProgress);
                if (playerControl != null) {
                    playerControl.seekTo(touchProgress);
                }
            }
        });
        playOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerControl != null) {
                    //播放或者暂停
                    playerControl.playOrPause();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerControl != null) {
                    //点击停止播放
                    playerControl.stopPlay();
                }
            }
        });
    }

    private void initView() {
        seekBar = this.findViewById(R.id.seek);
        playOrPause = this.findViewById(R.id.play_or_pause);
        stop = this.findViewById(R.id.stop);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        if (playerControl != null) {
            playerControl.unregisterViewController();
        }
    }
}
