package com.zhucai.api.config;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhucai.api.entity.ApiAuthSecretEntiry;
import com.zhucai.api.mapper.ApiAuthSecretMapper;
import com.zhucai.api.sys.ErrorCode;
import com.zhucai.api.sys.ZhucaiMallApiException;
import com.zhucai.api.utils.ZcAssert;


/**
 * Copyright © 2019 ZhuCai All rights reserved.
 * @author: WCH
 * @date: 2019年6月20日 下午10:56:41
 * @Description: TODO 认证验签
 */
@Component
public class CertificationCheck {
	
    private final static Logger log = LoggerFactory.getLogger(CertificationCheck.class);
    
    @Autowired
    private ApiAuthSecretMapper apiAuthSecretMapper;

    public ApiAuthSecretEntiry check(String params) throws Exception {
    	
    	log.info("验签<START>------------------------");
    	log.info("请求参数：{}",params);
    	
    	ZcAssert.notBlank(params, ErrorCode.NO_DATA);
    	
//    	Map<String, String> paramsMap = JSON.parseObject(params,new TypeReference<Map<String, String>>(){}); 乱序并且会自动过滤掉值为null的键，暂未解决
    	
    	JSONObject paramsMap = JSONObject.parseObject(params,Feature.OrderedField);
       
    	String app_key	= (String) paramsMap.get("app_key");
    	String sign	= (String) paramsMap.get("sign");
    	String timestamp = (String) paramsMap.get("timestamp");
    	String version = (String) paramsMap.get("version");
    	String biz_content = JSON.toJSONString(paramsMap.get("biz_content"), SerializerFeature.WriteMapNullValue);
    	String format = (String) paramsMap.get("format");//
    	String charset = (String) paramsMap.get("charset");//
    	
    	log.debug("app_key={},sign={},timestamp={},version={},format={},charset={},biz_content={}",
    			app_key,sign,timestamp,version,format,charset,biz_content);
    	
    	try {
	    	//判定APP_key是否存在
	    	ZcAssert.notBlank(app_key,  ErrorCode.APP_KEY_ERROR);
	    	QueryWrapper<ApiAuthSecretEntiry> query = new QueryWrapper<ApiAuthSecretEntiry>().eq("auth_code", app_key.trim());
	    	ApiAuthSecretEntiry apiAuthSecretEntiry = apiAuthSecretMapper.selectOne(query);
	    	ZcAssert.notNull(apiAuthSecretEntiry, ErrorCode.APP_KEY_ERROR);
	    	ZcAssert.notBlank(apiAuthSecretEntiry.getAuthSecret(), ErrorCode.API_AUTH_SECRET_ERROR);
	    	
	    	//必填参数判空
	    	ZcAssert.notBlank(sign,  ErrorCode.SIGN_EMPTY);
	    	ZcAssert.notBlank(version,  ErrorCode.VERSION_ERROR);
	    	ZcAssert.notBlank(biz_content,  ErrorCode.BIZ_CONTENT_ERROR);
	
	    	// 参与签名的参数
	        Map<String, String> paramsValid = new HashMap<>();
	        paramsValid.put("app_key",app_key.trim());
	        paramsValid.put("timestamp",timestamp.trim());
	        paramsValid.put("version",version.trim());
	        paramsValid.put("biz_content",biz_content.trim());
	        //非必填参数
	    	if (StringUtils.isNotBlank(format)) {
	    		paramsValid.put("format",format);
			}
	    	if (StringUtils.isNotBlank(charset)) {
	    		paramsValid.put("charset",charset);
			}
	    	
	    	//校验验签
    		checkSign(sign, paramsValid,apiAuthSecretEntiry);
    		log.info("验签<END>------------------------");
    		return apiAuthSecretEntiry;
    		
		} catch (Exception e) {
			throw e;
		}
    	
    }

    private void checkSign(String sign,Map<String, String> params,ApiAuthSecretEntiry authSecretEntiry) {
    	
    	 Map<String, String> temp = new TreeMap<>(params); // 对参数进行排序
         //拼接参数
         StringBuilder sb = new StringBuilder();
         for (String key : temp.keySet()) {
             sb.append(key).append("=").append(temp.get(key)).append("&");
         }
         sb.append("secret").append("=").append(authSecretEntiry.getAuthSecret().trim());
         String strParams = sb.toString().toUpperCase();
         
         try {
			String resultSign = DigestUtils.md5DigestAsHex(strParams.getBytes("utf-8"));
			log.info("验签结果:{}",resultSign);
			ZcAssert.isTrue(sign.equals(resultSign), ErrorCode.SIGN_ERROR);
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
			throw new ZhucaiMallApiException(ErrorCode.SIGN_ERROR);
		}
         
    }
    
    
}
