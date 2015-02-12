package yimscompany.lottoanalyzer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.Components.MonitoringConnectivity;
import yimscompany.lottoanalyzer.IntentServices.LottoAnalyzerIntent;
import yimscompany.lottoanalyzer.IntentServices.ParsingPageIntent;

public class MainActivity extends Activity {
    private LottoAnalyzer _lottoAnalyzer;
    private ResponseReceiver _intentReceiver;
    private MonitoringConnectivity _mConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init. helper classes
        this._lottoAnalyzer = new LottoAnalyzer(this);
        setContentView(R.layout.activity_main);

        //TODO : sync. data...
        //TODO : unregister receiver in proper time
        //TODO : error msgbox or popup box(?) (Component)
        //      ref: http://developer.android.com/guide/topics/ui/dialogs.html
        //TODO : examples of speical UI effect, sliding etc..  (Component)
        //TODO : enhancing lotto analyzer business logic
        //TODO : img file for each number
        //TODO : get rid of displayResult handler in this activity
        //TODO : ----ConnectivityManager----  (Component)
        //       ref: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
        //TODO : check pickers... I might use pickers to set up the analyzing factors
        //TODO : UI design, icons and etc,

        registerIntentReceiver();
        registerButtons();
        initAds();
        initComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerIntentReceiver();
        registerButtons();
    }

    @Override
    protected void onPause() {
        unregisterIntentReceiver();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerIntentReceiver();
        registerButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    final OnClickListener onLottoAnalyzerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("lottoMaster", "getid =" + v.getId());

            switch (v.getId()) {
                case R.id.BtnGetLastWinNums:
                    getLastWinningNumbers();
                    break;
                case R.id.BtnRun:
                    // TODO: think about a better design..
                    // TODO: might implement intentservice broadcast receiver for displaying results
                    getExpectedWinningNumbers();
                    break;
                case R.id.BtnGetAllNums:
                    getAllPossibleWinningNumbers();
                    break;
                default:
                    break;
            }
        }
    };

    private void getLastWinningNumbers()
    {
        ParsingPageIntent.startActionLastWinNums(this);
    }
    private void getExpectedWinningNumbers() {
        LottoAnalyzerIntent.startActionRun(this);
    }
    private void initComponents() {
        _mConnectivity = new MonitoringConnectivity(this);
    }

    private void getAllPossibleWinningNumbers() {LottoAnalyzerIntent.startActionGetPossibleNums(this);}


    private void registerButtons() {
        findViewById(R.id.BtnGetLastWinNums).setOnClickListener(onLottoAnalyzerClickListener);
        findViewById(R.id.BtnRun).setOnClickListener(onLottoAnalyzerClickListener);
        findViewById(R.id.BtnGetAllNums).setOnClickListener(onLottoAnalyzerClickListener);
    }

    private void registerIntentReceiver()
    {
        _intentReceiver = new ResponseReceiver();
        IntentFilter lastWinNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_LAST_WIN_NUMS);
        lastWinNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);

        IntentFilter runFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER);
        runFilter.addCategory(Intent.CATEGORY_DEFAULT);


        registerReceiver(_intentReceiver, lastWinNumsFilter);
        registerReceiver(_intentReceiver, runFilter);
    }

    private void unregisterIntentReceiver() {
        unregisterReceiver(_intentReceiver);
    }

    private void initAds(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Settings.Secure.ANDROID_ID).build();
        mAdView.loadAd(adRequest);
    }

    //sub class for receiving IntentServices
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String EXTRA_PARAM_SEND_RESULT = "yimscompany.lottoanalyzer.intent.send.msg";
        public static final String ACTION_RESP_LAST_WIN_NUMS =
                "yimscompany.lottoanalyzer.intent.action.resp.LAST_WIN_NUMS_MESSAGE_PROCESSED";
        public static final String ACTION_RESP_LOTTO_ANALYZER =
                "yimscompany.lottoanalyzer.intent.actions.resp";

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView result = (TextView) findViewById(R.id.TxtBoxResult);
            final String action = intent.getAction();
            String msgOut ="";
            if (action.equals(ACTION_RESP_LAST_WIN_NUMS)) {
                ArrayList<Integer> r = intent.getIntegerArrayListExtra(ParsingPageIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER);
                msgOut = r.toString();
            } else if (action.equals(ACTION_RESP_LOTTO_ANALYZER))
            {
                ArrayList<Integer> r = intent.getIntegerArrayListExtra(LottoAnalyzerIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER);
                msgOut = r.toString() + " Total:" + r.size();
                Intent i = new Intent(MainActivity.this, DisplayResultActivity.class);
                i.putExtra(EXTRA_PARAM_SEND_RESULT, msgOut);
                startActivity(i);
            }
            result.setText(msgOut);
        }
    }
}
