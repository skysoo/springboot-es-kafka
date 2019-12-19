package com.elastic.config;

import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오후 4:05
 **/
@Configuration
public class EsLowConfiguration {
    private final InitHttpsIgnore initHttpsIgnore;

    public EsLowConfiguration(InitHttpsIgnore initHttpsIgnore) {
        this.initHttpsIgnore = initHttpsIgnore;
    }

    public void get(String strUrl) {
        try {
            initHttpsIgnore.initializeHttpConnection();

            HttpURLConnection con = getConn(strUrl, "GET");
            System.out.println(">>> success get's connection get ");

            StringBuilder sb = getResponse(con);
            System.out.println(sb.toString());

            con.disconnect();
            System.out.println("connection is closed");

        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
        }

    }

    public void put(String strUrl, String jsonMessage) {
        try {
            HttpURLConnection con = getConn(strUrl, "PUT");
            System.out.println(">>> success put's connection get ");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(jsonMessage); //json 형식의 message 전달
            wr.flush();

            StringBuilder sb = getResponse(con);
            System.out.println(sb.toString());

            con.disconnect();
            System.out.println("connection is closed");

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public StringBuilder getResponse(HttpURLConnection con) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb;
        } else {
            System.out.println("error is " + con.getResponseCode());
            return null;
        }
    }

    public HttpURLConnection getConn(String strUrl, String method) throws IOException {
        HttpURLConnection con = null;
        try {
            URL url = new URL(strUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestMethod(method);
//            System.out.println("#1 "+con.getResponseCode());

            if (method.equals("GET")) {
                con.setDoOutput(false);
            } else if (method.equals("PUT")) {
                //json으로 message를 전달하고자 할 때
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoInput(true);
                con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정
                con.setUseCaches(false);
                con.setDefaultUseCaches(false);
            } else {
                System.out.println("Don't used the Connection Method! >>> " + method);
            }
        } catch (IOException e) {
            System.out.println("url = " + strUrl + " / method = " + method);
            e.printStackTrace();
        }
        return con;
    }
}
