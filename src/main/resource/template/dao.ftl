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
package cn.swiftpass.core.dao.cle.mapper;

import org.apache.ibatis.annotations.Param;

import cn.swiftpass.core.common.base.BaseDao;
import cn.swiftpass.core.dao.cle.modle.${convertToJava(name, true)}Modle;
import cn.swiftpass.slite.db.annotation.AutoGenDao;

/**
 * ${comment}Dao类
 * @author ziyuqi
 *
 */
@AutoGenDao
public interface ${convertToJava(name, true)}Dao  extends BaseDao<${convertToJava(name, true)}Modle, Integer> {
    /**
     * 根据ID进行删除
     * @param id 传入ID
     */
    void deleteById(@Param("id")Integer id);
}
