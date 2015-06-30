package com.example.harbon.test;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.Image;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by harbon on 15-6-18.
 */
public class CloudTaskGridView extends ViewGroup{
    public static final int INVALID_POSITION = -1;
    private int gapWidth;
    private int columnNum;
    private Rect mTouchFrame;
    private int mRawChildCount = 0;
    private BaseAdapter mBaseAdapter;
    private ScrollView mScrollView;
    private float mDownX;
    private float mDownY;
    private int mDragPosition;
    private int mRawDragPosition;
    private float mPointToItemLeft;
    private float mPointToItemTop;
    private float mItemLeft;
    private float mItemTop;
    private int mTouchSlop;
    public boolean isDragMode;
    private HashMap mMap;
    private View mFakeDragItemView;
    private FrameLayout mContainer;
    private FrameLayout mRootViewGroup;
    private int mToolBarHeight;
    private int mContainerRawChildCount;
    private OnDragListener mOnDragListener;
    public CountDownTimer mCountDownTimer = new CountDownTimer(500, 500) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            startDragMode();
        }
    };
    public CloudTaskGridView(Context context) {
        super(context);
    }

    public CloudTaskGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudTaskGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CloudTaskGridView, defStyleAttr, 0);
        gapWidth = typedArray.getDimensionPixelSize(R.styleable.CloudTaskGridView_gapWidth, dpToPx(16, context.getResources()));
        columnNum = typedArray.getInteger(R.styleable.CloudTaskGridView_columnNum, 2);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMap = new HashMap();
        setBackgroundColor(Color.parseColor("#d6dee9"));
    }

    public void setAdapter(BaseAdapter baseAdapter) {
        this.mBaseAdapter = baseAdapter;
        mRawChildCount = mBaseAdapter.getCount();
        mBaseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                forceLayout();
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = gapWidth;
        if (widthSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }
        int childWidth = (width - gapWidth*(columnNum +1))/columnNum;
        for (int i = 0; i < mRawChildCount; i++) {
            View convertView = getChildAt(i);
            if (convertView == null) {
                convertView = mBaseAdapter.getView(i, null, null);
                addView(convertView);
            }else {
                convertView = mBaseAdapter.getView(i, convertView, null);
                if (!isDragMode) {
                    convertView.setVisibility(View.VISIBLE);
                }
            }
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);
            convertView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if ((i + 1) % 2 == 1) {
                height += (convertView.getMeasuredHeight() + gapWidth);
            }
        }

        if (getChildCount() > mRawChildCount) {
            for (int i = mRawChildCount; i < getChildCount(); i++) {
                removeViewAt(mRawChildCount);
            }
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = gapWidth;
        int top = gapWidth;
        for (int i = 0; i < mRawChildCount; i++) {

            View convertView = getChildAt(i);
            int childWidth = convertView.getMeasuredWidth();
            int childHeight = convertView.getMeasuredHeight();
            convertView.layout(left, top, left + childWidth, top + childHeight);
            if ((i+1)%columnNum == 0) {
                top += childHeight + gapWidth;
                left = gapWidth;
            }else {
                left += childWidth +gapWidth;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                isDragMode = false;
                mCountDownTimer.cancel();
                mCountDownTimer.start();
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetX = Math.abs(x - mDownX);
                float offsetY = Math.abs(y - mDownY);
                boolean xMoved = offsetX > mTouchSlop;
                boolean yMoved = offsetY > mTouchSlop;
                if (xMoved || yMoved) {
                    mCountDownTimer.cancel();
                    isDragMode = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCountDownTimer.cancel();
                if (isDragMode) {
                    endDragMode();
                }
                break;

        }
        return  isDragMode;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isDragMode) {
            float x = ev.getX();
            float y = ev.getY();
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    dragMove(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mCountDownTimer.cancel();
                    if (isDragMode) {
                        endDragMode();
                    }
                    break;

            }
        }
        return  true;

    }

    public static int dpToPx(float dp, Resources resources){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public int pointToPosition(float x, float y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        for (int i = mRawChildCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains((int)x, (int)y)) {
                return  i;
            }
        }
        return INVALID_POSITION;
    }


    private void startDragMode(){

        mMap.clear();
        isDragMode = true;
        mDragPosition = pointToPosition(mDownX, mDownY);
        mRawDragPosition = mDragPosition;
        mContainerRawChildCount = mContainer.getChildCount();
        for (int i = 0; i < getChildCount(); i ++) {
            View view = getChildAt(i);
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(bitmap);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mContainer.addView(imageView, lp);
            imageView.setX(view.getLeft());
            imageView.setY(view.getTop());
            if (i == mDragPosition) {
                imageView.setVisibility(View.INVISIBLE);
                Bitmap bitmap_fake = Bitmap.createBitmap(view.getDrawingCache());
                ImageView imageView_fake = new ImageView(getContext());
                imageView_fake.setImageBitmap(bitmap_fake);
                mFakeDragItemView  = imageView_fake;
                mItemLeft = view.getLeft();
                mItemTop = view.getTop();
                mPointToItemLeft = mDownX - mItemLeft;
                mPointToItemTop = mDownY - mItemTop;
                LayoutParams lp_fake = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mRootViewGroup.addView(mFakeDragItemView, lp_fake);
                mFakeDragItemView.setX(mItemLeft);
                mFakeDragItemView.setY(mItemTop + mToolBarHeight - mScrollView.getScrollY());

            }
            view.setVisibility(View.INVISIBLE);
            mMap.put(i, i);
        }
        mFakeDragItemView.animate().scaleX(1.2f).scaleY(1.2f).start();
        if (mOnDragListener != null) {
            mOnDragListener.start();
        }
    }
    private void dragMove(float x, float y) {
        mFakeDragItemView.setX(x - mPointToItemLeft);
        mFakeDragItemView.setY(y + mToolBarHeight - mPointToItemTop - mScrollView.getScrollY());
        int position = pointToPosition(x, y);
        if (position != ListView.INVALID_POSITION && position != mDragPosition) {
            AnimatorSet animatorSet= createAnimators(mDragPosition, position);
            animatorSet.start();
        }
        if (mOnDragListener != null) {
            mOnDragListener.move(x, y);
        }
    }
    private void endDragMode(){
        isDragMode = false;
        View view = getChildAt(mDragPosition);
        mFakeDragItemView.animate().x(view.getLeft()).y(view.getTop() + mToolBarHeight - mScrollView.getScrollY()).scaleX(1.0f).scaleY(1.0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnDragListener != null) {
                    mOnDragListener.end(mRawDragPosition, mDragPosition);
                }
                int totalSize = mContainer.getChildCount();
                for (int i = mContainerRawChildCount; i < totalSize; i++) {
                    mContainer.removeViewAt(mContainerRawChildCount);
                }
                mRootViewGroup.removeView(mFakeDragItemView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }
    private AnimatorSet createAnimators(int oldPosition, int newPosition) {
        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new ArrayList<Animator>();
        if (oldPosition > newPosition) {
            for (int i = oldPosition - 1; i >= newPosition; i--) {
                View view = getChildAt(i + 1);
                int x = view.getLeft();
                int y = view.getTop();
                View fakeView = getFakeImageView((Integer) mMap.get(i));
                ObjectAnimator objectAnimator_x = ObjectAnimator.ofFloat(fakeView, "x", x);
                ObjectAnimator objectAnimator_y = ObjectAnimator.ofFloat(fakeView, "y", y);
                animators.add(objectAnimator_x);
                animators.add(objectAnimator_y);
                mMap.put(i+1, mMap.get(i));
            }
            mMap.put(newPosition, mMap.get(oldPosition));
            mDragPosition = newPosition;
        }else {
            for (int i = oldPosition + 1; i <=newPosition; i++) {
                View view = getChildAt(i - 1);
                int x = view.getLeft();
                int y = view.getTop() ;
                View fakeView = getFakeImageView((Integer) mMap.get(i));
                ObjectAnimator objectAnimator_x = ObjectAnimator.ofFloat(fakeView, "x", x);
                ObjectAnimator objectAnimator_y = ObjectAnimator.ofFloat(fakeView, "y", y);
                animators.add(objectAnimator_x);
                animators.add(objectAnimator_y);
                mMap.put(i - 1, mMap.get(i));
            }
            mMap.put(newPosition, mMap.get(oldPosition));
            mDragPosition = newPosition;
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        return  animatorSet;
    }

    private View  getFakeImageView(int position) {
        return mContainer.getChildAt(position + mContainerRawChildCount);
    }
    public void setContainer(FrameLayout container) {
        this.mContainer = container;
    }
    public void setRootViewGroup(FrameLayout viewGroup) {
        this.mRootViewGroup = viewGroup;
    }
    public void setScrollView(ScrollView scrollView) {
        this.mScrollView = scrollView;
    }

    public interface OnDragListener{
        public void start();
        public void move(float x, float y);
        public void end(int oldPosition, int newPosition);
    }

    public void setOnDragListener(OnDragListener l) {
        this.mOnDragListener = l;
    }

}
