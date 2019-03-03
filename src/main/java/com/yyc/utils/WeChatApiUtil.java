package com.yyc.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yyc.common.AccessTokenInfo;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * 素材上传方法（运行该类的main方法）
 *
 * 通过微信公众号平台提供的素材管理接口将图片,语音,视频上传到微信服务器上,
 * 上传成功后
 * 微信服务器会给我们返回一个mediaId
 * 用于标识上传成功的多媒体素材
 */
public class WeChatApiUtil {

    //测试号信息
    private static final String APP_ID="wx4917e7c64116c8d5";
    private static final String APP_SECRET="44c86c79f2eb1c1a958625af2d5376a5";

    // token 接口(GET)
    private static final String ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    // 素材上传(POST)https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE
    private static final String UPLOAD_MEDIA = "https://api.weixin.qq.com/cgi-bin/media/upload";
    // 素材下载:不支持视频文件的下载(GET)
    private static final String DOWNLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";

    public static String getTokenUrl(String appId, String appSecret) {
        return String.format(ACCESS_TOKEN, appId, appSecret);
    }

    public static String getDownloadUrl(String token, String mediaId) {
        return String.format(DOWNLOAD_MEDIA, token, mediaId);
    }

    /**
     * 通用接口获取Token凭证
     *
     * @param appId
     * @param appSecret
     * @return
     */
    public static String getToken(String appId, String appSecret) {
        if (appId == null || appSecret == null) {
            return null;
        }

        String token = null;
        String tockenUrl = WeChatApiUtil.getTokenUrl(appId, appSecret);
        String response = httpsRequestToString(tockenUrl, "GET", null);
        JSONObject jsonObject = JSON.parseObject(response);
        if (null != jsonObject) {
            try {
                token = jsonObject.getString("access_token");
            } catch (JSONException e) {
                token = null;// 获取token失败
            }
        }
        return token;
    }

    /**
     * 微信服务器素材上传
     *
     * @param file  表单名称media
     * @param token access_token
     * @param type  type只支持四种类型素材(video/image/voice/thumb)
     */
    public static JSONObject uploadMedia(File file, String token, String type) {
        if (file == null || token == null || type == null) {
            return null;
        }

        if (!file.exists()) {
            System.out.println("上传文件不存在,请检查!");
            return null;
        }

        String url = UPLOAD_MEDIA;
        JSONObject jsonObject = null;
        PostMethod post = new PostMethod(url);
        post.setRequestHeader("Connection", "Keep-Alive");
        post.setRequestHeader("Cache-Control", "no-cache");
        FilePart media;
        HttpClient httpClient = new HttpClient();
        //信任任何类型的证书
        Protocol myhttps = new Protocol("https", new SSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", myhttps);

        try {
            media = new FilePart("media", file);
            Part[] parts = new Part[]{new StringPart("access_token", token),
                    new StringPart("type", type), media};
            MultipartRequestEntity entity = new MultipartRequestEntity(parts,
                    post.getParams());
            post.setRequestEntity(entity);
            int status = httpClient.executeMethod(post);
            if (status == HttpStatus.SC_OK) {
                String text = post.getResponseBodyAsString();
                jsonObject = JSONObject.parseObject(text);
            } else {
                System.out.println("upload Media failure status is:" + status);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 多媒体下载接口
     *
     * @param fileName 素材存储文件路径
     * @param token    认证token
     * @param mediaId  素材ID（对应上传后获取到的ID）
     * @return 素材文件
     * @comment 不支持视频文件的下载
     */
    public static File downloadMedia(String fileName, String token,
                                     String mediaId) {
        String url = getDownloadUrl(token, mediaId);
        return httpRequestToFile(fileName, url, "GET", null);
    }

    /**
     * 多媒体下载接口
     *
     * @param fileName 素材存储文件路径
     * @param mediaId  素材ID（对应上传后获取到的ID）
     * @return 素材文件
     * @comment 不支持视频文件的下载
     */
    public static File downloadMedia(String fileName, String mediaId) {
        String token = WeChatApiUtil.getToken(APP_ID, APP_SECRET);
        return downloadMedia(fileName,token,mediaId);
    }

    /**
     * 以http方式发送请求,并将请求响应内容输出到文件
     *
     * @param path   请求路径
     * @param method 请求方法
     * @param body   请求数据
     * @return 返回响应的存储到文件
     */
    public static File httpRequestToFile(String fileName, String path, String method, String body) {
        if (fileName == null || path == null || method == null) {
            return null;
        }

        File file = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream fileOut = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(method);
            if (null != body) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.getBytes("UTF-8"));
                outputStream.close();
            }

            inputStream = conn.getInputStream();
            if (inputStream != null) {
                file = new File(fileName);
            } else {
                return file;
            }

            //写入到文件
            fileOut = new FileOutputStream(file);
            if (fileOut != null) {
                int c = inputStream.read();
                while (c != -1) {
                    fileOut.write(c);
                    c = inputStream.read();
                }
            }
        } catch (Exception e) {
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            /*
             * 必须关闭文件流
             * 否则JDK运行时，文件被占用其他进程无法访问
             */
            try {
                inputStream.close();
                fileOut.close();
            } catch (IOException execption) {
            }
        }
        return file;
    }

    /**
     * 上传素材
     * @param filePath 媒体文件路径(绝对路径)
     * @param type 媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @return
     */
    public static JSONObject uploadMedia(String filePath,String type){
        // 获取本地文件
        File f = new File(filePath);
        String token = WeChatApiUtil.getToken(APP_ID, APP_SECRET);
        JSONObject jsonObject = uploadMedia(f, token, type);
        return jsonObject;
    }

    /**
     * 发送请求以https方式发送请求并将请求响应内容以String方式返回
     *
     * @param path   请求路径
     * @param method 请求方法
     * @param body   请求数据体
     * @return 请求响应内容转换成字符串信息
     */
    public static String httpsRequestToString(String path, String method, String body) {
        if (path == null || method == null) {
            return null;
        }

        String response = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        HttpsURLConnection conn = null;
        try {
            TrustManager[] tm = {new JEEWeiXinX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            System.out.println(path);
            URL url = new URL(path);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(method);
            if (null != body) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.getBytes("UTF-8"));
                outputStream.close();
            }

            inputStream = conn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            response = buffer.toString();
        } catch (Exception e) {

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
            } catch (IOException execption) {

            }
        }
        return response;
    }

    /**
     * 自定义公众号底部菜单
     * @param accessToken accessToken目前需要手动设置
     * @return 设置后返回的json串
     */
    public static String setMenu(String accessToken){
        String path = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+accessToken ;
        String method = "POST";
        String body = "{\n" +
                "\t\"button\": [{\n" +
                "\t\t\t\"name\": \"招商服务\",\n" +
                "\t\t\t\"sub_button\": [{\n" +
                "\t\t\t\t\t\"type\": \"click\",\n" +
                "\t\t\t\t\t\"name\": \"园区简介\",\n" +
                "\t\t\t\t\t\"key\": \"jianjie\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"click\",\n" +
                "\t\t\t\t\t\"name\": \"招商详情\",\n" +
                "\t\t\t\t\t\"key\": \"xiangqing\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"click\",\n" +
                "\t\t\t\t\t\"name\": \"入驻商家\",\n" +
                "\t\t\t\t\t\"key\": \"ruzhu\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"click\",\n" +
                "\t\t\t\t\t\"name\": \"招商热线\",\n" +
                "\t\t\t\t\t\"key\": \"rexian\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"园区服务\",\n" +
                "\t\t\t\"sub_button\": [{\n" +
                "\t\t\t\t\t\"type\": \"view\",\n" +
                "\t\t\t\t\t\"name\": \"商家简介\",\n" +
                "\t\t\t\t\t\"url\": \"http://yychf.vicp.io/MerchantIntroduction.html\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"miniprogram\",\n" +
                "\t\t\t\t\t\"name\": \"测试\",\n" +
                "\t\t\t\t\t\"url\": \"http://mp.weixin.qq.com\",\n" +
                "\t\t\t\t\t\"appid\": \"wx286b93c14bbf93aa\",\n" +
                "\t\t\t\t\t\"pagepath\": \"pages/lunar/index\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"type\": \"click\",\n" +
                "\t\t\t\t\t\"name\": \"赞一下我们\",\n" +
                "\t\t\t\t\t\"key\": \"V1001_GOOD\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"name\": \"卖家导航\",\n" +
                "\t\t\t\"sub_button\": [{\n" +
                "\t\t\t\t\t\"type\": \"pic_sysphoto\",\n" +
                "\t\t\t\t\t\"name\": \"系统拍照发图\",\n" +
                "\t\t\t\t\t\"key\": \"rselfmenu_1_0\",\n" +
                "\t\t\t\t\t\"sub_button\": []\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"name\": \"发送位置\",\n" +
                "\t\t\t\t\t\"type\": \"location_select\",\n" +
                "\t\t\t\t\t\"key\": \"rselfmenu_2_0\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String res = httpsRequestToString(path, method, body);
        return res;
    }

    public static void main(String[] args) throws Exception{
//        //媒体文件路径
////        String filePath = "H:\\Workspaces\\IntelliJ IDEA\\wechats\\web\\media\\image\\20170614.jpg";
////        String filePath = "H:\\Workspaces\\IntelliJ IDEA\\wechats\\web\\media\\voice\\voice.mp3";
//        String filePath = "D:\\Workspaces\\IntelliJ IDEA\\wechats\\web\\media\\video\\小苹果.mp4";
//        //媒体文件类型
////        String type = "image";
////        String type = "voice";
//        String type = "video";
//        JSONObject uploadResult = uploadMedia(filePath, type);
//        //{"media_id":"dSQCiEHYB-pgi7ib5KpeoFlqpg09J31H28rex6xKgwWrln3HY0BTsoxnRV-xC_SQ","created_at":1455520569,"type":"image"}
//        System.out.println("上传成功："+uploadResult.toString());
//
//        //下载刚刚上传的图片    以id命名
////        String media_id = uploadResult.getString("media_id");
////        File file = downloadMedia("D:/" + media_id + ".png", media_id);
////        System.out.println(file.getName());

        //自定义公众号菜单（暂时由于accessToken没有保存，需要手动获取）
        String accessToken = "19_J2mVXtX3eKlbmi_o4_SugMO1NYhK7NfhHD9RvOFcEQMtUZ6lS3JnPg1AI64_LVDV9Xt5iD6rywOt0WgG2SScg32c0IbDZjwKt72Ncg3nwb2AyGtZCJsZl0hqQXcAFMiAGADIP";
        System.out.println(setMenu(accessToken));
    }



    private static class JEEWeiXinX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

