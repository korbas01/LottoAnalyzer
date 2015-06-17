package yimscompany.lottoanalyzer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import yimscompany.lottoanalyzer.BusinessLogic.LottoAnalyzer;
import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;
import yimscompany.lottoanalyzer.Components.MonitoringConnectivity;
import yimscompany.lottoanalyzer.IntentServices.LottoAnalyzerIntent;
import yimscompany.lottoanalyzer.IntentServices.OLGParsingPageIntent;
import yimscompany.lottoanalyzer.UIComponent.LottoUIHelper;

public class MainActivity extends Activity {
    private LottoAnalyzer _OLGAnalyzer;
    private ResponseReceiver mIntentReceiver;
    private MonitoringConnectivity mConnectivity;
    private LottoGame mSelectedGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //init. helper classes

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
        //TODO : design parsing and db classes to handle various games pick 3 pick4 lotto max etc...*****
        //set selected Game
        mSelectedGame = (LottoGame) getIntent().getSerializableExtra("SelectedGame");
        initActionBar();
        super.onCreate(savedInstanceState);

        //this._OLGAnalyzer = new LottoAnalyzer(this,mSelectedGame);
        setContentView(R.layout.activity_main);

        registerIntentReceiver();
        initListView();
        LottoUIHelper.InitAdView((AdView) findViewById(R.id.adView));
        initComponents();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerIntentReceiver();
//        registerButtons();
//        initListView();
//        LottoUIHelper.InitAdView((AdView) findViewById(R.id.adView));
//        initComponents();
    }

    @Override
    protected void onPause() {
        //unregisterIntentReceiver();
        super.onPause();
    }

    @Override
    protected void onStop() {
        //unregisterIntentReceiver();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerIntentReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.actionbar_settings:
                startPrefIntent();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_up, R.anim.push_out_down);
    }

    private void getLastWinningNumbers()
    {
        OLGParsingPageIntent.startActionLastWinNums(this, mSelectedGame);
    }

    private void initActionBar() {
        if(mSelectedGame != null) {
            if(mSelectedGame.getName().equals(getString(R.string.game_lotto649_on_ca))) {
                setTheme(R.style.Lotto649Theme);
            }else if(mSelectedGame.getName().equals(getString(R.string.game_lottomax_on_ca))) {
                setTheme(R.style.LottoMaxTheme);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(mSelectedGame.getName());
            LottoUIHelper.ActionBarTitleCenter(this);

        }else{
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(mSelectedGame.getName());
            LottoUIHelper.ActionBarTitleCenter(this);
        }
    }


    private void initComponents() {
        mConnectivity = new MonitoringConnectivity(this);
    }

    private void initListView() {
        String[] values = new String[] { getString(R.string.main_list_item_0),
                                         getString(R.string.main_list_item_1),
                                         getString(R.string.main_list_item_2)};

        // use your custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_row_layout, R.id.label, values);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(onListViewItemClick);

    }

    final AdapterView.OnItemClickListener onListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String item = (String) parent.getItemAtPosition(position);
            if( item.equals(getString(R.string.main_list_item_0)))     {
                //Next Winning Number
                getExpectedWinningNumbers();
            }else if(item.equals(getString(R.string.main_list_item_1))){
                getAllPossibleWinningNumbers();
            }else if(item.equals(getString(R.string.main_list_item_2))){
                getMyHistory();
            }
        }
    };
//request_num_417574972

    private void startPrefIntent(){
        try{

            Intent i = new Intent(MainActivity.this, LottoPreferenceActivity.class);
            PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
            startActivity(i);
            overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_down);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /* ListView click event helper */
    private void getExpectedWinningNumbers() {
        LottoAnalyzerIntent.startActionRun(this, mSelectedGame);
    }
    private void getAllPossibleWinningNumbers() {LottoAnalyzerIntent.startActionGetPossibleNums(this, mSelectedGame);}
    private void getMyHistory() {
        LottoAnalyzerIntent.startActionGetHistory(this, mSelectedGame);
    }
    private void registerIntentReceiver()
    {
        if(mIntentReceiver == null) {
            mIntentReceiver = new ResponseReceiver();
            IntentFilter lastWinNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_LAST_WIN_NUMS);
            lastWinNumsFilter.addAction(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS);
            lastWinNumsFilter.addAction(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS);
            lastWinNumsFilter.addAction(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY);

            lastWinNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);

//            IntentFilter expectedWinNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS);
//            expectedWinNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);
//
//            IntentFilter getAllPossibleNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS);
//            getAllPossibleNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);

            registerReceiver(mIntentReceiver, lastWinNumsFilter);
//            registerReceiver(mIntentReceiver, runFilter);
//            registerReceiver(mIntentReceiver, getAllPossibleNumsFilter);

        }
    }

    private void unregisterIntentReceiver() {
        try{
            unregisterReceiver(mIntentReceiver);
        }catch(IllegalArgumentException e ){

        }
    }


    //sub class for receiving IntentServices from
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String EXTRA_PARAM_SEND_INTEGER_ARRAYLIST_RESULT = "yimscompany.lottoanalyzer.intent.send.msg";
        public static final String EXTRA_PARAM_SEND_PARCELABLE_ARRAYLIST_RESULT = "yimscompany.lottoanalyzer.intent.parcelable.send.msg";

        public static final String ACTION_RESP_LAST_WIN_NUMS =
                "yimscompany.lottoanalyzer.intent.action.resp.LAST_WIN_NUMS_MESSAGE_PROCESSED";
        public static final String ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS =
                "yimscompany.lottoanalyzer.intent.actions.resp.all.nums";
        public static final String ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS =
                "yimscompany.lottoanalyzer.intent.actions.resp.expected.nums";
        public static final String ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY =
                "yimscompany.lottoanalyzer.intent.action.resp.history";
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_RESP_LAST_WIN_NUMS)) {
                ArrayList<Integer> r = intent.getIntegerArrayListExtra(OLGParsingPageIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER);
            } else if (action.equals(ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS))
            {

                ArrayList<LottoRecord> r = intent.getParcelableArrayListExtra(LottoAnalyzerIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_PARCELABLE);
                Intent i = new Intent(MainActivity.this, DisplayResultActivity.class);
                i.setAction(ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS);
                i.putParcelableArrayListExtra(EXTRA_PARAM_SEND_PARCELABLE_ARRAYLIST_RESULT, r);
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

            }else if (action.equals(ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS))
            {
                ArrayList<Integer> r = intent.getIntegerArrayListExtra(LottoAnalyzerIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_INTEGER);
                Intent i = new Intent(MainActivity.this, DisplayResultActivity.class);
                i.setAction(ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS);
                i.putIntegerArrayListExtra(EXTRA_PARAM_SEND_INTEGER_ARRAYLIST_RESULT, r);
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

            }else if (action.equals(ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY)) {
                ArrayList<LottoRecord> r = intent.getParcelableArrayListExtra(LottoAnalyzerIntent.EXTRA_PARAM_OUT_MSG_ARRAYLIST_PARCELABLE);
                Intent i = new Intent(MainActivity.this, DisplayResultActivity.class);
                i.setAction(ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY);
                i.putParcelableArrayListExtra(EXTRA_PARAM_SEND_PARCELABLE_ARRAYLIST_RESULT, r);
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);

            }
        }
    }
}
