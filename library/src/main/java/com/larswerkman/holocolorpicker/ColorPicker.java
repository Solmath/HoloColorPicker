/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Displays a holo-themed color picker.
 * 
 * <p>
 * Use {@link #getColor()} to retrieve the selected color. <br>
 * Use {@link #addSVBar(SVBar)} to add a Saturation/Value Bar. <br>
 * Use {@link #addOpacityBar(OpacityBar)} to add a Opacity Bar.
 * </p>
 */
public class ColorPicker extends View {
	/*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ANGLE = "angle";
	private static final String STATE_OLD_COLOR = "color";
	private static final String STATE_SHOW_OLD_COLOR = "showColor";

	/**
	 * Colors to construct the color wheel using {@link android.graphics.SweepGradient}.
	 */
	private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFFFF00,
			0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

	/**
	 * {@code Paint} instance used to draw the color wheel.
	 */
	private Paint mColorWheelPaint;

	/**
	 * {@code Paint} instance used to draw the pointer's "halo".
	 */
	private Paint mPointerHaloPaint;

	/**
	 * {@code Paint} instance used to draw the pointer (the selected color).
	 */
	private Paint mPointerColor;

	/**
	 * The width of the color wheel thickness.
	 */
	private int mColorWheelThickness;

	/**
	 * The radius of the color wheel.
	 */
	private int mColorWheelRadius;
	private int mPreferredColorWheelRadius;

	/**
	 * The radius of the center circle inside the color wheel.
	 */
	private int mColorCenterRadius;
	private int mPreferredColorCenterRadius;

	/**
	 * The radius of the halo of the center circle inside the color wheel.
	 */
	private int mColorCenterHaloRadius;
	private int mPreferredColorCenterHaloRadius;

	/**
	 * The radius of the pointer.
	 */
	private int mColorPointerRadius;

	/**
	 * The radius of the halo of the pointer.
	 */
	private int mColorPointerHaloRadius;

	/**
	 * The rectangle enclosing the color wheel.
	 */
	private RectF mColorWheelRectangle = new RectF();

	/**
	 * The rectangle enclosing the center inside the color wheel.
	 */
	private RectF mCenterRectangle = new RectF();

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(android.view.MotionEvent)
	 */
	private boolean mUserIsMovingPointer = false;

	/**
	 * The ARGB value of the currently selected color.
	 */
	private int mColor;

    /**
     * An array of floats that can be build into a {@code Color} <br>
     * Where we can extract the color from.
     */
    public float[] mHSVColor = new float[3];

	/**
	 * The ARGB value of the center with the old selected color.
	 */
	private int mCenterOldColor;
	
	/**
	 * Whether to show the old color in the center or not.
	 */
	private boolean mShowCenterOldColor;

	/**
	 * The ARGB value of the center with the new selected color.
	 */
	private int mCenterNewColor;

	/**
	 * Number of pixels the origin of this view is moved in X- and Y-direction.
	 * 
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal
	 * coordinate system. Android uses the upper left corner as origin for the
	 * View-specific coordinate system. So this is the value we use to translate
	 * from one coordinate system to the other.
	 * </p>
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 * 
	 * @see #onDraw(android.graphics.Canvas)
	 */
	private float mTranslationOffset;
	
	/**
	 * Distance between pointer and user touch in X-direction.
	 */
    private float mSlopX;
    
	/**
	 * Distance between pointer and user touch in Y-direction.
	 */
    private float mSlopY;

	/**
	 * The pointer's position expressed as angle (in rad).
	 */
	private float mAngle;

	/**
	 * {@code Paint} instance used to draw the center with the old selected
	 * color.
	 */
	private Paint mCenterOldPaint;

	/**
	 * {@code Paint} instance used to draw the center with the new selected
	 * color.
	 */
	private Paint mCenterNewPaint;

	/**
	 * {@code Paint} instance used to draw the halo of the center selected
	 * colors.
	 */
	private Paint mCenterHaloPaint;

	/**
	 * {@code SVBar} instance used to control the Saturation/Value bar.
	 */
	private SVBar mSVbar = null;

	/**
	 * {@code OpacityBar} instance used to control the Opacity bar.
	 */
	private OpacityBar mOpacityBar = null;

	/**
	 * {@code SaturationBar} instance used to control the Saturation bar.
	 */
	private SaturationBar mSaturationBar = null;

    /**
     * {@code TouchAnywhereOnColorWheelEnabled} instance used to control <br>
     * if the color wheel accepts input anywhere on the wheel or just <br>
     * on the halo.
     */
    private boolean mTouchAnywhereOnColorWheelEnabled = true;

	/**
	 * {@code ValueBar} instance used to control the Value bar.
	 */
	private ValueBar mValueBar = null;

	/**
	 * {@code onColorChangedListener} instance of the onColorChangedListener
	 */
	private OnColorChangedListener onColorChangedListener;

	/**
	 * {@code onColorChangedListener} instance of the onColorChangedListener
	 */
	private OnHueChangedListener onHueChangedListener;

	/**
	 * {@code onColorSelectedListener} instance of the onColorSelectedListener
	 */
	private OnColorSelectedListener onColorSelectedListener;

	public ColorPicker(Context context) {
		super(context);
		init(null, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	/**
	 * An interface that is called whenever the color is changed. Currently it
	 * is always called when the color changes.
	 * 
	 * @author lars
	 * 
	 */
	public interface OnColorChangedListener {
		void onColorChanged(int color);
	}

	/**
	 * An interface that is called whenever only the hue should be changed.
	 *
	 * @author thomas
	 *
	 */
	public interface OnHueChangedListener {
		void onHueChanged(float hue);
	}

	/**
	 * An interface that is called whenever a new color has been selected.
	 * Currently it is always called when the color wheel has been released.
	 * 
	 */
	public interface OnColorSelectedListener {
		void onColorSelected(int color);
	}

	/**
	 * Set a onColorChangedListener
	 * 
	 * @param listener {@code OnColorChangedListener}
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.onColorChangedListener = listener;
	}

	/**
	 * Gets the onColorChangedListener
	 * 
	 * @return {@code OnColorChangedListener}
	 */
	public OnColorChangedListener getOnColorChangedListener() {
		return this.onColorChangedListener;
	}

	/**
	 * Set a onColorChangedListener
	 *
	 * @param listener {@code OnHueChangedListener}
	 */
	public void setOnHueChangedListener(OnHueChangedListener listener) {
		this.onHueChangedListener = listener;
	}

	/**
	 * Gets the onColorChangedListener
	 *
	 * @return {@code OnHueChangedListener}
	 */
	public OnHueChangedListener getOnHueChangedListener() {
		return this.onHueChangedListener;
	}

	/**
	 * Set a onColorSelectedListener
	 * 
	 * @param listener {@code OnColorSelectedListener}
	 */
	public void setOnColorSelectedListener(OnColorSelectedListener listener) {
		this.onColorSelectedListener = listener;
	}

	/**
	 * Gets the onColorSelectedListener
	 * 
	 * @return {@code OnColorSelectedListener}
	 */
	public OnColorSelectedListener getOnColorSelectedListener() {
		return this.onColorSelectedListener;
	}
	
	/**
	 * Color of the latest entry of the onColorChangedListener.
	 */
	private int oldChangedListenerColor;
	
	/**
	 * Color of the latest entry of the onColorSelectedListener.
	 */
	private int oldSelectedListenerColor;

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorPicker, defStyle, 0);
		final Resources b = getContext().getResources();

		mColorWheelThickness = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_wheel_thickness,
				b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
		mColorWheelRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_wheel_radius,
				b.getDimensionPixelSize(R.dimen.color_wheel_radius));
		mPreferredColorWheelRadius = mColorWheelRadius;
		mColorCenterRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_center_radius,
				b.getDimensionPixelSize(R.dimen.color_center_radius));
		mPreferredColorCenterRadius = mColorCenterRadius;
		mColorCenterHaloRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_center_halo_radius,
				b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
		mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
		mColorPointerRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_pointer_radius,
				b.getDimensionPixelSize(R.dimen.color_pointer_radius));
		mColorPointerHaloRadius = a.getDimensionPixelSize(
				R.styleable.ColorPicker_color_pointer_halo_radius,
				b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));

		a.recycle();

		mAngle = (float) (-Math.PI / 2);
		calculateColor(mAngle);

        float[] hsvColor = {mHSVColor[0], 1.f, 1.f};

		Shader s = new SweepGradient(0, 0, COLORS, null);

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mColorWheelThickness);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(Color.BLACK);
		mPointerHaloPaint.setAlpha(0x50);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setColor(Color.HSVToColor(hsvColor));

		mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterNewPaint.setColor(mColor);
		mCenterNewPaint.setStyle(Paint.Style.FILL);

		mCenterOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterOldPaint.setColor(mColor);
		mCenterOldPaint.setStyle(Paint.Style.FILL);

		mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterHaloPaint.setColor(Color.BLACK);
		mCenterHaloPaint.setAlpha(0x00);
		
		mCenterNewColor = mColor;
		mCenterOldColor = mColor;
		mShowCenterOldColor = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// All of our positions are using our internal coordinate system.
		// Instead of translating
		// them we let Canvas do the work for us.
		canvas.translate(mTranslationOffset, mTranslationOffset);

		// Draw the color wheel.
		canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

		float[] pointerPosition = calculatePointerPosition(mAngle);

		// Draw the pointer's "halo"
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mColorPointerHaloRadius, mPointerHaloPaint);

		// Draw the pointer (the currently selected color) slightly smaller on
		// top.
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mColorPointerRadius, mPointerColor);

		// Draw the halo of the center colors.
		canvas.drawCircle(0, 0, mColorCenterHaloRadius, mCenterHaloPaint);
		
		if (mShowCenterOldColor) {
			// Draw the old selected color in the center.
			canvas.drawArc(mCenterRectangle, 90, 180, true, mCenterOldPaint);

			// Draw the new selected color in the center.
			canvas.drawArc(mCenterRectangle, 270, 180, true, mCenterNewPaint);
		}
		else {
			// Draw the new selected color in the center.
			canvas.drawArc(mCenterRectangle, 0, 360, true, mCenterNewPaint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(intrinsicSize, widthSize);
		} else {
			width = intrinsicSize;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(intrinsicSize, heightSize);
		} else {
			height = intrinsicSize;
		}

		int min = Math.min(width, height);
		setMeasuredDimension(min, min);
		mTranslationOffset = min * 0.5f;

		// fill the rectangle instances.
		mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius;
		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);

		mColorCenterRadius = (int) ((float) mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mColorCenterHaloRadius = (int) ((float) mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mCenterRectangle.set(-mColorCenterRadius, -mColorCenterRadius,
				mColorCenterRadius, mColorCenterRadius);
	}

	private int ave(int s, int d, float p) {
		return s + Math.round(p * (d - s));
	}

	/**
	 * Calculate the color using the supplied angle.
	 * 
	 * @param angle The selected color's position expressed as angle (in rad).
	 * 
	 * @return The ARGB value of the color on the color wheel at the specified
	 *         angle.
	 */
	private void calculateColor(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}

		mHSVColor[0] = unit * 360.f;

		mColor = Color.HSVToColor(mHSVColor);
	}

	/**
	 * Get the currently selected color.
	 * 
	 * @return The ARGB value of the currently selected color.
	 */
	public int getColor() {
		return mCenterNewColor;
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);

		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Check whether the user pressed on the pointer.
			float[] pointerPosition = calculatePointerPosition(mAngle);
			if (x >= (pointerPosition[0] - mColorPointerHaloRadius)
					&& x <= (pointerPosition[0] + mColorPointerHaloRadius)
					&& y >= (pointerPosition[1] - mColorPointerHaloRadius)
					&& y <= (pointerPosition[1] + mColorPointerHaloRadius)) {
				mSlopX = x - pointerPosition[0];
				mSlopY = y - pointerPosition[1];
				mUserIsMovingPointer = true;
				invalidate();
			}

            // Check whether the user pressed anywhere on the wheel.
            else if (Math.sqrt(x*x + y*y)  <= mColorWheelRadius + mColorPointerHaloRadius
                            && Math.sqrt(x*x + y*y) >= mColorWheelRadius - mColorPointerHaloRadius
                            && mTouchAnywhereOnColorWheelEnabled) {
                    mUserIsMovingPointer = true;
                    invalidate();
            }

			// If user did not press pointer or center, report event not handled
			else{
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);
				calculateColor(mAngle);

                float[] hsvColor = {mHSVColor[0], 1.f, 1.f};

				mPointerColor.setColor(Color.HSVToColor(hsvColor));

				// Update linked bars with new hue
				if (hasOpacityBar()) {
					// mColor = mOpacityBar.setHue(mColor);
				}

				if (hasValueBar()) {
					mValueBar.setHue(mHSVColor[0]);
				}

				if (hasSaturationBar()) {
					mSaturationBar.setHue(mHSVColor[0]);
				}

				if (hasSVBar()) {
					// mColor = mSVbar.setHue(mColor);
				}

				setNewCenterColor(mColor);

				invalidate();
			}
			// If user did not press pointer or center, report event not handled
			else{
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			mCenterHaloPaint.setAlpha(0x00);
			
			if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
				onColorSelectedListener.onColorSelected(mCenterNewColor);
				oldSelectedListenerColor = mCenterNewColor;
			}

			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
			if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
				onColorSelectedListener.onColorSelected(mCenterNewColor);
				oldSelectedListenerColor = mCenterNewColor;
			}
			break;
		}
		return true;
	}

	/**
	 * Calculate the pointer's coordinates on the color wheel using the supplied
	 * angle.
	 * 
	 * @param angle The position of the pointer expressed as angle (in rad).
	 * 
	 * @return The coordinates of the pointer's center in our internal
	 *         coordinate system.
	 */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));

		return new float[] { x, y };
	}

	/**
	 * Add a Saturation/Value bar to the color wheel.
	 * 
	 * @param bar The instance of the Saturation/Value bar.
	 */
	public void addSVBar(SVBar bar) {
		mSVbar = bar;
		// Give an instance of the color picker to the Saturation/Value bar.
		mSVbar.setColorPicker(this);
		mSVbar.setColor(mColor);
	}

	/**
	 * Add a Opacity bar to the color wheel.
	 * 
	 * @param bar The instance of the Opacity bar.
	 */
	public void addOpacityBar(OpacityBar bar) {
		mOpacityBar = bar;
		// Give an instance of the color picker to the Opacity bar.
		mOpacityBar.setColorPicker(this);
		mOpacityBar.setColor(mColor);
	}

	public void addSaturationBar(SaturationBar bar) {
		mSaturationBar = bar;
		mSaturationBar.setColorPicker(this);
		mSaturationBar.setHue(mHSVColor[0]);
	}

	public void addValueBar(ValueBar bar) {
		mValueBar = bar;
		mValueBar.setColorPicker(this);
		mValueBar.setHue(mHSVColor[0]);
	}

	/**
	 * Change the color of the center which indicates the new color.
	 * 
	 * @param color int of the color.
	 */
	public void setNewCenterColor(int color) {
	    mColor = color;
	    Color.colorToHSV(color, mHSVColor);
		mCenterNewColor = color;
		mCenterNewPaint.setColor(color);

		if (mCenterOldColor == 0) {
			mCenterOldColor = color;
			mCenterOldPaint.setColor(color);
		}

		if (onColorChangedListener != null && color != oldChangedListenerColor ) {
			onColorChangedListener.onColorChanged(color);
			oldChangedListenerColor  = color;
		}

		invalidate();
	}

	/**
	 * Change the color of the center which indicates the old color.
	 * 
	 * @param color int of the color.
	 */
	public void setOldCenterColor(int color) {
		mCenterOldColor = color;
		mCenterOldPaint.setColor(color);
		invalidate();
	}

	public int getOldCenterColor() {
		return mCenterOldColor;
	}
	
	/**
	 * Set whether the old color is to be shown in the center or not
	 * 
	 * @param show true if the old color is to be shown, false otherwise
	 */
	public void setShowOldCenterColor(boolean show) {
		mShowCenterOldColor = show;
		invalidate();
	}
	
	public boolean getShowOldCenterColor() {
		return mShowCenterOldColor;
	}

	/**
	 * Used to change the color of the {@code OpacityBar} used by the
	 * {@code SVBar} if there is an change in color.
	 * 
	 * @param color int of the color used to change the opacity bar color.
	 */
	public void changeOpacityBarColor(int color) {
		if (mOpacityBar != null) {
			mOpacityBar.setColor(color);
		}
	}

	/**
	 * Used to change the value of the {@code SaturationBar}.
	 * 
	 * @param value int of the color used to change the opacity bar color.
	 */
	public void changeSaturationBarValue(float value) {
		if (hasSaturationBar()) {
			mSaturationBar.setValue(value);
		}
	}

    /**
     * Used to change the saturation of the {@code ValueBar}.
     *
     * @param saturation int of the color used to change the opacity bar color.
     */
    public void changeValueBarSaturation(float saturation) {
        if (hasValueBar()) {
            mValueBar.setSaturation(saturation);
        }
    }

	/**
	 * Used to change the color of the {@code ValueBar}.
	 * 
	 * @param hue Current value of the picker.
	 */
	public void changeValueBarHue(float hue) {
		if (hasValueBar()) {
			mValueBar.setHue(hue);
		}
	}

	/**
	 * Used to change the color of the {@code ValueBar}.
	 *
	 * @param hue Current value of the picker.
	 */
	public void changeSaturationBarHue(float hue) {
		if (hasSaturationBar()) {
			mSaturationBar.setHue(hue);
		}
	}
	
	/**
	 * Checks if there is an {@code OpacityBar} connected.
	 * 
	 * @return true or false.
	 */
	public boolean hasOpacityBar(){
		return mOpacityBar != null;
	}
	
	/**
	 * Checks if there is a {@code ValueBar} connected.
	 * 
	 * @return true or false.
	 */
	public boolean hasValueBar(){
		return mValueBar != null;
	}
	
	/**
	 * Checks if there is a {@code SaturationBar} connected.
	 * 
	 * @return true or false.
	 */
	public boolean hasSaturationBar(){
		return mSaturationBar != null;
	}
	
	/**
	 * Checks if there is a {@code SVBar} connected.
	 * 
	 * @return true or false.
	 */
	public boolean hasSVBar(){
		return mSVbar != null;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloat(STATE_ANGLE, mAngle);
		state.putInt(STATE_OLD_COLOR, mCenterOldColor);
		state.putBoolean(STATE_SHOW_OLD_COLOR, mShowCenterOldColor);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		mAngle = savedState.getFloat(STATE_ANGLE);
		setOldCenterColor(savedState.getInt(STATE_OLD_COLOR));
		mShowCenterOldColor = savedState.getBoolean(STATE_SHOW_OLD_COLOR);
		calculateColor(mAngle);
        float[] hsvColor = {mHSVColor[0], 1.f, 1.f};
		mPointerColor.setColor(Color.HSVToColor(hsvColor));
		// TODO: mColor is always black here, because mHSVColor is initialized with zeros
        // Color gets overwritten by restore-function of bars
        // What if there is only the picker without any bars?
		setNewCenterColor(mColor);
	}

	public void setTouchAnywhereOnColorWheelEnabled(boolean TouchAnywhereOnColorWheelEnabled){
			mTouchAnywhereOnColorWheelEnabled = TouchAnywhereOnColorWheelEnabled;
	}

	public boolean getTouchAnywhereOnColorWheel(){
                return mTouchAnywhereOnColorWheelEnabled;
        }
}
