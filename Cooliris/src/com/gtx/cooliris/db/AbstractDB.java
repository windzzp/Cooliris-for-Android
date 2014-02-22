package com.gtx.cooliris.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.gtx.cooliris.app.CoolirisApplication;

/**
 * This class defines the abstract database processing.
 */
public class AbstractDB {
    /**
     * The MelonpanDBHelper instance.
     */
    private ImageDBHelper m_dbHelper = null;

    /**
     * The constructor method.
     * 
     * @param context The context object.
     */
    public AbstractDB() {
        Context context = CoolirisApplication.getAppContext();
        if (null != context) {
            m_dbHelper = ImageDBHelper.getInstance(context);
        }
    }

    /**
     * Create and/or open a database that will be used for reading and writing.
     * 
     * @return a read/write database object valid until {@link #close} is called.
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        return (null != m_dbHelper) ? m_dbHelper.getWritableDatabase() : null;
    }

    /**
     * Create and/or open a database.
     * 
     * @return a database object valid until {@link #getWritableDatabase} or {@link #close} is
     *         called.
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        return (null != m_dbHelper) ? m_dbHelper.getReadableDatabase() : null;
    }

    /**
     * Close the data base if it is opened.
     * 
     * @return true if succeeds, otherwise false.
     */
    public synchronized boolean closeDB(SQLiteDatabase db) {
        //
        // In the whole life cycle of the application, we do not close the database.
        // When the application closes, we only close the database.
        /**
         * if /null != db && db.isOpen()) { db.close(); }
         */

        return true;
    }

    /**
     * Close the cursor, if it is open.
     * 
     * @param cur to be closed cursor.
     */
    public synchronized void closeCursor(Cursor cur) {
        if (null != cur && !cur.isClosed()) {
            cur.close();
        }
    }

    /**
     * Execute a specified SQL.
     * 
     * @param db        The database to be operated by SQL.
     * @param sql       The string of SQL.
     * @param bindArgs  only byte[], String, Long and Double are supported in bindArgs.
     */
    public synchronized boolean execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) {
        try {
            if (null == bindArgs) {
                db.execSQL(sql);
            } else {
                db.execSQL(sql, bindArgs);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
