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
package cn.swiftpass.core.server.cle.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.swiftpass.core.common.mybatis.pagehelper.Page;
import cn.swiftpass.core.common.mybatis.pagehelper.PageHelper;
import cn.swiftpass.core.common.mybatis.pagehelper.PageInfo;
import cn.swiftpass.core.common.utils.BeanConvertUtils;
import cn.swiftpass.core.dao.cle.mapper.${convertToJava(name, true)}Dao;
import cn.swiftpass.core.dao.cle.mapper.SensitiveDataOprLogDao;
import cn.swiftpass.core.dao.cle.modle.${convertToJava(name, true)}Modle;
import cn.swiftpass.core.dao.cle.modle.SensitiveDataOprLogModle;
import cn.swiftpass.core.facade.cle.constants.OperateType;
import cn.swiftpass.core.facade.cle.dto.${convertToJava(name, true)}Dto;
import cn.swiftpass.core.facade.cle.dto.SensitiveDataOprLogDto;
import cn.swiftpass.core.facade.cle.service.${convertToJava(name, true)}Service;

/**
 * ${comment}
 * @author ziyuqi
 *
 */
@Service
public class ${convertToJava(name, true)}ServiceImpl implements ${convertToJava(name, true)}Service {

    @Resource(name="stats${convertToJava(name, true)}Dao")
    private ${convertToJava(name, true)}Dao ${convertToJava(name, false)}Dao;
    
    @Resource(name="statsSensitiveDataOprLogDao")
    private SensitiveDataOprLogDao sensitiveDataOprLogDao;

    @Transactional
    @Override
    public void insert(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto) {
        // STEP1: 插入对象
        ${convertToJava(name, true)}Modle modle = new ${convertToJava(name, true)}Modle();
        BeanConvertUtils.convert(modle, ${convertToJava(name, false)}Dto);
        ${convertToJava(name, false)}Dao.insert(modle);
        
        // STEP2: 插入操作日志
        SensitiveDataOprLogModle oprModel = new SensitiveDataOprLogModle();
        oprModel.setOperateEmp(modle.getCreateEmp());
        oprModel.setOperateUser(modle.getCreateUser());
        oprModel.setRelativeDataKey(String.valueOf(modle.get${convertToJava(primaryKey, true)}()));
        // FIXME: 替换具体类型
        oprModel.setOperateType(OperateType.ADD_CLEANING_REMARK_CONF.getKey());
        oprModel.setOperateRemark(modle.getRemarkHeader());
        sensitiveDataOprLogDao.insert(oprModel);
    }

    @Transactional
    @Override
    public void updateById(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto) {
        // STEP1: 更新对象
        ${convertToJava(name, true)}Modle modle = new ${convertToJava(name, true)}Modle();
        BeanConvertUtils.convert(modle, ${convertToJava(name, false)}Dto);
        ${convertToJava(name, false)}Dao.updateById(modle);
        
        // STEP2: 插入操作日志
        SensitiveDataOprLogModle oprModel = new SensitiveDataOprLogModle();
        oprModel.setOperateEmp(${convertToJava(name, false)}Dto.getCreateEmp());
        oprModel.setOperateUser(${convertToJava(name, false)}Dto.getCreateUser());
        oprModel.setRelativeDataKey(String.valueOf(${convertToJava(name, false)}Dto.get${convertToJava(primaryKey, true)}()));
        // FIXME: 替换操作类型和备注
        oprModel.setOperateType(OperateType.EDIT_CLEANING_REMARK_CONF.getKey());
        sensitiveDataOprLogDao.insert(oprModel);
    }

    @Transactional
    @Override
    public void deleteById(${convertToJava(name, true)}Dto ${convertToJava(name, false)}Dto) {
        // STEP1: 根据Id进行删除
        ${convertToJava(name, false)}Dao.deleteById(${convertToJava(name, false)}Dto.get${convertToJava(primaryKey, true)}());
        
        // STEP2: 插入操作日志
        SensitiveDataOprLogModle oprModel = new SensitiveDataOprLogModle();
        oprModel.setOperateEmp(${convertToJava(name, false)}Dto.getCreateEmp());
        oprModel.setOperateUser(${convertToJava(name, false)}Dto.getCreateUser());
        oprModel.setRelativeDataKey(String.valueOf(${convertToJava(name, false)}Dto.getId()));
        // FIXME: 替换操作类型和备注
        oprModel.setOperateType(OperateType.DELETE_CLEANING_REMARK_CONF.getKey());
        sensitiveDataOprLogDao.insert(oprModel);
    }

    @Override
    public PageInfo<${convertToJava(name, true)}Dto> pagingQuery(int pageNum, int pageSize, ${convertToJava(name, true)}Dto queryCond) {
        ${convertToJava(name, true)}Modle ${convertToJava(name, false)}Modle = new ${convertToJava(name, true)}Modle();
        BeanConvertUtils.convert(${convertToJava(name, false)}Modle, queryCond);
        PageHelper.startPage(pageNum, pageSize, queryCond.getTotal());
        Page<${convertToJava(name, true)}Modle> pageResults = (Page<${convertToJava(name, true)}Modle>) this.${convertToJava(name, false)}Dao
                .pagingQuery(${convertToJava(name, false)}Modle);
        List<${convertToJava(name, true)}Dto> ${convertToJava(name, false)}s = BeanConvertUtils.convertPagingList(pageResults,
                ${convertToJava(name, true)}Dto.class);

        return new PageInfo<${convertToJava(name, true)}Dto>(${convertToJava(name, false)}s);
    }

    @Override
    public ${convertToJava(name, true)}Dto findById(Integer id) {
        ${convertToJava(name, true)}Modle findOne = ${convertToJava(name, false)}Dao.findById(id);
        if (findOne == null) {
            return null;
        }
        ${convertToJava(name, true)}Dto result = new ${convertToJava(name, true)}Dto();
        BeanConvertUtils.convert(result, findOne);
        return result;
    }

    @Override
    public List<SensitiveDataOprLogDto> findAllOprLogsById(Integer id) {
        SensitiveDataOprLogModle queryCond = new SensitiveDataOprLogModle();
        queryCond.setRelativeDataKey(String.valueOf(id));
        
        // FIXME: 替换操作类型
        queryCond.setOperateTypes(Arrays.asList(OperateType.ADD_CLEANING_REMARK_CONF.getKey(),
                OperateType.EDIT_CLEANING_REMARK_CONF.getKey(), OperateType.DELETE_CLEANING_REMARK_CONF.getKey()));
        
        return BeanConvertUtils.convertList(sensitiveDataOprLogDao.pagingQuery(queryCond),
                SensitiveDataOprLogDto.class);
    }
}
