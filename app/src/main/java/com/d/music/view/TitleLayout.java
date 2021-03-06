package com.d.music.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.d.music.R;
import com.d.music.module.repeatclick.ClickUtil;
import com.d.music.view.dialog.MenuDialog;

/**
 * TitleLayout
 * Created by D on 2017/5/3.
 */
public class TitleLayout extends RelativeLayout implements View.OnClickListener {
    private Context context;
    private final String[] texts = new String[3];
    private final Drawable[] drawables = new Drawable[3];
    private final int[] ress = new int[3];
    private final int menuRes;
    private MenuDialog menu;
    private MenuDialog.OnMenuListener onMenuListener;

    public TitleLayout(Context context) {
        this(context, null);
    }

    public TitleLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleLayout);
        texts[0] = typedArray.getString(R.styleable.TitleLayout_tl_leftText);
        texts[1] = typedArray.getString(R.styleable.TitleLayout_tl_rightText);
        texts[2] = typedArray.getString(R.styleable.TitleLayout_tl_middleText);

        drawables[0] = typedArray.getDrawable(R.styleable.TitleLayout_tl_leftDrawable);
        drawables[1] = typedArray.getDrawable(R.styleable.TitleLayout_tl_rightDrawable);
        drawables[2] = typedArray.getDrawable(R.styleable.TitleLayout_tl_middleDrawable);

        ress[0] = typedArray.getResourceId(R.styleable.TitleLayout_tl_leftRes, -1);
        ress[1] = typedArray.getResourceId(R.styleable.TitleLayout_tl_rightRes, -1);
        ress[2] = typedArray.getResourceId(R.styleable.TitleLayout_tl_middleRes, -1);

        menuRes = typedArray.getResourceId(R.styleable.TitleLayout_tl_menu, -1);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View root = LayoutInflater.from(context).inflate(R.layout.layout_title, this);
        ImageView ivRight = (ImageView) root.findViewById(R.id.iv_title_right);
        ivRight.setOnClickListener(this);

        //left
        inflate(context, root, texts[0], drawables[0], ress[0],
                R.id.tv_title_left, R.id.iv_title_left, ALIGN_PARENT_LEFT);
        //right
        inflate(context, root, texts[1], drawables[1], ress[1],
                R.id.tv_title_right, R.id.iv_title_right, ALIGN_PARENT_RIGHT);
        //middle
        inflate(context, root, texts[2], drawables[2], ress[2],
                R.id.tv_title_title, R.id.iv_title_middle, CENTER_IN_PARENT);
    }

    private void inflate(Context context, View root, String text, Drawable drawable, int res,
                         int tv_id, int iv_id, int verb) {
        if (!TextUtils.isEmpty(text)) {
            TextView tv = (TextView) root.findViewById(tv_id);
            if (tv != null) {
                tv.setText(text);
                tv.setVisibility(VISIBLE);
            }
        } else if (drawable != null) {
            ImageView iv = (ImageView) root.findViewById(iv_id);
            if (iv != null) {
                iv.setImageDrawable(drawable);
                iv.setVisibility(VISIBLE);
            }
        } else if (res != -1) {
            View view = LayoutInflater.from(context).inflate(res, this, false);
            addView(view);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.addRule(verb);
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(lp);
        }
    }

    public void setVisibility(int resId, int visibility) {
        View v = findViewById(resId);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    public void setText(int resId, CharSequence text) {
        View v = findViewById(resId);
        if (v != null && v instanceof TextView) {
            ((TextView) v).setText(text);
        }
    }

    public void setOnClickListener(int resId, final OnClickListener l) {
        View v = findViewById(resId);
        if (v != null) {
            v.setOnClickListener(l);
        }
    }

    public void showMenu() {
        if (menu == null) {
            menu = new MenuDialog(context, menuRes);
            menu.setOnMenuListener(onMenuListener);
        }
        menu.show();
    }

    public void dismissMenu() {
        if (menu != null) {
            menu.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_title_right:
                showMenu();
                break;
        }
    }

    public void setOnMenuListener(MenuDialog.OnMenuListener listener) {
        onMenuListener = listener;
        if (menu != null) {
            menu.setOnMenuListener(onMenuListener);
        }
    }
}
