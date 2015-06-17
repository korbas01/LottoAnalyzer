package yimscompany.lottoanalyzer.UIComponent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import yimscompany.lottoanalyzer.R;

/**
 * Created by shyim on 15-05-04.
 */
public class LottoSeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private int mMax = 15;
    private int mMin = 1;
    private int mInterval = 1;

    private int mOldValue = 5;
    private Context mContext;
    private TextView mMonitorBox;


    public LottoSeekBarPreference(Context context) {
        super(context);
        mContext = context;
    }

    public LottoSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public LottoSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    @Override
    protected View onCreateView(ViewGroup parent) {

        LinearLayout layout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.LEFT;
        params1.weight  = 1.0f;


        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //params2.gravity = Gravity.RIGHT;
        params2.gravity = Gravity.LEFT;



        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.gravity = Gravity.CENTER;

        layout.setPadding(15, 5, 10, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView view = new TextView(getContext());
        view.setText(getTitle());

        view.setTextAppearance(mContext, android.R.style.TextAppearance_DialogWindowTitle);
        view.setGravity(Gravity.LEFT);
        view.setLayoutParams(params1);

        SeekBar bar = new SeekBar(getContext());
        bar.setMax(mMax);

        bar.setProgress(mOldValue);
        bar.setLayoutParams(params2);
        bar.setOnSeekBarChangeListener(this);

        this.mMonitorBox = new TextView(getContext());
        this.mMonitorBox.setTextSize(12);
        this.mMonitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
        this.mMonitorBox.setLayoutParams(params3);
        this.mMonitorBox.setPadding(2, 5, 0, 0);
        this.mMonitorBox.setText(bar.getProgress() + "");


        layout.addView(view);
        layout.addView(bar);
        layout.addView(this.mMonitorBox);
        layout.setId(android.R.id.widget_frame);


        return layout;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress =validateValue(Math.round(((float)progress)/ mInterval)* mInterval);

        if(!callChangeListener(progress)){
            seekBar.setProgress((int)this.mOldValue);
            return;
        }

        seekBar.setProgress(progress);
        this.mOldValue = progress;
        this.mMonitorBox.setText(progress + "");
        updatePreference(progress);

        notifyChanged();
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index,50);
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if(restorePersistedValue) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            mOldValue = pref.getInt(getContext().getResources().getString(R.string.pref_num_games), getContext().getResources().getInteger(R.integer.PREF_DEFAULT_NUM_GAMES));
        }else{
            mOldValue = (Integer) defaultValue;
        }

    }

    private int validateValue(int value){

        if(value > mMax)
            value = mMax;
        else if(value <= mMin)
            value = mMin;
        else if(value % mInterval != 0)
            value = Math.round(((float)value)/ mInterval)* mInterval;
        return value;
    }

    private void updatePreference(int newValue){
        SharedPreferences.Editor editor =  getEditor();
        editor.putInt(getContext().getString(R.string.pref_num_games), newValue);
        editor.commit();
    }
}
