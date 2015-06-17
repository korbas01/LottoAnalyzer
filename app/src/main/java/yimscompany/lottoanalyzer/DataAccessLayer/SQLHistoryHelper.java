package yimscompany.lottoanalyzer.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;

/**
 * Created by shyim on 15-05-30.
 */
public class SQLHistoryHelper extends SQLiteOpenHelper {
    private static final String HISTORY_COL_NAME_PREFIX_NUMBER = "num";
    private static final String HISTORY_COL_NAME_DATE = "date";

    public static final int DATA_BASE_VER = 1;

    private static final String HISTORY_DATABASE_FILE_NAME = "myhistory.db";


    private LottoGame mSelectedGame;
//    private static SQLHistoryHelper sInstance;
//
//    public static synchronized SQLHistoryHelper getInstance(Context context, LottoGame g) {
//
//        // Use the application context, which will ensure that you
//        // don't accidentally leak an Activity's context.
//        // See this article for more information: http://bit.ly/6LRzfx
//        if (sInstance == null) {
//            sInstance = new SQLHistoryHelper(context.getApplicationContext(), g);
//        }
//        return sInstance;
//    }

    public SQLHistoryHelper(Context context, LottoGame aGame) {
        super(context, HISTORY_DATABASE_FILE_NAME, null, DATA_BASE_VER);
        mSelectedGame = aGame;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //generating SQL Query based on LottoGame object info.
        String createSQLQuery = "CREATE TABLE " + this.mSelectedGame.getTableName() + " (";
        for(int i =0 ; i < this.mSelectedGame.getSetOfNums(); i ++) {
            createSQLQuery += HISTORY_COL_NAME_PREFIX_NUMBER + i + " INTEGER NOT NULL, ";
        }

        createSQLQuery += HISTORY_COL_NAME_DATE + " TEXT);";// PRIMARY KEY, UNIQUE (date) ON CONFLICT REPLACE);";
        database.execSQL(createSQLQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
        if(this.mSelectedGame == null) {
            throw new NullPointerException("LottoGame History DB has not been initialized");
        }
        database.execSQL("DROP TABLE IF EXISTS " + this.mSelectedGame.getTableName());
        onCreate(database);
    }


    public void addMyHistory(ArrayList<LottoRecord> records) {
        if(records != null && records.size() > 0) {
            deleteAllRecords();
            SQLiteDatabase db=this.getWritableDatabase();
            try{
                for(LottoRecord r : records){
                    ContentValues content = new ContentValues();
                    for(int i =0; i < r.getWinningNums().size(); i++) {
                        content.put(HISTORY_COL_NAME_PREFIX_NUMBER + i, r.getWinningNums().get(i).intValue());
                    }
                    content.put(HISTORY_COL_NAME_DATE, getCurrentTimeStamp());
                    long result = db.insert(mSelectedGame.getTableName(), null, content);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }finally{
                db.close();
            }
        }
    }

    /**
     *
     * @return true if db contains history otherwise return false
     */
    public boolean HistoryExist(LottoGame game) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor c = db.rawQuery(rawQuery, null);
        try{
//            Cursor c = db.query(game.getTableName(), //table
//                    null, //all columns
//                    null,//selection,
//                    null,//selectionArgs,
//                    null,//groupBy,
//                    null,//having,
//                    null);//orderBy
            Cursor c = db.rawQuery("select * from "+ game.getTableName(),null);
            if(c.moveToFirst()){
                return true;
            }
        }catch(Exception e){
            return false;
        }
        return false;
    }

    /**
     * it returns entire records of myhitory record(s).
     */
    public ArrayList<LottoRecord> GetMyHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<LottoRecord> result = new ArrayList<>();
        ArrayList<String> columnsList = new ArrayList<>();

        //Cursor c = db.rawQuery(rawQuery, null);
        try{
            Cursor c = db.rawQuery("select * from " + mSelectedGame.getTableName(), null);

            while(c.moveToNext()){
                    //construct record
                    ArrayList<Integer> winningNums = new ArrayList<>();
                    String date;
                    Integer bonusNum = new Integer(0);
                    Integer encoreNum = new Integer(0);

                    for(int i =0; i < mSelectedGame.getSetOfNums(); i++) {
                        winningNums.add(c.getInt(c.getColumnIndex(HISTORY_COL_NAME_PREFIX_NUMBER + i)));
                    }
                    date = c.getString(c.getColumnIndex(HISTORY_COL_NAME_DATE));

                    LottoRecord aRecord = new LottoRecord(winningNums, bonusNum, encoreNum, date);
                    result.add(aRecord);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            db.close();
            return result;
        }

    }



    private String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private void deleteAllRecords() {
        if(isExistTable(mSelectedGame.getTableName())){
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + mSelectedGame.getTableName());
//            long result = db.delete(mSelectedGame.getTableName(), null, null);
            db.close();
        }
    }

    private boolean isExistTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean result = false;
        try {
            Cursor c = db.query(tableName, //table
                    null, //all columns
                    null,//selection,
                    null,//selectionArgs,
                    null,//groupBy,
                    null,//having,
                    null);//orderBy
            result = c.moveToNext();
        }catch(SQLiteException e){
            e.printStackTrace();
        }finally{
            db.close();
            return result;
        }
    }
}
