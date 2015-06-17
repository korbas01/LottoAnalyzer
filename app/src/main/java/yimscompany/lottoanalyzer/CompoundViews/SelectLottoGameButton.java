package yimscompany.lottoanalyzer.CompoundViews;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import yimscompany.lottoanalyzer.R;
import yimscompany.lottoanalyzer.UIComponent.DisplayMetricsHelper;

/**
 * Created by shyim on 15-05-07.
 */
public class SelectLottoGameButton extends LinearLayout {
    public SelectLottoGameButton(Context context) {
        this(context, null);
    }

    public SelectLottoGameButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SelectLottoGameButton, 0, 0);
        String text1 = a.getString(R.styleable.SelectLottoGameButton_text1);
        String text2 = a.getString(R.styleable.SelectLottoGameButton_text2);
        int txtColor1 = a.getColor(R.styleable.SelectLottoGameButton_txtColor1, android.R.color.black);
        int txtColor2 = a.getColor(R.styleable.SelectLottoGameButton_txtColor2,android.R.color.black);
        int borderColor = a.getColor(R.styleable.SelectLottoGameButton_borderColor,android.R.color.black);
        a.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        //set custom view background styles
        GradientDrawable d = new GradientDrawable ();
        d.setColor( ((ColorDrawable) getBackground()).getColor() );
        d.setStroke(DisplayMetricsHelper.dpToPx(2),borderColor);
        d.setCornerRadius((float)DisplayMetricsHelper.dpToPx(3));
        setBackground(d);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compound_select_game_button, this, true);

        TextView textview1 = (TextView) getChildAt(0);
        TextView textview2 = (TextView) getChildAt(1);

        textview1.setText(text1);
        textview1.setTextColor(txtColor1);
        textview2.setText(text2);
        textview2.setTextColor(txtColor2);
    }





}
