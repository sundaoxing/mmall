package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
/*
            service层：文件上传
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    //日志打印
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 业务1：上传文件到Tomcat服务器的指定路径下
     *          1.获取file文件的源文件名
     *          2.截取源文件的扩展名
     *          3.使用UUID生成新的文件名
     *          4.判断Tomcat服务器的指定路径是否存在
     *              否：创建新的文件夹upload
     *              是：使用新的文件名和路径构造新的File对象
     *          5.调用transferTo（）方法将文件保存到新建的File对象（Tomcat服务器目录下）
     *          6.使用FTPUtil工具类把文件上传到远程FTP服务器的指定目录下
     *          7.删除本地文件（Tomcat服务器目录下）
     *          8.返回上传文件的文件名
     * @param file      MultipartFile类型的文件
     * @param path      Tomcat服务器的指定路径
     * @return          新的文件名
     */
    public String upload(MultipartFile file,String path){
        String filename = file.getOriginalFilename();
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，文件名为{}，上传的路径为{}，新文件名为{}",filename,path,uploadFileName);

        //本地路径不存在：则创建
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            //保存到本地
            file.transferTo(targetFile);
            //上传到远程FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //删除本地文件
            targetFile.delete();

        } catch (IOException e) {
            logger.error("文件上传异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
