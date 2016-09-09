package mdl.sinlov.android.screen_rec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import mdl.sinlov.android.screen_rec.ui.AlertDialogUtils;

public class SettingActivity extends Activity {

    public static final int REQUEST_MEDIA_PROJECTION = 18;
    private boolean isCapturePermission = false;
    private Switch swCapturePermission;
    private Switch swScreenCaotureService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        swCapturePermission = (Switch) findViewById(R.id.switch_setting_capture_permission);
        swScreenCaotureService = (Switch) findViewById(R.id.switch_setting_open_screen_cap_service);
        swCapturePermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isCapturePermission && isChecked) {
                    requestCapturePermission();
                }
            }
        });
        requestCapturePermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                swCapturePermission.setChecked(true);
                isCapturePermission = true;
            } else {
                Toast.makeText(getApplicationContext(), R.string.msg_sdk_runtime_error, Toast.LENGTH_SHORT).show();
                swCapturePermission.setChecked(false);
                isCapturePermission = false;
            }
        } else {
            swCapturePermission.setChecked(false);
            isCapturePermission = false;
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
    }
}
