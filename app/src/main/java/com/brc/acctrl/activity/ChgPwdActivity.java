package com.brc.acctrl.activity;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.listener.IServerContentChgListener;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.view.CommonEditView;

import butterknife.BindView;
import butterknife.OnClick;

public class ChgPwdActivity extends BaseActivity implements IServerContentChgListener {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_pwd_ori)
    CommonEditView layoutPwdOri;
    @BindView(R.id.layout_pwd_new)
    CommonEditView layoutPwdNew;
    @BindView(R.id.layout_pwd_new_confirm)
    CommonEditView layoutPwdNewConfirm;
    @BindView(R.id.tv_pwd_rule)
    TextView tvPwdRule;
    @BindView(R.id.tv_enter)
    TextView tvEnter;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_err)
    TextView tvErr;
    @BindView(R.id.layout_chg_suc)
    LinearLayout layoutChgSuc;

    public static final String CHANGE_TYPE = "CHANGE_TYPE";
    public static final int SETTING_TYPE = 0;
    public static final int OFFICE_TYPE = 1;
    private int chgType = 0;

    private String oldPwd;

    @Override
    public int getLayoutId() {
        return R.layout.activity_chg_pwd;
    }

    @Override
    public void initViews() {
        chgType = getIntent().getIntExtra(CHANGE_TYPE, SETTING_TYPE);
        oldPwd = SPUtil.getInstance().getValue(chgType == SETTING_TYPE ?
                SPUtil.PASSWORD : SPUtil.PASSWORD_OFFICE, Constants.ADMIN_PWD);

        layoutPwdOri.setEditInputPasswordType();
        layoutPwdNew.setEditInputPasswordType();
        layoutPwdNewConfirm.setEditInputPasswordType();

        layoutPwdOri.setEditTextCommonWatcher();
        layoutPwdOri.setContentChgListener(this);
        layoutPwdNew.setContentChgListener(this);
        layoutPwdNew.setEditTextCommonWatcher();
        layoutPwdNew.setEditInputNumberType();
        layoutPwdNewConfirm.setContentChgListener(this);
        layoutPwdNewConfirm.setEditTextCommonWatcher();
        layoutPwdNewConfirm.setEditInputNumberType();
    }

    @OnClick({R.id.img_back, R.id.tv_enter, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_enter:
                checkAndSavePwd();
                break;
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    private void checkAndSavePwd() {
        String oriEtPwd = layoutPwdOri.getContent();
        // 之所以使用 ADMIN_PWD 判别，是防止密码忘记，所以用管理员密码来处理
        if (!(oldPwd.equals(oriEtPwd) || Constants.ADMIN_PWD.equals(oriEtPwd))) {
            setErrText(R.string.str_pwd_input_ori_err);
            return;
        }
        String newEtPwd = layoutPwdNew.getContent();
        if (TextUtils.isEmpty(newEtPwd)) {
            setErrText(R.string.str_pwd_input_new);
            return;
        }
        String newConfirmPwd = layoutPwdNewConfirm.getContent();
        if (TextUtils.isEmpty(newEtPwd)) {
            setErrText(R.string.str_pwd_input_new_confirm);
            return;
        }

        if (!newEtPwd.equals(newConfirmPwd)) {
            setErrText(R.string.str_pwd_new_match_err);
            return;
        }

        if (!validatePwd(newEtPwd)) {
            setErrText(R.string.str_pwd_validate_error);
            return;
        }

        showChgSucView();
        missHandler.sendEmptyMessageDelayed(0, 4000L);
    }

    private void setErrText(int strId) {
        tvErr.setVisibility(View.VISIBLE);
        tvErr.setText(strId);
    }

    private boolean validatePwd(String pwd) {
        return pwd.length() >= 4;
//        if (pwd.length() < 4) {
//            return false;
//        }
//
//        boolean hasAlphabet = false, hasDigital = false;
//        for (int idx = 0; idx < pwd.length(); idx++) {
//            char curChar = pwd.charAt(idx);
//            if (curChar >= 48 && curChar <= 57) {
//                hasDigital = true;
//            } else if ((curChar >= 65 && curChar <= 90) || (curChar >= 97 && curChar <= 122)) {
//                hasAlphabet = true;
//            }
//        }
//
//        return hasDigital && hasAlphabet;
    }

    private Handler missHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            SPUtil.getInstance().setValue(chgType == SETTING_TYPE ?
                    SPUtil.PASSWORD : SPUtil.PASSWORD_OFFICE, layoutPwdNew.getContent());
            finish();
        }
    };

    private void showChgSucView() {
        ScaleAnimation showAnim = new ScaleAnimation(0.0f, 0.0f, 0.0f, 0.0f,
                0.5f, 0.5f);
        showAnim.setDuration(500);
        layoutChgSuc.startAnimation(showAnim);
        layoutChgSuc.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        missHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void realtimeRefreshText() {
        tvErr.setVisibility(View.GONE);
    }
}
