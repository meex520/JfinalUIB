package com.junit.platform.tool;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.jfinal.log.Log;
import com.platform.tools.ToolDateTime;
import com.platform.tools.ToolHttp;
import com.platform.tools.ToolNet;
import com.platform.tools.security.ToolAES;

public class TestToolHttpClient {

	private static final Log log = Log.getLog(TestToolHttpClient.class);

	private static final String userAgent = "HttpClient";
	private static final String aes = "JG8QSH4WUVTmcrBM";
	private static final String aesVector = "eHSKDw5FCHXCeVKB";

	/**
	 * 构造登录数据项
	 * @return
	 */
	private static Map<String, String> loginData(){
		// 加密串项
		String scheme = "http";
		String ips = ToolNet.ip();
		long date = ToolDateTime.getDateByTime();

		String data = new StringBuilder(scheme) // scheme.#.时间戳.#.USER_IP.#.USER_AGENT.#.autoLogin
			.append(".#.").append(date)
			.append(".#.").append(ips)
			.append(".#.").append(userAgent)
			.toString();
		
		// 加密
		String encode = "";
		try {
			byte[] dataEncode = ToolAES.encryptCbcPKCS5Padding(data.getBytes(), aes.getBytes(), aesVector.getBytes());
			encode = Base64.encodeBase64String(dataEncode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 数据集
		Map<String, String> param = new HashMap<String, String>();
		param.put("userAgent", userAgent);	// 登陆user-Agent
		param.put("accessKey", "admins");	// 登陆AK，账号即可
		param.put("encode", encode);		// 登陆密文
		
		return param;
	}
	
	@Test
	public void asSk(){
		// 1.登录
		Map<String, String> loginParam = loginData();
		String authmark = ToolHttp.mockLogin("http://10.192.66.8:8899/platform/login/vali", loginParam);
		if (authmark.equals("LoginAgain")) {
			if(log.isErrorEnabled()) log.error("登录失败");
		}
		
		// 2.调用数据接口
		Map<String, String> postParam = new HashMap<String, String>();
		postParam.put("userAgent", userAgent);	// 登陆user-Agent
//		postParam.put("xxx", "xxx"); // 继续添加请求参数
		String content = ToolHttp.mockPost(authmark, "http://10.192.66.8:8899/platform/index", postParam);
		if(content.indexOf("/platform/login/login.html") != -1){
			log.error("需要重新登录，认证码失效");
		}
		
		// 3.输出响应正文数据
		System.out.println(content);
	}
	

}
