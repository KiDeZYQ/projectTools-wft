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
package cn.swiftpass.core.facade.cle.dto;

import java.util.Date;

import cn.swiftpass.core.common.base.BaseDto;

/**
 * ${comment}
 * @author ziyuqi
 *
 */
public class ${convertToJava(name, true)}Dto extends BaseDto {
	private static final long serialVersionUID = 1L;

<#list columns as item>
    <#if item.name != "PHYSICS_FLAG" && item.name != "CREATE_USER" && item.name != "CREATE_EMP" && item.name != "CREATE_TIME" && item.name != "UPDATE_TIME">
    /**
     * ${item.comment}
     */
    private ${getCType(item.cType)} ${convertToJava(item.name, false)};
    
    </#if>
</#list>
<#list columns as item>  
    <#if item.name != "PHYSICS_FLAG" && item.name != "CREATE_USER" && item.name != "CREATE_EMP" && item.name != "CREATE_TIME" && item.name != "UPDATE_TIME">
    public void set${convertToJava(item.name, true)}(${getCType(item.cType)} ${convertToJava(item.name, false)}) {
        this.${convertToJava(item.name, false)} = ${convertToJava(item.name, false)};
    }
    
    public ${getCType(item.cType)} get${convertToJava(item.name, true)}() {
        return this.${convertToJava(item.name, false)};
    }
    </#if>
    
</#list> 
}
