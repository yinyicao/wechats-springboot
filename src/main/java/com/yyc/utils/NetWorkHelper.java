package com.yyc.utils;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 访问网络用到的工具类
 */
public class NetWorkHelper {

    /**
     * 发起Https请求
     * @param reqUrl 请求的URL地址
     * @param requestMethod
     * @return 响应后的字符串
     * getHttpsResponse方法是请求一个https地址，
     * 参数requestMethod为字符串“GET”或者“POST”，传null或者“”默认为get方式。
     */
    public String getHttpsResponse(String reqUrl, String requestMethod) {
        URL url;
        InputStream is;
        String resultData = "";
        try {
            url = new URL(reqUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            TrustManager[] tm = {xtm};

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);

            con.setSSLSocketFactory(ctx.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

            //允许输入流，即允许下载
            con.setDoInput(true);

            //在android中必须将此项设置为false
            //允许输出流，即允许上传
            con.setDoOutput(false);
            //不使用缓冲
            con.setUseCaches(false);
            if (null != requestMethod && !requestMethod.equals("")) {
                //使用指定的方式
                con.setRequestMethod(requestMethod);
            } else {
                //使用get请求
                con.setRequestMethod("GET");
            }
            //获取输入流，此时才真正建立链接
            is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = bufferReader.readLine()) != null) {
                resultData += inputLine + "\n";
            }
            System.out.println(resultData);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData;
    }

    X509TrustManager xtm = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {

        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {

        }
    };
}
