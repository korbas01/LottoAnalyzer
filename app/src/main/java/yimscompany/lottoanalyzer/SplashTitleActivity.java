package yimscompany.lottoanalyzer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.Components.LottoAnalyzerDialogFragment;
import yimscompany.lottoanalyzer.Components.MonitoringConnectivity;
import yimscompany.lottoanalyzer.IntentServices.LottoAnalyzerIntent;
import yimscompany.lottoanalyzer.IntentServices.ParsingPageIntent;

public class SplashTitleActivity extends Activity implements LottoAnalyzerDialogFragment.LottoDialogFragmentListener {
    private ResponseReceiver _intentReceiver;
    private ProgressDialog _progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_title);
        if(_intentReceiver == null) {
            registerIntentReceiver();
        }
        checkConnectivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerIntentReceiver();
        checkConnectivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(_intentReceiver);
    }

    @Override
    public void onReturnValue(boolean isOk) {
        if(isOk) {
            checkConnectivity();
        }
    }

    /* check connectivity whether it's able to download html content or not.
        * */
    public void checkConnectivity() {
        if( ! new MonitoringConnectivity(this).IsConnected()  ) {

            Bundle dialogArgs = new Bundle();
            dialogArgs.putString(LottoAnalyzerDialogFragment.DIALOG_MSG,"It's not connected to internet. Please try again later.");
            dialogArgs.putString(LottoAnalyzerDialogFragment.DIALOG_POS_BTN_LABEL,"OK") ;
            showAlert(dialogArgs);
        }else {
            showProgressSpin();
            for(LottoAnalyzer.GameType game: LottoAnalyzer.GameType.values())
            {
                parsingPastWinningNumbers(game);
            }
        }
    }

    private void showProgressSpin() {
        if(_progressDialog == null) {
            _progressDialog = new ProgressDialog(this);
        }
        _progressDialog.setMessage("Verifying data....");
        _progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        _progressDialog.setIndeterminate(true);
        _progressDialog.show();
    }

    private void registerIntentReceiver() {
        IntentFilter pastWinNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_PAST_WIN_NUMS);
        pastWinNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        _intentReceiver = new ResponseReceiver();
        registerReceiver(_intentReceiver, pastWinNumsFilter);
    }


    /* showAlert will pop up dialog,
       @param dialogArgs: string bundles which should have string context of dialog info. (title, msg etc)
     */
    private void showAlert(Bundle dialogArgs) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        LottoAnalyzerDialogFragment newFragment = LottoAnalyzerDialogFragment.newInstance(dialogArgs);
        newFragment.show(ft, "dialog");
    }

    /*it needs to generate a query in order to avoid using their forms,
     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
     * gameID: a game id, lotto649 is 1,
     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
     * 					  last two numbers, year. OLG has the last one year record
     * day: if it's 0, it shows all past winning numbers in a month respectively
     * x,y: some random numbers I couldn't figure out what those numbers are for
     * @param: gameID: number represents game e.g.) 1 = lotto 649
     */
    private void parsingPastWinningNumbers(LottoAnalyzer.GameType gameType) {
        ParsingPageIntent.startActionPastWinNums(this, gameType);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //sub class for receiving IntentServices
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP_PAST_WIN_NUMS =
                "yimscompany.lottoanalyzer.intent.action.resp.PAST_WIN_NUMS_MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_RESP_PAST_WIN_NUMS) && !isMyServiceRunning(LottoAnalyzerIntent.class)) {
                _progressDialog.dismiss();
                Intent i = new Intent(SplashTitleActivity.this, MainActivity.class);
                startActivity(i);

            }

        }
    }

}
