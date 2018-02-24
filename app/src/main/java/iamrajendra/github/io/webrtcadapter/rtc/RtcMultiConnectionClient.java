package iamrajendra.github.io.webrtcadapter.rtc;

import android.app.Activity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import iamrajendra.github.io.webrtcadapter.video.VideoView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by gwl on 25/7/17.
 */

public class RtcMultiConnectionClient {

    private static final String TAG = RtcMultiConnectionClient.class.getSimpleName();
    private static final String CONSTANT_VIDEO_ID = "ARDAMSv0";
    private static final String CONSTANT_AUDIO_ID = "ARDAMSa0";
    private final static int MAX_PEER = 5;
    private final String CONSTANT_SENDMESSAGE = "audio-video-screen-demo";
    private final String CONSTANT_MEDIA_STREAM = "ARDAMS";
    private final String CONSTANT_OFFER = "newParticipationRequest";
    private final String CONSTANT_ANSWER = "answer";
    private final String CONSTANT_CANDIDATE = "candidate";
    private final String CONSTANT_SEND_USER_PREF = "userPreferences";
    private final String CONSTANT_REMOTE_USERID = "remoteUserId:";
    private boolean[] endPoints = new boolean[MAX_PEER];
    private Socket client;
    private static  Activity mActivity;
    private HashMap<String, Peer> peers = new HashMap<>();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private String userId;
    private Callback callback;
    private boolean join = false;
    private String roomId = "raj";
    private SSLContext sc;
    private ScheduledExecutorService executor;
    private VideoView localVideoView;
    private PeerConnectionFactory factory;
    private static  RtcMultiConnectionClient mRtcMultiConnectionClient;

    private final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }
    }
    };



    public  static  RtcMultiConnectionClient  getIns(Activity activity)
    {
        Log.i(TAG, "getIns: MAI SINGLE HU :(");
       mActivity = activity;
        if (mRtcMultiConnectionClient ==null)
        {
            mRtcMultiConnectionClient  = new RtcMultiConnectionClient();
        }
        return mRtcMultiConnectionClient;
    }



    public void init(VideoView localVideoView) {
        this.localVideoView = localVideoView;
        callback = (Callback) mActivity;

        executor = Executors.newSingleThreadScheduledExecutor();
        Random random = new Random();
        factory = localVideoView.getFactory();
        int value = random.nextInt(1000);
        userId = "jack" + value;

        try {
            connectSocket();
        } catch (URISyntaxException e) {
            Log.i(TAG, "RtcMultiConnectionClient: " + e.toString());
        }
    }

    public RtcMultiConnectionClient setRoomName(String roomname) {
        roomId = roomname;
        return this;
    }

    public RtcMultiConnectionClient join(boolean isJoin) {
        join = isJoin;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    private Peer addPeer(String id, JSONObject connectionDes, int endPoint) {
        Peer peer = new Peer(id, endPoint, connectionDes);
        peers.put(id, peer);
        endPoints[endPoint] = true;
        return peer;
    }

    public void sendUserPreferences(String remoteUserId, JSONObject payload) throws JSONException {

        Log.i(TAG, "sendMessage: sending local sdp type" + "payload" + payload);
        JSONObject message = new JSONObject();
        message.put("remoteUserId", remoteUserId);
        //        here sender is local user
        message.put("sender", getUserId());
        message.put("message", payload);
        message.put("password", false);
        client.emit(CONSTANT_SENDMESSAGE, message);


    }

    private void connectSocket() throws URISyntaxException {

        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = false;
        options.secure = true;
        options.sslContext = sc;

        MessageHandler handler = new MessageHandler();

        if (join) {
            options.query = "socketCustomEvent=RTCMultiConnection-Custom-Message&sessionid=" + roomId + "&userid=" + getUserId() + "&autoCloseEntireSession=false&msgEvent=" + CONSTANT_SENDMESSAGE + "&maxParticipantsAllowed=1000&log=true&EIO:3&transport:polling&t:LqiYB5k";
        } else {
            userId = roomId;
            options.query = "socketCustomEvent=RTCMultiConnection-Custom-Message&sessionid=" + roomId + "&userid=" + getUserId() + "&autoCloseEntireSession=false&msgEvent=" + CONSTANT_SENDMESSAGE + "&maxParticipantsAllowed=1000&log=true&EIO:3&transport:polling&t:LqiYB5k";
        }

        client = IO.socket("https://rtcmulticonnection.herokuapp.com", options);


        client.on(Socket.EVENT_CONNECT, handler.onConntect);
        client.on(Socket.EVENT_CONNECT_ERROR, handler.onError);
        client.on(Socket.EVENT_ERROR, handler.onError);
        client.on(Socket.EVENT_CONNECT_TIMEOUT, handler.onError);
        client.on(CONSTANT_SENDMESSAGE, handler.onMessage);
        client.connect();

        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
    }

    private void sendMessage(String remoteUserId, JSONObject message) throws JSONException {
        JSONObject main = new JSONObject();
        main.put("remoteUserId", remoteUserId);
//        here sender is local user
        main.put("sender", getUserId());
        main.put("message", message);

        client.emit(CONSTANT_SENDMESSAGE, main);
    }

    private void removePeer(String id) {
        RtcMultiConnectionClient.Peer peer = peers.get(id);
        peer.pc.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }

    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++)
            if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    private void joinNew() {
        JSONObject message = new JSONObject();
        try {
            message.put("newParticipationRequest", true);
            message.put("isOneWay", false);
            message.put("isDataOnly", false);
            JSONObject localPeerCons = new JSONObject();
            localPeerCons.put("OfferToReceiveAudio", true);
            localPeerCons.put("OfferToReceiveVideo", true);
            message.put("localPeerSdpConstraints", localPeerCons);
            JSONObject remotePeerSdp = new JSONObject();
            remotePeerSdp.put("OfferToReceiveAudio", true);
            remotePeerSdp.put("OfferToReceiveVideo", true);
            message.put("remotePeerSdpConstraints", remotePeerSdp);
            sendMessage(roomId, message);
        } catch (JSONException e) {
            Log.e(TAG, "call: " + e);
        }
    }


    public interface Callback {

        public void onAddStream(MediaStream stream, int endPoint);

        void onRemoveStream(int endPoint);
    }

    private interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private class MessageHandler {
        private HashMap<String, Command> commandMap;
        private Emitter.Listener onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject mainJson = (JSONObject) args[0];

//                create offer
                try {
                    final JSONObject message = mainJson.getJSONObject("message");
                    ;
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!peers.containsKey(mainJson.getString("sender"))) {

                                    int endPoint = findEndPoint();
                                    addPeer(mainJson.getString("sender"), mainJson, endPoint);
                                }

                            } catch (JSONException exception) {
                                Log.e(TAG, "run: " + exception);
                            }
                        }
                    });

// ans for new paticipant
                    if (message.has(CONSTANT_OFFER)) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    commandMap.get(CONSTANT_OFFER).execute(mainJson.getString("sender"), mainJson);
                                } catch (JSONException mE) {
                                    mE.printStackTrace();
                                }
                            }
                        });

                    }
//
                    if (message.has(CONSTANT_SEND_USER_PREF)) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    commandMap.get(CONSTANT_SEND_USER_PREF).execute(roomId, message);
                                } catch (JSONException mE) {
                                    mE.printStackTrace();
                                }
                            }
                        });

                    }
//                    ans
                    if (message.has("type") && message.getString("type").equals("offer")) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    commandMap.get(CONSTANT_ANSWER).execute(mainJson.getString("sender"), message);
                                } catch (JSONException mE) {
                                    mE.printStackTrace();
                                }
                            }
                        });

                    }
//                     aad ice
                    if (message.has(CONSTANT_CANDIDATE)) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    commandMap.get(CONSTANT_CANDIDATE).execute(mainJson.getString("sender"), message);
                                } catch (JSONException mE) {
                                    mE.printStackTrace();
                                }
                            }
                        });

                    }
//                    offer
                    if (message.has("type") && message.getString("type").equals("answer")) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    commandMap.get(CONSTANT_ANSWER).execute(mainJson.getString("sender"), message);
                                } catch (JSONException mE) {
                                    mE.printStackTrace();
                                }
                            }
                        });

                    }

                } catch (JSONException e) {
                    Log.i(TAG, "call: " + e.toString());
                }
            }
        };
        private Emitter.Listener onConntect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                if (join) {
                    joinNew();
                }
            }
        };

        private Emitter.Listener onError = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "call: "+args[0] );
            }
        };

        private MessageHandler() {
            this.commandMap = new HashMap<>();
            commandMap.put(CONSTANT_OFFER, new CreateOfferCommand());
            commandMap.put(CONSTANT_ANSWER, new CreateAnswerCommand());
            commandMap.put(CONSTANT_SEND_USER_PREF, new SetRemoteSDPCommand());
            commandMap.put(CONSTANT_CANDIDATE, new AddIceCandidateCommand());
        }
    }

    private class CreateOfferCommand implements Command {

        public void execute(String peerId, JSONObject message) throws JSONException {

            Log.d(TAG, "CreateOfferCommand");
            Peer peer = peers.get(peerId);
            peer.pc.createOffer(peer, localVideoView.getMediaConstraint());

        }
    }

    private class CreateAnswerCommand implements RtcMultiConnectionClient.Command {
        public void execute(String peerId, JSONObject mainJson) throws JSONException {
            Log.d(TAG, "CreateAnswerCommand");
            RtcMultiConnectionClient.Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(mainJson.getString("type")), mainJson.getString("sdp"));
            peer.pc.setRemoteDescription(peer, sdp);
            if (sdp.type == SessionDescription.Type.OFFER)
                peer.pc.createAnswer(peer, localVideoView.getMediaConstraint());

        }
    }

    private class SetRemoteSDPCommand implements Command {
        public void execute(String roomId, JSONObject message) throws JSONException {
            sendUserPreferences(roomId, message);
        }
    }

    private class AddIceCandidateCommand implements Command {
        public void execute(String peerId, JSONObject message) throws JSONException {
            Log.d(TAG, "AddIceCandidateCommand");
            PeerConnection pc = peers.get(peerId).pc;
            //  if (pc.getRemoteDescription() != null) {
            Log.i(TAG, "execute: add ice candidate");
            IceCandidate candidate = new IceCandidate(
                    message.getString("sdpMid"),
                    message.getInt("sdpMLineIndex"),
                    message.getString("candidate"));

            pc.addIceCandidate(candidate);
            //  }
        }
    }

    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String id;
        private int endPoint;
        private JSONObject connectionDescription;

        public Peer(String id, int endPoint, JSONObject connectionDescription) {
            this.pc = factory.createPeerConnection(iceServers, localVideoView.getMediaConstraint(), this);

            this.id = id;
            this.endPoint = endPoint;
            this.connectionDescription = connectionDescription;
            pc.addStream(localVideoView.getLocalStream()); //, new MediaConstraints()

        }


        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

            Log.i(TAG, "onSignalingChange: ");

            switch (signalingState) {
                case CLOSED:
                    Log.i(TAG, "onSignalingChange: close");
                    break;
                case HAVE_LOCAL_OFFER:
                    Log.i(TAG, "onSignalingChange: local offer");
                    break;
                case HAVE_LOCAL_PRANSWER:

                    break;
                case HAVE_REMOTE_OFFER:
                    Log.i(TAG, "onSignalingChange: remote offer");
                    break;
                case HAVE_REMOTE_PRANSWER:
                    break;
                case STABLE:
                    Log.i(TAG, "onSignalingChange: stable");
                    break;
                default:

            }


        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i(TAG, "onIceConnectionChange: ");
            /*if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED)
            {

            }*/

            switch (iceConnectionState) {

                case NEW:
                    Log.i(TAG, "onIceConnectionChange: new");
                    break;
                case CLOSED:
                    Log.i(TAG, "onIceConnectionChange: closed");
                    break;
                case FAILED:
                    Log.i(TAG, "onIceConnectionChange: Failed");
                    break;
                case CHECKING:
                    Log.i(TAG, "onIceConnectionChange: checking");
                    break;

                case DISCONNECTED:
                    callback.onRemoveStream(endPoint + 1);
                    removePeer(id);
                    Log.i(TAG, "onIceConnectionChange: disconnected");
                    break;
                case COMPLETED:
                    Log.i(TAG, "onIceConnectionChange: completed");
                    break;
                case CONNECTED:
                    Log.i(TAG, "onIceConnectionChange: connected");
                    break;
                default:
                    Log.i(TAG, "onIceConnectionChange: Nahi hai :)");


            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i(TAG, "onIceConnectionReceivingChange: ");
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i(TAG, "onIceGatheringChange: ");
//            iceGatheringState == PeerConnection.IceGatheringState.GATHERING;

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i(TAG, "onIceCandidate(sdp): " + iceCandidate.sdp);
            Log.i(TAG, "onIceCandidate: (sdpMid)" + iceCandidate.sdpMid);
            Log.i(TAG, "onIceCandidate: (sdpMLineIndex)" + iceCandidate.sdpMLineIndex);
            JSONObject json = new JSONObject();
            try {

                json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                json.put("sdpMid", iceCandidate.sdpMid);
                json.put("candidate", iceCandidate.sdp);
                sendMessage(id, json);

            } catch (JSONException e) {

            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Log.i(TAG, "onIceCandidatesRemoved: ");
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.i(TAG, "onAddStream: ");
            callback.onAddStream(mediaStream, endPoint + 1);

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i(TAG, "onRemoveStream: ");

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i(TAG, "onDataChannel: ");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i(TAG, "onRenegotiationNeeded: ");
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            Log.i(TAG, "onAddTrack: ");
        }

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
        Log.i(TAG, "onCreateSuccess:   offer sdp " + sdp);
                    JSONObject message = new JSONObject();
                    try {
                        JSONObject remotePeerSdpContraints = new JSONObject();
                        JSONObject extra = new JSONObject();
                        remotePeerSdpContraints.put("OfferToReceiveAudio", true);
                        remotePeerSdpContraints.put("OfferToReceiveVideo", true);
                        message.put("type", sdp.type.canonicalForm());
                        message.put("sdp", sdp.description);
                        message.put("remotePeerSdpConstraints", remotePeerSdpContraints);
                        message.put("renegotiatingPeer", false);
                        message.put("connectionDescription", connectionDescription);
                        message.put("dontGetRemoteStream", false);
                        message.put("extra", extra);
                        JSONObject streamsToShare = new JSONObject();
                        JSONObject jsonObject = new JSONObject();
                        streamsToShare.put("isAudio", false);
                        streamsToShare.put("isVideo", true);
                        streamsToShare.put("isScreen", false);
                        jsonObject.put("streamId", streamsToShare);
                        message.put("streamsToShare", jsonObject);
                        message.put("isFirefoxOffered", true);
                        sendMessage(id, message);

                    } catch (JSONException e) {
                        Log.e(TAG, "onCreateSuccess: SDP " + e);
                    }
                    pc.setLocalDescription(Peer.this, sdp);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            Log.i(TAG, "onSetSuccess: ");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure: " + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure: " + s);
        }
    }
}
