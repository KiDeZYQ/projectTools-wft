package com.rampage.projecttools.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rampage.projecttools.intf.path.PathProcessor;


public class FileUtils {
    private FileUtils() {
    }
    
    private static boolean accept(File file, FileFilter filter) {
        return filter == null || filter.accept(file);
    }
    
    
    public static List<File> findAllFilesRecursively(File path) {
        return findAllFilesRecursively(path, null);
    }
    
    /**
     * 寻找路径下的所有文件
     * @param path 传入路径
     * @param filter 文件过滤器
     * @return  所有文件列表
     */
    public static List<File> findAllFilesRecursively(File path, FileFilter filter) {
        if (path == null || !path.exists()) {
            throw new IllegalArgumentException("Invalid path:" + path + "!");
        }
        
        List<File> resultFiles = new ArrayList<File>();
        if (path.isFile()) {
            if (accept(path, filter)) {
                resultFiles.add(path);
                return resultFiles;
            }
            
            return Collections.emptyList();
        }
        
        File[] subFiles = path.listFiles();
        if (subFiles == null) {
            return Collections.emptyList();
        }
        
        List<File> subFileList = Arrays.asList(subFiles);
        while (!subFileList.isEmpty()) {
            // 临时存储当前子文件列表进行处理
            List<File> curSubFileList = new ArrayList<File>(subFileList);
            // 重新赋值，存储当前路径下找到的路径列表
            subFileList = new ArrayList<File>();
            for (File subFile : curSubFileList) {
                if (subFile.isFile()) {
                    if (accept(subFile, filter)) {
                        resultFiles.add(subFile);
                    }
                } else {
                    subFiles = subFile.listFiles();
                    if (subFiles != null) {
                        subFileList.addAll(Arrays.asList(subFiles));
                    }
                }
            }
        }
        
        return resultFiles;
    }
    
    public static void processPathRecursively(File path, PathProcessor processor) {
        processPathRecursively(path, processor, null);
    }
    
    /**
     * 递归处理路径下的所有文件或目录
     * @param path      传入路径
     * @param processor 路径处理器
     * @param filter  文件过滤器
     */
    public static void processPathRecursively(File path, PathProcessor processor, FileFilter filter) {
        if (path == null || !path.exists() || processor == null) {
            throw new IllegalArgumentException("Invalid path:" + path + ", processor:" + processor + "!");
        }
        // 如果传入的是个文件
        if (path.isFile()) {
            if (accept(path, filter)) {
                processor.processFile(path);
                return;
            }
            return;
        }
        
        // 此时说明传入的是目录，调用对目录的处理
        processor.processDir(path);
        if (processor.ignoreSub()) {
            return;
        }
        
        // 如果传入的目录下没有任何子文件或目录直接返回
        File[] subFiles = path.listFiles();
        if (subFiles == null) {
            return;
        }
        
        // 循环处理子目录下的所有文件
        List<File> subFileList = Arrays.asList(subFiles);
        while (!subFileList.isEmpty()) {
            List<File> curSubFileList = new ArrayList<File>(subFileList);
            subFileList = new ArrayList<File>();
            for (File subFile : curSubFileList) {
                if (subFile.isFile()) {
                    if (accept(subFile, filter)) {
                        processor.processFile(subFile);   // 处理文件
                    }
                } else {
                    if (accept(subFile, filter)) {
                        processor.processDir(subFile);  // 处理目录
                    }
                    
                    if (!processor.ignoreSub()) {
                        subFiles = subFile.listFiles();
                        if (subFiles != null) {
                            subFileList.addAll(Arrays.asList(subFiles));
                        }
                    }
                }
            }
        }
        return;
    }
    
    /**
     * 拷贝小文件（600M以下）
     * @param srcFile  源文件 
     * @param destFile 目标文件
     * @return  是否拷贝成功
     * @throws IOException
     */
    private static boolean copyTinyFile(File srcFile, File destFile) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } finally {
            IOUtils.closeQuietly(fos, fis);
        }
        return true;
    }

    /**
     * 拷贝文件
     * @param srcFile   源文件
     * @param destFile  目标文件
     * @return  是否拷贝成功
     * @throws IOException 
     */
    public static boolean copyFile(File srcFile, File destFile) throws IOException {
        if (srcFile == null || destFile == null) {
            
        }
        
        // 如果文件存在则先删除
        if (destFile.exists()) {
            if (!destFile.delete()) {
                return false;
            }
        }
        
        // 根据文件大小决定使用哪种方式进行文件拷贝：
        if (srcFile.length() > (600 * 1024 * 1024)) {
            // 大于600M定义为大文件，调用FileChannel进行拷贝
            return copyLargeFile(srcFile, destFile);
        } else {
            // 调用通用的文件拷贝
            return copyTinyFile(srcFile, destFile);
        }
    }
    
    /**
     * 拷贝大文件
     * @param srcFile   源文件
     * @param destFile  目标文件
     * @return   是否拷贝成功
     * @throws IOException
     */
    private static boolean copyLargeFile(File srcFile, File destFile) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        FileChannel iChannel = null;
        FileChannel oChannel = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);
            iChannel = inputStream.getChannel();
            oChannel = outputStream.getChannel();
            iChannel.transferTo(0, srcFile.length(), oChannel);
        } finally {
            IOUtils.closeQuietly(iChannel, inputStream, oChannel, outputStream);
        }
        
        return true;
    }

    /**
     * 删除路径（传入的是文件或者目录都可以）
     * @param path  传入路径
     * @return  是否删除成功
     */
    public static boolean deletePath(File path) {
        if (path == null) {
            throw new NullPointerException("Null path for delete!");
        }
        if (!path.exists()) {
             return true;
        }
        
        if (path.isFile()) {
            return path.delete();
        }
        
        // 如果传入的目录下没有任何子文件或目录直接返回
        File[] subFiles = path.listFiles();
        if (subFiles == null) {
            return path.delete();
        }
        
        // 循环处理子目录下的所有文件
        List<File> allNeedDeleteDirs = new ArrayList<File>();
        allNeedDeleteDirs.add(path);
        List<File> subFileList = Arrays.asList(subFiles);
        while (!subFileList.isEmpty()) {
            List<File> curSubFileList = new ArrayList<File>(subFileList);
            subFileList = new ArrayList<File>();
            for (File subFile : curSubFileList) {
                if (subFile.isFile()) {
                    if (!subFile.delete()) {
                        return false;
                    }
                } else {
                    subFiles = subFile.listFiles();
                    if (subFiles != null) {
                        allNeedDeleteDirs.add(subFile);     // 添加到后续需要删除的文件夹列表中来
                        subFileList.addAll(Arrays.asList(subFiles));
                    } else {
                        if (!subFile.delete()) {
                            return false;
                        }
                    }
                }
            }
        }
        
        // 倒序删除文件夹
        for(int i=allNeedDeleteDirs.size() - 1; i>=0; i--) {
            if (!allNeedDeleteDirs.get(i).delete()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 复制路径
     * @param srcPath  原路径
     * @param destPath 目标路径
     * @return  是否拷贝成功
     * @throws IOException
     */
    public static boolean copyPath(File srcPath, File destPath) throws IOException {
        if (srcPath == null || destPath == null) {
            throw new IllegalArgumentException("Invalid null srcPath or destPath!");
        }
        if (!srcPath.exists()) {
            throw new IllegalStateException("Not existed srcPath:" + srcPath + "!");
        }
        
        if (srcPath.isFile()) {
            // 目标地址是个文件，则要确保目标文件地址存在，如果不存在则创建
            if (destPath.isFile()) {
                if (!destPath.getParentFile().exists()) {
                    if (!destPath.getParentFile().mkdirs()) {
                        return false;
                    }
                }
                return copyFile(srcPath, destPath);
            } else {
                // 目标地址是个目录,如果目录不存在先创建目录
                if (!destPath.exists()) {
                    if (!destPath.mkdirs()) {
                        return false;
                    }
                }
                // 目标文件名取成和源文件名一致
                File destFile = new File(destPath.getAbsolutePath() + File.separator + srcPath.getName());
                return copyFile(srcPath, destFile);
            }
        }
        
        // 无法将目录复制到文件
        if (destPath.isFile()) {
            return false;
        }
        
        // 如果目标目录不存在，则直接创建目录
        if (!destPath.exists()) {
            if (!destPath.mkdirs()) {
                return false;
            }
        }
        
     // 如果传入的目录下没有任何子文件或目录直接返回
        File[] subFiles = srcPath.listFiles();
        if (subFiles == null) {
            return true;
        }
        
        // 循环处理子目录下的所有文件
        List<File> subFileList = Arrays.asList(subFiles);
        while (!subFileList.isEmpty()) {
            List<File> curSubFileList = new ArrayList<File>(subFileList);
            subFileList = new ArrayList<File>();
            for (File subFile : curSubFileList) {
                File destSubFile = new File(destPath.getAbsolutePath() + subFile.getAbsolutePath().substring(srcPath.getAbsolutePath().length()));
                if (subFile.isFile()) {
                    if (!copyFile(subFile, destSubFile)) {
                        return false;
                    }
                } else {
                    if (!destSubFile.exists()) {
                        if (!destSubFile.mkdirs()) {
                            return false;
                        }
                    }
                    subFiles = subFile.listFiles();
                    if (subFiles != null) {
                        subFileList.addAll(Arrays.asList(subFiles));
                    } 
                }
            }
        }
        
        return true;
    }
    
    
    /***
     * 将文件内容转换成2进制形式
     * @param file  传入文件
     * @return  文件的2进制形式
     * @throws IOException
     */
    public static byte[] readFile2Bytes(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Invalid file:" + file + "!");
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            
            return bos.toByteArray();
        } finally {
            IOUtils.closeQuietly(bos, fis);
        }
    }
    
    public static void main(String[] args) {
        try {
            //System.out.println(copyPath(new File("test1"), new File("test3")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(new File("test1").list()));
        
        File file = new File("C:/Users/admin/Desktop/常用文件/php.pdf");
        if (file.exists()) {
            System.out.println(file.length());
        }
    }
}
