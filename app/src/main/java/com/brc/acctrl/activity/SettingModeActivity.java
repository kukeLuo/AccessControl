package com.brc.acctrl.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.SPUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingModeActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.text_mode_1)
    TextView textMode1;
    @BindView(R.id.text_mode_2)
    TextView textMode2;
    @BindView(R.id.text_mode_3)
    TextView textMode3;
    @BindView(R.id.text_mode_4)
    TextView textMode4;

    private ArrayList<TextView> textViews = new ArrayList<>();
    private String[] arrayTitles;
    private int selectMode = 0;
    private int lastMode = 0;

    @Override
    public int getLayoutId() {
        return R.layout.activity_access_mode;
    }

    @Override
    public void initViews() {
        selectMode = lastMode =
                SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        arrayTitles = getResources().getStringArray(R.array.mode_machine);

        textViews.add(textMode1);
        textViews.add(textMode2);
        textViews.add(textMode3);
        textViews.add(textMode4);

        int textIdsLen = textViews.size();
        int titleLen = arrayTitles.length;
        int minLen = titleLen > textIdsLen ? textIdsLen : titleLen;
        int maxLen = titleLen > textIdsLen ? titleLen : textIdsLen;
        for (int idx = 0; idx < minLen; idx++) {
            TextView curText = textViews.get(idx);
            curText.setVisibility(View.VISIBLE);
            curText.setText(arrayTitles[idx]);
            curText.setOnClickListener(new ModeTextClick(idx));
            curText.setActivated(false);
        }
        for (int idx = minLen; idx < maxLen; idx++) {
            textViews.get(idx).setVisibility(View.GONE);
            textViews.get(idx).setActivated(false);
        }

        textViews.get(selectMode).setActivated(true);
    }

    private class ModeTextClick implements View.OnClickListener {
        private int curIdx = 0;

        private ModeTextClick(int idx) {
            curIdx = idx;
        }

        @Override
        public void onClick(View view) {
            if (selectMode != curIdx) {
                selectMode = curIdx;
                for (TextView textView : textViews) {
                    textView.setActivated(false);
                }

                view.setActivated(true);

                SPUtil.getInstance().setValue(SPUtil.SP_MACHINE_MODE, selectMode);
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
