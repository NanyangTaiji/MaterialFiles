/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import me.zhanghai.android.files.theme.custom.CustomThemeAppCompatActivity;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.util.FragmentUtils;

public class SettingsActivity extends CustomThemeAppCompatActivity
        implements CustomThemeHelper.OnThemeChangedListener {

    private static final String KEY_PREFIX = SettingsActivity.class.getName() + '.';

    private static final String EXTRA_SAVED_INSTANCE_STATE = KEY_PREFIX + "SAVED_INSTANCE_STATE";

    private boolean mSuperCreated;

    private boolean mRestarting;

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @NonNull
    private static Intent newIntent(@NonNull Bundle savedInstanceState, @NonNull Context context) {
        return newIntent(context)
                .putExtra(EXTRA_SAVED_INSTANCE_STATE, savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getBundleExtra(EXTRA_SAVED_INSTANCE_STATE);
        }
        super.onCreate(savedInstanceState);
        mSuperCreated = true;

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            FragmentUtils.add(SettingsActivityFragment.newInstance(), this, android.R.id.content);
        }
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);

        // onNightModeChanged() can be called during super.onCreate(), and we should not call
        // restart() in that case.
        if (!mSuperCreated) {
            return;
        }
        // AppCompatDelegateImpl.updateForNightMode() calls ActivityCompat.recreate(), which may
        // call ActivityRecreator.recreate() without calling Activity.recreate(), so we cannot
        // simply override it. To work around this, we declare android:configChanges="uiMode" in our
        // manifest and manually call restart().
        restart();
    }

    @Override
    public void onThemeChanged(@StyleRes int theme) {
        // The same thing about ActivityCompat.recreate() in onNightModeChanged() applies here as
        // well.
        restart();
    }

    private void restart() {
        Bundle savedInstanceState = new Bundle();
        onSaveInstanceState(savedInstanceState);
        finish();
        startActivity(newIntent(savedInstanceState, this));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mRestarting = true;
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mRestarting || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(@NonNull KeyEvent event) {
        return mRestarting || super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        return mRestarting || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(@NonNull MotionEvent event) {
        return mRestarting || super.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(@NonNull MotionEvent event) {
        return mRestarting || super.dispatchGenericMotionEvent(event);
    }
}
