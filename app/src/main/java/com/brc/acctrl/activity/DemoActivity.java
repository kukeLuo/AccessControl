package com.brc.acctrl.activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.brc.acctrl.R;
import com.brc.acctrl.camera.CameraControl;
import com.brc.acctrl.camera.ICameraControl;
import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.camera.source.CameraImageSource;
import com.brc.acctrl.camera.source.ImageFrame;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.CameraView;
import com.brc.acctrl.view.PreviewView;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.db.FaceDBManager;
import com.deepglint.hri.db.User;
import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceSDKOptions;
import com.deepglint.hri.facesdk.FaceTracker;
import com.deepglint.hri.utils.ImageUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;

public class DemoActivity extends BaseActivity {
    @BindView(R.id.preview_view)
    TexturePreviewView previewView;
    @BindView(R.id.texture_view)
    TextureView textureView;
    @BindView(R.id.img_mask)
    ImageView imgMask;
    @BindView(R.id.img_align)
    ImageView imgAlign;
    @BindView(R.id.camera_layout)
    LinearLayout cameraLayout;
    @BindView(R.id.btn_rotation)
    Button btnRotation;

    private int cameraRotation = 270;
    private CameraImageSource cameraImageSource;
    private OnFrameAvailableListener frameAvailableListener;
    private FaceSDKManager faceSDKManager;
    private boolean isMatching = false;
    private ImageFrame lastFrame;
    private int matchedID = -1;
    private String matchName;
    private HashMap<String, Mat> groupFeatures;
    private HashMap<Integer, String> featureIDs;
    private ExecutorService executorPool = Executors.newSingleThreadExecutor();
    private Runnable processRunnable = new Runnable() {
        @Override
        public void run() {
            if (lastFrame == null) {
                return;
            }
            Mat bgrImage;
            synchronized (lastFrame) {
                bgrImage = lastFrame.getBgrMat();
                lastFrame = null;
            }
            checkTrackFace(bgrImage);
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_demo;
    }

    @Override
    public void initViews() {
        GoldenEyesUtils.hideSystemBar(this);
        initCamera();
        initFaceSDK();

        btnRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateCamera();
            }
        });
    }

    private void rotateCamera() {
        if (null != cameraImageSource) {
            cameraImageSource.stop();
            cameraImageSource.getCameraControl().setCameraIndex(
                    CameraControl.CAMERA_RGB
            );
            cameraRotation = (cameraRotation + 90) % 360;
            cameraImageSource.getCameraControl().setCameraRotation(cameraRotation);
            cameraImageSource.start();
        }
    }

    private void initCamera() {
        FaceDBManager.getInstance().init(this);
//        loadFaceDB();
        faceSDKManager = FaceSDKManager.getInstance();
        cameraImageSource = new CameraImageSource(this);
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280,
                720);
        cameraImageSource.setPreviewView(previewView);
        textureView.setOpaque(false);
        textureView.setKeepScreenOn(true);
        boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }
        cameraImageSource.getCameraControl().setCameraIndex(CameraControl.CAMERA_RGB);
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        previewView.setMirrored(false);
        frameAvailableListener = new OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(ImageFrame frame) {
                lastFrame = frame;
                processRunnable.run();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCamear();
    }

    private void startCamera() {
        if (cameraImageSource != null) {
            cameraImageSource.addOnFrameAvailableListener(
                    frameAvailableListener
            );
            cameraImageSource.getCameraControl().setCameraRotation(270);
            cameraImageSource.start();
        }
    }

    private void stopCamear() {
        if (cameraImageSource != null) {
            cameraImageSource.stop();
            cameraImageSource.removeOnFrameAvailableListener(
                    frameAvailableListener
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSDKManager.getInstance().release();
    }

    private void initFaceSDK() {
        executorPool.execute(new Runnable() {
            @Override
            public void run() {
                FaceSDKOptions options = new FaceSDKOptions();
                options.photoTrackerOptions.detectStrategy = FaceTracker.DetectStrategy.DETECT_BALANCE;
                FaceSDKManager.getInstance().init(
                        DemoActivity.this,
                        "443acae2-0668-4803-8872-db95c18151eb",
                        options
                );
                int status = FaceSDKManager.getInstance().getActivationStatus();
                if (status == 0) {
                    toast(StringUtil.CpStrPara(R.string.str_sdk_init_suc));
                    startCamera();
                } else if (status == -1 || status == -2) {
                    toast(StringUtil.CpStrPara(R.string.str_sdk_init_fail));
                } else if (status == -3) {
                    toast(StringUtil.CpStrPara(R.string.str_sdk_init_fail_model));
                }
            }
        });
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DemoActivity.this, tip, Toast.LENGTH_LONG).show();
            }
        });
    }

    // detect face
    private void checkTrackFace(Mat image) {
        if (cameraRotation == 90) {
            Core.transpose(image, image);
            Core.flip(image, image, 1);
        } else if (cameraRotation == 180) {
            Core.flip(image, image, 0);
            Core.flip(image, image, 1);
        } else if (cameraRotation == 270) {
            Core.transpose(image, image);
            Core.flip(image, image, 0);
        }

        FaceInfo[] faceInfos = faceSDKManager.getTrackingFaces(image,
                System.currentTimeMillis());
        drawFaceInfos(faceInfos);
        checkToMatchFaces(faceInfos, image);
    }

    private String filterFace(FaceInfo faceInfo, Mat image) {
        float[] orientation = faceInfo.orientation;
        float yaw = orientation[0];
        float pitch = orientation[1];
        float roll = orientation[2];
        if (Math.abs(yaw) > 30 || Math.abs(pitch) > 15 || Math.abs(roll) > 20) {
            return StringUtil.CpStrGet(R.string.str_check_face_error_range_too_large);
        }
        if (faceInfo.faceQuality < 0.5) {
            return StringUtil.CpStrGet(R.string.str_check_face_error_bad_quality);
        }

        // 这里就开始匹配人脸
        asyncIdentify(faceInfo, image);
        return StringUtil.CpStrGet(R.string.str_check_face_extract_feature);
    }

    private void checkToMatchFaces(FaceInfo[] faceInfos, Mat image) {
        if (faceInfos != null) {
            for (FaceInfo faceInfo : faceInfos) {
                filterFace(faceInfo, image);
            }
        }
    }

    private void asyncIdentify(final FaceInfo faceInfo, final Mat image) {
        // 如果正在匹配，则下一帧数据就不处理
        if (!isMatching) {
            executorPool.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            isMatching = true;
                            // 提取矫正后的人脸
                            Mat faceImage = new Mat();
                            faceSDKManager.alignFace(image, faceInfo,
                                    faceImage);
                            showAlignBitmap(
                                    ImageUtils.Mat2Bitmap(faceImage)
                            );

                            // 提取特征
                            Mat feature = new Mat();
                            faceSDKManager.extractFeature(faceImage, feature);

                            // 匹配搜索结果
//                            SearchResult searchResult =
//                                    FaceSDKManager.getInstance().searchHighestScoreFace(
//                                            feature
//                                    );
//                            if (searchResult.score > 80) {
//                                matchedID = faceInfo.trackingId;
//                                matchName = FaceDBApi.getInstance().getUser
//                                (groupId, featureIDs.get(searchResult
//                                .index)).getUserInfo();
//                            }
//                            if(searchResult.score  > 0) {
//                                showIdentifyResult(
//                                        searchResult.score ,
//                                        FaceDBApi.getInstance().getUser
//                                        (groupId, featureIDs.get
//                                        (searchResult.index)));
//                            }
                            isMatching = false;
                        }
                    }
            );
        }
    }

    private void showAlignBitmap(final Bitmap faceBitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgAlign.setImageBitmap(faceBitmap);
            }
        });
    }

    private void drawFaceInfos(FaceInfo[] faceInfos) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        // 如果没有检测到人脸
        if (faceInfos == null || faceInfos.length == 0) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    cameraLayout.setVisibility(View.VISIBLE);
                }
            });
            return;
        }

        // 检测到人脸
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraLayout.setVisibility(View.GONE);
            }
        });
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 检测到脸的信息,后续需要在这个地方做处理，比如添加检测到人脸的图.可以对图做缩放处理
        for (FaceInfo faceInfo : faceInfos) {
            int[] faceRect = faceInfo.rect;
            RectF rect = new RectF(faceRect[0], faceRect[1],
                    faceRect[2] + faceRect[0], faceRect[3] + faceRect[1]);
            previewView.mapFromOriginalRect(rect);
            Paint paint = new Paint();
            paint.setTextSize(30);
            if (faceInfo.trackingId == matchedID) {
                paint.setColor(Color.RED);
                paint.setStrokeWidth(3);
                canvas.drawText(matchName, rect.left, rect.top, paint);
            } else {
                paint.setStrokeWidth(3);
                paint.setColor(Color.YELLOW);
                canvas.drawText(faceInfo.trackingId + " " +
                                "confidence:" + faceInfo.confidence, rect.left,
                        rect.top, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        }

        textureView.unlockCanvasAndPost(canvas);
    }

    private void showIdentifyResult(final float score, final User user) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                maxScoreUserTextView.setVisibility(View.VISIBLE);
//                maxScoreUserTextView.setText("最高得分:"+user.getUserInfo()+" ,
//                比对得分"+String.valueOf(score));
//                if(score > 80 && user.getFeatureList().get(0).getImageUrl()
//                !=null && !user.getFeatureList().get(0).getImageUrl()
//                .isEmpty()) {
//                    File file = new File(user.getFeatureList().get(0)
//                    .getImageUrl());
//                    matchFaceImageView.setImageURI(Uri.fromFile(file));
//                    matchUserTextView.setText("匹配人脸:"+user.getUserInfo());
//                }
//            }
//        });
    }
}
