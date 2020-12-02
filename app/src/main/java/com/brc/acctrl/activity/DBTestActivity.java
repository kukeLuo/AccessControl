package com.brc.acctrl.activity;

import android.view.View;
import android.widget.Button;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.bean.MeetParticipant;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.MeetingDatabase;
import com.brc.acctrl.utils.AccessRecordUtil;
import com.brc.acctrl.utils.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DBTestActivity extends BaseActivity {
    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.btn_del)
    Button btnDel;
    @BindView(R.id.btn_update)
    Button btnUpdate;

    @Override
    public int getLayoutId() {
        return R.layout.activity_test_db;
    }

    @Override
    public void initViews() {
        Calendar dayStartCalendar = Calendar.getInstance();
        LogUtil.d("Calendar : " + dayStartCalendar.getTimeInMillis());
        LogUtil.d("System : " + System.currentTimeMillis());
        int hourMinute = dayStartCalendar.get(Calendar.MINUTE);
        int dayHour = dayStartCalendar.get(Calendar.HOUR_OF_DAY);
        int dayYear =
                dayStartCalendar.get(Calendar.DAY_OF_YEAR);
        dayStartCalendar.set(Calendar.SECOND, 0);
        dayStartCalendar.set(Calendar.MILLISECOND, 0);
        dayStartCalendar.set(Calendar.DAY_OF_YEAR,
                dayYear - AccessRecordUtil.MAX_DAY_ACCESS_RECORD_DURATION);
        LogUtil.d("Chg: " + dayStartCalendar.getTimeInMillis());
    }

    @OnClick({R.id.btn_add, R.id.btn_del, R.id.btn_update, R.id.btn_read})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
//                addNewAccess();
                addNewMeeting();
                break;
            case R.id.btn_del:
                delExpireAccess();
                break;
            case R.id.btn_update:
                updateAccess();
                break;
            case R.id.btn_read:
                loadOneMeeting();
                break;
        }
    }

    private int testIdx = 0;

    private void addNewAccess() {
        for (int i = 0; i < 400; i++) {
            AccessHistory record = new AccessHistory();
            record.setAccessTime(System.currentTimeMillis() - i);
            record.setUserName(testIdx + "");
            record.setType(0);
            testIdx++;

            DBStore.getInstance().insertAccessRecord(record);
        }

    }

    private void delExpireAccess() {
        DBStore.getInstance().deleteExpireRecord(System.currentTimeMillis() - 60000);
    }

    private void delAllAccess() {
        DBStore.getInstance().deleteAllRecords();
    }

    private void updateAccess() {
        AccessRecordUtil.checkUnuploadRecord(this);
    }

    private void addNewMeeting() {
        ArrayList<MeetingBean> meetings = new ArrayList<>();
        for (int idx = 0; idx < 3; idx++) {
            MeetingBean bean = new MeetingBean();
            bean.setMeetingId("123" + idx);
            bean.setMeetingRoom("ROOM-123");
            bean.setHostname("222");
            bean.setHosturl("222");
            bean.setTitle("MEETING " + idx);
            bean.setFaceIssuedTime(System.currentTimeMillis() - 100000);
            bean.setFaceEndTime(System.currentTimeMillis() + 100000);

            ArrayList<MeetParticipant> participants = new ArrayList<>();
            for (int jdx = 0; jdx < 2; jdx++) {
                MeetParticipant participant = new MeetParticipant();
                participant.setPersonId("per-" + jdx);
                participant.setPersonName("pername-" + jdx);
                participant.setPersonurl("perurl-" + jdx);

                participants.add(participant);
            }

            bean.setParticipants(participants);

            meetings.add(bean);
        }

        DBStore.getInstance().insertBatchMeetings(meetings);
    }

    private void loadOneMeeting() {
        MeetingDatabase.getInstance().getMeetingDao().loadSingleMeeting("1230")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<MeetingBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(MeetingBean singleMeeting) {
                        ArrayList<MeetParticipant> participants = singleMeeting.getParticipants();
                        if (participants != null) {
                            for (MeetParticipant participant : participants) {
                                LogUtil.e("participant " + participant.getPersonId());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }
}
