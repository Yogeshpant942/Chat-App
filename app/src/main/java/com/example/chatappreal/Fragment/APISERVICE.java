package com.example.chatappreal.Fragment;

import com.example.chatappreal.Notification.MyResponse;
import com.example.chatappreal.Notification.sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface APISERVICE {

    @Header({
            "Content-Type:application/json",
            "Authorization:key= "             //paste server key from firebase cloud messagin console
    })

    @POST("fcm/send")
    Call<MyResponse>sendNotification(@Body sender body);
}
