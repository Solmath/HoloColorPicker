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
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class SaturationBar extends ColorBar {

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(android.view.MotionEvent)
	 */
	private boolean mIsMovingPointer;

	/**
	 * Factor used to calculate the position to the Opacity on the bar.
	 */
	private float mPosToSatFactor;

	/**
	 * Factor used to calculate the Opacity to the postion on the bar.
	 */
	private float mSatToPosFactor;
	
    /**
     * Interface and listener so that changes in SaturationBar are sent
     * to the host activity/fragment
     */
    private OnSaturationChangedListener onSaturationChangedListener;
    
	/**
	 * Saturation of the latest entry of the onSaturationChangedListener.
	 */
	private int oldChangedListenerSaturation;

    public interface OnSaturationChangedListener {
        void onSaturationChanged(int saturation);
    }

    public void setOnSaturationChangedListener(OnSaturationChangedListener listener) {
        this.onSaturationChangedListener = listener;
    }

    public OnSaturationChangedListener getOnSaturationChangedListener() {
        return this.onSaturationChangedListener;
    }

	public SaturationBar(Context context) {
		super(context, SATURATION);
		super.init(null, 0);
	}

	public SaturationBar(Context context, AttributeSet attrs) {
		super(context, attrs, SATURATION);
		super.init(attrs, 0);
	}

	public SaturationBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle, SATURATION);
		super.init(attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// Fill the rectangle instance based on orientation
		int x1, y1;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			x1 = (mBarLength + mBarPointerHaloRadius);
			y1 = mBarThickness;
			mBarLength = w - (mBarPointerHaloRadius * 2);
			mBarRect.set(mBarPointerHaloRadius,
					(mBarPointerHaloRadius - (mBarThickness / 2)),
					(mBarLength + (mBarPointerHaloRadius)),
					(mBarPointerHaloRadius + (mBarThickness / 2)));
		}
		else {
			x1 = mBarThickness;
			y1 = (mBarLength + mBarPointerHaloRadius);
			mBarLength = h - (mBarPointerHaloRadius * 2);
			mBarRect.set((mBarPointerHaloRadius - (mBarThickness / 2)),
                    mBarPointerHaloRadius,
					(mBarPointerHaloRadius + (mBarThickness / 2)),
					(mBarLength + (mBarPointerHaloRadius)));
		}

		// Update variables that depend of mBarLength.
		if (!isInEditMode()){
			shader = new LinearGradient(mBarPointerHaloRadius, 0,
					x1, y1,
                    new int[] {
							Color.BLACK,
							Color.HSVToColor(0xFF, mHSVColor) },
                    null,
					Shader.TileMode.CLAMP);
		} else {
			shader = new LinearGradient(mBarPointerHaloRadius, 0,
					x1, y1,
                    new int[] {
			                Color.BLACK,
                            0xff81ff00 },
                    null,
                    Shader.TileMode.CLAMP);
			Color.colorToHSV(0xff81ff00, mHSVColor);
		}
		
		mBarPaint.setShader(shader);
		mPosToSatFactor = 1 / ((float) mBarLength);
		mSatToPosFactor = ((float) mBarLength) / 1;
		
		float[] hsvColor = new float[3];
		Color.colorToHSV(mColor, hsvColor);
		
		if (!isInEditMode()){
			mBarPointerPosition = Math.round((mSatToPosFactor * hsvColor[1])
					+ mBarPointerHaloRadius);
		} else {
			mBarPointerPosition = mBarLength + mBarPointerHaloRadius;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);

		// Convert coordinates to our internal coordinate system
		float dimen;
		if (mOrientation == ORIENTATION_HORIZONTAL) {
			dimen = event.getX();
		}
		else {
			dimen = event.getY();
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    	mIsMovingPointer = true;
			// Check whether the user pressed on (or near) the pointer
			if (dimen >= (mBarPointerHaloRadius)
					&& dimen <= (mBarPointerHaloRadius + mBarLength)) {
				mBarPointerPosition = Math.round(dimen);
				calculateColor(Math.round(dimen));
				mBarPointerPaint.setColor(mColor);

                if (mPicker != null) {
                    mPicker.changeValueBarSaturation(mHSVColor[1]);
                    mPicker.changeOpacityBarColor(mColor);
                    mPicker.setNewCenterColor(mColor);
                }
                invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsMovingPointer) {
				// Move the the pointer on the bar.
				if (dimen >= mBarPointerHaloRadius
						&& dimen <= (mBarPointerHaloRadius + mBarLength)) {
					mBarPointerPosition = Math.round(dimen);
					calculateColor(Math.round(dimen));
				} else if (dimen < mBarPointerHaloRadius) {
					mBarPointerPosition = mBarPointerHaloRadius;
					mColor = Color.HSVToColor(mHSVColor);
				} else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
					mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
					mColor = Color.HSVToColor(mHSVColor);
				}

				mBarPointerPaint.setColor(mColor);

                if (mPicker != null) {
                    mPicker.changeValueBarSaturation(mHSVColor[1]);
                    mPicker.changeOpacityBarColor(mColor);
                    mPicker.setNewCenterColor(mColor);
                }
                invalidate();
			}
			if(onSaturationChangedListener != null && oldChangedListenerSaturation != mColor){
	            onSaturationChangedListener.onSaturationChanged(mColor);
	            oldChangedListenerSaturation = mColor;
			}
			break;
		case MotionEvent.ACTION_UP:
			mIsMovingPointer = false;
			break;
		}

		return true;
	}

<<<<<<< ColorBarClass
	/**
	 * Set the bar color. <br>
	 * <br>
	 * Its discouraged to use this method. (why?)
	 * 
	 * @param color
	 */
	public float setHue(int color) {
        int x1, y1;
        if(mOrientation) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
        }
        else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
        }

		float[] hsvColor = new float[3];
		Color.colorToHSV(color, hsvColor);
		mHSVColor[0] = hsvColor[0];

        hsvColor[2] = mHSVColor[2];
		hsvColor[1] = 0.f;
        int startColor = Color.HSVToColor(hsvColor);

		hsvColor[1] = 1.f;
		int endColor = Color.HSVToColor(hsvColor);

        shader = new LinearGradient(mBarPointerHaloRadius, 0,
                x1, y1, new int[] {
                startColor, endColor}, null,
                Shader.TileMode.CLAMP);

        mBarPaint.setShader(shader);

        mColor = Color.HSVToColor(mHSVColor);
        mBarPointerPaint.setColor(mColor);
        invalidate();

		return mHSVColor[1];
	}

	public float setValue(float value) {
=======
	public void setValue(float value) {

		mHSVColor[BRIGHTNESS] = value;

		setGradient();

        mColor = Color.HSVToColor(mHSVColor);
		mBarPointerPaint.setColor(mColor);
		invalidate();
	}

	@Override
	public void setGradient(){
>>>>>>> local
		int x1, y1;
		if(mOrientation) {
			x1 = (mBarLength + mBarPointerHaloRadius);
			y1 = mBarThickness;
		}
		else {
			x1 = mBarThickness;
			y1 = (mBarLength + mBarPointerHaloRadius);
		}

		mHSVColor[2] = value;

		float[] hsvColor = {mHSVColor[0], 0.f, mHSVColor[2]};
        int startColor = Color.HSVToColor(hsvColor);

        hsvColor[1] = 1.f;
        int endColor = Color.HSVToColor(hsvColor);

		shader = new LinearGradient(mBarPointerHaloRadius, 0,
				x1, y1, new int[] {
				startColor, endColor }, null,
				Shader.TileMode.CLAMP);

		mBarPaint.setShader(shader);

		// calculateColor(mBarPointerPosition);
        mColor = Color.HSVToColor(mHSVColor);
		mBarPointerPaint.setColor(mColor);
		invalidate();

		return mHSVColor[1];
	}
<<<<<<< ColorBarClass

	/**
	 * Set the pointer on the bar. With the opacity value.
	 * 
	 * @param saturation float between 0 and 1
	 */
	public void setSaturation(float saturation) {
		mBarPointerPosition = Math.round((mSatToPosFactor * saturation))
				+ mBarPointerHaloRadius;
		calculateColor(mBarPointerPosition);
		mBarPointerPaint.setColor(mColor);

		if (mPicker != null) {
			mPicker.changeValueBarHue(mColor);
			mColor = mPicker.changeOpacityBarColor(mColor);
			mPicker.setNewCenterColor(mColor);
		}
		invalidate();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		setHue(Color.HSVToColor(savedState.getFloatArray(STATE_COLOR)));
		setSaturation(savedState.getFloat(STATE_POSITION));
	}
=======
>>>>>>> local
}
