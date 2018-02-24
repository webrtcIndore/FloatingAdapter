package iamrajendra.github.io.webrtcadapter.rtc;

/**
 * Created by gwl on 15/11/17.
 */

public class SDPCustomization {
    public  interface Callback
    {
        void onChangeSdp(String sdp);
    }

    public  Callback  mCallback;

    public SDPCustomization(Callback callback) {
        mCallback = callback;
    }

    /*
    custom audio and video bitrate*/
    public void setMediaBitrate(String sdp, int video, int audio) {

        if (sdp.contains("b=AS:")){
            String regex="/^[-+]?[1-9]\\d*$/";
            sdp = sdp.replaceAll("b=AS:"+regex, "b=AS:" + audio);
            sdp = sdp.replaceAll("b=AS:"+regex, "b=AS:" + video);
        }else {
            sdp = sdp.replace("a=mid:audio", "a=mid:audio\r\nb=AS:" + audio);
            sdp = sdp.replace("a=mid:video", "a=mid:video\r\nb=AS:" + video);
            mCallback.onChangeSdp(sdp);
        }

    }
}
