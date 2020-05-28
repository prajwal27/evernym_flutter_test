package com.example.evernymfluttertest;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.getkeepsafe.relinker.ReLinker;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    public static final String TAG = "MainActivity";
    private ConnectMeVcx sdkApi;
    private static final String CHANNEL = "test_channel";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Log.d(TAG, "OnCreate");
        // Init the sdkApi
        sdkApi = new ConnectMeVcx(this);
        sdkApi.init();
        Log.d(TAG, "IS" + (sdkApi==null));

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            String url = call.argument("url");
                            if (call.method.equals("test_method")) {
                                //result.success("done");
                                addConnectionOnClick(url, result);
                            } else {
                                result.notImplemented();
                            }
                        }
                );

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.d(TAG, "OnCreate");
        // Init the sdkApi
        sdkApi = new ConnectMeVcx(this);
        sdkApi.init();
        Log.d(TAG, "IS" + (sdkApi==null));

        /*new MethodChannel(getFlutterView(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        String url = call.argument("url");
                        if (call.method.equals("test_method")) {
                            result.success("done");
                            //openBrowser(call, result, url);
                        } else {
                            result.notImplemented();
                        }
                    }
                }
        );*/
    }


    public void addConnectionOnClick(String scannedQR, MethodChannel.Result result) {
        String invitationDetails = scannedQR;
        Log.d(TAG, "connection invitation is set to: " + invitationDetails);

        try {
            JSONObject json = new JSONObject(invitationDetails);
            sdkApi.createConnectionWithInvite(json.getString("id"), invitationDetails, new CompletableFuturePromise<>(connectionHandle -> {
                Log.e(TAG, "createConnectionWithInvite return code is: " + connectionHandle);
                if(connectionHandle != -1) {
                    sdkApi.vcxAcceptInvitation(connectionHandle, "{\"connection_type\":\"QR\",\"phone\":\"\"}", new CompletableFuturePromise<>(inviteDetails -> {
                        Log.e(TAG, "vcxAcceptInvitation return code is: " + inviteDetails);
                        if(invitationDetails != null) {
                            sdkApi.getSerializedConnection(connectionHandle, new CompletableFuturePromise<>(state -> {
                                Log.e(TAG, "getSerializedConnection returned state is: " + state);
                                result.success(state);
                            }, (t) -> {
                                Log.e(TAG, "getSerializedConnection error is: ", t);
                                result.error(TAG, "getSerializedConnection error" , t);
                                return null;
                            }));
                        }
                    }, (t) -> {
                        Log.e(TAG, "vcxAcceptInvitation error is: ", t);
                        result.error(TAG, "vcxAcceptInvitation error" , t);
                        return null;
                    }));
                }
            }, (t) -> {
                Log.e(TAG, "createConnectionWithInvite error is: ", t);
                result.error(TAG, "createConnecWithInvite error" , t);
                return -1;
            }));
        } catch (JSONException e) {
            e.printStackTrace();
            result.error("catch",null,e);
        }
    }
}
