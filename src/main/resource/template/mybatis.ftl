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
<#local result="VARCHAR">
<#if str?starts_with("NUMBER")>
<#local result = "DECIMAL">
<#elseif str?starts_with("TIMESTAMP")>
<#local result = "TIMESTAMP">
</#if>
<#return result>
</#function>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="cn.swiftpass.core.dao.cle.mapper.${convertToJava(name, true)}Dao">
    <sql id="${name}_COLUMN"><#list columns as item>t.${item.name}<#if item_has_next>, </#if></#list></sql>
    <resultMap id="${convertToJava(name, true)}ModleResult" type="${convertToJava(name, true)}Modle">
        <#list columns as item>
            <#if item.name = primaryKey>
        <id column="${item.name}" jdbcType="${getCType(item.cType)}" property="${convertToJava(item.name, false)}" />
            <#else>
        <result column="${item.name}" jdbcType="${getCType(item.cType)}" property="${convertToJava(item.name, false)}" />
            </#if>
        </#list>
    </resultMap>

    <insert id="insert" parameterType="${convertToJava(name, true)}Modle">
        <selectKey resultType="java.lang.Integer" keyProperty="${convertToJava(primaryKey, false)}" order="BEFORE">
            SELECT ${seqName}.NEXTVAL AS KEY_ID FROM DUAL
        </selectKey>
        INSERT INTO ${name} 
        (<#list columns as item>${item.name}<#if item_has_next>,</#if></#list>)
        VALUES
        (<#list columns as item><#if item.name == "CREATE_TIME" || item.name == "UPDATE_TIME">sysdate<#else>${"#{"}${convertToJava(item.name, false)}, jdbcType=${getCType(item.cType)}${"}"}</#if><#if item_has_next>, </#if></#list>)
    </insert>

    <select id="pagingQuery" parameterType="${convertToJava(name, true)}Modle" resultMap="${convertToJava(name, true)}ModleResult">
        SELECT <include refid="${name}_COLUMN"/>
        FROM ${name} t
        <where>
            <#list columns as item>
                <#if item.name != primaryKey>
                    <#if item.name?ends_with("_TIME") || item.name?ends_with("_DATE")>
            <if test="begin${convertToJava(item.name, true)} != null">
               AND t.${item.name} <![CDATA[>=]]>  ${"#{"}begin${convertToJava(item.name, true)}${"}"}
            </if>
            <if test="end${convertToJava(item.name, true)} != null">
               AND t.${item.name} <![CDATA[<]]> ${"#{"}end${convertToJava(item.name, true)}${"}"}
            </if>
                    <#elseif getCType(item.cType)?starts_with("VARCHAR")>
            <if test="${convertToJava(item.name, false)} != null and ${convertToJava(item.name, false)} != ''">
                AND t.${item.name} = ${"#{"}${convertToJava(item.name, false)}${"}"}
            </if>        
                    <#else>
            <if test="${convertToJava(item.name, false)} != null">
                AND t.${item.name} = ${"#{"}${convertToJava(item.name, false)}${"}"}
            </if>
                     </#if>
                </#if>
            </#list>
        </where> 
        order by t.UPDATE_TIME desc
    </select>

    <update id="updateById" parameterType="${convertToJava(name, true)}Modle">
        UPDATE ${name}
        <set>
            <#list columns as item>
                <#if item.name != primaryKey && item.name != "CREATE_TIME" && item.name != "UPDATE_TIME" && item.name != "CREATE_USER" && item.name != "CREATE_EMP">
            <if test="${convertToJava(item.name, false)} != null">
                ${item.name} = ${"#{"}${convertToJava(item.name, false)}${"}"},
            </if>
                </#if>
            </#list>
            UPDATE_TIME = sysdate,
        </set>
        WHERE ${primaryKey} = ${"#{"}${convertToJava(primaryKey, false)}${"}"}
    </update>

    <select id="findById" parameterType="java.lang.Integer" resultMap="${convertToJava(name, true)}ModleResult">
        SELECT <include refid="${name}_COLUMN"/> FROM ${name} t WHERE ${primaryKey} = ${"#{"}${convertToJava(primaryKey, false)}${"}"}
    </select>
    
    <delete id="deleteById" parameterType="java.lang.Integer">
        DELETE FROM ${name} WHERE ${primaryKey} = ${"#{"}${convertToJava(primaryKey, false)}${"}"}
    </delete>
</mapper>   
