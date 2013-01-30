package de.unierlangen.like.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GestureDetector {
    private static final int INVALID_POINTER_ID = -1;
    private static final float MAX_SCALE_FACTOR = 70.0f;
    private static final float MIN_SCALE_FACTOR = 6f;
    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mLastFocusX;
    private float mLastFocusY;
    private final ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = MIN_SCALE_FACTOR;

    private float baseScaleFactor = 1.0f;
    private float baseX;
    private float baseY;

    private final View mView;

    /**
     * TODO remove dependency on View
     * 
     * @param context
     * @param view
     */
    public GestureDetector(Context context, View view) {
        mView = view;
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mScaleFactor *= mScaleDetector.getScaleFactor();
    }

    public void applyTransitions(Canvas canvas) {
        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.translate(mPosX / mScaleFactor, mPosY / mScaleFactor);
    }

    public void setBaseScaleFactor(float baseScaleFactor) {
        // remove old
        mScaleFactor /= this.baseScaleFactor;
        // apply new
        mScaleFactor *= baseScaleFactor;
        this.baseScaleFactor = baseScaleFactor;
    }

    public void setBaseTranslation(float positionX, float positionY) {
        // remove old base translation
        mPosX -= this.baseX;
        mPosY -= this.baseY;
        // apply new base translation
        mPosX += positionX;
        mPosY += positionY;

        this.baseX = positionX;
        this.baseY = positionY;

    }

    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();

            mLastTouchX = x;
            mLastTouchY = y;
            mActivePointerId = ev.getPointerId(0);
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            final float dx;
            final float dy;
            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!mScaleDetector.isInProgress()) {
                dx = x - mLastTouchX;
                dy = y - mLastTouchY;
            } else {
                dx = mScaleDetector.getFocusX() - mLastFocusX;
                dy = mScaleDetector.getFocusY() - mLastFocusY;
                mLastFocusX = mScaleDetector.getFocusX();
                mLastFocusY = mScaleDetector.getFocusY();
            }

            mLastTouchX = x;
            mLastTouchY = y;

            mPosX += dx;
            mPosY += dy;

            mView.invalidate();
            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
            }
            break;
        }
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            // TODO make this configurable
            mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

            mView.invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mLastFocusX = detector.getFocusX();
            mLastFocusY = detector.getFocusY();
            return true;
        }
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public float getXTranslation() {
        return mPosX;
    }

    public float getYTranslation() {
        return mPosY;
    }
}
