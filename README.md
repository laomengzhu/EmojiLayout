#效果预览

![效果预览](https://raw.githubusercontent.com/xiaolifan/EmojiLayout/master/art/device-2015-12-01-172731.gif)

#说明

本项目中使用了https://github.com/rockerhieu/emojicon作为Emoji显示和编辑控件

#原理

通过布局高度变化监控键盘弹出，键盘弹出时显示Emoji按钮，键盘收起时隐藏Emoji按钮；

#实现

##使键盘弹出时，布局高度变化，显示或者隐藏Emoji按钮

参照这篇博客：http://blog.csdn.net/bear_huangzhen/article/details/45896333

要想让键盘弹出时，布局高度发生变化，就得将Activity的android:windowSoftInputMode属性设置为adjustResize，这样键盘弹出时总是调整屏幕的大小以便留出软键盘的空间，这样布局高度就会发生变化。
``` xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="stateAlwaysHidden|adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
``` 

##监控布局高度变化

参照这篇博客：http://blog.csdn.net/bear_huangzhen/article/details/45896333

Activity或者Fragment实现View.OnLayoutChangeListener接口，给根布局设置布局变化监听器：
``` java
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
    //给根部局设置布局变化监听器
    contentView.addOnLayoutChangeListener(this);

    etContent = (EmojiconEditText) findViewById(R.id.et_content);
    vEmojiBar = findViewById(R.id.ll_emoji_bar);
    vEmojiContainer = findViewById(R.id.fl_emoji_container);

    ivEmojiSwitch = (ImageView) findViewById(R.id.iv_emojicon);
    ivEmojiSwitch.setOnClickListener(this);

    //添加Emoji控件
    getSupportFragmentManager().beginTransaction().replace(R.id.fl_emoji_container, new EmojiconsFragment()).commitAllowingStateLoss();
}
```

键盘弹出时显示Emoji按钮，键盘收起时隐藏Emoji按钮：
```java
@Override
public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        int keyHeight = getResources().getDisplayMetrics().widthPixels / 3;
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            //监听到软键盘弹起
            if (softKeyboardHeight == 0) {
                softKeyboardHeight = oldBottom - bottom;
            }
            //隐藏Emoji视图
            hideEmojiLayout(false);
            ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
            vEmojiBar.setVisibility(View.VISIBLE);

        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            //监听到软件盘关闭

            if (showEmojiView) {
                //如果是通过点击Emoji按钮使键盘隐藏的则显示Emoji视图
                showEmojiView = false;

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)   vEmojiBar.getLayoutParams();
                lp.bottomMargin = softKeyboardHeight;
                vEmojiBar.setLayoutParams(lp);

                lp = (RelativeLayout.LayoutParams) vEmojiContainer.getLayoutParams();
                lp.height = softKeyboardHeight;
                vEmojiContainer.setVisibility(View.VISIBLE);

                emojiContainerShown = true;
            } else {
                //其他方式使键盘收起，则隐藏Emoji按钮
                ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
                vEmojiBar.setVisibility(View.GONE);
            }
        }
    }
```

##处理Emoji按钮的点击

```java
@Override
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.iv_emojicon: {
            if (emojiContainerShown) {
                //如果Emoji视图已经显示，再次点击Emoji按钮时显示键盘
                ivEmojiSwitch.setImageResource(R.drawable.ic_emoji);
                hideEmojiLayout(false);
                showSoftInput(this, etContent);
            } else {
                //否则显示Emoji视图
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
```
