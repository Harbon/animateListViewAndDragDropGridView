package com.example.harbon.test;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class MainActivity extends ActionBarActivity {
    private CloudTaskGridView mCloudTaskGridView;
    private CloudTaskAdapter mCloudTaskAdapter;
    private ScrollView mScrollView;
    private FrameLayout mContainer;
    private FrameLayout mRootViewGroup;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCloudTaskGridView = (CloudTaskGridView) findViewById(R.id.gridView);
        mContainer = (FrameLayout) findViewById(R.id.container);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mRootViewGroup = (FrameLayout) findViewById(R.id.rootViewGroup);
        mCloudTaskGridView.setContainer(mContainer);
        mCloudTaskGridView.setRootViewGroup(mRootViewGroup);
        mCloudTaskGridView.setScrollView(mScrollView);
        mCloudTaskAdapter = new CloudTaskAdapter(this);
        mCloudTaskGridView.setAdapter(mCloudTaskAdapter);
        mCloudTaskGridView.setOnDragListener(new CloudTaskGridView.OnDragListener() {
            @Override
            public void start() {
                Log.i("aaaaaa", "start");
            }

            @Override
            public void move(float x, float y) {
                Log.i("aaaaaa", "move");
            }

            @Override
            public void end(int oldPosition, int newPosition) {
                Log.i("aaaaaa", "old:"+oldPosition+"    new:"+newPosition);
                mCloudTaskAdapter.notifyDataSetChanged();
            }
        });
        mCloudTaskAdapter.notifyDataSetChanged();
    }

}
