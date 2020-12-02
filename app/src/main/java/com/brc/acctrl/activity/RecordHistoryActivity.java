package com.brc.acctrl.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.RecordDatabase;
import com.brc.acctrl.glide.GlideLoadUtil;
import com.brc.acctrl.utils.DateTimeUtil;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.LogUtil;
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

public class RecordHistoryActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView iconBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layout_history)
    RecyclerView layoutHistory;

    private ArrayList<AccessHistory> mRecords = new ArrayList<>();
    private CommonAdapter<AccessHistory> adapter;
    private int lastVisibleItem;
    private LinearLayoutManager layoutManager;
    private boolean isLoadingData = false;
    private int offsetData = 0;
    private boolean loadedAllData = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_record_history;
    }

    @Override
    public void initViews() {
        layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        layoutHistory.setLayoutManager(layoutManager);
        adapter = new CommonAdapter<AccessHistory>(this,
                R.layout.item_access_record, mRecords) {
            @Override
            protected void convert(ViewHolder holder,
                                   AccessHistory accessRecord,
                                   int position) {
                if (accessRecord.getType() == AccessHistory.TYPE_FACE) {
                    holder.getView(R.id.img_avatar).setVisibility(View.VISIBLE);
                    holder.getView(R.id.tv_name).setVisibility(View.VISIBLE);
                    holder.getView(R.id.tv_cardno).setVisibility(View.GONE);

                    holder.setText(R.id.tv_name, accessRecord.getUserName());
                    GlideLoadUtil.getInstance().loadFileActivity(RecordHistoryActivity.this,
                            userAvatarFile(accessRecord.getUserId()),
                            holder.getView(R.id.img_avatar));
                } else {
                    holder.getView(R.id.img_avatar).setVisibility(View.GONE);
                    holder.getView(R.id.tv_name).setVisibility(View.GONE);
                    holder.getView(R.id.tv_cardno).setVisibility(View.VISIBLE);

                    holder.setText(R.id.tv_cardno, accessRecord.getCardNo());
                }
                holder.setText(R.id.tv_time,
                        DateTimeUtil.accessTimeFormat(accessRecord.getAccessTime()));

            }
        };
        layoutHistory.setAdapter(adapter);
        layoutHistory.setHasFixedSize(true);

        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0xFF384557);
        layoutHistory.addItemDecoration(dividerLine);

        layoutHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
        fetchRecordDB();
    }

    private File userAvatarFile(String userId) {
        return new File(FaceSDKUtil.getInstance().getFaceFolderPath(), userId + ".jpg");
    }

    private void fetchRecordDB() {
        LogUtil.trackLogDebug("fetchRecordDB");
        if (isLoadingData || loadedAllData) {
            return;
        }
        isLoadingData = true;
        RecordDatabase.getInstance().getAccessHistoryDao().fetchAllRecordFromDB(offsetData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<AccessHistory>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessHistory> accessHistories) {
                        int oriCnt = mRecords.size();
                        LogUtil.d("OLD CNT = " + oriCnt);
                        mRecords.addAll(accessHistories);
                        int curPageSize = accessHistories.size();
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
                        e.printStackTrace();
                    }
                });
    }

    @OnClick({R.id.img_back, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_save:
                saveOneRecord();
                break;
            default:
                break;
        }
    }

    private boolean addFace = true;
    private int addIdx = 0;
    private Handler refreshHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            fetchRecordDB();
        }
    };

    private void saveOneRecord() {
        addFace = !addFace;
        AccessHistory history = new AccessHistory();
        history.setType(addFace ? AccessHistory.TYPE_FACE :
                AccessHistory.TYPE_CARD);
        if (addFace) {
            history.setUserName("FACE " + addIdx);
            history.setUserAvatar("https://timgsa.baidu" +
                    ".com/timg?image&quality=80&size=b9999_10000&sec" +
                    "=1559649578912&di=97b9165714bdd10c10fe002424553187" +
                    "&imgtype=0&src=http%3A%2F%2F02imgmini.eastday" +
                    ".com%2Fmobile%2F20180930" +
                    "%2F20180930112251_a18f14fe5b76203cf9622141ffb20261_1" +
                    ".jpeg");
        } else {
            history.setCardNo("CARD " + addIdx);
        }

        addIdx++;
        history.setAccessTime(System.currentTimeMillis());

        DBStore.getInstance().insertAccessRecord(history);

        refreshHandler.sendEmptyMessageDelayed(0, 2000L);
    }
}
