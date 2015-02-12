package yimscompany.lottoanalyzer.BusinessLogic;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/* this is a controller which interacts with activities
 * @author shyim
 *
 */
public class LottoAnalyzer {
    public enum GameType {
        Lotto649(1);

        private final int id;
        GameType(int id) { this.id = id; }
        public int getValue() { return id; }
    };

    public SQLHelper _lottoSQLHelper;
    private ArrayList<Integer> _omitList;
    private ParsingPage _pPage;
    private Context _ctx;
    private int numGames = 1;
    private int numFrequencyFactor = 5;
    private int numGamesFindSameNumbers = 2;
    private int numOmitNumbers = 18;
    /* Constructor
     */
    public LottoAnalyzer(Context c)
    {
        this._lottoSQLHelper = new SQLHelper(c);
        this._pPage = new ParsingPage(c);
        this._ctx =c;
        this._omitList = new ArrayList<>();
    }

    // get all possible winning numbers
    public ArrayList<Integer> Run()
    {

        ArrayList<Integer> result = new ArrayList<>();
        while(_omitList.size() < numOmitNumbers)
        {
            _omitList.clear();
            ArrayList<Integer> sameNums = findSameNumbers(numGamesFindSameNumbers);
            if(sameNums.size()>0)
            {
                _omitList.addAll(sameNums);
            }
            _omitList.addAll(getLastBonusNum(numGames));
            _omitList.addAll(findHighestFrequencyNumbers(numFrequencyFactor));
            _omitList.addAll(getEncoreNumbers(numGames));
            numGamesFindSameNumbers *= 2;
            numGames *= 2;
            numFrequencyFactor *= 2;
        }

        for(int i = 1 ; i <= 49 ; i++)
        {
            Integer aNum = new Integer(i);
            if(! _omitList.contains(aNum))
            {
                result.add(aNum);
            }
        }
        return result;
    }



    /* it will randomly pick "numpicks" numbers from "numbers" pool
       @param numbers: it's a list of expected winning number
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
    private ArrayList<Integer> findSameNumbers(int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> numbers = _lottoSQLHelper.GetLastWinningNumbers(numGames);
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
    private ArrayList<Integer> findHighestFrequencyNumbers(int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> winningNums = _lottoSQLHelper.GetLastWinningNumbers(numGames);
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
        result.add(sortedMap.firstEntry().getValue());

        Integer freq = new Integer(sortedMap.firstEntry().getValue());
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

        if(freq == 1)   //randomly pick some numbers because the numbers have the same rank
        {
            ArrayList<Integer> newResult = new ArrayList<>();
            RandomNumberGenerator rng = new RandomNumberGenerator(map.size());
            ArrayList<Integer> listIndex = rng.generateNumbers(numGames);
            for(Integer index : listIndex)
            {
                newResult.add(result.get(index));
            }
            return newResult;
        }

        return result;
    }


    /* get the bonus number from the last winning numbers
    * */
    private ArrayList<Integer> getLastBonusNum(int numGames)
    {
        return _lottoSQLHelper.GetLastBonusNumber(numGames);
    }


    private ArrayList<Integer> getEncoreNumbers(int numGames)
    {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<String> vEncoreNums = _lottoSQLHelper.GetLastEncoreNumber(numGames);
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

    /* This function must be triggered every time it's running
     * */
    public void doUpdateDB()
    {
        Calendar c =  Calendar.getInstance();
        int currMonth = c.get(Calendar.MONTH);

    }

    public void getNextWinningNumbers(int maxRange)
    {
        RandomNumberGenerator rand = new RandomNumberGenerator(maxRange);
        rand.generateNumbers(6);
    }

    public void getPossibleWinningNumbers()
    {
        _lottoSQLHelper.getPossibleWinningNumbers();
    }


    public void parsingLastWinningNumbers()
    {
        Log.d("lottoMaster", "called getLastWinningNumbers()");

        //downloadTask = new DownloadPage(downloadPageListener);
        //downloadTask.execute(new String[]{LAST_WINNING_NUMBERS_URL, "LastWinningNumbers"});

        //call the last winning numbers,
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /*it needs to be constructed a query in order to avoid using their form,
     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
     * gameID: a game id, lotto649 is 1,
     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
     * 					  last two numbers, year. OLG has the last one year record
     * day: if it's 0, it shows all past winning numbers in a month respectively
     * x,y: some random numbers I couldn't figure out what those numbers are for
     * @param: gameID: number represents game e.g.) 1 = lotto 649
    */
    public void parsingPastWinningNumbers(GameType gameType)
    {
        //get the current Year and Month
        Calendar c =  Calendar.getInstance();
        int currMonth = c.get(Calendar.MONTH);
        int currYear = c.get(Calendar.YEAR) - 1;
        Log.d("lottoMaster","clicked getPastWinningNumberes::month=" + currMonth + ",year=" + currYear);

        //we will get the last one year records
        int count = 0;
        while(count < 12) {
            currMonth = currMonth % 12;
            if(count > 0 && currMonth == 0)
            {
                currYear++;
            }
            //construct query
            String selectedMonthYear =  String.format("%02d", currMonth) + (new Integer(currYear).toString());
            String queryString = "command=submit&gameID=" +
                    gameType.getValue() +"&selectedMonthYear=" + selectedMonthYear +"&day=0&x=31&y=12";
            if(count >= 0)	 {
                //calling past winning numbers
                //Intent dnPageIntent = new Intent(_ctx, DownloadPageIntent.class);
                //dnPageIntent.putExtra(DownloadPageIntent.EXTRA_PARAM_QUERY_STRING,queryString );
                //startService(dnPageIntent);
                throw new UnsupportedOperationException("Not yet implemented");
            }
            count ++;
            currMonth ++;
        }

    }

    public void getFrequentWinningNumbers()
    {
        Log.d("lottoMaster","called getFrequentWinningNumbers");
    }

    public void getFrequentMaxMillionNumbers()
    {
        Log.d("lottoMaster","called getFrequentMaxMillionNumbers");
    }

    public ArrayList<Integer> displayResult()
    {
        //get possible winning numbers
        return _lottoSQLHelper.getPossibleWinningNumbers();
    }

    //LottoMasterCustomizedCallback implementation
    public void lottomasterCallback(List<String[]> result)
    {
        Log.d("lottoMaster","hello callback");
        ArrayList<Integer> winningNums = new ArrayList<Integer>();
        for(int i =0 ; i < result.size(); i++)
        {
            String[] aRecord = result.get(i);
            for(int j =1 ; j < aRecord.length; j++)
            {
                winningNums.add(new Integer(aRecord[j]));
                Log.d("lottoMaster", "result[" + i + "] = " + aRecord[j]);
            }
            _lottoSQLHelper.addWinningNumbers(winningNums, aRecord[0]);
            winningNums.clear();
        }
        //we must stop the task after it's finished
        //boolean aa = downloadTask.cancel(true);
        //Log.d("lottoMaster","hello @@@@@@@@@=" + aa);
    }


    /*** TESTING MEHTOD ***/
    public void doDBTest()
    {
        _lottoSQLHelper.testSelectAll();
        _lottoSQLHelper.testLatestDate();
    }

}



