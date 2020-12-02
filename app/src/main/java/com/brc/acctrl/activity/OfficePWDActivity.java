package com.brc.acctrl.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.PasswordCharSequenceStyle;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class OfficePWDActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_check_status)
    TextView tvCheckStatus;
    @BindView(R.id.et_input_pwd)
    EditText etInputPwd;
    @BindView(R.id.tv_enter)
    TextView tvEnter;

    private int errorCnt = 0;
    private int MAX_ERROR = 5;
    private AlertDialog errDlg = null;
    private TextView errMsgView;

    private final static long BACK_DALAY_TIME = 15000L;
    private Handler finishHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            finish();
            super.dispatchMessage(msg);
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_office_pwd;
    }

    @Override
    public void initViews() {
        errorCnt = 0;
        tvEnter.setEnabled(false);
        etInputPwd.setTransformationMethod(new PasswordCharSequenceStyle());
        tvEnter.setEnabled(false);
        etInputPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                resetBackTime();
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvEnter.setEnabled(s.toString().length() > 0);
            }
        });
    }

    private void resetBackTime() {
        finishHandler.removeCallbacksAndMessages(null);
        finishHandler.sendEmptyMessageDelayed(0, BACK_DALAY_TIME);
    }

    @OnClick({R.id.tv_enter, R.id.tv_chg_pwd, R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_enter:
                tvCheckStatus.setVisibility(View.INVISIBLE);
                String etPwd = etInputPwd.getText().toString();
                String spPwd = SPUtil.getInstance().getValue(SPUtil.PASSWORD_OFFICE,
                        Constants.ADMIN_PWD);
                if (Constants.ADMIN_PWD.equals(etPwd) || etPwd.equals(spPwd)) {
                    GoldenEyesUtils.setGoldenEyesAutoOpenCloseDoor();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    tvCheckStatus.setVisibility(View.VISIBLE);
                    errorCnt++;
                    if (errorCnt <= MAX_ERROR) {
                        showErrorDlg(errorCnt);
                    } else {
                        tvEnter.setEnabled(false);
                    }

                    resetBackTime();
                }
                break;
            case R.id.tv_chg_pwd:
                Intent chgIntent = new Intent(OfficePWDActivity.this, ChgPwdActivity.class);
                chgIntent.putExtra(ChgPwdActivity.CHANGE_TYPE, ChgPwdActivity.OFFICE_TYPE);
                startActivity(chgIntent);
                break;
            default:
                break;
        }
    }

    private void showErrorDlg(int errCnt) {
        if (errDlg == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
            builder.setTitle(null);

            /**
             * 设置内容区域为自定义View
             */
            LinearLayout pwdErrDialog =
                    (LinearLayout) getLayoutInflater().inflate(R.layout.dlg_office_pwd_err, null);
            builder.setView(pwdErrDialog);

            errMsgView = pwdErrDialog.findViewById(R.id.tv_error_msg);
            pwdErrDialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errDlg.dismiss();
                }
            });

            builder.setCancelable(true);
            errDlg = builder.create();
        }

        if (errCnt == MAX_ERROR) {
            errMsgView.setText(R.string.str_office_door_pwd_error);
        } else {
            String errMsg =
                    StringUtil.CpStrInt2Para(R.string.str_office_door_pwd_error_cnt, errCnt, MAX_ERROR - errCnt);
            errMsgView.setText(errMsg);
        }
        errDlg.show();

        resetBackTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetBackTime();
    }
}
