package it.eduman.mobileHome2;

import android.os.Handler;
import android.os.Looper;


public class UIUpdater {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mStatusChecker;
    private int updateInterval = 2000;

    public UIUpdater(final Runnable uiUpdater, int interval) {
        this.updateInterval = interval;
        mStatusChecker = new Runnable() {
            @Override
            public void run() {
                // Run the passed runnable
                uiUpdater.run();
                // Re-run it after the update interval
                mHandler.postDelayed(this, updateInterval);
            }
        };
    }

    public synchronized void startUpdates(){
        mStatusChecker.run();
    }

    public synchronized void stopUpdates(){
        mHandler.removeCallbacks(mStatusChecker);
    }
}
