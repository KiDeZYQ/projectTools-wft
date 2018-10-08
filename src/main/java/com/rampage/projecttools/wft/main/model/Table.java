package com.rampage.projecttools.wft.main.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 表定义
 * @author ziyuqi
 *
 */
@XmlAccessorType(XmlAccessType.FIELD) 
@XmlRootElement(name="table")
public class Table implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlElement
	private String name;
	
	@XmlElement
	private String seqName;
	
	@XmlElement
	private String comment;
	
	@XmlElement
	private String primaryKey;
	
	@XmlElementWrapper(name="uniques")
	@XmlElement(name="unique")
	private List<String> uniques;
	
	@XmlElementWrapper(name="indexes")
	@XmlElement(name="index")
	private List<String> indexes;
	
	@XmlElementWrapper(name="columns")
	@XmlElement(name="column")
	private List<Column> columns;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSeqName() {
		return seqName;
	}

	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<String> getUniques() {
		return uniques;
	}

	public void setUniques(List<String> uniques) {
		this.uniques = uniques;
	}

	public List<String> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<String> indexes) {
		this.indexes = indexes;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
}
