package com.example.evernymfluttertest;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.getkeepsafe.relinker.ReLinker;

import org.json.JSONArray;
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
        Log.d(TAG, "configureFlutterEngine");
        // Init the sdkApi
        sdkApi = new ConnectMeVcx(this);
        sdkApi.init();
        Log.d(TAG, "isNull configureFlutterEngine" + (sdkApi==null));

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            String url = call.argument("url");
                            Log.d(TAG,"URL::"+url);
                            if (call.method.equals("test_method")) {
                                //result.success("done");
                                addConnectionOnClick(url, result);
                            } else if (call.method.equals("test_vcx_method")) {
                                getVcxMessage(result);
                            } else {
                                result.notImplemented();
                            }
                        }
                );

    }

    private void addDone(String url, MethodChannel.Result result) {
        result.success(url);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.d(TAG, "OnCreate");
        // Init the sdkApi
        sdkApi = new ConnectMeVcx(this);
        sdkApi.init();
        Log.d(TAG, "isNull onCreate" + (sdkApi==null));

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

    public void getVcxMessage(MethodChannel.Result res) {
        String messageStatus = "MS-103,MS-101,MS-102,MS-104,MS-105";
        try {
            UtilsApi.vcxGetMessages(messageStatus, null, null).exceptionally((t) -> {
                Log.e(TAG, "vcxGetMessages: ", t);
                return null;
            }).thenAccept(result -> {
                Log.d(TAG, "result vcxGetMessages JAVA:: " + result);
                //Log.d(TAG,"Length of result before:: "+result.length() +".");

                result = result.replace("\\","");
                result = result.replace("\"{","{");
                result = result.replace("}\"","}");
                result = result.replace("\"[","[");
                result = result.replace("]\"","]");

                // Log.d(TAG,"Length of result after:: "+result.length() +".");
                // Log.d(TAG,"3000th character to last character of result:: "+result.substring(3000,result.length()));
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        Log.d("JSON obj @"+i+"::", explrObject.toString()+"!");
                        Log.d("pairwiseDID",explrObject.getString("pairwiseDID"));
                        Log.d("pairwiseDID",
                                String.valueOf(explrObject.getJSONArray("msgs")));

                        JSONArray msgs = explrObject.getJSONArray("msgs");
                        for(int j = 0;j<msgs.length(); j++) {
                            JSONObject msgObject = msgs.getJSONObject(j);
                            Log.d("msgs @"+j+"::", msgObject.toString()+"!");
                            Log.d("msg attr statusCode",msgObject.getString("statusCode"));
                            Log.d("msg attr payload",msgObject.getString("payload")+"!");

                            if(j==2) {
                                Log.d("msgs",msgObject.get("decryptedPayload")+"!");
                                Log.d("msgs",msgObject.getString("decryptedPayload")+"!");
                                JSONObject decrypt = new JSONObject(msgObject.getString("decryptedPayload"));
                                Log.d("decrypt",decrypt.getString("@type"));
                                Log.d("decrypt",decrypt.getString("@msg"));
                            }
                        }
                    }
                }catch (JSONException err){ //4233
                    Log.d("Error", err.toString()/*.substring(err.toString().length()-1500,err.toString().length())*/);
                }

                res.success(result);
            });
        } catch (VcxException e) {
            e.printStackTrace();
        }
    }
}
