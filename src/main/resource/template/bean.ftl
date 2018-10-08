<#function convertToJava str firstUpper>
<#local result="">
<#if str?starts_with("CLE_")>
<#local str = "${str?substring(4,str?length)}">
</#if>
<#list str?split("_") as item>
<#if item_index = 0>
<#if firstUpper>
<#local result = result + "${item?lower_case?cap_first}">
<#else>
<#local result = result + "${item?lower_case}">
</#if>
<#else>
<#local result = result + "${item?lower_case?cap_first}">
</#if>
</#list>
<#return result>
</#function>

<#function getCType str>
<#local result="String">
<#if str?starts_with("NUMBER")>
<#local result = "Integer">
<#elseif str?starts_with("TIMESTAMP")>
<#local result = "Date">
</#if>
<#return result>
</#function>
package cn.swiftpass.core.dao.cle.entity;

import java.util.Date;

import cn.swiftpass.core.common.base.BaseEntity;

/**
 * ${comment}
 * @author ziyuqi
 *
 */
public class ${convertToJava(name, true)} extends BaseEntity {
    private static final long serialVersionUID = 1L;
   
<#list columns as item>
    /**
     * ${item.comment}
     */
    private ${getCType(item.cType)} ${convertToJava(item.name, false)};
    
</#list>
<#list columns as item>  
    public void set${convertToJava(item.name, true)}(${getCType(item.cType)} ${convertToJava(item.name, false)}) {
        this.${convertToJava(item.name, false)} = ${convertToJava(item.name, false)};
    }
    
    public ${getCType(item.cType)} get${convertToJava(item.name, true)}() {
        return this.${convertToJava(item.name, false)};
    }
    
</#list> 

    @Override
    public Object getPrimaryKey() {
        return this.${convertToJava(primaryKey, false)};
    }

}
