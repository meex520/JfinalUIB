package com.platform.mvc.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.jfinal.aop.Before;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.platform.annotation.Service;
import com.platform.dto.ZtreeNode;
import com.platform.mvc.base.BaseService;
import com.platform.mvc.dept.Department;
import com.platform.tools.ToolRandoms;
import com.platform.tools.security.ToolIDEA;
import com.platform.tools.security.ToolPbkdf2;
import com.platform.tools.security.ToolRSA;

@Service(name = UserService.serviceName)
public class UserService extends BaseService {

	private static final Log log = Log.getLog(UserService.class);

	public static final String serviceName = "userService";

	/**
	 * 验证账号是否存在
	 * @param userIds
	 * @param userName
	 * @return
	 * 描述：新增用户时userIds为空，修改用户时userIds传值
	 */
	public boolean valiUserName(String userIds, String userName){
		return valiByUser(userIds, User.column_username, userName);
	}

	/**
	 * 验证邮箱是否存在
	 * @param userIds
	 * @param mailBox
	 * @return
	 * 描述：新增用户时userIds为空，修改用户时userIds传值
	 */
	public boolean valiMailBox(String userIds, String mailBox){
		return valiByUser(userIds, User.column_email, mailBox);
	}

	/**
	 * 验证身份证是否存在
	 * @param userIds
	 * @param idcard
	 * @return
	 * 描述：新增用户时userIds为空，修改用户时userIds传值
	 */
	public boolean valiIdcard(String userIds, String idcard){
		return valiByUser(userIds, User.column_idcard, idcard);
	}

	/**
	 * 验证手机号是否存在
	 * @param userIds
	 * @param mobile
	 * @return
	 * 描述：新增用户时userIds为空，修改用户时userIds传值
	 */
	public boolean valiMobile(String userIds, String mobile){
		return valiByUser(userIds, User.column_mobile, mobile);
	}
	
	/**
	 * 验证UserInfo表中的某一列是否唯一可用
	 * @param userIds
	 * @param condition
	 * @param value
	 * @return
	 */
	private boolean valiByUser(String userIds, String condition, String value){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("column", "ids");
		param.put("table", "pt_user");
		param.put("condition", condition);
		String sql = getSqlByBeetl("platform.baseModel.select", param);
		List<User> list = User.dao.find(sql, value);
		int size = list.size();
		if(size == 0){
			return true;
		}
		if(size == 1){
			User user = list.get(0);
			if(userIds != null && user.getStr(User.column_ids).equals(userIds)){
				return true;
			}
		}
		if(size > 1){
			return false;
		}
		return false;
	}
	
	/**
	 * 新增用户信息保存
	 * @param pkIds
	 * @param user
	 * @param password
	 * @param userInfo
	 * @param privateKey
	 */
	@Before(Tx.class)
	public void save(String pkIds, User user, String password, UserInfo userInfo, String privateKey) {
		try {
			// 密码解密
			password = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(password), Base64.decodeBase64(privateKey)));
			
			// 密码加密
			byte[] salt = ToolPbkdf2.generateSalt();// 密码盐
			byte[] encryptedPassword = ToolPbkdf2.getEncryptedPassword(password, salt);
			user.set(User.column_salt, Base64.encodeBase64String(salt));
			user.set(User.column_password, Base64.encodeBase64String(encryptedPassword));

			// 初始化idea密钥
			String idea = Base64.encodeBase64String(ToolIDEA.initKey());
			user.setIdea(idea);

			// 初始化aes密钥
			String aes = ToolRandoms.getAuthCodeAll(16);
			String aesVector = ToolRandoms.getAuthCodeAll(16);
			user.setAes(aes);
			user.setAesvector(aesVector);

			// 初始化RSA密钥
			Map<String, Object> keyMap = ToolRSA.initKey();
			String rsaPublic = Base64.encodeBase64String(ToolRSA.getPublicKey(keyMap));
			String rsaPrivate = Base64.encodeBase64String(ToolRSA.getPrivateKey(keyMap));
			user.setRsapublic(rsaPublic);
			user.setRsaprivate(rsaPrivate);;
			
			// 保存用户
			user.set(User.column_errorcount, 0);
			user.set(User.column_status, "1");
			user.save(pkIds);

			// 保存用户扩展信息
			userInfo.save(pkIds);

			// 缓存
			User.cacheAdd(user.getPKValue());
		} catch (NoSuchAlgorithmException  e) {
			throw new RuntimeException("保存用户密码加密操作异常", e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("保存用户密码加密操作异常", e);
		}catch (Exception e) {
			throw new RuntimeException("保存用户异常", e);
		}
	}

	/**
	 * 更新用户信息
	 * @param user
	 * @param password
	 * @param userInfo
	 * @param privateKey
	 */
	@Before(Tx.class)
	public void update(User user, String password, UserInfo userInfo, String privateKey) {
		try {
			// 密码加密
			if (null != password && !password.trim().equals("")) {
				// 密码解密
				password = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(password), Base64.decodeBase64(privateKey)));
				
				User oldUser = User.dao.findById(user.getPKValue());
				String saltStr = oldUser.getSalt();// 密码盐
				byte[] salt = Base64.decodeBase64(saltStr);
				byte[] encryptedPassword = ToolPbkdf2.getEncryptedPassword(password, salt);
				user.set(User.column_password, Base64.encodeBase64String(encryptedPassword));
			}

			// 更新用户
			user.update();

			// 更新用户扩展信息
			userInfo.update();

			// 缓存
			User.cacheAdd(user.getPKValue());
		} catch (Exception e) {
			throw new RuntimeException("更新用户异常", e);
		}
	}

	/**
	 * 删除用户信息
	 * @param ids
	 */
	@Before(Tx.class)
	public void delete(String ids) {
		String[] idsArr = splitByComma(ids);
		for (String userIds : idsArr) {
			// 缓存
			User.cacheRemove(userIds);
			
			// 删除
			User.dao.deleteById(userIds);
			UserInfo.dao.deleteById(userIds);
		}
	}

	/**
	 * 获取子节点数据
	 * @param cxt
	 * @param deptIds
	 * @return
	 */
	public List<ZtreeNode> childNodeData(String cxt, String deptIds) {
		// 查询部门数据
		List<Department> deptList = null;
		if (null != deptIds) {
			String sql = getSqlMy(Department.sqlId_childNode);
			deptList = Department.dao.find(sql, deptIds.replace("dept_", ""));
		} else {
			String sql = getSqlMy(Department.sqlId_rootNode);
			deptList = Department.dao.find(sql);
		}

		// 查询用户数据
		List<User> userList = null;
		if (null != deptIds) {
			String sql = getSqlMy(User.sqlId_treeUserNode);
			userList = User.dao.find(sql, deptIds.replace("dept_", ""));
		}

		List<ZtreeNode> nodeList = new ArrayList<ZtreeNode>();
		ZtreeNode node = null;

		// 封装用户数据
		if (null != userList) {
			for (User user : userList) {
				node = new ZtreeNode();
				node.setId("user_" + user.getPKValue());
				node.setName(user.getStr("names"));
				node.setIsParent(false);
				node.setIcon(cxt + "/jsfile/ztree/css/zTreeStyle/img/diy/5.png");
				nodeList.add(node);
			}
		}

		// 封装部门数据
		for (Department dept : deptList) {
			node = new ZtreeNode();
			node.setId("dept_" + dept.getPKValue());
			node.setName(dept.getStr("names"));
			
			if (null != deptIds) {
				node.setIsParent(true);
			} else {
				node.setIsParent(Boolean.parseBoolean(dept.getStr(Department.column_isparent)));
			}
			node.setIcon(cxt + "/jsfile/ztree/css/zTreeStyle/img/diy/" + dept.getStr(Department.column_images));
			nodeList.add(node);
		}

		return nodeList;
	}

	/**
	 * 验证密码是否正确
	 * @param user
	 * @param passWord
	 * @return
	 */
	public boolean valiPassWord(User user, String passWord) {
		try {
			// 密码解密
			String privateKey = user.getRsaprivate();
			passWord = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(passWord), Base64.decodeBase64(privateKey)));
			
			// 密码校验
			byte[] salt = Base64.decodeBase64(user.getSalt());// 密码盐
			byte[] encryptedPassword = user.getBytes(User.column_password);
			boolean bool = ToolPbkdf2.authenticate(passWord, encryptedPassword, salt);
			if (bool) {
				return true;
			}
		} catch (Exception e) {
			if(log.isErrorEnabled()) log.error("验证密码是否正确异常，userName:" + user.getUsername() + "，密码：" + passWord, e);
			return false;
		}
		return false;
	}
	
	/**
	 * 密码变更
	 * @param user
	 * @param passOld
	 * @param passNew
	 */
	public boolean passChange(User user, String passOld, String passNew){
		try {
			// 密码解密
			String privateKey = user.getRsaprivate();
			passOld = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(passOld), Base64.decodeBase64(privateKey)));
			passNew = new String(ToolRSA.decryptByPrivateKey(Base64.decodeBase64(passNew), Base64.decodeBase64(privateKey)));
			
			// 验证旧密码
			byte[] salt = Base64.decodeBase64(user.getSalt());	// 密码盐
			String passStr = user.getPassword();				// 密码
			byte[] encryptedPassword = Base64.decodeBase64(passStr);
			boolean bool = false;
			try {
				bool = ToolPbkdf2.authenticate(passOld, encryptedPassword, salt);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			
			// 设置新密码
			if (bool) {
				byte[] saltNew = ToolPbkdf2.generateSalt();// 密码盐
				byte[] encryptedPasswordNew = ToolPbkdf2.getEncryptedPassword(passNew, saltNew);
				user.set(User.column_salt, Base64.encodeBase64String(saltNew));
				user.set(User.column_password, Base64.encodeBase64String(encryptedPasswordNew));
				// 更新用户
				user.update();
				// 缓存
				User.cacheAdd(user.getPKValue());
				return true;
			}
		} catch (Exception e) {
			if(log.isErrorEnabled()) log.error("更新用户密码异常，userName:" + user.getUsername() + "，旧密码：" + passOld + "，新密码：" + passNew, e);
			return false;
		}
		
		return false;
	}
	
}
