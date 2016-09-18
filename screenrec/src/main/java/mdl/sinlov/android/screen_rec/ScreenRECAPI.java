package mdl.sinlov.android.screen_rec;

import android.content.Context;
import android.content.Intent;

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
 * Created by sinlov on 16/9/18.
 */
public class ScreenRECAPI {

    private static ScreenRECAPI instance;

    public void startREC(Context ctx) {
        Intent floatRECIntent = new Intent(ctx, FloatRECActivity.class);
        floatRECIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.getApplicationContext().startActivity(floatRECIntent);
    }

    public synchronized static ScreenRECAPI getInstance() {
        if (null == instance) {
            return new ScreenRECAPI();
        }
        return instance;
    }

    private ScreenRECAPI() {
    }
}
