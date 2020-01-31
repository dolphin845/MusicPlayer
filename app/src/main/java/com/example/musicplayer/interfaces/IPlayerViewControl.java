package com.example.musicplayer.interfaces;

public interface IPlayerViewControl {
    /**
     * 播放状态改变的通知
     * @param state
     */
    void onPlayerStateChange(int state);

    /**
     * 播放进度改变的通知
     */
    void onSeekChange(int seek);
}
