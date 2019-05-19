package per.goweii.wanandroid.module.mine.view;

import per.goweii.basic.core.base.BaseView;
import per.goweii.wanandroid.module.main.model.UpdateBean;

/**
 * @author CuiZhen
 * @date 2019/5/19
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
public interface SettingView extends BaseView {
    void updateSuccess(int code, UpdateBean data, boolean click);
    void updateFailed(int code, String msg, boolean click);

    void getCacheSizeSuccess(String size);
}
