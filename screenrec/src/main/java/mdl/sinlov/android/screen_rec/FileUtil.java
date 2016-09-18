package mdl.sinlov.android.screen_rec;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
public class FileUtil {
    public static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String SCREEN_CAPTURE_PATH = "ScreenCapture" + File.separator + "Screenshots" + File.separator;
    public static final String SCREENSHOT_NAME = "Screenshot";
    private static String appCatchPath;
    private static StringBuffer stringBuffer = new StringBuffer();

    public static String getAppPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return context.getFilesDir().toString();
        }
    }


    public static String getScreenShots(Context context) {
        stringBuffer.setLength(0);
        stringBuffer.append(File.separator);
        stringBuffer.append(SCREEN_CAPTURE_PATH);
        File file = new File(stringBuffer.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return stringBuffer.toString();
    }

    public static String getScreenShotsName(Context context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS", Locale.CHINA);
        String date = simpleDateFormat.format(new Date());
        stringBuffer.setLength(0);
        stringBuffer.append(getAppCatchPath(context));
        stringBuffer.append(File.separator);
        stringBuffer.append(SCREENSHOT_NAME);
        stringBuffer.append("_");
        stringBuffer.append(date);
        stringBuffer.append(".png");
        return stringBuffer.toString();
    }

    public static String getRECName(Context context){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SSS", Locale.CHINA);
        String date = simpleDateFormat.format(new Date());
        stringBuffer.setLength(0);
        stringBuffer.append(getAppCatchPath(context));
        stringBuffer.append(File.separator);
        stringBuffer.append(SCREENSHOT_NAME);
        stringBuffer.append("_");
        stringBuffer.append(date);
        stringBuffer.append(".mp4");
        return stringBuffer.toString();
    }

    public static String getAppCatchPath(Context context) {
        return appCatchPath == null ?
                SD_PATH + File.separator +
                        "Android/data/" +
                        context.getPackageName() + File.separator + "sc_catch" :
                appCatchPath;
    }
}
