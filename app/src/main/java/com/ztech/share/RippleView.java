package com.ztech.share;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RippleView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static class Ripple {
        private AnimatorSet mAnimatorSet;
        private ValueAnimator mRadiusAnimator;
        private ValueAnimator mAlphaAnimator;
        private Paint mPaint;

        Ripple(float startRadiusFraction, float stopRadiusFraction, float startAlpha,
               float stopAlpha, int color, long delay, long duration, float strokeWidth,
               ValueAnimator.AnimatorUpdateListener updateListener){
            mRadiusAnimator = ValueAnimator.ofFloat(startRadiusFraction, stopRadiusFraction);
            mRadiusAnimator.setDuration(duration);
            mRadiusAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mRadiusAnimator.addUpdateListener(updateListener);
            mRadiusAnimator.setInterpolator(new DecelerateInterpolator());

            mAlphaAnimator = ValueAnimator.ofFloat(startAlpha, stopAlpha);
            mAlphaAnimator.setDuration(duration);
            mAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAlphaAnimator.addUpdateListener(updateListener);
            mAlphaAnimator.setInterpolator(new DecelerateInterpolator());

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(mRadiusAnimator, mAlphaAnimator);
            mAnimatorSet.setStartDelay(delay);

            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(color);
            mPaint.setAlpha((int)(255*startAlpha));
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(strokeWidth);
        }

        public void setColorString(String colorString) {
            mPaint.setColor(Color.parseColor(colorString));
        }

        void draw(Canvas canvas, int centerX, int centerY, float radiusMultiplication){
            mPaint.setAlpha( (int)(255*(float)mAlphaAnimator.getAnimatedValue()));
            canvas.drawCircle(centerX, centerY, (float)mRadiusAnimator.getAnimatedValue()*radiusMultiplication, mPaint);
        }

        void startAnimation(){
            mAnimatorSet.start();
        }

        void stopAnimation(){
            mAnimatorSet.cancel();
        }
    }

    private List<Ripple> mRipples = new ArrayList<>();

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if( isInEditMode() )
            return;
        mRipples = new ArrayList<>();
        mRipples.add(new Ripple(0.0f, 1.0f, 1.0f, 0.0f, Color.BLUE, 0, 4000, 10, this));
        mRipples.add(new Ripple(0.0f, 1.0f, 1.0f, 0.0f, Color.BLUE, 1500, 4000, 10, this));
    }


    public void startAnimation(String colorString) {
        setVisibility(View.VISIBLE);
        for (Ripple ripple : mRipples) {
            ripple.setColorString(colorString);
            ripple.startAnimation();
        }
    }

    public void stopAnimation() {
        for (Ripple ripple : mRipples) {
            ripple.stopAnimation();
        }
        setVisibility(View.GONE);
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerX = getWidth()/2;
        int centerY = getHeight()/2;
        int radiusMultiplication = getWidth()/2;
        for (Ripple ripple : mRipples) {
            ripple.draw(canvas, centerX, centerY, radiusMultiplication);
        }
    }
}