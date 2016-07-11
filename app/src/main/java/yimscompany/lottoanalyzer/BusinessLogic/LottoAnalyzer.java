package yimscompany.lottoanalyzer.BusinessLogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
//        Log.d("lottoMaster", "called findHighestFrequencyNumbers()");
//        Log.d("lottoMaster", "result: " + sortedMap);

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
//        Log.d("LottoAnalyzer.java", "filterMod returns true");
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
        return true;
    }

}



