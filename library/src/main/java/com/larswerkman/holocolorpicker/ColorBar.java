package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public abstract class ColorBar extends View{

    /**
     * Constants used to save/restore the instance state.
     */
    public static final String STATE_PARENT = "parent";
    public static final String STATE_COLOR = "color";
    public static final String STATE_POSITION = "position";

    /**
     * Constants used to identify type of bar.
     */
    public static final int HUE = 0;
    public static final int SATURATION = 1;
    public static final int BRIGHTNESS = 2;

    /**
     * Constants used to identify orientation.
     */
    public static final boolean ORIENTATION_HORIZONTAL = true;
    private static final boolean ORIENTATION_VERTICAL = false;

    /**
     * Default orientation of the bar.
     */
    private static final boolean ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;

    /**
     * The thickness of the bar.
     */
    public int mBarThickness;

    /**
     * The length of the bar.
     */
    public int mBarLength;
    public int mPreferredBarLength;

    /**
     * The radius of the pointer.
     */
    public int mBarPointerRadius;

    /**
     * The radius of the halo of the pointer.
     */
    public int mBarPointerHaloRadius;

    /**
     * The position of the pointer on the bar.
     */
    public int mBarPointerPosition;

    /**
     * {@code Paint} instance used to draw the bar.
     */
    public Paint mBarPaint;

    /**
     * {@code Paint} instance used to draw the pointer.
     */
    public Paint mBarPointerPaint;

    /**
     * {@code Paint} instance used to draw the halo of the pointer.
     */
    private Paint mBarPointerHaloPaint;

    /**
     * The rectangle enclosing the bar.
     */
    public RectF mBarRect = new RectF();

    /**
     * {@code Shader} instance used to fill the shader of the paint.
     */
    public Shader shader;

    /**
     * The ARGB value of the currently selected color.
     */
    public int mColor;

    /**
     * An array of floats that can be build into a {@code Color} <br>
     * Where we can extract the color from.
     */
    public float[] mHSVColor = new float[3];

    /**
     * Factor used to calculate the position to the Saturation on the bar.
     */
    private float mPosToSatFactor;

    /**
     * Factor used to calculate the Saturation to the postion on the bar.
     */
    private float mSatToPosFactor;

    /**
     * {@code ColorPicker} instance used to control the ColorPicker.
     */
    public ColorPicker mPicker = null;

    /**
     * Used to toggle orientation between vertical and horizontal.
     */
    public boolean mOrientation;

    private int mBarType;


    public ColorBar(Context context, int barType) {
        super(context);
        mBarType = barType;
        init(null, 0);
    }

    public ColorBar(Context context, AttributeSet attrs, int barType) {
        super(context, attrs);
        mBarType = barType;
        init(attrs, 0);
    }

    public ColorBar(Context context, AttributeSet attrs, int defStyle, int barType) {
        super(context, attrs, defStyle);
        mBarType = barType;
        init(attrs, defStyle);
    }

    public void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ColorBar, defStyle, 0);
        final Resources b = getContext().getResources();

        mBarThickness = a.getDimensionPixelSize(
                R.styleable.ColorBar_bar_thickness,
                b.getDimensionPixelSize(R.dimen.bar_thickness));
        mBarLength = a.getDimensionPixelSize(R.styleable.ColorBar_bar_length,
                b.getDimensionPixelSize(R.dimen.bar_length));
        mPreferredBarLength = mBarLength;
        mBarPointerRadius = a.getDimensionPixelSize(
                R.styleable.ColorBar_bar_pointer_radius,
                b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
        mBarPointerHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorBar_bar_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
        mOrientation = a.getBoolean(
                R.styleable.ColorBar_bar_orientation_horizontal, ORIENTATION_DEFAULT);

        a.recycle();

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setShader(shader);

        mBarPointerPosition = mBarLength + mBarPointerHaloRadius;

        mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerHaloPaint.setColor(Color.BLACK);
        mBarPointerHaloPaint.setAlpha(0x50);

        mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerPaint.setColor(0xff81ff00);

        mPosToSatFactor = 1 / ((float) mBarLength);
        mSatToPosFactor = ((float) mBarLength) / 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = mPreferredBarLength
                + (mBarPointerHaloRadius * 2);

        // Variable orientation
        int measureSpec;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            measureSpec = widthMeasureSpec;
        }
        else {
            measureSpec = heightMeasureSpec;
        }
        int lengthMode = MeasureSpec.getMode(measureSpec);
        int lengthSize = MeasureSpec.getSize(measureSpec);

        int length;
        if (lengthMode == MeasureSpec.EXACTLY) {
            length = lengthSize;
        }
        else if (lengthMode == MeasureSpec.AT_MOST) {
            length = Math.min(intrinsicSize, lengthSize);
        }
        else {
            length = intrinsicSize;
        }

        int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
        mBarLength = length - barPointerHaloRadiusx2;
        if(mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(barPointerHaloRadiusx2,
                    (mBarLength + barPointerHaloRadiusx2));
        }
        else {
            setMeasuredDimension((mBarLength + barPointerHaloRadiusx2),
                    barPointerHaloRadiusx2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the bar.
        canvas.drawRect(mBarRect, mBarPaint);

        // Calculate the center of the pointer.
        int cX, cY;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition;
            cY = mBarPointerHaloRadius;
        }
        else {
            cX = mBarPointerHaloRadius;
            cY = mBarPointerPosition;
        }

        // Draw the pointer halo.
        canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
        // Draw the pointer.
        canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
    }

    /**
     * Calculate the color selected by the pointer on the bar.
     *
     * @param coord Coordinate of the pointer.
     */
    public void calculateColor(int coord) {
        coord = coord - mBarPointerHaloRadius;
        if (coord < 0) {
            coord = 0;
        } else if (coord > mBarLength) {
            coord = mBarLength;
        }

        mHSVColor[mBarType] = mPosToSatFactor * coord;

        mColor = Color.HSVToColor(mHSVColor);
    }

    /**
     * Set the bar hue. <br>
     * <br>
     * Its discouraged to use this method. (why?)
     *
     * @param hue The current value of the picker
     */
    public void setHue(float hue) {

        mHSVColor[0] = hue;

        setGradient();

        mColor = Color.HSVToColor(mHSVColor);
        mBarPointerPaint.setColor(mColor);
        invalidate();
    }

    public abstract void setGradient();

    /**
     * Get the currently selected color.
     *
     * @return The ARGB value of the currently selected color.
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Adds a {@code ColorPicker} instance to the bar. <br>
     * <br>
     * WARNING: Don't change the color picker. it is done already when the bar
     * is added to the ColorPicker
     *
     * @see com.larswerkman.holocolorpicker.ColorPicker#addSVBar(com.larswerkman.holocolorpicker.SVBar)
     * @param picker
     */
    public void setColorPicker(ColorPicker picker) {
        mPicker = picker;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloatArray(STATE_COLOR, mHSVColor);

        // float[] hsvColor = new float[3];
        // Color.colorToHSV(mColor, hsvColor);
        // state.putFloat(STATE_POSITION, mHSVColor[mBarType]);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        mHSVColor = savedState.getFloatArray((STATE_COLOR));
        mColor = Color.HSVToColor(mHSVColor);

        mBarPointerPaint.setColor(mColor);

        if (mPicker != null) {
            // mPicker.changeValueBarHue(mColor);
            // mColor = mPicker.changeOpacityBarColor(mColor);
            mPicker.setNewCenterColor(mColor);
        }

        setHue(mHSVColor[0]);

        invalidate();
    }
}
