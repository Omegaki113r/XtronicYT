package com.omegaki113r.xtronic.hm10_custom_android;

public interface BLECallback {

    void sendMessages(String _message);
    void recievedMessage(String _message);
    void callbackSet(BLECallback callback);
}
