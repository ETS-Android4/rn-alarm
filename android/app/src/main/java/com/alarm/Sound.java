package com.alarm;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

public class Sound {

    private static final String TAG = "Sound";
    private static final long DEFAULT_VIBRATION = 100;

    private AudioManager audioManager;
    private int userVolume;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private Context context;

    public Sound(Context context) {
        this.context = context;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        this.mediaPlayer = new MediaPlayer();
    }

    public void play(String soundName) {
        Uri soundUri = getSoundUri(soundName);
        playSound(soundUri);
        startVibration();
    }

    public void stop() {
        stopSound();
        stopVibration();
    }

    public void release() {
        mediaPlayer.release();
    }

    private void playSound(Uri soundUri) {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setDataSource(context, soundUri);
                mediaPlayer.setVolume(100, 100);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to play sound", e);
        }
    }

    private void stopSound() {
        try {
            // reset the volume to what it was before we changed it.
            //audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);
            mediaPlayer.stop();
            //mediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ringtone: " + e.getMessage());
        }
    }

    private void startVibration() {
        //startVibration
        vibrator.vibrate(DEFAULT_VIBRATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(500);
        }

        long[] pattern = {0, 100, 1000};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        vibrator.vibrate(pattern, 0);
    }

    private void stopVibration() {
        vibrator.cancel(); // cancels
    }

    private Uri getSoundUri(String soundName) {
        Uri soundUri = null;
        if (soundName.equals("default")) {
            soundUri = Settings.System.DEFAULT_RINGTONE_URI;
        } else {
            int resId;
            if (context.getResources().getIdentifier(soundName, "raw", context.getPackageName()) != 0) {
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
            } else {
                soundName = soundName.substring(0, soundName.lastIndexOf('.'));
                resId = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
            }

            soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
        }
        return soundUri;
    }
}
