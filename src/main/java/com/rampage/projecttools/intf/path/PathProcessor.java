package com.rampage.projecttools.intf.path;

import java.io.File;

/**
 * 文件处理器
 * @author ziyuqi
 *
 */
public interface PathProcessor {
    /**
     * 处理文件
     * @param file 待处理的文件
     * @return  处理成功与否
     */
    boolean processFile(File file);

    /**
     * 处理目录
     * @param dir  待处理的目录
     * @return 处理成功与否
     */
    boolean processDir(File dir);
    
    /**
     * 是否忽略子目录处理（一般如果processDir中进行了相关处理，可能不再需要遍历对应路径的子路径）
     * @return 是否忽略
     */
    boolean ignoreSub();
    
    /**
     * 如果只需要忽略几个子路径而不是所有子路径，可以使用该属性
     * @return 需要忽略的子路径
     */
    // List<String> getIgnoreSubPathName();
}
