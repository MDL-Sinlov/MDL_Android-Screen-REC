package mdl.sinlov.android.screen_rec;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenCaptureService extends Service {

    public static final int MAX_IMAGES = 1;
    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    public ScreenCaptureService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createImageReader();
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createImageReader() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, MAX_IMAGES);

    }

    private void startCapture() {
        Image image = mImageReader.acquireLatestImage();
    }
}
