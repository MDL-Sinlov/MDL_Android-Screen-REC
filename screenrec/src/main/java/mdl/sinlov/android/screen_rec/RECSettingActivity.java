package mdl.sinlov.android.screen_rec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import mdl.sinlov.android.screen_rec.ui.AlertDialogUtils;

public class RECSettingActivity extends Activity {

    private Switch swCapturePermission;
    private Switch swScreenCaptureService;
    public static final int REQUEST_CAN_DRAW_OVER_LAYOUT = 10;
    public static final int REQUEST_MEDIA_PROJECTION = 18;
    private boolean isCapturePermission = false;
    private Intent scService;
    private Intent mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        swCapturePermission = (Switch) findViewById(R.id.switch_setting_capture_permission);
        swScreenCaptureService = (Switch) findViewById(R.id.switch_setting_open_screen_cap_service);
        swCapturePermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isCapturePermission && isChecked) {
                    requestCapturePermission();
                }
            }
        });
        swScreenCaptureService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isCapturePermission) {
                    if (isChecked) {
                        if (mData != null) {
                            startScreenCaptureService();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (null != scService) {
                            stopService(scService);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                    swScreenCaptureService.setChecked(false);
                }
            }
        });
    }

    private void startScreenCaptureService() {
        ScreenRECService.setResultData(mData);
        scService = new Intent(getApplicationContext(), ScreenRECService.class);
        startService(scService);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CAN_DRAW_OVER_LAYOUT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(RECSettingActivity.this)) {
                        isCapturePermission = true;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                        isCapturePermission = false;
                        return;
                    }
                }
                break;
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    mData = data;
                    swCapturePermission.setChecked(true);
                    isCapturePermission = true;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                    swCapturePermission.setChecked(false);
                    isCapturePermission = false;
                }
                break;
            default:
                swCapturePermission.setChecked(false);
                isCapturePermission = false;
                break;
        }
    }

    public void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlertDialogUtils errorDialog = new AlertDialogUtils(this, AlertDialogUtils.ERROR_TYPE)
                    .setContentText(R.string.dialog_your_cellphone_api_error);
            errorDialog.show();
            return;
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(RECSettingActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CAN_DRAW_OVER_LAYOUT);
            }
        }
    }
}
