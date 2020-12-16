package com.example.demo.service.image_recognition.impl;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.image_recognition.ImageRecognition;
import com.pensees.hbox.sdk.HboxClient;
import com.pensees.hbox.sdk.HboxHelper;
import com.pensees.hbox.sdk.bean.*;
import com.pensees.hbox.sdk.bean.cond.SearchSimilaryCondBean;
import com.pensees.hbox.sdk.exception.CompareFailedException;
import com.pensees.hbox.sdk.exception.LoginException;
import com.pensees.hbox.sdk.exception.OperationObjectNotFoundException;
import com.pensees.hbox.sdk.exception.OverLimitException;
import com.pensees.hbox.sdk.impl.operation.HboxLibraryOperationImpl;
import com.pensees.hbox.sdk.operation.HboxCompareOperation;
import com.pensees.hbox.sdk.operation.HboxFaceProfileOperation;
import com.pensees.hbox.sdk.operation.HboxLibraryListOperation;
import com.pensees.hbox.sdk.operation.HboxLibraryOperation;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
public class ImageRecognitionImpl implements ImageRecognition {
    private Logger logger=LoggerFactory.getLogger(ImageRecognitionImpl.class);
    @Override
    public JSONObject contrast(MultipartFile multipartFile, int number) throws LoginException, CompareFailedException, OperationObjectNotFoundException, IOException {
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        // 获取比对操作对象
        HboxCompareOperation operation = client.compareOps();
        // 传入照片路径
        SearchSimilaryCondBean similaryCondBean = new SearchSimilaryCondBean();
        //1 比 N 底库比对文件
        long[] repoIds = {number}; //底库ID
        similaryCondBean.setRepositoryIds(repoIds);//设置底库 Id 列表
        similaryCondBean.setRepoType("1");//设置搜索类型
        similaryCondBean.setThreshold(50);//设置阈值(0-100）
        similaryCondBean.setLimit(10);
        File file = new File("./image/test1.jpg");
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        SimilaryCompareResultBean similaryCompareResultBean1 = operation.searchSimilaryFaces(readBytesFromFile(file), similaryCondBean);// 1比N底库比对文件
        deleteDir("./image/test1.jpg");
        if (similaryCompareResultBean1.getComplete()) {
            for (SimilaryResult cb : similaryCompareResultBean1.getResult()) {
                if (cb.getHitImages().isEmpty()) {
                    System.out.println("未比中图片");
                    JSONObject object = new JSONObject();
                    String info = "比对失败";
                    int status = -1;
                    String Comment = "未比对成功";
                    String PersonID = null;
                    String Name = "陌生人";
                    object.put("status", status);
                    object.put("info", info);
                    object.put("Comment", Comment);
                    object.put("PersonID", PersonID);
                    object.put("Name", Name);
                    logger.debug(Comment);
                    return object;
                }
                for (SimilaryHitImage tis1 : cb.getHitImages()) {
                    for (long repoId : repoIds) {
                        //获取底库列表操作对象
                        HboxLibraryListOperation operation1 = client.libraryListOps();
                        HboxLibraryOperation libraryOps = operation1.libraryOps(repoId);
                        HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(tis1.getFace_image_id());
                        ProfileBean profileInfo = profileOperation.retriveInfo();
//                            System.out.println(profileInfo.getName());    // 获取姓名
//                            System.out.println(profileInfo.getPersonId());// 获取证件号码
//                            System.out.println(profileInfo.getComment()); // 获取备注信息
                        JSONObject object = new JSONObject();
                        int status = 1;
                        String info = "比对成功";
                        object.put("status", status);
                        object.put("info", info);
                        object.put("Comment", profileInfo.getComment());
                        object.put("PersonID", profileInfo.getPersonId());
                        object.put("Name", profileInfo.getName());
                        logger.info(info);
                        return object;
                    }
                }
            }
        }
        JSONObject object = new JSONObject();
        String info = "比对失败";
        int status = -1;
        String Comment = "未识别出人脸信息";
        String PersonID = null;
        String Name = null;
        object.put("status", status);
        object.put("info", info);
        object.put("Comment", Comment);
        object.put("PersonID", PersonID);
        logger.debug(Comment);
        return object;
//        return null;
    }

    @Override
    public JSONObject add_library(String setName, int setFaceNum, String setAddFlag, String setAlarmLevel, int setAlaramSound, int setAuthType, String setCommit, int setSurveilNum, int setDelFlag, int setFunctionType, String setType, String setZimgNode) throws LoginException {
        // 创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        //新建底库
        LibraryBean newlibraryBean = new LibraryBean();
        newlibraryBean.setName(setName);//设置底库名称
        newlibraryBean.setFaceNum(setFaceNum);//设置图片数量
        newlibraryBean.setAddFlag(Integer.parseInt(setAddFlag)); //添加标记
        newlibraryBean.setAlarmLevel(Integer.parseInt(setAlarmLevel));//设置警报等级
        newlibraryBean.setAlarmSound(setAlaramSound);//设置音频告警
        newlibraryBean.setAuthType(setAuthType);//设置用户类型
        newlibraryBean.setComment(setCommit);//设置备注
        newlibraryBean.setSurveilNum(setSurveilNum);//设置布控数量
        newlibraryBean.setDelFlag(setDelFlag);//设置删除标记
        newlibraryBean.setFunctionType(setFunctionType);//设置方法类型
        newlibraryBean.setType(Integer.parseInt(setType));//设置底库类型   0 白名单   1 黑名单
        newlibraryBean.setZimgNode(setZimgNode);//设置 zimg 节点
        try {
            long repoId = operation.create(newlibraryBean);// 导入进sdk接口中
            JSONObject object = new JSONObject();
            object.put("repoID",repoId);
            object.put("data", newlibraryBean);
            return object;
        } catch (Exception e) {
            JSONObject object = new JSONObject();
            String error = "添加失败"+e.getMessage();
            object.put("repoID","null");
            object.put("data", newlibraryBean);
            object.put("error",error);
            logger.debug(error);
            return object;
        }
    }

    @Override
    public JSONObject delete_library(int repoId) throws LoginException {
        // 创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        try{
            HboxLibraryOperation libraryOps = operation.libraryOps(repoId);//导入sdk接口中
            libraryOps.delete();// 导入后生效
            JSONObject object = new JSONObject();
            String delete = "true";
            object.put("delete",delete);
            object.put("repositoryId",repoId);
            return object;
        }catch (Exception e){
            JSONObject object = new JSONObject();
            String error = "删除失败"+e.getMessage();
            String info = "-1";
            object.put("info",info);
            object.put("error",error);
            logger.debug(error);
            return object;
        }

    }

    @Override
    public JSONObject add_picture(MultipartFile picture, int repoId, String personID, String name, int certType, String Gender,String Comment) throws LoginException, OperationObjectNotFoundException {
        // 创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        HboxLibraryOperation library = new HboxLibraryOperationImpl(client, repoId);
        System.out.println(library);
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
        ProfileBean profileBean = new ProfileBean();
        profileBean.setPersonId(String.valueOf(personID));//设置人物
        profileBean.setName(name);//设置名称
        profileBean.setCertType(String.valueOf(certType));//设置证件类型
        profileBean.setRepositoryId(repoId);//设置repoId
        profileBean.setComment(Comment);//设置Comment备注
        profileBean.setGender(Integer.parseInt(Gender));//设置性别
        File file = new File("./image/test1.jpg");
        String faceId;
        try {
            FileUtils.copyInputStreamToFile(picture.getInputStream(), file);// 传输的文件流实体化
            long size = getFileSize("./image/test1.jpg");// 测量文件大小
            if (size<=2097152){
                faceId = libraryOps.addFace(file, profileBean);// 传入sdk 返回faceID
//                System.out.println(faceId);
                JSONObject object = new JSONObject();
                HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(faceId);
//                System.out.println(faceId);
                object.put("faceId",faceId);
                //获取人像信息
                ProfileBean profileInfo = profileOperation.retriveInfo();
                object.put("data",profileInfo);
                logger.info(String.valueOf(object));
                return object;
            }else {
                System.out.println("文件传输过大"+picture.getOriginalFilename());
                System.out.println("文件传输过大");
                JSONObject object = new JSONObject();
                object.put("文件传输过大:",picture.getOriginalFilename());
                logger.debug(String.valueOf(object));
                return object;
            }
        } catch (OverLimitException | IOException e) {
            JSONObject object = new JSONObject();
            String error = "添加失败"+e.getMessage();
            String info = "-1";
            object.put("info",info);
            object.put("error",error);
            logger.debug(String.valueOf(object));
            return object;
        }
        //        return null;
    }

    @Override
    public JSONObject add_pictures(MultipartFile filelist, int repoId, String certType, String gender) throws LoginException, IOException, OperationObjectNotFoundException {
        // 创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
        Date dt1 = new Date();
        long dt = dt1.getTime();//获取系统时间
        File file = new File("./" + dt + ".zip");//创建本地zip包路径
        FileUtils.copyInputStreamToFile(filelist.getInputStream(), file);//将post的文件流转换为zip包
        // 解压文件
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)),Charset.forName("GBK"));
            zipUncompres(zis,"./" + dt);
            zis.close();
            deleteDir("./" + dt + ".zip");
        } catch (Exception e) {
            System.out.println("解压失败:" + e.getMessage());
            JSONObject object = new JSONObject();
            String error = "解压失败"+e.getMessage();
            String info = "-1";
            object.put("info",info);
            object.put("error",error);
            logger.debug(error);
            return object;
        }
        // 文件名切片处理
        try {
            File filed = new File("./" + dt);
            String files[];
            String list[];
            files=filed.list();
            list =filed.list();
            int True_time = 0;
            int False_time = 0;
            JSONObject object = new JSONObject();
            JSONObject object2 = new JSONObject();
            for (int i = 0; i < files.length; i++) {
                ProfileBean profileBean = new ProfileBean();
                try {
                    System.out.println(files[i]);
                    list[i]=files[i].substring(0,files[i].lastIndexOf("."));
                    String[] Files = list[i].split("_");
                    int time = 1;
                    for (String n : Files) {
                        if (time % 4 == 0) {
                            break;
                        } else {
                            if (time == 1) {
                                profileBean.setPersonId(n);//设置人物 id
                                profileBean.setCertType(certType);//设置证件类型
                                profileBean.setGender(Integer.parseInt(gender));//设置性别（无效）
                                profileBean.setRepositoryId(repoId);//放入库ID
                            } else if (time == 2) {
                                profileBean.setName(n);//设置名称
                            } else if (time == 3) {
                                profileBean.setComment(n);//设置备注
                            }
                            time++;
                        }
                    }
                    // 执行单个导入
                    String faceId = null;
                    try {
                        File picture = new File("./" + dt + "/" + files[i]);
                        // 获取文件大小
                        long size = getFileSize("./" + dt + "/" + files[i]);
                        System.out.println("./" + dt + "/" + files[i]);
                        // 判断文件大小过滤过大文件，最大不能超过2mb
                        if (size <= 2097152) {
                            try {
                                if (profileBean.getPersonId()==null||profileBean.getName()==null||profileBean.getCertType()==null||profileBean.getComment()==null){
                                    JSONObject object3 = new JSONObject();
                                    object3.put("错误信息"+False_time, "名称错误");
                                    object3.put("错误对象"+False_time, files[i]);
                                    object2.put("错误"+False_time,object3);
                                    False_time++;
                                }else {
                                    faceId = libraryOps.addFace(picture, profileBean);
                                    HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(faceId);
                                    True_time++;
                                }
                            } catch (Exception e) {
                                JSONObject object3 = new JSONObject();
                                object3.put("错误信息"+False_time, e.getMessage());
                                object3.put("错误对象"+False_time, files[i]);
                                object2.put("错误"+False_time,object3);
                                False_time++;
                            }
                        } else {
                            System.out.println("文件传输过大" + picture.getParentFile());
                            object2.put("文件过大"+False_time, picture.getName());
                            False_time++;
                        }
                    } catch (Exception e) {
                        object2.put("人像到达上限"+False_time,e.getMessage());
                        False_time++;
                    }
                } catch (Exception e) {
                    object2.put("错误"+False_time, e.getMessage());
                    False_time++;
                }
            }
            deleteDir("./"+dt);
            object.put("True",True_time+"条数据已被处理");
            object.put("False",False_time+"条数据处理失败");
            if (False_time>0){
                object.put("error",object2);
            }
            logger.info(String.valueOf(object));
            return object;
        }catch (Exception e){
            JSONObject object = new JSONObject();
            String info = "-1";
            String error = "文件处理失败:"+e.getMessage();
            object.put("info",info);
            object.put("error",error);
            logger.debug(String.valueOf(object));
            return object;
        }
    }

    @Override
    public JSONObject Add_pictures(InputStream filelist, int repoId, String certType, String gender) throws LoginException, IOException, OperationObjectNotFoundException {
        // 创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
        ProfileBean profileBean = new ProfileBean();
        Date dt1 = new Date();
        long dt=dt1.getTime();
        File file = new File("./" + dt + ".zip");
        FileUtils.copyInputStreamToFile(filelist, file);
        // 解压文件
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)), Charset.forName("GBK"));
            zipUncompres(zis,"./" + dt);
            zis.close();
            deleteDir("./" + dt + ".zip");
        } catch (Exception e) {
            System.out.println("解压失败:" + e);
            JSONObject object = new JSONObject();
            String info = "-1";
            object.put("info", info);
            object.put("error", e);
            return object;
        }
        // 文件名切片处理
        try {
            File filed = new File("./" + dt);
            String files[];
            String list[];
            files = filed.list();
            list =filed.list();
            int True_time = 0;
            int False_time = 0;
            int error_time = 0;
            JSONObject object = new JSONObject();
            JSONObject object2 = new JSONObject();
            for (int i = 0; i < files.length; i++) {
                try {
                    System.out.println(files[i]);
                    list[i]=files[i].substring(0,files[i].lastIndexOf("."));
                    String[] Files = files[i].split("_");
                    int time = 1;
                    for (String n : Files) {
                        if (time % 4 == 0) {
                            break;
                        } else {
                            if (time == 1) {
                                profileBean.setPersonId(n);//设置人物 id
                                profileBean.setCertType(certType);//设置证件类型
                                profileBean.setGender(Integer.parseInt(gender));//设置性别（无效）
                                profileBean.setRepositoryId(repoId);//放入库ID
                            } else if (time == 2) {
                                profileBean.setName(n);//设置名称
                            } else if (time == 3) {
                                profileBean.setComment(n);//设置备注
                            }
                            time++;
                        }
                    }
                    // 执行单个导入
                    String faceId = null;
                    try {
                        File picture = new File("./" + dt + "/" + files[i]);
                        // 获取文件大小
                        long size = getFileSize("./" + dt + "/" + files[i]);
                        System.out.println("java.txt文件大小为: " + size);
                        // 判断文件大小过滤过大文件，最大不能超过2mb
                        if (size <= 2097152) {
                            try {
                                if (profileBean.getPersonId() == null || profileBean.getName() == null || profileBean.getCertType() == null || profileBean.getComment() == null) {
                                    JSONObject object3 = new JSONObject();
                                    object3.put("错误信息" + False_time, "名称错误");
                                    object3.put("错误对象" + False_time, files[i]);
                                    object2.put("错误" + False_time, object3);
                                    False_time++;
                                }else {
                                    faceId = libraryOps.addFace(picture, profileBean);
                                    HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(faceId);
                                    True_time++;
                                }
                            } catch (Exception e) {
                                JSONObject object3 = new JSONObject();
                                object3.put("错误信息"+False_time, e.getMessage());
                                object3.put("错误对象"+False_time, files[i]);
                                object2.put("错误"+False_time,object3);
                            }
                        } else {
                            System.out.println("文件传输过大" + picture.getName());
                            object2.put("文件过大", picture.getName());
                            False_time++;
                        }
                    } catch (Exception e) {
                        System.out.println("人像到达上限" + e.getMessage());
                        error_time++;
                    }
                }catch (Exception e){
                    object.put("错误",e.getMessage());
                    False_time++;
                }
            }
            deleteDir("./"+dt);
            object.put("True", True_time + "条数据已被处理");
            object.put("False", False_time + "条数据处理失败");
            if (False_time >= 0) {
                object.put("error:", object2);
            }
            logger.info(String.valueOf(object));
            return object;
        }catch (Exception e){
            JSONObject object = new JSONObject();
            String info = "-1";
            object.put("info",info);
            object.put("error",e.getMessage());
            logger.debug(String.valueOf(object));
            return object;
        }
    }

    @Override
    public JSONObject delete_picture(int faceId, int repoId) throws LoginException, OperationObjectNotFoundException {
        //创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        //获取单个底库操作对象
        HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
        //获取人像操作对象
        HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(String.valueOf(faceId));
        boolean delete = profileOperation.delete();
        if (delete) {
            System.out.println("删除成功");
            JSONObject object = new JSONObject();
            String True = "删除成功";
            object.put("True",True);
            object.put("faceId",faceId);
            object.put("repoId",repoId);
            logger.info(String.valueOf(object));
            return object;
        }else {
            System.out.println("删除失败");
            JSONObject object = new JSONObject();
            String error = "删除失败";
            String info = "-1";
            object.put("info",info);
            object.put("error",error);
            logger.debug(String.valueOf(object));
            return object;
        }
    }

    @Override
    public JSONObject select_picture(int faceId, int repoId) throws LoginException {
        //创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        try {
            //获取单个底库操作对象
            HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
            //获取人像操作对象
            HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(String.valueOf(faceId));
            //获取人像信息
            ProfileBean profileInfo = profileOperation.retriveInfo();
            System.out.println(profileInfo.getFaceImageUrl());
            JSONObject object = new JSONObject();
            object.put("data",profileInfo);
            logger.info(String.valueOf(object));
            return object;
        } catch (OperationObjectNotFoundException e) {
            System.out.println("查询失败");
            JSONObject object = new JSONObject();
            String error = "查询失败:"+e.getMessage();
            String info = "-1";
            object.put("info",info);
            object.put("error",error);
            logger.debug(String.valueOf(object));
            return object;
        }
    }

    @Override
    public JSONObject update_picture(int faceid, int repoId) throws LoginException, OperationObjectNotFoundException {
        //创建连接
        HboxClient client = HboxHelper.createClient("192.168.1.66", 8080, "system", "111111");
        //获取底库列表操作对象
        HboxLibraryListOperation operation = client.libraryListOps();
        //获取单个底库操作对象
        HboxLibraryOperation libraryOps = operation.libraryOps(repoId);
        //获取人像操作对象
        HboxFaceProfileOperation profileOperation = libraryOps.faceProfileOps(String.valueOf(faceid));
        //获取人像信息
        ProfileBean profileInfo = profileOperation.retriveInfo();
        //更新人像信息
        int update = profileOperation.updateFaceImage(new byte[10],profileInfo.getFaceImageId());
        switch (update){
            case 0:
                JSONObject object = new JSONObject();
                String True = "更新成功";
                String info = "1";
                object.put("info",info);
                object.put("True",True);
                return object;
            case 1:
                JSONObject object2 = new JSONObject();
                String False = "未检测到人脸";
                String info2 = "-1";
                object2.put("info",info2);
                object2.put("False",False);
                return object2;
            case 2:
                JSONObject object3 = new JSONObject();
                String error = "更新失败";
                String info3 = "-1";
                object3.put("info",info3);
                object3.put("errpr",error);
                return object3;
            default:
                break;

        }return null;
    }

    private static byte[] readBytesFromFile(File file) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            bytesArray = new byte[(int) file.length()];
            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }

    // 获取文件大小
    public static long getFileSize(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在");
            return -1;
        }
        return file.length();
    }

    // ZIP包的解压
    public static void zipUncompres(ZipInputStream zis, String destDirPath) {
        ZipEntry zipEntry = null;
        try {
            while ((zipEntry = zis.getNextEntry()) != null) {
                File file = new File(destDirPath + File.separator + zipEntry.getName());
                // 如果目录不存在就创建
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if (!zipEntry.isDirectory()) {
                    // 读取Entry对象
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = zis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                    }
                    bos.close();
                } else {
                    file.mkdirs();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 删除文件以及文件夹
    public static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if(file.isFile()) {
            file.delete();
        }else {
            File[] files = file.listFiles();
            if(files == null) {
                file.delete();
            }else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }
}

