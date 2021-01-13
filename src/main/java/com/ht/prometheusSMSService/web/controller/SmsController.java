package com.ht.prometheusSMSService.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ht.prometheusSMSService.service.SmsService;

/**
 * 短信服务接口
 * 
 * @author tujing
 *
 */
@RestController
public class SmsController {

	private static final Logger LOG = LoggerFactory.getLogger(SmsController.class);
	
	@Autowired
	private Environment env;

	@Autowired
	private SmsService smsService;

	@RequestMapping(value = "/smsSend", method = RequestMethod.POST)
	public void webHooks(@RequestBody String body) {
		LOG.info("smsSend in ");
		JSONObject jsonObject = JSONObject.parseObject(body);
		// 获取预警列表
		JSONArray jsonArray = jsonObject.getJSONArray("alerts");

		if (jsonArray.size() > 0) {
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);

				if (jsonObj.containsKey("annotations")) {
					String status =jsonObj.get("status").toString();
					jsonObj = JSONObject.parseObject(jsonObj.get("annotations").toString());
					String phones = jsonObj.getString("description");
					
					if(phones.equals("NoSMS")) {
						LOG.info("此监控不需要短信");
						return;
					}else if(phones.startsWith("group")) {
						String phone = this.getPhones(phones);
						if(phone==null||phone.equals("")) {
							LOG.error("没有找到此监控组的配置："+phones);
							return;
						}else {
							phones = phone;
						}
					}
					
					
					
					String [] phoneList = phones.split(",");
					
					String msg = status+":"+jsonObj.getString("summary");
					System.out.println(phones + "--------" + msg);
					if(phoneList.length>0) {
						for(String phone :phoneList) {
							try {
								smsService.sendSms(phone, msg);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				} else {
					LOG.error("smsSend error key[annotations] not found! ");
				}
			}
		}
	}

	private String getPhones(String phones) {
		 Yaml yaml = new Yaml();
		 String phone =null;
	      /*  //文件路径是相对类目录(src/main/java)的相对路径
	        InputStream in =  SmsController.class.getClassLoader().getResourceAsStream("application.yaml");//或者app.yaml
	        Map<String, Object> map = yaml.loadAs(in, Map.class);
	        Map<String, Object> group =  (Map<String, Object>) map.get("group");
	        
	        if(group !=null) {
	        	 Object name = group.get(phones.split("group.")[1]);
	        	 if(name!=null) {
	        	 phone = name.toString();
	        	 }	
	        } */
	        return env.getProperty(phones);
		
	}
}
