package com.mmall.util;

import com.mmall.vo.FTPVo;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/*
    FTP服务器连接，上传文件工具类
 */
public class FTPUtil {
    //日志打印
    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    /**
     * 上传文件到ftp服务器
     *              1.构造ftp服务器连接对象所需的参数
     *              2.logger打印信息
     *              3.调用uploadFile（）方法，指定远程ftp服务器的目录
     *              4.logger打印信息
     *              5.返回上传结果的boolean值
     * @param fileList  文件List集合
     * @return          上传结果的boolean值
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPVo ftp = new FTPVo(PropertiesUtil.getProperty("ftp.server.ip"),21,
                PropertiesUtil.getProperty("ftp.user"),PropertiesUtil.getProperty("ftp.pass"));
        logger.info("开始连接FTP服务器");
        boolean result = uploadFile("img",fileList,ftp);
        logger.info("结束上传，上传结果：{}",result);
        return result;
    }

    /**
     *上传文件到ftp服务器
     *              1.构造FTPClient对象
     *              2.调用connectFTP（）方法用来连接ftp服务器
     *              3.设置FTPClient客户端的相关参数的值
     *              4.批量上传文件到ftp服务器
     *              5.关闭资源
     *              6.返回上传结果的boolean值
     * @param remotePath    远程ftp服务器的指定目录下
     * @param fileList      文件List集合
     * @param ftp           ftp对象，
     * @return              上传结果的boolean值
     * @throws IOException
     */
    private static boolean uploadFile(String remotePath,List<File>fileList,FTPVo ftp) throws IOException {
        boolean uploaded =true;//上传结果
        FileInputStream fileInputStream = null;
        FTPClient ftpClient = new FTPClient();
        if(connectFTP(ftp,ftpClient)){//连接ftp服务器
            try {
                boolean r = ftpClient.changeWorkingDirectory(remotePath);//切换到指定目录
                logger.info("有没有切换目录{}",r);
                ftpClient.setBufferSize(1024);//设置缓冲区
                ftpClient.setControlEncoding("UTF-8");//设置字符集
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//设置文件类型：二进制
//                ftpClient.enterLocalPassiveMode();//设置成被动模式：pasv模式

                for(File fileItem :fileList){
                    fileInputStream = new FileInputStream(fileItem);//文件输入流
                    ftpClient.storeFile(fileItem.getName(),fileInputStream);//保存文件
                }
            } catch (IOException e) {
                uploaded=false;
                logger.error("上传文件到FTP服务器异常",e);
            }finally {
                fileInputStream.close();//释放文件流资源
                ftpClient.disconnect();//断开连接
            }
        }
        return uploaded;
    }

    /**
     * 连接FTP服务器
     *          1.连接ftp服务器
     *          2.登陆ftp服务器（使用创建好的用户名和密码）
     *          3.返回连接结果的boolean值
     * @param ftp           ftp对象：用来连接ftp服务器的相关参数
     * @param ftpClient     ftp客户对象
     * @return              连接结果的boolean值
     */
    private static boolean connectFTP(FTPVo ftp,FTPClient ftpClient){
        boolean isSuccess = false;

        try {
            ftpClient.connect(ftp.getIp());
            isSuccess =ftpClient.login(ftp.getUser(),ftp.getPassword());
        } catch (IOException e) {
            logger.error("FTP服务器连接异常",e);
        }
        return isSuccess;
    }

}
