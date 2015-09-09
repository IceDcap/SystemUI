
package com.amigo.navi.keyguard.haokan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amigo.navi.keyguard.haokan.entity.Music;
import com.android.keyguard.R;

public class PlayerLayout extends RelativeLayout {

    public PlayerLayout(Context context) {
        this(context, null);
    }

    public PlayerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PlayerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    private TextView mTextViewMusicName;
    private TextView mTextViewArtist;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextViewMusicName = (TextView) findViewById(R.id.haokan_page_layout_music);
        mTextViewArtist = (TextView) findViewById(R.id.haokan_page_layout_Artist);

    }

    public void showMusicName(final boolean anim, final Music mCurrentMusic) {

        long delayMillis = anim ? 50 : 0;
        postDelayed(new Runnable() {

            @Override
            public void run() {

                final String musicName = mCurrentMusic.getmMusicName();
                final String musicArtist = mCurrentMusic.getmArtist();
                boolean isEmptyMusicName = TextUtils.isEmpty(musicName);
                boolean isEmptyMusicArtist = TextUtils.isEmpty(musicArtist);

                PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat(
                        "translationX", 100, 0f);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.1f, 1.0f);
                AnimatorSet animatorSet = new AnimatorSet();

                if (!isEmptyMusicName) {
                    mTextViewMusicName.setText(musicName);
                    mTextViewMusicName.setVisibility(View.VISIBLE);
                    ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewMusicName, pvhTranslationX, alpha).setDuration(300);
                    translationName.setInterpolator(new OvershootInterpolator(2.5f));
                    animatorSet.play(translationName);
                } else {
                    mTextViewMusicName.setVisibility(View.GONE);
                }

                if (!isEmptyMusicArtist) {
                    mTextViewArtist.setText(musicArtist);
                    mTextViewArtist.setVisibility(View.VISIBLE);
                    ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewArtist, pvhTranslationX, alpha).setDuration(300);
                    translationArtist.setInterpolator(new OvershootInterpolator(2.5f));
                    translationArtist.setStartDelay(70);
                    animatorSet.play(translationArtist);
                } else {
                    mTextViewArtist.setVisibility(View.GONE);
                }

                if (anim) {
                    if (!isEmptyMusicArtist || !isEmptyMusicName) {
                        animatorSet.start();
                    }
                } else {
                    mTextViewMusicName.setAlpha(1.0f);
                    mTextViewArtist.setAlpha(1.0f);
                }
            }
        }, delayMillis);

    }

    public void hideMusicName(boolean anim, final boolean hasMusic) {

        if (anim) {

            postDelayed(new Runnable() {

                @Override
                public void run() {

                    PropertyValuesHolder pvhTranslationX = PropertyValuesHolder.ofFloat(
                            "translationX",
                            0f, 100);

                    PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);

                    ObjectAnimator translationName = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewMusicName, pvhTranslationX, alpha).setDuration(300);
                    ObjectAnimator translationArtist = ObjectAnimator.ofPropertyValuesHolder(
                            mTextViewArtist, pvhTranslationX, alpha).setDuration(300);

                    translationArtist.setStartDelay(70);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(translationName).with(translationArtist);
                    animatorSet.addListener(new AnimatorListener() {

                        @Override
                        public void onAnimationStart(Animator arg0) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0) {

                        }

                        @Override
                        public void onAnimationEnd(Animator arg0) {
                            setPlayerLayoutVisibility(hasMusic);
                        }

                        @Override
                        public void onAnimationCancel(Animator arg0) {

                        }
                    });
                    animatorSet.start();

                }
            }, 50);

        } else {
            mTextViewMusicName.setAlpha(0f);
            mTextViewArtist.setAlpha(0f);
            setPlayerLayoutVisibility(hasMusic);
        }

    }
    
    
    
    public void setPlayerLayoutVisibility(boolean musicIsExist) {

        if (musicIsExist) {
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }
        } else {
            if (getVisibility() == View.VISIBLE) {
                setVisibility(View.GONE);
            }
        }
    }

}
