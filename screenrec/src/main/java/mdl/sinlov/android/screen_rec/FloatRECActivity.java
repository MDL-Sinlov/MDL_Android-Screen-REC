package mdl.sinlov.android.screen_rec;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import mdl.sinlov.android.screen_rec.ui.AlertDialogUtils;

public class FloatRECActivity extends AppCompatActivity {

    public static final int REQUEST_OVERLAY_PERMISSION = 8088;
    public static final int REQUEST_MEDIA_PROJECTION = 8089;
    private boolean isCanShowFloatWindow = false;
    private Intent skip2LauncherIntent;
    private Intent mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_float_rec);
        askForPermission();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        askForPermission();
    }

    public void askForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlertDialogUtils errorDialog = new AlertDialogUtils(this, AlertDialogUtils.ERROR_TYPE)
                    .setContentText(R.string.dialog_your_cellphone_api_error);
            errorDialog.show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(getApplicationContext(), R.string.toast_no_floating_window_permission, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            } else {
                isCanShowFloatWindow = true;
                requestCapturePermission();
            }
        } else {
            isCanShowFloatWindow = true;
            requestCapturePermission();
        }
    }

    private void requestCapturePermission() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    private void startFloatWindow() {
        if (isCanShowFloatWindow) {
            ScreenRECService.setResultData(mData);
            Intent recServerIntent = new Intent(getApplicationContext(), ScreenRECService.class);
            startService(recServerIntent);
            skip2Launcher();
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_no_floating_window_authorization_failure, Toast.LENGTH_SHORT).show();
        }
    }

    private void skip2Launcher() {
        if (null == skip2LauncherIntent) {
            skip2LauncherIntent = new Intent(Intent.ACTION_MAIN);
            skip2LauncherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            skip2LauncherIntent.addCategory(Intent.CATEGORY_HOME);
        }
        startActivity(skip2LauncherIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        isCanShowFloatWindow = false;
                    } else {
                        isCanShowFloatWindow = true;
                        Toast.makeText(getApplicationContext(), R.string.toast_no_floating_window_authorization_success, Toast.LENGTH_SHORT).show();
                        startFloatWindow();
                    }
                } else {
                    isCanShowFloatWindow = true;
                }
                break;
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    mData = data;
                    startFloatWindow();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
