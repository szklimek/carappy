package com.szklimek.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity {
    // Exiting animation configuration
    public static final String KEY_EXITING_ANIM_ENTER_SCREEN = "animator-exit-enter-screen";
    public static final String KEY_EXITING_ANIM_EXIT_SCREEN = "animator-exit-exit-screen";

    public int exitingEnterScreenAnimationId;
    public int exitingExitScreenAnimationId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate: " + getClass().getSimpleName());
        exitingEnterScreenAnimationId = getIntent().getIntExtra(KEY_EXITING_ANIM_ENTER_SCREEN, 0);
        exitingExitScreenAnimationId = getIntent().getIntExtra(KEY_EXITING_ANIM_EXIT_SCREEN, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart: " + getClass().getSimpleName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume: " + getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause: " + getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop: " + getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy: " + getClass().getSimpleName());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(exitingEnterScreenAnimationId, exitingExitScreenAnimationId);
    }
}
