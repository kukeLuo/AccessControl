package com.brc.acctrl.activity;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessFail;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.bean.AccessLog;
import com.brc.acctrl.db.FailRecordDatabase;
import com.brc.acctrl.db.IdentificationLogDatabase;
import com.brc.acctrl.db.RecordDatabase;
import com.brc.acctrl.glide.GlideLoadUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.DateTimeUtil;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.GridItemDecorationUtil;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.DividerLine;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class IdentificationLogActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.layout_log)
    RecyclerView layoutLog;

    private GridLayoutManager manager;
    private CommonAdapter<AccessLog> adapter;
    private int lastVisibleItem;
    private ArrayList<AccessLog> mRecords = new ArrayList<>();
    private boolean isLoadingData = false;
    private int offsetData = 0;
    private boolean loadedAllData = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_identification_log;
    }

    @Override
    public void initViews() {

        manager = new GridLayoutManager(this, 2);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        layoutLog.setLayoutManager(manager);

        adapter = new CommonAdapter<AccessLog>(this,
                R.layout.item_identification_log, mRecords) {
            @Override
            protected void convert(ViewHolder holder,
                                   AccessLog accessRecord,
                                   int position) {
                if (accessRecord.getType() == AccessLog.TYPE_FACE) {

                    LogUtil.i("match:"+accessRecord.getMatchUserId()+",,,,,ErrJpg："+accessRecord.getErrJpgName());
                    GlideLoadUtil.getInstance().loadFileActivity(IdentificationLogActivity.this,
                            userAvatarFile(accessRecord.getMatchUserId()),
                            holder.getView(R.id.img_contrast));
                    GlideLoadUtil.getInstance().loadFileActivity(IdentificationLogActivity.this,
                            accessLogFile(accessRecord.getErrJpgName()),
                            holder.getView(R.id.img_grab));
                    LogUtil.i(userAvatarFile(accessRecord.getMatchUserId())+",,,"+accessLogFile(accessRecord.getErrJpgName()));
                    holder.setText(R.id.tv_sd, (int)accessRecord.getMatchRatio()+"%");
                    holder.setText(R.id.tv_time,DateTimeUtil.accessTimeFormat(accessRecord.getTime()));
                    LogUtil.i("ratrio1111="+accessRecord.getMatchRatio()+",detectFace="+Constants.THRESH_DETECT_FACE+",time="+
                            DateTimeUtil.accessTimeFormat(accessRecord.getTime()));
                    if(accessRecord.getComparison()==0){
                        holder.setBackgroundRes(R.id.img_background,R.drawable.img_iden_suc_bg);
                        holder.setBackgroundRes(R.id.img_status,R.drawable.img_identify_suc);
                        holder.setText(R.id.tv_name,accessRecord.getMatchUserName());
                        holder.setTextColorRes(R.id.tv_sd,R.color.color_00D47D);
                    }else {
                        holder.setBackgroundRes(R.id.img_background,R.drawable.img_iden_fail_bg);
                        holder.setBackgroundRes(R.id.img_status,R.drawable.img_identify_fail);
                        holder.setText(R.id.tv_name,getString(R.string.str_face_reg_err_door));
                        holder.setTextColorRes(R.id.tv_sd,R.color.color_E42929);
                    }
                } else {
                    holder.getView(R.id.img_avatar).setVisibility(View.GONE);
                    holder.getView(R.id.tv_name).setVisibility(View.GONE);

                }
            }
        };
        layoutLog.setAdapter(adapter);
        layoutLog.setHasFixedSize(true);
        GridItemDecorationUtil divider = new GridItemDecorationUtil.Builder(this)
                .setHorizontalSpan(R.dimen.common_vew_column_padding)
                .setVerticalSpan(R.dimen.common_vew_raw_padding)
                .setColorResource(R.color.color_FF384557)
                .setShowLastLine(true)
                .build();
        layoutLog.addItemDecoration(divider);


        layoutLog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 在newState为滑到底部时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItem + 1 == adapter.getItemCount()) {
                        fetchRecordDB();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 在滑动完成后，拿到最后一个可见的item的位置
                lastVisibleItem = manager.findLastVisibleItemPosition();
            }
        });
        fetchRecordDB();
    }

    private File userAvatarFile(String userId) {
        return new File(FaceSDKUtil.getInstance().getFaceFolderPath(), userId + ".jpg");
    }
    private File accessLogFile(String logName) {
        return new File(FaceSDKUtil.getInstance().getFaceFolderPath(), logName);
    }

    private void fetchRecordDB() {
        LogUtil.trackLogDebug("fetchRecordDB");
        if (isLoadingData || loadedAllData) {
            return;
        }
        isLoadingData = true;
        IdentificationLogDatabase.getInstance().getAccessLogDao().fetchAllAccessLogFromDB(offsetData)
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<AccessLog>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessLog> accessLogs) {
                        int oriCnt = mRecords.size();
                        mRecords.addAll(accessLogs);
                        int curPageSize = accessLogs.size();
                        adapter.notifyItemRangeInserted(oriCnt, curPageSize);
                        offsetData += curPageSize;
                        if (curPageSize < 20) {
                            loadedAllData = true;
                        }
                        isLoadingData = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        isLoadingData = false;
                        LogUtil.i("OLD CNT = FAIL"+e.getLocalizedMessage() );
                        e.printStackTrace();
                    }
                });


    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            default:
                break;
        }
    }

}
