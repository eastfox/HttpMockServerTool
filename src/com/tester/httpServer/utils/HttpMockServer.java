package com.tester.httpServer.utils;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpMockServer implements Runnable {

	public static HttpHandler httpHandler = new HttpHandler();
	public static HttpContext httpContext = HttpHandler.httpContext;
	public static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public void run() {
		boolean stopMe = httpContext.isStopMe();
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
		// System.out.println("进入run():" + df.format(new Date()));
		while (!stopMe) {
			try {
				// 启动socket服务,5个线程,端口从httpContext获取
				int port = httpContext.getPort();
				ServerSocket svrSocket = new ServerSocket(port, 5);
				Socket socket = null;
				while (!stopMe) {
					socket = svrSocket.accept();
					byte[] buf = new byte[1024 * 1024];
					InputStream in = socket.getInputStream();
					int byteRead = in.read(buf, 0, 1024 * 1024);
					String dataString = new String(buf, 0, byteRead);
					dataString = new String(dataString.getBytes(), "utf-8");
					// System.out.println(dataString);
					String date = df.format(new Date());
					HttpHandler.httpContext
							.getAreaOfConsole()
							.append("-----------------------------------------------"
									+ "-----------------------------------------------"
									+ "-----------------------------------------------"
									+ "\r\n"
									+ "Request-Time: "
									+ date
									+ "\r\n"
									+ dataString + "\r\n");
					String requestHeaders = dataString.substring(0,
							dataString.indexOf("\r\n\r\n"));
					String[] data = requestHeaders.split("\r\n");
					String[] firstLine = data[0].split(" ");
					String requedtMethod = firstLine[0].toUpperCase();
					// 获取请求地址
					String requestUrl = firstLine[1];
					OutputStream out = socket.getOutputStream();
					OutputStreamWriter outSW = new OutputStreamWriter(out,
							"UTF-8");
					BufferedWriter bw = new BufferedWriter(outSW);
					String response = null;
					if (requedtMethod.equals("POST")) {
						// System.out.println(requestUrl + "进入POST循环");
						// 获取请求体数据
						String requestBody = dataString.substring(dataString
								.indexOf("\r\n\r\n") + 1);
						// 获取post请求结果
						// System.out.println("requestUrl=" + requestUrl);
						// System.out.println("requestBody= + requestBody);
						response = HttpHandler.doPost(requestUrl, requestBody);
					} else if (requedtMethod.equals("GET")) {
						// 获取get的请求内容
						response = HttpHandler.doGet(requestUrl);
					}
					bw.write(response); // 向客户端反馈消息，加上分行符以便客户端接收
					bw.flush();
					bw.close();
					outSW.close();
					out.close();
					in.close();
					socket.close();
					stopMe = httpContext.isStopMe();
					if (stopMe) {
						break;
					}
				}
				stopMe = httpContext.isStopMe();
				svrSocket.close();
			} catch (Exception e) {
			}
			stopMe = httpContext.isStopMe();
		}
	}
}