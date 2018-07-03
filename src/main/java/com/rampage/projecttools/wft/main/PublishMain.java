package com.rampage.projecttools.wft.main;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.rampage.projecttools.intf.path.PathProcessor;
import com.rampage.projecttools.util.CMDUtils;
import com.rampage.projecttools.util.FileUtils;
import com.rampage.projecttools.util.StringUtils;
import com.rampage.projecttools.wft.main.model.Context;


/**
 * @author ziyuqi
 * V1.0.0 版本 特性： 支持maven install打包（cle、cle-sub、admin-war、service-war、acc-war五个模块） + 支持拷贝最新文件到对应应用路径
 * V1.0.1 20180111   特性：支持多线程打包和拷贝文件
 */
public class PublishMain {
    private static final String SCHEMA = "ceb";
    
    private static final String MAVEN_INSTALL_CMD = "mvn clean install -U";
    
    private static final String CMD_PREFIX = "cmd /c cd";
    
    private static final String ADMIN_SERVER_PATH = "E:/web/webapps";
    
    private static final String SERVIC_SERVER_PATH = "E:/service/webapps";
    
    private static final String ACC_SERVICE_PATH = "E:/acc/webapps";
    
    private static final String CLE_SERVER_PATH = "E:/cle/webapps";
    
    private static final String CLE2_SERVER_PATH = "E:/cle2/webapps";
    
    private static final String CLE3_SERVER_PATH = "E:/cle3/webapps";
    
    private static final List<String> NOT_NEED_DELETE_FILES = Arrays.asList("app-config.properties", "cache.properties", "log4j2.xml", "zjb@111111.pfx", "epay-sdk-1.0.jar", "koal_sun-1.0.jar");
    
    private static final List<String> NOT_NEED_COPY_FILES = Arrays.asList("log4j2.xml");
    
    // root是否采用新的模式，将class文件都打到jar包里面
    private static final boolean IS_ROOT_TO_JAR = false;
    
    // 线程数量不至于太多，这里直接创建自动适配的线程池
    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();
    
    public static void run() {
		long startTime = System.currentTimeMillis();
        // STEP1: 调用maven打包
    	processMavenInstall();
        
        // STEP2: 拷贝maven打包后的文件
        processFileCopy();
        
        // STEP3: 修改catalina.properties文件中配置加载的service
        processModifyProperties();
        
        // STEP4: 关闭线程池
        EXECUTORS.shutdownNow();
        System.out.println("---TotalCost:【" + (System.currentTimeMillis() - startTime) + "】ms!");
	}
    
    public static void main(String[] args) {
    	run();
    }

    @Deprecated
    private static void processModifyProperties() {
        // TODO: 暂时不考虑
    }


    private static boolean contentChange(File file1, File file2) {
        try {
        	MessageDigest md5Instance = MessageDigest.getInstance("MD5");
            // 得到两个文件的md5值然后进行比较
            md5Instance.update(FileUtils.readFile2Bytes(file1));  
            BigInteger bi = new BigInteger(1, md5Instance.digest());  
            String mdStr1 = bi.toString(16);
            
            md5Instance.update(FileUtils.readFile2Bytes(file2));  
            bi = new BigInteger(1, md5Instance.digest());  
            String mdStr2 = bi.toString(16); 
            
            return !mdStr1.equals(mdStr2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean needCopyFile(File srcFile, File destFile) {
        // 目标文件不存在，直接拷贝源文件过去
        if (!destFile.exists()) {
            return true;
        }
        
        if (NOT_NEED_COPY_FILES.contains(srcFile.getName().toLowerCase())) {
            return false;
        }
        return contentChange(srcFile, destFile);
    }
    
    private static void processFileCopy() {
        System.out.println("----------------------------------------------------------STEP2: 开始进行文件拷贝----------------------------------------------------------");
        Map<String, String> schemaMap = Context.orgMap.get(SCHEMA);
        
        // 多线程拷贝各个工程里面的内容
        List<Future<Void>> futures = new ArrayList<>(3);
        String baseSrcDir = schemaMap.get(Context.SERVICE_WAR_PROJECT) + File.separator + "WebContent";
        String baseDestDir = SERVIC_SERVER_PATH + File.separator + schemaMap.get(Context.SERVICE);
        futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir)));   		// service-war
        baseSrcDir = schemaMap.get(Context.ACC_WAR_PROJECT) + File.separator + "WebContent";
        baseDestDir = ACC_SERVICE_PATH + File.separator + schemaMap.get(Context.ACC);
        futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir)));   		// acc-war
        baseSrcDir = schemaMap.get(Context.ADMIN_WAR_PROJECT) + File.separator + "WebContent";
        baseDestDir = ADMIN_SERVER_PATH + File.separator + schemaMap.get(Context.ADMIN);
        futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir))); 			// admin-war
        
        baseSrcDir = schemaMap.get(Context.CLE_WAR_PROJECT) + File.separator + "WebContent";
        baseDestDir = CLE_SERVER_PATH + File.separator + schemaMap.get(Context.CLE);
        // futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir))); 			// cle-war
        /* baseDestDir = CLE2_SERVER_PATH + File.separator + schemaMap.get(Context.CLE);
        futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir))); 			// cle2-war
        baseDestDir = CLE3_SERVER_PATH + File.separator + schemaMap.get(Context.CLE);
        futures.add(EXECUTORS.submit(new CopyTask(baseSrcDir, baseDestDir))); 			// cle3-war
        */        
        for (Future<Void> future : futures) {
        	try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
        }
        
        // 删除admin下面class/cn下面的相关class文件
        deleteRootClasses();
    }

    private static void deleteRootClasses() {
    	if (!IS_ROOT_TO_JAR) {
    		return;
    	}
    	
        String	baseDestDir = ADMIN_SERVER_PATH + File.separator + Context.orgMap.get(SCHEMA).get(Context.ADMIN) + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "cn";
    	PathProcessor pathProcessor = new PathProcessor() {
			@Override
			public boolean processFile(File file) {
				if (file.delete()) {
					System.out.println("删除目标文件：" + file.getAbsolutePath() + " 成功！");
				} else {
					System.out.println("删除目标文件：" + file.getAbsolutePath() + " 失败！");
				}
				return true;
			}
			@Override
			public boolean processDir(File dir) {
				return true;
			}
			@Override
			public boolean ignoreSub() {
				return false;
			}
		};
		FileUtils.processPathRecursively(new File(baseDestDir), pathProcessor);
	}

	private static class CopyTask implements Callable<Void> {
    	
    	private final String baseSrcDir;
    	
    	private final String baseDestDir;
    	
    	public CopyTask(String baseSrcDir, String baseDestDir) {
    		this.baseSrcDir = baseSrcDir;
    		this.baseDestDir = baseDestDir;
		}
    	
		@Override
		public Void call() throws Exception {
			copyFiles(baseSrcDir, baseDestDir);
			return null;
		}
    	
    }

    private static void copyFiles(final String baseSrcDir, final String baseDestDir) {
        // 定义好文件处理类，只用遍历一次源文件夹，就能将源文件夹下需要拷贝的内容拷贝过去，并且将目的文件夹中多余的内容删除
        PathProcessor srcProcessor = new PathProcessor() {
            private boolean ignoreSub = false;
             
            @Override
            public boolean processFile(File file) {
                return true;
            }

            @Override
            public boolean processDir(File dir) {
                ignoreSub = false;
                File destDir = new File(baseDestDir + dir.getAbsolutePath().substring(baseSrcDir.length()));
                
                // 目标文件夹不存在，直接拷贝整个文件夹过去
                if (!destDir.exists() || destDir.listFiles() == null) {
                    ignoreSub = true;       // 因为是下面子目录都会进行拷贝，所以不需要再去处理自路径了
                    try {
                        if (!FileUtils.copyPath(dir, destDir)) {
                            System.out.println("拷贝文件夹：" + dir + " 到目标文件夹：" + destDir + " 失败！");
                            return false;
                        } else {
                            System.out.println("拷贝文件夹：" + dir + " 到目标文件夹：" + destDir + " 成功！");
                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("拷贝文件夹：" + dir + " 到目标文件夹：" + destDir + " 失败！");
                        return false;
                    }
                }
                
                File[] destSubFiles = destDir.listFiles();
                File[] srcSubFiles = dir.listFiles();
                if (srcSubFiles == null) {
                    srcSubFiles = new File[0];
                }
                for (File destSubFile : destSubFiles) {
                    boolean needDelete = true;
                    for (int i=0; i<srcSubFiles.length; i++) {
                        // 如果源地址也有对应的路径
                        if (srcSubFiles[i] != null && srcSubFiles[i].getName().equalsIgnoreCase(destSubFile.getName())) {
                            if (srcSubFiles[i].isFile()) {
                                // 对于源文件中存在而目标文件中不存在或者与源文件中不一致的情况下，用源文件夹中的文件覆盖目标文件夹中的文件
                                if (needCopyFile(srcSubFiles[i], destSubFile)) {
                                    try {
                                        if (FileUtils.copyFile(srcSubFiles[i], destSubFile)) {
                                            System.out.println("拷贝文件：" + srcSubFiles[i] + " 到目标文件：" + destSubFile + " 成功！");
                                        } else {
                                            System.out.println("拷贝文件：" + srcSubFiles[i] + " 到目标文件：" + destSubFile + " 失败！");
                                            return false;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("拷贝文件：" + srcSubFiles[i] + " 到目标文件：" + destSubFile + " 失败！");
                                        return false;
                                    }
                                }
                            }
                            srcSubFiles[i] = null;
                            needDelete = false;
                            break;
                        }
                    }
                    
                    // 如果需要删除
                    if (needDelete) {
                        if (!NOT_NEED_DELETE_FILES.contains(destSubFile.getName().toLowerCase())) {
                            if (FileUtils.deletePath(destSubFile)) {
                                System.out.println("删除目标路径：" + destSubFile + " 成功！");
                            } else {
                                System.out.println("删除目标路径：" + destSubFile + " 失败！");
                            }
                        }
                    }
                }
                
                // 源文件有的文件而目标文件没有的文件，则进行拷贝
                for (File srcSubFile : srcSubFiles) {
                    if (srcSubFile != null && srcSubFile.isFile()) {
                        try {
                            if (FileUtils.copyPath(srcSubFile, destDir)) {
                                System.out.println("拷贝文件：" + srcSubFile + " 到目标路径：" + destDir + " 成功！");
                            } else {
                                System.out.println("拷贝文件：" + srcSubFile + " 到目标路径：" + destDir + " 失败！");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("拷贝文件：" + srcSubFile + " 到目标路径：" + destDir + " 失败！");
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean ignoreSub() {
                return this.ignoreSub;
            }
        };
        
        // 遍历源文件路径，将源文件路径较新的文件或者目标路径缺失的文件拷贝过去
        FileUtils.processPathRecursively(new File(baseSrcDir), srcProcessor);
    } 

    private static void processMavenInstall() {
        System.out.println("----------------------------------------------------------STEP1: 开始进行maven打包----------------------------------------------------------");
        final Map<String, String> schemaMap = Context.orgMap.get(SCHEMA);
        // admin-common、CLE和CLE-Sub打包要按顺序来
        processSingleProjectMavenInstall(schemaMap.get(Context.ADMIN_COMMON_PROJECT));           // admin-common
        processSingleProjectMavenInstall(schemaMap.get(Context.CLE_PROJECT));           // cle
        processSingleProjectMavenInstall(schemaMap.get(Context.CLE_SUB_PROJECT));       // cle-sub
        
        List<Future<Void>> futures = new ArrayList<>(3);
        // futures.add(EXECUTORS.submit(new InstallTask(schemaMap.get(Context.CLE_WAR_PROJECT))));   // cle-war
        futures.add(EXECUTORS.submit(new InstallTask(schemaMap.get(Context.ADMIN_WAR_PROJECT))));   // admin-war
        futures.add(EXECUTORS.submit(new InstallTask(schemaMap.get(Context.SERVICE_WAR_PROJECT)))); // service-war
        futures.add(EXECUTORS.submit(new InstallTask(schemaMap.get(Context.ACC_WAR_PROJECT))));		// acc-war
        for (Future<Void> future : futures) {
        	try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				System.exit(-1);
			}
        }
    }
    
    /**
     * install任务
     * @author ziyuqi
     *
     */
    private static class InstallTask implements Callable<Void> {
    	public final String projectPath;
    	
    	public InstallTask(String projectPath) {
    		this.projectPath = projectPath;
    	}

    	@Override
		public Void call() throws Exception {
			processSingleProjectMavenInstall(projectPath);
			return null;
		}
    }

    private static void processSingleProjectMavenInstall(String projectPath) {
        if (StringUtils.isEmpty(projectPath)) {
            return;
        }
        StringBuilder sb = new StringBuilder(256);
        sb.append(CMD_PREFIX).append(" ").append(projectPath).append(" && ").append(MAVEN_INSTALL_CMD);
        CMDUtils.execute(sb.toString());
    }
}
