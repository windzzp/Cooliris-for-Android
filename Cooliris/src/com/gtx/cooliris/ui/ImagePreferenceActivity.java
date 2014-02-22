package com.gtx.cooliris.ui;

import java.io.File;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.gtx.cooliris.R;
import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.constant.GAConstant;
import com.gtx.cooliris.imagecache.ImageCache;
import com.gtx.cooliris.imagecache.ImageCache.ImageCacheParams;
import com.gtx.cooliris.imagecache.Utils;
import com.gtx.cooliris.utils.DownloadHelper;
import com.gtx.cooliris.utils.FileUtils;
import com.gtx.cooliris.utils.LogUtil;
import com.gtx.cooliris.utils.SettingsPreferenceMgr;

public class ImagePreferenceActivity extends PreferenceActivity 
									 implements OnSharedPreferenceChangeListener, 
									 			OnPreferenceClickListener
{
	private static final String TAG = "ImagePreferenceActivity";
	public static final String PREF_KEY_CLEAR_CACHE 			= "pref_key_clear_cache";
	public static final String PREF_KEY_SHOW_TITLE_DESCRIPTION 	= "pref_key_show_title_description";
	public static final String PREF_KEY_OPEN_DOWNLOAD_FOLDER 	= "pref_key_download_path";
	
	public static final String PREF_KEY_CATEGORY_EFFECT         = "preference_category_effect";
	public static final String PREF_KEY_SWITCH_EFFECT		    = "pref_key_switch_effect";
	
	public static final String PREF_KEY_CATEGORY_SLIDESHOW		= "preference_category_slideshow";
	public static final String PREF_KEY_SLIDESHOW_INTERVAL		= "pref_key_slideshow_interval";
	public static final String PREF_KEY_SLIDESHOW_MODE			= "pref_key_slideshow_mode";
	
	public static final String PREF_KEY_ABOUT					= "preference_category_about";
	public static final String PREF_KEY_ABOUT_FEEDBACK          = "pref_key_about_feedback";
	public static final String PREF_KEY_ABOUT_DISCLAIMER		= "pref_key_about_disclaimer";
	
	private static final String IMAGE_CACHE_THUMBS_DIR     = "thumbs";
	private static final String IMAGE_CACHE_IMAGES_DIR     = "images";
	private static final String IMAGE_CACHE_HTTP_DIR       = "http";
	
	private static final int	MSG_CLEAR_CACHE_STARTED    = 0;
	private static final int	MSG_CLEAR_CACHE_COMPLETED  = 1;
	private static boolean 		m_clearCacheInProgress 	   = false;
	//private final Object 		m_clearCacheWorkLock 	   = new Object();
	
	private static final File	m_downloadFolder = DownloadHelper.getDownloadDir();
	
	protected Tracker mGaTracker;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
    	//getWindow().addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	//getWindow().getDecorView().setFitsSystemWindows(true);
        super.onCreate(savedInstanceState);
        
        mGaTracker = EasyTracker.getTracker();
        mGaTracker.sendView(GAConstant.SCREEN_SETTINGS);
        
        // Add preference layout
        addPreferencesFromResource(R.xml.preferences);
        
        // Add a action bar on top
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.preference_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        
        initPreference();
    }
    
    @Override
    protected void onStart()
    {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

	@Override
    protected void onResume()
    {
    	super.onResume();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
    	super.onPause();
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            finish();
            return true;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		LogUtil.i(TAG, "key = " + key);
		if (PREF_KEY_SHOW_TITLE_DESCRIPTION.equals(key))
		{
			Preference pref = findPreference(key);
			boolean bOn = sharedPreferences.getBoolean(key, true);
			String summary = getResources().getString(bOn ? R.string.preference_ui_show : R.string.preference_ui_hide_desc);
			pref.setSummary(summary);
			
			// Set the state into global cache
			CoolirisApplication.setNeedShowDescription(bOn);
			
			// Send action to server
	        mGaTracker.sendEvent(GAConstant.EVENT_CATEGORY_UI_ACTION, GAConstant.EVENT_ACTION_PRESS_SETTINGS, key, bOn ? 1L : 0L);
		}
		else if (PREF_KEY_SWITCH_EFFECT.equals(key)) 
		{
			ListPreference pref = (ListPreference)findPreference(key);
			String selValue = pref.getValue();
			pref.setSummary(selValue);
			
			// Save Int value into sharepreference
			int ix = pref.findIndexOfValue(selValue);
			SettingsPreferenceMgr prefMgr = new SettingsPreferenceMgr(this);
			prefMgr.beginSet();
			prefMgr.setSwitchEffectType(ix);
			prefMgr.endSet();
			
			// Set the state into global cache
			CoolirisApplication.setSlideShowType(ix);
			
			mGaTracker.sendEvent(GAConstant.EVENT_CATEGORY_UI_ACTION, GAConstant.EVENT_ACTION_PRESS_SETTINGS, key, (long)ix);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		final String key = preference.getKey();
		
		// Send action to server
		mGaTracker.sendEvent(GAConstant.EVENT_CATEGORY_UI_ACTION, GAConstant.EVENT_ACTION_PRESS_SETTINGS, key, 0L);
		
		// onPreferenceTreeClick() may works well ???
		if (PREF_KEY_CLEAR_CACHE.equals(key))
		{
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(R.string.preference_clear_cache)
			 .setMessage(R.string.preference_clear_cache_summary)
			 .setIconAttribute(android.R.attr.alertDialogIcon)
			 .setNegativeButton(android.R.string.cancel, null)
			 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which)
				 {
				     // Send action to server
				     mGaTracker.sendEvent(GAConstant.EVENT_CATEGORY_UI_ACTION, GAConstant.EVENT_ACTION_PRESS_SETTINGS, key, 1L);
					 clearCacheAsync();
				 }
			 })
			 .show();
			
			return true;
		}
		else if (PREF_KEY_OPEN_DOWNLOAD_FOLDER.equals(key)) 
		{
			/*Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
			startActivity(Intent.createChooser(intent, "Open Folder"));*/
			/*Intent intent = new Intent();  
			intent.setAction(Intent.ACTION_GET_CONTENT);
			Uri imgUri = Uri.fromFile(m_downloadFolder);
			intent.setDataAndType(imgUri, "file/*");   
			startActivity(intent);*/
			
			// TODO: Current it'll show folder but user can choose a file to close it.
			if (null != m_downloadFolder)
			{
				if (!m_downloadFolder.exists())
				{
					m_downloadFolder.mkdirs();
				}
				
				try
                {
	                Intent intent = new Intent();  
	                intent.setAction(Intent.ACTION_GET_CONTENT);
	                Uri imgUri = Uri.fromFile(m_downloadFolder);
	                //intent.setDataAndType(imgUri, "vnd.android.cursor.dir/*");
	                intent.setDataAndType(imgUri, "file/*");
	                startActivity(intent);
                }
                catch (Exception e)
                {
                    Toast.makeText(this, R.string.toast_no_appropriate_app_to_open_path, Toast.LENGTH_LONG).show();
                    LogUtil.e(TAG, e.getMessage());
                }
				
				return true;
			}
			
		}
        else if (PREF_KEY_ABOUT_FEEDBACK.equals(key))
        {
            final Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_anim, R.anim.no_anim);
        }
		else if (PREF_KEY_ABOUT_DISCLAIMER.equals(key)) 
		{
			final Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.enter_anim, R.anim.no_anim);
		}
		
		return false;
	}
	
	private void initPreference()
	{
		SettingsPreferenceMgr prefMgr = new SettingsPreferenceMgr(this);
		
		// Set default data, Preference key = "pref_key_show_title_description"
	    SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Set listener when clear cache, Preference key = "pref_key_show_title_description"
		Preference pref = findPreference(PREF_KEY_CLEAR_CACHE);
		pref.setOnPreferenceClickListener(this);
		pref.setSummary(getString(R.string.preference_current_cache_size) + 
				getString(R.string.preference_calculating_cache_size));
		// Start to calcuate cache size
		new CalcuateCacheSizeTask().execute(FileUtils.getDiskCacheDir(this));
		
		// Set listener when click download directory, Preference key = "pref_key_download_path"
		pref = findPreference(PREF_KEY_OPEN_DOWNLOAD_FOLDER);
		pref.setSummary(m_downloadFolder.getAbsolutePath());
		pref.setOnPreferenceClickListener(this);
		
	    // Set the value into Preference (UI)
		boolean bOn = prefMgr.needShowDescription();
		SwitchPreference swPref = (SwitchPreference)findPreference(PREF_KEY_SHOW_TITLE_DESCRIPTION);
		swPref.setChecked(bOn);
		swPref.setDefaultValue(getString(bOn ? R.string.preference_ui_show : R.string.preference_ui_hide));
		swPref.setSummary(getString(bOn ? R.string.preference_ui_show : R.string.preference_ui_hide_desc));
		
		// Find Effect Group
		PreferenceGroup group = (PreferenceGroup)findPreference(PREF_KEY_CATEGORY_EFFECT);
	    // Set the value into Preference (UI), key = "pref_key_switch_effect"
        pref = group.findPreference(PREF_KEY_SWITCH_EFFECT);
        //pref.setSummary(prefMgr.getSwitchEffect());
        int type = prefMgr.getSwitchEffectType();
        String[] effects = getResources().getStringArray(R.array.pref_switch_effect_entries_list);
        pref.setSummary(effects[type]);
		
        // Find Slide Show Group
		group = (PreferenceGroup)findPreference(PREF_KEY_CATEGORY_SLIDESHOW);
		// Set the value into Preference (UI), key = "pref_key_slideshow_interval"
		pref = group.findPreference(PREF_KEY_SLIDESHOW_INTERVAL);
		pref.setSummary(Integer.toString(prefMgr.getSlideshowInterval()) + " " + getString(R.string.preference_slideshow_interval_unit));	

		// TODO: In next version
		// Set the value into Preference (UI), key = "pref_key_slideshow_mode"
		//pref = group.findPreference(PREF_KEY_SLIDESHOW_MODE);
		//pref.setSummary(prefMgr.getSlideShowMode());
		
		// Find About Group
		group = (PreferenceGroup)findPreference(PREF_KEY_ABOUT);
		pref = group.findPreference(PREF_KEY_ABOUT_FEEDBACK);
		pref.setOnPreferenceClickListener(this);

        pref = group.findPreference(PREF_KEY_ABOUT_DISCLAIMER);
        pref.setOnPreferenceClickListener(this);
	}

	private Handler m_handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_CLEAR_CACHE_STARTED:
				Toast.makeText(ImagePreferenceActivity.this, R.string.toast_clear_cache_started, Toast.LENGTH_SHORT).show();
				break;
				
			case MSG_CLEAR_CACHE_COMPLETED:
				Toast.makeText(ImagePreferenceActivity.this, R.string.toast_clear_cache_completed, Toast.LENGTH_LONG).show();
				break;
				
			default:
				break;
			}
		}
	};
	
	private void clearCacheAsync()
	{
		if (m_clearCacheInProgress)
		{
			LogUtil.d(TAG, "Clear cache is in progress, return directly.");
			return;
		}
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				m_clearCacheInProgress = true;
				m_handler.sendEmptyMessage(MSG_CLEAR_CACHE_STARTED);
				
				// Clear cache
				clearCacheInternal();
				
				m_handler.sendEmptyMessage(MSG_CLEAR_CACHE_COMPLETED);
				m_clearCacheInProgress = false;
			}
		}, "clean cache").start();
	}
	
	private void clearCacheInternal()
	{	    
		final String[] cacheDirs = {IMAGE_CACHE_THUMBS_DIR, IMAGE_CACHE_IMAGES_DIR, IMAGE_CACHE_HTTP_DIR}; 
		for (String dir : cacheDirs)
		{
			ImageCacheParams cacheParams = new ImageCacheParams(this, dir);
			ImageCache imageCache = ImageCache.getInstance2(cacheParams);
	        imageCache.initDiskCache();
	        imageCache.clearCache();
	        imageCache = null;
		}
	}
	
	private class CalcuateCacheSizeTask extends AsyncTask<File, Void, Long> {

		@Override
		protected Long doInBackground(File... params) {
			File folder = params[0];
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return FileUtils.getFileOrFolderSize(folder);
		}
		
		@Override
		protected void onPostExecute(Long result) {
			// Set listener when clear cache, Preference key = "pref_key_show_title_description"
			Preference pref = findPreference(PREF_KEY_CLEAR_CACHE);
			
			String takeSize = getString(R.string.preference_current_cache_size);
			takeSize += FileUtils.humanReadableByteCount(result, false);
			pref.setSummary(takeSize);
		}
	}
	
}
