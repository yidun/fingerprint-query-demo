/*
 * @(#) HttpClient4Utils.java 2022-03-21
 *
 * Copyright 2022 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.fingerprint.demo.utils;

import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

/**
 * @author chenxing03
 * @version 2022-03-21
 */
public class HttpClient4Utils {

    /**
     * 实例化HttpClient
     *
     * @param maxTotal
     * @param maxPerRoute
     * @param socketTimeout
     * @param connectTimeout
     * @param connectionRequestTimeout
     * @return
     */
    public static HttpClient createHttpClient(int maxTotal, int maxPerRoute, int socketTimeout, int connectTimeout,
                                              int connectionRequestTimeout) {
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(defaultRequestConfig).build();
        return httpClient;
    }

    /**
     * 发送post请求
     *
     * @param httpClient httpClient
     * @param url        请求地址
     * @param params     请求参数
     * @return
     * @throws Exception 发送请求异常
     */
    public static String sendPost(HttpClient httpClient, String url, Map<String, String> params) throws Exception {
        String encoding = Consts.UTF_8.name();
        String resp = "";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        if (params != null && params.size() > 0) {
            String json = JSON.toJSONString(params);
            StringEntity stringEntity = new StringEntity(json, ContentType.create("application/json", encoding));
            httpPost.setEntity(stringEntity);
        }
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                String msg = String.format("接口返回状态码异常, status=%d, reponse=", status, EntityUtils.toString(response.getEntity(), encoding));
                System.out.println(msg);
            }
            resp = EntityUtils.toString(response.getEntity(), encoding);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // log
                    e.printStackTrace();
                }
            }
        }
        return resp;
    }
}
