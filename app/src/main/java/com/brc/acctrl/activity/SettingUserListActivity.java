package com.brc.acctrl.activity;

import android.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessUser;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.glide.GlideLoadUtil;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.FloatingBarItemDecoration;
import com.brc.acctrl.view.IndexBar;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingUserListActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView iconBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layout_users)
    RecyclerView layoutUsers;
    @BindView(R.id.et_search_name)
    EditText etSearchName;
    @BindView(R.id.tv_cnt_total)
    TextView tvCntTotal;
    @BindView(R.id.share_add_contact_sidebar)
    IndexBar shareAddContaceSidebar;

    private ArrayList<AccessUser> mUser = new ArrayList<>();
    private ArrayList<AccessUser> mFilterUser = new ArrayList<>();
    private LinkedHashMap<Integer, String> mHeaderList = new LinkedHashMap<>();
    private CommonAdapter<AccessUser> mAdapter;
    private LinearLayoutManager layoutManager;

    private AlertDialog errDlg = null;
    private TextView errMsgView;

    private String searchStr = "";

    @Override
    public int getLayoutId() {
        return R.layout.activity_user_list;
    }

    @Override
    public void initViews() {
        layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        layoutUsers.setLayoutManager(layoutManager);
        mAdapter = new CommonAdapter<AccessUser>(this,
                R.layout.item_access_user, mFilterUser) {
            @Override
            protected void convert(ViewHolder holder, AccessUser accessUser,
                                   int position) {
                holder.setText(R.id.tv_name, accessUser.getUsername());
                GlideLoadUtil.getInstance().loadCircle(SettingUserListActivity.this,
                        accessUser.getAvatarUrl(),
                        holder.getView(R.id.img_avatar));
                if (position < mFilterUser.size() - 1) {
                    holder.getView(R.id.view_btm).setVisibility(mFilterUser.get(position).
                            getNamCapLetters().charAt(0) != mFilterUser.get(position + 1).
                            getNamCapLetters().charAt(0) ? View.VISIBLE : View.GONE);
                } else {
                    holder.getView(R.id.view_btm).setVisibility(View.VISIBLE);
                }

                holder.getView(R.id.layout_data).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CommonUtil.hideIMM(SettingUserListActivity.this);
                        tryToDelUser(accessUser);
                    }
                });
            }
        };
        layoutUsers.setAdapter(mAdapter);

        layoutUsers.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        layoutUsers.addItemDecoration(
                new FloatingBarItemDecoration(this, mHeaderList));

        shareAddContaceSidebar.setNavigators(new ArrayList<>(mHeaderList.values()));
        shareAddContaceSidebar.setOnTouchingLetterChangedListener(new IndexBar.OnTouchingLetterChangeListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                for (Integer position : mHeaderList.keySet()) {
                    if (mHeaderList.get(position).equals(s)) {
                        layoutManager.scrollToPositionWithOffset(position, 0);
                        return;
                    }
                }
                CommonUtil.hideIMM(SettingUserListActivity.this);
            }

            @Override
            public void onTouchingStart(String s) {

            }

            @Override
            public void onTouchingEnd(String s) {

            }
        });

//        etSearchName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_SEARCH) {
//                    filterSearch();
//                    return true;
//                }
//                return false;
//            }
//        });

        etSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchStr = editable.toString().trim();
                filterSearch();
            }
        });
    }

    private void tryToDelUser(AccessUser delUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setTitle(null);

        /**
         * 设置内容区域为自定义View
         */
        LinearLayout pwdErrDialog =
                (LinearLayout) getLayoutInflater().inflate(R.layout.dlg_del_user, null);
        builder.setView(pwdErrDialog);

        errMsgView = pwdErrDialog.findViewById(R.id.tv_error_msg);
        errMsgView.setText(StringUtil.CpStrStrPara(R.string.str_try_to_del_user, delUser.getUsername()));
        pwdErrDialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (AccessUser accessUser : mUser) {
                    if (delUser.getUserId().equals(accessUser.getUserId())) {
                        mUser.remove(accessUser);
                        break;
                    }
                }

                for (AccessUser accessUser : mFilterUser) {
                    if (delUser.getUserId().equals(accessUser.getUserId())) {
                        mFilterUser.remove(accessUser);
                        break;
                    }
                }

                DBStore.getInstance().deleteAccessUser(delUser.getUserId());
                errDlg.dismiss();
            }
        });

        pwdErrDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errDlg.dismiss();
            }
        });

        builder.setCancelable(true);
        errDlg = builder.create();
        errDlg.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchUserDB();
    }

    private void filterSearch() {
        mFilterUser.clear();
        if (TextUtils.isEmpty(searchStr)) {
            mFilterUser.addAll(mUser);
            prepareShowData();
            return;
        }

        String cmpStr = searchStr.toLowerCase();
        for (AccessUser singleUser : mUser) {
            if (singleUser.getUsername().contains(cmpStr) ||
                    singleUser.getNamePinyin().contains(cmpStr) ||
                    singleUser.getNamCapLetters().contains(cmpStr)) {
                mFilterUser.add(singleUser);
            }
        }

        prepareShowData();
    }

    private void prepareShowData() {
        if (mFilterUser.size() > 0) {
            Collections.sort(mFilterUser, new Comparator<AccessUser>() {
                @Override
                public int compare(AccessUser l, AccessUser r) {
                    return l.compareTo(r);
                }
            });
        }
        processHeaderList();
        shareAddContaceSidebar.setNavigators(new ArrayList<>(mHeaderList.values()));
        mAdapter.notifyDataSetChanged();
        tvCntTotal.setText(StringUtil.CpStrIntPara(R.string.str_filter_cnt, mFilterUser.size()));
    }

    @OnClick({R.id.img_back, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_save:
                saveOneRecord("SAVE");
                break;
            default:
                break;
        }
    }

    private int addIdx = 0;

    private void saveOneRecord(String name) {
        AccessUser user = new AccessUser();
        user.setUsername(name);
        user.setUserId(addIdx + "");
        user.setMeetingId("0");
        user.setPermissionId(addIdx + "");
        addIdx++;
        user.setAvatarUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1559649578912&di=97b9165714bdd10c10fe002424553187&imgtype=0&src=http%3A%2F%2F02imgmini.eastday.com%2Fmobile%2F20180930%2F20180930112251_a18f14fe5b76203cf9622141ffb20261_1.jpeg");

        DBStore.getInstance().insertAccessUser(user);

//        refreshHandler.sendEmptyMessageDelayed(0, 2000L);
    }

    private void fetchUserDB() {
//        String meetingId = MeetingUtils.getInstance().fetchCurrentMeetingId();
//        Single<List<AccessUser>> groupUsers = UserDatabase.getInstance().getAccessUserDao().
//                fetchMeetingUsersByMeetingId(meetingId);

        Single<List<AccessUser>> groupUsers = UserDatabase.getInstance().getAccessUserDao()
                .fetchUsersFromDB();
        groupUsers.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<AccessUser>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessUser> accessUsers) {
                        mUser.clear();
                        mFilterUser.clear();
                        mUser.addAll(accessUsers);
                        mFilterUser.addAll(accessUsers);
                        prepareShowData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void processHeaderList() {
        mHeaderList.clear();
        if (mFilterUser.size() == 0) {
            return;
        }
        addHeaderToList(0, mFilterUser.get(0).getNamCapLetters());
        for (int i = 1; i < mFilterUser.size(); i++) {
            if (mFilterUser.get(i - 1).getNamCapLetters().charAt(0) !=
                    mFilterUser.get(i).getNamCapLetters().charAt(0)) {
                addHeaderToList(i, mFilterUser.get(i).getNamCapLetters());
            }
        }
    }

    private void addHeaderToList(int index, String namePinyin) {
        mHeaderList.put(index, namePinyin.charAt(0) + "");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvRefreshFaceEvent(RefreshEvents.RefreshFaceEvent event) {
        prepareShowData();
    }
}
