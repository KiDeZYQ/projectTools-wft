package com.rampage.projecttools.wft.main.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * 列
 * @author ziyuqi
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)  
public class Column implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 列名
	 */
	@XmlElement
	private String name;
	
	/**
	 * 列类型
	 */
	@XmlElement
	private String cType;
	
	/**
	 * 是否可以为空标识
	 */
	@XmlElement
	private boolean nullable;
	
	/**
	 * 备注
	 */
	@XmlElement
	private String comment;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getcType() {
		return cType;
	}

	public void setcType(String cType) {
		this.cType = cType;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
