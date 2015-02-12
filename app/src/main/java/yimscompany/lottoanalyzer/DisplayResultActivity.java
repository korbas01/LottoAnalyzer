package yimscompany.lottoanalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class DisplayResultActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            String strResult = extra.getString(MainActivity.ResponseReceiver.EXTRA_PARAM_SEND_RESULT);
            TextView result = (TextView) findViewById(R.id.TxtBoxResult);
            result.setText(strResult);
        }

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

}
