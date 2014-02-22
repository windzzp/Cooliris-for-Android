package com.gtx.cooliris.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is used to create database for this application..
 */
public class ImageDBHelper extends SQLiteOpenHelper {
    /**
     * The database name.
     */
    // public final static String DATABASE_NAME = "girls.db";
    public final static String DATABASE_NAME = "sudasuta.db";

    /**
     * The lock object for locking the lasy loading instance.
     */
    private static Object m_lasyLoadingLock = new Object();

    /**
     * The database version.
     */
    public final static int DATABASE_VERSION = 1;

    /**
     * The instance of MelonpanDBHelper class.
     */
    private static volatile ImageDBHelper m_pascalDBHelper = null;

    /**
     * The database instance.
     */
    private SQLiteDatabase m_databaseInstance = null;

    /**
     * Call this method to create a database helper instance.
     * 
     * @param context to use to open or create the database.
     * 
     * @return The instance of the MelonpanDBHelper class.
     */
    public static ImageDBHelper getInstance(Context context) {
        synchronized (m_lasyLoadingLock) {
            // If the instance is null, allocate memory for it.
            if (null == m_pascalDBHelper) {
                m_pascalDBHelper = new ImageDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);

                // This calling will lead to create a actually database.
                m_pascalDBHelper.getWritableDatabase();
            }
            return m_pascalDBHelper;
        }
    }

    /**
     * Create a helper object to create, open, and/or manage a database. The database 
     * is not actually created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     * 
     * @param context to use to open or create the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *            {@link #onUpgrade} will be used to upgrade the database
     */
    private ImageDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     * 
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    /**
     * Called when the database needs to be upgraded. The implementation should
     * use this method to drop tables, add tables, or do anything else it needs
     * to upgrade to the new schema version.
     * 
     * <p>
     * The SQLite ALTER TABLE documentation can be found <a
     * href="http://sqlite.org/lang_altertable.html">here</a>. If you add new
     * columns you can use ALTER TABLE to insert them into a live table. If you
     * rename or remove columns you can use ALTER TABLE to rename the old table,
     * then create the new table and then populate the new table with the
     * contents of the old table.
     * 
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            try {
                db.beginTransaction();
                /* db.execSQL("DROP TABLE IF EXISTS " + AudioExInfoDB.AUDIO_EX_TABLE_NAME); */
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        onCreate(db);
    }

    /**
     * Called when the database has been opened. The implementation should check
     * {@link SQLiteDatabase#isReadOnly} before updating the database.
     * 
     * @param db The database.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
            db.execSQL("PRAGMA default_cache_size = 10000;");
        }
    }
    
    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be opened and
     * {@link #onCreate}, {@link #onUpgrade} and/or {@link #onOpen} will be
     * called.
     */
    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (null == m_databaseInstance) {
            m_databaseInstance = super.getWritableDatabase();
        }

        return m_databaseInstance;
    }
    
    /**
     * Create and/or open a database.  This will be the same object returned by
     * {@link #getWritableDatabase} unless some problem, such as a full disk,
     * requires the database to be opened read-only.  In that case, a read-only
     * database object will be returned.  If the problem is fixed, a future call
     * to {@link #getWritableDatabase} may succeed, in which case the read-only
     * database object will be closed and the read/write object will be returned
     * in the future.
     */
    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        // return super.getReadableDatabase();

        // Always return the writable database.
        return getWritableDatabase();
    }
    
    /**
     * Close the database.
     */
    @Override
    public synchronized void close() {
        super.close();

        final SQLiteDatabase db = m_databaseInstance;

        if (null != db) {
            if (db.isOpen()) {
                db.close();
            }
        }

        m_databaseInstance = null;
    }
}
