package com.example.demo.service.image_recognition;

import com.alibaba.fastjson.JSONObject;
import com.pensees.hbox.sdk.exception.CompareFailedException;
import com.pensees.hbox.sdk.exception.LoginException;
import com.pensees.hbox.sdk.exception.OperationObjectNotFoundException;
import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface ImageRecognition {
    JSONObject contrast(MultipartFile multipartFile , int number) throws LoginException, CompareFailedException, OperationObjectNotFoundException, IOException, JSONException;
    JSONObject add_library(String setName, int setFaceNum, String setAddFlag, String setAlarmLevel, int setAlaramSound, int setAuthType, String setCommit, int setSurveilNum, int setDelFlag, int setFunctionType, String setType, String setZimgNode) throws LoginException;
    JSONObject delete_library(int repoId) throws LoginException;

    JSONObject add_picture(MultipartFile picture, int repoId, String personID, String name, int certType, String Comment, String Gender) throws LoginException, OperationObjectNotFoundException;
    JSONObject add_pictures(MultipartFile filelist, int repoId, String certType, String gender) throws LoginException, IOException, OperationObjectNotFoundException;
    JSONObject Add_pictures(InputStream filelist, int repoId, String certType, String gender) throws LoginException, IOException, OperationObjectNotFoundException;

    JSONObject delete_picture(int repoId, int faceId) throws LoginException, OperationObjectNotFoundException;
    JSONObject select_picture(int faceid, int repoId) throws LoginException;
    JSONObject update_picture(int faceid, int repoId) throws LoginException, OperationObjectNotFoundException;
}
