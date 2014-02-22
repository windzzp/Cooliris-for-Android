package com.gtx.cooliris.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.gtx.cooliris.imagecache.Utils;
import com.gtx.cooliris.BuildConfig;
import com.gtx.cooliris.R;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
    private static final String TAG = "ImageGridFragment";
    private static final int DELAY_MILLIS = 2500;
    private int m_backPressCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        
        if (Utils.hasActionBar())
        {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
        }
        
        super.onCreate(savedInstanceState);
        
//        String aString = null;
//        aString.subSequence(1, 2);
        
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new ImageGridFragment(), TAG);
            ft.commit();
            
			/*getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, new ImageGridFragment(), TAG)
					.commit();*/
        }
        
        /*
        // Add a action bar on left
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setTitle("");
            
            // Add search view
            RelativeLayout relative = new RelativeLayout(actionBar.getThemedContext());
            SearchView mSearchView = new SearchView(actionBar.getThemedContext());
            mSearchView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setGravity(Gravity.LEFT);
            relative.addView(mSearchView);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(relative);
        }*/
    }
    
    @Override
    protected void onStart()
    {
        super.onStart();
        
        EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        
        EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
    public void onBackPressed()
    {
    	// Press back button in 2500sec. will exit this application. 
    	if (0 == m_backPressCount)
		{
    		m_backPressCount++;
			Toast.makeText(this, R.string.toast_exit_application, Toast.LENGTH_SHORT).show();
			
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					m_backPressCount = 0;
				}
			}, DELAY_MILLIS);
			
			return;
		}
    	super.onBackPressed();
    }
    
    @Override
    protected void onDestroy()
    {
    	m_backPressCount = 1;
    	super.onDestroy();
    }
}
