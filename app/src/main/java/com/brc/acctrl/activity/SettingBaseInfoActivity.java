package com.brc.acctrl.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.CommonEditView;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingBaseInfoActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layout_title)
    CommonEditView layoutTitle;
    @BindView(R.id.layout_title1)
    CommonEditView layoutTitle1;
    @BindView(R.id.layout_title2)
    CommonEditView layoutTitle2;

    @Override
    public int getLayoutId() {
        return R.layout.activity_base_info;
    }

    @Override
    public void initViews() {
        layoutTitle.setContent(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_TITLE
                , ""));
        layoutTitle.setEndSelection();
        layoutTitle1.setContent(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC
                , ""));
        layoutTitle2.setContent(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC_MORE
                , ""));

        if (SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING) == SPUtil.ACCESS_TYPE_BOSS) {
            layoutTitle.setTitle(R.string.str_shuipai_info_input_title, R.string.str_shuipai_info_input_title_hint);
            layoutTitle1.setTitle(R.string.str_shuipai_info_input_title1, R.string.str_shuipai_info_input_title1_hint);
            layoutTitle2.setTitle(R.string.str_shuipai_info_input_title2, R.string.str_shuipai_info_input_title2_hint);
        } else {
            layoutTitle.setTitle(R.string.str_base_info_input_title, R.string.str_base_info_input_title_hint);
            layoutTitle1.setTitle(R.string.str_base_info_input_title1, R.string.str_base_info_input_title1_hint);
            layoutTitle2.setTitle(R.string.str_base_info_input_title2, R.string.str_base_info_input_title2_hint);
        }
    }

    @OnClick({R.id.img_back, R.id.tv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_save:
                String titleContent = layoutTitle.getContent();
                if (TextUtils.isEmpty(titleContent.trim())) {
                    CommonUtil.showToast(SettingBaseInfoActivity.this, StringUtil.CpStrStrPara(R.string.str_base_info_input_err, StringUtil.CpStrGet(R.string.str_base_info_input_title)));
                    return;
                }

                String title1Content = layoutTitle1.getContent().trim();
                String title2Content = layoutTitle2.getContent().trim();

                SPUtil.getInstance().setValue(SPUtil.SP_MACHINE_INFO_TITLE,
                        titleContent);
                SPUtil.getInstance().setValue(SPUtil.SP_MACHINE_INFO_DESC,
                        title1Content);
                SPUtil.getInstance().setValue(SPUtil.SP_MACHINE_INFO_DESC_MORE,
                        title2Content);

                SPUtil.getInstance().setValue(SPUtil.HAS_CONFIG_NAME, true);
                setResult(RESULT_OK);
                finish();
                break;
            default:
                break;
        }
    }
}
