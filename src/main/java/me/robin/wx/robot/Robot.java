package me.robin.wx.robot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.wx.robot.frame.Server;
import me.robin.wx.robot.frame.listener.MessageSendListener;
import me.robin.wx.robot.frame.listener.ServerStatusListener;
import me.robin.wx.robot.frame.service.ContactService;
import me.robin.wx.robot.frame.service.impl.ContactServiceImpl;
import me.robin.wx.robot.frame.util.WxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/4/18.
 */
public class Robot {
    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private static final Logger logger = LoggerFactory.getLogger(Robot.class);

    public static void main(String[] args) throws InterruptedException {
        ContactService contactService = new ContactServiceImpl();
        Server server = new Server("wx782c26e4c19acffb", contactService);

        MessageSendListener messageSendListener = new MessageSendListener() {
            @Override
            public void userNotFound(String user, String message) {

            }

            @Override
            public void serverNotReady(String user, String message) {
                server.waitLoginDone();
                server.sendTextMessage(user, message, this);
            }

            @Override
            public void success(String user, String message, String messageId, String localId) {
                logger.debug("发送完成:{} {}", messageId, localId);
            }

            @Override
            public void failure(String user, String message) {

            }
        };

        server.setStatusListener(new ServerStatusListener() {

            @Override
            public void onUUIDSuccess(String url) {
                logger.debug("登录二维码:{}", url);
            }

            @Override
            public void onAddMsgList(JSONArray addMsgList, Server server) {
                for (int i = 0; i < addMsgList.size(); i++) {
                    JSONObject message = addMsgList.getJSONObject(i);
                    String MsgId = message.getString("MsgId");
                    String FromUserName = message.getString("FromUserName");
                    String ToUserName = message.getString("ToUserName");
                    String Content = WxUtil.revertXml(message.getString("Content"));
                    Long CreateTime = message.getLong("CreateTime");
                    int msgType = message.getIntValue("MsgType");

                    logger.debug("收到新消息:{} {} {} {} {}", MsgId, FromUserName, ToUserName, Content, msgType);
                    switch (msgType) {
                        case 10002://撤销消息
                            server.sendTextMessage(FromUserName, "你发了：" + Content, messageSendListener);
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onModContactList(JSONArray modContactList, Server server) {

            }

            @Override
            public void onDelContactList(JSONArray delContactList, Server server) {

            }

            @Override
            public void onModChatRoomMemberList(JSONArray modChatRoomMemberList, Server server) {

            }
        });
        server.run();

        /*while (true) {
            server.sendTextMessage("Lubin.Xuan,AgFighter", "消息发送：" + System.currentTimeMillis(), messageSendListener);
            TimeUnit.SECONDS.sleep(1);
        }*/
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                server.sendTextMessage("AgFighter", "消息发送：" + new Date(), messageSendListener);
            }
        }, 0, 15 * 60 * 1000);
        TimeUnit.DAYS.sleep(5);
    }
}
