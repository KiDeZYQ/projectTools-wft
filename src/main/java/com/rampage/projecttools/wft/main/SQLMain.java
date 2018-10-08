package com.rampage.projecttools.wft.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rampage.projecttools.intf.path.PathProcessor;
import com.rampage.projecttools.util.FileUtils;
import com.rampage.projecttools.util.IOUtils;
import com.rampage.projecttools.util.StringUtils;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

/**
 * SQL处理相关的操作类（包括整理SQL和校验SQL是否正确，不校验语法，语法直接执行是否报错就出来了，校验是否忘了带上用户前缀或者带了不必要的用户前缀）
 * @author ziyuqi
 *  V1.0.0 实现SQL校验功能，对简单的SQL进行校验，并输出错误的实例名。暂不支持具体输出错误实例名所在的行。 DONE
 *  V2.0.0 实现整理SQL的功能，将指定目录下的SQL进行整理，传入整理的开始更新时间和结束时间，然后过滤出对应的sql文件  DONE
 *  V2.0.1 优化校验SQL的功能，增加特性： 可以输出有问题的SQL语句所在的行 + 可以检测出SQL语句是否少了分号   --- 2017-12-22  DONE  
 *  	发现bug，在找匹配关键字来判断是否schema正常的时候，如果连续两个关键字，会导致第二个关键字匹配不到（add constraint-- NO_SCHEMA_PATTERN 匹配到 add , 后续constraint匹配不到） --- FIXED
 *
 */
public class SQLMain {
    /**
     * 原SQL文件所在的路径
     */
    // private static final String SOURCE_DIR = "";
    
    /**
     * 待生成的目标文件所在的路径
     */
    private static final String DEST_DIR = "F:/需求/2018/08/01_光大8月全量版本/ceb_20180807/patch/20180828/SQL";
    
    private static final String SOURCE_DDL_DIR = "D:/sql脚本/DDL/2018";
    
    private static final String SOURCE_DML_DIR = "D:/sql脚本/DML/2018";
    
    private static final String SCHEMA = "szceb";
    
    private static final List<String> COMMON_HEAD_TAIL = Arrays.asList("set define off;", "commit;", "exit;");
    
    private static final DateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // 需要带上实例名的pattern
    private static final Pattern SCHEMA_NEEDED_PATTERN = Pattern.compile(
            "(INSERT(\\s+|\\t+|\\r+|\\n+)INTO)|(CREATE(\\s+|\\t+|\\r+|\\n+)TABLE)|(COMMENT(\\s+|\\t+|\\r+|\\n+)ON(\\s+|\\t+|\\r+|\\n+)COLUMN)"
                    + "|COMMENT(\\s+|\\t+|\\r+|\\n+)ON(\\s+|\\t+|\\r+|\\n+)TABLE|(ALTER(\\s+|\\t+|\\r+|\\n+)TABLE)|(DROP(\\s+|\\t+|\\r+|\\n+)TABLE)"
                    + "|(CREATE(\\s+|\\t+|\\r+|\\n+)INDEX)|(DROP(\\s+|\\t+|\\r+|\\n+)INDEX)"
                    + "|(INSERT(\\s+|\\t+|\\r+|\\n+)INTO)|(SEQ_)|(UPDATE(\\s+|\\t+|\\r+|\\n+))|(FROM(\\s+|\\t+|\\r+|\\n+))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // 对于create index语句，还需校验on后面的表需要带上实例名
    private static final Pattern ON_PATTERN = Pattern.compile("(\\s+|\\t+|\\r+|\\n+)ON(\\s+|\\t+|\\r+|\\n+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // 不需要带实例名的pattern
    private static final Pattern NO_SCHEMA_PATTERN = Pattern.compile("(\\s+|\\t+|\\r+|\\n+)CONSTRAINT(\\s+|\\t+|\\r+|\\n+)|(\\s+|\\t+|\\r+|\\n+)ADD(\\s+|\\t+|\\r+|\\n+)|(\\s+|\\t+|\\r+|\\n+)MODIFY(\\s+|\\t+|\\r+|\\n+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    private static final List<String> NEW_SENTENCE_BEGIN = Arrays.asList("alter ", "comment ", "create ", "exit;", "insert ", "update ", "commit;");
    
    public static void main(String[] args) throws ParseException {
    	System.out.println("---------------------------------------------------------STEP1: 生成SQL文件开始-------------------------------------------------------");
    	// generateSQLFile(DATE_FORMATER.parse("2018-07-10 19:41:00"), DATE_FORMATER.parse("2018-08-07 14:04:00"), 1);
    	System.out.println("---------------------------------------------------------STEP1: 生成SQL文件结束-------------------------------------------------------");
    	System.out.println("---------------------------------------------------------STEP2: 校验SQL文件开始-------------------------------------------------------");
        checkSQLSyntax();  // 校验schema
    	System.out.println("---------------------------------------------------------STEP2: 校验SQL文件结束-------------------------------------------------------");
    }
    
    /**
     * 生成SQL文件
     * @param startTime  读取的SQL文件的开始修改时间
     * @param endTime	  读取的SQL文件的最后修改时间
     * @param startIndex 生成的文件的下标开始计数（dba要求SQL文件的格式如: startIndex(两位) + 下划线 + dml/ddl + .sql后缀）
     */
    private static void generateSQLFile(final Date startTime, final Date endTime, final int startIndex) {
    	// STEP1: 创建ddl和dml文件，当前默认只生成一个，如果文件太大需手工进行拆分
    	// 默认历史脚本归档到01_ddl.sql、03_dml 其中02_ddl.sql、04_dml.sql用来存储此次需求增加的脚本
    	final File ddlFile = createDDLFile(startIndex);
    	final File dmlFile = createDMLFile(startIndex + 2);
    	
    	// STEP2: 先处理DDL文件生成
		FileUtils.processPathRecursively(new File(SOURCE_DDL_DIR), new PathProcessor() {
			@Override
			public boolean processFile(File file) {
				// 如果修改时间不是指定时间内的,直接返回
				if (!needProcessFile(file, startTime, endTime)) {
					return true;
				}
				
				// 对指定时间内的进行归档
				mergeFile(file, ddlFile, startIndex);
				return true;
			}
			@Override
			public boolean processDir(File dir) {
				return false;
			}
			@Override
			public boolean ignoreSub() {
				return false;
			}
		}, new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".sql");
			}
		});
		endFile(ddlFile, false);
		System.out.println("创建DDL文件：" + ddlFile.getAbsolutePath() + "成功!");
		
		// STEP3: 再处理dml文件
		FileUtils.processPathRecursively(new File(SOURCE_DML_DIR), new PathProcessor() {
			@Override
			public boolean processFile(File file) {
				// 如果修改时间不是指定时间内的,直接返回
				if (!needProcessFile(file, startTime, endTime)) {
					return true;
				}
				
				// 对指定时间内的进行归档
				mergeFile(file, dmlFile, startIndex + 1);
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
		}, new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".sql");
			}
		});
		endFile(dmlFile, true);
		System.out.println("创建DML文件：" + dmlFile.getAbsolutePath() + "成功!");
	}
    
    /**
     * 结束文件   
     * @param file			待结束的文件
     * @param needCommit    是否需要提交
     */
    private static void endFile(File file, boolean needCommit) {
    	BufferedWriter bw = null;
    	try  {
    		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "GB2312"));
			bw.newLine();
			if (needCommit) {
				bw.write("commit;");
				bw.newLine();;
			}
			bw.write("exit;");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(bw);
		}
    }

	/**
     * 创建dml文件，
     * @param index
     * @return
     */
    private static File createDMLFile(int index) {
		File file = createFile(index, "dml");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GB2312"));
			bw.write("set define off;");
			bw.newLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(bw);
		}
		return file;
	}

    private static File createFile(int index, String fileType) {
    	String fileName = getFileNameByIndex(index, fileType);
		File file = new File(fileName);
		
		// 存在文件则先删除
		if (file.exists()) {
			file.delete();
		}
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
    }
    
	private static File createDDLFile(int index) {
		File file = createFile(index, "ddl");
		return file;
	}

	private static String getFileNameByIndex(int index, String suffix) {
		String prefix = "0" + index;
		
		// 一般情况下sql文件个数不会超过100个，如果index < 10， 则第一位用0补齐
		if (prefix.length()> 2) {
			prefix = prefix.substring(1, prefix.length());
		}
		
		File file = new File(DEST_DIR);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return DEST_DIR + File.separator + prefix + "_" + suffix + ".sql";
	}

	/**
     * 处理DDL文件，整理到SQL脚本
     * @param sourceFile   待处理的源文件
	 * @param sourceFile   待合并入的目标文件
     * @param index  文件的后缀下标
     */
	protected static void mergeFile(File sourceFile, File destFile, int index) {
		String fileCharset = getFileCharset(sourceFile);
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile, true), "GB2312"));
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), fileCharset));
			bw.newLine();
			bw.write("--" + sourceFile.getName());	// 加上文件的注释，表明这是哪个文件
			bw.newLine();
			String lineStr = replaceSchema(br.readLine());
			while (lineStr != null) {
				if (StringUtils.isNotEmpty(lineStr)) {
					bw.write(lineStr);
					bw.newLine();
				}
				lineStr = replaceSchema(br.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(br, bw);
		}
		
		System.out.println("合并文件：" + sourceFile.getName() + "到目标文件：" + destFile.getName() + "成功!");
	}

	/**
	 * 得到文件的编码格式
	 * @param file  传入的文件
	 * @return
	 */
	private static String getFileCharset(File file) {
		 /*
         * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
         * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，如ParsingDetector、
         * JChardetFacade、ASCIIDetector、UnicodeDetector。
         * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
         * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
         * cpDetector是基于统计学原理的，不保证完全正确。
         */
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(JChardetFacade.getInstance());	// 用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        java.nio.charset.Charset charset = null;
        try {
            charset = detector.detectCodepage(file.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return charset != null ? charset.name() : "GBK";
	}

	/**
	 * 替换SQL中的shema
	 * @param lineStr 原始字符串
	 * @return  替换schema之后的字符串
	 */
	private static String replaceSchema(String lineStr) {
		if (StringUtils.isEmpty(lineStr)) {
			return lineStr;
		}
		
		return lineStr.replace("${SCHEMA}", SCHEMA).replace("${sheme}", SCHEMA).replace("${schema}", SCHEMA).replace("${scheme}", SCHEMA).replace("zhifu", SCHEMA).replace("tjzf", SCHEMA);
	}

	protected static boolean needProcessFile(File file, Date startTime, Date endTime) {
		// 得到文件的最后修改时间
		long modifyTime = file.lastModified();
		return modifyTime >= startTime.getTime() && modifyTime < endTime.getTime();
	}

   /**
    * 校验SQL语法，主要是校验有没有少实例名或者最重要的dml、ddl的前缀
    */
    private static void checkSQLSyntax() {
        PathProcessor sqlPathProcessor = new PathProcessor() {
            @Override
            public boolean processFile(File file) {
                String fileName = file.getName();
                
                // 非SQL文件直接跳过
                if (!fileName.toLowerCase().endsWith(".sql")) {
                    return false;
                }
                System.out.println("----------------------------------开始校验SQL文件：" + fileName + "----------------------------------");
                BufferedReader br = null;
                String lineStr = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    lineStr = br.readLine();
                    // 因为考虑到可能每个人写SQL的风格各异，可能关键字出现在各种不同的行，可能一个语句换成N行，这里直接先将文件内容缓存起来
                    StringBuilder sb = new StringBuilder(4096);
                    int lineCount = 0;
                    String preStr = null;
                    while (lineStr != null) {
                        lineStr = lineStr.trim().replace("?", "");	// 暂时不知原因的重新写SQL会出现最前面出现英文?
                        lineCount++;
                        // 如果是通用的SQL语句，则直接push入栈
                        if (COMMON_HEAD_TAIL.contains(lineStr.toLowerCase())) {
                            System.out.println("检测到通用SQL头尾语句【" + lineStr + "】");
                            lineStr = br.readLine();
                            sb.append("\n");
                            continue;
                        }
                        
                        // 过滤掉逐注释行
                        if (lineStr.indexOf("--") == 0 || lineStr.indexOf("/*") == 0) {
                            lineStr = br.readLine();
                            sb.append("\n");
                            continue;
                        }
                        
                        // 校验末尾的分号
                        if (isNewSentence(lineStr) && preStr != null) {
                        	if (!preStr.endsWith(";")) {
                                System.out.println("Line[" + lineCount + "]ERROR: 上一个SQL语句未找到结尾的分号!");
                        	}
                        }
                        sb.append(lineStr).append("\n");
                        if (StringUtils.isNotEmpty(lineStr)) {
                        	preStr = lineStr;
                        }
                        lineStr = br.readLine();
                    }
                    // STEP1: 校验哪些需要用户名但是确未添加的情况
                    processNeedSchemaAnalyse(sb.toString(), SCHEMA);
                    // STEP2: 校验哪些不需要用户名却添加了的情况
                    processNoNeedSchemaAnalyse(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(br);
                }
                
                System.out.println("----------------------------------结束校验SQL文件：" + fileName + "----------------------------------");
                return false;
            }
            
            @Override
            public boolean processDir(File dir) {
                return false;
            }
            
            @Override
            public boolean ignoreSub() {
                return false;
            }
        };
        
        FileUtils.processPathRecursively(new File(DEST_DIR), sqlPathProcessor);
    }

    /**
     * 是否是新的句子
     * @param lineStr
     * @return
     */
    protected static boolean isNewSentence(String lineStr) {
    	String lowStr = lineStr.toLowerCase();
    	for (String one : NEW_SENTENCE_BEGIN) {
    		if (lowStr.startsWith(one)) {
    			return true;
    		}
    	}
    	return false;
    }

	/**
     * 校验不需要实例名的语法是否正确
     * @param sql
     * @param lineCount 
     */
    private static void processNoNeedSchemaAnalyse(String sql) {
        int endIndex = 0;
        int startIndex = 0;
        String groupStr = null;
        Matcher matcher = NO_SCHEMA_PATTERN.matcher(sql);
        char ch = 0;
        StringBuilder schemaBuilder = new StringBuilder();
        while (matcher.find(Math.max(0, endIndex - 1))) {
            groupStr = matcher.group();
            endIndex = matcher.end();
            startIndex = matcher.start();
            
            boolean startCount = false;
            boolean foundSchema = false;
            schemaBuilder.setLength(0);
            for (int j=endIndex; j<sql.length(); j++) {
                ch = sql.charAt(j);
                if (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r' || ch == '.') {
                    if (startCount) {
                        if (ch == '.') {
                            foundSchema = true;
                        }
                        break;
                    } else {
                        continue;
                    }
                } else {
                    startCount = true;
                    schemaBuilder.append(ch);
                }
            }
            if (foundSchema && schemaBuilder.length() > 0) {
                System.out.println("Line[" + getLineCount(sql, startIndex) + "]ERROR: 对于匹配到的不需带Schema类型的SQL【" + groupStr + "】却检测到Schema为【" + schemaBuilder + "】!");
            }
        }
    }

    /**
     * 
     * @param sql
     * @param schema
     */
    private static void processNeedSchemaAnalyse(String sql, String schema) {
        Matcher matcher = SCHEMA_NEEDED_PATTERN.matcher(sql);
        int endIndex = -1;
        int startIndex = -1;
        String groupStr = null;
        char ch = 0;
        StringBuilder schemaBuilder = new StringBuilder();
        while (matcher.find(Math.max(0, endIndex - 1))) {
            groupStr = matcher.group();
            endIndex = matcher.end();
            startIndex = matcher.start();
            
            schemaBuilder.setLength(0);
            // 对于SEQ需要特殊处理(取前面的schema)
            if (groupStr.toLowerCase().indexOf("seq_") != -1) {
                int j=startIndex-2;
                for (; j>0; j--) {
                    ch = sql.charAt(j);
                    if (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r' || ch == '(') {
                        break;
                    }
                }
                schemaBuilder.append(sql.substring(j + 1, startIndex - 1));
            } else {
                boolean startCount = false;
                for (int j=endIndex; j<sql.length(); j++) {
                    ch = sql.charAt(j);
                    if (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r' || ch == '.') {
                        if (startCount) {
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        startCount = true;
                        schemaBuilder.append(ch);
                    }
                }
            }
            if (!schema.toLowerCase().equals(schemaBuilder.toString())) {
                System.out.println("Line[" + getLineCount(sql, startIndex) + "]ERROR: 对于匹配到的需带Schema类型的SQL【" + groupStr + "】所检测到的Schema为【" + schemaBuilder + "】, 与期待的Schema不符!");
            }
            
            // 对于create index类型的还需要校验on后面是否带了实例名
            schemaBuilder.setLength(0);
            if (groupStr.toLowerCase().indexOf("create") != -1 && groupStr.toLowerCase().indexOf("index") != -1) {
                Matcher onMatcher = ON_PATTERN.matcher(sql);
                if (onMatcher.find(endIndex)) {
                    groupStr = onMatcher.group();
                    endIndex = onMatcher.end();
                    startIndex = onMatcher.start();
                    boolean startCount = false;
                    for (int j=endIndex; j<sql.length(); j++) {
                        ch = sql.charAt(j);
                        if (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r' || ch == '.') {
                            if (startCount) {
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            startCount = true;
                            schemaBuilder.append(ch);
                        }
                    }
                }
                if (!schema.toLowerCase().equals(schemaBuilder.toString())) {
                    System.out.println("Line[" + getLineCount(sql, startIndex) + "]ERROR: 对于匹配到的需带Schema类型的SQL【CREATE INDEX ON】所检测到的Schema为【" + schemaBuilder + "】, 与期待的Schema不符!");
                }
            } 
        }
    }

	private static int getLineCount(String sql, int startIndex) {
		int lineCount = 1;
		// 根据前面的换行符的个数，得到当前是第几行
    	String preStr = sql.substring(0, startIndex);
    	for (int i=0; i<preStr.length(); i++) {
    		if (preStr.charAt(i) == '\n') {
    			lineCount++;
    		}
    	}
    	return lineCount;
	}
}
