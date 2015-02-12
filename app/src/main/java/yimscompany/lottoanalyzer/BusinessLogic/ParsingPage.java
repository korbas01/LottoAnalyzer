package yimscompany.lottoanalyzer.BusinessLogic;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* requesting query for past winning numbers to olg website then store records into DB
* Author: shyim
*/
public class ParsingPage {
    private static final String OLG_LAST_WINNING_NUMBERS_URL = "http://www.olg.ca/lotteries/viewWinningNumbers.do";
    private static final String OLG_PAST_WINNING_NUMBERS_URL = "http://www.olg.ca/lotteries/viewPastNumbers.do";
    private SQLHelper _lottoSQLHelper;

    public ParsingPage(Context c)
    {
        this._lottoSQLHelper = new SQLHelper(c);
    }


    //get the most recent winning numbers (excluding a bonus number)
    public ArrayList<Integer> ParsingLastWinningNumbers()
    {
        //is it already in the db?
        if(_lottoSQLHelper != null && _lottoSQLHelper.GetLastWinningNumbers(1).size()>0) {
            ArrayList<Integer> result = new ArrayList<>();
            result = _lottoSQLHelper.GetLastWinningNumbers(1);
            if(result.size() > 0)
                return result;
        }


        URL url = null;
        //result contains past winning numbers,
        //index 0: data, index 1-6: winning numbers, index 7: bonus number, index8: encore number
        String[] aRecord = new String[9];
        List<String[]> result = new ArrayList<String[]>();
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

            for(int i =1 ; i <= 6; i++) {
//				winningNums[i] = lotto649WinningnumRow.children().get(i+1).attr("alt");
                aRecord[i] =lotto649WinningnumRow.children().get(i).attr("alt");
//				Log.d("DownloadPageRunnable","winningnumbers123 ="+ aRecord[i]);

            }

            aRecord[7] = lotto649Row.children().get(2).children().get(0).attr("alt");
            aRecord[8] = lotto649Row.children().get(3).children().get(0).attr("alt");
            result.add(aRecord);
            this.insertIntoDB(result);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return ParsingLastWinningNumbers();
    }

    /*it needs to construct a query in order to avoid using their forms,
     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
     * gameID: a game id, lotto649 is 1,
     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
     * 					  last two numbers, year. OLG has the last one year record
     * day: if it's 0, it shows all past winning numbers in a month respectively
     * x,y: some random numbers I couldn't figure out what those numbers are for
     * @param: gameID: number represents game e.g.) 1 = lotto 649
    */
    public void ParsingPastWinningNumbers(String quseryStr)
    {
        URL url = null;
        //result contains past winning numbers,
        //index 0: data, index 1-6: winning numberes, index 7: bonus number
        List<String[]> result = new ArrayList<String[]>();
        String requestURL="";
        try {
            if(quseryStr.isEmpty())
            {
                throw new MalformedURLException("URL for Pasting Winning numbers does not have a query string");
            }
            requestURL =OLG_PAST_WINNING_NUMBERS_URL + "?" + quseryStr;

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        Document doc = null;
        try {
//			Log.d("DownloadPageRunnable", "parsingPastWinningNumbers()::url=" + requestURL);
            String sUrl = String.format(requestURL);
            doc = Jsoup.connect(sUrl).get();

            Element winningNumTable = doc.getElementById("lottery_border");
            //children[3] represent <tr> of lotto 649
//			Log.d("DownloadPageRunnable", "@@@aURL=" + sUrl);
//			Log.d("DownloadPageRunnable","@@@WinningPastNumbers="+winningNumTable.children().get(0).children().size());
            Element lottoTable =  winningNumTable.children().get(0);
            for(int i =1 ; i < lottoTable.children().size() ; i++ )
            {
                String[] aRecord = new String[9];
                Element lotto649WinningnumRow = lottoTable.children().get(i);
                //aRecord[0] = lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim();
                aRecord[0] = parsingDate(lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim());

                //OLG has changed their table format, we must handle the exceptions whether the first row is String or winning numbers
                String winningNum = lotto649WinningnumRow.children().get(2).children().get(0).children().get(0).html().trim();

                if(winningNum.equalsIgnoreCase("MAIN DRAW"))
                {
                    //get the next row
                    winningNum = lotto649WinningnumRow.children().get(2).children().get(0).children().get(1).html().trim();
                }

                String[] winningNumbers = winningNum.split(" ");
                if(winningNumbers.length >= 6)
                {
                    aRecord[1] = winningNumbers[0];
                    aRecord[2] = winningNumbers[1];
                    aRecord[3] = winningNumbers[2];
                    aRecord[4] = winningNumbers[3];
                    aRecord[5] = winningNumbers[4];
                    aRecord[6] = winningNumbers[5];
                    aRecord[7] = lotto649WinningnumRow.children().get(3).children().get(0).children().get(0).html().trim();
                    aRecord[8] = lotto649WinningnumRow.children().get(4).children().get(0).children().get(0).html().trim();
                    result.add(aRecord);
                }
                //TODO: parsing encore
                Log.d("DownloadPageRunnable", "Parsing Result=" + Arrays.asList(aRecord).toString().toString() + "::at " + parsingDate(lotto649WinningnumRow.children().get(0).children().get(0).children().get(0).html().trim()));
            }
            this.insertIntoDB(result);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    //TODO : implement parsing encore numers
    public void ParsingEncoreNumbers()
    {

    }

    /* put parsing results into the DB
     * */
    private void insertIntoDB(List<String[]> result)
    {
        try{
            ArrayList<Integer> winningNums = new ArrayList<Integer>();
            for(int i =0 ; i < result.size(); i++)
            {
                String[] aRecord = result.get(i);
                for(int j =1 ; j < aRecord.length; j++)
                {
                    winningNums.add(new Integer(aRecord[j]));
//					Log.d("lottoMaster", "result[" + i + "] = " + aRecord[j]);
                }
                this._lottoSQLHelper.addWinningNumbers(winningNums, aRecord[0]);
                winningNums.clear();
            }

        }catch(IllegalArgumentException e)
        {

        }
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
//		Log.d("DownloadPageRunnable","@@@@@@ParsingDate@@@@@@@");
//		Log.d("DownloadPageRunnable","@@@@@@result " + result + "@@@@@@@");
//		Log.d("DownloadPageRunnable","@@@@@@@@@@@@@@@@@@@@@@@@");

        return result;
    }

}