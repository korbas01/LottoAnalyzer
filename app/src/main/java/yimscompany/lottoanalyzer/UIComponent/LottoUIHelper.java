package yimscompany.lottoanalyzer.UIComponent;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by shyim on 15-05-09.
 */
public class LottoUIHelper {
    /**
     *
     * @param c: context
     * @param resID: resource id refers to the image
     * @param imgWidth: width in DP of imageview
     * @param imgHeight: height in DP of imageview
     * @return
     */
    public static ImageView DisplaySplashImage(Context c, int resID, int imgWidth, int imgHeight ) {
        ImageView imgView = new ImageButton(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( DisplayMetricsHelper.dpToPx(imgWidth), DisplayMetricsHelper.dpToPx(imgHeight));
        imgView.setLayoutParams(layoutParams);
        imgView.setBackgroundResource(resID);
        imgView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imgView;
    }
    /* generate custom styled button */
    public static Button DisplayLottoNumber (Context c, String aNum, int radiusDp, int drawableID, int color) {
        Button aBtn = new Button(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( DisplayMetricsHelper.dpToPx(radiusDp), DisplayMetricsHelper.dpToPx(radiusDp));
        layoutParams.setMargins(0,0, DisplayMetricsHelper.dpToPx(5),0);
        aBtn.setLayoutParams(layoutParams);

        aBtn.setText(aNum);
        aBtn.setTextSize(12);
        //adjust text size
//        if(radiusDp <= 40) {
//            aBtn.setTextSize(Text);
//        }
//Color.parseColor("#000000")
        aBtn.setTextColor(color);
        aBtn.setBackground(c.getResources().getDrawable(drawableID));
        return aBtn;
    }


    public static LinearLayout CreateDisplayNumLinearLayout (Context c) {
        LinearLayout ll = new LinearLayout(c);
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT );

        layoutParams.setMargins(0, DisplayMetricsHelper.dpToPx(5), 0, DisplayMetricsHelper.dpToPx(5));
        ll.setLayoutParams(layoutParams);
        return ll;
    }

    public static void InitAdView(AdView adView) {
        if(adView != null) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(Settings.Secure.ANDROID_ID).build();
            adView.loadAd(adRequest);
        }
    }

    public static void ActionBarTitleCenter(Activity activity) {
        int titleId = activity.getResources().getIdentifier("action_bar_title", "id", "android");
        if(titleId > 0) {
            TextView titleTextView = (TextView) activity.findViewById(titleId);

            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();

            // Fetch layout parameters of titleTextView (LinearLayout.LayoutParams : Info from HierarchyViewer)
            LinearLayout.LayoutParams txvPars = (LinearLayout.LayoutParams) titleTextView.getLayoutParams();
            txvPars.gravity = Gravity.CENTER_HORIZONTAL;
            txvPars.width = metrics.widthPixels;
            titleTextView.setLayoutParams(txvPars);

            titleTextView.setGravity(Gravity.CENTER);
        }
    }

}
