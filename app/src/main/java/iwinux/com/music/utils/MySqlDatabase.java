package iwinux.com.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Audio;

public class MySqlDatabase extends SQLiteOpenHelper {
    static public String databaseName = "AllMusic.db";
    static public String TABLE_HISTORY = "HISTORY_TABLE";
    static public String TABLE_ALBUMS = "ALBUMS_TABLE";
    static public String TYPE_DOWNLOAD =  "DOWNLOAD";
    static public String TYPE_LIKE =  "LIKE";
    //Audio Columns
    String COLUMN_TITLE ="TITLE";
    String COLUMN_Artist ="Artist";
    String COLUMN_Album ="Album";
    String COLUMN_duration ="Duration";
    String COLUMN_AudioId ="AudioId";
    String COLUMN_ImgUrl ="ImgUrl";
    String COLUMN_pallete ="Pallete";
    String COLUMN_AlbumId ="AlbumId";
    String COLUMN_ArtistId ="ArtistId";
    String COLUMN_TYPE = "COLUMN_TYPE";
    String COLUMN_path ="Path";
    String TAG = "DATABASE";
    //Album Column
    String COLUMN_IDS_TRACK = "IDS_TRACK";
    static public String TABLE_AUDIOS_USER = "AUDIOS_USER_TABLE";
    public MySqlDatabase(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, databaseName, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAudio_UserTable = "CREATE TABLE " + TABLE_AUDIOS_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AudioId + " TEXT NOT NULL, " +
                COLUMN_ImgUrl + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_Artist + " TEXT NOT NULL, " +
                COLUMN_AlbumId + " TEXT NOT NULL, " +
                COLUMN_Album + " TEXT NOT NULL, " +
                COLUMN_duration + " INTEGER NOT NULL, " +
                COLUMN_ArtistId + " TEXT NOT NULL, " +
                COLUMN_pallete + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                COLUMN_path + " TEXT " + ")";
        db.execSQL(createAudio_UserTable);
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY+ " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AudioId + " TEXT NOT NULL, " +
                COLUMN_ImgUrl + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_Artist + " TEXT NOT NULL, " +
                COLUMN_AlbumId + " TEXT NOT NULL, " +
                COLUMN_Album + " TEXT NOT NULL, " +
                COLUMN_duration + " INTEGER NOT NULL, " +
                COLUMN_ArtistId + " TEXT NOT NULL, " +
                COLUMN_pallete + " TEXT NOT NULL, " +
                COLUMN_path + " TEXT " + ")";
        db.execSQL(createHistoryTable);
        String createAlbumTable = "CREATE TABLE " + TABLE_ALBUMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_IDS_TRACK + " TEXT NOT NULL, " +
                COLUMN_ImgUrl + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_Artist + " TEXT NOT NULL, " +
                COLUMN_AlbumId + " TEXT NOT NULL, " +
                COLUMN_Album + " TEXT NOT NULL, " +
                COLUMN_ArtistId + " TEXT NOT NULL, " +
                COLUMN_pallete + " TEXT NOT NULL " +
             ")";
        db.execSQL(createAlbumTable);

        Log.i( TAG, "onCreate: OK " );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<Audio> loadAudios(String type, String table) {
        List<Audio> audios= new ArrayList<>(  );
        String query = "Select * FROM " + table +" WHERE " + COLUMN_TYPE + " = " + "'" + type + "'" +" ORDER BY id ASC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
              Audio audio = new Audio();
              if(type.equals( TYPE_DOWNLOAD ))
              audio.setPath( cursor.getString( 11 ) );
              audio.setId( cursor.getString( 1 ) );
              audio.setImgUrl( cursor.getString( 2 ) );
              audio.setTitle( cursor.getString( 3 ) );
              audio.setArtist( cursor.getString( 4 ) );
              audio.setAlbumid( cursor.getString( 5 ) );
              audio.setAlbumname( cursor.getString( 6 ) );
              audio.setDuration( cursor.getInt( 7 ) );
              audio.setArtistid( cursor.getString( 8 ) );
              audio.setPallete( cursor.getString( 9 ) );
              audios.add( audio );
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return audios;
    }
    public List<Audio> loadAudiosHistory(int count) {
        List<Audio> audios= new ArrayList<>(  );
        String query = "Select * FROM " + TABLE_HISTORY+" ORDER BY id ASC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
                Audio audio = new Audio();
                audio.setId( cursor.getString( 1 ) );
                audio.setImgUrl( cursor.getString( 2 ) );
                audio.setTitle( cursor.getString( 3 ) );
                audio.setArtist( cursor.getString( 4 ) );
                audio.setAlbumid( cursor.getString( 5 ) );
                audio.setAlbumname( cursor.getString( 6 ) );
                audio.setDuration( cursor.getInt( 7 ) );
                audio.setArtistid( cursor.getString( 8 ) );
                audio.setPallete( cursor.getString( 9 ) );
                audios.add( audio );
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return audios;
    }
//    rownum < 100000000
    public Audio getAudio(Audio a,String table,String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if(!a.getId().contains( "vk:" ))
           cursor = db.rawQuery("SELECT * FROM "
                        + table + " WHERE "+
                COLUMN_AudioId + "=? AND "+COLUMN_TITLE + "=? AND "+COLUMN_Artist + "=? AND " +COLUMN_TYPE+"=?",
                new String[] { a.getId(),a.getTitle(),a.getArtist(),type});
        else
           cursor = db.rawQuery("SELECT * FROM "
                            + table + " WHERE "+COLUMN_TITLE + "=? AND "+COLUMN_Artist + "=? AND " +COLUMN_TYPE+"=? AND "
                   +COLUMN_ImgUrl+"=?",
                    new String[] { a.getTitle(),a.getArtist(),type,a.getImgUrl()});
        if(cursor != null && cursor.moveToFirst()) {
            Audio audio = new Audio();
            Log.e( TAG, "getAudio: "  );
            if(type.equals( TYPE_DOWNLOAD ))
                audio.setPath( cursor.getString( 11 ) );
            audio.setId( cursor.getString( 1 ) );
            audio.setImgUrl( cursor.getString( 2 ) );
            audio.setTitle( cursor.getString( 3 ) );
            audio.setArtist( cursor.getString( 4 ) );
            audio.setAlbumid( cursor.getString( 5 ) );
            audio.setAlbumname( cursor.getString( 6 ) );
            audio.setDuration( cursor.getInt( 7 ) );
            audio.setArtistid( cursor.getString( 8 ) );
            audio.setPallete( cursor.getString( 9 ) );
            return audio;
        }
        else{
            return null;
        }

    }
    public void addAudio(Audio audio,String type,String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e( TAG, "addAudio: "+audio.getPath() );
        ContentValues cv = new ContentValues();
        cv.put( COLUMN_TITLE, audio.getTitle());
        cv.put( COLUMN_Album,audio.getAlbumname() );
        cv.put( COLUMN_AlbumId,audio.getAlbumid() );
        cv.put( COLUMN_Artist,audio.getArtist() );
        cv.put( COLUMN_ArtistId,audio.getArtistid() );
        cv.put( COLUMN_duration,audio.getDuration() );
        cv.put( COLUMN_ImgUrl,audio.getImgUrl() );
        cv.put( COLUMN_pallete,audio.getPallete() );
        if(type.equals( TYPE_DOWNLOAD ))
            cv.put( COLUMN_path,audio.getPath() );

        cv.put( COLUMN_TYPE,type );
        cv.put( COLUMN_AudioId,audio.getId() );
        db.insert(table, null, cv);
        db.close();

    }
    public void addHistoryAudio(Audio audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e( TAG, "addAudio: "+audio.getPath() );
        ContentValues cv = new ContentValues();
        cv.put( COLUMN_TITLE, audio.getTitle());
        cv.put( COLUMN_Album,audio.getAlbumname() );
        cv.put( COLUMN_AlbumId,audio.getAlbumid() );
        cv.put( COLUMN_Artist,audio.getArtist() );
        cv.put( COLUMN_ArtistId,audio.getArtistid() );
        cv.put( COLUMN_duration,audio.getDuration() );
        cv.put( COLUMN_ImgUrl,audio.getImgUrl() );
        cv.put( COLUMN_pallete,audio.getPallete() );
        cv.put( COLUMN_AudioId,audio.getId() );
        db.insert(TABLE_HISTORY, null, cv);
        db.close();

    }
    public void addAlbum(Album album){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put( COLUMN_TITLE, album.getTitle());
        cv.put( COLUMN_AlbumId, album.getId() );
        cv.put( COLUMN_Artist, album.getArtist() );
        cv.put( COLUMN_ArtistId,album.getIdArtist() );
        cv.put( COLUMN_Artist,album.getArtist() );
        cv.put( COLUMN_ImgUrl,album.getImgurl() );
        cv.put( COLUMN_pallete,album.getPalette() );
        db.insert(TABLE_ALBUMS, null, cv);
        db.close();
    }
    public int deleteAudio(Audio audio,String tablename,String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(type.equals( TYPE_DOWNLOAD ) || type.equals( TYPE_LIKE ))
        return db.delete(tablename, COLUMN_AudioId + "=? AND "+COLUMN_Artist+"=? "+"AND "
                        +COLUMN_TITLE+"=? "+"AND "+COLUMN_TYPE+"=?",
                new String[] { audio.getId(),audio.getArtist(),audio.getTitle(),type });
        else
            return db.delete(tablename, COLUMN_AudioId + "=?"+" , "+COLUMN_Artist+"=?"+" , "
                            +COLUMN_TITLE+"=?",
                    new String[] { audio.getId(),audio.getArtist(),audio.getTitle()});
    }
    public int deleteAlbum(Album album) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ALBUMS, COLUMN_AlbumId + "=?",
                new String[] { album.getId() });
    }
}
