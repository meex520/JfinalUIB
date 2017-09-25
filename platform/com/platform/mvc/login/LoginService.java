package com.platform.mvc.login;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.jfinal.kit.PropKit;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.platform.annotation.Service;
import com.platform.constant.ConstantInit;
import com.platform.constant.ConstantLogin;
import com.platform.interceptor.AuthInterceptor;
import com.platform.mvc.base.BaseService;
import com.platform.mvc.user.User;
import com.platform.tools.ToolDateTime;
import com.platform.tools.ToolWeb;
import com.platform.tools.security.ToolAES;
import com.platform.tools.security.ToolPbkdf2;
import com.platform.tools.security.ToolRSA;

@Service(name = LoginService.serviceName)
public class LoginService extends BaseService {

	@SuppressWarnings("unused")
	private static final Log log = Log.getLog(LoginService.class);

	public static final String serviceName = "loginService";
	
	/**
	 * 账号验证是否可以进行登陆
	 */
	public Ret userName(String userName){
		User user = User.cacheGetByUserName(userName);
		
		// 1.用户不存在
		if(null == user){
			return Ret.create("result", 0);
		}
		
		// 2.停用账户
		String status = user.getStr(User.column_status);
		if (status.equals("0")) {
			return Ret.create("result", 1);
		}
		
		// 3.密码错误次数超限
		long errorCount = user.getNumber(User.column_errorcount).longValue();
		int passErrorCount = PropKit.getInt(ConstantInit.config_passErrorCount_key);
		if(errorCount >= passErrorCount){
			Date stopDate = user.getDate(User.column_stopdate);
			int hourSpace = ToolDateTime.getDateHourSpace(stopDate, ToolDateTime.getDate());
			int passErrorHour = PropKit.getInt(ConstantInit.config_passErrorHour_key);
			if(hourSpace < passErrorHour){ // 密码错误次数超限，几小时内不能登录
				return Ret.create("result", 2)
						.set("stopDate", user.getStopdate())
						.set("hour", passErrorHour); 
			}
		}
		
		return Ret.create("result", 3).set("publicKey", Hex.encodeHexString(Base64.decodeBase64(user.getRsapublic())));
	}

	/**
	 * 用户登录后台验证
	 * @param request
	 * @param response
	 * @param accessKey AK
	 * @param encode	加密串
	 * @return
	 */
	public int skLogin(HttpServletRequest request, HttpServletResponse response, String accessKey, String encode) {
		// 1.取用户
		User user = User.cacheGetByUserName(accessKey);
		if (null == user) {
			return ConstantLogin.login_info_0;// 用户不存在
		}
		
		// 2.停用账户
		String status = user.getStr(User.column_status);
		if (status.equals("0")) {
			return ConstantLogin.login_info_1;
		}

		// 3.密码错误次数超限
		long errorCount = user.getNumber(User.column_errorcount).longValue();
		int passErrorCount = PropKit.getInt(ConstantInit.config_passErrorCount_key);
		if(errorCount >= passErrorCount){
			Date stopDate = user.getDate(User.column_stopdate);
			int hourSpace = ToolDateTime.getDateHourSpace(stopDate, ToolDateTime.getDate());
			int passErrorHour = PropKit.getInt(ConstantInit.config_passErrorHour_key);
			if(hourSpace < passErrorHour){
				return ConstantLogin.login_info_2;// 密码错误次数超限，几小时内不能登录
			}
		}
		
		// 4.解密加密串
		byte[] aes = user.getAes().getBytes();
		byte[] aesVector = user.getAesvector().getBytes();
		String data = "";
		try {
			byte[] decodeData = ToolAES.decryptCbcPKCS5Padding(Base64.decodeBase64(encode), aes, aesVector);
			data = new String(decodeData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 5.分解数据
		String[] dataArr = data.split(".#."); 					// arr[0]：scheme，arr[1]：时间戳，arr[2]：USER_IP， arr[3]：USER_AGENT， arr[4]：autoLogin
		String scheme = dataArr[0];								// scheme
		long loginDateTimes = Long.parseLong(dataArr[1]); 		// 时间戳
		String ips = dataArr[2]; 								// ip地址
		String userAgent = dataArr[3]; 							// USER_AGENT

		// 6.用户当前数据
		String newScheme = request.getScheme();
		String newIp = ToolWeb.getIpAddr(request);
		String newUserAgent = request.getHeader("User-Agent");
		Date start = ToolDateTime.getDate();
		start.setTime(loginDateTimes); 											// 用户自动登录开始时间
		int day = ToolDateTime.getDateDaySpace(start, ToolDateTime.getDate()); 	// 已经登录多少天
		int maxAge = PropKit.getInt(ConstantInit.config_maxAge_key);			// cookie自动登录有效天数

		// 4. 验证数据有效性
		if (scheme.equals(newScheme) 
				&& ips.equals(newIp) 
				&& userAgent.equals(newUserAgent) 
				&& day <= maxAge) {
			String authmark = AuthInterceptor.setCurrentUser(request, response, user, false); // 重写登录标识cookie
			request.setAttribute("authmark", authmark);
			return ConstantLogin.login_info_3;
		}
		return ConstantLogin.login_info_4;
	}
	
	/**
	 * 用户登录后台验证
	 * @param request
	 * @param response
	 * @param userName	账号
	 * @param passWord	密码
	 * @param autoLogin	是否自动登录
	 * @return
	 */
	public int passLogin(HttpServletRequest request, HttpServletResponse response, String userName, String passWord, boolean autoLogin) {
		// 1.取用户
		User user = User.cacheGetByUserName(userName);
		if (null == user) {
			return ConstantLogin.login_info_0;// 用户不存在
		}
		
		// 2.停用账户
		String status = user.getStr(User.column_status);
		if (status.equals("0")) {
			return ConstantLogin.login_info_1;
		}

		// 3.密码错误次数超限
		long errorCount = user.getNumber(User.column_errorcount).longValue();
		int passErrorCount = PropKit.getInt(ConstantInit.config_passErrorCount_key);
		if(errorCount >= passErrorCount){
			Date stopDate = user.getDate(User.column_stopdate);
			int hourSpace = ToolDateTime.getDateHourSpace(stopDate, ToolDateTime.getDate());
			int passErrorHour = PropKit.getInt(ConstantInit.config_passErrorHour_key);
			if(hourSpace < passErrorHour){
				return ConstantLogin.login_info_2;// 密码错误次数超限，几小时内不能登录
			}
		}

		// 4.验证密码
		String saltStr = user.getSalt();			// 密码盐
		byte[] salt = Base64.decodeBase64(saltStr);
		String passStr = user.getPassword();		// 密码
		byte[] encryptedPassword = Base64.decodeBase64(passStr);
		boolean bool = false;
		try {
			String privateKey = user.getRsaprivate();
			passWord = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(passWord), Base64.decodeBase64(privateKey)));
			bool = ToolPbkdf2.authenticate(passWord, encryptedPassword, salt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bool) {
			// 密码验证成功
			if(errorCount != 0){
				String sql = getSqlMy(User.sqlId_start);
				Db.use(ConstantInit.db_dataSource_main).update(sql, user.getPKValue());
				// 更新缓存
				user = User.cacheAdd(user.getPKValue());
			}
			AuthInterceptor.setCurrentUser(request, response, user, autoLogin);// 设置登录账户
			return ConstantLogin.login_info_3;
			
		} else {
			// 密码验证失败
			String sql = getSqlMy(User.sqlId_stop);
			Db.use(ConstantInit.db_dataSource_main).update(sql, ToolDateTime.getSqlTimestamp(ToolDateTime.getDate()), errorCount + 1, user.getPKValue());
			// 更新缓存
			User.cacheAdd(user.getPKValue());
			return ConstantLogin.login_info_4;
		}
	}

	/**
	 * 用户登录后台验证
	 * @param request
	 * @param response
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public int pass(HttpServletRequest request, HttpServletResponse response, String userName, String passWord) {
		// 1.取用户
		User user = User.cacheGetByUserName(userName);
		if (null == user) {
			return ConstantLogin.login_info_0;// 用户不存在
		} 
		
		// 2.停用账户
		String status = user.getStr(User.column_status);
		if (status.equals("0")) {
			return ConstantLogin.login_info_1;
		}

		// 3.密码错误次数超限
		long errorCount = user.getNumber(User.column_errorcount).longValue();
		int passErrorCount = PropKit.getInt(ConstantInit.config_passErrorCount_key);
		if(errorCount >= passErrorCount){
			Date stopDate = user.getDate(User.column_stopdate);
			int hourSpace = ToolDateTime.getDateHourSpace(stopDate, ToolDateTime.getDate());
			int passErrorHour = PropKit.getInt(ConstantInit.config_passErrorHour_key);
			if(hourSpace < passErrorHour){
				return ConstantLogin.login_info_2;// 密码错误次数超限，几小时内不能登录
			}
		}

		// 4.验证密码
		String saltStr = user.getSalt();			// 密码盐
		byte[] salt = Base64.decodeBase64(saltStr);
		String passStr = user.getPassword();		// 密码
		byte[] encryptedPassword = Base64.decodeBase64(passStr);
		boolean bool = false;
		try {
			bool = ToolPbkdf2.authenticate(passWord, encryptedPassword, salt);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		if (bool) {
			// 密码验证成功
			if(errorCount != 0){
				String sql = getSqlMy(User.sqlId_start);
				Db.use(ConstantInit.db_dataSource_main).update(sql, user.getPKValue());
				// 更新缓存
				user = User.cacheAdd(user.getPKValue());
			}
			return ConstantLogin.login_info_3;
		} else {
			// 密码验证失败
			String sql = getSqlMy(User.sqlId_stop);
			Db.use(ConstantInit.db_dataSource_main).update(sql, ToolDateTime.getSqlTimestamp(ToolDateTime.getDate()), errorCount+1, user.getPKValue());
			// 更新缓存
			User.cacheAdd(user.getPKValue());
			return ConstantLogin.login_info_4;
		}
	}

}
