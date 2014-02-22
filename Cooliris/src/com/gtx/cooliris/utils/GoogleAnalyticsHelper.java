package com.gtx.cooliris.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.analytics.tracking.android.ExceptionParser;

public class GoogleAnalyticsHelper
{
    private static final String TAG = "ExceptionUtil";
    
    public static class AnalyticsExceptionParser implements ExceptionParser
    {
        public String getDescription(String p_thread, Throwable p_throwable)
        {
            return "Thread: " + p_thread + 
                   ", Exception: " + ExceptionUtils.getStackTrace(p_throwable);
        }
    }
    
    /**
     * Print current thread's stack trace.
     */
    public static void printStackTrace()
    {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (null != elements)
        {
            for (StackTraceElement stackTraceElement : elements)
            {
                LogUtil.e(TAG, stackTraceElement.toString());
            }
        }
    }
}
