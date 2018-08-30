package com.rampage.projecttools.wft.main.model;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public static Map<String, Map<String, String>> orgMap = new HashMap<String, Map<String, String>>(3);
    
    public static final String ADMIN = "admin";
    
    public static final String SERVICE = "service";
    
    public static final String ACC = "acc";
    
    public static final String CLE = "cle";
    
    public static final String CLE1 = "cle1";
    
    public static final String CLE2 = "cle2"; 
    
    public static final String ADMIN_COMMON_PROJECT = "adminCommonProject";
    
    public static final String CLE_PROJECT = "cleProject";
    
    public static final String CLE_SUB_PROJECT = "cleSubProject";
    
    public static final String CLE_WAR_PROJECT = "cleWar";
    public static final String CLE1_WAR_PROJECT = "cle1War";
    public static final String CLE2_WAR_PROJECT = "cle2War";
    
    public static final String ADMIN_WAR_PROJECT = "adimProject";
    
    public static final String SERVICE_WAR_PROJECT = "serviceProject";
    
    public static final String ACC_WAR_PROJECT = "accProject";
    
    public static final String SOURCE_SUBMIT_LOG_PATH = "";
    
    public static final String DEST_SUBMIT_LOG_PATH = "";
    
    
    
    static {
        // 初始化各个受理机构的map
        // 1. 光大五月份新需求
        Map<String, String> gdNewMap = new HashMap<String, String>();
        gdNewMap.put(ADMIN, "admin-ceb");
        gdNewMap.put(SERVICE, "service-ceb");
        gdNewMap.put(ACC, "service-acc");
        gdNewMap.put(CLE, "sppay-cle-war");
        gdNewMap.put(CLE1, "service-acc");
        gdNewMap.put(CLE2, "service-acc");
        /*gdNewMap.put(CLE_PROJECT, "E:/workspacce/guangda0512/cle");
        gdNewMap.put(CLE_SUB_PROJECT, "E:/workspacce/guangda0512/cle-sub");
        gdNewMap.put(ADMIN_WAR_PROJECT, "E:/workspacce/guangda0512/admin-war");
        gdNewMap.put(SERVICE_WAR_PROJECT, "E:/workspacce/guangda0512/service-war");*/
        // ceb20180412 cibszgzTrunk ceb20180507 cibV4.1 onepack20180514 ceb20180528  public20180529 ceb20180602 ceb-distribute-v2
        gdNewMap.put(ADMIN_COMMON_PROJECT, "E:/workspacce/2018/08/01_ceb20180807/admin-common");
        gdNewMap.put(SERVICE_WAR_PROJECT, "E:/workspacce/2018/08/03_huishang20180822/service-war");
        // 01无卡 03_huishang20180822 01_ceb20180807
        gdNewMap.put(CLE1_WAR_PROJECT, "E:/workspacce/2018/07/03_银联无卡/war/wk-admin-war/trunk");
        gdNewMap.put(CLE2_WAR_PROJECT, "E:/workspacce/2018/07/03_银联无卡/war/wk-service-war/trunk");
        gdNewMap.put(CLE_PROJECT, "E:/workspacce/2018/08/01_ceb20180807/cle");
        gdNewMap.put(CLE_SUB_PROJECT, "E:/workspacce/2018/08/01_ceb20180807/cle-sub");
        gdNewMap.put(CLE_WAR_PROJECT, "E:/workspacce/2018/08/01_ceb20180807/cle/sppay-cle-war");
        gdNewMap.put(ADMIN_WAR_PROJECT, "E:/workspacce/2018/08/03_huishang20180822/admin-war");
        gdNewMap.put(ACC_WAR_PROJECT, "E:/workspacce/2018/08/01_ceb20180807/acc-war");
        orgMap.put("ceb", gdNewMap);
    }
}
