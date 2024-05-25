package com.ztech.share;

public interface ListenerProgress{
    void updateSendProgress(long size);
    void updateReceiveProgress(long size);
}
