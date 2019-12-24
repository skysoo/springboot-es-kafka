package com.elastic.config;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
public class HUCConfiguration {
    private final InitHttpsIgnore initHttpsIgnore;

    public HUCConfiguration(InitHttpsIgnore initHttpsIgnore) {
        this.initHttpsIgnore = initHttpsIgnore;
    }

    public boolean get(String strUrl, String method) {
        boolean result = false;
        try {
            initHttpsIgnore.initializeHttpConnection();

            HttpURLConnection con = getConn(strUrl, "GET");
            log.debug(">>> success get's connection get ");

            StringBuilder sb = getResponse(con);
            if (sb == null) {
                log.warn(">>> {} get's response is null", strUrl);
                return false;
            }

            if (method.equals("search")) {
                log.info("##### searched doc length is {}", sb.length());
            } else if (method.equals("count")) {
                log.info("##### searched doc count is {}", sb.length());
            }
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) result = true;

            con.disconnect();
            log.debug("connection is closed");

        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            log.error("", e);
        }
        return result;
    }

    public boolean put(String strUrl, String jsonMessage) {
        boolean result = false;
        try {
            HttpURLConnection con = getConn(strUrl, "PUT");
            log.debug(">>> success put's connection get ");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(jsonMessage); //json 형식의 message 전달
            wr.flush();

            StringBuilder sb = getResponse(con);
            if (sb == null) {
                log.warn(">>> {} put's response is null", strUrl);
                return false;
            }

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) result = true;

            con.disconnect();
            log.debug("connection is closed");

        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    public boolean post(String strUrl, String jsonMessage) {
        boolean result = false;
        try {
            HttpURLConnection con = getConn(strUrl, "POST");
            log.debug(">>> success post's connection get ");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(jsonMessage); //json 형식의 message 전달
            wr.flush();

            StringBuilder sb = getResponse(con);
            if (sb == null) {
                log.warn(">>> {} post's response is null", strUrl);
                return false;
            }

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK ||
                    con.getResponseCode() == HttpURLConnection.HTTP_CREATED) result = true;

            con.disconnect();
            log.debug("connection is closed");

        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    public boolean delete(String strUrl) {
        boolean result = false;
        try {
            HttpURLConnection con = getConn(strUrl, "DELETE");
            log.debug(">>> success delete's connection get ");

            StringBuilder sb = getResponse(con);
            log.debug(sb.toString());

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) result = true;

            con.disconnect();
            log.debug("connection is closed");

        } catch (IOException e) {
            log.error("", e);
        }
        return result;
    }

    public StringBuilder getResponse(HttpURLConnection con) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK ||
                con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                br.close();
                log.debug("##### Response is Normal");
            } catch (IOException e) {
                log.error("", e);
            }
            return sb;
        } else {
            log.error("error is " + con.getResponseCode());
            return null;
        }
    }

    public HttpURLConnection getConn(String strUrl, String method) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(strUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(15000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(30000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestMethod(method);

            switch (method) {
                case "GET":
                    con.setDoOutput(false);
                    break;
                case "DELETE":
                    con.setDoOutput(true);
                    break;
                case "PUT":
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoInput(true);
                    con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정
                    con.setUseCaches(false);
                    con.setDefaultUseCaches(false);
                    break;
                case "POST":
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoInput(true);
                    con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정
                    con.setUseCaches(false);
                    con.setDefaultUseCaches(false);
                    break;
                default:
                    log.warn("Don't used the Connection Method! {} >>> ", method);
                    break;
            }

        } catch (IOException e) {
            log.error("url = " + strUrl + " / method = " + method);
            log.error("", e);
        }
        if (con == null)
            log.warn(">>> failed {}'s connection get ", method);
        return con;
    }
}
