package com.yyc.utils;

import com.yyc.common.MessageType;
import com.yyc.entry.Music;
import com.yyc.entry.NewsItem;
import com.yyc.entry.Video;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @InnerClassName HandleMessagesUtil
 * @Description TODO 消息处理工具类(接收、处理、返回)
 * @Author yinyicao
 * @DateTime 2019/2/22 16:01
 * @Blog http://www.cnblogs.com/hyyq/
 */
public class RequestAndResponseUtil {

    /**
     * 解析微信发来的请求（XML）
     * @throws Exception
     * @param request 封装了请求信息的HttpServletRequest对象
     * @return map 解析结果
     */
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();
        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        System.out.println("正在解析微信传过来的xml");
        for (Element e : elementList) {
            System.out.println(e.getName() + "|" + e.getText());
            map.put(e.getName(), e.getText());
        }
        System.out.println("微信传过来的xml解析完毕");

        // 释放资源
        inputStream.close();
        inputStream = null;
        return map;
    }

    /**
     * 根据消息类型构造返回消息
     * @param map 封装了解析结果的Map
     * @return responseMessage(响应消息)
     */
    public static String buildResponseMessage(Map map) {
        //响应消息
        String responseMessage = "";
        //得到消息类型
        String msgType = map.get("MsgType").toString();
        System.out.println("MsgType:" + msgType);
        //消息类型
        MessageType messageEnumType = MessageType.valueOf(MessageType.class, msgType.toUpperCase());
        switch (messageEnumType) {
            case TEXT:
                //处理文本消息
                responseMessage = HandleMessagesUtil.handleTextMessage(map);
                break;
            case IMAGE:
                //处理图片消息
                responseMessage = HandleMessagesUtil.handleImageMessage(map);
                break;
            case VOICE:
                //处理语音消息
                responseMessage = HandleMessagesUtil.handleVoiceMessage(map);
                break;
            case VIDEO:
                //处理视频消息
                responseMessage = HandleMessagesUtil.handleVideoMessage(map);
                break;
            case SHORTVIDEO:
                //处理小视频消息
                responseMessage = HandleMessagesUtil.handleSmallVideoMessage(map);
                break;
            case LOCATION:
                //处理位置消息
                responseMessage = HandleMessagesUtil.handleLocationMessage(map);
                break;
            case LINK:
                //处理链接消息
                responseMessage = HandleMessagesUtil.handleLinkMessage(map);
                break;
            case EVENT:
                //处理事件消息,用户在关注与取消关注公众号时，微信会向我们的公众号服务器发送事件消息,开发者接收到事件消息后就可以给用户下发欢迎消息
                responseMessage = HandleMessagesUtil.handleEventMessage(map);
            default:
                break;
        }
        //返回响应消息
        return responseMessage;
    }


    /**
     * @InnerClassName HandleMessagesUtil
     * @Description TODO 处理消息：接收自微信
     * @Author yinyicao
     * @DateTime 2019/2/22 17:01
     * @Blog http://www.cnblogs.com/hyyq/
     */
    private static class HandleMessagesUtil {
        /**
         * 接收到文本消息后处理
         *
         * @param map 封装了解析结果的Map
         * @return
         */
        static String handleTextMessage(Map<String, String> map) {
            //响应消息
            String responseMessage;
            // 消息内容
            String content = map.get("Content").trim();
            switch (content) {
                case "博客园":
                    String msgText = "欢迎朋友们访问我在博客园上面写的博客\n" +
                            "<a href=\"http://www.cnblogs.com/hyyq/\">敲代码的小松鼠的博客</a>";
                    responseMessage = BuildResponseMessagesUtil.buildTextMessage(map, msgText);
                    break;
//            case "图片":
//                //通过素材管理接口上传图片时得到的media_id
//                String imgMediaId = "kQUS8tNnWA_7dX48D-4VBzxHU6wmCNZYPWI0HT9imVWOnaUpQgOeNNFLtVKA3NnC";
//                responseMessage = BuildResponseMessagesUtil.buildImageMessage(map, imgMediaId);
//                break;
//            case "语音":
//                //通过素材管理接口上传语音文件时得到的media_id
//                String voiceMediaId = "F9u5ymVfal_vvr67zOODCfQKFcdr1RKFBGP-PmX3RPI3Ukn41nGaMSO3LnKTh-a7";
//                responseMessage = BuildResponseMessagesUtil.buildVoiceMessage(map,voiceMediaId);
//                break;
//            case "图文":
//                responseMessage = BuildResponseMessagesUtil.buildNewsMessage(map);
//                break;
//            case "音乐":
//                Music music = new Music();
//                music.title = "赵丽颖、许志安 - 乱世俱灭";
//                music.description = "电视剧《蜀山战纪》插曲";
//                music.musicUrl = "http://ii9eu8.natappfree.cc/media/music/music.mp3";
//                music.hqMusicUrl = "http://ii9eu8.natappfree.cc/media/music/music.mp3";
//                responseMessage = BuildResponseMessagesUtil.buildMusicMessage(map, music);
//                break;
//            case "视频":
//                Video video = new Video();
//                video.mediaId = "WdCwqe_R98ueL9chPFGeVEHDXg3pSv276VliNjtPbAqaxGz_RnhT4NXERDk06dfx";
//                video.title = "小苹果";
//                video.description = "小苹果搞笑视频";
//                responseMessage = BuildResponseMessagesUtil.buildVideoMessage(map, video);
//                break;
                default:
                    responseMessage = BuildResponseMessagesUtil.buildWelcomeTextMessage(map);
                    break;

            }
            //返回响应消息
            return responseMessage;
        }

        /**
         * 处理接收到图片消息
         *
         * @param map
         * @return
         */
        static String handleImageMessage(Map<String, String> map) {
            String picUrl = map.get("PicUrl");
            String mediaId = map.get("MediaId");
            System.out.print("picUrl:" + picUrl);
            System.out.print("mediaId:" + mediaId);
            String result = String.format("已收到您发来的图片，图片Url为：%s\n图片素材Id为：%s", picUrl, mediaId);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理接收到语音消息
         *
         * @param map
         * @return
         */
        static String handleVoiceMessage(Map<String, String> map) {
            String format = map.get("Format");
            String mediaId = map.get("MediaId");
            System.out.print("format:" + format);
            System.out.print("mediaId:" + mediaId);
            String result = String.format("已收到您发来的语音，语音格式为：%s\n语音素材Id为：%s", format, mediaId);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理接收到的视频消息
         *
         * @param map
         * @return
         */
        static String handleVideoMessage(Map<String, String> map) {
            String thumbMediaId = map.get("ThumbMediaId");
            String mediaId = map.get("MediaId");
            System.out.print("thumbMediaId:" + thumbMediaId);
            System.out.print("mediaId:" + mediaId);
            String result = String.format("已收到您发来的视频，视频中的素材ID为：%s\n视频Id为：%s", thumbMediaId, mediaId);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理接收到的小视频消息
         *
         * @param map
         * @return
         */
        static String handleSmallVideoMessage(Map<String, String> map) {
            String thumbMediaId = map.get("ThumbMediaId");
            String mediaId = map.get("MediaId");
            System.out.print("thumbMediaId:" + thumbMediaId);
            System.out.print("mediaId:" + mediaId);
            String result = String.format("已收到您发来的小视频，小视频中素材ID为：%s,\n小视频Id为：%s", thumbMediaId, mediaId);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理接收到的地理位置消息
         *
         * @param map
         * @return
         */
        static String handleLocationMessage(Map<String, String> map) {
            String latitude = map.get("Location_X");  //纬度
            String longitude = map.get("Location_Y");  //经度
            String label = map.get("Label");  //地理位置精度
            String result = String.format("纬度：%s\n经度：%s\n地理位置：%s", latitude, longitude, label);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理接收到的链接消息
         *
         * @param map
         * @return
         */
        static String handleLinkMessage(Map<String, String> map) {
            String title = map.get("Title");
            String description = map.get("Description");
            String url = map.get("Url");
            String result = String.format("已收到您发来的链接，链接标题为：%s,\n描述为：%s\n,链接地址为：%s", title, description, url);
            return BuildResponseMessagesUtil.buildTextMessage(map, result);
        }

        /**
         * 处理消息Message
         *
         * @param map 封装了解析结果的Map
         * @return
         */
        static String handleEventMessage(Map<String, String> map) {
            String responseMessage = BuildResponseMessagesUtil.buildWelcomeTextMessage(map);
            return responseMessage;
        }
    }

    /**
     * @InnerClassName BuildResponseMessagesUtil
     * @Description TODO 数据封装:返回给微信（格式为xml）
     * @Author yinyicao
     * @DateTime 2019/2/22 17:11
     * @Blog http://www.cnblogs.com/hyyq/
     */
    private static class BuildResponseMessagesUtil {
        /**
         * 构建提示消息(关注时、默认回复等)
         *
         * @param map 封装了解析结果的Map
         * @return responseMessageXml
         */
        static String buildWelcomeTextMessage(Map<String, String> map) {
            String responseMessageXml;
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            responseMessageXml = String
                    .format(
                            "<xml>" +
                                    "<ToUserName><![CDATA[%s]]></ToUserName>" +
                                    "<FromUserName><![CDATA[%s]]></FromUserName>" +
                                    "<CreateTime>%s</CreateTime>" +
                                    "<MsgType><![CDATA[text]]></MsgType>" +
                                    "<Content><![CDATA[%s]]></Content>" +
                                    "</xml>",
                            //"感谢您关注我的个人公众号，请回复如下关键词来使用公众号提供的服务：\n博客园\n图片\n语音\n视频\n音乐\n图文"
                            fromUserName, toUserName, getMessageCreateTime(),
                            "感谢您关注我的测试公众号，您可以回复图片、语音、位置等消息测试功能。\n请回复如下关键词来使用公众号提供的服务：\n博客园"
                    );
            return responseMessageXml;
        }

        /**
         * 构造文本消息
         *
         * @param map     封装了解析结果的Map
         * @param content 文本消息内容
         * @return 文本消息XML字符串
         */
        static String buildTextMessage(Map<String, String> map, String content) {
            //发送方帐号
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            /**
             * 文本消息XML数据格式
             * <xml>
             <ToUserName><![CDATA[toUser]]></ToUserName>
             <FromUserName><![CDATA[fromUser]]></FromUserName>
             <CreateTime>1348831860</CreateTime>
             <MsgType><![CDATA[text]]></MsgType>
             <Content><![CDATA[this is a test]]></Content>
             <MsgId>1234567890123456</MsgId>
             </xml>
             */
            return String.format(
                    "<xml>" +
                            "<ToUserName><![CDATA[%s]]></ToUserName>" +
                            "<FromUserName><![CDATA[%s]]></FromUserName>" +
                            "<CreateTime>%s</CreateTime>" +
                            "<MsgType><![CDATA[text]]></MsgType>" +
                            "<Content><![CDATA[%s]]></Content>" +
                            "</xml>",
                    fromUserName, toUserName, getMessageCreateTime(), content);
        }

        /**
         * 构造图片消息
         *
         * @param map     封装了解析结果的Map
         * @param mediaId 通过素材管理接口上传多媒体文件得到的id
         * @return 图片消息XML字符串
         */
        static String buildImageMessage(Map<String, String> map, String mediaId) {
            //发送方帐号
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            /**
             * 图片消息XML数据格式
             *<xml>
             <ToUserName><![CDATA[toUser]]></ToUserName>
             <FromUserName><![CDATA[fromUser]]></FromUserName>
             <CreateTime>12345678</CreateTime>
             <MsgType><![CDATA[image]]></MsgType>
             <Image>
             <MediaId><![CDATA[media_id]]></MediaId>
             </Image>
             </xml>
             */
            return String.format(
                    "<xml>" +
                            "<ToUserName><![CDATA[%s]]></ToUserName>" +
                            "<FromUserName><![CDATA[%s]]></FromUserName>" +
                            "<CreateTime>%s</CreateTime>" +
                            "<MsgType><![CDATA[image]]></MsgType>" +
                            "<Image>" +
                            "   <MediaId><![CDATA[%s]]></MediaId>" +
                            "</Image>" +
                            "</xml>",
                    fromUserName, toUserName, getMessageCreateTime(), mediaId);
        }

        /**
         * 构造音乐消息
         *
         * @param map   封装了解析结果的Map
         * @param music 封装好的音乐消息内容
         * @return 音乐消息XML字符串
         */
        static String buildMusicMessage(Map<String, String> map, Music music) {
            //发送方帐号
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            /**
             * 音乐消息XML数据格式
             *<xml>
             <ToUserName><![CDATA[toUser]]></ToUserName>
             <FromUserName><![CDATA[fromUser]]></FromUserName>
             <CreateTime>12345678</CreateTime>
             <MsgType><![CDATA[music]]></MsgType>
             <Music>
             <Title><![CDATA[TITLE]]></Title>
             <Description><![CDATA[DESCRIPTION]]></Description>
             <MusicUrl><![CDATA[MUSIC_Url]]></MusicUrl>
             <HQMusicUrl><![CDATA[HQ_MUSIC_Url]]></HQMusicUrl>
             <ThumbMediaId><![CDATA[media_id]]></ThumbMediaId>
             </Music>
             </xml>
             */
            return String.format(
                    "<xml>" +
                            "<ToUserName><![CDATA[%s]]></ToUserName>" +
                            "<FromUserName><![CDATA[%s]]></FromUserName>" +
                            "<CreateTime>%s</CreateTime>" +
                            "<MsgType><![CDATA[music]]></MsgType>" +
                            "<Music>" +
                            "   <Title><![CDATA[%s]]></Title>" +
                            "   <Description><![CDATA[%s]]></Description>" +
                            "   <MusicUrl><![CDATA[%s]]></MusicUrl>" +
                            "   <HQMusicUrl><![CDATA[%s]]></HQMusicUrl>" +
                            "</Music>" +
                            "</xml>",
                    fromUserName, toUserName, getMessageCreateTime(), music.title, music.description, music.musicUrl, music.hqMusicUrl);
        }

        /**
         * 构造视频消息
         *
         * @param map   封装了解析结果的Map
         * @param video 封装好的视频消息内容
         * @return 视频消息XML字符串
         */
        static String buildVideoMessage(Map<String, String> map, Video video) {
            //发送方帐号
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            /**
             * 音乐消息XML数据格式
             *<xml>
             <ToUserName><![CDATA[toUser]]></ToUserName>
             <FromUserName><![CDATA[fromUser]]></FromUserName>
             <CreateTime>12345678</CreateTime>
             <MsgType><![CDATA[video]]></MsgType>
             <Video>
             <MediaId><![CDATA[media_id]]></MediaId>
             <Title><![CDATA[title]]></Title>
             <Description><![CDATA[description]]></Description>
             </Video>
             </xml>
             */
            return String.format(
                    "<xml>" +
                            "<ToUserName><![CDATA[%s]]></ToUserName>" +
                            "<FromUserName><![CDATA[%s]]></FromUserName>" +
                            "<CreateTime>%s</CreateTime>" +
                            "<MsgType><![CDATA[video]]></MsgType>" +
                            "<Video>" +
                            "   <MediaId><![CDATA[%s]]></MediaId>" +
                            "   <Title><![CDATA[%s]]></Title>" +
                            "   <Description><![CDATA[%s]]></Description>" +
                            "</Video>" +
                            "</xml>",
                    fromUserName, toUserName, getMessageCreateTime(), video.mediaId, video.title, video.description);
        }

        /**
         * 构造语音消息
         *
         * @param map     封装了解析结果的Map
         * @param mediaId 通过素材管理接口上传多媒体文件得到的id
         * @return 语音消息XML字符串
         */
        static String buildVoiceMessage(Map<String, String> map, String mediaId) {
            //发送方帐号
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            /**
             * 语音消息XML数据格式
             *<xml>
             <ToUserName><![CDATA[toUser]]></ToUserName>
             <FromUserName><![CDATA[fromUser]]></FromUserName>
             <CreateTime>12345678</CreateTime>
             <MsgType><![CDATA[voice]]></MsgType>
             <Voice>
             <MediaId><![CDATA[media_id]]></MediaId>
             </Voice>
             </xml>
             */
            return String.format(
                    "<xml>" +
                            "<ToUserName><![CDATA[%s]]></ToUserName>" +
                            "<FromUserName><![CDATA[%s]]></FromUserName>" +
                            "<CreateTime>%s</CreateTime>" +
                            "<MsgType><![CDATA[voice]]></MsgType>" +
                            "<Voice>" +
                            "   <MediaId><![CDATA[%s]]></MediaId>" +
                            "</Voice>" +
                            "</xml>",
                    fromUserName, toUserName, getMessageCreateTime(), mediaId);
        }

        /**
         * 构造图文消息
         *
         * @param map 封装了解析结果的Map
         * @return 图文消息XML字符串
         */
        static String buildNewsMessage(Map<String, String> map) {
            String fromUserName = map.get("FromUserName");
            // 开发者微信号
            String toUserName = map.get("ToUserName");
            NewsItem item = new NewsItem();
            item.Title = "敲代码的小松鼠";
            item.Description = "敲代码的小松鼠的博客园主页\n";
            item.PicUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497442253857&di=8a9de5aa5fbab3736967c3c970c15225&imgtype=0&src=http%3A%2F%2Fs1.sinaimg.cn%2Fmw690%2F003ANoP1ty6JyzB1b0I40%26690";
            item.Url = "http://www.cnblogs.com/hyyq/";
            String itemContent1 = buildSingleItem(item);

            NewsItem item2 = new NewsItem();
            item2.Title = "Spring从入门到精通（二）";
            item2.Description = "Spring获取bean的一种方式";
            item2.PicUrl = "http://scimg.jb51.net/allimg/150706/11-150F61006001L.jpg";
            item2.Url = "http://www.cnblogs.com/hyyq/p/7003055.html";
            String itemContent2 = buildSingleItem(item2);


            String content = String.format("<xml>\n" +
                    "<ToUserName><![CDATA[%s]]></ToUserName>\n" +
                    "<FromUserName><![CDATA[%s]]></FromUserName>\n" +
                    "<CreateTime>%s</CreateTime>\n" +
                    "<MsgType><![CDATA[news]]></MsgType>\n" +
                    "<ArticleCount>%s</ArticleCount>\n" +
                    "<Articles>\n" + "%s" +
                    "</Articles>\n" +
                    "</xml> ", fromUserName, toUserName, getMessageCreateTime(), 2, itemContent1 + itemContent2);
            return content;

        }

        /**
         * 生成图文消息的一条记录
         *
         * @param item
         * @return
         */
        static String buildSingleItem(NewsItem item) {
            String itemContent = String.format("<item>\n" +
                    "<Title><![CDATA[%s]]></Title> \n" +
                    "<Description><![CDATA[%s]]></Description>\n" +
                    "<PicUrl><![CDATA[%s]]></PicUrl>\n" +
                    "<Url><![CDATA[%s]]></Url>\n" +
                    "</item>", item.Title, item.Description, item.PicUrl, item.Url);
            return itemContent;
        }

        /**
         * 生成消息创建时间 （整型）
         *
         * @return 消息创建时间
         */
        static String getMessageCreateTime() {
            Date dt = new Date();// 如果不需要格式,可直接用dt,dt就是当前系统时间
            DateFormat df = new SimpleDateFormat("yyyyMMddhhmm");// 设置显示格式
            String nowTime = df.format(dt);
            long dd = (long) 0;
            try {
                dd = df.parse(nowTime).getTime();
            } catch (Exception e) {

            }
            return String.valueOf(dd);
        }
    }
}

