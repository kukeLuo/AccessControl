package com.brc.acctrl.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.brc.acctrl.bean.AccessUser;
import com.brc.acctrl.bean.MeetParticipant;
import com.brc.acctrl.bean.RspUserProperty;
import com.brc.acctrl.bean.UploadFailReq;
import com.brc.acctrl.bean.UploadFailRsp;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.deepglint.hri.db.Feature;
import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.utils.ArrayUtils;
import com.deepglint.hri.utils.FileUtils;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceSDKUtil {
    private static FaceSDKUtil sdkInstance;

    public static FaceSDKUtil getInstance() {
        if (null == sdkInstance) {
            synchronized (FaceSDKUtil.class) {
                if (null == sdkInstance) {
                    sdkInstance = new FaceSDKUtil();
                }
            }
        }

        return sdkInstance;
    }

    // 这里有个隐患就是如果之前是人脸但是上传了一张非人脸，则图片替换了，但是后面提取人脸时不对，导致了异常
    // 因为是单线程，所以可以先用temp.jpg作为中转，检测到人脸后再改名即可
    public String addNewUser(RspUserProperty userProperty) {
        // device save user face file
        File tempFile =
                new File(FaceSDKUtil.getInstance().getFaceFolderPath(), "temp.jpg");
        if (tempFile.exists()) {
            tempFile.delete();
        }

        String userFaceTempPath = tempFile.getAbsolutePath();
        boolean saveResult = downloadUserPic(userProperty.getFaceUrl(), userFaceTempPath);

        if (saveResult) {
            // 提取人脸特征数据
            LogUtil.d("extract user feature = " + userProperty.getName());

            Mat faceFeature = new Mat();
            String result = extractFaceData(userFaceTempPath, userProperty.getPersonId(), faceFeature);
            if (TextUtils.isEmpty(result)) {
                // 保存用户数据
                // 因为后续是使用该标准头像展示，所以不适合用矫正头像来画
                File userIdFile =
                        new File(FaceSDKUtil.getInstance().getFaceFolderPath(),
                                userProperty.getPersonId() + ".jpg");
                addUserToDB(faceFeature, userIdFile.getAbsolutePath(), userProperty);
            }
            return result;
        } else {
            return "下载人脸失败";
        }
    }

    public String addMeetingUser(MeetParticipant participant, String meetingId,
                                 long startTime, long endTime) {
        // device save user face file
        File tempFile =
                new File(FaceSDKUtil.getInstance().getFaceFolderPath(), "temp.jpg");
        if (tempFile.exists()) {
            tempFile.delete();
        }

        String userFaceTempPath = tempFile.getAbsolutePath();
        boolean saveResult = downloadUserPic(participant.getPersonurl(), userFaceTempPath);

        if (saveResult) {
            // 提取人脸特征数据,只有检测到人脸才会替换，否则不替换
            LogUtil.d("extract user feature = " + participant.getPersonName());

            Mat faceFeature = new Mat();
            String strResult = extractFaceData(userFaceTempPath, participant.getPersonId(), faceFeature);
            if (TextUtils.isEmpty(strResult)) {
                // 因为后续是使用该标准头像展示，所以不适合用矫正头像来画
                File userIdFile =
                        new File(FaceSDKUtil.getInstance().getFaceFolderPath(),
                                participant.getPersonId() + ".jpg");

                addMeetingUserToDB(faceFeature, userIdFile.getAbsolutePath(), participant,
                        meetingId, startTime, endTime);

            }

            return strResult;
        } else {
            return "下载人脸失败";
        }
    }

    private boolean downloadUserPic(String userAvatarUrl, String saveFacePath) {
        int dlCnt = 0;
        while (dlCnt < 3) {
            dlCnt++;
            HttpURLConnection connection = null;
            try {
                URL bitmapUrl = new URL(userAvatarUrl);
                connection = (HttpURLConnection) bitmapUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                //通过返回码判断网络是否请求成功
                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap dlBitmap = BitmapFactory.decodeStream(inputStream);
                    if (saveBitmap2File(dlBitmap, saveFacePath)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        return false;
    }

    /**
     * 保存方法
     */
    public boolean saveBitmap2File(Bitmap bitmap, String userFacePath) {
        try {
            FileOutputStream out = new FileOutputStream(userFacePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    private boolean addUserToDB(Mat faceFeature, String savedImagePath,
                                RspUserProperty userProperty) {
        // add face & feature to face sdk db
        AccessUser dbUser = new AccessUser();
        dbUser.setAvatarUrl(userProperty.getFaceUrl());
        dbUser.setUserId(userProperty.getPersonId());
        dbUser.setGroupId(userProperty.getGroupId());
        dbUser.setUsername(userProperty.getName());
        dbUser.setDeviceBmpPath(savedImagePath);
        dbUser.setPermissionId(userProperty.getPermissionId());
        dbUser.setValidStartTime(userProperty.getValidStartTime());
        dbUser.setValidEndTime(userProperty.getValidEndTime());
        dbUser.setNamePinyin(Chinese2Pinyin.changeToTonePinYin(userProperty.getName()));
        dbUser.setNamCapLetters(Chinese2Pinyin.changeToGetCapLetters(userProperty.getName()));
        // 需要人和会议匹配在一起
        dbUser.setMeetingId("0");

        // set feature
        Feature feature = new Feature();
        feature.setGroupId("0");
        feature.setUserId(userProperty.getPersonId());
        feature.setImageUrl(savedImagePath);
        float rawData[] = new float[faceFeature.rows() * faceFeature.cols()];
        faceFeature.get(0, 0, rawData);
        dbUser.setFeatureBytes(ArrayUtils.toByteArray(rawData));

        return UserDatabase.getInstance().getAccessUserDao().insertUser(dbUser) > 0;
    }

    private boolean addMeetingUserToDB(Mat faceFeature, String savedImagePath,
                                       MeetParticipant participant, String meetingId,
                                       long meetFaceStartTime, long meetFaceEndTime) {
        // add face & feature to face sdk db
        AccessUser dbUser = new AccessUser();
        dbUser.setAvatarUrl(participant.getPersonurl());
        dbUser.setUserId(participant.getPersonId());
        dbUser.setGroupId("0");
        dbUser.setUsername(participant.getPersonName());
        dbUser.setDeviceBmpPath(savedImagePath);
        dbUser.setPermissionId(meetingId + "$$" + participant.getPersonId());
        dbUser.setValidStartTime(meetFaceStartTime);
        dbUser.setValidEndTime(meetFaceEndTime);
        dbUser.setNamePinyin(Chinese2Pinyin.changeToTonePinYin(participant.getPersonName()));
        dbUser.setNamCapLetters(Chinese2Pinyin.changeToGetCapLetters(participant.getPersonName()));
        // 需要人和会议匹配在一起
        dbUser.setMeetingId(meetingId);

        // set feature
        Feature feature = new Feature();
        feature.setGroupId(meetingId);
        feature.setUserId(participant.getPersonId());
        feature.setImageUrl(savedImagePath);
        float rawData[] = new float[faceFeature.rows() * faceFeature.cols()];
        faceFeature.get(0, 0, rawData);
        dbUser.setFeatureBytes(ArrayUtils.toByteArray(rawData));

        return UserDatabase.getInstance().getAccessUserDao().insertUser(dbUser) > 0;
    }

    public String extractFaceData(String bmpFilePath, String personId, Mat feature) {
        // extract user face feature
        try {
            Bitmap alignedFaceImage = Bitmap.createBitmap(380, 380,
                    Bitmap.Config.RGB_565);
            InputStream inputStream = new FileInputStream(bmpFilePath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            FaceSDKManager.ErrorCode code =
                    FaceSDKManager.getInstance().extractFeatureWithDetect(
                            bitmap,
                            alignedFaceImage,
                            feature
                    );
            // save user info to db
            LogUtil.i("FACE extract " + personId + ":result = " + code);
            if (code == FaceSDKManager.ErrorCode.OK) {
                // 这里检测到人脸，所以这里需要替换下真实图片即可
                File userIdFile =
                        new File(FaceSDKUtil.getInstance().getFaceFolderPath(), personId + ".jpg");
                if (userIdFile.exists()) {
                    userIdFile.delete();
                }

                File bmpFile = new File(bmpFilePath);
                bmpFile.renameTo(userIdFile);

                return "";
            } else {
                return switchCode2Str(code);
            }
        } catch (FileNotFoundException e) {
            return "下载的图片文件未找到";
        }
    }

    private String switchCode2Str(FaceSDKManager.ErrorCode errorCode) {
        if (errorCode == FaceSDKManager.ErrorCode.UNINITIALIZED) {
            return "SDK 没有初始化";
        } else if (errorCode == FaceSDKManager.ErrorCode.NO_FACE) {
            return "SDK 未检测到人脸";
        } else if (errorCode == FaceSDKManager.ErrorCode.ALIGN_FAILED) {
            return "SDK 对齐人脸失败";
        } else if (errorCode == FaceSDKManager.ErrorCode.FEATURE_EXTRACT_FAILED) {
            return "SDK 人脸特征提取失败";
        } else if (errorCode == FaceSDKManager.ErrorCode.ATTRIBUTES_FAILED) {
            return "SDK 人脸属性特征失败";
        } else if (errorCode == FaceSDKManager.ErrorCode.FAILED) {
            return "SDK 其他失败";
        } else {
            return "SDK 其他错误";
        }
    }

    private String saveFacePicture(final Bitmap faceBitmap, String uid) {
        String path = FileUtils.getFaceDirectory().getPath();
        File userFile = new File(path, uid + ".jpg");

        if (userFile.exists()) {
            userFile.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(userFile);
            faceBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return userFile.getAbsolutePath();
    }

    private boolean saveBase64Picture(String base64DataStr,
                                      String userBmpFilePath) {
        // 1.去掉base64中的前缀
        String base64Str =
                base64DataStr.substring(base64DataStr.indexOf(",") + 1,
                        base64DataStr.length());

        // 2. 解析保存图片
        byte[] data = Base64.decode(base64Str, Base64.DEFAULT);

        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                data[i] += 256;
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(userBmpFilePath);
            os.write(data);
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getFaceFolderPath() {
        return FileUtils.getFaceDirectory().getPath();
    }

    public static File errJpgDirectory() {
        File sdRootFile = FileUtils.getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "errJpgs");
            if (!file.exists()) {
                boolean var2 = file.mkdirs();
            }
        }

        return file;
    }

    public boolean saveErrBitmap2File(Bitmap bitmap, String fileName) {
        File errFile = new File(errJpgDirectory(), fileName);
        try {
            FileOutputStream out = new FileOutputStream(errFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }
    public boolean saveLogBitmap2File(Bitmap bitmap, String fileName) {
        File logFile =
                new File(FaceSDKUtil.getInstance().getFaceFolderPath(), fileName);
        try {
            FileOutputStream out = new FileOutputStream(logFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public void deleteAllJpgFiles() {
        try {
            File faceFolder = new File(FaceSDKUtil.getInstance().getFaceFolderPath());
            File[] allFaceJpg = faceFolder.listFiles();
            if (allFaceJpg != null && allFaceJpg.length > 0) {
                for (File singleFile : allFaceJpg) {
                    if (singleFile.isFile()) {
                        singleFile.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFailImages() {
        // 这里应该上传来判别，而不是只上传图片
        Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                File[] errFiles = errJpgDirectory().listFiles();
                if (errFiles != null && errFiles.length > 0) {
                    for (File singleErrFile : errFiles) {
                        if (singleErrFile.getName().endsWith(".jpg")) {
                            // try to upload and delete
                            emitter.onNext(singleErrFile);
                            // add sleep or crash : too many files open
                            try {
                                Thread.sleep(300L);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Observer<File>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(File bmpFile) {
                try {
                    String base64Str = encodeBase64File(bmpFile);
                    RetrofitConfig.createService().uploadFailImage(new UploadFailReq(base64Str)).subscribeOn(Schedulers.io()).subscribe(new Observer<UploadFailRsp>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(UploadFailRsp rsp) {
                            // 如果上传成功则删除
                            try {
                                bmpFile.delete();
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
    public void deleteIndetifiFace(){
        String path=FaceSDKUtil.getInstance().getFaceFolderPath();
        float fileSize=getCacheSize(path);
        File file=new File(path);
        File[] files=file.listFiles();
        if(fileSize>20){
            for(File filePath:files){
                if(filePath.getName().contains("_")){
                    if(filePath.exists()&&filePath.isFile()){
                        filePath.delete();
                    }
                }
            }
        }
    }
    /**
     * 获得当前大小
     */
    private float getCacheSize(String path) {
        // TODO:设置数据显示
        float size = getFolderSize(new File(path));
        float size_show = (float) (Math.round(size / 1024.0f / 1024 * 100)) / 100;// (这里的100就是2位小数点,如果要其它位,如4位,这里两个100改成10000)
        if (size_show == 0) size_show = size == 0 ? 0 : 0.01f;
        return size_show;
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    private long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) size = size + getFolderSize(fileList[i]);
                else size = size + fileList[i].length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
    public static String encodeBase64File(File oriFile) throws Exception {
        FileInputStream inputFile = new FileInputStream(oriFile);
        byte[] buffer = new byte[(int) oriFile.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }
}
