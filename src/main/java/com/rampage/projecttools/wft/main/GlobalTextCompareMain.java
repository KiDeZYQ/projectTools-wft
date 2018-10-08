package com.rampage.projecttools.wft.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.rampage.projecttools.util.IOUtils;
import com.rampage.projecttools.util.StringUtils;


/**
 * 文本对比工具类 (只对比整体内容，如果内容出现错行，只要内容一致，就认为没有差异) 
 * v1.0.0 
 * 		2018-01-05 特性： 支持beyond compare外的错行匹配（即本来是同样内容，只不过行号错开了） + 支持自定义匹配规则
 * V1.0.1 
 * 		2018-07-03特性：修改清分文件的对比模式，因为可能排序条件不一样导致相同的记录在不同的清分文件，从而导致比对失败。
 * 		这里修改比较逻辑，先将所有记录放入list，然后再进行比较
 * @author ziyuqi
 *
 */
public class GlobalTextCompareMain {
	
	private static final String CHARSET = "GBK";

	private static final List<String> LEFT_FILES = Arrays.asList(
			"C:/Users/admin/Desktop/cleaningOld/光大银行_20170503_1_1.csv",
	        "C:/Users/admin/Desktop/cleaningOld/光大银行_20170503_2_1.csv",
	        "C:/Users/admin/Desktop/cleaningOld/光大银行_20170503_2_2.csv",
	        "C:/Users/admin/Desktop/cleaningOld/光大银行_20170503_2_3.csv"
			/*"E:/acc/tempfile/invoice/201801/WFT_KH_10000_20180105_001.txt",
			"E:/acc/tempfile/invoice/201801/WFT_JY_20000_20180105_001.txt"*/
			/*"C:/Users/admin/Desktop/1.txt"*/
	        );
	    

	private static final List<String> RIGHT_FILES = Arrays.asList(
		   "C:/Users/admin/Desktop/cleaningNew/光大银行_20170503_1_1.csv",
	        "C:/Users/admin/Desktop/cleaningNew/光大银行_20170503_2_1.csv",
	        "C:/Users/admin/Desktop/cleaningNew/光大银行_20170503_2_2.csv",
	        "C:/Users/admin/Desktop/cleaningNew/光大银行_20170503_2_3.csv"
			/*"E:/acc/tempfile/invoice/201801/WFT_KH_10000_20180105_002.txt",
			"E:/acc/tempfile/invoice/201801/WFT_JY_20000_20180105_002.txt"*/
			/*"C:/Users/admin/Desktop/2.txt"*/
	        );

	
	public static void main(String[] args) {
		System.out.println(
		        "-------------------------------------------开始进行文件比较---------------------------------------------");
		// 简单校验左右两侧文件数量应该一致
		if (LEFT_FILES.size() != RIGHT_FILES.size()) {
			throw new RuntimeException("待对比的左右两侧文件数量不一致！");
		}
		
		// 原始版本必须要左右两侧文件按顺序逐个比对
		/*for (int i = 0; i < LEFT_FILES.size(); i++) {
			// TrimIgnoreCaseComparator CebCleanFileTextComparator
			compareFile(new File(LEFT_FILES.get(i)), new File(RIGHT_FILES.get(i)), new CebCleanFileTextComparator());
		}*/
		// List比较
		compareFiles(new CebCleanFileTextComparator());
		System.out.println(
		        "---------------------------------------------文件比较结束---------------------------------------------");
	}
	

	private static void compareFiles(CebCleanFileTextComparator cebCleanFileTextComparator) {
		List<String> fileStr1 = LEFT_FILES.stream().map(file -> {
			return getCleaningDatas(file);
		}).flatMap(list -> list.stream()).collect(Collectors.toList());
		
		List<String> fileStr2 = RIGHT_FILES.stream().map(file -> {
			return getCleaningDatas(file);
		}).flatMap(list -> list.stream()).collect(Collectors.toList());
		
		for (String str1 : fileStr1) {
			if (!fileStr2.contains(str1)) {
				System.out.println("左侧文件存在【" + str1 + "】在右侧文件中不存在！");
			}
		}
		
		for (String str2 : fileStr2) {
			if (!fileStr1.contains(str2)) {
				System.out.println("右侧文件存在【" + str2 + "】在左侧文件中不存在！");
			}
		}
	}

	/**
	 * 得到可比较的清分数据
	 * @param file 文件名称
	 * @return 数据列列表
	 */
	private static List<String> getCleaningDatas(String file) {
		List<String> oneFileStr = new ArrayList<>();
		String line = null;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET))) {
			line = br.readLine();
			while (line != null) {
				if (StringUtils.isEmpty(line)) {
					line = br.readLine();
					continue;
				}
				if (line.startsWith("结算单号")) {
					line = br.readLine();
					continue;
				}
				String[] strArr = line.split(",");
				StringBuilder sb = new StringBuilder();
				for (int i=1; i<strArr.length; i++) {
					if (i != strArr.length - 1) {
						sb.append(strArr[i]);
					} else {
						sb.append(strArr[i].substring(0, 13));
					}
				}
				oneFileStr.add(sb.toString());
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oneFileStr;
	}


	/**
	 * 比较文件
	 * @param leftFile
	 * @param rightFile
	 * @param textComparator
	 */
	private static void compareFile(File leftFile, File rightFile, TextComparable textComparator) {
		System.out.println("当前比较的左侧文件为【" + leftFile + "】, 右侧文件为【" + rightFile + "】, 当前比较器为【" + textComparator.getClass().getSimpleName() + "】!");
		
		// STEP1: 开始进行比较，得到差异语句列表
		List<String> leftDifferences = new ArrayList<>();
		List<String> rightDifferences = new ArrayList<>();
		doCompare(leftDifferences, rightDifferences, leftFile, rightFile, textComparator);
		if (leftDifferences.isEmpty() && rightDifferences.isEmpty()) {		
			// 不存在差异列表。则说明两个文件内容一致，这里直接返回
			System.out.println("【无差异】经比较，左右文件内容一致!");
			return;
		}
		
		// STEP2: 输出差异详情
		listDifference(leftDifferences, rightDifferences);
	}

	/**
	 * 文件具体的比较细节
	 * @param leftDifferences   左侧差异列表
	 * @param rightDifferences  右侧差异列表
	 * @param leftFile		    左侧文件
	 * @param rightFile		    右侧文件
	 * @param textComparator   文本比较器
	 */
	private static void doCompare(List<String> leftDifferences, List<String> rightDifferences, File leftFile,
	        File rightFile, TextComparable textComparator) {
		BufferedReader leftFileBr = null;
		BufferedReader rightFileBr = null;
		try {
			leftFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(leftFile), CHARSET));
			rightFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(rightFile), CHARSET));
			String leftStr = leftFileBr.readLine();
			String rightStr = rightFileBr.readLine();
			while (leftStr != null || rightStr != null) {
				// 只有不相同的时候才考虑是否放入对比队列 --- 不能简单的通过逐行调用比较器比较来判断是否差异行，考虑错行的情况
				if (!checkSameText(leftDifferences, rightDifferences, leftStr, rightStr, textComparator)) {
					if (StringUtils.isNotEmpty(leftStr)) {
						leftDifferences.add(leftStr);
					}
					if (StringUtils.isNotEmpty(rightStr)) {
						rightDifferences.add(rightStr);
					}
				}

				// 不为空，则读取下一行
				if (leftStr != null) {
					leftStr = leftFileBr.readLine();
				}
				if (rightStr != null) {
					rightStr = rightFileBr.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			IOUtils.closeQuietly(leftFileBr);
			IOUtils.closeQuietly(rightFileBr);
		}
	}

	/**
	 * 校验同文件内容
	 * @param leftDifferences   左侧文件差异列表
	 * @param rightDifferences  右侧文件差异列表
	 * @param leftStr		    当前比对的左侧字符串
	 * @param rightStr		    当前比对的右侧字符串
	 * @param textComparator   比较器
	 * @return  true, 当前左侧或者右侧出现的语句在左右文件中存在;false，其他，未找到匹配。
	 */
	private static boolean checkSameText(List<String> leftDifferences, List<String> rightDifferences, String leftStr,
	        String rightStr, TextComparable textComparator) {
		// 左右文件存入的差异列表都为空，只需比较当前字符串是否相同即可
		if (leftDifferences.isEmpty() && rightDifferences.isEmpty()) {
			return textComparator.sameText(leftStr, rightStr);
		}

		// 如果直接对比匹配，则直接返回true
		if (textComparator.sameText(leftStr, rightStr)) {
			return true;
		}

		// 如果左侧差异列表不为空，则需要比对右侧文本是否在左侧差异列表中找到匹配
		boolean leftFound =  removeIfFound(textComparator, leftDifferences, rightStr, leftStr);
		boolean rightFound = removeIfFound(textComparator, rightDifferences, leftStr, rightStr);
		
		// 同时都找到，可能会存在交替add导致差异列表刚好交换的情况，此时需要移除add进去的字符串
		if (leftFound && rightFound) {
			leftDifferences.remove(leftStr);
			rightDifferences.remove(rightStr);
		}
		
		return leftFound || rightFound;
	}
	
	/**
	 * 如果比较字符串在差异列表中找到匹配的，则: 移掉差异列表里面匹配字符串 + 将待添加的字符串加入差异列表 如果未找到匹配的直接返回false
	 * @param textComparator  文本比较器
	 * @param differentTexts  差异列表
	 * @param compareStr	   待比较的字符串
	 * @param addStr		   待添加的字符串
	 * @return   true,找到匹配字符串。false,未找到匹配字符串。
	 */
	private static boolean removeIfFound(TextComparable textComparator, List<String> differentTexts, String compareStr, String addStr) {
		if (!differentTexts.isEmpty()) {
			boolean found = false;
			for (Iterator<String> itr = differentTexts.iterator(); itr.hasNext();) {
				String text = itr.next();
				if (textComparator.sameText(text, compareStr)) {
					itr.remove();
					found = true;
					break;
				}
			}
			
			// 如果找到，消掉左侧差异列表中与右侧文本相同的语句，同时将该次比较语句放入差异列表
			if (found) {
				if (StringUtils.isNotEmpty(addStr)) {
					differentTexts.add(addStr);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * 列出差异
	 * @param leftFileTexts
	 *            左侧差异内容列表
	 * @param rightFileTexts
	 *            右侧差异内容列表
	 * @param textComparator
	 *            文本比较器
	 */
	private static void listDifference(List<String> leftFileTexts, List<String> rightFileTexts) {
		int leftLen = leftFileTexts.size();
		int rightLen = rightFileTexts.size();
		int endIndex = Math.max(leftLen, rightLen);
		// STEP1: 为了使相近的差异尽量放在一起，这里还是做一下排序
		Collections.sort(leftFileTexts);
		Collections.sort(rightFileTexts);
		
		// STEP2: 列出差异列表
		for (int i = 0; i < endIndex; i++) {
			String leftText = i < leftLen ? leftFileTexts.get(i) : "";
			String rightText = i < rightLen ? rightFileTexts.get(i) : "";
			System.out.println("【差异】检测到左侧【" + leftText + "】与右侧【" + rightText + "】不符！");
		}
	}

	/**
	 * 做trim处理并且忽略大小写的比较器
	 * @author ziyuqi
	 *
	 */
	static class TrimIgnoreCaseComparator implements TextComparable {
		@Override
		public boolean sameText(String leftText, String rightText) {
			if (leftText == null) {
				return rightText == null;
			}
			if (rightText == null) {
				return leftText == null;
			}
			return leftText.trim().equalsIgnoreCase(rightText.trim());
		}
	}

	/**
	 * 光大清分文件内容对比器
	 * 
	 * @author ziyuqi
	 *
	 */
	static class CebCleanFileTextComparator implements TextComparable {
		@Override
		public boolean sameText(String leftText, String rightText) {
			if (StringUtils.isEmpty(leftText)) {
				return StringUtils.isEmpty(rightText);
			}
			if (StringUtils.isEmpty(rightText)) {
				return StringUtils.isEmpty(leftText);
			}
			String[] leftArr = leftText.split(",");
			String[] rightArr = rightText.split(",");
			// 比较的内容： 除第一个流水号外，以及最后一个后缀了原始流水号外，其余字段都应相同
			for (int i = 1; i < leftArr.length; i++) {
				if (i != leftArr.length - 1) {
					if (!leftArr[i].equals(rightArr[i])) {
						return false;
					}
				} else {
					if (!leftArr[i].equals(rightArr[i])
					        && !leftArr[i].substring(0, 13).equals(rightArr[i].substring(0, 13))) {
						return false;
					}
				}
			}
			return true;
		}
	}
	
	/**
	 * 文本比较接口
	 * @author ziyuqi
	 *
	 */
	interface TextComparable {
		boolean sameText(String leftText, String rightText);
	}
}
