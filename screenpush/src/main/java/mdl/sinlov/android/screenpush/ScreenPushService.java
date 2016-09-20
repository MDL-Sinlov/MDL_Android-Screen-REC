package mdl.sinlov.android.screenpush;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * <pre>
 *     sinlov
 *
 *     /\__/\
 *    /`    '\
 *  ≈≈≈ 0  0 ≈≈≈ Hello world!
 *    \  --  /
 *   /        \
 *  /          \
 * |            |
 *  \  ||  ||  /
 *   \_oo__oo_/≡≡≡≡≡≡≡≡o
 *
 * </pre>
 * Created by sinlov on 16/9/20.
 */
public class ScreenPushService extends Service{

    private static final int MSG_CHECK_SAVE_CATCH = 1;
    private static final int MSG_START_RECORD = 2;
    private static final int MSG_STOP_RECORD = 3;
    private static Intent mResultData;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;
    private ImageView mFloatView;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private double mScreenPercentage;

    private MediaProjection mMediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private VirtualDisplay mVirtualDisplay;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mScreenPercentage = 3;
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = (int) (metrics.widthPixels / mScreenPercentage);
        mScreenHeight = (int) (metrics.heightPixels / mScreenPercentage);
        createFloatView();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class SafeHandler extends Handler {
        private WeakReference<ScreenPushService> wk;

        public SafeHandler(ScreenPushService screenCaptureService) {
            this.wk = new WeakReference<ScreenPushService>(screenCaptureService);
        }

        public ScreenPushService get() {
            return wk.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScreenPushService scs = get();
            if (null != scs) {
                switch (msg.what) {
                    case MSG_CHECK_SAVE_CATCH:
                        break;
                    case MSG_START_RECORD:
                        break;
                    case MSG_STOP_RECORD:
                        break;
                }
            }
        }
    }

    private void createFloatView() {
        mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestureTouchListener());
        mLayoutParams = new WindowManager.LayoutParams();

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // set Window flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = mScreenWidth;
        mLayoutParams.y = 100;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatView = new ImageView(getApplicationContext());
        mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_input_get));
        mFloatView.setBackgroundColor(Color.GREEN);
        mWindowManager.addView(mFloatView, mLayoutParams);
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    private class FloatGestureTouchListener implements GestureDetector.OnGestureListener {

        int lastX, lastY;
        int paramX, paramY;

        @Override
        public boolean onDown(MotionEvent event) {
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            paramX = mLayoutParams.x;
            paramY = mLayoutParams.y;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
//            if (System.currentTimeMillis() - recStartTime < START_RECORD_TIME) {
//                return true;
//            } else {
//                recStartTime = System.currentTimeMillis();
//            }
//            if (!isREC) {
//                Toast.makeText(getApplicationContext(), R.string.toast_rec_will_start, Toast.LENGTH_SHORT).show();
//                handler.sendMessageDelayed(handler.obtainMessage(MSG_START_RECORD), START_RECORD_TIME);
//            } else {
//                handler.sendMessage(handler.obtainMessage(MSG_STOP_RECORD));
//            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx = (int) e2.getRawX() - lastX;
            int dy = (int) e2.getRawY() - lastY;
            mLayoutParams.x = paramX + dx;
            mLayoutParams.y = paramY + dy;
            // reset
            mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
