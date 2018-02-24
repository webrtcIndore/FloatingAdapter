package iamrajendra.github.io.webrtcadapter.model;


import iamrajendra.github.io.webrtcadapter.video.PercentFrameLayout;
import iamrajendra.github.io.webrtcadapter.video.VideoView;

/**
 * Created by gwl on 19/1/18.
 */

public class User {
    private  String id;
    private String name;
    private PercentFrameLayout mPercentFrameLayout;
    private VideoView videoView;
    private int singalStrengh =0;
    private String url;

    public String getUrl() {
        return url;
    }

    public User(String id, String name, PercentFrameLayout mPercentFrameLayout, VideoView videoView, int singalStrengh, String url) {
        this.id = id;
        this.name = name;
        this.mPercentFrameLayout = mPercentFrameLayout;
        this.videoView = videoView;
        this.singalStrengh = singalStrengh;
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSingalStrengh() {
        return singalStrengh;
    }

    public void setSingalStrengh(int singalStrengh) {
        this.singalStrengh = singalStrengh;
    }



    public VideoView getVideoView() {
        return videoView;
    }

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
    }

    public User(String id, String name, PercentFrameLayout mPercentFrameLayout) {
        this.id = id;
        this.name = name;
        this.mPercentFrameLayout = mPercentFrameLayout;
    }

    public User() {

    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PercentFrameLayout getPercentFrameLayout() {
        return mPercentFrameLayout;
    }

    public void setmPercentFrameLayout(PercentFrameLayout mPercentFrameLayout) {
        this.mPercentFrameLayout = mPercentFrameLayout;
    }
}
