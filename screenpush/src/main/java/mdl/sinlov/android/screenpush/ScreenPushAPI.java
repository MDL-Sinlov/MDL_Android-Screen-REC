package mdl.sinlov.android.screenpush;

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
public class ScreenPushAPI {

    private ScreenPushAPI instance;

    public synchronized ScreenPushAPI getInstance() {
        if (null == instance) {
            return new ScreenPushAPI();
        }
        return instance;
    }

    private ScreenPushAPI() {
    }
}
