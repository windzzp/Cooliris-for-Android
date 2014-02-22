package com.gtx.cooliris.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.gtx.cooliris.utils.GoogleAnalyticsHelper;
import com.gtx.cooliris.utils.SettingsPreferenceMgr;

/**
 * This class extends the Application class.
 */
public class CoolirisApplication extends Application
{
    private class CurrentState
	{
		public boolean needShowDescription;
		public int     slideShowType = 0;
		
		public CurrentState(Context context)
		{
			SettingsPreferenceMgr prefMgr = new SettingsPreferenceMgr(context);
			needShowDescription = prefMgr.needShowDescription();
			slideShowType = prefMgr.getSwitchEffectType();
		}
	}

	/**
     * The application context.
     */
    private static Context          s_appContext            = null;
    
    private static CurrentState 	s_currentState			= null;

    /**
     * The flag indicates whether the data has been initialized or not.
     * After loading global completed, it'll be set into true.
     */
    private static boolean          s_hasInitialized        = false;

    /**
     * The top activity.
     */
    private Activity m_topActivity = null;
    
    /**
     * Get the application context.
     * 
     * @return The application context.
     */
    public static Context getAppContext()
    {
        return s_appContext;
    }
    
    /**
     * set the application context.
     * @param context The context of application.
     */
    private static void setAppContext(Context context)
    {
        s_appContext = context;
    }

    /**
     * Called when the application is starting, before any other application
     * objects have been created.  Implementations should be as quick as
     * possible (for example using lazy initialization of state) since the time
     * spent in this function directly impacts the performance of starting the
     * first activity, service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    public void onCreate()
    {
        super.onCreate();
        
        initGoogleAnalytics();
        
        // Set the application context.
        setAppContext(this.getApplicationContext());
        
        SettingsPreferenceMgr prefMgr = new SettingsPreferenceMgr(this);
        if (prefMgr.isFirstRun())
        {
            prefMgr.resetDefault();
        }
        
        s_currentState = new CurrentState(this);
    }

    /**
     * Called when the application is stopping.  There are no more application
     * objects running and the process will exit.  <em>Note: never depend on
     * this method being called; in many cases an unneeded application process
     * will simply be killed by the kernel without executing any application
     * code.</em>
     * If you override this method, be sure to call super.onTerminate().
     */
    public void onTerminate()
    {
        super.onTerminate();
    }
     
    /**
     * Called when low memory.
     */
    public void onLowMemory()
    {
        super.onLowMemory();
    }
    
    /**
     * Set the top activity activity.
     * 
     * @param topActivity
     */
    public void setTopActivity(Activity topActivity)
    {
        m_topActivity = topActivity;
    }
    
    /**
     * Get the top activity.
     * 
     * @return the top activity instance.
     */
    public Activity getTopActivity()
    {
        return m_topActivity;
    }

    /**
     * Judge the data has been initialized or not.
     * 
     * @return True if been initialized, else return false.
     */
    public static boolean hasInitialized()
    {
        return s_hasInitialized;
    }
    
    /**
     * Set the flag about initializing the data.
     * 
     * @param isInitialized The value indicates the data has been initialize or not.
     */
    public static void setDataInitialized(boolean isInitialized)
    {
        s_hasInitialized = isInitialized;
    }
    
    public static void setNeedShowDescription(boolean needShowDescription)
    {
    	s_currentState.needShowDescription = needShowDescription;
    }
    
    public static boolean needShowDescription()
    {
    	return s_currentState.needShowDescription;
    }
    
    public static void setSlideShowType(int type)
    {
    	s_currentState.slideShowType = type;
    }
    
    public static int getSlideShowType()
    {
    	return s_currentState.slideShowType;
    }
    
    protected void initGoogleAnalytics()
    {        
        // Set global context for Google Analytics
        EasyTracker.getInstance().setContext(this);

        // Set dispatch period to 30 seconds.
        GAServiceManager.getInstance().setDispatchPeriod(30);
//        
//        UncaughtExceptionHandler myHandler = new ExceptionReporter(
//                mGaTracker,                                       // Currently used Tracker.
//                GAServiceManager.getInstance(),                   // GAServiceManager singleton.
//                Thread.getDefaultUncaughtExceptionHandler(),      // Current default uncaught exception handler. 
//                this);
//
//        // Make myHandler the new default uncaught exception handler.
//        Thread.setDefaultUncaughtExceptionHandler(myHandler);
//        mGaTracker.setExceptionParser(new AnalyticsExceptionParser());
        
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (uncaughtExceptionHandler instanceof ExceptionReporter)
        {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new GoogleAnalyticsHelper.AnalyticsExceptionParser());
        }
    }
}
