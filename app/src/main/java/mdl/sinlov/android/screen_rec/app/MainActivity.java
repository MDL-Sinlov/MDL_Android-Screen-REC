package mdl.sinlov.android.screen_rec.app;

import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mdl.sinlov.android.screen_rec.RECSettingActivity;

public class MainActivity extends MDLTestActivity {

    @BindView(R.id.btn_main_skip_screen_setting)
    Button btnMainSkipScreenSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void bindListener() {

    }

    @OnClick(R.id.btn_main_skip_screen_setting)
    public void onClick() {
        skip2Activity(RECSettingActivity.class);
    }
}
