package net.jjc1138.android.twitter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class Fetcher extends Service {
	final static String LOG_TAG = "nanoTweeter";

	private SharedPreferences prefs;

	private FetcherThread fetcherThread;
	private Handler handler;
	private PowerManager.WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = getSharedPreferences(TwitterConfig.PREFS, 0);
		handler = new Handler();
		wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
			.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
		wakeLock.setReferenceCounted(false);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		wakeLock.acquire();
		Log.d(LOG_TAG, "Service started.");
		
		((AlarmManager) getSystemService(ALARM_SERVICE)).set(
			AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime() +
				(prefs.getInt("interval",
					TwitterConfig.INTERVALS[
						TwitterConfig.DEFAULT_INTERVAL_INDEX]) * 60 * 1000),
			PendingIntent.getService(this, 0,
				new Intent(this, Fetcher.class), 0));
		Log.d(LOG_TAG, "Scheduled next run.");
		
		if (fetcherThread != null && fetcherThread.inProgress()) {
			Log.d(LOG_TAG, "Refusing to start another fetcher " +
				"because one is in progress.");
			return;
		} else {
			fetcherThread = new FetcherThread();
			fetcherThread.start();
		}
	}

	private class FetcherThread extends Thread {
		// stopIfIdle() below needs to know if this thread is still doing work.
		// You might think that we could use Thread.isAlive() to find that out,
		// but that wouldn't work because stopIfIdle() is queued to be called
		// from this thread, and it would be possible for stopIfIdle() to start
		// before this thread had actually died. For that reason we use this
		// variable so that the thread can indicate its impending death before
		// queuing the call to stopIfIdle().
		private volatile boolean inProgress = true;
	
		public boolean inProgress() {
			return inProgress;
		}
	
		@Override
		public void run() {
			// TODO stuff!
			
			inProgress = false;
			handler.post(new Runnable() {
				@Override
				public void run() {
					stopIfIdle();
				}
			});
		}
	}

	private void stopIfIdle() {
		if (fetcherThread != null && fetcherThread.inProgress()) {
			// This could happen if this call to stopIfIdle() gets queued after
			// another starting of the service (which would fire up another
			// thread).
			Log.d(LOG_TAG, "Not stopping service because thread is running.");
			return;
		}
		wakeLock.release();
		Log.d(LOG_TAG, "Stopping service.");
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(LOG_TAG, "Service destroyed.");
	}
}
