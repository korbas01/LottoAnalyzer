package yimscompany.lottoanalyzer.BusinessLogic.OLG;

import android.content.Context;
import android.content.res.Resources;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;
import yimscompany.lottoanalyzer.Components.LottoRecordComparator;
import yimscompany.lottoanalyzer.DataAccessLayer.SQLHelper;
import yimscompany.lottoanalyzer.Exceptions.InvalidConnectionException;
import yimscompany.lottoanalyzer.Interfaces.ParsingPage;
import yimscompany.lottoanalyzer.R;

/* requesting query for past winning numbers to olg website then store records into DB
* this class doesn't need to know what types we want to parse because it receives the
* query string from outside.
* todo: how do I get the number of children in an element?
* Author: shyim
*/
public class ParsingOLGPage extends NullPointerException implements ParsingPage {
    private static final String OLG_LAST_WINNING_NUMBERS_URL = "http://www.olg.ca/lotteries/viewWinningNumbers.do";
    private static final String OLG_PAST_WINNING_NUMBERS_URL = "http://www.olg.ca/lotteries/viewPastNumbers.do";
    private SQLHelper mLottoSQLHelper;
    private LottoGame mSelectedGame;
    private Context mContext;

    public ParsingOLGPage(Context c, LottoGame game)
    {
        mSelectedGame = game;
        mContext = c;
        //this.mLottoSQLHelper = SQLHelper.getInstance(c, game);
        this.mLottoSQLHelper = new SQLHelper(c, game);
    }


    /**
     * parsing the most recent winning numbers (excluding a bonus number) and
     * add in to DB
     * @return
     */
    public ArrayList<Integer> ParsingLastWinningNumbers()
    {

        URL url = null;
        //result contains past winning numbers,
        //index 0: data, index 1-6: winning numbers, index 7: bonus number, index8: encore number
        String[] aRecord = new String[9];
        ArrayList<Integer> result = new ArrayList<>();
        List<String[]> listNums = new ArrayList<String[]>();
        try {
            url = new URL(OLG_LAST_WINNING_NUMBERS_URL);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        Document doc = null;
        try {
            String sUrl = OLG_LAST_WINNING_NUMBERS_URL;
            doc = Jsoup.connect(sUrl).get();

            Element winningNumTable = doc.getElementById("lottery_border");
            //children[3] represent <tr> of lotto 649
            Element lotto649Row = winningNumTable.children().get(0).children().get(3);
            Element lotto649WinningnumRow = lotto649Row.children().get(1);
            String winningNumDate = lotto649WinningnumRow.children().get(0).html().trim();
//			Log.d("DownloadPageRunnable","winningnum date123="+winningNumDate);

            //parsing month and year from the URL and day from the HTML
            aRecord[0] = parsingDate(winningNumDate);


            for(int i =1 ; i <= mSelectedGame.getSetOfNums(); i++) {
//				winningNums[i] = lotto649WinningnumRow.children().get(i+1).attr("alt");
                aRecord[i] =lotto649WinningnumRow.children().get(i).attr("alt");
                result.add(new Integer(aRecord[i]));
            }

            aRecord[7] = lotto649Row.children().get(2).children().get(0).attr("alt");
            aRecord[8] = lotto649Row.children().get(3).children().get(0).attr("alt");
            listNums.add(aRecord);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    /*it needs to construct a query in order to avoid using their forms,
     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
     * gameID: a game id, lotto649 is 1, lottomax is 73
     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
     * 					  last two numbers, year. OLG has the last one year record
     * day: if it's 0, it shows all past winning numbers in a month respectively
     * x,y: some random numbers I couldn't figure out what those numbers are for
     * @param: gameID: number represents game e.g.) 1 = lotto 649
    */
    public void ParsingPastWinningNumbers(String queryStr) throws InvalidConnectionException
    {
        URL url = null;
        //result contains past winning numbers,
        //index 0: data, index 1-6: winning numberes, index 7: bonus number
        String requestURL="";
        try {
            if(queryStr.isEmpty())
            {
                throw new MalformedURLException("URL for Pasting Winning numbers does not have a query string");
            }
            requestURL =OLG_PAST_WINNING_NUMBERS_URL + "?" + queryStr;

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        Document doc = null;
        ArrayList<LottoRecord> lottoRecords = new ArrayList<>();
        try {
            String sUrl = String.format(requestURL);
            doc = Jsoup.connect(sUrl).get();

            //olg website has been changed...
            Elements winningNumTable = doc.select("table.font.lottery_border");

            //table contents
            Element lottoTable =  winningNumTable.get(0).children().get(0);
            lottoRecords = parsingLottoPastWinningNumTable(lottoTable) ;


            if(!lottoRecords.isEmpty()) {
                mLottoSQLHelper.addGameRecord(mSelectedGame, lottoRecords);
            }

        }catch(Exception e1) {
           //throw new InvalidConnectionException(mContext.getString(R.string.err_no_connection));
        }

    }

    public boolean IsUpToDate(String queryStr) {
        //parsing olg web page and get the last date
        ArrayList<LottoRecord> records = getMostRecentWinningRecords(queryStr);
        Collections.sort(records, new LottoRecordComparator());
        if(records.size() == 0) {
            return false;
        }

        String lastDate = records.get(records.size()-1).getDate();
        //requesting DB to get the last date

        ArrayList<LottoRecord> recentGame = mLottoSQLHelper.GetRecentGame(mSelectedGame,1);

        if(recentGame != null && recentGame.size() > 0) {
//            Log.d("lottoanalyzer" , "compare two values :" + lastDate + ", " +recentGame.get(0).getDate() +"; returns " + lastDate.equals(recentGame.get(0).getDate()));
            return lastDate.equals(recentGame.get(0).getDate());
        }
        return false;
    }


    private ArrayList<LottoRecord> parsingLottoPastWinningNumTable(Element lottoTable) {
        Resources res = mContext.getResources();
        if(res.getInteger(R.integer.OLG_LOTTO649_GAME_ID) == mSelectedGame.getGameID()) {
            return parsingLotto649Table(lottoTable);
        }else if(res.getInteger(R.integer.OLG_LOTTOMAX_GAME_ID) == mSelectedGame.getGameID()){
            return parsingLottoMaxTable(lottoTable);
        }
        return new ArrayList<LottoRecord>();
    }

    /**
     * it will parse a table with "Date Day Numbers Bonus Encore Winnings" header
     * the winning number should have 6
     * @param lottoTable: it's a table contains past winning numbers
     * @return past winning number records
     */
    private ArrayList<LottoRecord> parsingLotto649Table(Element lottoTable){
        ArrayList<LottoRecord> lottoRecords = new ArrayList<>();
        //retrieving from the second row, first row is header
        for(int i =1 ; i < lottoTable.children().size() ; i++ )
        {
            //String[] aRecord = new String[9];
            Element lotto649WinningnumRow = lottoTable.children().get(i);
            //aRecord[0] = parsingDate(lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim());
            String date =  parsingDate(lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim());
            //OLG has changed their table format, we must handle the exceptions whether the first row is String or winning numbers
            String winningNum = lotto649WinningnumRow.children().get(2).children().get(0).children().get(0).html().trim();

            if(winningNum.equalsIgnoreCase("MAIN DRAW"))
            {
                //get the next row
                winningNum = lotto649WinningnumRow.children().get(2).children().get(0).children().get(1).html().trim();
            }

            //String[] winningNumbers = winningNum.split(" ");
            ArrayList<Integer> winningNumbers = new ArrayList<>();

            for(String aNumber : winningNum.split(" ")) {
                try{
                    winningNumbers.add(new Integer(aNumber));
                }catch(NumberFormatException e){
                    //td content might have comment which causes to throw number format exception.
                }
            }

            Integer bonusNum = new Integer(lotto649WinningnumRow.children().get(3).children().get(0).children().get(0).html().trim()); //bonus num
            Integer encoreNum = new Integer(lotto649WinningnumRow.children().get(4).children().get(0).children().get(0).html().trim()); //encore num
            lottoRecords.add(new LottoRecord(winningNumbers, bonusNum, encoreNum, date));

        }
        return lottoRecords;
    }

    /**
     * it will parse a table with "Date Numbers Bonus Encore Winnings" header
     * the winning number should have 7
     * @param lottoTable: it's a table contains past winning numbers
     * @return past winning number records
     */
    private ArrayList<LottoRecord> parsingLottoMaxTable(Element lottoTable){
        ArrayList<LottoRecord> lottoRecords = new ArrayList<>();
        //retrieving from the second row, first row is header
        for(int i =1 ; i < lottoTable.children().size() ; i++ )
        {
            //String[] aRecord = new String[9];
            Element lottoMaxWinningnumRow = lottoTable.children().get(i);
            //aRecord[0] = parsingDate(lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim());
            String date =  parsingDate(lottoMaxWinningnumRow.children().get(0).children().get(0).children().get(0).html().trim());
            //OLG has changed their table format, we must handle the exceptions whether the first row is String or winning numbers
            String winningNum = lottoMaxWinningnumRow.children().get(1).children().get(0).children().get(0).html().trim();

            if(winningNum.equalsIgnoreCase("MAIN DRAW"))
            {
                //get the first child in the next row
                winningNum = lottoMaxWinningnumRow.children().get(1).children().get(0).children().get(1).html().trim();
            }

            ArrayList<Integer> winningNumbers = new ArrayList<>();

            //we must parse the first 7 numbers because winningNum also contains maxmillion winning nums
            int parsingNumberCount = 0;
            for(String aNumber : winningNum.split(" ")) {
                try{
                    if(parsingNumberCount < 7)
                        winningNumbers.add(new Integer(aNumber));
                    parsingNumberCount ++;
                }catch(NumberFormatException e){
                    //td content might have comment which causes to throw number format exception.
                }
            }

            Integer bonusNum = new Integer(lottoMaxWinningnumRow.children().get(2).children().get(0).children().get(0).html().trim()); //bonus num
            Integer encoreNum = new Integer(lottoMaxWinningnumRow.children().get(3).children().get(0).children().get(0).html().trim()); //encore num
            lottoRecords.add(new LottoRecord(winningNumbers, bonusNum, encoreNum, date));

        }
        return lottoRecords;
    }


    /**
     * it will parse OLG DOM document and extract info. what we need then it will format data in LottoRecord
     * @param queryStr: query string to get the OLG past winning number
     * @return list of LottoRecord
     */
    private ArrayList<LottoRecord> getMostRecentWinningRecords(String queryStr) {
        URL url = null;
        String requestURL="";
        try {
            if(queryStr.isEmpty())
            {
                throw new MalformedURLException("URL for Pasting Winning numbers does not have a query string");
            }
            requestURL =OLG_PAST_WINNING_NUMBERS_URL + "?" + queryStr;

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        Document doc = null;
        ArrayList<LottoRecord> lottoRecords = new ArrayList<>();
        try {
            String sUrl = String.format(requestURL);
            doc = Jsoup.connect(sUrl).get();

            Elements winningNumTable = doc.select("table.font.lottery_border");
            //table contents
            Element lottoTable =  winningNumTable.get(0).children().get(0);
            lottoRecords = parsingLottoPastWinningNumTable(lottoTable);


        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
        return lottoRecords;

    }



    /* @param: date: it's what we're going to covert to Julian format,
     *   	   value of date will be something like "04-Jan-2014"
     * */
    private String parsingDate(String date)
    {
        String result = "";

        String[] d = date.split("-");
        //we need to convert month to a numeric value
        if(d[1].equalsIgnoreCase("Jan"))
        {
            d[1] = "01";
        }else if(d[1].equalsIgnoreCase("Feb"))
        {
            d[1] = "02";
        }else if(d[1].equalsIgnoreCase("Mar"))
        {
            d[1] = "03";
        }else if(d[1].equalsIgnoreCase("Apr"))
        {
            d[1] = "04";
        }else if(d[1].equalsIgnoreCase("May"))
        {
            d[1] = "05";
        }else if(d[1].equalsIgnoreCase("Jun"))
        {
            d[1] = "06";
        }else if(d[1].equalsIgnoreCase("Jul"))
        {
            d[1] = "07";
        }else if(d[1].equalsIgnoreCase("Aug"))
        {
            d[1] = "08";
        }else if(d[1].equalsIgnoreCase("Sep"))
        {
            d[1] = "09";
        }else if(d[1].equalsIgnoreCase("Oct"))
        {
            d[1] = "10";
        }else if(d[1].equalsIgnoreCase("Nov"))
        {
            d[1] = "11";
        }else if(d[1].equalsIgnoreCase("Dec"))
        {
            d[1] = "12";
        }
        result = d[2] +"-" + d[1] + "-" + d[0];
        return result;
    }

}