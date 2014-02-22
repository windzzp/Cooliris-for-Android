package com.gtx.cooliris.utils;

/**
 * This class extends the {@link Thread} class, it is can be restart, pause, etc.
 */
public class ThreadUtil extends Thread
{
    /**
     * Indicate the thread has been stopped or not.
     */
    protected boolean m_isStop = false;

    /**
     * The constructor method.
     * 
     * @param threadName
     *            The name of the thread.
     */
    public ThreadUtil(String threadName)
    {
        super(threadName);
    }

    /**
     * Constructs a new {@code Thread} with a {@code Runnable} object and a
     * newly generated name. The new {@code Thread} will belong to the {@code
     * ThreadGroup} passed as parameter.
     *
     * @param group
     *            {@code ThreadGroup} to which the new {@code Thread} will
     *            belong
     * @param runnable
     *            a {@code Runnable} whose method <code>run</code> will be
     *            executed by the new {@code Thread}
     * @throws SecurityException
     *             if <code>group.checkAccess()</code> fails with a
     *             SecurityException
     * @throws IllegalThreadStateException
     *             if <code>group.destroy()</code> has already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public ThreadUtil(ThreadGroup group, Runnable runnable)
    {
        super(group, runnable);
    }
    
    /**
     * Constructs a new {@code Thread} with a {@code Runnable} object, the given
     * name and belonging to the {@code ThreadGroup} passed as parameter.
     *
     * @param group
     *            ThreadGroup to which the new {@code Thread} will belong
     * @param runnable
     *            a {@code Runnable} whose method <code>run</code> will be
     *            executed by the new {@code Thread}
     * @param threadName
     *            the name for the {@code Thread} being created
     * @throws SecurityException
     *             if <code>group.checkAccess()</code> fails with a
     *             SecurityException
     * @throws IllegalThreadStateException
     *             if <code>group.destroy()</code> has already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public ThreadUtil(ThreadGroup group, Runnable runnable, String threadName)
    {
        super(group, runnable, threadName);
    }
    
    /**
     * Check the thread stops or not.
     * 
     * @return true if it is stopping, otherwise false.
     */
    public boolean isStop()
    {
        return m_isStop;
    }

    /**
     * Call this method to restart the thread.
     */
    public synchronized void restart()
    {
        if (Thread.State.WAITING == getState())
        {
            // notify anybody waiting on "this"
            notify();
        }
    }

    /**
     * Call this method to stop thread.
     */
    public void destroy()
    {
        m_isStop = true;

        if (Thread.State.WAITING == this.getState())
        {
            restart();
        }
    }

    /**
     * Call this method to pause this thread.
     */
    public void pause()
    {
        try
        {
            synchronized (this)
            {
                wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The thread working method.
     */
    public void run()
    {
        if (isStop())
        {
            return;
        }
        
        super.run();
    }
}
