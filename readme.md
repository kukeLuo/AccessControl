三合一门禁流程:
功能: 门禁功能、基础设置
门禁又包含办公室门禁、VIP模式门禁、会议模式门禁 3块，其中每一块都提供基本RGB检测和红外检测
三者模式基本都相同，只是UI不同，故以办公室门禁为例讲解整体流程

## RGB 检测
1. 正常状态下显示动画特效，但是RGB检测常开 processRGBRunnable
2. RGB检测到人脸后隐藏动画，给人脸画框 drawFaceInfos
3. 做人脸检测识别并提取特征比对 doDetectRGBFaceAction -> checkPersonHasPermit
4. 比对成功 -> detectMatchedFace ; 失败 -> showNoMatchFaceFail
5. 将成功及失败(识别错误人脸)结果保存到服务器

## 红外检测
1. 正常状态下显示动画特效，但是RGB检测常开 processRGBRunnable
2. RGB检测到人脸后隐藏动画，给人脸画框 drawFaceInfos并提取出最大人脸区域, 并打开红外摄像头IR
3. 红外摄像头打开并获取到数据后处理 processIRRunnable -> detectFaceInIrFrame
4. 做人脸检测识别并提取特征比对 doDetectRGBFaceAction，同时如果检测最大人脸区域
5. 判别RGB最大人脸和IR最大人脸是否位置一致，一致则判别是否是活体 FaceSDKManager.checkIrLiveness
6. 如果是活体则进行人脸检测识别 checkPersonHasPermit，流程参考RBG检测的4即可

## mqtt事件 mqttservice
1. ADD_PERSON                   下发人脸
- 目前人脸数据库是以 personId 作为DB的primary key，这样保证了单个人在多个组下发中也只有一份人脸数据，保证了数据唯一性
- 先下载人脸，提取出人脸特征，只有检测到人脸特征才认为该人脸数据有效。如果检测失败则通知服务器
2. DEL_PERSON                   删除人脸
3. ADD_MEETING                  添加会议
4. DEL_MEETING                  删除会议
5. UPDATE_MEETING               更新会议
6. REBOOT/RESET_DEIVCE          重启设备
7. DEL_ALL_PERSON               删除所有人员
8. OPEN_DOOR                    开门
9. KEEP_OPEN_DOOR               保持门常开
10. CLOSE_KEEP_OPEN_DOOR        关闭门常开
11. GET_PERSON                  获取平板人脸数据
- 为了校验平板人脸数据是否下发完整，服务器向该平板请求人脸数量，平板通过 sendBase2Server 通知服务器

## DB
- 平板用户      UserDatabase
- 会议信息      MeetingDatabase
- 通行记录      RecordDatabase
- 失败人脸记录   FailRecordDatabase

## 关键类
- BaseDateTimeActivity 该页面主要用于处理与时间相关内容，因为平板有时间刷新，故每1分钟刷新一次
    - 每天凌晨 0点-8点整点处理之前通行记录因网络原因未上传的失败情况
    - 凌晨3点进行版本检测升级
    - 凌晨4点重启(之前为了规避摄像头内存泄漏，目前可以不需要了)
    - 每一小时请求天气数据
    - 顶灯亮起时间设置(默认晚上6点-凌晨8点)
    - 刷新会议信息
    - 每2分钟进行心跳发送给服务器，用于服务器检测设备状态
    
- BaseRGBCameraActivity / BaseLiveCameraActivity extends BaseCameraActivity extends BasePreviewActivity

## MQTT ACTION
http://sit.iot.brc.com.cn:32007/#/ admin#brc@1234


