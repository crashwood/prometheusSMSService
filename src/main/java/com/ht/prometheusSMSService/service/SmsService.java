package com.ht.prometheusSMSService.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ht.prometheusSMSService.util.StringUtil;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


/**
 * 调用短信服务
 *
 * @author tujing
 * 注意： 各个项目组根据自己情况 调整 url(短信平台地址)，spid(短信平台账号)，短信平台密码
 */
@Service
public class SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    RestTemplate restTemplate = new RestTemplate();

    /**
     * 缓存工具，用于存发送token
     */
    Cache<String, String> cache = CacheUtil.newFIFOCache(1);

    /**
     * 发送指定内容到指定手机
     *
     * @param phones 手机号码，多个好吗已,逗号分隔
     * @param msg    需要发送的内容
     * @throws IOException
     * @throws ParseException
     */
    public void sendSms(String phones, String msg) throws ParseException, IOException {


        // 获取token
//        String token = cache.get("token");
//        if (StringUtils.isEmpty(token)) {
//            token = getToken();
//            if (!StringUtils.isEmpty(token)) {
//                // 12个小时过期
//                cache.put("token", token, 12 * 60 * 60 * 1000L);
//            }
//        }

        // 发送短信
        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", token);
        headers.add("SysKey", "sys_promsms");
        headers.add("SysSecret", "prom1234");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        Map<String, String> param = new HashMap<>();
        param.put("phone", phones);
        param.put("content", msg);
        param.put("type", "2");
        // 部门
        param.put("param2", "0");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(param, headers);
        String result = restTemplate.postForObject("http://mgateway.prod.cs/services/sms/api/open/sendSms", request, String.class);
        LOG.info("发送结果： " + result);

    }

    private String getToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, String> param = new HashMap<>();
//        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.put("username", "sys_promsms");
        param.put("password", "prom1234");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(param, headers);

        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity("http://mgateway.prod.cs/api/authenticate", request, JSONObject.class);

        JSONObject jsonObject = jsonObjectResponseEntity.getBody();
        LOG.info("获取token结果:" + jsonObject);
        Object id_token = jsonObject.get("id_token");
        if (id_token != null) {
            return "Bearer " + id_token.toString();
        }
        return null;
    }

    private void sendSmsOld(String phones, String msg) throws IOException {
        String url = "http://10.2.19.6:8082/sms/mt";
        String result = "";

        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //设置接口参数
        Map<String, String> map = new HashMap<String, String>();
        //详见华泰http接口文档
        map.put("command", "MT_REQUEST");//基本不用调整
        map.put("spid", "DIANZI");//账号
        map.put("sppassword", "123456");//密码
        map.put("da", phones);
        map.put("dc", "15");
        map.put("sm", StringUtil.converGBK16(msg));


        // 装填参数
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (map != null) {
            for (Entry<String, String> entry : map.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        LOG.info("=================>>sendSmsService start");
        // 设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));

        // 设置header信息
        // 指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        // 执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = httpClient.execute(httpPost);
        // 获取结果实体
        // 判断网络连接状态码是否正常(0--200都数正常)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        // 释放链接
        response.close();

        LOG.info("=================>>sendSmsService:" + phones + " result:" + result);
    }
}
