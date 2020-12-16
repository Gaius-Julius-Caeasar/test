package com.example.demo.web.image_recognition;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.image_recognition.ImageRecognition;
import com.pensees.hbox.sdk.exception.LoginException;
import com.pensees.hbox.sdk.exception.OperationObjectNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "HBox")
@RestController
@RequestMapping("/Image")
public class ImageRecognitionController {
    @Resource
    ImageRecognition imageRecognition;
    @PostMapping("/contrast")
    @ApiOperation("照片查询")@ApiImplicitParams({
            @ApiImplicitParam(name = "repoId",value = "库ID"),
    })
    public JSONObject post1(@RequestParam("picture") MultipartFile multipartFile,
                           @RequestParam("repoId") int repoId){
       try{
           JSONObject object = imageRecognition.contrast(multipartFile,repoId);
           return object;
       }catch (Exception e){
           JSONObject object = new JSONObject();
           String info = "比对失败";
           int status = -2;
           String Comment = "图片异常"+e.getMessage();
           object.put("status",status);
           object.put("info",info);
           object.put("Comment",Comment);
           return object;
       }
    }

    @PostMapping("/add_library")
    @ApiOperation("添加底库的接口")@ApiImplicitParams({
            @ApiImplicitParam(name = "setName",value = "库名字"),
            @ApiImplicitParam(name = "setAddFlag",value = "添加标记"),
            @ApiImplicitParam(name = "setAlarmLevel",value = "设置警报等级"),
            @ApiImplicitParam(name = "setType",value = "设置底库类型"),
            @ApiImplicitParam(name = "setCommit",value = "备注")
    })
    public JSONObject post2(
                           @RequestParam("setName") String setName,
                           @RequestParam("setAddFlag") String setAddFlag,
                           @RequestParam("setAlarmLevel") String setAlarmLevel,
                           @RequestParam("setType") String setType,
                           @RequestParam("setCommit") String setCommit){
        try {
            int setFaceNum = 2; //设置图片数量
            int setAlaramSound = 1; //设置音频告警
            int setAuthType = 1;   //设置用户类型
            int setSurveilNum = 10; //设置布控数量
            int setDelFlag = 0;
            int setFunctionType = 1;
            String setZimgNode = "";
            JSONObject object = imageRecognition.add_library(setName, setFaceNum, setAddFlag, setAlarmLevel, setAlaramSound, setAuthType, setCommit, setSurveilNum, setDelFlag, setFunctionType, setType, setZimgNode);
            return object;
        }catch (Exception e){
            JSONObject object = new JSONObject();
            String info = "添加失败";
            int status = -2;
            String Comment = "添加底库失败";
            String repoName = "无";
            object.put("status",status);
            object.put("info",info);
            object.put("Comment",Comment);
            object.put("repoName",repoName);
            return object;
        }
    }

    @PostMapping("/delete_library")
    @ApiOperation("删除底库的接口")@ApiImplicitParams({
            @ApiImplicitParam(name = "repoId",value = "库ID"),
    })
    public JSONObject post3(@RequestParam("repoId") int repoId){
        try{
            JSONObject object = imageRecognition.delete_library(repoId);
            return object;
        }catch (Exception e){
            JSONObject object = new JSONObject();
            String info = "删除失败";
            int status = -2;
            String Comment = "删除底库失败";
            String repoName = "无";
            object.put("status",status);
            object.put("info",info);
            object.put("Comment",Comment);
            object.put("repoName",repoName);
            return object;
        }
    }

    @PostMapping("/add_picture")
    @ApiOperation("添加用户的接口")@ApiImplicitParams({
            @ApiImplicitParam(name = "repoId",value = "库ID"),
            @ApiImplicitParam(name = "PersonId",value = "卡ID"),
            @ApiImplicitParam(name = "Name",value = "名字"),
            @ApiImplicitParam(name = "Comment",value = "备注")
    })
    public JSONObject post3(@RequestParam("picture") MultipartFile picture,
                            @RequestParam("repoId") int repoId,
                            @RequestParam("PersonId") String PersonID,
                            @RequestParam("Name") String Name,
                            @RequestParam("Comment") String Comment){
        try {
            int CertType = 1;
            String Gender = "1";
            JSONObject object = imageRecognition.add_picture(picture,repoId,PersonID,Name,CertType,Gender,Comment);
            return object;
        }catch(Exception e){
            JSONObject object = new JSONObject();
            System.out.println(e);
            String error = "添加失败"+e;
            String info = "-2";
            object.put("info",info);
            object.put("error",error);
            return object;
        }
    }

    @PostMapping("/add_pictures")
    @ApiOperation("添加用户的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repoId",value = "库ID")
    })
    public JSONObject post3s(@RequestParam("filelist") MultipartFile filelist,
                             @RequestParam("repoId") int repoId) throws LoginException, IOException, OperationObjectNotFoundException {
        try {
            String CertType = "1";
            String Gender = "1";
            JSONObject object = imageRecognition.add_pictures(filelist,repoId,CertType,Gender);
            return object;
        }catch(Exception e){
            JSONObject object = new JSONObject();
            String error = "批量添加失败--"+e.getMessage();
            String info = "-2";
            object.put("info",info);
            object.put("error",error);
            return object;
        }
    }

    @PostMapping("/Add_pictures")
    @ApiOperation("添加用户的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repoId",value = "库ID")
    })
    public JSONObject Post3s(HttpServletRequest filelist, HttpServletResponse response,
                             @RequestParam("repoId") int repoId) throws Exception {
        try {
            String CertType = "1";
            String Gender = "1";
            Map<String,Object> responseMap = imageRecognition.Add_pictures(filelist.getInputStream(),repoId,CertType,Gender);
            // 生成响应的json字符串
            String jsonResponse = JSONObject.toJSONString(responseMap);
            sendResponse(jsonResponse,response);
        }catch(Exception e){
            Map<String,Object> responseMap = new HashMap<>();
            String error = "批量添加失败--"+e.getMessage();
            String info = "-2";
            responseMap.put("info",info);
            responseMap.put("error",error);
            //生成响应的json字符串
            String jsonResponse = JSONObject.toJSONString(responseMap);
            sendResponse(jsonResponse,response);
        }
        return null;
    }



    @PostMapping("/delete_picture")
    @ApiOperation("删除用户的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "faceId",value = "脸ID"),
            @ApiImplicitParam(name = "repoId",value = "库ID")
    })
    public JSONObject post4(@RequestParam("faceId") int faceid,
                            @RequestParam("repoId") int repoId){
        try {
            JSONObject object = imageRecognition.delete_picture(faceid, repoId);
            return object;
        } catch (LoginException | OperationObjectNotFoundException e) {
            JSONObject object = new JSONObject();
            String error = "删除失败";
            String info = "-2";
            object.put("info",info);
            object.put("error",error);
            return object;
        }
    }

    @PostMapping("/select_picture")
    @ApiOperation("查询用户的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "faceId",value = "脸ID"),
            @ApiImplicitParam(name = "repoId",value = "库ID")
    })
    public JSONObject post5(@RequestParam("faceId") int faceid,
                            @RequestParam("repoId") int repoId){
        try {
            JSONObject object = imageRecognition.select_picture(faceid,repoId);
            return object;
        } catch (Exception e) {
            JSONObject object = new JSONObject();
            String error = "查询失败";
            String info = "-2";
            object.put("info",info);
            object.put("error",error);
            return object;
        }
    }

    @PostMapping("/update_picture")
    @ApiOperation("更新用户的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "faceId",value = "脸ID"),
            @ApiImplicitParam(name = "repoId",value = "库ID")
    })
    public JSONObject post6(@RequestParam("faceId") int faceid,
                            @RequestParam("repoId") int repoId){
        try {
            JSONObject object = imageRecognition.update_picture(faceid,repoId);
            return object;
        } catch (Exception e) {
            JSONObject object = new JSONObject();
            String error = "更新失败";
            String info = "-2";
            object.put("info",info);
            object.put("error",error);
            return object;
        }
    }
    private void sendResponse(String responseString,HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.write(responseString);
            pw.flush();
        } finally {
            IOUtils.closeQuietly(pw);
        }
    }
}
