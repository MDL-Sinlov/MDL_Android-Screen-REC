package mdl.sinlov.android.screen_rec;

import android.app.Application;
import android.graphics.Bitmap;

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
 * Created by sinlov on 16/9/9.
 */
public class ScreenCaptureApplication extends Application {
    private Bitmap mScreenCaptureBitmap;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Bitmap getScreenCaptureBitmap() {
        return mScreenCaptureBitmap;
    }

    public void setScreenCaptureBitmap(Bitmap mScreenCaptureBitmap) {
        this.mScreenCaptureBitmap = mScreenCaptureBitmap;
    }
}
