package yimscompany.lottoanalyzer.BusinessLogic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by shyim on 15-02-02.
 * past winning numbers will be stored in client db.
 */
public class SQLHelper extends SQLiteOpenHelper {
    private static final String LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME ="lotto649";
    private static final String LOTTO_COL_NAME_NUMBER1 = "num1";
    private static final String LOTTO_COL_NAME_NUMBER2 = "num2";
    private static final String LOTTO_COL_NAME_NUMBER3 = "num3";
    private static final String LOTTO_COL_NAME_NUMBER4 = "num4";
    private static final String LOTTO_COL_NAME_NUMBER5 = "num5";
    private static final String LOTTO_COL_NAME_NUMBER6 = "num6";
    private static final String LOTTO649_COL_NAME_BONUS_NUMBER = "bonus_num";
    private static final String LOTTO649_COL_NAME_ENCORE_NUMBER = "encore_num";

    private static final String LOTTO_COL_DATE = "date";

    public static final int DATA_BASE_VER = 1;

    private static final String LOTTOMASTER_DATABASE_FILE_NAME = "lotto.db";
    private static final String CREATE_LOTTO649_TABLE ="CREATE TABLE " +
            LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME + " (" + LOTTO_COL_NAME_NUMBER1 +" integer not null,"
            + LOTTO_COL_NAME_NUMBER2 + " integer not null, "
            + LOTTO_COL_NAME_NUMBER3 + " integer not null, "
            + LOTTO_COL_NAME_NUMBER4 + " integer not null, "
            + LOTTO_COL_NAME_NUMBER5 + " integer not null, "
            + LOTTO_COL_NAME_NUMBER6 + " integer not null, "
            + LOTTO649_COL_NAME_BONUS_NUMBER + " integer not null, "
            + LOTTO649_COL_NAME_ENCORE_NUMBER + " integer not null, "
            + LOTTO_COL_DATE +" text PRIMARY KEY,"
            + "UNIQUE (" +  LOTTO_COL_DATE +  ") ON CONFLICT REPLACE);";



    public SQLHelper(Context context) {
        super(context, LOTTOMASTER_DATABASE_FILE_NAME, null, DATA_BASE_VER);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // TODO Auto-generated method stub
        Log.d("SQLHelper", "lottoMaster oncreateL!!!!!:");
        database.execSQL(CREATE_LOTTO649_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
        // TODO Auto-generated method stub
        Log.d("SQLHelper", "lottoMaster onUpgrade!!!!!:");
        database.execSQL("DROP TABLE IF EXISTS " + LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME);
        onCreate(database);
    }

    public void addWinningNumbers(ArrayList<Integer> winningNum, String date)
    {
        if(winningNum.size() == 8 && date != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues content = new ContentValues();
            content.put(LOTTO_COL_NAME_NUMBER1, winningNum.get(0).intValue());
            content.put(LOTTO_COL_NAME_NUMBER2, winningNum.get(1).intValue());
            content.put(LOTTO_COL_NAME_NUMBER3, winningNum.get(2).intValue());
            content.put(LOTTO_COL_NAME_NUMBER4, winningNum.get(3).intValue());
            content.put(LOTTO_COL_NAME_NUMBER5, winningNum.get(4).intValue());
            content.put(LOTTO_COL_NAME_NUMBER6, winningNum.get(5).intValue());
            content.put(LOTTO649_COL_NAME_BONUS_NUMBER, winningNum.get(6).intValue());
            content.put(LOTTO649_COL_NAME_ENCORE_NUMBER, winningNum.get(7).intValue());
            content.put(LOTTO_COL_DATE, date);
            //UPDATE <table> SET col1 = val1 , ... WHERE <condition>
            db.insert(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, null, content);
            db.close();
            Log.d("sqlhelper","ADD WINNING NUMBERES!! @" + winningNum.size() + "," + date);

        }
    }

    /* it will return "number" of the most recent winning numbers.
     * number: a number of records of the winning numbers you want to get
     * return: it returns a set of the last n-th winning records,
     * */
    public ArrayList<Integer>  GetLastBonusNumber(int numGames)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> result = new ArrayList<Integer>();
        String orderBySQLQuery = "";
        if(numGames <= 0)
        {
            orderBySQLQuery = "date DESC limit 1";

        }else{
            orderBySQLQuery = "date DESC limit " + numGames;
        }

        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
                new String[] {LOTTO649_COL_NAME_BONUS_NUMBER}, //columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                orderBySQLQuery);//orderBy

        if(c.moveToFirst()){
            do{

                String debugMsg= "";
                if( ! result.contains(Integer.valueOf(c.getInt(0))))
                {
                    result.add(Integer.valueOf(c.getInt(0)));
                    debugMsg += c.getInt(0) + ",";
                }
                Log.d("SQLHelper","ArrayList<Integer> GetBonusNumbers returns " + debugMsg);
            }while(c.moveToNext());
        }
        return result;
    }

    /* return the more recent of "numGames" of encore numbers
    * */
    public ArrayList<String> GetLastEncoreNumber(int numGames)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> result = new ArrayList<>();
        String orderBySQLQuery = "";
        if(numGames <= 0)
        {
            orderBySQLQuery = "date DESC limit 1";

        }else{
            orderBySQLQuery = "date DESC limit " + numGames;
        }

        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
                new String[] {LOTTO649_COL_NAME_ENCORE_NUMBER}, //columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                orderBySQLQuery);//orderBy

        if(c.moveToFirst()){
            do{

                String debugMsg= "";
                if( ! result.contains(String.valueOf(c.getString(0))))
                {
                    result.add(String.valueOf(c.getString(0)));
                    debugMsg += c.getInt(0) + ",";
                }
                Log.d("SQLHelper","ArrayList<Integer> GetBonusNumbers returns " + debugMsg);
            }while(c.moveToNext());
        }
        return result;
    }

    /* it will return "number" of the most recent winning numbers.
     * @param numGames: a number of records of the winning numbers you want to get
     * return: it returns a set of the last n-th winning records,
     * */
    public ArrayList<Integer> GetLastWinningNumbers(int numGames)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> result = new ArrayList<Integer>();
        String orderBySQLQuery;
        if(numGames <= 0)
        {
            orderBySQLQuery = "date DESC limit 1";

        }else{
            orderBySQLQuery = "date DESC limit " + numGames;
        }
        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
                new String[] {LOTTO_COL_DATE,LOTTO_COL_NAME_NUMBER1, LOTTO_COL_NAME_NUMBER2, LOTTO_COL_NAME_NUMBER3, LOTTO_COL_NAME_NUMBER4, LOTTO_COL_NAME_NUMBER5, LOTTO_COL_NAME_NUMBER6}, //columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                orderBySQLQuery);//orderBy

        if(c.moveToFirst()){
            do{

                String debugMsg= "";
                for(int i =1 ; i <= 6 ; i++)
                {
                    if( ! result.contains(Integer.valueOf(c.getInt(i))))
                    {
                        result.add(Integer.valueOf(c.getInt(i)));
                        debugMsg += c.getInt(i) + ",";
                    }
                }
                debugMsg += " at " + c.getString(0);
                Log.d("SQLHelper","ArrayList<Integer> GetLastWinningNumbers returns " + debugMsg);
            }while(c.moveToNext());
        }else{
            Log.d("SQLHelper", "Wtf something is wrong in DB T T");

        }

        //sorting the result
        Collections.sort(result);
        return result;
    }

    /* we're finding 'numCons' of consecutive numbers in the last 'number' of games
     * param: @numGames: set the number of the most recent games we're lookging for
     * 		  @numCons: set the number of consecutive winning numbers
     * return: if there exists, return the next consecutive number,
     *         e.g.) if there are 8,9,10 in the past three games then return 11
     *         return empty arraylist if it does not exist
     * */
    private ArrayList<Integer> getConsecutiveNumbers(int numGames, int numCons)
    {
        ArrayList<Integer> result = new ArrayList<Integer>();

        //**NOTE: assuming that pastWinningNumbers is sorted!!!!!!!
        ArrayList<Integer> pastWinningNumbers = this.GetLastWinningNumbers(numGames);
        if(!pastWinningNumbers.isEmpty())
        {
            int prevNum= 0;
            int countConsecutiveNumbers = 0;
            for(int i =0; i < pastWinningNumbers.size(); i++)
            {
                if(prevNum ==0) //it's not been assigned, we must set it
                {
                    prevNum = pastWinningNumbers.get(i);
                }else{
                    int diff = pastWinningNumbers.get(i) - prevNum;
                    if(diff == 1)
                    {
                        countConsecutiveNumbers ++;
                        prevNum = pastWinningNumbers.get(i);
                        if(countConsecutiveNumbers == numCons)
                        {
                            //reset all local vars.
                            prevNum = 0;
                            countConsecutiveNumbers = 0;
                            Log.d("SQLHelper","ArrayList<Integer> getConsecutiveNumbers returns " + (pastWinningNumbers.get(i) + 1));

                            result.add(Integer.valueOf( (pastWinningNumbers.get(i) + 1) ));
                        }
                    }else{
                        countConsecutiveNumbers = 0;
                        prevNum = 0;
                    }

                }
            }
        }
        return result;
    }

    //main function to get the possible winning numbers
    public ArrayList<Integer> getPossibleWinningNumbers()
    {
        ArrayList<Integer> possibleWinningNums = new ArrayList<Integer>();
        for(int i = 1 ; i <= 49 ; i ++)
        {
            possibleWinningNums.add(Integer.valueOf(i));
        }

        ArrayList<Integer> result = new ArrayList<Integer>();
        result = this.GetLastWinningNumbers(5);
        result.addAll(this.getConsecutiveNumbers(5, 2));



        //omit numbers what we've been analyzing...
        for(int i=0 ; i < result.size() ; i++)
        {
            int index = possibleWinningNums.indexOf(result.get(i));
            if(index >= 0)
                possibleWinningNums.remove(index);
        }


        return possibleWinningNums;

    }


    public String getLatestDate()
    {
        String result = "";
        //get rid of the last winning number
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
                new String[] {LOTTO_COL_DATE}, //columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                "date("+ LOTTO_COL_DATE +") DESC");//orderBy

        Log.d("sqlhelper", "count= " + c.getCount());
        if(c.moveToFirst()){
            do{
                ArrayList<Integer> aWinningNum = new ArrayList<Integer>();
                aWinningNum.add(Integer.valueOf(c.getInt(1)));
                aWinningNum.add( Integer.valueOf(c.getInt(2)));
                aWinningNum.add( Integer.valueOf(c.getInt(3)));
                aWinningNum.add( Integer.valueOf(c.getInt(4)));
                aWinningNum.add( Integer.valueOf(c.getInt(5)));
                aWinningNum.add(Integer.valueOf(c.getInt(6)));

                Log.d("sqlhelper","ArrayList<Integer> aWinningNum=" + c.getString(0));
            }while(c.moveToNext());
        }else{
            Log.d("sqlhelper", "Wtf something is wrong in DB T T");

        }


        Log.d("sqlhelper", "----END testSelectAll-----");


        return result;
    }

    /* Test Methods */
    public void testLatestDate()
    {
        Log.d("sqlhelper", "----START testLatestDate-----");
        getLatestDate();

        Log.d("sqlhelper", "----END testLatestDate-----");
    }

    public void testSelectAll()
    {
        //get rid of the last winning number
        SQLiteDatabase db = this.getReadableDatabase();

        String rawQuery = "select * from " + LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME;

        //Cursor c = db.rawQuery(rawQuery, null);
        Cursor c = db.query(LOTTO649_PAST_WINNING_NUMBERS_TABLE_NAME, //table
                new String[] {LOTTO_COL_DATE,LOTTO_COL_NAME_NUMBER1, LOTTO_COL_NAME_NUMBER2, LOTTO_COL_NAME_NUMBER3, LOTTO_COL_NAME_NUMBER4, LOTTO_COL_NAME_NUMBER5, LOTTO_COL_NAME_NUMBER6}, //columns
                null,//selection,
                null,//selectionArgs,
                null,//groupBy,
                null,//having,
                null);//orderBy

        Log.d("sqlhelper", "----START testSelectAll-----");
        Log.d("sqlhelper", "count= " + c.getCount());
        if(c.moveToFirst()){
            do{
                ArrayList<Integer> aWinningNum = new ArrayList<Integer>();
                aWinningNum.add(Integer.valueOf(c.getInt(1)));
                aWinningNum.add(Integer.valueOf(c.getInt(2)));
                aWinningNum.add(Integer.valueOf(c.getInt(3)));
                aWinningNum.add(Integer.valueOf(c.getInt(4)));
                aWinningNum.add(Integer.valueOf(c.getInt(5)));
                aWinningNum.add(Integer.valueOf(c.getInt(6)));

                Log.d("sqlhelper","date=" + c.getString(0)  + "," + aWinningNum.toString());
            }while(c.moveToNext());
        }else{
            Log.d("sqlhelper", "Wtf something is wrong in DB T T");

        }


        Log.d("sqlhelper", "----END testSelectAll-----");
    }

}
