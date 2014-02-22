package com.gtx.cooliris.widget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gtx.cooliris.R;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue = 0;

	public SeekBarPreference(Context context)
	{
		this(context, null);
	}

	public SeekBarPreference(Context context, AttributeSet attrs)
	{
		//this(context, attrs, 0);
		super(context, attrs);
		
		init(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs)
	{
		mContext = context;
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		super.onPrepareDialogBuilder(builder);
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = inflate.inflate(R.layout.dialog_seekbar_layout, null);
		builder.setView(contentView)
			   .setPositiveButton(android.R.string.ok, null)
			   .setNegativeButton(android.R.string.cancel, null);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);
	}

//	@Override
//	protected View onCreateDialogView()
//	{
//		LinearLayout.LayoutParams params;
//		LinearLayout layout = new LinearLayout(mContext);
//		layout.setOrientation(LinearLayout.VERTICAL);
//		layout.setPadding(6, 6, 6, 6);
//
//		mSplashText = new TextView(mContext);
//		if (mDialogMessage != null)
//			mSplashText.setText(mDialogMessage);
//		layout.addView(mSplashText);
//
//		mValueText = new TextView(mContext);
//		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
//		mValueText.setTextSize(32);
//		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//		layout.addView(mValueText, params);
//
//		mSeekBar = new SeekBar(mContext);
//		mSeekBar.setOnSeekBarChangeListener(this);
//		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//
//		if (shouldPersist())
//			mValue = getPersistedInt(mDefault);
//
//		mSeekBar.setMax(mMax);
//		mSeekBar.setProgress(mValue);
//		return layout;
//	}
//
//	@Override
//	protected void onBindDialogView(View v)
//	{
//		super.onBindDialogView(v);
//		mSeekBar.setMax(mMax);
//		mSeekBar.setProgress(mValue);
//	}
//
//	@Override
//	protected void onSetInitialValue(boolean restore, Object defaultValue)
//	{
//		super.onSetInitialValue(restore, defaultValue);
//		if (restore)
//			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
//		else
//			mValue = (Integer) defaultValue;
//	}
//
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
	{
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		if (shouldPersist())
		{
			persistInt(value);
		}
		callChangeListener(new Integer(value));
	}

	public void onStartTrackingTouch(SeekBar seek)
	{
	}

	public void onStopTrackingTouch(SeekBar seek)
	{
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
		mValue = progress;
		if (mSeekBar != null)
		{
			mSeekBar.setProgress(progress);
		}
	}

	public int getProgress()
	{
		return mValue;
	}
}