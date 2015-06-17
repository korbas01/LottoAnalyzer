package yimscompany.lottoanalyzer.IntentServices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

import yimscompany.lottoanalyzer.BusinessLogic.OLG.ParsingOLGPage;
import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.Exceptions.InvalidConnectionException;
import yimscompany.lottoanalyzer.MainActivity;
import yimscompany.lottoanalyzer.SelectGameActivity;

/**
 * todo: need to find a better way to maintain parsing logic, if OLG changed their structure, our business logic will not work at all..
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class OLGParsingPageIntent extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PAST_WIN_NUMS = "yimscompany.lottoanalyzer.IntentServices.action.PastWinNums";
    private static final String ACTION_LAST_WIN_NUMS = "yimscompany.lottoanalyzer.IntentServices.action.LastWinNums";
    private static final String ACTION_CHECK_UPDATE = "yimscompany.lottoanalyzer.IntentServices.action.CheckUpdate";
    private static final String EXTRA_PARAM_QUERY_STRING = "yimscompany.lottoanalyzer.IntentServices.extra.QueryString";
    private static final String EXTRA_PARAM_QUERY_SERIALIZABLE = "yimscompany.lottoanalyzer.IntentServices.extra.Serializable";
    public static final String EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER = "yimscompany.lottoanalyzer.IntentService.IntArrayList.MSG.Out";



    public OLGParsingPageIntent() {
        super("ParsingPageIntent");
    }

    /**
     * Starts this service to perform action PastWinNums with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPastWinNums(Context context, LottoGame game) {
        Intent intent = new Intent(context, OLGParsingPageIntent.class);
        intent.setAction(ACTION_PAST_WIN_NUMS);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action LastWinNum. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionLastWinNums(Context context, LottoGame game) {
        Intent intent = new Intent(context, OLGParsingPageIntent.class);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        intent.setAction(ACTION_LAST_WIN_NUMS);
        context.startService(intent);
    }


    public static void startActionCheckUpdate(Context context, LottoGame game) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent broadcastIntent = new Intent();
            final String action = intent.getAction();
            if (ACTION_PAST_WIN_NUMS.equals(action)) {
                final LottoGame game = (LottoGame) intent.getSerializableExtra(EXTRA_PARAM_QUERY_SERIALIZABLE);
                try{
                    handleActionParsingPastWinNums(game);
                    broadcastIntent.setAction(SelectGameActivity.ResponseReceiver.ACTION_RESP_PAST_WIN_NUMS);
                }catch(InvalidConnectionException e) {
                    broadcastIntent.setAction(SelectGameActivity.ResponseReceiver.ACTION_RESP_INVALID_CONN_ERR);
                }
            } else if (ACTION_LAST_WIN_NUMS.equals(action)) {
                LottoGame selectedGame = (LottoGame) intent.getSerializableExtra(EXTRA_PARAM_QUERY_SERIALIZABLE);
                ArrayList<Integer> r = handleActionLastWinNums(selectedGame);
                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,r);
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LAST_WIN_NUMS);
            }else if(ACTION_CHECK_UPDATE.equals(action)) {
                //TODO: compare last winning num data from db and the web
                if(isUpToDate(null)){

                }
            }
            //postback result to activity
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        }
    }




    /**
     * Handle action Past Winning nums in the provided background thread.
     */
    private void handleActionParsingPastWinNums(LottoGame game) throws InvalidConnectionException{
        ParsingOLGPage pPage = new ParsingOLGPage(getApplicationContext(),game);
        //get the current Year and Month
        Calendar c = Calendar.getInstance();
        int currMonth = c.get(Calendar.MONTH);
        int currYear = c.get(Calendar.YEAR) - 1;

        //we will get the last one year records
        int count = 0;
        boolean isUpToDate = false;


        String updateChkQueryString = "command=submit&gameID=" +
                game.getGameID() + "&selectedMonthYear=" + String.format("%02d", currMonth) + (new Integer(c.get(Calendar.YEAR)).toString()) + "&day=0&x=31&y=12";

        if(! pPage.IsUpToDate(updateChkQueryString)) {
            while (count < 13 && !isUpToDate) {
                currMonth = currMonth % 12;
                if (count > 0 && currMonth == 0) {
                    currYear++;
                }
                //construct query
                String selectedMonthYear = String.format("%02d", currMonth) + (new Integer(currYear).toString());
                String queryString = "command=submit&gameID=" +
                        game.getGameID() + "&selectedMonthYear=" + selectedMonthYear + "&day=0&x=31&y=12";
                if (count >= 0) {
                    //calling past winning numbers
                    pPage.ParsingPastWinningNumbers(queryString);
                }
                count++;
                currMonth++;

            }
        }

    }

    /**
     * Handle action Last Winning Nums in the provided background thread with parameters.
     *
     */
    private ArrayList<Integer> handleActionLastWinNums(LottoGame selectedGame) {
        ParsingOLGPage pPage = new ParsingOLGPage(getApplicationContext(), selectedGame);
        return pPage.ParsingLastWinningNumbers();
        //todo: fix or handle?
        //return null;
    }

    /**
     * check whether the DB is up to date or not
     * @param game: it contains game info.
     * @return true if it's up to date
     */
    private boolean isUpToDate(LottoGame game){
        //ParsingOLGPage pPage = new ParsingOLGPage(getApplicationContext());
        //TODO: has not implemented yet!
        //ArrayList<Integer> lastWinningNumsFromDB = pPage.ParsingLastWinningNumbers();
        return false;
    }


}
