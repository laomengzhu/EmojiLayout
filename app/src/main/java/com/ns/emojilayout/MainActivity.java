package com.ns.emojilayout;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MainActivity extends AppCompatActivity implements View.OnLayoutChangeListener, View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private EmojiconEditText etContent;
    private View vEmojiContainer, vEmojiBar;
    private ImageView ivEmojiSwitch;

    /**
     * 键盘高度
     */
    private int softKeyboardHeight = 0;
    /**
     * emoji视图是否显示
     */
    private boolean emojiContainerShown = false;
    /**
     * 软键盘关闭时是否显示emoji视图
     */
    private boolean showEmojiView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(this, R.layout.activity_main, null);
        setContentView(contentView);
        contentView.addOnLayoutChangeListener(this);

        etContent = (EmojiconEditText) findViewById(R.id.et_content);
        vEmojiBar = findViewById(R.id.ll_emoji_bar);
        vEmojiContainer = findViewById(R.id.fl_emoji_container);

        ivEmojiSwitch = (ImageView) findViewById(R.id.iv_emojicon);
        ivEmojiSwitch.setOnClickListener(this);

        //添加Emoji控件
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_emoji_container, new EmojiconsFragment()).commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_emojicon: {
                if (emojiContainerShown) {
                    ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
                    hideEmojiLayout(false);
                    showSoftInput(this, etContent);
                } else {
                    ivEmojiSwitch.setImageResource(R.drawable.ic_keyboard);
                    showEmojiView = true;
                    hideSoftInput(this, etContent);
                }
                break;
            }
        }
    }

    /**
     * 隐藏Emoji视图
     *
     * @param hideEmojiBar 是否隐藏Emoji按钮
     */
    private void hideEmojiLayout(boolean hideEmojiBar) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vEmojiBar.getLayoutParams();
        lp.bottomMargin = 0;
        vEmojiBar.setLayoutParams(lp);
        vEmojiContainer.setVisibility(View.GONE);

        if (hideEmojiBar) {
            ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
            vEmojiBar.setVisibility(View.GONE);
        }
        emojiContainerShown = false;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        int keyHeight = getResources().getDisplayMetrics().widthPixels / 3;
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            //监听到软键盘弹起
            if (softKeyboardHeight == 0) {
                softKeyboardHeight = oldBottom - bottom;
            }
            hideEmojiLayout(false);
            ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
            vEmojiBar.setVisibility(View.VISIBLE);

        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            //监听到软件盘关闭

            if (showEmojiView) {
                //显示Emoji视图
                showEmojiView = false;

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vEmojiBar.getLayoutParams();
                lp.bottomMargin = softKeyboardHeight;
                vEmojiBar.setLayoutParams(lp);

                lp = (RelativeLayout.LayoutParams) vEmojiContainer.getLayoutParams();
                lp.height = softKeyboardHeight;
                vEmojiContainer.setVisibility(View.VISIBLE);

                emojiContainerShown = true;
            } else {
                ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
                vEmojiBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示软键盘
     */
    private void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(etContent);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(etContent, emojicon);
    }

    @Override
    public void onBackPressed() {
        if (emojiContainerShown) {
            hideEmojiLayout(true);
        } else {
            super.onBackPressed();
        }
    }
}
