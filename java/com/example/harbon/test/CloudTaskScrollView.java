package com.example.harbon.test;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by harbon on 15-6-22.
 */
public class CloudTaskScrollView extends ScrollView {
    public CloudTaskScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:break;
            case MotionEvent.ACTION_MOVE:break;
            case MotionEvent.ACTION_UP:break;
        }
        FrameLayout container = (FrameLayout) getChildAt(0);
        CloudTaskGridView gridView = (CloudTaskGridView) container.getChildAt(0);
        if (gridView.isDragMode) {
            return false;
        }else {
            boolean b = super.onInterceptTouchEvent(ev);
            if (b) {
                gridView.mCountDownTimer.cancel();
            }
            return b;
        }
    }

}
