package com.example.harbon.test;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harbon on 15-6-17.
 */
public class EditTaskListView extends ScrollView{

    private BaseAdapter mBaseAdapter;
    private LinearLayout mLinearLayout;
    private OnItemClickListener mOnItemClickListener;
    private int[] addArray;
    private int[] deleteArray;
    private int mItemHeight;
    private int mItemWidth;


    public EditTaskListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mLinearLayout, lp);
    }


    public void setAdapter(BaseAdapter baseAdapter) {
        this.mBaseAdapter = baseAdapter;
    }

    public void notifyDataSetChanged(int[] addArray, int[] deleteArray) {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        for (int i = 0; i < addArray.length; i++) {
            View convertView = mBaseAdapter.getView(addArray[i], null, null);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(0, 0);
            mLinearLayout.addView(convertView, addArray[i], lp);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, mItemHeight);
            valueAnimator.setInterpolator(new AccelerateInterpolator(2f));
            valueAnimator.addUpdateListener(new CustomAnimatorUpdateListener(convertView));
            animators.add(valueAnimator);
        }
        if (addArray.length > 0) {
            List<Integer> adds = new ArrayList<Integer>();
            for (int i = 0; i < addArray.length; i++) {
                adds.add(addArray[i]);
            }
            for (int i = addArray[0]+1; i < mLinearLayout.getChildCount(); i++) {
                if (!adds.contains(i)) {
                    View convertView = mLinearLayout.getChildAt(i);
                    mBaseAdapter.getView(i, convertView, null);
                }
            }
        }

        for (int i = 0; i < deleteArray.length; i++) {
            View convertView = mLinearLayout.getChildAt(deleteArray[i]);
            if (convertView != null) {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(convertView.getHeight(), 0);
                valueAnimator.addUpdateListener(new CustomAnimatorUpdateListener(convertView));
                valueAnimator.addListener(new CustomAnimatorListener(convertView));
                animators.add(valueAnimator);
            }
        }
        if (deleteArray.length > 0) {
            List<Integer> deletes = new ArrayList<Integer>();
            for (int i = 0; i < deleteArray.length; i++) {
                deletes.add(deleteArray[i]);
            }
            for (int i = deleteArray[0] + 1; i < mLinearLayout.getChildCount(); i++) {

                if (!deletes.contains(i)) {
                    int deleteItemCountBefore = 0;
                    for (int j = 0; j < deletes.size(); j++) {
                        if (i > deletes.get(j)) {
                            deleteItemCountBefore++;
                        }
                    }
                    View convertView = mLinearLayout.getChildAt(i);
                    mBaseAdapter.getView(i - deleteItemCountBefore, convertView, null);
                }
            }
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.start();

    }
    public void removeViewByItemType(int type) {
        int totalItemDataCount = mBaseAdapter.getCount();
        int totalItemViewCount = mLinearLayout.getChildCount();
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        if (totalItemDataCount != totalItemViewCount) {
//            notifyDataSetChanged();
        }
        for (int i = 0; i < totalItemDataCount; i++) {
            int viewType = mBaseAdapter.getItemViewType(i);
            if (viewType == type) {
                View view = mLinearLayout.getChildAt(i);
                ValueAnimator valueAnimator = ValueAnimator.ofInt(view.getHeight(), 0);
                valueAnimator.addUpdateListener(new CustomAnimatorUpdateListener(view));
                animators.add(valueAnimator);
            }
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private class CustomAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private View mAnimateView;
        public CustomAnimatorUpdateListener(View view) {
            mAnimateView = view;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int height = (int)animation.getAnimatedValue();
            mAnimateView.getLayoutParams().height = height;
            mAnimateView.requestLayout();
        }
    }
    private class CustomAnimatorListener implements Animator.AnimatorListener {
        private View mAnimateView;
        public CustomAnimatorListener(View view) {
            this.mAnimateView = view;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mLinearLayout.removeView(mAnimateView);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
    public void addItemAfterPosition(int position, int number) {
        if (getChildAt(position) == null) {
            return;
        }
        int typeCount = number;
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        int height = getChildAt(position).getHeight();
        for (int i = 0; i < typeCount; i++) {
            View convertView = mBaseAdapter.getView(position + i + 1, null, null);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            mLinearLayout.addView(convertView, position + i +1, lp);
        }
        for (int i = 0; i <typeCount; i++) {
            View view = getChildAt(position + i + 1);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, height);
            valueAnimator.addUpdateListener(new CustomAnimatorUpdateListener(view));
            animators.add(valueAnimator);
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    public void deleteItemAByPosition(int position, int number) {
        if (getChildAt(position) == null) {
            return;
        }
        int typeCount = number;
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        for (int i = 0; i <typeCount; i++) {
            View view = getChildAt(position + i);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(view.getHeight(), 0);
            valueAnimator.addUpdateListener(new CustomAnimatorUpdateListener(view));
            animators.add(valueAnimator);
        }
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.start();
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        public void OnItemClick(Adapter adapter, View view, int position);
    }

}
