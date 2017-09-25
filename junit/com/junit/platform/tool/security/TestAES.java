package com.junit.platform.tool.security;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.platform.tools.ToolRandoms;
import com.platform.tools.security.ToolAES;

public class TestAES { // extends TestBase  {

	@Test
    public void test() throws Exception{
		String inputStr = "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作";
		byte[] inputData = inputStr.getBytes();
		System.err.println("原文:\t" + inputStr);

		// 初始化密钥
		byte[] key = ToolAES.initKey();
		System.err.println("密钥:\t" + Base64.encodeBase64String(key));

		// 加密
		inputData = ToolAES.encrypt(inputData, key);
		System.err.println("加密后:\t" + Base64.encodeBase64String(inputData));

		// 解密
		byte[] outputData = ToolAES.decrypt(inputData, key);

		String outputStr = new String(outputData);
		System.err.println("解密后:\t" + outputStr);
	}

	/**
	 * 密钥、向量为16位长度
	 * @throws Exception
	 */
	@Test
    public void testCbcPKCS5Padding() throws Exception{
		String inputStr = "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作"
				+ "录入密码，检测录入的密码和数据库密码是否是123456，如果是，强制弹出密码修改框改密码，修改完后重新录入新密码继续登录操作";
		byte[] inputData = inputStr.getBytes();
		System.err.println("原文:\t" + inputStr);

		// 初始化密钥
		byte[] key = ToolRandoms.getAuthCodeAll(16).getBytes();
		System.err.println("密钥:\t" + Base64.encodeBase64String(key));
		
		byte[] vector = ToolRandoms.getAuthCodeAll(16).getBytes();
		System.err.println("Vector:\t" + Base64.encodeBase64String(vector));

		// 加密
		inputData = ToolAES.encryptCbcPKCS5Padding(inputData, key, vector);
		System.err.println("加密后:\t" + Base64.encodeBase64String(inputData));

		// 解密
		byte[] outputData = ToolAES.decryptCbcPKCS5Padding(inputData, key, vector);

		String outputStr = new String(outputData);
		System.err.println("解密后:\t" + outputStr);
	}

	/**
	 * 密钥、向量、加密内容，全部为16位长度
	 * @throws Exception
	 */
	@Test
    public void testCbcNoPadding() throws Exception{
		String inputStr = "修改完后重新录入密码继续登录操作";
		byte[] inputData = inputStr.getBytes();
		System.err.println("原文:\t" + inputStr);

		// 初始化密钥
		byte[] key = ToolRandoms.getAuthCodeAll(16).getBytes();
		System.err.println("密钥:\t" + Base64.encodeBase64String(key));
		
		byte[] vector = ToolRandoms.getAuthCodeAll(16).getBytes();
		System.err.println("Vector:\t" + Base64.encodeBase64String(vector));

		// 加密
		inputData = ToolAES.encryptCbcNoPadding(inputData, key, vector);
		System.err.println("加密后:\t" + Base64.encodeBase64String(inputData));

		// 解密
		byte[] outputData = ToolAES.decryptCbcNoPadding(inputData, key, vector);

		String outputStr = new String(outputData);
		System.err.println("解密后:\t" + outputStr);
	}

}
