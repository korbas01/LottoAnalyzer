package yimscompany.lottoanalyzer.DataAccessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;

/**
 * generating or updating game table
 * Created by shyim on 15-02-02.
 * past winning numbers will be stored in client db.
 */
public class SQLHelper extends SQLiteOpenHelper {
    private LottoGame mGame;
    private static final String LOTTO_COL_NAME_PREFIX_NUMBER = "num";
    private static final String LOTTO_COL_NAME_BONUS_NUMBER = "bonus_num";
    private static final String LOTTO_COL_NAME_ENCORE_NUMBER = "encore_num";
    private static final String LOTTO_COL_NAME_DATE = "date";

    public static final int DATA_BASE_VER = 1;

    private static final String LOTTOMASTER_DATABASE_FILE_NAME = "lotto.db";

//    private static SQLHelper sInstance;
//
//    public static synchronized SQLHelper getInstance(Context context, LottoGame g) {
//
//        // Use the application context, which will ensure that you
//        // don't accidentally leak an Activity's context.
//        // See this article for more information: http://bit.ly/6LRzfx
//        if (sInstance == null) {
//            sInstance = new SQLHelper(context.getApplicationContext(), g);
//        }
//        return sInstance;
//    }


    public SQLHelper(Context context, LottoGame aGame) {
        super(context, aGame.getTableName() + ".db", null, DATA_BASE_VER);
        this.mGame = aGame;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        if(this.mGame == null) {
            throw new NullPointerException("LottoGame has not been initialized");
        }
        //generating SQL Query based on LottoGame object info.
        String createSQLQuery = "CREATE TABLE " + this.mGame.getTableName() + " (";
        for(int i =0 ; i < this.mGame.getSetOfNums(); i ++) {
            createSQLQuery += LOTTO_COL_NAME_PREFIX_NUMBER + i + " INTEGER NOT NULL, ";
        }
        if(this.mGame.getHasBonusNum()) {
            createSQLQuery += LOTTO_COL_NAME_BONUS_NUMBER + " INTEGER NOT NULL, ";
        }
        if(this.mGame.getHasEncore()) {
            createSQLQuery += LOTTO_COL_NAME_ENCORE_NUMBER + " INTEGER NOT NULL, ";
        }
        createSQLQuery += LOTTO_COL_NAME_DATE + " TEXT PRIMARY KEY, UNIQUE (date) ON CONFLICT REPLACE);";
        database.execSQL(createSQLQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
        if(this.mGame == null) {
            throw new NullPointerException("LottoGame has not been initialized");
        }
        database.execSQL("DROP TABLE IF EXISTS " + this.mGame.getTableName());
        onCreate(database);
    }


    public void addGameRecord(LottoGame gameType, ArrayList<LottoRecord> records) {
        for(LottoRecord r : records){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues content = new ContentValues();
            for(int i =0; i < r.getWinningNums().size(); i++) {
                content.put(LOTTO_COL_NAME_PREFIX_NUMBER + i, r.getWinningNums().get(i).intValue());
            }
            if(gameType.getHasEncore())
                content.put(LOTTO_COL_NAME_ENCORE_NUMBER, r.getEncore().intValue());
            if(gameType.getHasBonusNum())
                content.put(LOTTO_COL_NAME_BONUS_NUMBER, r.getBonusNum().intValue());
            content.put(LOTTO_COL_NAME_DATE, r.getDate());
            db.insert(gameType.getTableName(),null,content);
            db.close();
            Log.d("sqlhelper","ADD Game Record!! @" + r.getWinningNums().size() + "," + r.getDate() );
        }
    }


    /**
     * it returns winning record(s)from the most recent games.
     * @param game: it needs to provide game type
     * @param numGames: number of the most recent games
     * @return
     */
        public ArrayList<LottoRecord> GetRecentGame(LottoGame game, int numGames) {
            SQLiteDatabase db = this.getReadableDatabase();
            ArrayList<LottoRecord> result = new ArrayList<>();
            String orderBySQLQuery = "";
            if(numGames <= 0)
            {
                orderBySQLQuery = "date DESC limit 1";

            }else{
                orderBySQLQuery = "date DESC limit " + numGames;
            }
            ArrayList<String> columnsList = new ArrayList<>();

            //Cursor c = db.rawQuery(rawQuery, null);
            try{
                Cursor c = db.query(game.getTableName(), //table
                        null, //all columns
                        null,//selection,
                        null,//selectionArgs,
                        null,//groupBy,
                        null,//having,
                        orderBySQLQuery);//orderBy

                if(c.moveToFirst()){
                    do{
                        //construct record
                        ArrayList<Integer> winningNums = new ArrayList<>();
                        String date;
                        Integer bonusNum = new Integer(0);
                        Integer encoreNum = new Integer(0);

                        String debugMsg= "";

                        for(int i =0; i < game.getSetOfNums(); i++) {
                            winningNums.add(c.getInt(c.getColumnIndex(LOTTO_COL_NAME_PREFIX_NUMBER + i)));
                        }
                        date = c.getString(c.getColumnIndex(LOTTO_COL_NAME_DATE));

                        if(game.getHasEncore())
                            bonusNum = new Integer(c.getInt(c.getColumnIndex(LOTTO_COL_NAME_ENCORE_NUMBER)));

                        if(game.getHasBonusNum())
                            encoreNum =  new Integer(c.getInt(c.getColumnIndex(LOTTO_COL_NAME_BONUS_NUMBER)));

                        LottoRecord aRecord = new LottoRecord(winningNums, bonusNum, encoreNum, date);
                        result.add(aRecord);
                    }while(c.moveToNext());
                }
            }finally{
                db.close();
                return result;
            }

        }


//    /* it will return "number" of the most recent winning numbers.
//     * number: a number of records of the winning numbers you want to get
//     * return: it returns a set of the last n-th winning records,
//     * */
//     //todo: this method needs to be deprecated
//     public ArrayList<Integer>  GetLastBonusNumber111111(int numGames)
//    {
//        SQLiteDatabase db = this.getReadableDatabase();
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        String orderBySQLQuery = "";
//        if(numGames <= 0)
//        {
//            orderBySQLQuery = "date DESC limit 1";
//
//        }else{
//            orderBySQLQuery = "date DESC limit " + numGames;
//        }
//
//        //Cursor c = db.rawQuery(rawQuery, null);
//        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
//                new String[] {LOTTO649_COL_NAME_BONUS_NUMBER}, //columns
//                null,//selection,
//                null,//selectionArgs,
//                null,//groupBy,
//                null,//having,
//                orderBySQLQuery);//orderBy
//
//        if(c.moveToFirst()){
//            do{
//
//                String debugMsg= "";
//                if( ! result.contains(Integer.valueOf(c.getInt(0))))
//                {
//                    result.add(Integer.valueOf(c.getInt(0)));
//                    debugMsg += c.getInt(0) + ",";
//                }
//                Log.d("SQLHelper","ArrayList<Integer> GetBonusNumbers returns " + debugMsg);
//            }while(c.moveToNext());
//        }
//        return result;
//    }
//
//    /* return the more recent of "numGames" of encore numbers
//    * */
////todo:it needs to be deprecated
//     public ArrayList<String> GetLastEncoreNumber111111(int numGames)
//    {
//        SQLiteDatabase db = this.getReadableDatabase();
//        ArrayList<String> result = new ArrayList<>();
//        String orderBySQLQuery = "";
//        if(numGames <= 0)
//        {
//            orderBySQLQuery = "date DESC limit 1";
//
//        }else{
//            orderBySQLQuery = "date DESC limit " + numGames;
//        }
//
//        //Cursor c = db.rawQuery(rawQuery, null);
//        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
//                new String[] {LOTTO649_COL_NAME_ENCORE_NUMBER}, //columns
//                null,//selection,
//                null,//selectionArgs,
//                null,//groupBy,
//                null,//having,
//                orderBySQLQuery);//orderBy
//
//        if(c.moveToFirst()){
//            do{
//
//                String debugMsg= "";
//                if( ! result.contains(String.valueOf(c.getString(0))))
//                {
//                    result.add(String.valueOf(c.getString(0)));
//                    debugMsg += c.getInt(0) + ",";
//                }
//                Log.d("SQLHelper","ArrayList<Integer> GetBonusNumbers returns " + debugMsg);
//            }while(c.moveToNext());
//        }
//        return result;
//    }
//
//    /* it will return "number" of the most recent winning numbers.
//     * @param numGames: a number of records of the winning numbers you want to get
//     * return: it returns a set of the last n-th winning records,
//     * */
////todo: it needs to be deprecated
//     public ArrayList<Integer> GetLastWinningNumbers111111(int numGames)
//    {
//        SQLiteDatabase db = this.getReadableDatabase();
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        String orderBySQLQuery;
//        if(numGames <= 0)
//        {
//            orderBySQLQuery = "date DESC limit 1";
//
//        }else{
//            orderBySQLQuery = "date DESC limit " + numGames;
//        }
//        //Cursor c = db.rawQuery(rawQuery, null);
//        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
//                new String[] {LOTTO_COL_DATE,LOTTO_COL_NAME_NUMBER1, LOTTO_COL_NAME_NUMBER2, LOTTO_COL_NAME_NUMBER3, LOTTO_COL_NAME_NUMBER4, LOTTO_COL_NAME_NUMBER5, LOTTO_COL_NAME_NUMBER6}, //columns
//                null,//selection,
//                null,//selectionArgs,
//                null,//groupBy,
//                null,//having,
//                orderBySQLQuery);//orderBy
//
//        if(c.moveToFirst()){
//            do{
//
//                String debugMsg= "";
//                for(int i =1 ; i <= 6 ; i++)
//                {
//                    if( ! result.contains(Integer.valueOf(c.getInt(i))))
//                    {
//                        result.add(Integer.valueOf(c.getInt(i)));
//                        debugMsg += c.getInt(i) + ",";
//                    }
//                }
//                debugMsg += " at " + c.getString(0);
//                Log.d("SQLHelper","ArrayList<Integer> GetLastWinningNumbers returns " + debugMsg);
//            }while(c.moveToNext());
//        }else{
//            Log.d("SQLHelper", "Wtf something is wrong in DB T T");
//
//        }
//
//        //sorting the result
//        Collections.sort(result);
//        return result;
//    }

    /**
     * check db and if the most recent record does not match with the latest date in DB, return true
     * @param game : lotto game info.
     * @param date : date of the last game from website.
     * @return true if we need to update our DB.
     */
    public boolean needUpdateDB(LottoGame game, String date){
        //todo: have not been implemented
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<LottoRecord> result = new ArrayList<>();
        String orderBySQLQuery = "date DESC limit 1";

        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(game.getTableName(), //table
                null, //all columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                orderBySQLQuery);//orderBy

        if(c.moveToFirst()){
            String d = c.getString(c.getColumnIndex(LOTTO_COL_NAME_DATE));
            if(d.equals(date)){
                return true;
            }
        }
        return false;
    }
}
