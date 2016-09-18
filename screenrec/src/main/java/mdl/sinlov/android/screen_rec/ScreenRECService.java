package mdl.sinlov.android.screen_rec;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class ScreenRECService extends Service {

    public static final long START_RECORD_TIME = 3000l;
    private static final int MSG_CHECK_SAVE_CATCH = 1;
    private static final int MSG_START_RECORD = 2;
    private static final int MSG_STOP_RECORD = 3;
    private static final int VIDEO_FRAME_RATE = 30;
    private static final int VIDEO_ENCODING_BIT_RATE = 5 * 1024 * 1024;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;
    private ImageView mFloatView;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mediaRecorder;
    private String recFilePath;
    private static Intent mResultData;
    private SafeHandler handler;
    private boolean isREC = false;


    public ScreenRECService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new SafeHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Bundle bind = intent.getExtras();
//        mScreenWidth = bind.getInt(SC_WIDTH, 0);
//        mScreenHeight = bind.getInt(SC_HEIGHT, 0);
//        mScreenDensity = bind.getInt(SC_DENSITY, 0);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        createFloatView();
        handler.sendMessage(handler.obtainMessage(MSG_CHECK_SAVE_CATCH));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecord();
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
        }
        stopVirtual();
        tearDownMediaProjection();
    }

    private static class SafeHandler extends Handler {
        private WeakReference<ScreenRECService> wk;

        public SafeHandler(ScreenRECService screenCaptureService) {
            this.wk = new WeakReference<ScreenRECService>(screenCaptureService);
        }

        public ScreenRECService get() {
            return wk.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScreenRECService scs = get();
            if (null != scs) {
                switch (msg.what) {
                    case MSG_CHECK_SAVE_CATCH:
                        scs.checkAppCatch();
                        break;
                    case MSG_START_RECORD:
                        scs.startRecord();
                        break;
                    case MSG_STOP_RECORD:
                        scs.stopRecord();
                        break;
                }
            }
        }
    }

    private void startRecord() {
        if (null == mediaRecorder) {
            initRecorder();
        }
        startVirtual();
        mediaRecorder.start();
        isREC = true;
        mFloatView.setBackgroundColor(Color.RED);
    }

    private void stopRecord() {
        if (!isREC) {
            return;
        }
        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mVirtualDisplay.release();
            mMediaProjection.stop();
            mFloatView.setBackgroundColor(Color.GREEN);
            String tInfo = getString(R.string.toast_rec_stop_file) + recFilePath;
            Toast.makeText(getApplicationContext(), tInfo, Toast.LENGTH_LONG).show();
            isREC = false;
        }
    }

    private void checkAppCatch() {
        String appPath = FileUtil.getAppCatchPath(getApplicationContext());
        File appCatch = new File(appPath);
        if (!appCatch.exists()) {
            appCatch.mkdirs();
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
        mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_camera));
        mFloatView.setBackgroundColor(Color.GREEN);
        mWindowManager.addView(mFloatView, mLayoutParams);
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    private void initRecorder() {
        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recFilePath = FileUtil.getRECName(getApplicationContext());
        mediaRecorder.setOutputFile(recFilePath);
        mediaRecorder.setVideoSize(mScreenWidth / 2, mScreenHeight / 2);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(VIDEO_ENCODING_BIT_RATE);
        mediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    private void virtualDisplay() {
        if (null != mMediaProjection) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen",
                    mScreenWidth / 2, mScreenHeight / 2, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mediaRecorder.getSurface(), null, null);
//            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
//                    mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                    mImageReader.getSurface(), null, null);
        } else {
            throw new NullPointerException("mMediaProjection is null pointer");
        }
    }

    public void setUpMediaProjection() {
        mediaProjectionManager = getMediaProjectionManager();
        if (mediaProjectionManager != null) {
            mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mResultData);
        } else {
            throw new NullPointerException("mediaProjectionManager is null pointer");
        }
    }

    public static Intent getResultData() {
        return mResultData;
    }

    public static void setResultData(Intent mResultData) {
        ScreenRECService.mResultData = mResultData;
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return null == mediaProjectionManager ?
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE) :
                mediaProjectionManager;
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }


    private void saveImage(final Image image) {
        Runnable saveTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = matchBitmapFromCatch(image);
                File fileImage = null;
                if (bitmap != null) {
                    try {
                        String screenShotsName = FileUtil.getScreenShotsName(getApplicationContext());
                        Log.d("SC", screenShotsName);
                        fileImage = new File(screenShotsName);
                        if (!fileImage.exists()) {
                            fileImage.createNewFile();
                        }
                        FileOutputStream out = new FileOutputStream(fileImage);
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(fileImage);
                            media.setData(contentUri);
                            sendBroadcast(media);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        fileImage = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        fileImage = null;
                    }
                }
                if (fileImage != null) {
                    Log.w("REC", "获取图片成功");
                    ((ScreenCaptureApplication) getApplication()).setScreenCaptureBitmap(bitmap);
                    startActivity(PreviewPictureActivity.newIntent(getApplicationContext()));
                }
            }
        };
    }

    private Bitmap matchBitmapFromCatch(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //每个像素的间距
        int pixelStride = planes[0].getPixelStride();
        //总的间距
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        int w = width + rowPadding / pixelStride;
        int h = height;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        return bitmap;
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
            if (!isREC) {
                Toast.makeText(getApplicationContext(), R.string.toast_rec_will_start, Toast.LENGTH_SHORT).show();
                handler.sendMessageAtTime(handler.obtainMessage(MSG_START_RECORD), START_RECORD_TIME);
            } else {
                handler.sendMessageAtTime(handler.obtainMessage(MSG_STOP_RECORD), START_RECORD_TIME);
            }
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