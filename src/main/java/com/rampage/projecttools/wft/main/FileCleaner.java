package com.rampage.projecttools.wft.main;

import java.io.File;

import com.rampage.projecttools.intf.path.PathProcessor;
import com.rampage.projecttools.util.FileUtils;


/**
 * 文件清理工具类
 * @author ziyuqi
 * Version1.0.0 20180330 暂时实现当前最破的需求，清理需求目录下的war包文件
 *
 */
public class FileCleaner {
	private static final String ROOT_CLEAN_PATH = "F:/需求";
	
	
	public static void main(String[] args) {
		// 需求1： 清理需求目录下的War文件
		// 定义好文件处理类，只用遍历一次源文件夹，就能将源文件夹下需要拷贝的内容拷贝过去，并且将目的文件夹中多余的内容删除
        PathProcessor srcProcessor = new PathProcessor() {
            private boolean ignoreSub = false;
             
            @Override
            public boolean processFile(File file) {
            	if (file.getName().endsWith(".war")) {
            		if (file.delete()) {
            			System.out.println("删除文件：" + file.getAbsolutePath() + "成功！");
            		} else {
            			System.out.println("删除文件：" + file.getAbsolutePath() + "失败！");
            		}
            	}
                return true;
            }

            @Override
            public boolean processDir(File dir) {
                ignoreSub = false;
                return true;
            }

            @Override
            public boolean ignoreSub() {
                return this.ignoreSub;
            }
        };
        
        // 遍历源文件路径，将源文件路径较新的文件或者目标路径缺失的文件拷贝过去
        FileUtils.processPathRecursively(new File(ROOT_CLEAN_PATH), srcProcessor);
	}
}
