package com.brc.acctrl.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.PasswordCharSequenceStyle;
import com.brc.acctrl.utils.SPUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingPWDActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.tv_enter)
    TextView tvEnter;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_err)
    TextView tvErr;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting_pwd;
    }

    @Override
    public void initViews() {
        etPwd.setTransformationMethod(new PasswordCharSequenceStyle());
        tvEnter.setEnabled(false);
        etPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                tvErr.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvEnter.setEnabled(s.toString().length() > 0);
            }
        });
    }

    @OnClick({R.id.tv_enter, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_enter:
                checkPwdCorrect();
                break;
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    private void checkPwdCorrect() {
        String inputPwd = etPwd.getEditableText().toString().trim();
        String oriPwd = SPUtil.getInstance().getValue(SPUtil.PASSWORD, Constants.ADMIN_PWD);
        if (Constants.ADMIN_PWD.equals(inputPwd) || oriPwd.equals(inputPwd)) {
            startActivity(new Intent(SettingPWDActivity.this, SettingFunctionActivity.class));
            finish();
        } else {
            tvErr.setVisibility(View.VISIBLE);
        }
    }
}
