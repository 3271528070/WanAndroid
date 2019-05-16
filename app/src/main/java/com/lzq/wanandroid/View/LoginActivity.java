package com.lzq.wanandroid.View;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.toast.ToastUtils;
import com.lzq.wanandroid.BaseActivity;
import com.lzq.wanandroid.Contract.LoginContract;
import com.lzq.wanandroid.Model.Data;
import com.lzq.wanandroid.Model.Event;
import com.lzq.wanandroid.Net.LoginTask;
import com.lzq.wanandroid.Presenter.LoginPresenter;
import com.lzq.wanandroid.R;
import com.lzq.wanandroid.View.Custom.ClearEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity implements LoginContract.LoginView {
    private static final String TAG = "LoginActivity";
    @BindView(R.id.cardView)
    CardView mCardView;
    @BindView(R.id.user_edt_id)
    ClearEditText mIDEdt;
    @BindView(R.id.user_edt_pwd)
    ClearEditText mPwdEdt;
    @BindView(R.id.user_edt_confirm)
    ClearEditText mConfirmEdt;
    @BindView(R.id.user_btn_login)
    Button mLoginBtn;
    @BindView(R.id.user_btn_register_now)
    Button mRegisterNowBtn;
    @BindView(R.id.user_ll_confirm)
    LinearLayout mConfirmView;
    @BindView(R.id.user_line_confirm)
    View mLineView;
    private ConstraintLayout.LayoutParams mLoginLayout = null;
    private LoginContract.LoginPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        doAnimation(0);
        ImmersionBar.with(this).statusBarColor(R.color.bg_daily_mode).autoDarkModeEnable(true).fitsSystemWindows(true).keyboardEnable(true).init();
        mLoginLayout = (ConstraintLayout.LayoutParams) mLoginBtn.getLayoutParams();
        if (mPresenter == null) {
            LoginTask mTask = LoginTask.getInstance();
            mPresenter = LoginPresenter.createPresenter(this, mTask);
        }
        ViewTreeObserver.OnGlobalLayoutListener mListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    mPresenter.resetLoginLocation(LoginActivity.this);
                } catch (Exception e) {
                }
            }
        };
        mIDEdt.getViewTreeObserver().addOnGlobalLayoutListener(mListener);
        TextView.OnEditorActionListener mTvListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode()) {
                    Login();
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    showSoftInputUtil(mPwdEdt);
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Login();
                    return true;
                }
                return false;
            }
        };
        mIDEdt.setOnEditorActionListener(mTvListener);
        mPwdEdt.setOnEditorActionListener(mTvListener);
    }

    @Override
    public void LoginSuccess(Data data) {
        ToastUtils.show("登录成功！");
        SPUtils.getInstance("userinfo", MODE_PRIVATE).put("isLogin", true);
        SPUtils.getInstance("userinfo", MODE_PRIVATE).put("username", data.getUsername());
        SPUtils.getInstance("userinfo", MODE_PRIVATE).put("id", String.valueOf(data.getId()));
        Event event = new Event();
        event.target = Event.TARGET_USER;
        event.type = Event.TYPE_LOGIN_SUCCESS;
        EventBus.getDefault().post(event);
        event.target = Event.TARGET_COLLECT;
        event.type = Event.TYPE_LOGIN_SUCCESS;
        EventBus.getDefault().post(event);
        finish();
        //不必传cookie，只需要传返回的结果，用Presenter层来实现
        //cookie在App启动的时候检测一下size和null，如果为0或者为null，就说明需要登录
    }

    @Override
    public void RegisterResult(String... infos) {

    }

    @Override
    public void setLoginLocation(int height) {
        mLoginLayout.bottomMargin = 16;//此处单位是dp
        mLoginBtn.setLayoutParams(mLoginLayout);
        mRegisterNowBtn.setVisibility(View.GONE);
    }

    @Override
    public void refreshLocation(int height) {
        mLoginLayout.bottomMargin = ConvertUtils.dp2px(100);
        mLoginBtn.setLayoutParams(mLoginLayout);
        mRegisterNowBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(LoginContract.LoginPresenter presenter) {
        mPresenter = presenter;
    }

    @OnClick(R.id.user_btn_register_now)
    public void Register() {
        if (mLoginBtn.getText().toString().equals("登录")) {
            mRegisterNowBtn.setText("已有账号，直接登录");
            mLoginBtn.setText("注册");
            doAnimation(2);
        } else {
            mRegisterNowBtn.setText("没有账号？注册一个");
            mLoginBtn.setText("登录");
            doAnimation(1);
        }
    }

    @OnClick(R.id.user_btn_login)
    public void Login() {
        if (!TextUtils.isEmpty(mIDEdt.getText().toString()) && !TextUtils.isEmpty(mPwdEdt.getText().toString())) {
            if (mLoginBtn.getText().toString().equals("登录")) {
                mPresenter.doLogin(mIDEdt.getText().toString(), mPwdEdt.getText().toString());
            }
            if (mLoginBtn.getText().toString().equals("注册")) {
                mPresenter.doRegister(mIDEdt.getText().toString(), mPwdEdt.getText().toString(), mConfirmEdt.getText().toString());
            }
        } else if (TextUtils.isEmpty(mPwdEdt.getText().toString()) && TextUtils.isEmpty(mIDEdt.getText().toString())) {
            ToastUtils.show("请输入用户名和密码");
            return;
        } else if (TextUtils.isEmpty(mIDEdt.getText().toString())) {
            ToastUtils.show("尚未输入用户名");
            return;
        } else {
            ToastUtils.show("尚未输入密码");
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (event.target == Event.TARGET_MAIN) {
            if (event.type == Event.TYPE_LOGIN_ANIMATION) {
                doAnimation(0);
            }
        }
    }

    public void doAnimation(int flag) {
        switch (flag) {
            case 0://显示
                try {
                    Float OldCardHeight = mCardView.getTranslationY();
                    Float OldLoginBtnHeight = mLoginBtn.getTranslationY();
                    Float OldRegisterBtnHeight = mRegisterNowBtn.getTranslationY();

                    ObjectAnimator alpha_CardView = ObjectAnimator.ofFloat(mCardView, "alpha", 0, 1);
                    ObjectAnimator alpha_RegisterNow = ObjectAnimator.ofFloat(mRegisterNowBtn, "alpha", 0, 1);
                    ObjectAnimator alpha_LoginBtn = ObjectAnimator.ofFloat(mLoginBtn, "alpha", 0, 1);


                    ObjectAnimator translationY_CardView = ObjectAnimator.ofFloat(mCardView, "translationY", mCardView.getTranslationY(), -45, OldCardHeight + 20, OldCardHeight);
                    ObjectAnimator translationY_LoginBtn = ObjectAnimator.ofFloat(mLoginBtn, "translationY", mLoginBtn.getTranslationY(), -45, OldCardHeight + 20, OldLoginBtnHeight);
                    ObjectAnimator translationY_RegisterNow = ObjectAnimator.ofFloat(mRegisterNowBtn, "translationY", mRegisterNowBtn.getTranslationY(), -45, OldCardHeight + 20, OldRegisterBtnHeight);

                    AnimatorSet alphaSet = new AnimatorSet();
                    AnimatorSet translationSet = new AnimatorSet();

                    alphaSet.playTogether(alpha_CardView, alpha_LoginBtn, alpha_RegisterNow);
                    alphaSet.setDuration(1000);

                    translationSet.playTogether(translationY_CardView, translationY_RegisterNow);
                    translationSet.setDuration(1000);

                    translationY_LoginBtn.setDuration(1000).setStartDelay(100);

                    //好戏上演
                    alphaSet.start();
                    translationSet.start();
                    translationY_LoginBtn.start();
                } catch (Exception e) {
                    Log.d(TAG, "----doAnimation: " + e.getMessage());
                }
                break;
            case 1://注册消失,alpha--1--0
                ObjectAnimator alpha_line_hide = ObjectAnimator.ofFloat(mLineView, "alpha", 1, 0);
                ObjectAnimator alpha_confirm_hide = ObjectAnimator.ofFloat(mConfirmView, "alpha", 1, 0);
                AnimatorSet alphaHide = new AnimatorSet();
                alphaHide.playTogether(alpha_line_hide, alpha_confirm_hide);
                alphaHide.setDuration(500).start();
                alphaHide.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLineView.setVisibility(View.GONE);
                        mConfirmView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                break;
            case 2://注册显示,alpha--0--1
                mLineView.setVisibility(View.VISIBLE);
                mConfirmView.setVisibility(View.VISIBLE);
                ObjectAnimator alpha_line_show = ObjectAnimator.ofFloat(mLineView, "alpha", 0, 1);
                ObjectAnimator alpha_confirm_show = ObjectAnimator.ofFloat(mConfirmView, "alpha", 0, 1);
                AnimatorSet alphaShow = new AnimatorSet();
                alphaShow.playTogether(alpha_line_show, alpha_confirm_show);
                alphaShow.setDuration(1000).start();
                break;
            default:
                break;
        }
    }
}
