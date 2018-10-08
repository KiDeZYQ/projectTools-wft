package com.rampage.projecttools.wft.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.xml.bind.JAXB;

import com.rampage.projecttools.wft.main.model.Table;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 根据表的配置SQL.xml，自动生成SQL脚本，facade，dao，service，serviceImpl等
 * @author ziyuqi
 *
 */
public class NewBeanMain {
	
	private static final String BASE_DIR = System.getProperty("user.dir");
	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException, TemplateException {
		File outputFile = new File(BASE_DIR + File.separator + "output");
		if (!outputFile.exists()) {
			outputFile.mkdirs();
			System.out.println("创建output目录......");
		}
		
		Table table = JAXB.unmarshal(new File(BASE_DIR + File.separator + "src/main/resource/xml/SQL.xml"), Table.class);
		
		//创建一个合适的Configration对象  
        Configuration configuration = new Configuration();  
        configuration.setDirectoryForTemplateLoading(new File(BASE_DIR + File.separator + "src/main/resource/template"));  
        configuration.setObjectWrapper(new DefaultObjectWrapper());  
        configuration.setDefaultEncoding("UTF-8");
        System.out.println("加载freemarker配置完成......");
        
        // STEP1: 生成表的SQL
        Template template = configuration.getTemplate("sql.ftl");  
        Writer writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/02_ddl.sql"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成SQL完成......");
        
        // STEP2: 生成DTO、Modle等
        template = configuration.getTemplate("dto.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + "Dto.java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Dto类完成......");
        template = configuration.getTemplate("bean.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + ".java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Bean类完成......");
        template = configuration.getTemplate("model.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + "Modle.java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Model类完成......");
        
        // STEP3: 生成Dao类和xml文件
        template = configuration.getTemplate("mybatis.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + ".xml"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Mybatis文件完成......");
        template = configuration.getTemplate("dao.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + "Dao.java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Dao类完成......");
        // STEP4: 生成Service和serviceImpl
        template = configuration.getTemplate("service.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + "Service.java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成Service类完成......");
        template = configuration.getTemplate("serviceImpl.ftl");
        writer  = new OutputStreamWriter(new FileOutputStream(BASE_DIR + File.separator + "output/" + parseTableToJava(table.getName(), true) + "ServiceImpl.java"), "UTF-8"); 
        template.process(table, writer);  
        writer.close();
        System.out.println("生成ServiceImpl类完成......");
        
	}
	
	/**
	 * 将表的命名方式（全大写下划线分割）转换成java的命名方式（驼峰）
	 */
	private static String parseTableToJava(String tableStr, boolean className) {
		if (tableStr.startsWith("CLE_")) {
			tableStr = tableStr.substring(4, tableStr.length());
		}
		String[] nameStr = tableStr.split("_");
		StringBuilder sb = new StringBuilder(tableStr.length());
		char ch = 0;
		for (String name : nameStr) {
			for (int i=0; i<name.length(); i++) {
				ch = name.charAt(i);
				if (sb.length() == 0) {
					if (className) {
						sb.append(ch);
					} else {
						sb.append(String.valueOf(ch).toLowerCase(Locale.UK));
					}
				} else {
					if (i == 0) {
						sb.append(ch);
					} else {
						sb.append(String.valueOf(ch).toLowerCase(Locale.UK));
					}
				}
			}
		}
		return sb.toString();
	}
}
