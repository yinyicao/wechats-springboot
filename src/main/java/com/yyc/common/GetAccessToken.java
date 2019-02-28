package com.yyc.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yyc.utils.NetWorkHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName GetAccessToken
 * @Description TODO 用于获取accessToken
 * @Author yinyicao
 * @DateTime 2019/2/22 16:32
 * @Blog http://www.cnblogs.com/hyyq/
 */
@Component
public class GetAccessToken implements CommandLineRunner {
    static final String appId = Constant.APPID;
    static final String appSecret = Constant.APPSECRET;
    @Override
    public void run(String... args) {
        //开启一个新的线程
        new Thread(() -> {
            while (true) {
                try {
                    //获取accessToken
                    AccessTokenInfo.accessToken = getAccessToken(appId, appSecret);
                    //获取成功
                    if (AccessTokenInfo.accessToken != null) {
                        //获取到access_token 休眠7000秒,大约2个小时左右
                        Thread.sleep(7000 * 1000);
//                            Thread.sleep(10 * 1000);//10秒钟获取一次
                    } else {
                        //获取失败
                        //获取的access_token为空 休眠3秒
                        Thread.sleep(1000 * 3);
                    }
                } catch (Exception e) {
                    System.out.println("发生异常：" + e.getMessage());
                    e.printStackTrace();
                    try {
                        //发生异常休眠1秒
                        Thread.sleep(1000 * 10);
                    } catch (Exception e1) {

                    }
                }
            }

        }).start();

    }

    /**
     * 获取access_token
     *access_token是公众号的全局唯一接口调用凭据，公众号调用各接口时都需使用access_token。
     * @return AccessToken
     */
    private static AccessToken getAccessToken(String appId, String appSecret) {
        NetWorkHelper netHelper = new NetWorkHelper();
        /**
         * 接口地址为https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET，其中grant_type固定写为client_credential即可。
         */
        String Url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, appSecret);
        //此请求为https的get请求，返回的数据格式为{"access_token":"ACCESS_TOKEN","expires_in":7200}
        String result = netHelper.getHttpsResponse(Url, "");
        System.out.println("获取到的access_token="+result);
        //使用FastJson将Json字符串解析成Json对象
        JSONObject json = JSON.parseObject(result);
        AccessToken token = new AccessToken();
        token.setAccessToken(json.getString("access_token"));
        token.setExpiresin(json.getInteger("expires_in"));
        return token;
    }




}
