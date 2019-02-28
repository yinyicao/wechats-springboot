package com.yyc.controller;

import com.yyc.common.Constant;
import com.yyc.utils.RequestAndResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * @ClassName TestController
 * @Description TODO  微信接口
 * @Author yinyicao
 * @DateTime 2019/2/22 15:39
 * @Blog http://www.cnblogs.com/hyyq/
 */
@RestController
public class WxController {


    /**
     * 处理微信服务器发来的消息（因为微信服务器发来的消息是用的post方式）
     */
    @PostMapping(value = "/WxServlet")
    public void handleMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TODO 接收、处理、响应由微信服务器转发的用户发送给公众帐号的消息
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        System.out.println("请求进入");
        String result = "";
        try {
            //解析微信发来的请求
            Map<String,String> map = RequestAndResponseUtil.parseXml(request);
            System.out.println("开始构造消息");
//            result = RequestAndResponseUtil.buildXml(map);
            //对微信发来的请求做出响应
            result = RequestAndResponseUtil.buildResponseMessage(map);
            System.out.println(result);
            if(result.equals("")){
                result = "未正确响应";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发生异常："+ e.getMessage());
            result = "未正确响应";
        }
        response.getWriter().println(result);
    }

    /**
     * 通过get请求，与微信公众号配置连接信息
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping(value = "/WxServlet")
    public void connect(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("开始校验签名");
        /**
         * 接收微信服务器发送请求时传递过来的4个参数
         */
        //微信加密签名signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
        String signature = request.getParameter("signature");
        //时间戳
        String timestamp = request.getParameter("timestamp");
        //随机数
        String nonce = request.getParameter("nonce");
        //随机字符串
        String echostr = request.getParameter("echostr");

        //排序
        String sortString = sort(Constant.TOKEN, timestamp, nonce);

        //加密
        String mySignature = sha1(sortString);

        //校验签名
        if (mySignature != null && mySignature != "" && mySignature.equals(signature)) {
            System.out.println("签名校验通过。");
            //如果检验成功输出echostr，微信服务器接收到此输出，才会确认检验完成。
            response.getWriter().write(echostr);
        } else {
            System.out.println("签名校验失败.");
        }
    }


    /**
     * 排序方法
     * @param token
     * @param timestamp
     * @param nonce
     * @return
     */
    public String sort(String token, String timestamp, String nonce) {
        String[] strArray = {token, timestamp, nonce};
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (String str : strArray) {
            sb.append(str);
        }

        return sb.toString();
    }

    /**
     * 加密的方法
     * @param str  要加密的字符串
     * @return 加密后的内容
     */
    public String sha1(String str)  {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
