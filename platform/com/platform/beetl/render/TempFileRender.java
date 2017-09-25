package com.platform.beetl.render;

import java.io.File;

import com.jfinal.render.FileRender;

/**
 * 临时文件下载，下载后删除临时文件
 * 
 * @author 董华健
 * 
 * 应用示例：
 * 	render(new TempFileRender(file)); // 输出下载
 */
public class TempFileRender extends FileRender {
	
	private String fileName;
	private File file;

	public TempFileRender(String fileName) {
		super(fileName);
		this.fileName = fileName;
	}

	public TempFileRender(File file, String downloadSaveFileName) {
		super(file, downloadSaveFileName);
		this.file = file;
	}
	
	public TempFileRender(File file) {
		super(file);
		this.file = file;
	}

	public TempFileRender(String fileName, String downloadSaveFileName) {
		super(fileName, downloadSaveFileName);
		this.fileName = fileName;
	}
	
	@Override
	public void render() {
		/**
		 * 解决IE8下下载失败的问题
		 */
		String userAgent = request.getHeader("User-Agent");
		if(userAgent.toLowerCase().indexOf("msie") != -1){
			response.reset(); 
		}
		
		try {
			super.render();
		} finally {
			if (null != fileName) {
				file = new File(fileName);
			}

			if (null != file) {
				file.delete();
				file.deleteOnExit();
			}
		}
	}
	
}
