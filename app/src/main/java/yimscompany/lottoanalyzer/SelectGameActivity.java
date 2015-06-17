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
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.ads.AdView;

import yimscompany.lottoanalyzer.BusinessObjects.LottoGame;
import yimscompany.lottoanalyzer.Components.LottoAnalyzerDialogFragment;
import yimscompany.lottoanalyzer.Fragments.AlertDialogFragment;
import yimscompany.lottoanalyzer.IntentServices.LottoAnalyzerIntent;
import yimscompany.lottoanalyzer.IntentServices.OLGParsingPageIntent;
import yimscompany.lottoanalyzer.UIComponent.LottoUIHelper;

public class SelectGameActivity extends Activity {
    private LottoGame mSelectedLottoGame;
    private ResponseReceiver mIntentReceiver;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);
        getActionBar().setTitle("Select Game");
        LottoUIHelper.ActionBarTitleCenter(this);
        LottoUIHelper.InitAdView((AdView) findViewById(R.id.adView));
        registerButtons();

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_select_game, menu);
//        return true;
//    }

    /* check connectivity whether it's able to download html content or not.
    * */
    public boolean checkConnectivity() {
        //TODO: improve check connectivity, it needs to handle a case where 3g data is not available.
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
//
//        if( ! isConnected  ) {
//            Bundle dialogArgs = new Bundle();
//            dialogArgs.putString(LottoAnalyzerDialogFragment.DIALOG_MSG,"It's not connected to internet. Please try again later.");
//            dialogArgs.putString(LottoAnalyzerDialogFragment.DIALOG_POS_BTN_LABEL, "OK") ;
//            showAlert(dialogArgs);
//            return false;
//        }
//        return true;

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
        newFragment.getDialog().setCancelable(false);
    }

    private void showProgressSpin() {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setMessage("Verifying data....");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void registerIntentReceiver() {
        IntentFilter pastWinNumsFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_PAST_WIN_NUMS);
        IntentFilter errHandlerFilter = new IntentFilter(ResponseReceiver.ACTION_RESP_INVALID_CONN_ERR);

        pastWinNumsFilter.addCategory(Intent.CATEGORY_DEFAULT);
        errHandlerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mIntentReceiver = new ResponseReceiver();
        registerReceiver(mIntentReceiver, pastWinNumsFilter);
        registerReceiver(mIntentReceiver, errHandlerFilter);
    }


    private void registerButtons(){
        findViewById(R.id.BtnLotto649).setOnClickListener(onSelectGameClickListener);
        findViewById(R.id.BtnLottoMax).setOnClickListener(onSelectGameClickListener);
    }


    /*it needs to generate a query in order to avoid using their forms,
     * format would be http://www.olg.ca/lotteries/viewPastNumbers.do?command=submit&gameID=1&selectedMonthYear=102013&day=0&x=31&y=12
     * gameID: a game id, lotto649 is 1,
     * selectedMonthYear: it would be 4 digits,  first two numbers indicate month starting with 00 (e.g 00 = Jan. 11 = Dec.)
     * 					  last two numbers, year. OLG has the last one year record
     * day: if it's 0, it shows all past winning numbers in a month respectively
     * x,y: some random numbers I couldn't figure out what those numbers are for
     * @param: gameID: number represents game e.g.) 1 = lotto 649
     *
     * **changed(Feb. 15, 2015): I changed logic, it now passes a Game object which contains the basic info. for
     * paarsing and add data into the db.
     */
    private void parsingPastWinningNumbers() {
        OLGParsingPageIntent.startActionPastWinNums(this, mSelectedLottoGame);
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


    final OnClickListener onSelectGameClickListener = new OnClickListener() {
        @Override
        public void onClick(View v){
            Resources res = getResources();
            switch(v.getId()){
                case R.id.BtnLotto649:
                    mSelectedLottoGame = new LottoGame(getString(R.string.game_lotto649_on_ca),
                            getString(R.string.game_province_ontario),
                            getString(R.string.game_country_canada),
                            res.getInteger(R.integer.OLG_LOTTO649_GAME_ID),
                            res.getInteger(R.integer.OLG_LOTTO649_PICK_NUMS),
                            true, true,
                            res.getInteger(R.integer.OLG_LOTTO649_MIN_RANGE),
                            res.getInteger(R.integer.OLG_LOTTO649_MAX_RANGE) );
                    break;
                case R.id.BtnLottoMax:
                    mSelectedLottoGame = new LottoGame(getString(R.string.game_lottomax_on_ca),
                            getString(R.string.game_province_ontario),
                            getString(R.string.game_country_canada),
                            res.getInteger(R.integer.OLG_LOTTOMAX_GAME_ID),
                            res.getInteger(R.integer.OLG_LOTTOMAX_PICK_NUMS),
                            true, true,
                            res.getInteger(R.integer.OLG_LOTTOMAX_MIN_RANGE),
                            res.getInteger(R.integer.OLG_LOTTOMAX_MAX_RANGE));
                    break;
                default:
                    break;
            }
            UpdatingPastWinningNums();
        }
    };



    //sub class for receiving IntentServices
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP_PAST_WIN_NUMS =
                "yimscompany.lottoanalyzer.intent.action.resp.PAST_WIN_NUMS_MESSAGE_PROCESSED";
        public static final String ACTION_RESP_INVALID_CONN_ERR = "yimscompany.lottoanalyzer.intent.action.resp.ACTION_RESP_INVALID_CONN_ERR";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_RESP_PAST_WIN_NUMS) && !isMyServiceRunning(LottoAnalyzerIntent.class)) {
                mProgressDialog.dismiss();

                Intent i = new Intent(SelectGameActivity.this, MainActivity.class);
                i.putExtra("SelectedGame", mSelectedLottoGame);
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_down);

            }else if(action.equals(ACTION_RESP_INVALID_CONN_ERR)){
                mProgressDialog.dismiss();
                AlertDialogFragment newFragment = AlertDialogFragment.newInstance(
                        R.string.dialog_title_warning,R.string.err_no_connection);
                newFragment.show(getFragmentManager(), "dialog");
            }

        }
    }

    /**
     * init. / return a Game object.
     * @return null if a game name not in the pre-defined list
     *
     */


//    public LottoGame getSelectedGame(){
//        Spinner spinner = (Spinner) findViewById(R.id.selectGameSpinner);
//        String selectedGame = spinner.getSelectedItem().toString();
//        Resources res = getResources();
//        if(selectedGame.equals(getString(R.string.game_lotto649_on_ca))){
//            LottoGame aLottoGame = new LottoGame(getString(R.string.game_lotto649_on_ca),
//                    getString(R.string.game_province_ontario),
//                    getString(R.string.game_country_canada),
//                    res.getInteger(R.integer.OLG_LOTTO649_GAME_ID),
//                    res.getInteger(R.integer.OLG_LOTTO649_PICK_NUMS),
//                    true, true,
//                    res.getInteger(R.integer.OLG_LOTTO649_MIN_RANGE),
//                    res.getInteger(R.integer.OLG_LOTTO649_MAX_RANGE) );
//            return aLottoGame;
//        }else if(selectedGame.equals(getString(R.string.game_lottomax_on_ca))){
//            LottoGame aLottoGame = new LottoGame(getString(R.string.game_lottomax_on_ca),
//                    getString(R.string.game_province_ontario),
//                    getString(R.string.game_country_canada),
//                    res.getInteger(R.integer.OLG_LOTTOMAX_GAME_ID),
//                    res.getInteger(R.integer.OLG_LOTTOMAX_PICK_NUMS),
//                    true, true,
//                    res.getInteger(R.integer.OLG_LOTTOMAX_MIN_RANGE),
//                    res.getInteger(R.integer.OLG_LOTTOMAX_MAX_RANGE));
//            return aLottoGame;
//        }
//        return null;
//    }

    private void UpdatingPastWinningNums() {
        if(mIntentReceiver == null) {
            registerIntentReceiver();
        }
        try{
            if(checkConnectivity()) { //it's able to connect to the internet
                showProgressSpin();
                parsingPastWinningNumbers();
            }else{
                AlertDialogFragment newFragment = AlertDialogFragment.newInstance(
                        R.string.dialog_title_warning,R.string.err_no_connection);
                newFragment.show(getFragmentManager(), "dialog");

            }
        }catch(Exception e) {
            AlertDialogFragment newFragment = AlertDialogFragment.newInstance(
                    R.string.dialog_title_warning,R.string.err_no_connection);
            newFragment.show(getFragmentManager(), "dialog");

        }
    }
}
