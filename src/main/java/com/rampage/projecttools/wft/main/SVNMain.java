package com.rampage.projecttools.wft.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.rampage.projecttools.util.CMDUtils;


/**
 * SVN操作的相关类 包括但不限于以下功能：从svn上拉分支、修改各个拉出分支的版本号、自动执行mvn命令将版本后缀加到工程中使其能导入eclipse
 * 甚至实现合并代码的功能
 * V1.0.0 20171229 特性: 1. 支持本地自动创建目录用来存放新的工作空间    2. 支持svn拉分支功能  3. 支持将新拉的分支自动checkOut到本地新建工作空间上 （修改版本号，以及执行添加后缀操作，后续实现，目前可能不适用）
 * V1.1.0 20180412 特性： 1.拉分支和checkOut增加对admin-common的支持   2. 增加自动修改版本号的功能
 * V1.1.1 20180413 特性： 1. 增加了自动添加版本号的功能  2. 内嵌了publishMain的操作，实现从分支创建到最后部署一键自动化实现 3. 优化最后版本号的输出，不同模块最终版本号采用换行输出
 * @author ziyuqi
 *
 */
public class SVNMain {
	
	/**
	 * 新的项目的本地地址
	 */
	private static final String WORKSPACE_PATH = "E:/workspacce/ceb20180625";
	
	/**
	 * SVN用户名
	 */
	private static final String SVN_USER_NAME = "ziyuqi";
	
	/**
	 * SVN密码
	 */
	private static final String SVN_PASSWORD = "lbgHuMAHouhRUZ6leVpv";
	
	/**
	 * 分支拉取注释
	 */
	private static final String COMMIT_COMMENT = "光大2018年6月V3.2版本";
	
	/**
	 * 版本号后缀
	 */
	private static final String VERSION_SUFFIX = ".ceb";
	
	private static final String DEFAULT_CHARTSET = "UTF-8";
	
	private static final Pattern VERSION_PATTERN = Pattern.compile("<version>(.*)</version>");
	
	private static final String MODUAL_ADMIN_COMMON = "admin-common";
	
	private static final String MODUAL_CLE = "cle";
	
	private static final String MODUAL_CLE_SUB = "cle-sub";
	
	private static final String MODUAL_ADMIN_WAR = "admin-war";
	
	private static final String MODUAL_SERVICE_WAR = "service-war";
	
	private static final String MODUAL_ACC_WAR = "acc-war";
	
	private static Map<String, String> modualVersionMap = new HashMap<>();
	
	private static final String MAVEN_ADDVERSION_CMD = " mvn -U clean eclipse:clean eclipse:eclipse -DdownloadSources=true -Declipse.addVersionToProjectName=true";
	
	private static final String CMD_PREFIX = "cmd /c cd";
	
	private static final Map<String, Pattern> MODUAL_PATTERN_MAP = new HashMap<String, Pattern>() {
		private static final long serialVersionUID = 1L;
		{
			put("commons", Pattern.compile("<sppay.commons.version>(.*)</sppay.commons.version>"));
			put("cms", Pattern.compile("<sppay.cms.version>(.*)</sppay.cms.version>"));
			put(MODUAL_CLE, Pattern.compile("<sppay.cle.version>(.*)</sppay.cle.version>"));
			put("tra", Pattern.compile("<sppay.tra.version>(.*)</sppay.tra.version>"));
			put("acc", Pattern.compile("<sppay.acc.version>(.*)</sppay.acc.version>"));
			put("die", Pattern.compile("<sppay.die.version>(.*)</sppay.die.version>"));
			put("task", Pattern.compile("<sppay.task.version>(.*)</sppay.task.version>"));
			put("msg", Pattern.compile("<sppay.msg.version>(.*)</sppay.msg.version>"));
			put("rns", Pattern.compile("<sppay.rns.version>(.*)</sppay.rns.version>"));
			put("wop", Pattern.compile("<sppay.wop.version>(.*)</sppay.wop.version>"));
			put("system", Pattern.compile("<sppay.system.version>(.*)</sppay.system.version>"));
			put("pay", Pattern.compile("<sppay.pay.version>(.*)</sppay.pay.version>"));
			put("metrics", Pattern.compile("<sppay.metrics.version>(.*)</sppay.metrics.version>"));
			put("gateway", Pattern.compile("<sppay.gateway.version>(.*)</sppay.gateway.version>"));
			put("sec", Pattern.compile("<sppay.sec.version>(.*)</sppay.sec.version>"));
			put("client.commons", Pattern.compile("<sppay.client.commons.version>(.*)</sppay.client.commons.version>"));
			put("enhance", Pattern.compile("<sppay.enhance.version>(.*)</sppay.enhance.version>"));
			put(MODUAL_CLE_SUB, Pattern.compile("<sppay.cle.ceb.version>(.*)</sppay.cle.ceb.version>"));
			put("seq", Pattern.compile("<sppay.seq.version>(.*)</sppay.seq.version>"));
			put("misc", Pattern.compile("<sppay.misc.version>(.*)</sppay.misc.version>"));
			put("uniform.alerm", Pattern.compile("<sppay.uniform.alerm.version>(.*)</sppay.uniform.alerm.version>"));
			put(MODUAL_ADMIN_COMMON, Pattern.compile("<sppay.admin.common.version>(.*)</sppay.admin.common.version>"));
		}
	};
	
	
	
	/**
	 * 原svn地址和旧svn地址之间的映射关系
	 */
	private static final Map<String, String> SVN_MAP = new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;
		{					
			// -------------------------------光大
			put("http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-war/branches/ceb_v3.5_20180625 ");		// admin-war
			put("http://192.168.1.6:8080/svn/spXmas/code/war/sppay-ceb-service-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/war/sppay-ceb-service-war/branches/ceb_v3.5_20180625 ");			// service-war
			put("http://192.168.1.6:8080/svn/spXmas/code/war/sppay-ceb-acc-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/war/sppay-ceb-acc-war/branches/ceb_v3.5_20180625 ");					// acc-war
			put("http://192.168.1.6:8080/svn/spXmas/code/cle/trunk", "http://192.168.1.6:8080/svn/spXmas/code/cle/branches/ceb_v3.5_20180625 ");														// cle
			put("http://192.168.1.6:8080/svn/spXmas/code/cle-sub/sppay-cle-ceb-service/trunk", "http://192.168.1.6:8080/svn/spXmas/code/cle-sub/sppay-cle-ceb-service/branches/ceb_v3.5_20180625 ");	// cle-sub
			// put("http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-common/trunk", "http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-common/branches/ceb_v3.3_20180507 "); // admin-common
		
			// --------------------------------兴业
			/*put("http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/public-war/sppay-admin-war/branches/CIB_V_4.1");		// admin-war
			put("http://192.168.1.6:8080/svn/spXmas/code/war/sppay-cibszgz-service-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/war/sppay-cibszgz-service-war/branches/CIB_V_4.1");			// service-war
			put("http://192.168.1.6:8080/svn/spXmas/code/war/sppay-cibszgz-acc-war/trunk", "http://192.168.1.6:8080/svn/spXmas/code/war/sppay-cibszgz-acc-war/branches/CIB_V_4.1");					// acc-war
			put("http://192.168.1.6:8080/svn/spXmas/code/cle/trunk", "http://192.168.1.6:8080/svn/spXmas/code/cle/branches/CIB_V_4.1");														// cle
			put("http://192.168.1.6:8080/svn/spXmas/code/cle-sub/sppay-cle-cibszgz-service/trunk", "http://192.168.1.6:8080/svn/spXmas/code/cle-sub/sppay-cle-cibszgz-service/branches/CIB_V_4.1");	// cle-sub
*/
		}
	};
	
	
	public static void main(String[] args) {
		SVNRepositoryFactoryImpl.setup();
		try {
			// STEP1: 创建文件夹
			System.out.println("----------------------------------------------------------STEP1: 开始进行文件夹创建----------------------------------------------");
			createWorkSpaceDir();
			
			// STEP2: 创建分支
			System.out.println("----------------------------------------------------------STEP2: 开始拉取SVN分支----------------------------------------------");
			SVNClientManager clientManager = SVNClientManager.newInstance(
			        (DefaultSVNOptions) SVNWCUtil.createDefaultOptions(true), SVN_USER_NAME, SVN_PASSWORD);
			createBranch(clientManager);
			
			// STEP3: checkOut分支到本地
			System.out.println("----------------------------------------------------------STEP3: 开始CheckOut分支到本地----------------------------------------------");
			checkOut(clientManager);
		
			// STEP4: 修改版本号(大模块的版本后面加上后缀，accWar、serviceWar其他模块版本号与adminWar保持一致)
			System.out.println("----------------------------------------------------------STEP4: 开始修改版本号----------------------------------------------");
			modifyVersion();
		
			// STEP5: 执行mvn clean eclipse:eclipse -Declipse.addVersionToProjectName=true 命令，给工程名后面加上版本后缀
			System.out.println("----------------------------------------------------------STEP5: 开始给工程加上版本号后缀以方便导入eclipse----------------------------------------------");
			addVersionToProject();
			
			// STEP6: 执行publish流程
			System.out.println("----------------------------------------------------------STEP6: 开始执行部署流程----------------------------------------------");
			doPublish();		// 必须保证PublishMain中的路径已经修改正确
		} catch (Exception svne) {
			svne.printStackTrace();
		}
	}

	private static void doPublish() {
		PublishMain.run();
	}

	private static void addVersionToProject() {
		// 执行指令 mvn -U clean eclipse:clean eclipse:eclipse -DdownloadSources=true -Declipse.addVersionToProjectName=true 命令，给文件添加后缀
		 StringBuilder sb = new StringBuilder(256);
		 
		 // 先要进入到指定目录：
		 
		 
		 // admin-common
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_ADMIN_COMMON).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
         
         // cle
         sb.setLength(0);
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_CLE).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
         
         // cle-sub
         sb.setLength(0);
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_CLE_SUB).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
         
         // admin-war
         sb.setLength(0);
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_ADMIN_WAR).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
         
         // service-war
         sb.setLength(0);
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_SERVICE_WAR).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
         
         // acc-war
         sb.setLength(0);
         sb.append(CMD_PREFIX).append(" ").append(WORKSPACE_PATH + File.separator + MODUAL_ACC_WAR).append(" && ").append(MAVEN_ADDVERSION_CMD);
         CMDUtils.execute(sb.toString());
	}

	private static void modifyVersion() throws IOException {
		// STEP1: 先处理cle根目录下pom文件，后面admin-common依赖于cle-facade的版本
		String rootPath = WORKSPACE_PATH + File.separator + MODUAL_CLE;
		String pomFilePath = getPomFilePath(rootPath);
		swicthVersion(pomFilePath, MODUAL_CLE);
		
		// STEP2: 处理admin-common
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_ADMIN_COMMON;
		pomFilePath = getPomFilePath(rootPath);
		swicthVersion(pomFilePath, MODUAL_ADMIN_COMMON);
		
		// STEP3: 处理cle剩下部分
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_CLE;
		swicthVersion(getPomFilePath(rootPath + File.separator + "sppay-cle-facade"), MODUAL_CLE, new MultiPatternMatcherLineProcessor("sppay-cle-facade", false));
		swicthVersion(getPomFilePath(rootPath + File.separator + "sppay-cle-dao"), MODUAL_CLE, new MultiPatternMatcherLineProcessor("sppay-cle-dao", false));
		swicthVersion(getPomFilePath(rootPath + File.separator + "sppay-cle-service"), MODUAL_CLE, new MultiPatternMatcherLineProcessor("sppay-cle-service", false));
		swicthVersion(getPomFilePath(rootPath + File.separator + "sppay-cle-war"), MODUAL_CLE, new MultiPatternMatcherLineProcessor("sppay-cle-war", false));
		swicthVersion(getPomFilePath(rootPath + File.separator + "sppay-cle-controller"), MODUAL_CLE, new MultiPatternMatcherLineProcessor("sppay-cle-controller", false));
		
		// STEP4: 处理ceb-sub
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_CLE_SUB;
		swicthVersion(getPomFilePath(rootPath), MODUAL_CLE_SUB, new MultiPatternMatcherLineProcessor(null, true, true));
		
		// STEP4: 处理admin-war
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_ADMIN_WAR;
		swicthVersion(getPomFilePath(rootPath), MODUAL_ADMIN_WAR, new MultiPatternMatcherLineProcessor(null, true, true));
		
		// STEP5: 处理service-war
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_SERVICE_WAR;
		swicthVersion(getPomFilePath(rootPath), MODUAL_SERVICE_WAR, new MultiPatternMatcherLineProcessor(null, true, true));
		
		// STEP6: 处理acc-war
		rootPath = WORKSPACE_PATH + File.separator + MODUAL_ACC_WAR;
		swicthVersion(getPomFilePath(rootPath), MODUAL_ACC_WAR, new MultiPatternMatcherLineProcessor(null, true, false));
		
		System.out.println("最终版本号列表如下：");
		for (Entry<String, String> entry : modualVersionMap.entrySet()) {
			System.out.println(entry.getKey() + "--->" + entry.getValue());
		}
	}

	
	static class MultiPatternMatcherLineProcessor implements LineProcessor {

		private String subModual;
		
		/**
		 * 是否已经替换过当前模块的版本号 <version></version>标签对中的值，如果替换过，则不需要再次替换
		 */
		private boolean replacedModualVersion;
		
		/**
		 * 是否忽略对parent中的version替换
		 */
		private boolean skipParentVersion = true;
		
		/**
		 * 是否已经进入parent中的version标签
		 */
		private boolean enterParentVersion;
		
		/**
		 *  是否可以决定版本号 对于不能决定版本号的，如果  {@link #modualVersionMap} 中不存在该模块版本，则跳过； 能决定版本号的（admin-war）则将当前模块版本号放入map
		 */
		private boolean decideVersion;
		
		public MultiPatternMatcherLineProcessor() {
		}
		
		public MultiPatternMatcherLineProcessor(boolean skipParentVersion) {
			this.skipParentVersion = skipParentVersion;
		}
		
		public MultiPatternMatcherLineProcessor(String subModual, boolean skipParentVersion) {
			this.subModual = subModual;
			this.skipParentVersion = skipParentVersion;
		}
		
		public MultiPatternMatcherLineProcessor(String subModual, boolean skipParentVersion, boolean decideVersion) {
			this.subModual = subModual;
			this.skipParentVersion = skipParentVersion;
			this.decideVersion = decideVersion;
		}
		
		@Override
		public void processLine(List<String> fileContent, String lineStr, String modual) {
			Matcher matcher = VERSION_PATTERN.matcher(lineStr);
			if (((skipParentVersion && !enterParentVersion) || !skipParentVersion) && !replacedModualVersion && matcher.find()) {
				replacedModualVersion = true;
				// 当前模块的版本直接增加后缀进行替换   {@link #VERSION_SUFFIX} 
				String orginalVersion = matcher.group(1);
				String newVersion = orginalVersion + VERSION_SUFFIX;
				System.out.println("替换【" + modual + (subModual == null ? "" : "--->" + subModual) + "】版本号【" + orginalVersion + "--->" + newVersion + "】......");
				fileContent.add(lineStr.replace(orginalVersion, newVersion));
				modualVersionMap.put(modual, newVersion);
			} else {
				Matcher otherMather = null;
				boolean macthed = false;
				// 遍历找是否有其他模块的版本配置，如果找到则金进行替换
				for (Entry<String, Pattern> entry : MODUAL_PATTERN_MAP.entrySet()) {
					otherMather = entry.getValue().matcher(lineStr);
					if (otherMather.find()) {
						String orginalVersion = otherMather.group(1);
						String newVersion = modualVersionMap.get(entry.getKey());
						macthed = true;
						if (newVersion != null && !orginalVersion.equals(newVersion)) {
							System.out.println("替换【" + modual + (subModual == null ? "" : "--->" + subModual) + "】" + "下的【" + entry.getKey() + "】版本号【" + orginalVersion + "--->" + newVersion + "】......");
							fileContent.add(lineStr.replace(orginalVersion, newVersion));
							if (decideVersion) {
								modualVersionMap.put(entry.getKey(), newVersion);
							}
						} else {
							fileContent.add(lineStr);
							if (decideVersion) {
								modualVersionMap.put(entry.getKey(), orginalVersion);
							}
						}
						break;
					}
				}
				
				// 如果未找到，则当做普通字符串处理
				if (!macthed) {
					if (lineStr.indexOf("<parent>") != -1) {
						enterParentVersion = true;
					} else if (lineStr.indexOf("</parent>") != -1) {
						enterParentVersion = false;
					}
					fileContent.add(lineStr);
				}
			}
		} 
		
	}

	private static void swicthVersion(String pomFilePath, String modual) throws IOException {
		swicthVersion(pomFilePath, modual, new MultiPatternMatcherLineProcessor());
	}

	public interface LineProcessor {
		void processLine(List<String> fileContent, String lineStr, String modual);
	}
	
	/**
	 * 给原来的版本加上后缀
	 * @param pomFilePath  pom文件地址
	 * @param modual		模块名称
	 * @param lineProcessor	每行字符串的处理器
	 * @throws IOException
	 */
	private static void swicthVersion(String pomFilePath, String modual, LineProcessor lineProcessor) throws IOException {
		List<String> fileContent = new ArrayList<>();
		File pomFile = new File(pomFilePath);
		if (!pomFile.exists()) {
			System.out.println("文件" + pomFilePath + "不存在，跳过版本号修改！");
			return;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pomFilePath), DEFAULT_CHARTSET))) {
			String lineStr = br.readLine();
			// 替换版本号
			while (lineStr != null) {
				lineProcessor.processLine(fileContent, lineStr, modual);
				lineStr = br.readLine();
			}
		}
		writeFileWithContents(pomFilePath, fileContent);
	}

	private static void writeFileWithContents(String destFile, List<String> fileContents) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), DEFAULT_CHARTSET))) {
			for (String one : fileContents) {
				bw.write(one);
				bw.newLine();
			}
		}
	}

	private static String getPomFilePath(String rootPath) {
		return rootPath + File.separator + "pom.xml";
	}

	private static void checkOut(SVNClientManager clientManager) throws SVNException {
		String subDir = null;
		long nowRevision = -1;
		for (String destUrl : SVN_MAP.values()) {
			subDir = getSubDir(destUrl);
			nowRevision = clientManager.getUpdateClient().doCheckout(SVNURL.parseURIEncoded(destUrl),
			        new File(WORKSPACE_PATH + File.separator + subDir), SVNRevision.HEAD, SVNRevision.HEAD,
			        SVNDepth.INFINITY, true);
			System.out.println("checkOut路径【" + destUrl + "】成功，当前检出的版本号是：" + nowRevision);
		}
	}

	private static String getSubDir(String destUrl) {
		if (destUrl.indexOf("sppay-admin-war") != -1) {
			return MODUAL_ADMIN_WAR;
		} else if (destUrl.indexOf("sppay-ceb-service-war") != -1 || destUrl.indexOf("sppay-cibszgz-service-war") != -1) {
			return MODUAL_SERVICE_WAR;
		} else if (destUrl.indexOf("sppay-ceb-acc-war") != -1 || destUrl.indexOf("sppay-cibszgz-acc-war") != -1) {
			return MODUAL_ACC_WAR;
		} else if (destUrl.indexOf("cle-sub") != -1) {
			return MODUAL_CLE_SUB;
		} else if (destUrl.indexOf("cle/") != -1) {
			return MODUAL_CLE;
		} else if (destUrl.indexOf("/sppay-admin-common/") != -1) {
			return MODUAL_ADMIN_COMMON;
		}
		
		// 其他为暂时不支持的目录类型
		throw new RuntimeException("Not supported branch!");
	}

	private static void createBranch(SVNClientManager clientManager) throws SVNException {
		SVNCopyClient copyClient = clientManager.getCopyClient();
		copyClient.setIgnoreExternals(false);
		SVNURL repositoryOptUrl = null;
		SVNURL destUrl = null;
		SVNCopySource[] copySources = new SVNCopySource[1];
		for (Entry<String, String> entry : SVN_MAP.entrySet()) {
			try {
				repositoryOptUrl = SVNURL.parseURIEncoded(entry.getKey());
				destUrl = SVNURL.parseURIEncoded(entry.getValue());
				copySources[0] = new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, repositoryOptUrl);
				copyClient.doCopy(copySources, destUrl, false, false, true, COMMIT_COMMENT, null);
				System.out.println("从【" + entry.getKey() + "】拉取分支到【" + entry.getValue() + "】成功!");
			} catch (Exception e) {
				System.out.println("从【" + entry.getKey() + "】拉取分支到【" + entry.getValue() + "】失败!");

			}
		}
	}

	private static void createWorkSpaceDir() {
		File workSpaceDir = new File(WORKSPACE_PATH);
		if (!workSpaceDir.exists()) {
			if (workSpaceDir.mkdirs()) {
				System.out.println("文件目录【" + WORKSPACE_PATH + "】创建成功！");
			} else {
				System.out.println("文件目录【" + WORKSPACE_PATH + "】创建失败！");
			}
		} else {
			System.out.println("文件目录【" + WORKSPACE_PATH + "】已经存在，跳过创建！");
		}
	}
}
