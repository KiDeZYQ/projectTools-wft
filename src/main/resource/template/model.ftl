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
package cn.swiftpass.core.dao.cle.modle;

import java.util.Date;

import cn.swiftpass.core.dao.cle.entity.${convertToJava(name, true)};

/**
 * ${comment}
 * @author ziyuqi
 *
 */
public class ${convertToJava(name, true)}Modle extends ${convertToJava(name, true)} {
    private static final long serialVersionUID = 1L;
}
