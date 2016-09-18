package mdl.sinlov.android.screen_rec;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScreenRECService extends Service {

    public static final int MAX_IMAGES = 1;
    private static final long SCREEN_CAPTURE_SLEEP_TIME = 2000l;
    private static final int MSG_CHECK_IMAGE_CATCH = 1;
    private static final int MSG_SINGLE_SCREEN_CAPTURE = 2;
    public static final String SC_WIDTH = "service:ScreenRECService:SC:width";
    public static final String SC_HEIGHT = "service:ScreenRECService:SC:height";
    public static final String SC_DENSITY = "service:ScreenRECService:SC:Density";
    public static final String SC_REDUCTION_MAGNIFICATION = "service:ScreenRECService:SC:Reduction_Magnification";
    private static final int FIXED_THREAD_POOL_SIZE = 2;
    private ExecutorService fixedThreadPool;
    private ImageReader mImageReader;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mReductionMagnification;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mediaProjectionManager;
    private static Intent mResultData;
    private SafeHandler handler;


    public ScreenRECService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fixedThreadPool = Executors.newFixedThreadPool(FIXED_THREAD_POOL_SIZE);
        handler = new SafeHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bind = intent.getExtras();
        mScreenWidth = bind.getInt(SC_WIDTH, 0);
        mScreenHeight = bind.getInt(SC_HEIGHT, 0);
        mScreenDensity = bind.getInt(SC_DENSITY, 0);
        mReductionMagnification = bind.getInt(SC_REDUCTION_MAGNIFICATION, 1);
        createImageReader();
        handler.sendMessage(handler.obtainMessage(MSG_CHECK_IMAGE_CATCH));
        startScreenCapture();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVirtual();
        tearDownMediaProjection();
        stopSaveImage();
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
                    case MSG_CHECK_IMAGE_CATCH:
                        scs.checkAppCatch();
                        break;
                    case MSG_SINGLE_SCREEN_CAPTURE:
                        scs.startScreenShot();
                        break;
                }
            }
        }
    }

    private void startScreenCapture() {
        Runnable screenREC = new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(SCREEN_CAPTURE_SLEEP_TIME);
                handler.sendMessageDelayed(handler.obtainMessage(MSG_SINGLE_SCREEN_CAPTURE), 5);
            }
        };
        fixedThreadPool.execute(screenREC);
    }

    private void checkAppCatch() {
        String appPath = FileUtil.getAppCatchPath(getApplicationContext());
        File appCatch = new File(appPath);
        if (!appCatch.exists()) {
            appCatch.mkdirs();
        }
    }

    private void createImageReader() {
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, MAX_IMAGES);
    }

    private void startCapture() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            startScreenShot();
        } else {
            saveImage(image);
        }
    }

    private void startScreenShot() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startVirtual();
            }
        }, 5);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startCapture();
            }
        }, 30);
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
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
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

    private void stopSaveImage() {
        if (!fixedThreadPool.isShutdown()) {
            fixedThreadPool.shutdown();
        }
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
        fixedThreadPool.execute(saveTask);
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
        int w = (width + rowPadding / pixelStride) / mReductionMagnification;
        int h = (height) / mReductionMagnification;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width / mReductionMagnification, height / mReductionMagnification);
        image.close();
        return bitmap;
    }

}