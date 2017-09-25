package com.platform.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ToolThread {

	/********************************		异步执行		*********************************************/
	
	/**
	 * 任务处理线程池
	 */
	private static final ExecutorService pool = Executors.newFixedThreadPool(10);
	
	/**
	 * 放入任务处理池
	 * @param command
	 */
	public static void execute(Runnable command){
		pool.execute(command);
	}
	
	/**
	 * 线程任务处理基类
	 */
	public static abstract class Run implements Runnable{
		protected static List<?> data;

		public static List<?> getData() {
			return data;
		}

		public static void setData(List<?> data) {
			Run.data = data;
		}
	}
	
	/**
	 * 分批处理
	 * @param data			待处理数据
	 * @param threadCount	线程数
	 * @param classes		处理类
	 */
	public static void splitList(List<?> data, int threadCount, Class<? extends Run> classes){
		int size = data.size();				// 数据量
		if(threadCount <= 0){
			threadCount = 10;				// 线程数
		}
		int batch = size / threadCount;		// 平均每批次处理数量
		int modulo = size % threadCount;	// 剩余数量
		
		for (int i = 0; i < threadCount; i++) {
			int start = i * batch;
			int end = i * batch + batch;
			if(i == (threadCount - 1)){
				end += modulo; // 最后一个线程，多处理剩余数量
			}
			
			// 分批待处理数据
			List<?> batchData = data.subList(start, end); 
			
			// 处理线程实例
			ToolThread.Run run = null;
			try {
				run = classes.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			// 数据传递
			Run.setData(batchData);
			
			// 调度执行线程
			execute(run);
		}
	}
	
	/********************************		同步执行		*********************************************/
	
	/**
	 * 线程任务处理基类
	 */
	public static interface BaseHandler{
		public void handler(List<?> data);
	}
	
	/**
	 * 分批处理
	 * @param data			待处理数据
	 * @param threadCount	线程数
	 * @param hander		处理类
	 */
	public static void splitList(List<?> data, int threadCount, final BaseHandler hander){
		int size = data.size();				// 数据量
		if(threadCount <= 0){
			threadCount = 10;				// 线程数
		}
		int batch = size / threadCount;		// 平均每批次处理数量
		int modulo = size % threadCount;	// 剩余数量
		
		List<Thread> threadList = new ArrayList<Thread>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			int start = i * batch;
			int end = i * batch + batch;
			if(i == (threadCount - 1)){
				end += modulo; // 最后一个线程，多处理剩余数量
			}
			final List<?> batchData = data.subList(start, end);
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						// 处理 batchData
						hander.handler(batchData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			thread.setName("ToolThread-splitList-" + (i + 1));
			thread.start();
			threadList.add(thread);
		}
		
		while(true){
			boolean end = true;
			for (Thread thread : threadList) {
				if(!thread.getState().equals(Thread.State.TERMINATED)){
					end = false;
					break;
				}
			}
			if(end){
				return;
			}else{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
