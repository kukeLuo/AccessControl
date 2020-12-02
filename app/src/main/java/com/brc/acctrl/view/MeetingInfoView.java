package com.brc.acctrl.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.glide.GlideLoadUtil;

public class MeetingInfoView extends RelativeLayout {
    private TextView tvMeetingAuthor, tvMeetingTime, tvMeetingDesc, tvMeetingGroup;
    private ImageView imgAuthor;
    private Context mCtx;

    public MeetingInfoView(Context context) {
        this(context, null);
    }

    public MeetingInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeetingInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCtx = context;

        LayoutInflater.from(context).inflate(R.layout.layout_meeting_info, this, true);

        tvMeetingAuthor = (TextView) findViewById(R.id.tv_meeting_author);
        tvMeetingDesc = (TextView) findViewById(R.id.tv_meeting_desc);
        tvMeetingTime = (TextView) findViewById(R.id.tv_meeting_time);
        tvMeetingGroup = (TextView) findViewById(R.id.tv_meeting_group);
//        imgAuthor = (ImageView) findViewById(R.id.img_meeting_avatar);
    }

    public void setMeetingAuthor(String author) {
        tvMeetingAuthor.setText(author);
    }

    public void setMeetingTime(String meetingTime) {
        tvMeetingTime.setText(meetingTime);
    }

    public void setMeetingDesc(String meetingDesc) {
        tvMeetingDesc.setText(meetingDesc);
    }

    public void setMeetingAuthorAvatar(String avatar) {
        GlideLoadUtil.getInstance().load(mCtx, avatar, imgAuthor);
    }

    public void setMeetingGroup(String content) {
        tvMeetingGroup.setText(content);
    }

    public void setMeetingAuthorRes(int avatarId) {
        imgAuthor.setImageResource(avatarId);
    }
}
