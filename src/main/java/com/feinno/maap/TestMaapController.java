package com.feinno.maap;


import com.feinno.common.webmvc.utils.HttpKit;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Description: Test
 * @Author 韦吉谋
 * @Date 2020-11-06 15:07
 */
@RestController
@RequestMapping("/testMaap/")
public class TestMaapController {
    LinkedBlockingQueue<Map<String,String>> linkedBlockingQueue = new LinkedBlockingQueue<>();
    String notifyTxt = "<?xml version=\"1.0\"?><msg:deliveryInfoNotification xmlns:msg=\"urn:oma:xml:rest:netapi:messaging:1\"><deliveryInfo>  <address>sip:+86%s@zj.5GMC.ims.mnc000.mcc460.3gppnetwork.org</address><messageId>%s</messageId><deliveryStatus>DeliveredToTerminal</deliveryStatus>  <description>SVC1000</description></deliveryInfo></msg:deliveryInfoNotification>";
   //需要修改ip地址成商用
    String url = "http://10.10.202.141:7040/gaungzhou/api/msgcenter/maap/notifications/DeliveryInfoNotification/";
    int count = 0;

    @PostMapping(value = "test",consumes ={"application/xml"} )
    public String test(@RequestBody String body) {
        String chotbotId = body.substring(body.indexOf("<senderAddress>") + 15, body.indexOf("</senderAddress>"));
        String messageId = UUID.randomUUID().toString();
        body = body.replace("</bodyText>", "</bodyText><messageId>" + messageId + "</messageId>");
        long phone = 12345678901L + count++;
        Map<String,String> httpEntity=new HashMap<>();
        httpEntity.put("url",url+chotbotId);
        httpEntity.put("body",String.format(notifyTxt,messageId, phone));
        linkedBlockingQueue.add(httpEntity);
        return body;
    }

    @PostConstruct
    protected void doNotify() {
        Map<String,String>headers=new HashMap<>();
        headers.put("Content-Type", "application/xml");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    while(linkedBlockingQueue.size()>0){
                        Map<String,String> httpEntity=linkedBlockingQueue.poll();
                        HttpKit.post(httpEntity.get("url"),null,httpEntity.get("body"),headers);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
