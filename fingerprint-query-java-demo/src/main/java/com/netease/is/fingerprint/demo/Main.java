/*
 * @(#) Main.java 2022-03-21
 *
 * Copyright 2022 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.fingerprint.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.is.fingerprint.demo.utils.HttpClient4Utils;
import com.netease.is.fingerprint.demo.utils.ParamUtils;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 2022-03-21
 */
public class Main {

    private static final String URI_SEND_FINGERPRINT = "https://fp-query.dun.163.com/v1/device/query";

    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);


    /**
     * SECRET_ID 和 SECRET_KEY 是产品密钥。可以登录易盾官网找到自己的凭证信息。请妥善保管，避免泄露。
     * BUSINESS_ID 为易盾官网申请的应用id
     */
    private static final String SECRET_ID = "your secret id";
    private static final String SECRET_KEY = "your secret key";
    private static final String BUSINESS_ID = "your business id";
    private static final String TOKEN = "your token";


    public static void main(String[] args) throws Exception {
        Map<String, String> requestParams = createRequestParams();
        String res = HttpClient4Utils.sendPost(httpClient, URI_SEND_FINGERPRINT, requestParams);
        JSONObject resObj = JSON.parseObject(res);
        if (resObj.getInteger("code") == 200) {
            System.out.println("请求成功");
        }
    }


    private static Map<String, String> createRequestParams() {
        Map<String, String> params = new HashMap<>(7);
        params.put("nonce", ParamUtils.createNonce());
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "v1");
        params.put("token", TOKEN);
        params.put("businessId", BUSINESS_ID);
        params.put("secretId", SECRET_ID);
        //参数赋值完成之后，最后生成签名
        params.put("signature", ParamUtils.genSignature(SECRET_KEY, params));
        return params;
    }


}
