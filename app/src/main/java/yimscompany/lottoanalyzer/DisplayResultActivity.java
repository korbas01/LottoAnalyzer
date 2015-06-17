package yimscompany.lottoanalyzer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;

import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;
import yimscompany.lottoanalyzer.UIComponent.DisplayMetricsHelper;
import yimscompany.lottoanalyzer.UIComponent.LottoUIHelper;


public class DisplayResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

        setContentView(R.layout.activity_display_result);

        LottoUIHelper.ActionBarTitleCenter(this);
        LottoUIHelper.InitAdView((AdView) findViewById(R.id.adView));

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            //String strResult = extra.getString(MainActivity.ResponseReceiver.EXTRA_PARAM_SEND_RESULT);
            //TextView result = (TextView) findViewById(R.id.TxtBoxResult);
            //result.setText(strResult);

            //getIntent().getSerializableExtra()
            if(getIntent().getAction().equals(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_EXPECTED_WINNING_NUMS))
            {
                ArrayList<LottoRecord> result = extra.getParcelableArrayList(MainActivity.ResponseReceiver.EXTRA_PARAM_SEND_PARCELABLE_ARRAYLIST_RESULT);
                for(int i =0 ; i < result.size(); i ++)
                {
                    renderLottoNumbers(result.get(i).getWinningNums(), i);
                }
                getActionBar().setTitle(getString(R.string.main_list_item_0));
            }else if(getIntent().getAction().equals(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_ALL_NUMS))
            {
                ArrayList<Integer> result = extra.getIntegerArrayList(MainActivity.ResponseReceiver.EXTRA_PARAM_SEND_INTEGER_ARRAYLIST_RESULT);
                for(int i =0; i < (int) Math.ceil(result.size() / 6.0 ); i ++) {
                    int start = i * 6;
                    int end = result.size() < (i+1) * 6 ? result.size() : (i+1)*6 ;
                    renderLottoNumbers(new ArrayList<> (result.subList(start,end)),i);
                }

                getActionBar().setTitle(getString(R.string.main_list_item_1));

            }else if(getIntent().getAction().equals(MainActivity.ResponseReceiver.ACTION_RESP_LOTTO_ANALYZER_MY_HISTORY))
            {
                ArrayList<LottoRecord> result = extra.getParcelableArrayList(MainActivity.ResponseReceiver.EXTRA_PARAM_SEND_PARCELABLE_ARRAYLIST_RESULT);
                for(int i =0 ; i < result.size(); i ++)
                {
                    renderLottoNumbers(result.get(i).getWinningNums(), i);
                }
                getActionBar().setTitle(getString(R.string.main_list_item_2));

            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        LottoUIHelper.InitAdView((AdView) findViewById(R.id.adView));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderLottoNumbers(ArrayList<Integer> result, int... index) {
        LinearLayout layoutFrame = (LinearLayout) findViewById(R.id.displayLayout);
        LinearLayout layout = LottoUIHelper.CreateDisplayNumLinearLayout(getApplicationContext());


        //@todo: how do I get layout screen width in DP?
        //@todo: how do I create LinearLayout programatically?
        //@todo: how do I know whether buttons are overflow or not?
        //@todo: how do I send object arraylist via Intent?
        Collections.sort(result);

        float btnRadius = (DisplayMetricsHelper.getDPScreenWidth(getApplicationContext()) - 75) / Math.max(6,result.size());
        int counter = 1;
        int offset = index.length > 0? (index[0] + 1) : 1;

        for (Integer aNum : result) {
            if(aNum != null) {
                Button aBtn;
                if(index.length >0 && index[0] % 2 == 1) {
                    aBtn = LottoUIHelper.DisplayLottoNumber(getApplicationContext(), String.valueOf(aNum.intValue()),(int) btnRadius, R.drawable.lotto_number_b, Color.parseColor("#9D9D9D"));
                }else{
                    aBtn = LottoUIHelper.DisplayLottoNumber(getApplicationContext(), String.valueOf(aNum.intValue()),(int) btnRadius, R.drawable.lotto_number_a, Color.parseColor("#000000"));
                }
                Animation bounce = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.btn_bounce);

                bounce.setStartOffset(counter * offset * 200);
                aBtn.startAnimation(bounce);
                layout.addView(aBtn);
                counter++;
            }
        }
        layoutFrame.addView(layout);

    }

}
