package com.tvs.vitalsign.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.ui.MainActivity;

import org.jetbrains.annotations.NotNull;

public class SplashActivity extends AppCompatActivity {


    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imgLogo = findViewById(R.id.img_logo);
        if (hasPermission()) {
            SplashAnimation();
        } else {
            requestPermission();
        }
    }

    private void SplashAnimation() {
        Animation imgAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
        imgLogo.startAnimation(imgAnim);
        imgAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, @NotNull final String[] permissions, @NotNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                SplashAnimation();
            } else {
                requestPermission();
            }
        }
    }

    private boolean hasPermission() {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(PERMISSION_RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
                || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)
                || shouldShowRequestPermissionRationale(PERMISSION_RECORD_AUDIO)) {
            Toast.makeText(SplashActivity.this,
                    "Camera, record audio and storage permission are required for this demo", Toast.LENGTH_LONG).show();
        }
        requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE, PERMISSION_RECORD_AUDIO}, PERMISSIONS_REQUEST);
    }
}
