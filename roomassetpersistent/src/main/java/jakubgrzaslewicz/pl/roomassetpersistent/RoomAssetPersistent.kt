/*
 * Copyright (c) 2018.
 * Created by Ibrahim Eid (https://github.com/humazed)
 * Modified by Jakub Grząślewicz (https://github.com/jakubgrzaslewicz)
 *
 */

package jakubgrzaslewicz.pl.roomassetpersistent


import android.content.Context
import android.content.res.AssetManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.*

import java.nio.file.AccessMode
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * A helper class to manage database creation and version management using
 * an application's raw asset files.
 *
 * This class provides developers with a simple way to ship their Android app
 * with an existing SQLite database (which may be pre-populated with data) and
 * to manage its initial creation and any upgrades required with subsequent
 * version releases.
 *
 *
 * This class makes it easy for [android.content.ContentProvider]
 * implementations to defer opening and upgrading the database until first use,
 * to avoid blocking application startup with long-running database upgrades.
 *
 *
 * For examples see [
 * https://github.com/jgilfelt/android-sqlite-asset-helper](https://github.com/jgilfelt/android-sqlite-asset-helper)
 *
 *
 * **Note:** this class assumes
 * monotonically increasing version numbers for upgrades.  Also, there
 * is no concept of a database downgrade; installing a new version of
 * your app which uses a lower version number than a
 * previously-installed version will result in undefined behavior.
 */
class roomassetpersistent
/**
 * Create a helper object to create, open, and/or manage a database in
 * a specified location.
 * This method always returns very quickly.  The database is not actually
 * created or opened until one of [.getWritableDatabase] or
 * [.getReadableDatabase] is called.
 *
 * @param context to use to open or create the database
 * @param name of the database file
 * @param storageDirectory to store the database file upon creation; caller must
 * ensure that the specified absolute path is available and can be written to
 * @param factory to use for creating cursor objects, or null for the default
 * @param version number of the database (starting at 1); if the database is older,
 * SQL file(s) contained within the application assets folder will be used to
 * upgrade the database
 */
(private val mContext: Context, private val mName: String?, storageDirectory: String?, private val mFactory: CursorFactory?) : SQLiteOpenHelper(mContext, mName, mFactory, 1) {

    private var mDatabase: SQLiteDatabase? = null
    private var mIsInitializing = false

    private var mDatabasePath: String? = null

    private val mAssetPath: String

    private var forcedUpgrade = false

    init {
        if (mName == null) throw IllegalArgumentException("Database name cannot be null")

        mAssetPath = "$ASSET_DB_PATH/$mName"
        if (storageDirectory != null) {
            mDatabasePath = storageDirectory
        } else {
            mDatabasePath = mContext.getDatabasePath("a").parentFile.absolutePath
        }
    }


    /**
     * Read content of file in [mDatabasePath] directory that's name equals [mName].ver
     *
     * If file not found returns null;
     *
     * @return content of .ver file as String (content wil be trimmed from leading and trailing whitespaces)
     */
    val extractedDatabaseVersion = fun(): String? {
        val file = File("$mDatabasePath/$mName.ver")
        if (!file.exists())
            return null
        else {
            return file.readText().trim()
        }
    }

    /**
     * Read content of file in assets with name equals [mName].ver directly or in zip file.
     *
     * If .ver file not found in assets, will look for [mName].zip file
     * and iterate over every file in archive and return content of it as String
     * if name equals [mName].ver
     *
     * Returned value will be saved in memory and served to every other call.
     * Thanks to kotlin's lazy function.
     *
     *
     * @throws FileNotFoundException if .ver file not found
     * @return content of .ver file as String (content wil be trimmed from leading and trailing whitespaces)
     */
    val includedDatabaseVersion: String by lazy {
        var ver: InputStream?
        try {
            ver = mContext.assets.open("$ASSET_DB_PATH/$mName.ver")
            ver.bufferedReader().use { it.readText() }.trim()
        } catch (ignored: Exception) {
            ver = mContext.assets.open("$mAssetPath.zip")
            if (ver == null)
                throw FileNotFoundException("Cannot find $mAssetPath.zip file in your assets")

            val zip = ZipInputStream(BufferedInputStream(ver))
            var entry: ZipEntry? = zip.nextEntry
            var version = ""
            while (entry != null) {
                if (entry.name == "$mName.ver") {
                    version = zip.reader().readText()
                    zip.closeEntry()
                }
                entry = zip.nextEntry
            }
            zip.close()
            if (version.isEmpty())
                throw FileNotFoundException("Cannot find $mAssetPath.zip and $mAssetPath.ver files in your assets.")
            version
        }
    }


    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be extracted and copied
     * from the application's assets folder.
     *
     *
     * Once opened successfully, the database is cached, so you can
     * call this method every time you need to write to the database.
     * (Make sure to call [.close] when you no longer need the database.)
     * Errors such as bad permissions or a full disk may cause this method
     * to fail, but future attempts may succeed if the problem is fixed.
     *
     *
     * Database upgrade may take a long time, you
     * should not call this method from the application main thread, including
     * from [ContentProvider.onCreate()][android.content.ContentProvider.onCreate].
     *
     * @throws SQLiteException,[IllegalStateException] if the database cannot be opened for writing
     * @return a read/write database object valid until [.close] is called
     */
    @Synchronized
    override fun getWritableDatabase(): SQLiteDatabase {
        if (mDatabase != null && mDatabase!!.isOpen && !mDatabase!!.isReadOnly) {
            return mDatabase!!  // The database is already open for business
        }

        if (mIsInitializing) {
            throw IllegalStateException("getWritableDatabase called recursively")
        }

        var success = false
        var db: SQLiteDatabase? = null
        try {
            mIsInitializing = true
            db = createOrOpenDatabase(false)
            val version: String? = extractedDatabaseVersion.invoke()

            Log.i(TAG, "Current extracted file version: " + version.toString())

            // Delete extracted file if version is not equal to the newest version
            val newVersion: String = includedDatabaseVersion
            if (version != newVersion) {
                Log.i(TAG, "Replacing old DB with version $version to $newVersion")
                try {
                    db?.close()
                    File("$mDatabasePath/$mName").delete()
                    File("$mDatabasePath/$mName-shm").delete()
                    File("$mDatabasePath/$mName-wal").delete()
                    File("$mDatabasePath/$mName.ver").delete()
                    db = createOrOpenDatabase(true)
                } catch (e: SQLiteException) {
                    e.printStackTrace()
                    throw SQLiteException("Can't upgrade database file.", e as Throwable)
                }
            }
            Log.i(TAG, "Database is now up to date: $newVersion")

            onOpen(db)
            success = true
            return db!!
        } finally {
            mIsInitializing = false
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase!!.close()
                    } catch (ignored: Exception) {
                    }
                }
                mDatabase = db
            } else {
                db?.close()
            }
        }

    }

    /**
     * Create and/or open a database.  This will be the same object returned by
     * [.getWritableDatabase] unless some problem, such as a full disk,
     * requires the database to be opened read-only.  In that case, a read-only
     * database object will be returned.  If the problem is fixed, a future call
     * to [.getWritableDatabase] may succeed, in which case the read-only
     * database object will be closed and the read/write object will be returned
     * in the future.
     *
     *
     * Like [.getWritableDatabase], this method may
     * take a long time to return, so you should not call it from the
     * application main thread, including from
     * [ContentProvider.onCreate()][android.content.ContentProvider.onCreate].
     *
     * @throws SQLiteException if the database cannot be opened
     * @return a database object valid until [.getWritableDatabase]
     * or [.close] is called.
     */
    @Synchronized
    override fun getReadableDatabase(): SQLiteDatabase {
        if (mDatabase != null && mDatabase!!.isOpen) {
            return mDatabase!!  // The database is already open for business
        }

        if (mIsInitializing) {
            throw IllegalStateException("getReadableDatabase called recursively")
        }

        try {
            return writableDatabase
        } catch (e: SQLiteException) {
            if (mName == null) throw e  // Can't open a temp database read-only!
            Log.e(TAG, "Couldn't open $mName for writing (will try read-only):", e)
        }

        var db: SQLiteDatabase? = null
        try {
            mIsInitializing = true
            val path = mContext.getDatabasePath(mName).path
            db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY)
            if (extractedDatabaseVersion.invoke() != includedDatabaseVersion) {
                throw SQLiteException("Can't upgrade read-only database from version " +
                        db.version + " to " + includedDatabaseVersion + ": " + path)
            }

            onOpen(db)
            Log.w(TAG, "Opened $mName in read-only mode")
            mDatabase = db
            return mDatabase!!
        } finally {
            mIsInitializing = false
            if (db != null && db != mDatabase) db.close()
        }
    }

    /**
     * Close any open database object.
     */
    @Synchronized
    override fun close() {
        if (mIsInitializing) throw IllegalStateException("Closed during initialization")

        if (mDatabase != null && mDatabase!!.isOpen) {
            mDatabase!!.close()
            mDatabase = null
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        // not supported!
    }

    override fun onCreate(db: SQLiteDatabase) {
        // do nothing - createOrOpenDatabase() is called in
        // getWritableDatabase() to handle database creation.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // not supported!
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // not supported!
    }

    @Throws(SQLiteAssetException::class)
    private fun createOrOpenDatabase(force: Boolean): SQLiteDatabase? {

        // test for the existence of the db file first and don't attempt open
        // to prevent the error trace in log on API 14+
        var db: SQLiteDatabase? = null
        val file = File("$mDatabasePath/$mName")
        if (file.exists()) {
            db = returnDatabase()
        }
        //SQLiteDatabase db = returnDatabase();

        if (db != null) {
            // database already exists
            if (force) {
                Log.w(TAG, "forcing database upgrade!")
                copyDatabaseFromAssets()
                db = returnDatabase()
            }
            return db
        } else {
            // database does not exist, copy it from assets and return it
            copyDatabaseFromAssets()
            db = returnDatabase()
            return db
        }
    }

    private fun returnDatabase(): SQLiteDatabase? {
        try {
            val db = SQLiteDatabase.openDatabase("$mDatabasePath/$mName", mFactory, SQLiteDatabase.OPEN_READWRITE)
            Log.i(TAG, "successfully opened database " + mName!!)
            return db
        } catch (e: SQLiteException) {
            Log.w(TAG, "could not open database " + mName + " - " + e.message)
            return null
        }

    }

    @Throws(SQLiteAssetException::class)
    private fun copyDatabaseFromAssets() {
        Log.w(TAG, "copying database from assets...")

        val path = mAssetPath
        val dest = "$mDatabasePath/$mName"
        val input: ArrayList<InputStream> = ArrayList()
        var isZip = false

        try {
            // try uncompressed
            input.add(mContext.assets.open(path))
            input.add(mContext.assets.open("$path.ver"))
        } catch (e: IOException) {
            // try zip
            try {
                input.add(mContext.assets.open("$path.zip"))
                isZip = true
            } catch (e2: IOException) {
                val se = SQLiteAssetException("Missing $mAssetPath file (or .zip) in assets, or target folder not writable", e2 as Throwable)
                throw se
            }

        }

        try {
            val f: File?
            if (!mDatabasePath!!.endsWith("/"))
                f = File(mDatabasePath!!)
            else
                f = File(mDatabasePath!! + "/")

            if (!f.exists()) {
                f.mkdir()
            }
            if (isZip) {
                input.forEach {
                    Utils.ExtractZip(it, f.absolutePath)
                }
            } else {
                input.forEach {
                    Utils.writeExtractedFileToDisk(it, FileOutputStream(dest))
                }
            }

            Log.w(TAG, "database copy complete")

        } catch (e: IOException) {
            e.printStackTrace()
            val se = SQLiteAssetException("Unable to write $dest to data directory", e as Throwable)
            se.stackTrace = e.stackTrace
            throw se
        }

    }

    /**
     * An exception that indicates there was an error with SQLite asset retrieval or parsing.
     */
    class SQLiteAssetException(error: String, cause: Throwable) : SQLiteException(error) {
        init {
            this.stackTrace = cause.stackTrace
        }
    }

    companion object {

        val TAG = roomassetpersistent::class.java.simpleName
        private val ASSET_DB_PATH = "databases"
    }

}
/**
 * Bypass the upgrade process for every version increment and simply overwrite the existing
 * database with the supplied asset file.
 */
