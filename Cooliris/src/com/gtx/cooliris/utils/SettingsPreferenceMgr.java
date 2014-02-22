package com.gtx.cooliris.utils;

import com.gtx.cooliris.app.CoolirisApplication;
import com.gtx.cooliris.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SettingsPreferenceMgr
{
    public static final String PREF_KEY_IS_FIRST_RUN            = "pref_key_is_first_run";
    
    public static final String PREF_KEY_CLEAR_CACHE             = "pref_key_clear_cache";
    public static final String PREF_KEY_SET_CACHE_SIZE          = "pref_key_set_cache_size";
    public static final String PREF_KEY_DOWNLOAD_PATH           = "pref_key_download_path";
    public static final String PREF_KEY_SHOW_TITLE_DESCRIPTION  = "pref_key_show_title_description";
    public static final String PREF_KEY_SWITCH_EFFECT		    = "pref_key_switch_effect";
    public static final String PREF_KEY_SLIDESHOW_INTERVAL      = "pref_key_slideshow_interval";
    public static final String PREF_KEY_SLIDESHOW_MODE          = "pref_key_slideshow_mode";
    public static final String PREF_KEY_DATA_SOURCE             = "pref_key_data_source";
    public static final String PREF_KEY_ABOUT_DISCLAIMER        = "pref_key_about_disclaimer";
    
    public static final int    PREF_KEY_SLIDESHOW_INTERVAL_DEF  = 5;
    public static final int    PREF_KEY_SLIDESHOW_INTERVAL_MIN  = 3;
    public static final int    PREF_KEY_SLIDESHOW_INTERVAL_MAX  = 15;
    
    public static final String PREF_KEY_SWITCH_EFFECT_DEF	= 
        CoolirisApplication.getAppContext().getString(R.string.preference_switch_effect_default);
    public static final String PREF_KEY_SLIDESHOW_MODE_DEF		= 
        CoolirisApplication.getAppContext().getString(R.string.preference_slideshow_mode_default);
    
    public static final String PREF_KEY_SWITCH_EFFECT_TYPE	    = "pref_key_switch_effect_type";
    public static final int    SWITCH_EFFECT_DEFAULT			= 0;
    public static final int    SWITCH_EFFECT_ZOOMOUT			= 1;
    public static final int    SWITCH_EFFECT_DEPTH			    = 2;
    
    private SharedPreferences m_sharedPref = null;
    private Editor m_editor = null;
    
    public SettingsPreferenceMgr(Context context)
    {
        m_sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    /**
     * This method is called before set value into SharePrefence.
     */
    public void beginSet()
    {
        m_editor = m_sharedPref.edit();
    }
    
    /**
     * This method is called after set value into SharePrefence.
     */
    public void endSet()
    {
        if (null != m_editor)
        {
            m_editor.commit();
        }
    }
    
    /**
     * Reset all into default value.
     */
    public void resetDefault()
    {
        // TODO
        beginSet();
        setSlideshowInterval(PREF_KEY_SLIDESHOW_INTERVAL_DEF);
        
        // After reset to default, we need to change the first run flag into false.
        setIsFirstRun(false);
        endSet();
    }
    
    /**
     * Set is first run this application.
     * 
     * @param isFirstRun The flag indicate whether is first run.
     */
    public void setIsFirstRun(boolean isFirstRun)
    {
        if (null != m_editor)
        {
            m_editor.putBoolean(PREF_KEY_IS_FIRST_RUN, isFirstRun);
        }
    }
    
    /**
     * Whether the application is first run.
     * 
     * @return True indicate run fist.
     */
    public boolean isFirstRun()
    {
        if (null != m_sharedPref)
        {
            return m_sharedPref.getBoolean(PREF_KEY_IS_FIRST_RUN, true);
        }
        
        return true;
    }
    
    /**
     * Set slide show interval time in second.
     * 
     * @param value Slide show interval time.
     */
    public void setSlideshowInterval(int value)
    {
        if (null != m_editor /*&& isIntervalValid(value)*/)
        {
            m_editor.putInt(PREF_KEY_SLIDESHOW_INTERVAL, value);
        }
    }
    
    /**
     * Get the slide show interval time.
     * 
     * @return The slide show interval time.
     */
    public int getSlideshowInterval()
    {
        if (null != m_sharedPref)
        {
            return m_sharedPref.getInt(PREF_KEY_SLIDESHOW_INTERVAL, PREF_KEY_SLIDESHOW_INTERVAL_DEF);
        }
        
        return PREF_KEY_SLIDESHOW_INTERVAL_DEF;
    }
   
    /**
     * Is the slide show interval time valid. 
     * The range is [ {@link #PREF_KEY_SLIDESHOW_INTERVAL_MIN}, {@link #PREF_KEY_SLIDESHOW_INTERVAL_MAX} ].
     * 
     * @param value The specified value.
     * 
     * @return Return true if the value is valid.
     */
    public boolean isIntervalValid(int value)
    {
        return (value >= PREF_KEY_SLIDESHOW_INTERVAL_MIN && 
                value <= PREF_KEY_SLIDESHOW_INTERVAL_MAX);
    }
    
    /**
     * Get switch effect name.
     */
    public String getSwitchEffect()
	{
	    if (null != m_sharedPref)
	    {
	        return m_sharedPref.getString(PREF_KEY_SWITCH_EFFECT, PREF_KEY_SWITCH_EFFECT_DEF);
	    }
	    
	    return PREF_KEY_SWITCH_EFFECT_DEF;
	}
        
    public void setSwitchEffectType(int value)
    {
        if (null != m_editor)
        {
            m_editor.putInt(PREF_KEY_SWITCH_EFFECT_TYPE, value);
        }
    }
    
    /**
     * Get slideshow effect name.
     */
    public int getSwitchEffectType()
	{
	    if (null != m_sharedPref)
	    {
	        return m_sharedPref.getInt(PREF_KEY_SWITCH_EFFECT_TYPE, SWITCH_EFFECT_DEFAULT);
	    }
	    
	    return SWITCH_EFFECT_DEFAULT;
	}
    
    /**
     * Get slideshow mode name.
     */
    public String getSlideShowMode()
	{
	    if (null != m_sharedPref)
	    {
	        return m_sharedPref.getString(PREF_KEY_SLIDESHOW_MODE, PREF_KEY_SLIDESHOW_MODE_DEF);
	    }
	    
	    return PREF_KEY_SLIDESHOW_MODE_DEF;
	}    

	/**
     * Show title or description or not.
     * 
     * @return If return true, it'll need to show description.
     */
    public boolean needShowDescription()
    {
        if (null != m_sharedPref)
        {
            return m_sharedPref.getBoolean(PREF_KEY_SHOW_TITLE_DESCRIPTION, false);
        }
        
        return true;
    }
}
