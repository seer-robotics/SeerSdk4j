package com.seer.sdk.demo;

import com.seer.sdk.rbk.RbkClient;
import com.seer.sdk.rbk.RbkResult;
import com.seer.sdk.rbk.RbkResultKind;
import org.json.JSONObject;

public class RbkClientDemo {

    public static void main(String[] args) {
        // TODO 请求 1110 查询机器人当前电量
        //第一步: 建立与机器人连接的客户端
        RbkClient rbkClient = new RbkClient("192.168.8.114");
        //第二步：发送查询机器人电量的请求，根据 rbk 协议查询机器人电量的请求编号是 1007，请求参数 {"simple":true}。
        String reqStr = "{\"simple\": true}"; // 请求参数时 json 字符串
        RbkResult result = rbkClient.request(1007, reqStr, 10000);
        if (RbkResultKind.Ok.equals(result.getKind())) {  // sdk 请求机器人成功
            String resStr = result.getResStr();         // 获取机器人返回的信息
            JSONObject resJson = new JSONObject(resStr);
            if (resJson.getInt("ret_code") == 0) {    // 机器人返回成功
                float batteryLevel = resJson.getFloat("battery_level");// 获取电量
            } else {                                     // 机器人返回失败的原因
                String robotErrMsg = resJson.getString("err_msg");
            }
        } else {                                          // sdk 请求请求机器人失败原因
            String sdkErrMsg = result.getErrMsg();
        }
        //第三步：客户端不在使用时，释放客户端连接
        rbkClient.dispose();//释放连接资源
    }
}
