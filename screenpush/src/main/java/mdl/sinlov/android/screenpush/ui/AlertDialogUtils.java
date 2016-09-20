package mdl.sinlov.android.screenpush.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import mdl.sinlov.android.screenpush.R;


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
public class AlertDialogUtils {
    public static final int NORMAL_TYPE = 1;
    public static final int ERROR_TYPE = 1 << 1;
    public static final int SUCCESS_TYPE = 1 << 2;
    public static final int WARNING_TYPE = 1 << 3;
    public static final int PROGRESS_TYPE = 1 << 4;
    public static final int PROGRESS_HORIZONTAL_TYPE = PROGRESS_TYPE + 1;

    private AlertDialog.Builder alertDialogBuilder;

    private ProgressDialog progressDialog;
    private Context ctx;
    private int type;
    private boolean isShowCancelButton;
    private OnDialogClickListener cancelListener;
    private OnDialogClickListener confirmListener;
    private boolean isCanceledOnTouchOutside = false;
    private boolean isCancelable = false;
    private boolean isCanCloseDialog = true;
    private AlertDialog alertDialog;
    private String positiveBtnText;
    private String cancelBtnText;
    private String titleText;
    private String contentText;
    Handler handler = new SafeHandler(this);

    private static class SafeHandler extends Handler {
        private WeakReference<AlertDialogUtils> wk;

        public SafeHandler(AlertDialogUtils alertDialogUtils) {
            this.wk = new WeakReference<AlertDialogUtils>(alertDialogUtils);
        }

        public AlertDialogUtils get() {
            return wk.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlertDialogUtils alertDialog = get();
            if (null != alertDialog) {
                alertDialog.progressDialog.setProgress(msg.what);
            }
        }
    }

    public interface OnDialogClickListener {
        void onClick(AlertDialogUtils AlertDialogUtils);
    }

    private static void canCloseDialog(DialogInterface dialogInterface, boolean close) {
        try {
            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialogInterface, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AlertDialogUtils(Activity activity, int type) {
        this.type = type;
        this.ctx = activity.getApplication();
        if (type <= WARNING_TYPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                alertDialogBuilder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
            } else {
                alertDialogBuilder = new AlertDialog.Builder(activity);
            }
        } else if (type == PROGRESS_TYPE) {
            progressDialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
        } else if (type == PROGRESS_HORIZONTAL_TYPE) {
            progressDialog = new ProgressDialog(activity, ProgressDialog.STYLE_HORIZONTAL);
        }
        positiveBtnText = ctx.getString(R.string.dialog_ok);
        cancelBtnText = ctx.getString(R.string.dialog_cancel);
    }

    public AlertDialog createAlertDialog() {
        if (type < PROGRESS_TYPE) {
            filterAlertDialog();
        } else {
            createProgressDialog();
            alertDialog = progressDialog;
        }
        return alertDialog;
    }

    public ProgressDialog createProgressDialog() {
        progressDialog.setCancelable(isCancelable);
        progressDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        if (!TextUtils.isEmpty(titleText)) {
            progressDialog.setTitle(titleText);
        }
        if (!TextUtils.isEmpty(contentText)) {
            progressDialog.setMessage(contentText);
        }
        if (type == PROGRESS_HORIZONTAL_TYPE) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        return progressDialog;
    }

    public void show() {
        if (null == alertDialog) {
            createAlertDialog();
        }
        alertDialog.show();
    }

    private void filterAlertDialog() {
        alertDialogBuilder.setCancelable(isCancelable);
        switch (type) {
            case NORMAL_TYPE:
                bindAlertTitle(R.string.dialog_default_title);
                bindPositiveButton();
                bindNegativeButton();
                break;
            case ERROR_TYPE:
                bindAlertTitle(R.string.dialog_error_title);
                bindPositiveButton();
                bindNegativeButton();
                break;
            case SUCCESS_TYPE:
                bindAlertTitle(R.string.dialog_success_title);
                bindPositiveButton();
                bindNegativeButton();
                break;
            case WARNING_TYPE:
                bindAlertTitle(R.string.dialog_warning_title);
                bindPositiveButton();
                bindNegativeButton();
                break;
            default:
                new IllegalArgumentException("You setting dialog type error").printStackTrace();
                alertDialogBuilder.setCancelable(true);
                break;
        }
        if (!TextUtils.isEmpty(contentText)) {
            alertDialogBuilder.setMessage(contentText);
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
    }

    private void bindAlertTitle(int titleId) {
        if (TextUtils.isEmpty(titleText)) {
            alertDialogBuilder.setTitle(titleId);
        } else {
            alertDialogBuilder.setTitle(titleText);
        }
    }

    private void bindNegativeButton() {
        if (isShowCancelButton) {
            this.alertDialogBuilder.setNegativeButton(cancelBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    canCloseDialog(dialog, isCanCloseDialog);
                    if (null != cancelListener) {
                        cancelListener.onClick(AlertDialogUtils.this);
                    }
                }
            });
        }
    }

    private void bindPositiveButton() {
        if (null == confirmListener) {
            alertDialogBuilder.setPositiveButton(R.string.dialog_ok, new DefaultClickCloseListener());
        } else {
            this.alertDialogBuilder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    canCloseDialog(dialog, isCanCloseDialog);
                    if (null != confirmListener) {
                        confirmListener.onClick(AlertDialogUtils.this);
                    }
                }
            });
        }
    }

    public void setProgress(int progress) {
        if (type == PROGRESS_HORIZONTAL_TYPE) {
            handler.sendEmptyMessageDelayed(progress, 200l);
        }
    }

    public void dismiss() {
        if (null != alertDialog) {
            alertDialog.dismiss();
        }
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    public AlertDialogUtils setTitleText(String title) {
        this.titleText = title;
        return this;
    }

    public AlertDialogUtils setTitleText(int title) {
        this.titleText = ctx.getString(title);
        return this;
    }

    public AlertDialogUtils setContentText(String msg) {
        this.contentText = msg;
        return this;
    }

    public AlertDialogUtils setContentText(int msg) {
        this.contentText = ctx.getString(msg);
        return this;
    }

    public AlertDialogUtils showCancelButton(boolean isShow) {
        this.isShowCancelButton = isShow;
        return this;
    }

    public AlertDialogUtils setCanCloseDialog(boolean isCanCloseDialog) {
        this.isCanCloseDialog = isCanCloseDialog;
        return this;
    }

    public AlertDialogUtils setCancelText(String text) {
        this.cancelBtnText = text;
        return this;
    }

    public AlertDialogUtils setCancelText(int text) {
        this.cancelBtnText = ctx.getString(text);
        return this;
    }

    public AlertDialogUtils setCancelClickListener(OnDialogClickListener listener) {
        this.cancelListener = listener;
        return this;
    }

    public AlertDialogUtils setConfirmText(String text) {
        this.positiveBtnText = text;
        return this;
    }

    public AlertDialogUtils setConfirmText(int text) {
        this.positiveBtnText = ctx.getString(text);
        return this;
    }

    public AlertDialogUtils setConfirmClickListener(OnDialogClickListener listener) {
        this.confirmListener = listener;
        return this;
    }

    public AlertDialogUtils setIsCanceledOnTouchOutside(boolean isCanceledOnTouchOutside) {
        this.isCanceledOnTouchOutside = isCanceledOnTouchOutside;
        return this;
    }

    public AlertDialogUtils setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
        return this;
    }

    private class DefaultClickCloseListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            canCloseDialog(dialog, isCanCloseDialog);
            alertDialog.dismiss();
        }
    }
}
