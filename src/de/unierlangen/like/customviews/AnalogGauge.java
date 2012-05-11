package de.unierlangen.like.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import de.unierlangen.like.ui.R;

public final class AnalogGauge extends View {
    private static final String TAG = AnalogGauge.class.getSimpleName();
    // drawing tools
    private RectF rimRect;
    private Paint rimPaint;
    private Paint rimCirclePaint;

    private RectF faceRect;
    private Bitmap faceTexture;
    private Paint facePaint;
    private Paint rimShadowPaint;

    private Paint scalePaint;
    private RectF scaleRect;

    private Paint titlePaint;
    private Path titlePath;

    private Paint logoPaint;
    private Bitmap logo;
    private Matrix logoMatrix;
    private float logoScale;

    private Paint handPaint;
    private Path handPath;
    private Paint handScrewPaint;

    private Paint backgroundPaint;
    // end drawing tools

    private Bitmap background; // holds the cached static part

    // default scale configuration, use configureScale to change values
    /** int minValue represents lowest possible value */
    private int minValue = -20;
    /** int maxValue represents highest possible value */
    private int maxValue = 120;
    /**
     * int sweepAngle represents angle between min and max. Should be less than
     * 270
     */
    private int sweepAngle = 180;
    /** Represents how many values are between two tags */
    int tagValueRange = 10;
    /** int nicksPerTag represent amount of nicks between two tags */
    private int nicksPerTag = 2;
    /** String units is used to print unit of measurement */
    private String units = "dBm";
    // hand dynamics -- all are angular
    private boolean handInitialized = false;

    private int handCurrentPosition = minValue;
    private int value = minValue;
    private float handVelocity = 0.0f;
    private float handAcceleration = 0.0f;
    private long lastHandMoveTime = -1L;

    // Constructors, must have'em all
    public AnalogGauge(Context context) {
        super(context);
        init();
    }

    public AnalogGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        // XXX get values from XML (AnalogGauge)
        nicksPerTag = attrs.getAttributeIntValue("de.unierlangen.like.customviews.AnalogGauge",
                "nicks", 10);

        init();
    }

    public AnalogGauge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize drawing tools
     */
    private void init() {
        new Handler();
        rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);

        // the linear gradient is a bit skewed for realism
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f,
                Color.rgb(0xf0, 0xf5, 0xf0), Color.rgb(0x30, 0x31, 0x30), Shader.TileMode.CLAMP));

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        float rimSize = 0.02f;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, rimRect.right - rimSize,
                rimRect.bottom - rimSize);

        faceTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.plastic);
        BitmapShader paperShader = new BitmapShader(faceTexture, Shader.TileMode.MIRROR,
                Shader.TileMode.MIRROR);
        Matrix paperMatrix = new Matrix();
        facePaint = new Paint();
        facePaint.setFilterBitmap(true);
        paperMatrix.setScale(1.0f / faceTexture.getWidth(), 1.0f / faceTexture.getHeight());
        paperShader.setLocalMatrix(paperMatrix);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setShader(paperShader);

        rimShadowPaint = new Paint();
        rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f, new int[] {
                0x00000000, 0x00000500, 0x50000500 }, new float[] { 0.96f, 0.96f, 0.99f },
                Shader.TileMode.MIRROR));
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setColor(0x9f004d0f);
        scalePaint.setStrokeWidth(0.005f);
        scalePaint.setAntiAlias(true);

        scalePaint.setTextSize(0.045f);
        scalePaint.setTypeface(Typeface.SANS_SERIF);
        scalePaint.setTextScaleX(0.8f);
        scalePaint.setTextAlign(Paint.Align.CENTER);

        float scalePosition = 0.10f;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition, faceRect.right
                - scalePosition, faceRect.bottom - scalePosition);

        titlePaint = new Paint();
        titlePaint.setColor(0xaf946109);
        titlePaint.setAntiAlias(true);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(0.05f);
        titlePaint.setTextScaleX(0.8f);

        titlePath = new Path();
        titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);

        logoPaint = new Paint();
        logoPaint.setFilterBitmap(true);
        logo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo);
        logoMatrix = new Matrix();
        logoScale = (1.0f / logo.getWidth()) * 0.3f;
        ;
        logoMatrix.setScale(logoScale, logoScale);

        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setColor(0xff392f2c);
        handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
        handPaint.setStyle(Paint.Style.FILL);

        handPath = new Path();
        handPath.moveTo(0.5f, 0.5f + 0.2f);
        handPath.lineTo(0.5f - 0.010f, 0.5f + 0.2f - 0.007f);
        handPath.lineTo(0.5f - 0.002f, 0.5f - 0.32f);
        handPath.lineTo(0.5f + 0.002f, 0.5f - 0.32f);
        handPath.lineTo(0.5f + 0.010f, 0.5f + 0.2f - 0.007f);
        handPath.lineTo(0.5f, 0.5f + 0.2f);
        handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);

        handScrewPaint = new Paint();
        handScrewPaint.setAntiAlias(true);
        handScrewPaint.setColor(0xff493f3c);
        handScrewPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }

    /**
     * Interface method of this view
     * 
     * @param value
     *            Value to indicate. Usually from 0 to 100
     */
    public void setHandTarget(int value) {
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }
        this.value = value;
        handInitialized = true;
        invalidate();
    }

    // Drawing on canvas methods, used in onDraw
    private void drawRim(Canvas canvas) {
        // first, draw the metallic body
        canvas.drawOval(rimRect, rimPaint);
        // now the outer rim circle
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(faceRect, facePaint);
        // draw the inner rim circle
        canvas.drawOval(faceRect, rimCirclePaint);
        // draw the rim shadow inside the face
        canvas.drawOval(faceRect, rimShadowPaint);
    }

    private void drawScale(Canvas canvas) {
        // canvas.drawOval(scaleRect, scalePaint);
        // Okay, here we go
        /**
         * Angle=0 is 3 o'clock. -90 offset for 12 o'clock and half-a-sweepAngle
         * to get to position, which corresponds with minValue.
         */
        canvas.drawArc(scaleRect, -sweepAngle / 2 - 90, sweepAngle, false, scalePaint);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        float y1 = scaleRect.top;
        float y2 = y1 - 0.020f;

        /* Draw values */
        // rotate canvas to initial position
        canvas.rotate(-sweepAngle / 2, 0.5f, 0.5f);
        /** Describes angle between two adjacent tags */
        float degreesPerTag = 1.0f * sweepAngle / (maxValue - minValue) * tagValueRange;
        // for each value which requires a tag printed
        for (int tagValue = minValue; tagValue <= maxValue; tagValue += tagValueRange) {
            String valueString = Integer.toString(tagValue);
            canvas.drawText(valueString, 0.5f, y2 - 0.015f, scalePaint);
            canvas.rotate(degreesPerTag, 0.5f, 0.5f);
        }
        canvas.restore();
        /* Draw nicks */
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        // rotate canvas to initial position
        canvas.rotate(-sweepAngle / 2, 0.5f, 0.5f);
        /** How many values to skip before next nick */
        int valuesPerNick = tagValueRange / nicksPerTag;
        float degreesPerNick = 1.0f * sweepAngle / (maxValue - minValue) * valuesPerNick;
        for (int tagValue = minValue; tagValue <= maxValue; tagValue += valuesPerNick) {
            canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);
            canvas.rotate(degreesPerNick, 0.5f, 0.5f);
        }
        canvas.restore();
    }

    private void drawValue(Canvas canvas) {
        String title = Integer.toString(value) + " " + units;
        canvas.drawTextOnPath(title, titlePath, 0.0f, 0.0f, titlePaint);
    }

    private void drawLogo(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(0.5f - logo.getWidth() * logoScale / 2.0f, 0.5f - logo.getHeight()
                * logoScale / 2.0f);

        int filterColor = 0;
        /** Calculates value relative to range */
        float position;
        int centerDegree = (maxValue + minValue) / 2;

        if (handCurrentPosition < centerDegree) {
            position = 1f * (centerDegree - handCurrentPosition) / (centerDegree - minValue);
            filterColor |= (int) ((0xf0) * position); // blue
        } else {
            position = 1f * (handCurrentPosition - centerDegree) / (maxValue - centerDegree);
            filterColor |= ((int) ((0xf0) * position)) << 16; // red
        }

        logoPaint.setColorFilter(new LightingColorFilter(0xff338822, filterColor));

        canvas.drawBitmap(logo, logoMatrix, logoPaint);
        canvas.restore();
    }

    private void drawHand(Canvas canvas) {
        if (handInitialized) {
            float handAngle = valueToAngle(handCurrentPosition);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(handAngle, 0.5f, 0.5f);
            canvas.drawPath(handPath, handPaint);
            canvas.restore();

            canvas.drawCircle(0.5f, 0.5f, 0.01f, handScrewPaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (background == null) {
            Log.w(TAG, "Background not created");
        } else {
            canvas.drawBitmap(background, 0, 0, backgroundPaint);
        }
    }

    /**
     * Calculates angle according to input value
     * 
     * @param value
     * @return float angle between -sweepAngle/2 and +sweepAngle/2
     */
    private float valueToAngle(int value) {
        float offset = -sweepAngle / 2F;// offset to the left
        return (1.0f * (value - minValue) / (maxValue - minValue) * sweepAngle + offset);
    }

    /** Decides whether there is a need to move the hand */
    private boolean handNeedsToMove() {
        return Math.abs(handCurrentPosition - value) > 0.01f;
    }

    /** Hand movement */
    private void moveHand() {
        if (!handNeedsToMove()) {
            return;
        }

        if (lastHandMoveTime != -1L) {
            long currentTime = System.currentTimeMillis();
            float delta = (currentTime - lastHandMoveTime) / 1000.0f;

            float direction = Math.signum(handVelocity);
            if (Math.abs(handVelocity) < 90.0f) {
                handAcceleration = 5.0f * (value - handCurrentPosition);
            } else {
                handAcceleration = 0.0f;
            }
            handCurrentPosition += handVelocity * delta;
            handVelocity += handAcceleration * delta;
            if ((value - handCurrentPosition) * direction < 0.01f * direction) {
                handCurrentPosition = value;
                handVelocity = 0.0f;
                handAcceleration = 0.0f;
                lastHandMoveTime = -1L;
            } else {
                lastHandMoveTime = System.currentTimeMillis();
            }
            invalidate();
        } else {
            lastHandMoveTime = System.currentTimeMillis();
            moveHand();
        }
    }

    // Override View's methods
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
        Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            widthSize = 300;
        }
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            heightSize = 300;
        }

        int chosenDimension = Math.min(widthSize, heightSize);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);// XXX peek what is it
                                             // (AnalogGauge)
        canvas.scale(getWidth(), getHeight());

        drawLogo(canvas);
        drawHand(canvas);
        drawValue(canvas);

        canvas.restore();

        if (handNeedsToMove()) {
            moveHand();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "Size changed to " + w + "x" + h);
        // redraw all constant graphics
        // free the old bitmap
        if (background != null) {
            background.recycle();
        }
        background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(background);
        backgroundCanvas.scale(getWidth(), getHeight());

        drawRim(backgroundCanvas);
        drawFace(backgroundCanvas);
        drawScale(backgroundCanvas);
    }

}
