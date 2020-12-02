package com.brc.acctrl.activity;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.view.CommonEditView;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layout_name)
    CommonEditView layoutName;
    @BindView(R.id.layout_remark)
    CommonEditView layoutRemark;
    @BindView(R.id.layout_mac)
    CommonEditView layoutMac;

    private final static String SP_ABOUT_TITLE = "SP_ABOUT_TITLE";
    private final static String SP_ABOUT_REMARK = "SP_ABOUT_REMARK";

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initViews() {
        layoutName.setContent(SPUtil.getInstance().getValue(SP_ABOUT_TITLE,
                ""));
        layoutRemark.setContent(SPUtil.getInstance().getValue(SP_ABOUT_REMARK
                , ""));
        layoutMac.setContent(NetworkUtil.ethernetMac());
        layoutMac.setEtDisable();
    }

    @OnClick({R.id.img_back, R.id.tv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_save:
                SPUtil.getInstance().setValue(SP_ABOUT_TITLE,
                        layoutName.getContent());
                SPUtil.getInstance().setValue(SP_ABOUT_REMARK,
                        layoutRemark.getContent());
                finish();
                break;
        }
    }
}
