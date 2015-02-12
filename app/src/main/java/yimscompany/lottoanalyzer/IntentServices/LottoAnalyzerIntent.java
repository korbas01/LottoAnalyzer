package yimscompany.lottoanalyzer.IntentServices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashSet;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.BusinessLogic.RandomNumberGenerator;
import yimscompany.lottoanalyzer.MainActivity;

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

    public static final String EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER = "yimscompany.lottoanalyzer.IntentService.IntArrayList.MSG.Out";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRun(Context context) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.setAction(ACTION_RUN);
        context.startService(intent);
    }

    public static void startActionGetPossibleNums(Context context) {
        Intent intent = new Intent(context, LottoAnalyzerIntent.class);
        intent.setAction(ACTION_GET_POSSIBLE_NUMBERS);
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
            if (ACTION_RUN.equals(action)) {
                ArrayList<Integer> r = handleActionRun();
                ArrayList<Integer> result = new ArrayList<>(new HashSet<>(r));
                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,result);
            }else if(ACTION_GET_POSSIBLE_NUMBERS.equals(action)) {
                ArrayList<Integer> r = getPossibleWinningNums();
                broadcastIntent.putIntegerArrayListExtra(EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER,r);
            }

            broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Handle action Run in the provided background thread with the provided
     * parameters.
     */
    private ArrayList<Integer> handleActionRun() {
        LottoAnalyzer analyzer = new LottoAnalyzer(getApplicationContext());
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> possibleWinningNums = analyzer.Run();

        RandomNumberGenerator randIndex = new RandomNumberGenerator(possibleWinningNums.size()-1);
        ArrayList<Integer> listIndex = new ArrayList<Integer>(randIndex.generateNumbers(6));

        for(Integer i : listIndex)
        {
            result.add(possibleWinningNums.get(i.intValue()));
        }

        return result;
    }

    private ArrayList<Integer> getPossibleWinningNums(){
        LottoAnalyzer analyzer = new LottoAnalyzer(getApplicationContext());
        return analyzer.Run();
    }

}
