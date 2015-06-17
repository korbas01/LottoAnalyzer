package yimscompany.lottoanalyzer.UIComponent;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by shyim on 15-05-08.
 */
public class DisplayMetricsHelper {
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    public static float getDPScreenWidth(Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }


}
