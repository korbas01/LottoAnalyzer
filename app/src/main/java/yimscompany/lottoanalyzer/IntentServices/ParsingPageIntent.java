package yimscompany.lottoanalyzer.IntentServices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.BusinessLogic.ParsingPage;
import yimscompany.lottoanalyzer.MainActivity;
import yimscompany.lottoanalyzer.SplashTitleActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class ParsingPageIntent extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_PAST_WIN_NUMS = "yimscompany.lottoanalyzer.IntentServices.action.PastWinNums";
    public static final String ACTION_LAST_WIN_NUMS = "yimscompany.lottoanalyzer.IntentServices.action.LastWinNums";
    private static final String EXTRA_PARAM_QUERY_STRING = "yimscompany.lottoanalyzer.IntentServices.extra.QueryString";
    private static final String EXTRA_PARAM_QUERY_SERIALIZABLE = "yimscompany.lottoanalyzer.IntentServices.extra.Serializable";
    public static final String EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER = "yimscompany.lottoanalyzer.IntentService.IntArrayList.MSG.Out";



    public ParsingPageIntent() {
        super("ParsingPageIntent");
    }

    /**
     * Starts this service to perform action PastWinNums with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPastWinNums(Context context, LottoAnalyzer.GameType gType) {
        Intent intent = new Intent(context, ParsingPageIntent.class);
        intent.setAction(ACTION_PAST_WIN_NUMS);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, gType);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action LastWinNum. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionLastWinNums(Context context) {
        Intent intent = new Intent(context, ParsingPageIntent.class);
        intent.setAction(ACTION_LAST_WIN_NUMS);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent broadcastIntent = new Intent();
            final String action = intent.getAction();
            if (ACTION_PAST_WIN_NUMS.equals(action)) {
                final LottoAnalyzer.GameType type = (LottoAnalyzer.GameType) intent.getSerializableExtra(EXTRA_PARAM_QUERY_SERIALIZABLE);
                handleActionParsingPastWinNums(type);
                broadcastIntent.setAction(SplashTitleActivity.ResponseReceiver.ACTION_RESP_PAST_WIN_NUMS);
            } else if (ACTION_LAST_WIN_NUMS.equals(action)) {
                ArrayList<Integer> r = handleActionLastWinNums();
                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,r);
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LAST_WIN_NUMS);
            }
            //postback result to activity
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Handle action Past Winning nums in the provided background thread with the provided
     * parameters.
     */
    private void handleActionParsingPastWinNums(LottoAnalyzer.GameType gameType) {
        ParsingPage pPage = new ParsingPage(getApplicationContext());
        //get the current Year and Month
        Calendar c = Calendar.getInstance();
        int currMonth = c.get(Calendar.MONTH);
        int currYear = c.get(Calendar.YEAR) - 1;

        //we will get the last one year records
        int count = 0;
        while (count < 13) {
            currMonth = currMonth % 12;
            if (count > 0 && currMonth == 0) {
                currYear++;
            }
            //construct query
            String selectedMonthYear = String.format("%02d", currMonth) + (new Integer(currYear).toString());
            String queryString = "command=submit&gameID=" +
                    gameType.getValue() + "&selectedMonthYear=" + selectedMonthYear + "&day=0&x=31&y=12";
            if (count >= 0) {
                //calling past winning numbers
                pPage.ParsingPastWinningNumbers(queryString);
            }
            count++;
            currMonth++;

        }
    }

    /**
     * Handle action Last Winning Nums in the provided background thread with the provided
     * parameters.
     */
    private ArrayList<Integer> handleActionLastWinNums() {
        ParsingPage pPage = new ParsingPage(getApplicationContext());
        return pPage.ParsingLastWinningNumbers();
    }
}
