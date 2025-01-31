package per.goweii.wanandroid.module.mine.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import per.goweii.anypermission.AnyPermission;
import per.goweii.anypermission.RequestInterceptor;
import per.goweii.anypermission.RequestListener;
import per.goweii.anypermission.RuntimeRequester;
import per.goweii.basic.core.base.BaseActivity;
import per.goweii.basic.ui.dialog.ListDialog;
import per.goweii.basic.ui.dialog.PermissionDialog;
import per.goweii.basic.ui.dialog.TipDialog;
import per.goweii.basic.ui.dialog.UpdateDialog;
import per.goweii.basic.utils.AppInfoUtils;
import per.goweii.basic.utils.ResUtils;
import per.goweii.basic.utils.ToastMaker;
import per.goweii.basic.utils.listener.SimpleCallback;
import per.goweii.rxhttp.request.base.BaseBean;
import per.goweii.wanandroid.R;
import per.goweii.wanandroid.event.LoginEvent;
import per.goweii.wanandroid.event.SettingChangeEvent;
import per.goweii.wanandroid.module.main.dialog.DownloadDialog;
import per.goweii.wanandroid.module.main.model.UpdateBean;
import per.goweii.wanandroid.module.mine.presenter.SettingPresenter;
import per.goweii.wanandroid.module.mine.view.SettingView;
import per.goweii.wanandroid.utils.RvAnimUtils;
import per.goweii.wanandroid.utils.SettingUtils;
import per.goweii.wanandroid.utils.UpdateUtils;
import per.goweii.wanandroid.utils.UserUtils;

/**
 * @author CuiZhen
 * @date 2019/5/17
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public class SettingActivity extends BaseActivity<SettingPresenter> implements SettingView {

    private static final int REQ_CODE_PERMISSION = 1;

    @BindView(R.id.sc_show_read_later)
    SwitchCompat sc_show_read_later;
    @BindView(R.id.sc_show_top)
    SwitchCompat sc_show_top;
    @BindView(R.id.sc_hide_about_me)
    SwitchCompat sc_hide_about_me;
    @BindView(R.id.sc_hide_open)
    SwitchCompat sc_hide_open;
    @BindView(R.id.sc_web_swipeback_edge)
    SwitchCompat sc_web_swipeback_edge;
    @BindView(R.id.tv_rv_anim)
    TextView tv_rv_anim;
    @BindView(R.id.tv_cache)
    TextView tv_cache;
    @BindView(R.id.tv_has_update)
    TextView tv_has_update;
    @BindView(R.id.tv_curr_version)
    TextView tv_curr_version;
    @BindView(R.id.ll_logout)
    LinearLayout ll_logout;

    private RuntimeRequester mRuntimeRequester;
    private UpdateUtils mUpdateUtils;
    private boolean mShowTop;
    private boolean mShowReadLater;
    private boolean mHideAboutMe;
    private boolean mHideOpen;
    private int mRvAnim;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Nullable
    @Override
    protected SettingPresenter initPresenter() {
        return new SettingPresenter();
    }

    @Override
    protected void initView() {
        tv_has_update.setText("");
        tv_curr_version.setText("当前版本" + AppInfoUtils.getVersionName());
        mShowTop = SettingUtils.getInstance().isShowTop();
        sc_show_top.setChecked(mShowTop);
        mShowReadLater = SettingUtils.getInstance().isShowReadLater();
        sc_show_read_later.setChecked(mShowReadLater);
        mHideAboutMe = SettingUtils.getInstance().isHideAboutMe();
        sc_hide_about_me.setChecked(mHideAboutMe);
        mHideOpen = SettingUtils.getInstance().isHideOpen();
        sc_hide_open.setChecked(mHideOpen);
        sc_web_swipeback_edge.setChecked(SettingUtils.getInstance().isWebSwipeBackEdge());
        mRvAnim = SettingUtils.getInstance().getRvAnim();
        tv_rv_anim.setText(RvAnimUtils.getName(mRvAnim));
        sc_show_top.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                SettingUtils.getInstance().setShowTop(isChecked);
            }
        });
        sc_show_read_later.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                SettingUtils.getInstance().setShowReadLater(isChecked);
            }
        });
        sc_hide_about_me.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                SettingUtils.getInstance().setHideAboutMe(isChecked);
            }
        });
        sc_hide_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                SettingUtils.getInstance().setHideOpen(isChecked);
            }
        });
        sc_web_swipeback_edge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                SettingUtils.getInstance().setWebSwipeBackEdge(isChecked);
            }
        });
        if (UserUtils.getInstance().isLogin()) {
            ll_logout.setVisibility(View.VISIBLE);
        } else {
            ll_logout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void loadData() {
        presenter.update(false);
        presenter.getCacheSize();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postSettingChangedEvent();
    }

    private void postSettingChangedEvent() {
        boolean showTopChanged = mShowTop != SettingUtils.getInstance().isShowTop();
        boolean showReadLaterChanged = mShowReadLater != SettingUtils.getInstance().isShowReadLater();
        boolean hideAboutMeChanged = mHideAboutMe != SettingUtils.getInstance().isHideAboutMe();
        boolean hideOpenChanged = mHideOpen != SettingUtils.getInstance().isHideOpen();
        boolean rvAnimChanged = mRvAnim != SettingUtils.getInstance().getRvAnim();
        if (showReadLaterChanged || showTopChanged || hideAboutMeChanged || hideOpenChanged || rvAnimChanged) {
            SettingChangeEvent event = new SettingChangeEvent();
            event.setShowTopChanged(showTopChanged);
            event.setShowReadLaterChanged(showReadLaterChanged);
            event.setHideAboutMeChanged(hideAboutMeChanged);
            event.setHideOpenChanged(hideOpenChanged);
            event.setRvAnimChanged(rvAnimChanged);
            event.post();
        }
    }

    @OnClick({
            R.id.ll_rv_anim, R.id.ll_update, R.id.ll_cache, R.id.ll_about, R.id.ll_logout
    })
    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onClick2(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_rv_anim:
                ListDialog.with(getContext())
                        .cancelable(true)
                        .title("列表动画")
                        .datas(RvAnimUtils.getName(RvAnimUtils.RvAnim.NONE),
                                RvAnimUtils.getName(RvAnimUtils.RvAnim.ALPHAIN),
                                RvAnimUtils.getName(RvAnimUtils.RvAnim.SCALEIN),
                                RvAnimUtils.getName(RvAnimUtils.RvAnim.SLIDEIN_BOTTOM),
                                RvAnimUtils.getName(RvAnimUtils.RvAnim.SLIDEIN_LEFT),
                                RvAnimUtils.getName(RvAnimUtils.RvAnim.SLIDEIN_RIGHT))
                        .currSelectPos(SettingUtils.getInstance().getRvAnim())
                        .listener(new ListDialog.OnItemSelectedListener() {
                            @Override
                            public void onSelect(String data, int pos) {
                                tv_rv_anim.setText(RvAnimUtils.getName(pos));
                                SettingUtils.getInstance().setRvAnim(pos);
                            }
                        })
                        .show();
                break;
            case R.id.ll_update:
                presenter.update(true);
                break;
            case R.id.ll_cache:
                TipDialog.with(getContext())
                        .message("确定要清除缓存吗？")
                        .onYes(new SimpleCallback<Void>() {
                            @Override
                            public void onResult(Void data) {
                                presenter.clearCache();
                            }
                        })
                        .show();
                break;
            case R.id.ll_about:
                AboutActivity.start(getContext());
                break;
            case R.id.ll_logout:
                TipDialog.with(getContext())
                        .message("确定要退出登录吗？")
                        .onYes(new SimpleCallback<Void>() {
                            @Override
                            public void onResult(Void data) {
                                presenter.logout();
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public void updateSuccess(int code, UpdateBean data, boolean click) {
        if (mUpdateUtils == null) {
            mUpdateUtils = UpdateUtils.newInstance();
        }
        boolean isNewest = mUpdateUtils.isNewest(data.getVersion_code());
        if (isNewest) {
            tv_has_update.setTextColor(ResUtils.getColor(R.color.text_main));
            tv_has_update.setText("发现新版本");
        } else {
            tv_has_update.setTextColor(ResUtils.getColor(R.color.text_gray_light));
            tv_has_update.setText("已是最新版");
        }
        if (!click) {
            return;
        }
        if (!isNewest) {
            ToastMaker.showShort("已是最新版");
            return;
        }
        UpdateDialog.with(getContext())
                .setUrl(data.getUrl())
                .setVersionCode(data.getVersion_code())
                .setVersionName(data.getVersion_name())
                .setForce(data.isForce())
                .setDescription(data.getDesc())
                .setTime(data.getTime())
                .setOnUpdateListener(new UpdateDialog.OnUpdateListener() {
                    @Override
                    public void onDownload(String url, boolean isForce) {
                        download(data.getVersion_name(), url, isForce);
                    }

                    @Override
                    public void onIgnore(int versionCode) {
                    }
                })
                .show();
    }

    @Override
    public void updateFailed(int code, String msg, boolean click) {
    }

    @Override
    public void logoutSuccess(int code, BaseBean data) {
        UserUtils.getInstance().logout();
        new LoginEvent(false).post();
        finish();
    }

    @Override
    public void logoutFailed(int code, String msg) {
        ToastMaker.showShort(msg);
    }

    @Override
    public void getCacheSizeSuccess(String size) {
        tv_cache.setText(size);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mRuntimeRequester != null) {
            mRuntimeRequester.onActivityResult(requestCode);
        }
    }

    private void download(final String versionName, final String url, final boolean isForce) {
        mRuntimeRequester = AnyPermission.with(getContext())
                .runtime(REQ_CODE_PERMISSION)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onBeforeRequest(new RequestInterceptor<String>() {
                    @Override
                    public void intercept(@NonNull String data, @NonNull Executor executor) {
                        PermissionDialog.with(getContext())
                                .setGoSetting(false)
                                .setGroupType(PermissionDialog.GroupType.STORAGE)
                                .setOnNextListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.execute();
                                    }
                                })
                                .setOnCloseListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.cancel();
                                    }
                                })
                                .show();
                    }
                })
                .onBeenDenied(new RequestInterceptor<String>() {
                    @Override
                    public void intercept(@NonNull String data, @NonNull Executor executor) {
                        PermissionDialog.with(getContext())
                                .setGoSetting(false)
                                .setGroupType(PermissionDialog.GroupType.STORAGE)
                                .setOnNextListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.execute();
                                    }
                                })
                                .setOnCloseListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.cancel();
                                    }
                                })
                                .show();
                    }
                })
                .onGoSetting(new RequestInterceptor<String>() {
                    @Override
                    public void intercept(@NonNull String data, @NonNull Executor executor) {
                        PermissionDialog.with(getContext())
                                .setGoSetting(true)
                                .setGroupType(PermissionDialog.GroupType.STORAGE)
                                .setOnNextListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.execute();
                                    }
                                })
                                .setOnCloseListener(new SimpleCallback<Void>() {
                                    @Override
                                    public void onResult(Void data) {
                                        executor.cancel();
                                    }
                                })
                                .show();
                    }
                })
                .request(new RequestListener() {
                    @Override
                    public void onSuccess() {
                        DownloadDialog.with(getActivity(), isForce, url, versionName);
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }
}
