package yimscompany.lottoanalyzer.BusinessLogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import yimscompany.lottoanalyzer.BusinessLogic.OLG.ParsingOLGPage;
import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;
import yimscompany.lottoanalyzer.DataAccessLayer.SQLHelper;
import yimscompany.lottoanalyzer.DataAccessLayer.SQLHistoryHelper;
import yimscompany.lottoanalyzer.R;

/* this is a controller which interacts  activities,
 * it will access to db and analyzing db return result.
 * @author shyim
 *with
 */
public class LottoAnalyzer {

    public SQLHelper mLottoSQLHelper;
    public SQLHistoryHelper mLottoSQLHistoryHelper;
    private ArrayList<Integer> mOmitList;
    private ParsingOLGPage _pPage;
    private Context mContext;
    private int mNumGames = 1; //look up the number of past games
    private int mNumFrequencyFactor = 5;
    private int mNumGamesFindSameNumbers = 2;
    private int mNumOmitNumbers = 18;
    private LottoGame mSelectedGame;
    /* Constructor
     */
    public LottoAnalyzer(Context c, LottoGame selectedGame)
    {
        this.mLottoSQLHelper = new SQLHelper(c,selectedGame);
        this.mLottoSQLHistoryHelper = new SQLHistoryHelper(c, selectedGame);

        this.mContext =c;
        this.mOmitList = new ArrayList<>();
        this.mSelectedGame = selectedGame;
    }

    /**
     * analyze next expected winning number pool.
     * @return
     * return a set of numbers which could be the next winning numbers
     */
    public ArrayList<Integer> Run()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isExludeNums = pref.getBoolean(mContext.getResources().getString(R.string.pref_exclude_numbers), true);
        boolean isPatternAnalyze = pref.getBoolean(mContext.getResources().getString(R.string.pref_pattern_analyze), true);

        ArrayList<Integer> result = new ArrayList<>();

        while(mOmitList.size() < mNumOmitNumbers && isExludeNums)
        {
            mOmitList.clear();
            ArrayList<Integer> sameNums = findSameNumbers(mSelectedGame, mNumGamesFindSameNumbers);
            if(sameNums.size()>0)
            {
                mOmitList.addAll(sameNums);
            }
            mOmitList.addAll(getLastBonusNum(mSelectedGame, mNumGames));
            mOmitList.addAll(findHighestFrequencyNumbers(mSelectedGame, mNumFrequencyFactor));
            mOmitList.addAll(getEncoreNumbers(mSelectedGame, mNumGames));
            mNumGamesFindSameNumbers *= 2;
            mNumGames *= 2;
            mNumFrequencyFactor *= 2;
        }

        //filter some patterns
        do {
            result.clear();
            for (int i = mSelectedGame.getMinRange(); i <= mSelectedGame.getMaxRange(); i++) {
                Integer aNum = new Integer(i);
                if (!mOmitList.contains(aNum)) {
                    result.add(aNum);
                }
            }
            if(! isPatternAnalyze) {
                return result;
            }
        }while ( !filterWinningNumPattern(result) );
        return result;
    }

    /* it will randomly pick "numpicks" numbers from "numbers" pool
       @param numbers: it's a list of expected winning numbers
       @param numPicks: how many numbers does a user want to randomly pick from the list
    * */
    public ArrayList<Integer> PickNumbers(ArrayList<Integer> numbers, int numPicks)
    {
        ArrayList<Integer> result = new ArrayList<>();
        //TODO: not yet implemented
        return result;
    }



    /* find the same numbers from "numGames" of the most recent winning numberes
    * */
    private ArrayList<Integer> findSameNumbers(LottoGame game, int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<LottoRecord> winningRecords = mLottoSQLHelper.GetRecentGame(game, numGames);
        ArrayList<Integer> numbers = new ArrayList<>();

        for(LottoRecord aRecord: winningRecords) {
            numbers.addAll( aRecord.getWinningNums());
        }

        Integer candidate = new Integer(0);
        for(Integer num : numbers)
        {
            if(candidate.intValue() ==0)
            {
                candidate = num;
            }else if(candidate.equals(num))
            {
                candidate = 0;
                result.add(candidate);
            }else{
                candidate = num;
            }
        }
        //remove duplicate from result
        HashSet<Integer> hs = new HashSet<>();
        hs.addAll(result);
        result.clear();
        result.addAll(hs);
        return result;
    }

    /* find the highest frequency number from "numGames" of the most recent winning numberes
    * enhanced logic, if there exist more than one number with the same frequency, randomly pick one
    * */
    private ArrayList<Integer> findHighestFrequencyNumbers(LottoGame game, int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<LottoRecord> winningRecords = mLottoSQLHelper.GetRecentGame(game, numGames);
        ArrayList<Integer> winningNums = new ArrayList<>();
        for(LottoRecord aRecord: winningRecords) {
            winningNums.addAll( aRecord.getWinningNums());
        }

        HashMap<Integer,Integer> map = new HashMap<>();
        ValueComparator vc = new ValueComparator(map);
        TreeMap<Integer,Integer> sortedMap = new TreeMap<>(vc);

        for(Integer num : winningNums){
            Integer val = map.get(num);
            if(val != null){
                map.put(num, new Integer(val + 1));
            }else{
                map.put(num,1);
            }
        }

        sortedMap.putAll(map);
        Log.d("lottoMaster", "called findHighestFrequencyNumbers()");
        Log.d("lottoMaster", "result: " + sortedMap);
        //result.add(sortedMap.firstEntry().getValue());

        Integer freq = new Integer(sortedMap.lastEntry().getValue());
        Iterator iter = map.keySet().iterator();
        while(iter.hasNext())
        {
            Integer k = (Integer) iter.next();
            Integer v = map.get(k);
            if( v.equals(freq))
            {
                result.add(k);
            }
        }

        if(freq == 1)   //randomly pick some numbers because the value of Rank is too low.
        {
            ArrayList<Integer> newResult = new ArrayList<>();
            RandomNumberGenerator rng = new RandomNumberGenerator(result.size());
            ArrayList<Integer> listIndex = rng.generateNumbers(numGames);
            for(Integer index : listIndex)
            {
                newResult.add(result.get(index));
            }
            return newResult;
        }

        return result;
    }


    /* get the bonus number(s) from the last winning games,
       returns empty arraylist if the game does not have a bonus number.
    * */
    private ArrayList<Integer> getLastBonusNum(LottoGame game, int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if(! game.getHasBonusNum()) {
            return result;
        }

        ArrayList<LottoRecord> winningRecords = mLottoSQLHelper.GetRecentGame(game, numGames);
        for(LottoRecord aRecord: winningRecords) {
            result.add(aRecord.getBonusNum());
        }
        return result;
    }


    private ArrayList<Integer> getEncoreNumbers(LottoGame game, int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if(! game.getHasBonusNum()) {
            return result;
        }

        ArrayList<LottoRecord> winningRecords = mLottoSQLHelper.GetRecentGame(game, numGames);
        ArrayList<String> vEncoreNums = new ArrayList<>();

        for(LottoRecord aRecord: winningRecords) {
            vEncoreNums.add(Integer.toString(aRecord.getEncore()));
        }


        for(String encoreNum : vEncoreNums)
        {
            for(int i = 0 ; i < encoreNum.length() - 1; i++)
            {
                Integer aNum = new Integer (encoreNum.substring(i, i+2));
                if(aNum <= 49)
                {
                    result.add(aNum);
                }
            }
        }
        return result;
    }

    /* returns true if it filters the following pattern successfully
    */
    private boolean filterWinningNumPattern(ArrayList<Integer> expectedWinningNums)
    {
        ArrayList<LottoRecord> pastWinningRecords = mLottoSQLHelper.GetRecentGame(mSelectedGame, mNumGames);
        return filterMod(7, pastWinningRecords, expectedWinningNums)
                && filterHighLow( pastWinningRecords, expectedWinningNums)
                && filterGroupBy10( pastWinningRecords, expectedWinningNums);
    }

    private boolean filterMod(int mod, ArrayList<LottoRecord> pastWinningRecords, ArrayList<Integer> expectedWinningNums)
    {
        ArrayList<Integer> a1 = new ArrayList<>();

        for(Integer i : expectedWinningNums)
        {
            a1.add(new Integer(i % mod));
        }
        Collections.sort(a1);

        for(LottoRecord aRecord: pastWinningRecords)
        {
            ArrayList<Integer> a2 = new ArrayList<>();
            for(Integer i : aRecord.getWinningNums())
            {
                a2.add(new Integer(i % mod));
            }
            Collections.sort(a2);
            if(a1.equals(a2))
            {
                return false;
            }
        }
        Log.d("LottoAnalyzer.java", "filterMod returns true");
        return true;
    }

    private boolean filterHighLow(ArrayList<LottoRecord> pastWinningRecords, ArrayList<Integer> expectedWinningNums)
    {
        ArrayList<Integer> a1 = new ArrayList<>();
        int d = 10;
        for(Integer i : expectedWinningNums)
        {
            int remainder = i % 10;
            if( remainder >= 0 && remainder < 5)
            {
                a1.add(new Integer(0));
            }else {
                a1.add(new Integer(1));
            }
        }
        Collections.sort(a1);

        for(LottoRecord aRecord: pastWinningRecords)
        {
            ArrayList<Integer> a2 = new ArrayList<>();
            for(Integer i : aRecord.getWinningNums())
            {
                int remainder = i % 10;
                if( remainder >= 0 && remainder < 5)
                {
                    a2.add(new Integer(0));
                }else {
                    a2.add(new Integer(1));
                }
            }
            Collections.sort(a2);
            if(a1.equals(a2))
            {
                return false;
            }
        }
        Log.d("LottoAnalyzer.java", "filterHighLow returns true");
        return true;

    }

    private boolean filterGroupBy10(ArrayList<LottoRecord> pastWinningRecords, ArrayList<Integer> expectedWinningNums)
    {
        ArrayList<Integer> a1 = new ArrayList<>();

        for(Integer i : expectedWinningNums)
        {
            a1.add(new Integer((i-1) / 10 ));
        }
        Collections.sort(a1);

        for(LottoRecord aRecord: pastWinningRecords)
        {
            ArrayList<Integer> a2 = new ArrayList<>();
            for(Integer i : aRecord.getWinningNums())
            {
                a2.add(new Integer( (i-1) / 10));
            }
            Collections.sort(a2);
            if(a1.equals(a2))
            {
                return false;
            }
        }
        Log.d("LottoAnalyzer.java", "filterGroupBy10 returns true");
        return true;
    }
//
//    /* This function must be triggered every time it's running
//     * */
//    public void doUpdateDB()
//    {
//        Calendar c =  Calendar.getInstance();
//        int currMonth = c.get(Calendar.MONTH);
//
//    }
//
//    public void getNextWinningNumbers(int maxRange)
//    {
//        RandomNumberGenerator rand = new RandomNumberGenerator(maxRange);
//        rand.generateNumbers(6);
//    }
//
//    public void getPossibleWinningNumbers()
//    {
//
//        mLottoSQLHelper.getPossibleWinningNumbers();
//    }
//
//
//    public void parsingLastWinningNumbers()
//    {
//        Log.d("lottoMaster", "called getLastWinningNumbers()");
//
//        //downloadTask = new DownloadPage(downloadPageListener);
//        //downloadTask.execute(new String[]{LAST_WINNING_NUMBERS_URL, "LastWinningNumbers"});
//
//        //call the last winning numbers,
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//
//    /*it needs to be constructed a query in order to avoid using their form,
//     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
//     * gameID: a game id, lotto649 is 1,
//     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
//     * 					  last two numbers, year. OLG has the last one year record
//     * day: if it's 0, it shows all past winning numbers in a month respectively
//     * x,y: some random numbers I couldn't figure out what those numbers are for
//     * @param: gameID: number represents game e.g.) 1 = lotto 649
//    */
//    public void parsingPastWinningNumbers(LottoGame game)
//    {
////        //get the current Year and Month
////        Calendar c =  Calendar.getInstance();
////        int currMonth = c.get(Calendar.MONTH);
////        int currYear = c.get(Calendar.YEAR) - 1;
////        Log.d("lottoMaster","clicked getPastWinningNumberes::month=" + currMonth + ",year=" + currYear);
////
////        //we will get the last one year records
////        int count = 0;
////        while(count < 12) {
////            currMonth = currMonth % 12;
////            if(count > 0 && currMonth == 0)
////            {
////                currYear++;
////            }
////            //construct query
////            String selectedMonthYear =  String.format("%02d", currMonth) + (new Integer(currYear).toString());
////
////            String queryString = "command=submit&gameID=" +
////                    gameType.getValue() +"&selectedMonthYear=" + selectedMonthYear +"&day=0&x=31&y=12";
////            if(count >= 0)	 {
////                //calling past winning numbers
////                //Intent dnPageIntent = new Intent(_ctx, DownloadPageIntent.class);
////                //dnPageIntent.putExtra(DownloadPageIntent.EXTRA_PARAM_QUERY_STRING,queryString );
////                //startService(dnPageIntent);
////                throw new UnsupportedOperationException("Not yet implemented");
////            }
////            count ++;
////            currMonth ++;
////        }
//
//    }
//
//    public void getFrequentWinningNumbers()
//    {
//        Log.d("lottoMaster","called getFrequentWinningNumbers");
//    }
//
//    public void getFrequentMaxMillionNumbers()
//    {
//
//    public ArrayList<Integer> displayResult()
//    {
//        //get possible winning numbers
//        return mLottoSQLHelper.getPossibleWinningNumbers();
//
//    }
//
//    //LottoMasterCustomizedCallback implementation
//    public void lottomasterCallback(List<String[]> result)
//    {
//        Log.d("lottoMaster","hello callback");
//        ArrayList<Integer> winningNums = new ArrayList<Integer>();
//        for(int i =0 ; i < result.size(); i++)
//        {
//            String[] aRecord = result.get(i);
//            for(int j =1 ; j < aRecord.length; j++)
//            {
//                winningNums.add(new Integer(aRecord[j]));
//                Log.d("lottoMaster", "result[" + i + "] = " + aRecord[j]);
//            }
//            mLottoSQLHelper.addWinningNumbers(winningNums, aRecord[0]);
//            winningNums.clear();
//        }
//        //we must stop the task after it's finished
//        //boolean aa = downloadTask.cancel(true);
//        //Log.d("lottoMaster","hello @@@@@@@@@=" + aa);
//    }
//
//
//    /* we're finding 'numCons' of consecutive numbers in the last 'number' of games
//     * param: @numGames: set the number of the most recent games we're lookging for
//     * 		  @numCons: set the number of consecutive winning numbers
//     * return: if there exists, return the next consecutive number,
//     *         e.g.) if there are 8,9,10 in the past three games then return 11
//     *         return empty arraylist if it does not exist
//     * */
//    private ArrayList<Integer> getConsecutiveNumbers(int numGames, int numCons)
//    {
//        ArrayList<Integer> result = new ArrayList<Integer>();
//
//        //**NOTE: assuming that pastWinningNumbers is sorted!!!!!!!
//        ArrayList<Integer> pastWinningNumbers = this.GetLastWinningNumbers(numGames);
//        if(!pastWinningNumbers.isEmpty())
//        {
//            int prevNum= 0;
//            int countConsecutiveNumbers = 0;
//            for(int i =0; i < pastWinningNumbers.size(); i++)
//            {
//                if(prevNum ==0) //it's not been assigned, we must set it
//                {
//                    prevNum = pastWinningNumbers.get(i);
//                }else{
//                    int diff = pastWinningNumbers.get(i) - prevNum;
//                    if(diff == 1)
//                    {
//                        countConsecutiveNumbers ++;
//                        prevNum = pastWinningNumbers.get(i);
//                        if(countConsecutiveNumbers == numCons)
//                        {
//                            //reset all local vars.
//                            prevNum = 0;
//                            countConsecutiveNumbers = 0;
//                            Log.d("SQLHelper","ArrayList<Integer> getConsecutiveNumbers returns " + (pastWinningNumbers.get(i) + 1));
//
//                            result.add(Integer.valueOf( (pastWinningNumbers.get(i) + 1) ));
//                        }
//                    }else{
//                        countConsecutiveNumbers = 0;
//                        prevNum = 0;
//                    }
//
//                }
//            }
//        }
//        return result;
//    }
//
//    //main function to get the possible winning numbers
//    public ArrayList<Integer> getPossibleWinningNumbers()
//    {
//        ArrayList<Integer> possibleWinningNums = new ArrayList<Integer>();
//        for(int i = 1 ; i <= 49 ; i ++)
//        {
//            possibleWinningNums.add(Integer.valueOf(i));
//        }
//
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        result = this.GetLastWinningNumbers(5);
//        result.addAll(this.getConsecutiveNumbers(5, 2));
//
//
//
//        //omit numbers what we've been analyzing...
//        for(int i=0 ; i < result.size() ; i++)
//        {
//            int index = possibleWinningNums.indexOf(result.get(i));
//            if(index >= 0)
//                possibleWinningNums.remove(index);
//        }
//
//
//        return possibleWinningNums;
//
//    }
//        Log.d("lottoMaster","called getFrequentMaxMillionNumbers");
//    }

}



