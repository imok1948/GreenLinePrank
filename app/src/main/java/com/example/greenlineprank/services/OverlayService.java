package com.example.greenlineprank.services;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.greenlineprank.other.PrankSettings;
import com.example.greenlineprank.other.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OverlayService extends Service {
  private WindowManager mWindowManager;
  private View mOverlayView;
  private Runnable mShowOverlayRunnable;
  private Handler mHandler = new Handler();
  private List<View> overlayViews = new ArrayList<>();

  @Override
  public void onCreate() {
    super.onCreate();
    mHandler = new Handler();
  }

  private void printExtras(Intent intent) {
    if (intent != null) {
      Bundle extras = intent.getExtras();
      if (extras != null) {
        for (String key : extras.keySet()) {
          Object value = extras.get(key);
          String s = "Extra key: " + key + ", value: " + value;
          Log.d("EXTRAS", s);
        }
      } else {
        Log.d("EXTRAS", "Extra == Null");
      }
    }
  }

  private void showSingleLine(int color, int width, int xAxis, int delayInSeconds) {
    int delayInMillis = delayInSeconds * 1000;
    mOverlayView = new View(this);
    mOverlayView.setBackgroundColor(color);
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
    params.x = xAxis;
    params.y = Utilities.Y_NEGATIVE;
    params.width = width;
    params.height = Utilities.SINGLE_LINE_HEIGHT;
    mShowOverlayRunnable = () -> mWindowManager.addView(mOverlayView, params);
    mHandler.postDelayed(mShowOverlayRunnable, delayInMillis);
  }

  private void showMultipleOverlays(int[] colors, int[] widths, int[] xAxes, int delay) {
    clearAllOverlays();
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    int n = Math.min(colors.length, Math.min(widths.length, xAxes.length));
    Runnable showOverlaysRunnable = () -> {
      for (int i = 0; i < n; i++) {
        View overlayView = new View(this);
        overlayViews.add(overlayView);
        overlayView.setBackgroundColor(colors[i]);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(widths[i], WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        params.x = xAxes[i];
        params.y = Utilities.Y_NEGATIVE;
        params.width = widths[i];
        params.height = Utilities.SINGLE_LINE_HEIGHT;
        if (mWindowManager != null) {
          mWindowManager.addView(overlayView, params);
        }
      }
    };
    int delayInMillis = delay * 1000;
    mHandler.postDelayed(showOverlaysRunnable, delayInMillis);
  }

  private void clearAllOverlays() {
    for (View overlayView : overlayViews) {
      if (mWindowManager != null && overlayView.isAttachedToWindow()) {
        mWindowManager.removeView(overlayView);
      }
    }
    overlayViews.clear();
    mHandler.removeCallbacksAndMessages(null);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    int lineType = intent != null ? intent.getIntExtra(Utilities.EXTRA_TYPE_OF_LINE, 0) : 0;
    int lineNumbers = intent != null ? intent.getIntExtra(Utilities.EXTRA_NUMBER_OF_LINES, Utilities.DEFAULT_NUMBER_OF_LINES) : Utilities.DEFAULT_NUMBER_OF_LINES;
    float lineLocation = intent != null ? intent.getFloatExtra(Utilities.EXTRA_LOCATION_OF_LINE, 0) : 0;
    int lineColor = intent != null ? intent.getIntExtra(Utilities.EXTRA_LINE_COLOR, Utilities.LINE_DEFAULT_COLOR) : Utilities.LINE_DEFAULT_COLOR;
    int delayToStart = intent != null ? intent.getIntExtra(Utilities.EXTRA_LINE_DELAY, Utilities.DEFAULT_LINE_DELAY) : Utilities.DEFAULT_LINE_DELAY;


    int lineLocationOnXAxis = (int) (lineLocation * Utilities.getDisplayLastX(getApplicationContext()));

    if (lineNumbers != PrankSettings.RANDOM_LINES) {
      showMultipleOverlays(Utilities.getSameColors(lineNumbers, lineColor), Utilities.getSameWidth(lineNumbers), Utilities.getRandomLocations(lineNumbers, lineLocationOnXAxis), delayToStart);
    } else {
      int randomLines = Utilities.getRandomInt(7);
      int screenWidth = Utilities.getDisplayWidth(getApplicationContext());

      int eightyPercentScreenWidth = (int) (screenWidth * 0.80f);
      int randomLineLocation = Utilities.getRandomInt(eightyPercentScreenWidth);
      showMultipleOverlays(Utilities.getRandomColors(randomLines), Utilities.getSameWidth(randomLines), Utilities.getRandomLocations(randomLines, randomLineLocation), delayToStart);
    }
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    clearAllOverlays();

    if (mOverlayView != null && mOverlayView.isAttachedToWindow()) {
      mWindowManager.removeView(mOverlayView);
    }
    if (mHandler != null && mShowOverlayRunnable != null) {
      mHandler.removeCallbacks(mShowOverlayRunnable);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}