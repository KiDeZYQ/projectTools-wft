package com.rampage.projecttools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CMDUtils {

	private CMDUtils() {
	}
	
	/**
	 * 执行cmd命令
	 * @param cmd cmd命令字符串
	 */
	public static void execute(String cmd) {
		Runtime runtime = Runtime.getRuntime();
		BufferedReader br = null;
		try {
			Process process = runtime.exec(cmd);
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("----------------------------------------------------------命令【" + cmd
			        + "】执行失败！----------------------------------------------------------");
			System.exit(-1);
		} finally {
			IOUtils.closeQuietly(br);
		}
	}
}
