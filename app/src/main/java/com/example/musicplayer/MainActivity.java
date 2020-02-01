package com.example.musicplayer;

import android.Manifest;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.musicplayer.interfaces.IPlayerControl;
import com.example.musicplayer.interfaces.IPlayerViewControl;
import com.example.musicplayer.services.PlayerService;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
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
            switch (state) {
                case PLAY_STATE_PLAY:
                    playOrPause.setText("pause");
                    break;
                case PLAY_STATE_PAUSE:
                    playOrPause.setText("play");
                    break;
                case PLAY_STATE_STOP:
                    playOrPause.setText("play");
                    seekBar.setProgress(0);
                    break;
            }
        }

        @Override
        public void onSeekChange(final int seek) {
            //改变播放进度，当手触摸到进度条时不更新(避免抖动)
            //在Android里面有两个控件可以用子线程去更新
            //一个是progressBar,另一个是surfaceView
            //规范起见还是用runOnUiThread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isUserTouchProgressBar) {
                        seekBar.setProgress(seek);
                    }
                }
            });
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
        //动态申请权限
        myRequestPermission();

        //初始化控件和事件
        initView();
        initEvent();

        //为了能后台长期运行，这里要先开启服务
        initService();
        //绑定服务
        initBindService();
    }

    private void myRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
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
