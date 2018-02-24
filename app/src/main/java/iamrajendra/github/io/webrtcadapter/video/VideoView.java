package iamrajendra.github.io.webrtcadapter.video;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import iamrajendra.github.io.webrtcadapter.rtc.PeerConnectionParameters;

/**
 * Created by rajendra verma.
 */

public class VideoView {
    private static final String TAG = VideoView.class.getSimpleName();
    private Activity activity;
    private PercentFrameLayout cover;
    private SurfaceViewRenderer videoView;
    private final List<VideoRenderer.Callbacks> videoViewCallback = new ArrayList<VideoRenderer.Callbacks>();
    private int x, y, width, height;
    private RendererCommon.ScalingType type;
    private VideoRenderer videoRenderer;
    private VideoTrack currentVideoTrack;
    private PeerConnectionFactory factory;
    private MediaConstraints pcConstraints;
    private final String CONSTANT_MEDIA_STREAM = "ARDAMS";
    private EglBase eglBase;
    private MediaStream local_mediaStream;
    private PeerConnectionParameters peerConnectionParameters;
    private static final String CONSTANT_VIDEO_ID = "ARDAMSv0";
    private static final String CONSTANT_AUDIO_ID = "ARDAMSa0";
    private MediaStream remoteStream;
    private AudioTrack localAudioTrack;
    private VideoTrack mVideoTrack;
    CameraVideoCapturer videoCapturer = null;
    private boolean viewChanged;
    private String mUserId;
    private  boolean isStreamAdded=false;

    public boolean isStreamAdded() {
        return isStreamAdded;
    }

    public void setStreamAdded(boolean streamAdded) {
        isStreamAdded = streamAdded;
    }

    public void setViewChanged(boolean f)
    {
        viewChanged =f;
    }
    public boolean isViewChanged() {
        return viewChanged;
    }

    public View getCover() {
        return cover;
    }
    public String getMediaStreamId() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return "jack" + simpleDateFormat.format(date);
    }

    public void setUserId(String userId) {
        Log.i(TAG, "setUserId: "+userId);
        mUserId = userId;
    }

    public String getUserId() {
        Log.i(TAG, "getUserId: "+mUserId);
        return mUserId;
    }

    public interface Callback {
        public void onLocalStream(MediaStream mStream);
    }

    private Callback callback;

    public void setCallback(Callback mCallback) {
        callback = mCallback;
    }

    public VideoView( Activity mActivity, PercentFrameLayout percentFrameLayout) {
        activity = mActivity;
        cover = percentFrameLayout;
        videoView = (SurfaceViewRenderer)((RelativeLayout)cover.getChildAt(0)).getChildAt(0);
        videoViewCallback.add(videoView);
    }

    public void init(EglBase rootEglBase) {
        eglBase = rootEglBase;
        videoView.init(rootEglBase.getEglBaseContext(), null);
        videoView.setZOrderMediaOverlay(false);
        videoView.setEnableHardwareScaler(true);
    }

    public void setOverlayView(boolean mOverlayView) {
        videoView.setZOrderMediaOverlay(mOverlayView);

    }

    public void setOnClickVideo(View.OnClickListener mListener) {
        videoView.setOnClickListener(mListener);
    }

    public void setDimension(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

    }

    public void setScalingType(RendererCommon.ScalingType mType) {
        type = mType;
    }

    public void updateView() {
//        cover.setPosition(x, y, width, height);
        videoView.setScalingType(type);
        videoView.setMirror(false);
        cover.requestLayout();
    }

    public void setRemoteMediaStream(MediaStream mStream) {
        if(videoView.getVisibility()==View.INVISIBLE)
        {
            videoView.setVisibility(View.VISIBLE);
            cover.setVisibility(View.VISIBLE);
        }

        remoteStream = mStream;
        VideoTrack videoTrack = mStream.videoTracks.get(0);
        videoTrack.setEnabled(true);

        if (videoRenderer != null && currentVideoTrack != null)
            currentVideoTrack.removeRenderer(videoRenderer);
        for (VideoRenderer.Callbacks remoteRender : videoViewCallback) {
            videoRenderer = new VideoRenderer(remoteRender);
            currentVideoTrack = videoTrack;
            videoTrack.addRenderer(videoRenderer);
        }

    }


    public void removeMediaStream(String mStream) {

        videoView.setVisibility(View.INVISIBLE);
    }

    public void createLocalStream(PeerConnectionParameters peerConnectionParameters) {
        PeerConnectionFactory.initializeAndroidGlobals(activity, true, true, true);
        this.peerConnectionParameters = peerConnectionParameters;
        factory = new PeerConnectionFactory();

    }

    public void startCamera() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                pcConstraints = new MediaConstraints();
                pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

                factory.setVideoHwAccelerationOptions(eglBase.getEglBaseContext(), eglBase.getEglBaseContext());
                local_mediaStream = factory.createLocalMediaStream(getMediaStreamId());
                mVideoTrack = createVideoTrack(getVideoCapturer(new Camera2Enumerator(activity)));
                local_mediaStream.addTrack(mVideoTrack);

                AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
                localAudioTrack = factory.createAudioTrack(CONSTANT_AUDIO_ID, audioSource);
                local_mediaStream.addTrack(localAudioTrack);
                callback.onLocalStream(local_mediaStream);



            }
        });

        t.start();
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        VideoSource videoSource = factory.createVideoSource(capturer);
        capturer.startCapture(peerConnectionParameters.videoWidth, peerConnectionParameters.videoHeight, peerConnectionParameters.videoFps);
        VideoTrack localVideoTrack = factory.createVideoTrack(CONSTANT_VIDEO_ID, videoSource);
        localVideoTrack.setEnabled(true);
        videoRenderer = new VideoRenderer(videoView);
        localVideoTrack.addRenderer(videoRenderer);
        currentVideoTrack = localVideoTrack;
        return localVideoTrack;
    }

    private VideoCapturer getVideoCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        Log.d(TAG, "Looking for front facing cameras.");

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer." + deviceName);
                videoCapturer = enumerator.createCapturer(deviceName, null);
                // videoCapturer.startCapture(1280, 720, 10000);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating other camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return videoCapturer;
    }

    public MediaStream getRemoteStream() {
        if (remoteStream == null) {
            return local_mediaStream;
        } else {
            return remoteStream;
        }
    }

    public PeerConnectionFactory getFactory() {
        return factory;
    }

    public MediaConstraints getMediaConstraint() {
        return pcConstraints;
    }

    public MediaStream getLocalStream() {
        return local_mediaStream;
    }

    public void onToggleMic(boolean mic) {
        if (localAudioTrack != null) {

            localAudioTrack.setEnabled(mic);

        }
    }

    public void setVideoEnable(boolean flag) {

        mVideoTrack.setEnabled(flag);
    }

    public  void enableSpeaker(boolean isOn, Context context)
    {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(isOn){
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }else{
            //Seems that this back and forth somehow resets the audio channel
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(isOn);
    }

    public void switchCameraInternal() {

            if (videoCapturer instanceof CameraVideoCapturer){

                Log.d(TAG, "Switch camera");
                CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;

                cameraVideoCapturer.switchCamera(null);

            } else {

                Log.d(TAG, "Will not switch camera, video caputurer is not a camera");

            }

    }


}
