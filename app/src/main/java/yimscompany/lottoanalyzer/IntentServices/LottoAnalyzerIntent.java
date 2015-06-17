package yimscompany.lottoanalyzer.IntentServices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.BusinessLogic.RandomNumberGenerator;
import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;
import yimscompany.lottoanalyzer.DataAccessLayer.SQLHistoryHelper;
import yimscompany.lottoanalyzer.MainActivity;
import yimscompany.lottoanalyzer.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LottoAnalyzerIntent extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RUN = "yimscompany.lottoanalyzer.IntentServices.action.RUN";
    private static final String ACTION_GET_POSSIBLE_NUMBERS = "yimscompany.lottoanalyzer.IntentServices.action.GET_POSSIBLE_NUMS";
    private static final String ACTION_GET_HISTORY = "yimscompany.lottoanalyzer.IntentServices.action.GET_HISTORY";
    private static final String EXTRA_PARAM_QUERY_SERIALIZABLE = "yimscompany.lottoanalyzer.extra.Serializable.Param1";
    public static final String EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER = "yimscompany.lottoanalyzer.IntentService.IntArrayList.MSG.Out";
    public static final String EXTRA_PARAM_OUT_MSG_ARRAYLIST_PARCELABLE = "yimscompany.lottoanalyzer.IntentService.ParcelableArrayList.MSG.Out";

    private static SQLHistoryHelper mHistoryHelper;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRun(Context context, LottoGame game) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        intent.setAction(ACTION_RUN);
        //mHistoryHelper = SQLHistoryHelper.getInstance(context, game);
        mHistoryHelper = new SQLHistoryHelper(context, game);
        context.startService(intent);
    }

    public static void startActionGetPossibleNums(Context context, LottoGame game) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        intent.setAction(ACTION_GET_POSSIBLE_NUMBERS);
        context.startService(intent);
    }

    public static void startActionGetHistory(Context context, LottoGame game) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.putExtra(EXTRA_PARAM_QUERY_SERIALIZABLE, game);
        intent.setAction(ACTION_GET_HISTORY);
        mHistoryHelper = new SQLHistoryHelper(context, game);
        context.startService(intent);
    }


    public LottoAnalyzerIntent() {
        super("LottoAnalyzerIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent broadcastIntent = new Intent();
            final String action = intent.getAction();
            LottoGame gType = (LottoGame) intent.getSerializableExtra(EXTRA_PARAM_QUERY_SERIALIZABLE);
            if (ACTION_RUN.equals(action)) {

                ArrayList<LottoRecord> r = handleActionRun(gType);
//                ArrayList<Integer> result = new ArrayList<>(new HashSet<>(r));
//                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,result);
                mHistoryHelper.addMyHistory(r);
                broadcastIntent.putParcelableArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_PARCELABLE,r);
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS);


            }else if(ACTION_GET_POSSIBLE_NUMBERS.equals(action)) {
                ArrayList<Integer> r = getPossibleWinningNums(gType);
                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,r);
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS);

            }else if(ACTION_GET_HISTORY.equals(action)) {
                ArrayList<LottoRecord> r = handleActionGetHistory(gType);
                broadcastIntent.putParcelableArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_PARCELABLE,r);
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY);


            }
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Handle action Run in the provided background thread with the provided
     * parameters.
     */
    private ArrayList<LottoRecord> handleActionRun(LottoGame selectedGame) {

        LottoAnalyzer analyzer = new LottoAnalyzer(getApplicationContext(), selectedGame);
        ArrayList<LottoRecord> result = new ArrayList<>();

        ArrayList<Integer> possibleWinningNums = analyzer.Run();

        RandomNumberGenerator randIndex = new RandomNumberGenerator(possibleWinningNums.size()-1);


        //set number of games
        //PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //getApplicationContext().getEditor();
        for(int i =0; i < pref.getInt(getString(R.string.pref_num_games), 5); i++ )
        {
            ArrayList<Integer> expectedWinningNums = new ArrayList<>();
            ArrayList<Integer> listIndex = randIndex.generateNumbers(selectedGame.getSetOfNums());
            for(Integer j : listIndex)
            {
                expectedWinningNums.add(possibleWinningNums.get(j.intValue()));
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            LottoRecord g = new LottoRecord(expectedWinningNums,0,0,sdf.format(new Date()));
            result.add(g);
        }
        return result;
    }

    private ArrayList<LottoRecord> handleActionGetHistory(LottoGame selectedGame) {
        return mHistoryHelper.GetMyHistory();
    }

    private ArrayList<Integer> getPossibleWinningNums(LottoGame selectedGame){
        LottoAnalyzer analyzer = new LottoAnalyzer(getApplicationContext(),selectedGame);

        return analyzer.Run();
    }

}
