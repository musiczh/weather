package com.example.a17280.whether.util;

import com.example.a17280.whether.Interface.HttpCallBackListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 17280 on 2019/5/7.
 */

public class InternetUtil {



    public static void sendHttpURL(final String address, final HttpCallBackListener callBackListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (callBackListener != null) {
                        //回调onFinish函数
                        callBackListener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    if(callBackListener!=null){
                        //回调onError方法
                        callBackListener.onError(e);
                    }
                }finally {
                    if (connection!=null){
                        connection.disconnect();
                    }
                }

            }
        }).start();

    }
}
