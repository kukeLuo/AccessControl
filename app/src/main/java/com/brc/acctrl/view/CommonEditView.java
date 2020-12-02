package com.brc.acctrl.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.listener.IServerContentChgListener;
import com.brc.acctrl.utils.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonEditView extends LinearLayout {
    private TextView tvTitle;
    private EditText etContent;
    private Context mCtx;
    private IServerContentChgListener contentChgListener;

    private int maxLen = 0;

    public CommonEditView(Context context) {
        this(context, null);
    }

    public CommonEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCtx = context;

        LayoutInflater.from(context).inflate(R.layout.layout_title_edit, this
                , true);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        etContent = (EditText) findViewById(R.id.et_content);

        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.CommonEditItem);
        String strTitle =
                typeArray.getString(R.styleable.CommonEditItem_tagTitle);
        if (strTitle != null) {
            tvTitle.setText(strTitle);
        }

        String strHint =
                typeArray.getString(R.styleable.CommonEditItem_tagHint);
        if (strHint != null) {
            etContent.setHint(strHint);
        }

        maxLen =
                typeArray.getInt(R.styleable.CommonEditItem_tagLength, 0);
        if (maxLen > 0) {
            etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
            etContent.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        typeArray.recycle();

    }

    public void setContent(String content) {
        etContent.setText(content);
    }

    public void setEndSelection() {
        String content = getContent();
        if (!TextUtils.isEmpty(content)) {
            etContent.setSelection(content.length());
        }
    }

    public void setTitle(int titleRes, int hintRes) {
        tvTitle.setText(titleRes);
        etContent.setHint(hintRes);
    }

    public String getContent() {
        return etContent.getText().toString();
    }

    public void setEditInputNumberType() {
        etContent.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void setEditInputPasswordType() {
        etContent.setKeyListener(new DigitsKeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }

            @Override
            protected char[] getAcceptedChars() {
                char[] data =
                        StringUtil.CpStrGet(R.string.digitsAndAlphet).toCharArray();
                return data;
            }
        });
    }

    public void setEditInputServerAddr() {
        etContent.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence charSequence, int i,
                                               int i1, int i2) {

                 }

                 @Override
                 public void onTextChanged(CharSequence charSequence, int i,
                                           int i1
                         , int i2) {
                     String editable =
                             etContent.getText().toString();
                     String regEx = "[^a-zA-Z0-9/\\." +
                             "]";  //只能输入字母或数字
                     Pattern p =
                             Pattern.compile(regEx);
                     Matcher m =
                             p.matcher(editable);
                     String str = m.replaceAll(
                             "").trim();
                     //删掉不是字母或数字的字符
                     if (!editable.equals(str)) {
                         etContent.setText(str);
                         if (str.length() > 0) {
                             etContent.setSelection(str.length());
                         }
                     }
                 }

                 @Override
                 public void afterTextChanged(Editable s) {
                    if (contentChgListener != null) {
                        contentChgListener.realtimeRefreshText();
                    }
                 }
             }
        );
    }

    public void setEditTextCommonWatcher() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (contentChgListener != null) {
                    contentChgListener.realtimeRefreshText();
                }
            }
        });
    }

    public void setEtDisable() {
        etContent.setEnabled(false);
        etContent.setFocusable(false);
        etContent.setKeyListener(null);
        etContent.addTextChangedListener(null);
        etContent.setOnFocusChangeListener(null);
    }

    public void setContentChgListener(IServerContentChgListener textChgListener) {
        contentChgListener = textChgListener;
    }

}
