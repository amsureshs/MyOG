package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.ssgames.com.omiplus.R;


public class OmiGameView extends LinearLayout {

    private static final String TAG = OmiGameView.class.getSimpleName();

    public interface OmiGameViewListener {
        public void visibleButtonTapped();
        public void partnerSelected(String partnerName);
    }

    public OmiGameView(Context context) {
        super(context);
        init(null, 0);
    }

    public OmiGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OmiGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }
}
