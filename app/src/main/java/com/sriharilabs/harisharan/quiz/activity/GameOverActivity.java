package com.sriharilabs.harisharan.quiz.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.sriharilabs.harisharan.quiz.R;
import com.sriharilabs.harisharan.quiz.databinding.ActivityGameOverBinding;
import com.sriharilabs.harisharan.quiz.utill.Utility;

public class GameOverActivity extends AppCompatActivity {

    private ActivityGameOverBinding mBinding;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_game_over);
        mBinding.setActivity(this);
        getDataFromBundle();
        if (Utility.isNetworkConnected(GameOverActivity.this)) {
            interstitialAd();
        }
    }

    /**
     * Get data from bundle
     */
    private void getDataFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBinding.textViewScore.setText(bundle.getString("point"));
        }
    }

    /**
     * Initialize interstitial ad
     */
    private void interstitialAd() {
        //MobileAds.initialize(this, getString(R.string.ad_unit));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_unit_id_bottom));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openInterstitialAd();
            }
        }, 2000);
    }

    /**
     * Open interstitial ad
     */
    private void openInterstitialAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Utility.showToast(GameOverActivity.this, "The interstitial wasn't loaded yet.");
        }
    }
}
