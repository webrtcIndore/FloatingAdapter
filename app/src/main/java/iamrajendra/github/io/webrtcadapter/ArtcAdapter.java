package iamrajendra.github.io.webrtcadapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

import iamrajendra.github.io.webrtcadapter.model.User;
import iamrajendra.github.io.webrtcadapter.rtc.PeerConnectionParameters;
import iamrajendra.github.io.webrtcadapter.video.PercentFrameLayout;
import iamrajendra.github.io.webrtcadapter.video.VideoView;

/**
 * Created by rajendra on 18/1/18.
 */
public class ArtcAdapter implements View.OnClickListener, View.OnTouchListener {
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String TAG = ArtcAdapter.class.getSimpleName();
    private FrameLayout mFrameLayout_main;
    private Activity activity;
    private List<User> users;
    private EglBase rootEglBase;
    private Callback callback;
    private int _xDelta;
    private int _yDelta;
    private onClickListenerCallback onClickListenerCallback;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    public ArtcAdapter(Activity activity) {
        this.activity = activity;
        mFrameLayout_main = activity.findViewById(R.id.main_layout);
        users = new ArrayList<>();
        onClickListenerCallback = (ArtcAdapter.onClickListenerCallback) activity;
        initView();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void initView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        PercentFrameLayout percentFrameLayout = (PercentFrameLayout) inflater.inflate(R.layout.decor_adapter_rtc, null);
        percentFrameLayout.setId(0);
        SurfaceViewRenderer renderer = percentFrameLayout.findViewById(R.id.video_view);
        renderer.setId(0);
        renderer.setZOrderMediaOverlay(false);
        renderer.setOnClickListener(this);
       // renderer.setOnTouchListener(this);
        mFrameLayout_main.addView(percentFrameLayout);
        users.add(new User("0", "Rajendra Verma", percentFrameLayout, new VideoView(activity, percentFrameLayout), 1, "https://i.stack.imgur.com/gcSZo.jpg?s=328&g=1"));

        rootEglBase = EglBase.create();
        PercentFrameLayout layout = users.get(0).getPercentFrameLayout();
        TextView textView_user_bottom = layout.findViewById(R.id.username_tv);
        textView_user_bottom.setText(users.get(0).getName());
        TextView textView_user_top = layout.findViewById(R.id.usernametop_tv);
        textView_user_top.setText(users.get(0).getName());
        ImageView imageView_user = layout.findViewById(R.id.user_iv);
        imageView_user.setVisibility(View.GONE);
        Picasso.with(activity).load(users.get(0).getUrl()).placeholder(R.drawable.randeep).resize(100, 100).into(imageView_user);
        ImageView imageView_top = layout.findViewById(R.id.usertop_iv);
        Picasso.with(activity).load(users.get(0).getUrl()).placeholder(R.drawable.randeep).centerCrop().resize(100, 100).into(imageView_top);

        ImageView imageViewSingalStrength = layout.findViewById(R.id.signal_level_iv);
        imageViewSingalStrength.setImageLevel(users.get(0).getSingalStrengh());
        final VideoView videoView = users.get(0).getVideoView();
        videoView.init(rootEglBase);
        videoView.setScalingType(scalingType);
        PeerConnectionParameters params = new PeerConnectionParameters(true, true, 50, 50, 25, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        videoView.createLocalStream(params);
        videoView.setStreamAdded(true);
        videoView.startCamera();
        videoView.setCallback(new VideoView.Callback() {
            @Override
            public void onLocalStream(MediaStream mStream) {
                callback.onLocalVideoView(videoView);
            }
        });


        videoView.updateView();
        resizeView(layout);
    }

    @Override
    public void onClick(View view) {
        resizeView(view);

    }

    private void resizeView(View view) {

        User layout_x = users.get(view.getId());
        PercentFrameLayout percentFrameLayouta = (PercentFrameLayout) layout_x.getPercentFrameLayout();
        View view_bottom = percentFrameLayouta.findViewById(R.id.bottom_view);
        View view_top = percentFrameLayouta.findViewById(R.id.top_view);

        if (layout_x.getPercentFrameLayout().getWidthPercent() == 100 || layout_x.getPercentFrameLayout().getWidthPercent() == 100) {
            view_bottom.setVisibility(View.GONE);
            view_top.setVisibility(View.VISIBLE);
            onClickListenerCallback.onClick(layout_x.getPercentFrameLayout());
            return;
        }
        mFrameLayout_main.removeAllViews();
        view_bottom.setVisibility(View.GONE);
        view_top.setVisibility(View.VISIBLE);

        percentFrameLayouta.setPosition(0, 0, 100, 100);
        SurfaceViewRenderer surfaceViewRenderer1 = (SurfaceViewRenderer) ((RelativeLayout) percentFrameLayouta.getChildAt(0)).getChildAt(0);
        surfaceViewRenderer1.setEnableHardwareScaler(true);
        surfaceViewRenderer1.setZOrderMediaOverlay(false);
        percentFrameLayouta.requestLayout();
        users.remove(layout_x);
        mFrameLayout_main.addView(percentFrameLayouta);
        for (int i = 0; i < users.size(); i++) {
            PercentFrameLayout frameLayout = users.get(i).getPercentFrameLayout();
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) ((RelativeLayout) frameLayout.getChildAt(0)).getChildAt(0);
            surfaceViewRenderer.setEnableHardwareScaler(true);
            surfaceViewRenderer.setZOrderMediaOverlay(true);
            frameLayout.setPosition(75, 25 * i, 25, 25);
            frameLayout.setId(i);
            surfaceViewRenderer.setId(i);
            View view_bottom_i = frameLayout.findViewById(R.id.bottom_view);
            View view_top_i = frameLayout.findViewById(R.id.top_view);
            view_bottom_i.setVisibility(View.VISIBLE);
            view_top_i.setVisibility(View.GONE);

            mFrameLayout_main.addView(frameLayout);
        }
        users.add(0, layout_x);
        for (int i = 0; i < users.size(); i++) {
            PercentFrameLayout frameLayout = users.get(i).getPercentFrameLayout();
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) ((RelativeLayout) users.get(i).getPercentFrameLayout().getChildAt(0)).getChildAt(0);
            frameLayout.setId(i);
            surfaceViewRenderer.setId(i);
        }
    }


    private void resizeView(int pos) {
        User layout_x = users.get(pos);
        PercentFrameLayout percentFrameLayouta = (PercentFrameLayout) layout_x.getPercentFrameLayout();
        View view_bottom = percentFrameLayouta.findViewById(R.id.bottom_view);
        View view_top = percentFrameLayouta.findViewById(R.id.top_view);
        mFrameLayout_main.removeAllViews();
        view_bottom.setVisibility(View.GONE);
        view_top.setVisibility(View.VISIBLE);

        percentFrameLayouta.setPosition(0, 0, 100, 100);
        SurfaceViewRenderer surfaceViewRenderer1 = (SurfaceViewRenderer) ((RelativeLayout) percentFrameLayouta.getChildAt(0)).getChildAt(0);
        surfaceViewRenderer1.setEnableHardwareScaler(true);
        surfaceViewRenderer1.setZOrderMediaOverlay(false);
        percentFrameLayouta.requestLayout();


        users.remove(layout_x);
        mFrameLayout_main.addView(percentFrameLayouta);
        for (int i = 0; i < users.size(); i++) {
            PercentFrameLayout frameLayout = users.get(i).getPercentFrameLayout();
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) ((RelativeLayout) frameLayout.getChildAt(0)).getChildAt(0);
            surfaceViewRenderer.setEnableHardwareScaler(true);
            surfaceViewRenderer.setZOrderMediaOverlay(true);
            frameLayout.setPosition(75, 25 * i, 25, 25);
            frameLayout.setId(i);
            surfaceViewRenderer.setId(i);
            View view_bottom_i = frameLayout.findViewById(R.id.bottom_view);
            View view_top_i = frameLayout.findViewById(R.id.top_view);
            view_bottom_i.setVisibility(View.VISIBLE);
            view_top_i.setVisibility(View.GONE);

            mFrameLayout_main.addView(frameLayout);
        }
        users.add(0, layout_x);
        for (int i = 0; i < users.size(); i++) {
            PercentFrameLayout frameLayout = users.get(i).getPercentFrameLayout();
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) ((RelativeLayout) users.get(i).getPercentFrameLayout().getChildAt(0)).getChildAt(0);
            frameLayout.setId(i);
            surfaceViewRenderer.setId(i);
        }
    }


    public void onAddStream(final MediaStream stream, final int endPoint) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addView(endPoint);
                PercentFrameLayout layout = users.get(endPoint).getPercentFrameLayout();
                SurfaceViewRenderer renderer = (SurfaceViewRenderer) ((RelativeLayout) users.get(endPoint).getPercentFrameLayout().getChildAt(0)).getChildAt(0);
                TextView textView = layout.findViewById(R.id.username_tv);
                textView.setText(users.get(endPoint).getName());
                ImageView imageView_user = layout.findViewById(R.id.user_iv);
                Picasso.with(activity).load(users.get(endPoint).getUrl()).placeholder(R.drawable.randeep).resize(100, 100).into(imageView_user);
                ImageView imageView_top = layout.findViewById(R.id.usertop_iv);
                Picasso.with(activity).load(users.get(endPoint).getUrl()).placeholder(R.drawable.randeep).resize(100, 100).into(imageView_top);
                imageView_user.setVisibility(View.GONE);
                TextView textView_user_top = layout.findViewById(R.id.usernametop_tv);
                textView_user_top.setText(users.get(endPoint).getName());
                ImageView imageViewSingalStrength = layout.findViewById(R.id.signal_level_iv);
                imageViewSingalStrength.setImageLevel(users.get(endPoint).getSingalStrengh());
                VideoView videoView = users.get(endPoint).getVideoView();
                videoView.init(rootEglBase);
                videoView.setScalingType(scalingType);
                videoView.setRemoteMediaStream(stream);
                videoView.updateView();
                layout.requestLayout();
                renderer.setEnableHardwareScaler(true);
                renderer.setZOrderMediaOverlay(true);
                mFrameLayout_main.addView(layout);

            }
        });
    }

    private void addView(int i) {
        int viewPos = i - 1;
        LayoutInflater inflater = LayoutInflater.from(activity);
        PercentFrameLayout percentFrameLayout = (PercentFrameLayout) inflater.inflate(R.layout.decor_adapter_rtc, null);
        percentFrameLayout.setId(i);
        SurfaceViewRenderer renderer = percentFrameLayout.findViewById(R.id.video_view);
        renderer.setEnableHardwareScaler(true);
        renderer.setZOrderMediaOverlay(true);
        renderer.setId(i);
        renderer.setOnClickListener(this);
        percentFrameLayout.setOnTouchListener(this);
        View view_top = percentFrameLayout.findViewById(R.id.top_view);
        view_top.setVisibility(View.GONE);


        if (i == 0) {
            percentFrameLayout.setPosition(75, 0, 25, 25);

        } else {
            percentFrameLayout.setPosition(75, 25 * viewPos, 25, 25);

        }
        switch (i) {
            case 1:
                users.add(new User("1", "Jack", percentFrameLayout, new VideoView(activity, percentFrameLayout), 2, "https://static.pexels.com/photos/36483/aroni-arsa-children-little.jpg"));
                break;
            case 2:
                users.add(new User("2", "Akshay", percentFrameLayout, new VideoView(activity, percentFrameLayout), 3, "https://pbs.twimg.com/profile_images/841956276141711360/Yh7xHO41_400x400.jpg"));
                break;
            case 3:
                users.add(new User("3", "Sumit", percentFrameLayout, new VideoView(activity, percentFrameLayout), 1, "https://images.yourstory.com/2016/03/yourstory-amitabh-bachchan.jpg?auto=compress"));
                break;
            case 4:
                users.add(new User("4", "Aditi", percentFrameLayout, new VideoView(activity, percentFrameLayout), 4, "https://images-na.ssl-images-amazon.com/images/M/MV5BMTY3OTc3NTg2OV5BMl5BanBnXkFtZTgwMjkzMDY3NTE@._V1_UY317_CR25,0,214,317_AL_.jpg"));
                break;
        }
        renderer.requestLayout();

    }

    public void onRemoveStream(final String userId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getId().equals(userId)) {
                        VideoView videoView = users.get(i).getVideoView();
                      videoView.removeMediaStream("");
                        videoView = null;
                        mFrameLayout_main.removeView(users.get(i).getPercentFrameLayout());
                        users.remove(i);
                    }
                }

                resizeView(0);
            }
        });


    }

    public void localStreamEnable(boolean checked) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals("0")) {
                VideoView videoView = users.get(i).getVideoView();
                videoView.setVideoEnable(checked);
                ImageView imageView = users.get(i).getPercentFrameLayout().findViewById(R.id.user_iv);
                imageView.setVisibility(checked ? View.GONE : View.VISIBLE);

            }
        }
    }


    public void localMicEnable(boolean checked) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals("0")) {
                VideoView videoView = users.get(i).getVideoView();
                videoView.onToggleMic(checked);
                CheckBox mic_checkbox = users.get(i).getPercentFrameLayout().findViewById(R.id.mic_check_cb);
                mic_checkbox.setChecked(checked);

            }
        }
    }


    public void remoteStreamEnable(boolean checked, String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                VideoView videoView = users.get(i).getVideoView();
                ImageView imageView = users.get(i).getPercentFrameLayout().findViewById(R.id.user_iv);
                imageView.setVisibility(checked ? View.GONE : View.VISIBLE);

            }
        }
    }

    public void remoteStreamMicEnable(boolean checked, String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                VideoView videoView = users.get(i).getVideoView();
                videoView.setVideoEnable(checked);
                CheckBox mic_checkbox = users.get(i).getPercentFrameLayout().findViewById(R.id.mic_check_cb);
                mic_checkbox.setChecked(checked);
            }
        }
    }

    public void localCameraSwitch() {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals("0")) {
                VideoView videoView = users.get(i).getVideoView();
                videoView.switchCameraInternal();

            }
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        mFrameLayout_main.invalidate();
        return true;
    }

    public interface onClickListenerCallback {
        public void onClick(View view);


    }
    public interface Callback {
        public void onLocalVideoView(VideoView videoView);


    }


}
