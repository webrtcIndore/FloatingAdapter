package iamrajendra.github.io.webrtcadapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import org.webrtc.MediaStream;

import iamrajendra.github.io.webrtcadapter.rtc.RtcMultiConnectionClient;
import iamrajendra.github.io.webrtcadapter.video.VideoView;

public class ViewChangeActivity extends AppCompatActivity implements RtcMultiConnectionClient.Callback, CompoundButton.OnCheckedChangeListener, ArtcAdapter.onClickListenerCallback {
    private static final String TAG = ViewChangeActivity.class.getSimpleName();
    private ArtcAdapter artcAdapter;
    private ToggleButton toggleButton_video;
    private ToggleButton toggleButton_mic;
    private ToggleButton toggleButton_cameraSwitch;
    private String roomName;
    private boolean isJoin;
    private LinearLayout linearLayout_console;
    private FrameLayout frameLayout_view_main;
    private FloatingActionButton floatingActionButton;
    private RtcMultiConnectionClient rtcClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_change);
        initView();
        catchIntent();
        artcAdapter = new ArtcAdapter(this);
        artcAdapter.setCallback(new ArtcAdapter.Callback() {
            @Override
            public void onLocalVideoView(VideoView localVideoView) {
                Log.i(TAG, "onLocalVideoView: ");
                rtcClient = RtcMultiConnectionClient.getIns(ViewChangeActivity.this);
                rtcClient.setRoomName(roomName).join(isJoin).

                        init(localVideoView);
            }


        });

    }

    private void catchIntent() {
        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");
        isJoin = intent.getBooleanExtra("join", false);
    }

    private void initView() {
        floatingActionButton = findViewById(R.id.new_join);
        linearLayout_console = findViewById(R.id.console_ll);
        toggleButton_video = findViewById(R.id.enable_stream);
        toggleButton_mic = findViewById(R.id.mic_stream);
        toggleButton_cameraSwitch = findViewById(R.id.camera_switch_stream);
        frameLayout_view_main = findViewById(R.id.main_view_layout);
        toggleButton_video.setOnCheckedChangeListener(this);
        toggleButton_mic.setOnCheckedChangeListener(this);
        toggleButton_cameraSwitch.setOnCheckedChangeListener(this);
    }

    private int dp(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

    @Override
    public void onAddStream(MediaStream stream, int endPoint) {
        Log.i(TAG, "onAddStream: ");
        artcAdapter.onAddStream(stream, endPoint);
    }

    @Override
    public void onRemoveStream(int endPoint) {
        Log.i(TAG, "onRemoveStream: ");
        artcAdapter.onRemoveStream(endPoint + "");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.enable_stream:
                artcAdapter.localStreamEnable(isChecked);
                break;
            case R.id.mic_stream:
                artcAdapter.localMicEnable(isChecked);
                break;
            case R.id.camera_switch_stream:
                artcAdapter.localCameraSwitch();
                break;
        }
    }


    private void viewvisibility_animation() {
        linearLayout_console.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                linearLayout_console.setVisibility(View.GONE);

            }
        }, 1000 * 5);

    }

    @Override
    public void onClick(View view) {
        viewvisibility_animation();
    }
}
