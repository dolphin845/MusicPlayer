package com.example.musicplayer.interfaces;

public interface IPlayerControl {

    //播放状态
    int PLAY_STATE_PLAY = 1;
    int PLAY_STATE_PAUSE = 2;
    int PLAY_STATE_STOP = 3;


    /**
     * 把UI的控制接口设置给逻辑层
     * @param viewController
     */
    void registerViewController(IPlayerViewControl viewController);

    /**
     * 取消ui控制的注册
     */
    void unregisterViewController();

    void playOrPause();

    void stopPlay();

    void seekTo(int seek);
}
