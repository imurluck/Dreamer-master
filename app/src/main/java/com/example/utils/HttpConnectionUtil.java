package com.example.utils;

/**
 * Created by 10405 on 2016/6/6.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Using singleton pattern.
 * To make sure that only one thread can connect the server.
 */
public class HttpConnectionUtil {

    private static HttpConnectionUtil httpConnectionUtilInstance = new HttpConnectionUtil();
    private HttpConnectionUtil() {}

    public static HttpConnectionUtil getHttpConnectionUtilInstance() {
        return httpConnectionUtilInstance;
    }

    private static final String GET = "GET";
    private static final String SERVERURL = "http://www.dreamera.net:8000/cross/";
    static final String PLACEURL = SERVERURL + "place/";
    private String jsonString; //jsong string return from our server
    private boolean status; //if connection success

    /**
     * Set http connection status
     */
    private void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * Get status,judge if connection success
     */
    public boolean getStatus() {
        return status;
    }


    /**
     * Set json String
     */
    private void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    /**
     * Get json String
     */
    public String getJsonString() {
        return jsonString;
    }


    void doGet() throws Exception {
        URL url = new URL(PLACEURL);
        System.out.println(url.getPath());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(GET);
        conn.setRequestProperty("Content-Type","text/html; charset=UTF-8");
        conn.setRequestProperty("Accept-Encoding", "gzip"); // 用gzip压缩数据
        conn.connect();
        if (conn.getResponseCode() != 200) {
            setStatus(false);
            System.out.println("asdf code == 200");
        } else {
            setStatus(true);
            String encoding = conn.getContentEncoding();
            changeInputToString(conn, encoding);
            System.out.println("asdf code = " + conn.getResponseCode());
        }
    }


    /**
     * Change InputStream to string
     * @param conn HttUrlConnection
     * @throws Exception
     */
    private void changeInputToString(HttpURLConnection conn, String encoding) throws Exception {
        InputStream ism = conn.getInputStream();
        if(encoding != null && encoding.contains("gzip")) {
            System.out.println("asdf gzip 解压啦");
            ism = new GZIPInputStream(conn.getInputStream());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(ism));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        setJsonString(sb.toString());
        br.close();
    }
}
