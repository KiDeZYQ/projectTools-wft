package com.rampage.projecttools.wft.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rampage.projecttools.util.StringUtils;


/**
 * @author ziyuqi
 *
 */
public class ReadMeMain {
	/**
	 * 是否全量升级
	 */
	private static boolean fullUpload = false;

	/**
	 * 需要替换的jar包
	 */
	private static List<String> replacedJars = Arrays.asList("sppay-cle-ceb-service-2.2.jar", "sppay-cle-service-1.69.ceb.jar");

	/**
	 * 需要替换的其他文件路径，一般其他文件都是ROOT应用里面的，所以路径只需指定在ROOT应用的WEB-INF下的相对路径即可
	 */
	private static Map<String, List<String>> otherFiles = new LinkedHashMap<String, List<String>>() {
		private static final long serialVersionUID = 1L;

		{
			// 相对于WEB-INF的路径
			this.put(APP_ROOT, Arrays.<String> asList("page/cms/base/channel-add.jsp"));
			this.put(APP_SERVICE, Arrays.<String> asList());
			this.put(APP_ACC, Arrays.<String> asList());
		}
	};

	private static final String ROOT_WEB_PATH = "/home/weixin35/szceb-3.5/webapps/ROOT/WEB-INF/";

	private static final String SERVICE_WEB_PATH = "/home/weixin35/szceb-3.5-service/webapps/sppay-service-war/WEB-INF/";

	private static final String ACC_WEB_PATH = "/home/weixin35/szceb-3.5-acc/webapps/sppay-acc-war/WEB-INF/";

	private static Map<String, List<String>> appJarsMap = new HashMap<String, List<String>>(3);

	private static Map<String, String> appRootPathesMap = new HashMap<String, String>(3);

	private static final String APP_ROOT = "ROOT";

	private static final String APP_SERVICE = "service";

	private static final String APP_ACC = "acc";

	private static final String LIB_RELATIVE_PATH = "lib";

	private static final String LOCAL_ROOT_LIB_PATH = "E:/web/webapps/admin-ceb/WEB-INF/lib";

	private static final String LOCAL_SERVICE_LIB_PATH = "E:/service/webapps/service-ceb/WEB-INF/lib";

	private static final String LOCAL_ACC_LIB_PATH = "E:/acc/webapps/service-acc/WEB-INF/lib";

	static {
		appJarsMap.put(APP_ROOT, new ArrayList<String>());
		appJarsMap.put(APP_SERVICE, new ArrayList<String>());
		appJarsMap.put(APP_ACC, new ArrayList<String>());

		appRootPathesMap.put(APP_ROOT, ROOT_WEB_PATH);
		appRootPathesMap.put(APP_SERVICE, SERVICE_WEB_PATH);
		appRootPathesMap.put(APP_ACC, ACC_WEB_PATH);
	}

	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder(1024);
		if (fullUpload) {
			sb.append("本次升级为全量升级, 具体步骤如下: \n").append("1. 执行SQL\n").append("2. 修改配置文件配置：\n")
			        .append("3. 全量替换光大深圳私有云ROOT、service、acc应用war包 (两台都需替换) \n")
			        .append("4. 重启光大深圳私有云应用ROOT、service、acc    (两台都需重启) ");
			System.out.println(sb);
			return;
		}

		sb.append("具体操作步骤如下：\n");
		Map<String, List<String>> appReplacedJarsMap = new LinkedHashMap<String, List<String>>();
		if (replacedJars != null && !replacedJars.isEmpty()) {
			// 待替换的jar包不为空，则遍历jar包看应该放在哪
			initWebAppJars();
			appReplacedJarsMap.put(APP_ROOT, new ArrayList<String>(replacedJars.size()));
			appReplacedJarsMap.put(APP_SERVICE, new ArrayList<String>(replacedJars.size()));
			appReplacedJarsMap.put(APP_ACC, new ArrayList<String>(replacedJars.size()));
			List<String> apps = null;
			for (String replacedJar : replacedJars) {
				apps = getJarBelongApp(replacedJar);
				if (apps != null) {
					for (String app : apps) {
						appReplacedJarsMap.get(app).add(replacedJar);
					}
				}
			}
		}

		boolean restartRoot = false;
		boolean restartService = false;
		boolean restartAcc = false;
		int i = 1;
		if (!appReplacedJarsMap.isEmpty()) {
			for (Entry<String, List<String>> entry : appReplacedJarsMap.entrySet()) {
				if (entry.getValue() == null || entry.getValue().isEmpty()) {
					continue;
				}

				// 替换了jar包肯定要重启
				if (APP_ROOT.equals(entry.getKey())) {
					restartRoot = true;
				} else if (APP_SERVICE.equals(entry.getKey())) {
					restartService = true;
				} else if (APP_ACC.equals(entry.getKey())) {
					restartAcc = true;
				}

				sb.append(i).append(". 进入光大深圳私有云").append(entry.getKey()).append("应用jar包路径: ")
				        .append(appRootPathesMap.get(entry.getKey())).append(LIB_RELATIVE_PATH).append("\n")
				        .append("用补丁jar包: ");
				for (String jar : entry.getValue()) {
					sb.append(jar).append("、 ");
				}
				sb.deleteCharAt(sb.length() - 2);
				sb.append("替换当前路径中jar包");
				if (APP_SERVICE.equals(entry.getKey()) || APP_ROOT.equals(entry.getKey())) {
					sb.append(" (两台都需替换)");
				}
				sb.append("\n");
				i++;
			}
		}

		// 替换其他文件的补丁
		for (Entry<String, List<String>> entry : otherFiles.entrySet()) {
			if (entry.getValue() == null || entry.getValue().isEmpty()) {
				continue;
			}
			sb.append(i).append(". 进入").append(entry.getKey()).append("应用\n");
			for (String path : entry.getValue()) {
				sb.append("路径: ").append(appRootPathesMap.get(entry.getKey()))
				        .append(path.substring(0, path.lastIndexOf("/"))).append(" 用补丁文件: ")
				        .append(path.substring(path.lastIndexOf("/") + 1, path.length())).append(" 替换当前路径文件 ").append("\n");
				if (entry.getValue().indexOf(".jsp") != -1) {
					// 替换了jsp外的文件也需要重启
					if (APP_ROOT.equals(entry.getKey())) {
						restartRoot = true;
					} else if (APP_SERVICE.equals(entry.getKey())) {
						restartService = true;
					} else if (APP_ACC.equals(entry.getKey())) {
						restartAcc = true;
					}
				}
			}
			if (!APP_ACC.equals(entry.getKey())) {
				sb.append(" (两台都需替换)");
			}
			i++;
		}
		
		sb.append("\n\n");
		if (restartRoot || restartService || restartAcc) {
			sb.append(i).append(". ").append("重启光大深圳私有云 ");
			if (restartRoot) {
				sb.append(APP_ROOT).append("、");
			}
			if (restartService) {
				sb.append(APP_SERVICE).append("、");
			}
			if (restartAcc) {
				sb.append(APP_ACC);
			}
		} else {
			sb.append("不需要重启应用!");
		}
		
		if (restartRoot || restartService) {
			sb.append(" (两台都需重启)");
		}

		System.out.println(sb);
	}

	/**
	 * 初始化web应用和jar包间的映射关系
	 */
	private static void initWebAppJars() {
		File libPath = new File(LOCAL_ROOT_LIB_PATH);
		String[] arrJars = libPath.list();
		for (String jar : arrJars) {
			appJarsMap.get(APP_ROOT).add(jar.substring(0, jar.lastIndexOf("-")));
		}

		libPath = new File(LOCAL_SERVICE_LIB_PATH);
		arrJars = libPath.list();
		for (String jar : arrJars) {
			appJarsMap.get(APP_SERVICE).add(jar.substring(0, jar.lastIndexOf("-")));
		}

		libPath = new File(LOCAL_ACC_LIB_PATH);
		arrJars = libPath.list();
		for (String jar : arrJars) {
			appJarsMap.get(APP_ACC).add(jar.substring(0, jar.lastIndexOf("-")));
		}
	}

	/**
	 * 得到jar包所属应用
	 * 
	 * @param replacedJar
	 *            待替换的jar包
	 * @return 得到jar包所属应用的列表
	 */
	private static List<String> getJarBelongApp(String replacedJar) {
		if (StringUtils.isEmpty(replacedJar)) {
			return null;
		}

		boolean found = false;
		List<String> apps = new ArrayList<String>(3);
		if (appJarsMap.get(APP_ROOT).contains(replacedJar.substring(0, replacedJar.lastIndexOf("-")))) {
			found = true;
			apps.add(APP_ROOT);
		}
		if (appJarsMap.get(APP_SERVICE).contains(replacedJar.substring(0, replacedJar.lastIndexOf("-")))) {
			found = true;
			apps.add(APP_SERVICE);
		}
		if (appJarsMap.get(APP_ACC).contains(replacedJar.substring(0, replacedJar.lastIndexOf("-")))) {
			found = true;
			apps.add(APP_ACC);
		}
		if (!found) {
			throw new RuntimeException("未找到jar包【" + replacedJar + "】所属的光大的应用！");
		}

		return apps;
	}
}
