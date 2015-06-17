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
//    private ResponseReceiver _intentReceiver;
//    private ProgressDialog _progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_title);
        initAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerIntentReceiver();
        //checkConnectivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(_intentReceiver);
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




//        ArrayList<ImageView> imgContainer = new ArrayList<>();
//
//        imgContainer.add( (ImageView) findViewById(R.id.imageView1));
//        imgContainer.add( (ImageView) findViewById(R.id.imageView2));
//        imgContainer.add( (ImageView) findViewById(R.id.imageView3));
//        imgContainer.add( (ImageView) findViewById(R.id.imageView4));
//
////        ImageView image2 = (ImageView) findViewById(R.id.imageView2);
////        ImageView image3 = (ImageView) findViewById(R.id.imageView3);
////        ImageView image4 = (ImageView) findViewById(R.id.imageView4);
////
//        Animation bounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.img_bounce);
//        int counter = 1;
//        for(ImageView img : imgContainer) {
//            bounce.setStartOffset(counter * 200);
//            counter ++;
//            img.startAnimation(bounce);
//        }
//
////
////        ObjectAnimator animation1 = ObjectAnimator.ofFloat(image1, "translationY",0, 200.0f);
////        ObjectAnimator animation2 = ObjectAnimator.ofFloat(image2, "translationX",0, 200.0f);
////        ObjectAnimator animation3 = ObjectAnimator.ofFloat(image3, "translationX",0, -200.0f);
////        ObjectAnimator animation4 = ObjectAnimator.ofFloat(image4, "translationY",0, -250.0f);
////
////        animation1.setDuration(2500);
////        animation2.setDuration(2500);
////        animation3.setDuration(2500);
////        animation4.setDuration(2500);
////
////        AnimatorSet animatorSet = new AnimatorSet();
////        animatorSet.playTogether(animation1, animation2, animation3, animation4);
////        animatorSet.addListener(MainAnimatorListener);
////        animatorSet.start();


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
//            View view = findViewById(android.R.id.content);
//            Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
//            mLoadAnimation.setDuration(2000);
//            view.startAnimation(mLoadAnimation);
            Intent i = new Intent(SplashTitleActivity.this, SelectGameActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//
//            }, 1000);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };



//    //sub class for receiving IntentServices
//    public class ResponseReceiver extends BroadcastReceiver {
//        public static final String ACTION_RESP_PAST_WIN_NUMS =
//                "yimscompany.lottoanalyzer.intent.action.resp.PAST_WIN_NUMS_MESSAGE_PROCESSED";
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (action.equals(ACTION_RESP_PAST_WIN_NUMS) && !isMyServiceRunning(LottoAnalyzerIntent.class)) {
//                _progressDialog.dismiss();
//                Intent i = new Intent(SplashTitleActivity.this, MainActivity.class);
//                startActivity(i);
//            }
//
//
//        }
//    }

}
