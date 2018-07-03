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
package cn.swiftpass.core.facade.cle.service;

import java.util.List;

import javax.jws.WebService;

import cn.swiftpass.core.common.mybatis.pagehelper.PageInfo;
import cn.swiftpass.core.facade.cle.dto.${convertToJava(name, true)}Dto;
import cn.swiftpass.core.facade.cle.dto.SensitiveDataOprLogDto;
import cn.swiftpass.sbox.annotation.HessianService;

/**
 * ${comment}Service类
 * @author ziyuqi
 *
 */
@HessianService
@WebService
public interface ${convertToJava(name, true)}Service {
    /**
     * 插入数据
     * @param ${convertToJava(name, false)}Dto 待插入的对象
     */
    void insert(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto);

    /**
     * 根据主键进行更新
     * @param ${convertToJava(name, false)}Dto 待更新对象
     */
    void updateById(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto);
    
    /**
     * 根据Id进行删除
     * @param ${convertToJava(name, false)}Dto 传入对象（为了记录操作日志）
     */
    void deleteById(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto);
    
    /**
     * 分页查询
     * @param pageNum 页码
     * @Param pageSize 每页条数
     * @param ${convertToJava(name, false)}Dto 传入条件
     */
    PageInfo<${convertToJava(name, true)}Dto> pagingQuery(int pageNum, int pageSize,
           ${convertToJava(name, true)}Dto queryCond);

    /**
     * 通过ID查找清分摘要配置信息
     * @param id 传入ID
     * @return 摘要配置信息
     */
    ${convertToJava(name, true)}Dto findById(Integer id);

    /**
     * 根据配置Id查找所有操作记录
     * @param id 配置ID
     * @return 操作记录列表
     */
    List<SensitiveDataOprLogDto> findAllOprLogsById(Integer id);
}
