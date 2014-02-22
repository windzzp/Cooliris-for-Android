package com.gtx.cooliris.widget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gtx.cooliris.utils.LogUtil;
import com.gtx.cooliris.R;

public class SeekBarPreference2 extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private static final String TAG = "SeekBarPreference2";
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar = null;
	private TextView mMessageText = null;
	private TextView mValueText = null;
	private TextView mUnitText = null;

	private String mSubTitle = "";
	private String mUnit = "";
	
	private int mDefault = 0;
	private int mMax = 0;
	private int mMin = 0;
	private int mProgressValue = 0;

	public SeekBarPreference2(Context context)
	{
		this(context, null);
	}

	public SeekBarPreference2(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initData(context, attrs);
	}

	public SeekBarPreference2(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initData(context, attrs);
	}
	
	private void initData(Context context, AttributeSet attrs)
	{
		// Initialize value from xml
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		
		// Get the side bar extension value
	    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sidebar_pref); 
	    mSubTitle = a.getString(R.styleable.sidebar_pref_subtitle);
	    mUnit = a.getString(R.styleable.sidebar_pref_unit);
	    mMin = a.getInt(R.styleable.sidebar_pref_min, 0);
	    a.recycle();
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		super.onPrepareDialogBuilder(builder);
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = inflate.inflate(R.layout.seekbar_preference_layout, null);
		builder.setView(contentView)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					save(mSeekBar.getProgress());
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		
		// Initialize controls
		mSeekBar = (SeekBar) contentView.findViewById(R.id.seek_bar);
		mMessageText = (TextView) contentView.findViewById(R.id.message_title);
		mValueText = (TextView) contentView.findViewById(R.id.value);
		mUnitText = (TextView) contentView.findViewById(R.id.suffix);
		
		// Initialize controls' value 
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setProgress(mProgressValue);
		mSeekBar.setOnSeekBarChangeListener(this);
		mMessageText.setText(mSubTitle);
		mValueText.setText(Integer.toString(mProgressValue + mMin));
		mUnitText.setText(mUnit);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)
	{
		super.onSetInitialValue(restore, defaultValue);
		
		int value = 0;
		if (restore)
		{
		    value = shouldPersist() ? getPersistedInt(mDefault) : mMin;
			//mProgressValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		}
		else
		{
		    value = (Integer) defaultValue;
		}
		
		mProgressValue = ((value - mMin) < 0) ? 0 : (value - mMin);
		LogUtil.d(TAG, "mProgressValue = " + mProgressValue);
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
	{
		String t = String.valueOf(value + mMin);
		mValueText.setText(t);
		mProgressValue = value;
		//save(value);
	}

	public void onStartTrackingTouch(SeekBar seek)
	{
	}

	public void onStopTrackingTouch(SeekBar seek)
	{
	}

	public void setMin(int min)
	{
		mMin = min;
	}

	public int getMin()
	{
		return mMin;
	}
	
	public void setMax(int max)
	{
		mMax = max;
	}

	public int getMax()
	{
		return mMax;
	}

	public void setProgress(int progress)
	{
		mProgressValue = progress;
		if (mSeekBar != null)
		{
			mSeekBar.setProgress(progress);
		}
	}

	public int getProgress()
	{
		return mProgressValue;
	}
	
	private void save(int progressValue)
	{
	    final int savedValue = progressValue + mMin;
	    LogUtil.e(TAG, "Seek bar value: " + progressValue + ", last value: " + savedValue);
		if (shouldPersist())
		{
			persistInt(savedValue);
		}
		callChangeListener(Integer.valueOf(savedValue));
	}
}