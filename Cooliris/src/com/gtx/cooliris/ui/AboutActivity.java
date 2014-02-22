package com.gtx.cooliris.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.gtx.cooliris.R;
import com.gtx.cooliris.constant.GAConstant;
import com.gtx.cooliris.imagecache.Utils;

public class AboutActivity extends Activity
{
	private TextView m_aboutApp = null;
	private TextView m_appVersion = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		EasyTracker.getTracker().sendView(GAConstant.SCREEN_SETTINGS_ABOUT);
		
		m_aboutApp = (TextView) findViewById(R.id.about_app);
		m_appVersion = (TextView) findViewById(R.id.app_version);
		
		String appVer = getAppVersion();
		String appVerPrefix = getString(R.string.app_version);
		m_appVersion.setText(appVerPrefix + appVer);
		
        // Add a action bar on top
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.preference_about_disclaimer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
	}
	
	private String getAppVersion()
	{
		try
		{
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			String version = info.versionName;
			return "V" + version;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return getString(R.string.default_app_version_code);
		}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
        	//NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }	
	
	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition(R.anim.no_anim, R.anim.exit_anim);
	}
}
