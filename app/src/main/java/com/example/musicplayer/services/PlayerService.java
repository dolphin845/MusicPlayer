package com.example.musicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.musicplayer.presenter.PlayerPresenter;

public class PlayerService extends Service {
    private PlayerPresenter playerPresenter;
    @Override
    public void onCreate() {
        super.onCreate();
        if (playerPresenter == null) {
            playerPresenter = new PlayerPresenter();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return playerPresenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerPresenter = null;
    }
}
