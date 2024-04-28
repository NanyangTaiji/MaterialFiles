package me.zhanghai.android.files.storage;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;

import androidx.core.view.ViewCompat;

import me.zhanghai.android.files.util.OneShotGlobalLayoutListener;


public class ViewUtils {


    public static boolean isLayoutDirectionRtl(View view) {
        return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isLayoutInStatusBar(View view) {
        return (view.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0;
    }

    public static void setLayoutInStatusBar(View view, boolean value) {
        view.setSystemUiVisibility(value
                ? view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                : view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static boolean isLayoutInNavigation(View view) {
        return (view.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0;
    }

    public static void setLayoutInNavigation(View view, boolean value) {
        view.setSystemUiVisibility(value
                ? view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                : view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public static void fadeIn(View view, boolean force) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
        }
        view.animate().alpha(1f)
                .setDuration(force || view.isLaidOut() ? 0 :
                        view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    public static void fadeOut(View view, boolean force, boolean gone) {
        TimeInterpolator interpolator = new LinearInterpolator();
        view.animate().alpha(0f)
                .setDuration(force || view.isLaidOut() ? 0 :
                        view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setInterpolator(interpolator).start();
        if (gone) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }


    public static void showSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    public static void hideSoftInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void fadeToVisibility(View view, boolean visible, boolean force, boolean gone) {
        if (visible) {
            fadeIn(view, force);
        } else {
            fadeOut(view, force, gone);
        }
    }

    public static void fadeToVisibilityUnsafe(final View view, final boolean visible, final boolean force, final boolean gone) {
        mainHandler.post(() -> fadeToVisibility(view, visible, force, gone));
    }

    @SuppressLint("RtlHardcoded")
    public static void slideIn(View view, int gravity, boolean force) {
        view.setVisibility(View.VISIBLE);
        view.animate().setDuration(force || view.isLaidOut() ? 0 :
                        view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setInterpolator(new AccelerateDecelerateInterpolator());
        switch (Gravity.getAbsoluteGravity(gravity, view.getLayoutDirection())) {
            case Gravity.LEFT:
            case Gravity.RIGHT:
                view.setTranslationX(0f);
                break;
            case Gravity.TOP:
            case Gravity.BOTTOM:
                view.setTranslationY(0f);
                break;
        }
    }

    public static void slideInUnsafe(final View view, final int gravity, final boolean force) {
        mainHandler.post(() -> slideIn(view, gravity, force));
    }

    @SuppressLint("RtlHardcoded")
    public static void slideOut(View view, int gravity, boolean force, boolean gone) {
        view.animate().setDuration(force || view.isLaidOut() ? 0 :
                        view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setInterpolator(new LinearInterpolator());
        switch (Gravity.getAbsoluteGravity(gravity, view.getLayoutDirection())) {
            case Gravity.LEFT:
                view.setTranslationX((-view.getRight()));
                break;
            case Gravity.RIGHT:
                view.setTranslationX(((View) view.getParent()).getWidth() - view.getLeft());
                break;
            case Gravity.TOP:
                view.setTranslationY((-view.getBottom()));
                break;
            case Gravity.BOTTOM:
                view.setTranslationY(((View) view.getParent()).getHeight() - view.getTop());
                break;
        }
        if (gone) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static void slideOutUnsafe(final View view, final int gravity, final boolean force, final boolean gone) {
        mainHandler.post(() -> slideOut(view, gravity, force, gone));
    }

    @SuppressLint("RtlHardcoded")
    public static void slideToVisibility(View view, int gravity, boolean visible, boolean force, boolean gone) {
        if (visible) {
            slideIn(view, gravity, force);
        } else {
            slideOut(view, gravity, force, gone);
        }
    }

    public static void slideToVisibilityUnsafe(final View view, final int gravity, final boolean visible, final boolean force, final boolean gone) {
        mainHandler.post(() -> slideToVisibility(view, gravity, visible, force, gone));
    }

}



