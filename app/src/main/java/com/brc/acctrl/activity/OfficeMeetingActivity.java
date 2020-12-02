package com.brc.acctrl.activity;

import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.DateTimeUtil;
import com.brc.acctrl.utils.MeetingUtils;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.MeetingInfoView;
import com.brc.acctrl.view.TexturePreviewView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class OfficeMeetingActivity extends BaseRGBCameraActivity {
    @BindView(R.id.preview_view)
    TexturePreviewView previewView;
    @BindView(R.id.texture_view)
    TextureView textureView;
    @BindView(R.id.videoview)
    VideoView videoView;
    @BindView(R.id.tv_scan_suc)
    TextView tvScanSuc;
    @BindView(R.id.layout_scan_suc)
    RelativeLayout layoutScanSuc;
    @BindView(R.id.layout_scan_err)
    RelativeLayout layoutScanErr;
    @BindView(R.id.tv_enter_suc_type)
    TextView tvEnterSucType;
    @BindView(R.id.layout_pwd_card_suc)
    LinearLayout layoutPwdCardSuc;
    @BindView(R.id.layout_camera)
    RelativeLayout layoutCamera;
    @BindView(R.id.tv_err)
    TextView tvErr;
    @BindView(R.id.tv_datetime)
    TextView tvDatetime;
    @BindView(R.id.icon_brc)
    ImageView iconBrc;
    @BindView(R.id.tv_info_title)
    TextView tvInfoTitle;
    @BindView(R.id.tv_info_desc)
    TextView tvInfoDesc;
    @BindView(R.id.tv_apartment)
    TextView tvApartment;
    @BindView(R.id.tv_meeting_name)
    TextView tvMeetingName;
    @BindView(R.id.tv_meeting_creator)
    TextView tvMeetingCreator;
    @BindView(R.id.tv_meeting_time)
    TextView tvMeetingTime;
    @BindView(R.id.tv_meeting_status)
    TextView tvMeetingStatus;
    @BindView(R.id.layout_meeting_show)
    LinearLayout layoutMeetingShow;
    @BindView(R.id.tv_available_status)
    TextView tvAvailableStatus;
    @BindView(R.id.layout_meeting_no)
    LinearLayout layoutMeetingNo;
    @BindView(R.id.layout_meeting_error)
    LinearLayout layoutMeetingError;
    @BindView(R.id.layout_room_status)
    FrameLayout layoutRoomStatus;
    @BindView(R.id.layout_meeting_1)
    MeetingInfoView layoutMeeting1;
    @BindView(R.id.layout_meeting_2)
    MeetingInfoView layoutMeeting2;
    @BindView(R.id.tv_click_pwd)
    TextView tvClickPwd;
    @BindView(R.id.tv_info_descmore)
    TextView tvInfoDescmore;
    @BindView(R.id.img_setting)
    ImageView imgSetting;
    @BindView(R.id.iv_meeting_show_btm)
    ImageView ivMeetingShowBtm;
    @BindView(R.id.layout_scan_door_open)
    LinearLayout layoutScanDoorOpen;

    @Override
    public int getLayoutId() {
        return R.layout.activity_meeting;
    }

    @Override
    public void initViews() {
        videoView.setVisibility(View.GONE);
        initCameraPreviewLayout(previewView, textureView);
        initSDK();

        MeetingUtils.getInstance().refreshShowMeetings(true);
    }

    private void initMockMeetingData() {
        layoutMeeting1.setVisibility(View.VISIBLE);
        layoutMeeting1.setMeetingAuthor("发起者：Daniel Wu");
        layoutMeeting1.setMeetingDesc("智能研发部：智能研发会议");
        layoutMeeting1.setMeetingTime("14:00 - 15:30");

        layoutMeeting2.setVisibility(View.VISIBLE);
        layoutMeeting2.setMeetingAuthor("发起者：Fiona Wang");
        layoutMeeting2.setMeetingDesc("智能研发部：智能评审会议");
        layoutMeeting2.setMeetingTime("16:00 - 18:00");
        layoutMeeting2.setMeetingGroup("Test");
    }

    @Override
    public TextView fetchTimeText() {
        return tvDatetime;
    }

    @OnClick({R.id.img_setting, R.id.tv_click_pwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_setting:
                checkSettingAction();
                break;
            case R.id.tv_click_pwd:
                gotoEnterPWDActivity();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldShowDescMore();
    }

    @Override
    public int rawVideoResource() {
        return R.raw.office_loading;
    }

    @Override
    public View getInputSucView() {
        return layoutPwdCardSuc;
    }

    @Override
    public TextView getTextSucView() {
        return tvEnterSucType;
    }

    @Override
    public TextView getInfoDescMoreView() {
        return tvInfoDescmore;
    }

    @Override
    public TextView getInfoDescView() {
        return tvInfoDesc;
    }

    @Override
    public TextView getInfoTitleView() {
        return tvInfoTitle;
    }

    @Override
    public View getScanSucView() {
        return layoutScanSuc;
    }

    @Override
    public View getScanErrView() {
        return layoutScanErr;
    }

    @Override
    public TextView getTextScanSucView() {
        return tvScanSuc;
    }

    @Override
    public TextView getErrTextView() {
        return tvErr;
    }

    @Override
    public ImageView getImageView() {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvMeetingRefreshEvent(RefreshEvents.RefreshMeetingEvent event) {
        List<MeetingBean> showMeetings = event.getMeetings();

        layoutMeeting1.setVisibility(View.GONE);
        layoutMeeting2.setVisibility(View.GONE);

        layoutMeetingNo.setVisibility(View.GONE);
        layoutMeetingShow.setVisibility(View.GONE);
        if (showMeetings != null && showMeetings.size() > 0) {
            MeetingBean firstMeeting = showMeetings.get(0);
            CommonUtil.initMeetingLayoutData(layoutMeeting1, firstMeeting);
            if (System.currentTimeMillis() >= firstMeeting.getStartTime()) {
                layoutMeetingShow.setVisibility(View.VISIBLE);
                tvMeetingName.setText(firstMeeting.getTitle());
                tvMeetingCreator.setText(StringUtil.CpStrStrPara(R.string.str_meeting_author_title,
                        firstMeeting.getHostname()));
                tvMeetingTime.setText(StringUtil.CpStrStr2Para(R.string.str_meeting_time,
                        DateTimeUtil.formatMeetingTime(firstMeeting.getStartTime()),
                        DateTimeUtil.formatMeetingTime(firstMeeting.getEndTime())));
            } else {
                layoutMeetingNo.setVisibility(View.VISIBLE);
                tvAvailableStatus.setText(R.string.str_room_nomeeting);
            }

            if (showMeetings.size() > 1) {
                CommonUtil.initMeetingLayoutData(layoutMeeting2, showMeetings.get(1));
            }
        } else {
            layoutMeetingNo.setVisibility(View.VISIBLE);
            tvAvailableStatus.setText(R.string.str_room_available);
        }

//        refreshFaceByType(event.isForceRefresh());
    }

    @Override
    public void showVideoMask() {
        ivMeetingShowBtm.setVisibility(View.VISIBLE);
        layoutRoomStatus.setAlpha(1);
    }

    @Override
    public void hideVideoMask() {
        ivMeetingShowBtm.setVisibility(View.GONE);
        layoutRoomStatus.setAlpha(0);
    }

    @Override
    public void showAlwaysOpenDoorUI() {
        layoutScanDoorOpen.setVisibility(isAlwaysOpenDoor ? View.VISIBLE : View.GONE);
    }
}
