package per.goweii.wanandroid.module.mine.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import per.goweii.actionbarex.common.ActionBarCommon;
import per.goweii.actionbarex.common.OnActionBarChildClickListener;
import per.goweii.basic.core.base.BaseFragment;
import per.goweii.wanandroid.R;
import per.goweii.wanandroid.event.LoginEvent;
import per.goweii.wanandroid.event.SettingChangeEvent;
import per.goweii.wanandroid.module.login.model.LoginBean;
import per.goweii.wanandroid.module.mine.activity.AboutMeActivity;
import per.goweii.wanandroid.module.mine.activity.CollectionActivity;
import per.goweii.wanandroid.module.mine.activity.OpenActivity;
import per.goweii.wanandroid.module.mine.activity.ReadLaterActivity;
import per.goweii.wanandroid.module.mine.activity.SettingActivity;
import per.goweii.wanandroid.module.mine.presenter.MinePresenter;
import per.goweii.wanandroid.module.mine.view.MineView;
import per.goweii.wanandroid.utils.ImageLoader;
import per.goweii.wanandroid.utils.PictureSelectorUtils;
import per.goweii.wanandroid.utils.SettingUtils;
import per.goweii.wanandroid.utils.UserInfoUtils;
import per.goweii.wanandroid.utils.UserUtils;

/**
 * @author CuiZhen
 * @date 2019/5/12
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public class MineFragment extends BaseFragment<MinePresenter> implements MineView {

    private static final int REQUEST_CODE_SELECT_USER_ICON = 1;
    private static final int REQUEST_CODE_SELECT_BG = 2;

    @BindView(R.id.abc)
    ActionBarCommon abc;
    @BindView(R.id.srl)
    SmartRefreshLayout srl;
    @BindView(R.id.iv_blur)
    ImageView iv_blur;
    @BindView(R.id.rl_user_info)
    RelativeLayout rl_user_info;
    @BindView(R.id.civ_user_icon)
    ImageView civ_user_icon;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_user_id)
    TextView tv_user_id;
    @BindView(R.id.ll_read_later)
    LinearLayout ll_read_later;
    @BindView(R.id.ll_open)
    LinearLayout ll_open;
    @BindView(R.id.ll_about_me)
    LinearLayout ll_about_me;

    public static MineFragment create() {
        return new MineFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (isDetached()) {
            return;
        }
        changeUserInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingChangeEvent(SettingChangeEvent event) {
        if (isDetached()) {
            return;
        }
        if (event.isShowReadLaterChanged() || event.isHideAboutMeChanged() || event.isHideOpenChanged()) {
            changeMenuVisible();
        }
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mine;
    }

    @Nullable
    @Override
    protected MinePresenter initPresenter() {
        return new MinePresenter();
    }

    @Override
    protected void initView() {
        abc.setOnRightIconClickListener(new OnActionBarChildClickListener() {
            @Override
            public void onClick(View v) {
                UserUtils.getInstance().logout();
            }
        });
        srl.setOnMultiPurposeListener(new OnMultiPurposeListener() {
            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                iv_blur.getLayoutParams().height = rl_user_info.getMeasuredHeight() + offset;
                iv_blur.requestLayout();
            }

            @Override
            public void onHeaderReleased(RefreshHeader header, int headerHeight, int maxDragHeight) {
            }

            @Override
            public void onHeaderStartAnimator(RefreshHeader header, int headerHeight, int maxDragHeight) {
            }

            @Override
            public void onHeaderFinish(RefreshHeader header, boolean success) {
            }

            @Override
            public void onFooterMoving(RefreshFooter footer, boolean isDragging, float percent, int offset, int footerHeight, int maxDragHeight) {
                iv_blur.getLayoutParams().height = rl_user_info.getMeasuredHeight() - offset;
                iv_blur.requestLayout();
            }

            @Override
            public void onFooterReleased(RefreshFooter footer, int footerHeight, int maxDragHeight) {
            }

            @Override
            public void onFooterStartAnimator(RefreshFooter footer, int footerHeight, int maxDragHeight) {
            }

            @Override
            public void onFooterFinish(RefreshFooter footer, boolean success) {
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
            }

            @Override
            public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
            }
        });
        rl_user_info.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (UserUtils.getInstance().doIfLogin(getContext())) {
                    PictureSelectorUtils.ofImage(MineFragment.this, REQUEST_CODE_SELECT_BG);
                }
                return true;
            }
        });
        changeMenuVisible();
    }

    @Override
    protected void loadData() {
        changeUserInfo();
    }

    private void changeMenuVisible() {
        SettingUtils settingUtils = SettingUtils.getInstance();
        if (settingUtils.isShowReadLater()) {
            ll_read_later.setVisibility(View.VISIBLE);
        } else {
            ll_read_later.setVisibility(View.GONE);
        }
        if (!settingUtils.isHideAboutMe()) {
            ll_about_me.setVisibility(View.VISIBLE);
        } else {
            ll_about_me.setVisibility(View.GONE);
        }
        if (!settingUtils.isHideOpen()) {
            ll_open.setVisibility(View.VISIBLE);
        } else {
            ll_open.setVisibility(View.GONE);
        }
    }

    private void changeUserInfo() {
        if (UserUtils.getInstance().isLogin()) {
            LoginBean bean = UserUtils.getInstance().getLoginBean();
            ImageLoader.userIcon(civ_user_icon, UserInfoUtils.getInstance().getIcon());
            ImageLoader.userBlur(iv_blur, UserInfoUtils.getInstance().getBg());
            tv_user_name.setText(bean.getUsername());
            tv_user_id.setText(bean.getId() + "");
        } else {
            civ_user_icon.setImageResource(R.color.transparent);
            ImageLoader.userBlur(iv_blur, R.color.transparent);
            tv_user_name.setText("去登陆");
            tv_user_id.setText("-----");
        }
    }

    @OnClick({
            R.id.civ_user_icon, R.id.tv_user_name, R.id.ll_user_id,
            R.id.ll_collect, R.id.ll_read_later, R.id.ll_about_me,
            R.id.ll_open, R.id.ll_setting
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
            case R.id.civ_user_icon:
                if (UserUtils.getInstance().doIfLogin(getContext())) {
                    PictureSelectorUtils.ofImage(this, REQUEST_CODE_SELECT_USER_ICON);
                }
                break;
            case R.id.tv_user_name:
                if (UserUtils.getInstance().doIfLogin(getContext())) {
                }
                break;
            case R.id.ll_user_id:
                UserUtils.getInstance().doIfLogin(getContext());
                break;
            case R.id.ll_collect:
                if (UserUtils.getInstance().doIfLogin(getContext())) {
                    CollectionActivity.start(getContext());
                }
                break;
            case R.id.ll_read_later:
                ReadLaterActivity.start(getContext());
                break;
            case R.id.ll_about_me:
                AboutMeActivity.start(getContext());
                break;
            case R.id.ll_open:
                OpenActivity.start(getContext());
                break;
            case R.id.ll_setting:
                SettingActivity.start(getContext());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            default:
                break;
            case REQUEST_CODE_SELECT_USER_ICON:
                String userIconPath = PictureSelectorUtils.forResult(resultCode, data);
                if (!TextUtils.isEmpty(userIconPath)) {
                    UserInfoUtils.getInstance().setIcon(userIconPath);
                    UserInfoUtils.getInstance().setBg(userIconPath);
                    ImageLoader.userIcon(civ_user_icon, userIconPath);
                    ImageLoader.userBlur(iv_blur, userIconPath);
                }
                break;
            case REQUEST_CODE_SELECT_BG:
                String bgPath = PictureSelectorUtils.forResult(resultCode, data);
                if (!TextUtils.isEmpty(bgPath)) {
                    UserInfoUtils.getInstance().setBg(bgPath);
                    ImageLoader.userBlur(iv_blur, bgPath);
                }
                break;
        }
    }
}
