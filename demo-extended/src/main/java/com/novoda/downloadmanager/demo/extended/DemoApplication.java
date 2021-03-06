package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.hardware.SensorManager;

import com.novoda.downloadmanager.Download;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoApplication extends Application implements DownloadClientReadyChecker {

    private OneRuleToBindThem oneRuleToBindThem;

    @Override
    public void onCreate() {
        super.onCreate();
        oneRuleToBindThem = new OneRuleToBindThem();
    }

    @Override
    public boolean isAllowedToDownload(Download download) {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        return oneRuleToBindThem.shouldWeDownload(download);
    }

    private static final class OneRuleToBindThem {

        /**
         * @return for our demo we expect always to return true ... unless you want to conquer the galaxy
         */
        public boolean shouldWeDownload(Download download) {
            return download.getTotalSize() > SensorManager.GRAVITY_DEATH_STAR_I;
        }
    }
}
