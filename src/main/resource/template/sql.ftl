CREATE SEQUENCE szceb.${seqName}
 INCREMENT BY 1
 MINVALUE 1
 MAXVALUE 999999999999999
 START WITH 1
 CACHE 5
 CYCLE ;
-- Create table
create table szceb.${name}
(
<#list columns as c>
    ${c.name} ${c.cType}<#if !(c.nullable)> NOT NULL</#if><#if c_has_next>,</#if> 
</#list>
);
-- Add comments to the table 
comment on table szceb.${name} is '${comment}';
-- Add comments to the columns 
<#list columns as c>
comment on column szceb.${name}.${c.name} is '${c.comment}';
</#list>
-- Create/Recreate primary, unique and foreign key constraints 
alter table szceb.${name} add constraint ${name}_PK primary key (${primaryKey}) using index;
<#list uniques as u>
alter table szceb.${name} add constraint ${name}_UK_${u_index} unique (${u}) using index;
</#list>
<#if indexes?? && (indexes?size>0)>
	<#list indexes as i>
create index szceb.IDX_${name}_${i_index} on szceb.${name} (${i});
	</#list>
</#if>