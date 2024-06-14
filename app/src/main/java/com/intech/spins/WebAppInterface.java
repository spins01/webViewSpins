package com.intech.spins;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class WebAppInterface {
    private Context mContext;

    WebAppInterface(Context context) {
        mContext = context;
    }

    // 处理来自 JavaScript 的方法调用
    @JavascriptInterface
    public void appInvoke(String paramJson) {
        // 在这里处理传递过来的 JSON 字符串
        // 例如，可以将其解析为对象
        try {
            JSONObject jsonObject = new JSONObject(paramJson);
            String requestId = jsonObject.getString("requestId");
            String service = jsonObject.getString("service");
            String method = jsonObject.getString("method");
            String data = jsonObject.getString("data");
            Log.i("关羽","requestID:"+requestId+"  service:"+service+"   method:"+method+"    data:"+data);
            JSONObject urlJsonObject = new JSONObject(data);
            String innerUrl = urlJsonObject.getString("url");
            Intent innerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(innerUrl));
            mContext.startActivity(innerIntent);
            // 处理你的业务逻辑
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
