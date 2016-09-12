package mdl.sinlov.android.screen_rec;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class PreviewPictureActivity extends Activity implements GlobalScreenshot.onScreenShotListener {

    private ImageView mPreviewImageView;

    public static final Intent newIntent(Context context) {
        Intent intent = new Intent(context, PreviewPictureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_picture);
        mPreviewImageView = (ImageView) findViewById(R.id.preview_image);
        GlobalScreenshot screenshot = new GlobalScreenshot(getApplicationContext());
        Bitmap bitmap = ((ScreenCaptureApplication) getApplication()).getScreenCaptureBitmap();
        mPreviewImageView.setImageBitmap(bitmap);
        mPreviewImageView.setVisibility(View.GONE);
        if (bitmap != null) {
            screenshot.takeScreenshot(bitmap, this, true, true);
        }
        mPreviewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExFolder(FileUtil.getAppCatchPath(getApplicationContext()));
                finish();
            }
        });
    }

    @Override
    public void onStartShot() {

    }

    @Override
    public void onFinishShot(boolean success) {
        mPreviewImageView.setVisibility(View.VISIBLE);
    }

    private void openExFolder(String path) {
        File file = new File(path);
        if (null == file || !file.exists()) {
            Toast.makeText(getApplicationContext(), R.string.msg_sdk_path_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "file/*");
        try {
            startActivity(intent);
            String titleFileTool = getString(R.string.msg_intent_choose_file_tools);
            startActivity(Intent.createChooser(intent, titleFileTool));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
