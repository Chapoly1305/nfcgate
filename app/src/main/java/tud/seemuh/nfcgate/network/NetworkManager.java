package tud.seemuh.nfcgate.network;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManagerFix;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.util.NfcComm;

import static tud.seemuh.nfcgate.network.c2s.C2S.ServerData.Opcode;

public class NetworkManager implements ServerConnection.Callback {
    public interface Callback {
        void onReceive(NfcComm data);
        void onNetworkStatus(NetworkStatus status);
    }

    // references
    private MainActivity mActivity;
    private ServerConnection mConnection;
    private Callback mCallback;

    // preference data
    private String mHostname;
    private int mPort, mSessionNumber;

    public NetworkManager(MainActivity activity, Callback cb) {
        mActivity = activity;
        mCallback = cb;
    }

    public void connect() {
        // read fresh preference data
        getPreferenceData();

        // establish connection
        mConnection = new ServerConnection(mHostname, mPort)
                .setCallback(this)
                .connect();

        // queue initial handshake message
        sendServer(Opcode.OP_SYN, null);
    }

    public void disconnect() {
        if (mConnection != null)
            mConnection.disconnect();
    }

    public void send(NfcComm data) {
        // queue data message
        sendServer(Opcode.OP_PSH, data.toByteArray());
        // waiting for partner
        onNetworkStatus(NetworkStatus.PARTNER_WAIT);
    }

    @Override
    public void onReceive(byte[] data) {
        C2S.ServerData serverData = null;
        try {
            serverData = C2S.ServerData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        switch (serverData.getOpcode()) {
            case OP_SYN:
                // empty syn message indicates our peer has just connected
                onNetworkStatus(NetworkStatus.PARTNER_CONNECT);
                // return ack
                sendServer(Opcode.OP_ACK, null);

                break;
            case OP_ACK:
                // empty ack message indicates our peer was already connected
                onNetworkStatus(NetworkStatus.PARTNER_CONNECT);

                break;
            case OP_FIN:
                // our peer has disconnected
                onNetworkStatus(NetworkStatus.PARTNER_LEFT);
                // TODO: disconnect

                break;
            case OP_PSH:
                // pass data to callback
                mCallback.onReceive(new NfcComm(serverData.getData().toByteArray()));

                break;
        }
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        mCallback.onNetworkStatus(status);
    }

    private void getPreferenceData() {
        // read data from shared prefs
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(mActivity);
        mHostname = prefs.getString("host", null);
        mPort = Integer.parseInt(prefs.getString("port", "0"));
        mSessionNumber = Integer.parseInt(prefs.getString("session", "0"));

        Log.d("nfcgate", "Connecting to: " + mHostname + ":" + mPort + " in session " + mSessionNumber);

        // TODO: verify / sanitize inputs?
    }

    private void sendServer(Opcode opcode, byte[] data) {
        mConnection.send(mSessionNumber,
                C2S.ServerData.newBuilder()
                    .setOpcode(opcode)
                    .setData(data == null ? ByteString.EMPTY : ByteString.copyFrom(data))
                    .build()
                    .toByteArray());
    }
}
