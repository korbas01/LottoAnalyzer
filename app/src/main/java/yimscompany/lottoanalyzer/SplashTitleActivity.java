package yimscompany.lottoanalyzer;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import yimscompany.lottoanalyzer.UIComponent.LottoUIHelper;

public class SplashTitleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_title);
        initAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    private void initAnimation() {

        LinearLayout layout = (LinearLayout) findViewById(R.id.splashLinearLayout);

        //imageView should be added in order into the arraylist
        ArrayList<ImageView> imgViewContainer = new ArrayList<>();
        imgViewContainer.add(LottoUIHelper.DisplaySplashImage(getApplicationContext(), R.drawable.img_title_next, 250,100));
        imgViewContainer.add(LottoUIHelper.DisplaySplashImage(getApplicationContext(), R.drawable.img_title_lottery, 250,100));
        imgViewContainer.add(LottoUIHelper.DisplaySplashImage(getApplicationContext(), R.drawable.img_title_winning, 250,100));
        imgViewContainer.add(LottoUIHelper.DisplaySplashImage(getApplicationContext(), R.drawable.img_title_number, 250,100));

        int initOffset = 3000;
        int imgCounter = 1;
        for(int i = 0 ; i < imgViewContainer.size(); i ++) {
            Animation bounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.img_bounce);
            bounce.setStartOffset(initOffset - (imgCounter * 700));
            imgCounter ++;
            layout.addView(imgViewContainer.get(i));
            imgViewContainer.get(i).startAnimation(bounce);
            if(i == 0) {
                bounce.setAnimationListener(MainAnimationListener);
            }
        }
    }

    final Animation.AnimationListener MainAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Intent i = new Intent(SplashTitleActivity.this, SelectGameActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    final Animator.AnimatorListener MainAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Intent i = new Intent(SplashTitleActivity.this, SelectGameActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

}
