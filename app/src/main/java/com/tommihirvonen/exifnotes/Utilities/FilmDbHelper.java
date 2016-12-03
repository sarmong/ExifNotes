package com.tommihirvonen.exifnotes.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tommihirvonen.exifnotes.Datastructures.Camera;
import com.tommihirvonen.exifnotes.Datastructures.Filter;
import com.tommihirvonen.exifnotes.Datastructures.Frame;
import com.tommihirvonen.exifnotes.Datastructures.Lens;
import com.tommihirvonen.exifnotes.Datastructures.Roll;

import java.util.ArrayList;


// Copyright 2015
// Tommi Hirvonen

/**
 * FilmDbHelper is the SQL database class that holds all the information
 * the user stores in the app. It contains all the rolls, frames, cameras and lenses.
 */
public class FilmDbHelper extends SQLiteOpenHelper {

    //=============================================================================================
    //Table names
    private static final String TABLE_FRAMES = "frames";
    private static final String TABLE_LENSES = "lenses";
    private static final String TABLE_ROLLS = "rolls";
    private static final String TABLE_CAMERAS = "cameras";
    private static final String TABLE_MOUNTABLES = "mountables";
    //Added in database version 14
    private static final String TABLE_FILTERS = "filters";

    //=============================================================================================
    //Column names
    private static final String KEY_FRAME_ID = "frame_id";
    private static final String KEY_COUNT = "count";
    private static final String KEY_DATE = "date";
    private static final String KEY_SHUTTER = "shutter";
    private static final String KEY_APERTURE = "aperture";
    private static final String KEY_FRAME_NOTE = "frame_note";
    private static final String KEY_LOCATION = "location";
    //Added in database version 14
    private static final String KEY_FOCAL_LENGTH = "focal_length";
    private static final String KEY_EXPOSURE_COMP = "exposure_comp";
    private static final String KEY_NO_OF_EXPOSURES = "no_of_exposures";
    private static final String KEY_FLASH_USED = "flash_used";
    private static final String KEY_FLASH_POWER = "flash_power";
    private static final String KEY_FLASH_COMP = "flash_comp";
    private static final String KEY_FRAME_SIZE = "frame_size";
    private static final String KEY_METERING_MODE = "metering_mode";

    private static final String KEY_LENS_ID = "lens_id";
    private static final String KEY_LENS_MAKE = "lens_make";
    private static final String KEY_LENS_MODEL = "lens_model";
    //Added in database version 14
    private static final String KEY_LENS_MAX_APERTURE = "lens_max_aperture";
    private static final String KEY_LENS_MIN_APERTURE = "lens_min_aperture";
    private static final String KEY_LENS_MAX_FOCAL_LENGTH = "lens_max_focal_length";
    private static final String KEY_LENS_MIN_FOCAL_LENGTH = "lens_min_focal_length";
    private static final String KEY_LENS_SERIAL_NO = "lens_serial_no";

    private static final String KEY_CAMERA_ID = "camera_id";
    private static final String KEY_CAMERA_MAKE = "camera_make";
    private static final String KEY_CAMERA_MODEL = "camera_model";
    //Added in database version 14
    private static final String KEY_CAMERA_MAX_SHUTTER = "camera_max_shutter";
    private static final String KEY_CAMERA_MIN_SHUTTER = "camera_min_shutter";
    private static final String KEY_CAMERA_SERIAL_NO = "camera_serial_no";

    private static final String KEY_ROLL_ID = "roll_id";
    private static final String KEY_ROLLNAME = "rollname";
    private static final String KEY_ROLL_DATE = "roll_date";
    private static final String KEY_ROLL_NOTE = "roll_note";
    //Added in database version 14
    private static final String KEY_ROLL_ISO = "roll_iso";
    private static final String KEY_ROLL_PUSH = "roll_push";
    private static final String KEY_ROLL_FORMAT = "roll_format";

    //Added in database version 14
    private static final String KEY_FILTER_ID = "filter_id";
    private static final String KEY_FILTER_MAKE = "filter_make";
    private static final String KEY_FILTER_MODEL = "filter_model";

    //=============================================================================================
    //Database information
    private static final String DATABASE_NAME = "filmnotes.db";

    //Updated version from 13 to 14 - 2016-12-03
    private static final int DATABASE_VERSION = 14;

    //=============================================================================================
    //onCreate strings
    private static final String CREATE_FRAME_TABLE = "create table " + TABLE_FRAMES
            + "(" + KEY_FRAME_ID + " integer primary key autoincrement, "
            + KEY_ROLL_ID + " integer not null, "
            + KEY_COUNT + " integer not null, "
            + KEY_DATE + " text not null, "
            + KEY_LENS_ID + " integer not null, "
            + KEY_SHUTTER + " text not null, "
            + KEY_APERTURE + " text not null, "
            + KEY_FRAME_NOTE + " text, "
            + KEY_LOCATION + " text, "
            + KEY_FOCAL_LENGTH + " integer, "
            + KEY_EXPOSURE_COMP + " text, "
            + KEY_NO_OF_EXPOSURES + " integer, "
            + KEY_FLASH_USED + " integer, "
            + KEY_FLASH_POWER + " text, "
            + KEY_FLASH_COMP + " text, "
            + KEY_FRAME_SIZE + " text, "
            + KEY_FILTER_ID + " integer, "
            + KEY_METERING_MODE + " text"
            + ");";
    private static final String CREATE_LENS_TABLE = "create table " + TABLE_LENSES
            + "(" + KEY_LENS_ID + " integer primary key autoincrement, "
            + KEY_LENS_MAKE + " text not null, "
            + KEY_LENS_MODEL + " text not null, "
            + KEY_LENS_MAX_APERTURE + " text, "
            + KEY_LENS_MIN_APERTURE + " text, "
            + KEY_LENS_MAX_FOCAL_LENGTH + " integer, "
            + KEY_LENS_MIN_FOCAL_LENGTH + " integer, "
            + KEY_LENS_SERIAL_NO + " text"
            + ");";
    private static final String CREATE_CAMERA_TABLE = "create table " + TABLE_CAMERAS
            + "(" + KEY_CAMERA_ID + " integer primary key autoincrement, "
            + KEY_CAMERA_MAKE + " text not null, "
            + KEY_CAMERA_MODEL + " text not null, "
            + KEY_CAMERA_MAX_SHUTTER + " text, "
            + KEY_CAMERA_MIN_SHUTTER + " text, "
            + KEY_CAMERA_SERIAL_NO + " text"
            + ");";
    private static final String CREATE_ROLL_TABLE = "create table " + TABLE_ROLLS
            + "(" + KEY_ROLL_ID + " integer primary key autoincrement, "
            + KEY_ROLLNAME + " text not null, "
            + KEY_ROLL_DATE + " text not null, "
            + KEY_ROLL_NOTE + " text, "
            + KEY_CAMERA_ID + " integer not null, "
            + KEY_ROLL_ISO + " integer, "
            + KEY_ROLL_PUSH + " text, "
            + KEY_ROLL_FORMAT + " text"
            + ");";
    private static final String CREATE_MOUNTABLES_TABLE = "create table " + TABLE_MOUNTABLES
            + "(" + KEY_CAMERA_ID + " integer not null, "
            + KEY_LENS_ID + " integer not null"
            + ");";

    private static final String CREATE_FILTER_TABLE = "create table " + TABLE_FILTERS
            + "(" + KEY_FILTER_ID + " integer primary key autoincrement, "
            + KEY_FILTER_MAKE + " text not null, "
            + KEY_FILTER_MODEL + " text not null"
            + ");";

    //=============================================================================================
    //onUpgrade strings
    private static final String ALTER_TABLE_FRAMES_1 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FOCAL_LENGTH + " integer;";
    private static final String ALTER_TABLE_FRAMES_2 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_EXPOSURE_COMP + " text;";
    private static final String ALTER_TABLE_FRAMES_3 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_NO_OF_EXPOSURES + " integer;";
    private static final String ALTER_TABLE_FRAMES_4 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FLASH_USED + " integer;";
    private static final String ALTER_TABLE_FRAMES_5 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FLASH_POWER + " text;";
    private static final String ALTER_TABLE_FRAMES_6 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FLASH_COMP + " text;";
    private static final String ALTER_TABLE_FRAMES_7 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FRAME_SIZE + " text;";
    private static final String ALTER_TABLE_FRAMES_8 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_FILTER_ID + " integer;";
    private static final String ALTER_TABLE_FRAMES_9 = "ALTER TABLE " + TABLE_FRAMES
            + " ADD COLUMN " + KEY_METERING_MODE + " text;";

    private static final String ALTER_TABLE_LENSES_1 = "ALTER TABLE " + TABLE_LENSES
            + " ADD COLUMN " + KEY_LENS_MAX_APERTURE + " text;";
    private static final String ALTER_TABLE_LENSES_2 = "ALTER TABLE " + TABLE_LENSES
            + " ADD COLUMN " + KEY_LENS_MIN_APERTURE + " text;";
    private static final String ALTER_TABLE_LENSES_3 = "ALTER TABLE " + TABLE_LENSES
            + " ADD COLUMN " + KEY_LENS_MAX_FOCAL_LENGTH + " integer;";
    private static final String ALTER_TABLE_LENSES_4 = "ALTER TABLE " + TABLE_LENSES
            + " ADD COLUMN " + KEY_LENS_MIN_FOCAL_LENGTH + " integer;";
    private static final String ALTER_TABLE_LENSES_5 = "ALTER TABLE " + TABLE_LENSES
            + " ADD COLUMN " + KEY_LENS_SERIAL_NO + " text;";

    private static final String ALTER_TABLE_CAMERAS_1 = "ALTER TABLE " + TABLE_CAMERAS
            + " ADD COLUMN " + KEY_CAMERA_MAX_SHUTTER + " text;";
    private static final String ALTER_TABLE_CAMERAS_2 = "ALTER TABLE " + TABLE_CAMERAS
            + " ADD COLUMN " + KEY_CAMERA_MIN_SHUTTER + " text;";
    private static final String ALTER_TABLE_CAMERAS_3 = "ALTER TABLE " + TABLE_CAMERAS
            + " ADD COLUMN " + KEY_CAMERA_SERIAL_NO + " text;";

    private static final String ALTER_TABLE_ROLLS_1 = "ALTER TABLE " + TABLE_ROLLS
            + " ADD COLUMN " + KEY_ROLL_ISO + " integer;";
    private static final String ALTER_TABLE_ROLLS_2 = "ALTER TABLE " + TABLE_ROLLS
            + " ADD COLUMN " + KEY_ROLL_PUSH + " text;";
    private static final String ALTER_TABLE_ROLLS_3 = "ALTER TABLE " + TABLE_ROLLS
            + " ADD COLUMN " + KEY_ROLL_FORMAT + " text;";

    private static final String REPLACE_QUOTE_CHARS = "UPDATE " + TABLE_FRAMES
            + " SET " + KEY_SHUTTER + " = REPLACE(" + KEY_SHUTTER + ", \'q\', \'\"\')"
            + " WHERE " + KEY_SHUTTER + " LIKE \'%q\';";



    public FilmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * When the database is first created, the required tables are created.
     * @param database the SQLite database to be populated.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_FRAME_TABLE);
        database.execSQL(CREATE_LENS_TABLE);
        database.execSQL(CREATE_ROLL_TABLE);
        database.execSQL(CREATE_CAMERA_TABLE);
        database.execSQL(CREATE_MOUNTABLES_TABLE);
        database.execSQL(CREATE_FILTER_TABLE);
    }

    /**
     * When the database version is changed to a newer one, this function is called.
     * @param db the database to be updated
     * @param oldVersion the old version number of the database
     * @param newVersion the new version number of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ( oldVersion <=13 ) {
            //TABLE_FRAMES
            db.execSQL(ALTER_TABLE_FRAMES_1);
            db.execSQL(ALTER_TABLE_FRAMES_2);
            db.execSQL(ALTER_TABLE_FRAMES_3);
            db.execSQL(ALTER_TABLE_FRAMES_4);
            db.execSQL(ALTER_TABLE_FRAMES_5);
            db.execSQL(ALTER_TABLE_FRAMES_6);
            db.execSQL(ALTER_TABLE_FRAMES_7);
            db.execSQL(ALTER_TABLE_FRAMES_8);
            db.execSQL(ALTER_TABLE_FRAMES_9);
            //TABLE_LENSES
            db.execSQL(ALTER_TABLE_LENSES_1);
            db.execSQL(ALTER_TABLE_LENSES_2);
            db.execSQL(ALTER_TABLE_LENSES_3);
            db.execSQL(ALTER_TABLE_LENSES_4);
            db.execSQL(ALTER_TABLE_LENSES_5);
            //TABLE_CAMERAS
            db.execSQL(ALTER_TABLE_CAMERAS_1);
            db.execSQL(ALTER_TABLE_CAMERAS_2);
            db.execSQL(ALTER_TABLE_CAMERAS_3);
            //TABLE_ROLLS
            db.execSQL(ALTER_TABLE_ROLLS_1);
            db.execSQL(ALTER_TABLE_ROLLS_2);
            db.execSQL(ALTER_TABLE_ROLLS_3);
            //In an earlier version special chars were not allowed.
            //Instead quote marks were changed to 'q' when stored in the SQLite database.
            db.execSQL(REPLACE_QUOTE_CHARS);
            //TABLE_FILTERS
            db.execSQL(CREATE_FILTER_TABLE);
        }
    }

    // ******************** CRUD operations for the frames table ********************

    /**
     * Adds a new frame to the database.
     * @param frame the new frame to be added to the database
     */
    public void addFrame(Frame frame) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();
        // Create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ROLL_ID, frame.getRoll());
        values.put(KEY_COUNT, frame.getCount());
        values.put(KEY_DATE, frame.getDate());
        values.put(KEY_LENS_ID, frame.getLensId());
        values.put(KEY_SHUTTER, frame.getShutter());
        values.put(KEY_APERTURE, frame.getAperture());
        values.put(KEY_FRAME_NOTE, frame.getNote());
        values.put(KEY_LOCATION, frame.getLocation());
        // Insert
        db.insert(TABLE_FRAMES, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ value
        // Close
        db.close();
    }

    /**
     * Gets the last inserted frame from the database.
     * @return the last inserted Frame
     */
    public Frame getLastFrame(){
        Frame frame = new Frame();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRAMES, null, null, null, null, null, KEY_FRAME_ID + " DESC", "1");
        if ( cursor != null ) {
            cursor.moveToFirst();
            frame.setId(cursor.getInt(cursor.getColumnIndex(KEY_FRAME_ID)));
            frame.setRoll(cursor.getInt(cursor.getColumnIndex(KEY_ROLL_ID)));
            frame.setCount(cursor.getInt(cursor.getColumnIndex(KEY_COUNT)));
            frame.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
            frame.setLensId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            frame.setShutter(cursor.getString(cursor.getColumnIndex(KEY_SHUTTER)));
            frame.setAperture(cursor.getString(cursor.getColumnIndex(KEY_APERTURE)));
            frame.setNote(cursor.getString(cursor.getColumnIndex(KEY_FRAME_NOTE)));
            frame.setLocation(cursor.getString(cursor.getColumnIndex(KEY_LOCATION)));
            cursor.close();
        }
        db.close();
        return frame;
    }

    /**
     * Gets all the frames from a specified roll.
     * @param roll_id the id of the roll
     * @return an array of Frames
     */
    public ArrayList<Frame> getAllFramesFromRoll(int roll_id){
        ArrayList<Frame> frames = new ArrayList<>();
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRAMES, null, KEY_ROLL_ID + "=?", new String[]{Integer.toString(roll_id)}, null, null, KEY_COUNT);
        Frame frame;
        // Go over each row, build list
        while ( cursor.moveToNext() ) {
            frame = new Frame();
            frame.setId(cursor.getInt(cursor.getColumnIndex(KEY_FRAME_ID)));
            frame.setRoll(cursor.getInt(cursor.getColumnIndex(KEY_ROLL_ID)));
            frame.setCount(cursor.getInt(cursor.getColumnIndex(KEY_COUNT)));
            frame.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
            frame.setLensId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            frame.setShutter(cursor.getString(cursor.getColumnIndex(KEY_SHUTTER)));
            frame.setAperture(cursor.getString(cursor.getColumnIndex(KEY_APERTURE)));
            frame.setNote(cursor.getString(cursor.getColumnIndex(KEY_FRAME_NOTE)));
            frame.setLocation(cursor.getString(cursor.getColumnIndex(KEY_LOCATION)));
            frames.add(frame);
        }
        cursor.close();
        db.close();
        return frames;
    }

    /**
     * Updates the information of a frame.
     * @param frame the frame to be updated.
     */
    public void updateFrame(Frame frame) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ROLL_ID, frame.getRoll());
        contentValues.put(KEY_COUNT, frame.getCount());
        contentValues.put(KEY_DATE, frame.getDate());
        contentValues.put(KEY_LENS_ID, frame.getLensId());
        contentValues.put(KEY_SHUTTER, frame.getShutter());
        contentValues.put(KEY_APERTURE, frame.getAperture());
        contentValues.put(KEY_FRAME_NOTE, frame.getNote());
        contentValues.put(KEY_LOCATION, frame.getLocation());
        db.update(TABLE_FRAMES, contentValues, KEY_FRAME_ID + "=?", new String[]{Integer.toString(frame.getId())});
        db.close();
    }

    /**
     * Deletes a frame from the database.
     * @param frame the frame to be deleted
     */
    public void deleteFrame(Frame frame) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete
        db.delete(TABLE_FRAMES,
                KEY_FRAME_ID + " = ?",
                new String[]{String.valueOf(frame.getId())});
        // Close
        db.close();
    }

    /**
     * Deletes all frames from a specified roll.
     * @param roll_id the id of the roll whose frames are to be deleted.
     */
    public void deleteAllFramesFromRoll(int roll_id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRAMES, KEY_ROLL_ID + " = ? ", new String[]{String.valueOf(roll_id)});
        db.close();
    }

    // ******************** CRUD operations for the lenses table ********************

    /**
     * Adds a new lens to the database.
     * @param lens the lens to be added to the database
     */
    public void addLens(Lens lens){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LENS_MAKE, lens.getMake());
        values.put(KEY_LENS_MODEL, lens.getModel());
        db.insert(TABLE_LENSES, null, values);
        db.close();
    }

    /**
     * Gets the last inserted lens from the database.
     * @return the last inserted lens
     */
    public Lens getLastLens(){
        Lens lens = new Lens();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LENSES, null, null, null, null, null, KEY_LENS_ID + " DESC", "1");
        if ( cursor != null ) {
            cursor.moveToFirst();
            lens.setId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            lens.setMake(cursor.getString(cursor.getColumnIndex(KEY_LENS_MAKE)));
            lens.setModel(cursor.getString(cursor.getColumnIndex(KEY_LENS_MODEL)));
            cursor.close();
        }
        db.close();
        return lens;
    }

    /**
     * Gets a lens corresponding to the id.
     * @param lens_id the id of the lens
     * @return a Lens corresponding to the id
     */
    public Lens getLens(int lens_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Lens lens = new Lens();
        Cursor cursor = db.query(TABLE_LENSES, null, KEY_LENS_ID + "=?", new String[]{Integer.toString(lens_id)}, null, null, null);
        if ( cursor != null ) {
            cursor.moveToFirst();
            lens.setId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            lens.setMake(cursor.getString(cursor.getColumnIndex(KEY_LENS_MAKE)));
            lens.setModel(cursor.getString(cursor.getColumnIndex(KEY_LENS_MODEL)));
            cursor.close();
        }
        db.close();
        return lens;
    }

    /**
     * Gets all the lenses from the database.
     * @return an ArrayList of all the lenses in the database.
     */
    public ArrayList<Lens> getAllLenses(){
        ArrayList<Lens> lenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LENSES, null, null, null, null, null, KEY_LENS_MAKE);
        Lens lens;
        while ( cursor.moveToNext() ) {
            lens = new Lens();
            lens.setId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            lens.setMake(cursor.getString(cursor.getColumnIndex(KEY_LENS_MAKE)));
            lens.setModel(cursor.getString(cursor.getColumnIndex(KEY_LENS_MODEL)));
            lenses.add(lens);
        }
        cursor.close();
        db.close();
        return lenses;
    }

    /**
     * Deletes Lens from the database.
     * @param lens the Lens to be deleted
     */
    public void deleteLens(Lens lens){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LENSES, KEY_LENS_ID + " = ?", new String[]{String.valueOf(lens.getId())});
        db.delete(TABLE_MOUNTABLES, KEY_LENS_ID + " = ?", new String[]{String.valueOf(lens.getId())});
        db.close();
    }

    /**
     * Checks if the lens is being used in some frame.
     * @param lens Lens to be checked
     * @return true if the lens is in use, false if not
     */
    public boolean isLensInUse(Lens lens){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRAMES, new String[]{KEY_LENS_ID}, KEY_LENS_ID + "=?", new String[]{Integer.toString(lens.getId())}, null, null, null);
        if ( cursor.moveToFirst() ) {
            cursor.close();
            db.close();
            return true;
        }
        else {
            cursor.close();
            db.close();
            return false;
        }
    }

    /**
     * Updates the information of a lens
     * @param lens the Lens to be updated
     */
    public void updateLens(Lens lens) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_LENS_MAKE, lens.getMake());
        contentValues.put(KEY_LENS_MODEL, lens.getModel());
        db.update(TABLE_LENSES, contentValues, KEY_LENS_ID + "=?", new String[]{Integer.toString(lens.getId())});
        db.close();
    }

    // ******************** CRUD operations for the cameras table ********************

    /**
     * Adds a new camera to the database.
     * @param camera the camera to be added to the database
     */
    public void addCamera(Camera camera){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CAMERA_MAKE, camera.getMake());
        values.put(KEY_CAMERA_MODEL, camera.getModel());
        db.insert(TABLE_CAMERAS, null, values);
        db.close();
    }

    /**
     * Gets the last inserted Camera.
     * @return the last inserted Camera
     */
    public Camera getLastCamera(){
        Camera camera = new Camera();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CAMERAS, null, null, null, null, null, KEY_CAMERA_ID + " DESC", "1");
        if (cursor != null ) {
            cursor.moveToFirst();
            camera.setId(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            camera.setMake(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MAKE)));
            camera.setModel(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MODEL)));
            cursor.close();
        }
        db.close();
        return camera;
    }

    /**
     * Gets the Camera corresponding to the camera id
     * @param camera_id the id of the Camera
     * @return the Camera corresponding to the given id
     */
    public Camera getCamera(int camera_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Camera camera = new Camera();
        Cursor cursor = db.query(TABLE_CAMERAS, null, KEY_CAMERA_ID + "=?", new String[]{Integer.toString(camera_id)}, null, null, null);
        if ( cursor != null ) {
            cursor.moveToFirst();
            camera.setId(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            camera.setMake(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MAKE)));
            camera.setModel(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MODEL)));
            cursor.close();
        }
        db.close();
        return camera;
    }

    /**
     * Gets all the cameras from the database
     * @return an ArrayList of all the cameras in the database
     */
    public ArrayList<Camera> getAllCameras(){
        ArrayList<Camera> cameras = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CAMERAS, null, null, null, null, null, KEY_CAMERA_MAKE);
        Camera camera;
        while ( cursor.moveToNext() ) {
            camera = new Camera();
            camera.setId(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            camera.setMake(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MAKE)));
            camera.setModel(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MODEL)));
            cameras.add(camera);
        }
        cursor.close();
        db.close();
        return cameras;
    }

    /**
     * Deletes the specified camera from the database
     * @param camera the camera to be deleted
     */
    public void deleteCamera(Camera camera){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CAMERAS, KEY_CAMERA_ID + " = ?", new String[]{String.valueOf(camera.getId())});
        db.delete(TABLE_MOUNTABLES, KEY_CAMERA_ID + " = ?", new String[]{String.valueOf(camera.getId())});
        db.close();
    }

    /**
     * Checks if a camera is being used in some roll.
     * @param camera the camera to be checked
     * @return true if the camera is in use, false if not
     */
    public boolean isCameraBeingUsed(Camera camera) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROLLS, new String[]{KEY_CAMERA_ID}, KEY_CAMERA_ID + "=?", new String[]{Integer.toString(camera.getId())}, null, null, null);
        if ( cursor.moveToFirst() ) {
            cursor.close();
            db.close();
            return true;
        }
        else {
            cursor.close();
            db.close();
            return false;
        }
    }

    /**
     * Updates the information of the specified camera.
     * @param camera the camera to be updated
     */
    public void updateCamera(Camera camera) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CAMERA_MAKE, camera.getMake());
        contentValues.put(KEY_CAMERA_MODEL, camera.getModel());
        db.update(TABLE_CAMERAS, contentValues, KEY_CAMERA_ID + "=?", new String[]{Integer.toString(camera.getId())});
        db.close();
    }

    // ******************** CRUD operations for the mountables table ********************

    /**
     * Adds a mountable combination of camera and lens to the database.
     * @param camera the camera that can be mounted with the lens
     * @param lens the lens that can be mounted with the camera
     */
    public void addMountable(Camera camera, Lens lens){
        SQLiteDatabase db = this.getWritableDatabase();
        //Here it is safe to use a raw query, because we only use id values, which are database generated.
        //So there is no danger of SQL injection.
        String query = "INSERT INTO " + TABLE_MOUNTABLES + "(" + KEY_CAMERA_ID + "," + KEY_LENS_ID
                + ") SELECT " + camera.getId() + ", " + lens.getId()
                + " WHERE NOT EXISTS(SELECT 1 FROM " + TABLE_MOUNTABLES + " WHERE "
                + KEY_CAMERA_ID + "=" + camera.getId() + " AND " + KEY_LENS_ID + "=" + lens.getId() + ")";
        db.execSQL(query);
        db.close();
    }

    /**
     * Deletes a mountable combination from the database
     * @param camera the camera that can be mounted with the lens
     * @param lens the lens that can be mounted with the camera
     */
    public void deleteMountable(Camera camera, Lens lens){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOUNTABLES, KEY_CAMERA_ID + " = ? AND " + KEY_LENS_ID + " = ?",
                    new String[]{Integer.toString(camera.getId()), Integer.toString(lens.getId())});
        db.close();
    }

    /**
     * Gets all the lenses that can be mounted to the specified camera
     * @param camera the camera whose lenses we want to get
     * @return an ArrayList of all the mountable lenses
     */
    public ArrayList<Lens> getMountableLenses(Camera camera){
        ArrayList<Lens> lenses = new ArrayList<>();
        //Here it is safe to use a raw query, because we only use id values, which are database generated.
        //So there is no danger of SQL injection
        String query = "SELECT * FROM " + TABLE_LENSES + " WHERE " + KEY_LENS_ID + " IN "
                + "(" + "SELECT " + KEY_LENS_ID + " FROM " + TABLE_MOUNTABLES + " WHERE "
                + KEY_CAMERA_ID + "=" + camera.getId() + ") ORDER BY " + KEY_LENS_MAKE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Lens lens;
        while ( cursor.moveToNext() ) {
            lens = new Lens();
            lens.setId(cursor.getInt(cursor.getColumnIndex(KEY_LENS_ID)));
            lens.setMake(cursor.getString(cursor.getColumnIndex(KEY_LENS_MAKE)));
            lens.setModel(cursor.getString(cursor.getColumnIndex(KEY_LENS_MODEL)));
            lenses.add(lens);
        }
        cursor.close();
        db.close();
        return lenses;
    }

    /**
     * Gets all the camras that can be mounted to the specified lens
     * @param lens the lens whose cameras we want to get
     * @return an ArrayList of all the mountable cameras
     */
    public ArrayList<Camera> getMountableCameras(Lens lens){
        ArrayList<Camera> cameras = new ArrayList<>();
        //Here it is safe to use a raw query, because we only use id values, which are database generated.
        //So there is no danger of SQL injection
        String query = "SELECT * FROM " + TABLE_CAMERAS + " WHERE " + KEY_CAMERA_ID + " IN "
                + "(" + "SELECT " + KEY_CAMERA_ID + " FROM " + TABLE_MOUNTABLES + " WHERE "
                + KEY_LENS_ID + "=" + lens.getId() + ") ORDER BY " + KEY_CAMERA_MAKE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Camera camera;
        while ( cursor.moveToNext() ) {
            camera = new Camera();
            camera.setId(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            camera.setMake(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MAKE)));
            camera.setModel(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MODEL)));
            cameras.add(camera);
        }
        cursor.close();
        db.close();
        return cameras;
    }

    // ******************** CRUD operations for the rolls table ********************

    /**
     * Adds a new roll to the database.
     * @param roll the roll to be added
     */
    public void addRoll(Roll roll){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROLLNAME, roll.getName());
        values.put(KEY_ROLL_DATE, roll.getDate());
        values.put(KEY_ROLL_NOTE, roll.getNote());
        values.put(KEY_CAMERA_ID, roll.getCamera_id());
        db.insert(TABLE_ROLLS, null, values);
        db.close();
    }

    /**
     * Gets the last inserted roll.
     * @return the last inserted roll
     */
    public Roll getLastRoll(){
        Roll roll = new Roll();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROLLS, null, null, null, null, null, KEY_ROLL_ID + " DESC", "1");
        if ( cursor != null ) {
            cursor.moveToFirst();
            roll.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROLL_ID)));
            roll.setName(cursor.getString(cursor.getColumnIndex(KEY_ROLLNAME)));
            roll.setDate(cursor.getString(cursor.getColumnIndex(KEY_ROLL_DATE)));
            roll.setNote(cursor.getString(cursor.getColumnIndex(KEY_ROLL_NOTE)));
            roll.setCamera_id(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            cursor.close();
        }
        db.close();
        return roll;
    }

    /**
     * Gets all the rolls in the database
     * @return an ArrayList of all the rolls in the database
     */
    public ArrayList<Roll> getAllRolls(){
        ArrayList<Roll> rolls = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROLLS, null, null, null, null, null, KEY_ROLL_DATE + " DESC");
        Roll roll;
        while ( cursor.moveToNext() ) {
            roll = new Roll();
            roll.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROLL_ID)));
            roll.setName(cursor.getString(cursor.getColumnIndex(KEY_ROLLNAME)));
            roll.setDate(cursor.getString(cursor.getColumnIndex(KEY_ROLL_DATE)));
            roll.setNote(cursor.getString(cursor.getColumnIndex(KEY_ROLL_NOTE)));
            roll.setCamera_id(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            rolls.add(roll);
        }
        cursor.close();
        db.close();
        return rolls;
    }

    /**
     * Gets the roll corresponding to the given id.
     * @param id the id of the roll
     * @return the roll corresponding to the given id
     */
    public Roll getRoll(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Roll roll = new Roll();
        Cursor cursor = db.query(TABLE_ROLLS, null, KEY_ROLL_ID + "=?", new String[]{Integer.toString(id)}, null, null, null);
        if ( cursor != null ) {
            cursor.moveToFirst();
            roll.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROLL_ID)));
            roll.setName(cursor.getString(cursor.getColumnIndex(KEY_ROLLNAME)));
            roll.setDate(cursor.getString(cursor.getColumnIndex(KEY_ROLL_DATE)));
            roll.setNote(cursor.getString(cursor.getColumnIndex(KEY_ROLL_NOTE)));
            roll.setCamera_id(cursor.getInt(cursor.getColumnIndex(KEY_CAMERA_ID)));
            cursor.close();
        }
        db.close();
        return roll;
    }

    /**
     * Deletes a roll from the database.
     * @param roll the roll to be deleted from the database
     */
    public void deleteRoll(Roll roll){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROLLS, KEY_ROLL_ID + " = ?", new String[]{String.valueOf(roll.getId())});
        db.close();
    }

    /**
     * Updates the specified roll's information
     * @param roll the roll to be updated
     */
    public void updateRoll(Roll roll){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ROLLNAME, roll.getName());
        contentValues.put(KEY_ROLL_DATE, roll.getDate());
        contentValues.put(KEY_ROLL_NOTE, roll.getNote());
        contentValues.put(KEY_CAMERA_ID, roll.getCamera_id());
        db.update(TABLE_ROLLS, contentValues, KEY_ROLL_ID + "=?", new String[]{Integer.toString(roll.getId())});
        db.close();
    }

    /**
     * Gets the number of frames on a specified roll.
     * @param roll the roll whose frame count we want
     * @return an integer of the the number of frames on that roll
     */
    public int getNumberOfFrames(Roll roll){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRAMES, new String[]{"COUNT(" + KEY_FRAME_ID + ")"}, KEY_ROLL_ID + "=?", new String[]{Integer.toString(roll.getId())}, null, null, null);
        int returnValue = 0;
        if ( cursor != null ) {
            cursor.moveToFirst();
            returnValue = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return returnValue;
    }


    // ******************** CRUD operations for the filters table ********************

    /**
     * Adds a new filter to the database.
     * @param filter the filter to be added to the database
     */
    public void addFilter(Filter filter){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FILTER_MAKE, filter.getMake());
        values.put(KEY_FILTER_MODEL, filter.getModel());
        db.insert(TABLE_FILTERS, null, values);
        db.close();
    }

    /**
     * Gets the last inserted Camera.
     * @return the last inserted Camera
     */
    public Filter getLastFilter(){
        Filter filter = new Filter();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FILTERS, null, null, null, null, null, KEY_FILTER_ID + " DESC", "1");
        if (cursor != null ) {
            cursor.moveToFirst();
            filter.setId(cursor.getInt(cursor.getColumnIndex(KEY_FILTER_ID)));
            filter.setMake(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MAKE)));
            filter.setModel(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MODEL)));
            cursor.close();
        }
        db.close();
        return filter;
    }

    /**
     * Gets the Filter corresponding to the filter id
     * @param filter_id the id of the Filter
     * @return the Filter corresponding to the given id
     */
    public Filter getFilter(int filter_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Filter filter = new Filter();
        Cursor cursor = db.query(TABLE_FILTERS, null, KEY_FILTER_ID + "=?", new String[]{Integer.toString(filter_id)}, null, null, null);
        if ( cursor != null ) {
            cursor.moveToFirst();
            filter.setId(cursor.getInt(cursor.getColumnIndex(KEY_FILTER_ID)));
            filter.setMake(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MAKE)));
            filter.setModel(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MODEL)));
            cursor.close();
        }
        db.close();
        return filter;
    }

    /**
     * Gets all the filters from the database
     * @return an ArrayList of all the filters in the database
     */
    public ArrayList<Filter> getAllFilters(){
        ArrayList<Filter> filters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FILTERS, null, null, null, null, null, KEY_FILTER_MAKE);
        Filter filter;
        while ( cursor.moveToNext() ) {
            filter = new Filter();
            filter.setId(cursor.getInt(cursor.getColumnIndex(KEY_FILTER_ID)));
            filter.setMake(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MAKE)));
            filter.setModel(cursor.getString(cursor.getColumnIndex(KEY_FILTER_MODEL)));
            filters.add(filter);
        }
        cursor.close();
        db.close();
        return filters;
    }

    /**
     * Deletes the specified filter from the database
     * @param filter the filter to be deleted
     */
    public void deleteFilter(Filter filter){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FILTERS, KEY_FILTER_ID + " = ?", new String[]{String.valueOf(filter.getId())});
        db.close();
    }

    /**
     * Checks if a filter is being used in some roll.
     * @param filter the filter to be checked
     * @return true if the filter is in use, false if not
     */
    public boolean isFilterBeingUsed(Filter filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRAMES, new String[]{KEY_FILTER_ID}, KEY_FILTER_ID + "=?", new String[]{Integer.toString(filter.getId())}, null, null, null);
        if ( cursor.moveToFirst() ) {
            cursor.close();
            db.close();
            return true;
        }
        else {
            cursor.close();
            db.close();
            return false;
        }
    }

    /**
     * Updates the information of the specified filter.
     * @param filter the filter to be updated
     */
    public void updateFilter(Filter filter) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_FILTER_MAKE, filter.getMake());
        contentValues.put(KEY_FILTER_MODEL, filter.getModel());
        db.update(TABLE_FILTERS, contentValues, KEY_FILTER_ID + "=?", new String[]{Integer.toString(filter.getId())});
        db.close();
    }
}
