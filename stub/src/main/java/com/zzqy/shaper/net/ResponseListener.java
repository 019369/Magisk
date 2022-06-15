package com.zzqy.shaper.net;

public interface ResponseListener<T> {
    void onResponse(T response);
}
