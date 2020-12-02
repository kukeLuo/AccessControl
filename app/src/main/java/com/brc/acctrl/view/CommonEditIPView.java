package com.brc.acctrl.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.listener.IPLayoutClickListener;
import com.brc.acctrl.utils.CommonUtil;

public class CommonEditIPView extends LinearLayout {
    private ImageView checkImage;
    private TextView tvTitle;
    private EditText etIp1, etIp2, etIp3, etIp4;
    private View viewBtmImg;
    private LinearLayout layoutIPAddr;
    private RelativeLayout layoutContent;
    private Context mCtx;
    private IPLayoutClickListener clickListener;

    public CommonEditIPView(Context context) {
        this(context, null);
    }

    public CommonEditIPView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonEditIPView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCtx = context;

        LayoutInflater.from(context).inflate(R.layout.layout_title_ip, this
                , true);

        checkImage = findViewById(R.id.img_check);
        tvTitle = findViewById(R.id.tv_title);
        etIp1 = findViewById(R.id.et_ip_1);
        etIp2 = findViewById(R.id.et_ip_2);
        etIp3 = findViewById(R.id.et_ip_3);
        etIp4 = findViewById(R.id.et_ip_4);
        viewBtmImg = findViewById(R.id.view_btm_img);
        layoutIPAddr = findViewById(R.id.layout_ip_addr);

        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.CommonIPItem);
        String strTitle =
                typeArray.getString(R.styleable.CommonIPItem_tagIPTitle);
        if (strTitle != null) {
            tvTitle.setText(strTitle);
        }

        boolean bShowCheck =
                typeArray.getBoolean(R.styleable.CommonIPItem_tagShowChecked,
                        false);
        checkImage.setVisibility(bShowCheck ? VISIBLE : INVISIBLE);
        viewBtmImg.setVisibility(bShowCheck ? VISIBLE : INVISIBLE);

        boolean bShowIP =
                typeArray.getBoolean(R.styleable.CommonIPItem_tagShowIP,
                        true);
        layoutIPAddr.setVisibility(bShowIP ? VISIBLE : INVISIBLE);

        typeArray.recycle();

        layoutContent = findViewById(R.id.layout_content);
        layoutContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null && !layoutContent.isActivated()) {
                    clickListener.onLayoutClickListener(CommonEditIPView.this);
                }
            }
        });
    }

    public void setActivate(boolean activate) {
        layoutContent.setActivated(activate);
        etIp1.setEnabled(activate);
        etIp2.setEnabled(activate);
        etIp3.setEnabled(activate);
        etIp4.setEnabled(activate);
    }

    public String getInputIP() {
        String ip1 = editStr(etIp1);
        if (TextUtils.isEmpty(ip1)) {
            return "";
        }

        String ip2 = editStr(etIp2);
        if (TextUtils.isEmpty(ip2)) {
            return "";
        }

        String ip3 = editStr(etIp3);
        if (TextUtils.isEmpty(ip3)) {
            return "";
        }

        String ip4 = editStr(etIp4);
        if (TextUtils.isEmpty(ip4)) {
            return "";
        }

        return ip1 + "." + ip2 + "." + ip3 + "." + ip4;
    }

    private String editStr(EditText et) {
        String etStr = et.getText().toString();
        if (etStr.length() == 0) {
            return "0";
        } else {
            int value = Integer.parseInt(etStr);
            if (value >= 256) {
                CommonUtil.showToast(mCtx, R.string.str_ip_addr_err);
                return "";
            } else {
                return value + "";
            }
        }
    }

    public void setClickListener(IPLayoutClickListener listener) {
        this.clickListener = listener;
    }

    public void setIPContent(String content) {
        String tempValue = content;
        if (TextUtils.isEmpty(tempValue)) {
            return;
        }
        String[] splitValue = tempValue.split("\\.");
        etIp1.setText(splitValue[0]);
        etIp2.setText(splitValue[1]);
        etIp3.setText(splitValue[2]);
        etIp4.setText(splitValue[3]);
    }

    public void setEnable(boolean enable) {
        if (!enable) {
            setEditDisable(etIp1);
            setEditDisable(etIp2);
            setEditDisable(etIp3);
            setEditDisable(etIp4);
        }
    }

    public void setEditDisable(EditText etView) {
        etView.setEnabled(false);
        etView.setFocusable(false);
        etView.setKeyListener(null);
        etView.addTextChangedListener(null);
    }
}
