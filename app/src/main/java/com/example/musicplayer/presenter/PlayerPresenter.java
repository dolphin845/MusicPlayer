package com.example.musicplayer.presenter;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;

import com.example.musicplayer.interfaces.IPlayerControl;
import com.example.musicplayer.interfaces.IPlayerViewControl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerPresenter extends Binder implements IPlayerControl {
    private static final String TAG = "PlayerPresenter";
    private IPlayerViewControl playerViewControl;
    private int currentPlayerState = PLAY_STATE_STOP;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private SeekTimerTask seekTimerTask;

    @Override
    public void registerViewController(IPlayerViewControl viewController) {
        playerViewControl = viewController;
    }

    @Override
    public void unregisterViewController() {
        playerViewControl = null;
    }

    @SuppressLint("SdCardPath")
    @Override
    public void playOrPause() {
        Log.d(TAG, "playOrPause: ");
        if (currentPlayerState == PLAY_STATE_STOP) {
            //初始化播放器
            if (mediaPlayer == null) {
                initPlayer();
            }
            mediaPlayer.start();
            currentPlayerState = PLAY_STATE_PLAY;
        } else if (currentPlayerState == PLAY_STATE_PLAY) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
            currentPlayerState = PLAY_STATE_PAUSE;

        } else if (currentPlayerState == PLAY_STATE_PAUSE) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
            currentPlayerState = PLAY_STATE_PLAY;
        }
        playerViewControl.onPlayerStateChange(currentPlayerState);
    }

    private void initPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            //设置数据源
            mediaPlayer.setDataSource("/mnt/sdcard/test.mp3");
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开启一个计时任务，更新播放器进度条
        startTimer();
    }

    @Override
    public void stopPlay() {
        Log.d(TAG, "stopPlay: ");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentPlayerState = PLAY_STATE_STOP;
        if (playerViewControl != null) {
            playerViewControl.onPlayerStateChange(currentPlayerState);
        }
        stopTimer();
    }

    @Override
    public void seekTo(int seek) {
        Log.d(TAG, "seekTo: " + seek);
        if (mediaPlayer == null) {
            initPlayer();
        }
        int targetSeek = (int) (seek * 1.0 / 100 * mediaPlayer.getDuration());
        mediaPlayer.seekTo(targetSeek);
    }

    /**
     * 开启一个timerTask
     */
    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        if (seekTimerTask == null) {
            seekTimerTask = new SeekTimerTask();
        }
        timer.schedule(seekTimerTask, 0, 500);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (seekTimerTask != null) {
            seekTimerTask.cancel();
            seekTimerTask = null;
        }
    }

    private class SeekTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mediaPlayer != null && playerViewControl != null) {
                //获取当前的播放进度
                int currentPosition = (int) (mediaPlayer.getCurrentPosition() * 1.0 /  mediaPlayer.getDuration() * 100);
                playerViewControl.onSeekChange(currentPosition);
            }
        }
    }
}
