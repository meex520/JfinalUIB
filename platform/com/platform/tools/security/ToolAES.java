package com.platform.tools.security;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.jfinal.log.Log;

/**
 * AES安全编码组件
 * 
 * 高级数据加密标准---AES：由于DES的问题所以产生了AES,像是DES的升级，密钥建立时间短，灵敏性好，内存要求低，被广泛应用
 * 
 * 说明：
 * 
 * 对于java.security.InvalidKeyException: Illegal key size or default
 * parameters异常， 去掉这种限制需要下载Java Cryptography Extension (JCE) Unlimited Strength
 * Jurisdiction Policy Files， 下载包的readme.txt
 * 有安装说明。就是替换${java_home}/jre/lib/security/
 * 下面的local_policy.jar和US_export_policy.jar
 * 
 * JCE中AES支持五中模式：CBC，CFB，ECB，OFB，PCBC；支持三种填充：NoPadding，PKCS5Padding，ISO10126Padding。不支持SSL3Padding。不支持“NONE”模式。
 * 
 * 算法/模式/填充                		16字节加密后数据长度        	不满16字节加密后长度
 * AES/CBC/NoPadding             16                          不支持
 * AES/CBC/PKCS5Padding          32                          16
 * AES/CBC/ISO10126Padding       32                          16
 * AES/CFB/NoPadding             16                          原始数据长度
 * AES/CFB/PKCS5Padding          32                          16
 * AES/CFB/ISO10126Padding       32                          16
 * AES/ECB/NoPadding             16                          不支持
 * AES/ECB/PKCS5Padding          32                          16
 * AES/ECB/ISO10126Padding       32                          16
 * AES/OFB/NoPadding             16                          原始数据长度
 * AES/OFB/PKCS5Padding          32                          16
 * AES/OFB/ISO10126Padding       32                          16
 * AES/PCBC/NoPadding            16                          不支持
 * AES/PCBC/PKCS5Padding         32                          16
 * AES/PCBC/ISO10126Padding      32                          16
 */
public abstract class ToolAES {

	@SuppressWarnings("unused")
	private static final Log log = Log.getLog(ToolAES.class);

	/**
	 * 密钥算法
	 */
	public static final String KEY_ALGORITHM = "AES";

	/**
	 * 加密/解密算法 / 工作模式 / 填充方式 Java 6支持PKCS5Padding填充方式 Bouncy
	 * Castle支持PKCS7Padding填充方式
	 */
	public static final String CIPHER_ALGORITHM_ECB = "AES/ECB/PKCS5Padding";

	/**
	 * 加密/解密算法 / 工作模式 / 填充方式 Java 6支持PKCS5Padding填充方式 Bouncy
	 * Castle支持PKCS7Padding填充方式
	 */
	public static final String CIPHER_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";

	/**
	 * AES/CBC/NoPadding 要求
	 * 密钥必须是16位的；Initialization vector (IV) 必须是16位
	 * 待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出如下异常：
	 * javax.crypto.IllegalBlockSizeException: Input length not multiple of 16 bytes
	 * 
	 *  由于固定了位数，所以对于被加密数据有中文的, 加、解密不完整
	 *  
	 *  可 以看到，在原始数据长度为16的整数n倍时，假如原始数据长度等于16*n，则使用NoPadding时加密后数据长度等于16*n，
	 *  其它情况下加密数据长 度等于16*(n+1)。在不足16的整数倍的情况下，假如原始数据长度等于16*n+m[其中m小于16]，
	 *  除了NoPadding填充之外的任何方 式，加密数据长度都等于16*(n+1).
	 */
	public static final String CIPHER_ALGORITHM_CBC_NoPadding = "AES/CBC/NoPadding";

	/**
	 * 生成密钥
	 * @return byte[] 二进制密钥
	 * @throws Exception
	 */
	public static byte[] initKey() throws Exception {
		// 实例化
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);

		/*
		 * AES 要求密钥长度为 128位、192位或 256位
		 */
		kg.init(256);

		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();

		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}

	/**
	 * 加密
	 * @param data 待加密数据
	 * @param key 密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);

		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);

		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 * @param data 待解密数据
	 * @param key密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);

		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);

		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 加密
	 * @param data 待加密数据
	 * @param key 密钥
	 * @param iv 
	 * @return 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptCbcPKCS5Padding(byte[] data, byte[] key, byte[] iv) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);

		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(iv));
		
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 * @param data 待解密数据
	 * @param key密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptCbcPKCS5Padding(byte[] data, byte[] key, byte[] iv) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);

		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));

		// 执行操作
		return cipher.doFinal(data);
	}


	/**
	 * 加密
	 * @param data 待加密数据
	 * @param key 密钥
	 * @param iv 
	 * @return 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptCbcNoPadding(byte[] data, byte[] key, byte[] iv) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);

		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(iv));
		
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 * @param data 待解密数据
	 * @param key密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptCbcNoPadding(byte[] data, byte[] key, byte[] iv) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);

		/*
		 * 实例化 使用PKCS7Padding填充方式，按如下方式实现 Cipher.getInstance(CIPHER_ALGORITHM, "BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);

		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));

		// 执行操作
		return cipher.doFinal(data);
	}

}
