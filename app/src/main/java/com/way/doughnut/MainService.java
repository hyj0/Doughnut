package com.way.doughnut;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import com.way.arclayout.ArcLayout;
import com.way.doughnut.activity.SettingsActivity;
import com.way.doughnut.fragment.SettingsFragment;
import com.way.doughnut.util.*;

import java.io.*;
import java.util.ArrayList;

public class MainService extends Service
        implements OnClickListener, DoughtnutImageView.Callbacks, OnSharedPreferenceChangeListener {
    private static final String TAG = "MainService";
    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 9083150;
    private static final String SHOW_FLOAT_VIEW = "show_float_view";

    /**
     * Vibrator
     */
    private Vibrator mVibrator;
    private WindowManager mWindowManager;
    /**
     * DisplayMetrics
     */
    private DisplayMetrics mMetrics;
    private FloatingView mFloatingView;
    private DoughtnutImageView mDoughnutImageView;
    private SharedPreferences mPreferences;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Dialog mFloatMenuDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        addFloatView();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
//        String daemserv = "daemserv";
        try {
            PlayState.runDaemServer(getApplicationContext(),
                    getResources().getAssets().open("doroot"),
                    getResources().getAssets().open("httpcmdserv"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFloatView() {
        if (!mPreferences.getBoolean(SettingsFragment.KEY_FLOAT_VIEW_TOGGLE, true))
            return;
        if (mFloatingView != null)
            return;
        mFloatingView = new FloatingView(this);
        mDoughnutImageView = new DoughtnutImageView(this, this);
        int theme = mPreferences.getInt(SettingsFragment.KEY_FLOAT_VIEW_THEME, 0);
        mDoughnutImageView.setImageResource(SettingsFragment.THEME_ICON_RES[theme]);
        mDoughnutImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVibrator.vibrate(mPreferences.getInt(SettingsFragment.KEY_VIBRATOR_LEVEL,
                        SettingsFragment.DEFAULT_VIBRATE_LEVEL));
                showDialog(v);
            }
        });
        mFloatingView.addView(mDoughnutImageView);
        final DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mFloatingView.setOverMargin((int) (8 * metrics.density));
        mFloatingView.addToWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.d(TAG, "onStartCommand intent = " + intent);
        if (intent != null) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SHOW_FLOAT_VIEW)) {
                if (mFloatingView != null) {
                    mFloatingView.scaleToShow(true);
                }
                stopForeground(true);
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            mFloatingView.removeFromWindow();
            mFloatingView = null;
        }
        mWindowManager = null;
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * 通知
     */
    private Notification createNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.theme_captain);
        builder.setContentTitle(getString(R.string.click_for_restore_float_view_title));
        builder.setContentText(getString(R.string.click_for_restore_float_view));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        // PendingIntent作成
        final Intent notifyIntent = new Intent(this, MainService.class);
        notifyIntent.setAction(SHOW_FLOAT_VIEW);
        PendingIntent notifyPendingIntent = PendingIntent.getService(this, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainService.this, "get permission... wait a monent ", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(MainService.this, "read server err ...", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(MainService.this, "start play ...", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onSwipeToDirection(int direction) {

        switch (direction) {
            case DoughtnutImageView.SWIPE_TO_TOP:
                //                        mVibrator.vibrate(
//                                mPreferences.getInt(SettingsFragment.KEY_VIBRATOR_LEVEL, SettingsFragment.DEFAULT_VIBRATE_LEVEL));
//                        if (mFloatingView != null) {
//                            mFloatingView.scaleToDimiss(true);
//                        }
//                        startForeground(NOTIFICATION_ID, createNotification());

                int playState = PlayState.getState();
                if (playState > 0) {
                    Toast.makeText(MainService.this, "正在计算...", Toast.LENGTH_SHORT).show();
                    break;
                }

//                String daemserv = "daemserv";
                try {
                    PlayState.runDaemServer(getApplicationContext(),
                            getResources().getAssets().open("doroot"),
                            getResources().getAssets().open("httpcmdserv"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                PlayState.setState(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyLog.d(TAG, "cap Screen");
                        new File(Environment.getExternalStorageDirectory()+"/mmm.png").delete();
                        if (!ScreenShotUtils.capScreen1()) {
                            Message msg = new Message();
                            msg.what = 1;
                            handler.sendMessage(msg);
                            PlayState.setState(0);
                            return;
                        }

                        try {
                            MyLog.d(TAG, "upload image");
                            HttpPostfile httpPostfile = new HttpPostfile();
                            String serverHost = "192.168.3.112:8081";
                            File server = new File(Environment.getExternalStorageDirectory() + "/popstar_autoplay_url.txt");
                            if (server.exists()) {
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(server));
                                String line = bufferedReader.readLine();
                                line = line.trim();
                                if (line.length() > 0) {
                                    serverHost = line;
                                }
                            }
                            String res = httpPostfile.post(String.format("http://%s/play_game", serverHost),
                                    Environment.getExternalStorageDirectory()+"/mmm.png");
                            if (res.length() == 0) {
                                Message msg = new Message();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }

                            MyLog.d(TAG, "start click");

                            Message msg = new Message();
                            msg.what = 3;
                            handler.sendMessage(msg);

                            ArrayList<Pair> stepLst = httpPostfile.parseSteps(res);
                            InputMonit inputMonit = new InputMonit();
                            for (Pair xy :
                                    stepLst) {
                                int x = (int) xy.first;
                                int y = (int) xy.second;
                                //需要多次点击
                                inputMonit.click(x, y);
                                Thread.sleep(100);
                                inputMonit.click(x, y);
                                inputMonit.click(x, y);
                                inputMonit.click1(x, y);

                                Thread.sleep(1000);
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //end play
                        PlayState.setState(0);
                    }
                }).start();

                break;
            case DoughtnutImageView.SWIPE_TO_BOTTOM:
                mVibrator.vibrate(
                        mPreferences.getInt(SettingsFragment.KEY_VIBRATOR_LEVEL, SettingsFragment.DEFAULT_VIBRATE_LEVEL));
                Utils.execHomeButton(MainService.this);
                break;
            case DoughtnutImageView.SWIPE_TO_LEFT:
                mVibrator.vibrate(
                        mPreferences.getInt(SettingsFragment.KEY_VIBRATOR_LEVEL, SettingsFragment.DEFAULT_VIBRATE_LEVEL));
                Utils.execBackButton(MainService.this);
                break;
            case DoughtnutImageView.SWIPE_TO_RIGHT:
                mVibrator.vibrate(
                        mPreferences.getInt(SettingsFragment.KEY_VIBRATOR_LEVEL, SettingsFragment.DEFAULT_VIBRATE_LEVEL));
                Utils.launchRecentApps(MainService.this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDebug() {
        // do nothing

    }

    @Override
    public void onClick(View v) {
        if (mFloatMenuDialog != null)
            mFloatMenuDialog.cancel();
        switch (v.getId()) {
            case R.id.menu_center:
                Intent i = new Intent(this, SettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(i);
                break;
            case R.id.menu_one:
                try {
                    Intent screenRecordIntent = new Intent(Intent.ACTION_MAIN, null);
                    screenRecordIntent.setComponent(
                            new ComponentName("com.way.telecine", "com.way.screenshot.TakeScreenshotActivity"));
                    screenRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(screenRecordIntent);
                } catch (ActivityNotFoundException e) {
                }

                break;
            case R.id.menu_two:
                try {
                    Intent screenRecordIntent = new Intent(Intent.ACTION_MAIN, null);
                    screenRecordIntent.setComponent(
                            new ComponentName("com.way.telecine", "com.way.telecine.TelecineShortcutLaunchActivity"));
                    screenRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(screenRecordIntent);
                } catch (ActivityNotFoundException e) {
                }

                break;
            case R.id.menu_three:
                Utils.openPowerMenu(this);
                break;
            case R.id.menu_four:
                Utils.execLockScreen(this);
                break;
            case R.id.menu_five:
                Utils.openNotifications(this);
                break;
            default:
                break;
        }
    }

    private void showDialog(View v) {
        if (mFloatingView != null) {
            mFloatingView.scaleToDimiss(true);
        }
        if (mFloatMenuDialog != null && !mFloatMenuDialog.isShowing()) {
            mFloatMenuDialog.show();
            return;
        }
        View rootView = initDialogView();

        mFloatMenuDialog = new Dialog(this, R.style.Theme_Dialog);
        mFloatMenuDialog.setContentView(rootView);
        mFloatMenuDialog.setCanceledOnTouchOutside(true);
        mFloatMenuDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        mFloatMenuDialog.getWindow().setWindowAnimations(R.style.dialog_window_anim);
        mFloatMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mFloatingView != null) {
                    mFloatingView.scaleToShow(true);
                }
            }
        });
        mFloatMenuDialog.show();

    }

    private View initDialogView() {
        View rootView = LayoutInflater.from(this).inflate(R.layout.float_dialog_menu, null);
        ArcLayout arcLayout = (ArcLayout) rootView.findViewById(R.id.arc_layout);
        rootView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mFloatMenuDialog != null && mFloatMenuDialog.isShowing()) {
                        mFloatMenuDialog.cancel();
                        return true;
                    }
                }
                return false;
            }
        });
        ImageButton centerItem = (ImageButton) rootView.findViewById(R.id.menu_center);
        centerItem.setColorFilter(Color.WHITE);

        centerItem.setOnClickListener(this);
        for (int i = 0, size = arcLayout.getChildCount(); i < size; i++) {
            ImageButton button = (ImageButton) arcLayout.getChildAt(i);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && i < 3) {
                button.setVisibility(View.GONE);
                continue;
            }
            button.setColorFilter(Color.WHITE);
            button.setOnClickListener(this);
        }
        return rootView;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mFloatingView == null)
            return;
        switch (key) {
            case SettingsFragment.KEY_AUTO_SIDE_MODEL:
                mFloatingView.setAllowMoveEdge(sharedPreferences.getBoolean(key, false));
                break;
            case SettingsFragment.KEY_FLOAT_VIEW_THEME:
                int theme = sharedPreferences.getInt(SettingsFragment.KEY_FLOAT_VIEW_THEME, 0);
                if (mDoughnutImageView != null)
                    mDoughnutImageView.setImageResource(SettingsFragment.THEME_ICON_RES[theme]);
                break;
            case SettingsFragment.KEY_FLOAT_VIEW_ALPHA:
                mFloatingView.updateAlpha(sharedPreferences.getInt(key, 80) / 100.0f);
                break;
            case SettingsFragment.KEY_FLOAT_VIEW_SIZE:
                mFloatingView.updateSize(sharedPreferences.getInt(key, 100) / 100.0f);
                break;
            case SettingsFragment.KEY_SMART_HIDE:
                boolean isSmartHide = sharedPreferences.getBoolean(key, true);
                mFloatingView.updateType(isSmartHide);
                break;

            default:
                break;
        }
    }

}
