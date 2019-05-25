package com.example.a17280.whether.Interface;

/**
 * Created by 17280 on 2019/5/8.
 * HttpURL网络访问回调接口
 */

public interface HttpCallBackListener {
    void onFinish(String response);
    void onError(Exception e);
}
