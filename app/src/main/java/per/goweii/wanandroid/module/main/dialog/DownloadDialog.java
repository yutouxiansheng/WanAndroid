package per.goweii.wanandroid.module.main.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import per.goweii.anylayer.AnyLayer;
import per.goweii.anylayer.LayerManager;
import per.goweii.anypermission.AnyPermission;
import per.goweii.anypermission.RequestListener;
import per.goweii.basic.utils.ResUtils;
import per.goweii.basic.utils.ToastMaker;
import per.goweii.basic.utils.coder.MD5Coder;
import per.goweii.basic.utils.file.CacheUtils;
import per.goweii.basic.utils.file.FileUtils;
import per.goweii.rxhttp.download.DownloadInfo;
import per.goweii.rxhttp.download.RxDownload;
import per.goweii.wanandroid.R;


/**
 * 版本更新弹窗
 *
 * @author Cuizhen
 * @date 2018/8/6-上午9:17
 */
public class DownloadDialog {

    private final Activity mActivity;
    private AnyLayer mAnyLayer = null;
    private final boolean isForce;
    private final String url;
    private final String versionName;
    private boolean isAutoInstall = true;

    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvApkSize;
    private TextView tvState;
    private TextView tvSpeed;
    private TextView tvLine;
    private File mApk;
    private RxDownload mRxDownload;

    private int retryCount = 0;

    public static DownloadDialog with(Activity activity, boolean isForce, String url, String versionName) {
        return new DownloadDialog(activity, isForce, url, versionName);
    }

    private DownloadDialog(Activity activity, boolean isForce, String url, String versionName) {
        this.mActivity = activity;
        this.url = url;
        this.isForce = isForce;
        this.versionName = versionName;
        showDialog();
        startDownload();
    }

    public DownloadDialog setAutoInstall(boolean autoInstall) {
        isAutoInstall = autoInstall;
        return this;
    }

    private void startDownload() {
        DownloadInfo info = DownloadInfo.create(url, CacheUtils.getCacheDir(), MD5Coder.encode(url) + ".apk");
        mRxDownload = RxDownload.create(info)
                .setProgressListener(new RxDownload.ProgressListener() {
                    @Override
                    public void onProgress(float progress, long downloadLength, long contentLength) {
                        setProgress((int) (progress * 100));
                    }
                })
                .setDownloadListener(new RxDownload.DownloadListener() {
                    @Override
                    public void onStarting(DownloadInfo info) {
                        preDownload();
                    }

                    @Override
                    public void onDownloading(DownloadInfo info) {
                        if (tvApkSize != null) {
                            tvApkSize.setText(FileUtils.formatSize(info.contentLength));
                        }
                        if (tvLine != null) {
                            tvLine.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onStopped(DownloadInfo info) {
                    }

                    @Override
                    public void onCanceled(DownloadInfo info) {
                    }

                    @Override
                    public void onCompletion(DownloadInfo info) {
                        mApk = new File(info.saveDirPath, info.saveFileName);
                        if (isAutoInstall && tvState != null) {
                            tvState.performClick();
                        }
                    }

                    @Override
                    public void onError(DownloadInfo info, Throwable e) {
                        FileUtils.delete(new File(info.saveDirPath, info.saveFileName));
                        retryCount++;
                        if (retryCount <= 3) {
                            startDownload();
                        } else {
                            ToastMaker.showShort("下载失败，建议清除缓存后重新尝试");
                            dismiss();
                        }
                    }
                })
                .setSpeedListener(new RxDownload.SpeedListener() {
                    @Override
                    public void onSpeedChange(float bytesPerSecond, String speedFormat) {
                        if (tvSpeed != null) {
                            tvSpeed.setText(speedFormat);
                        }
                    }
                });
        mRxDownload.start();
    }

    private void showDialog() {
        mAnyLayer = AnyLayer.with(mActivity)
                .contentView(R.layout.dialog_download)
                .cancelableOnTouchOutside(false)
                .cancelableOnClickKeyBack(false)
                .bindData(new LayerManager.IDataBinder() {
                    @Override
                    public void bind(AnyLayer anyLayer) {
                        ImageView ivClose = anyLayer.getView(R.id.iv_dialog_download_close);
                        if (isForce) {
                            ivClose.setVisibility(View.GONE);
                        } else {
                            ivClose.setVisibility(View.VISIBLE);
                        }
                        progressBar = anyLayer.getView(R.id.pb_dialog_download);
                        tvProgress = anyLayer.getView(R.id.tv_dialog_download_progress);
                        tvApkSize = anyLayer.getView(R.id.tv_dialog_download_apk_size);
                        tvState = anyLayer.getView(R.id.tv_dialog_download_state);
                        tvSpeed = anyLayer.getView(R.id.tv_dialog_download_speed);
                        tvLine = anyLayer.getView(R.id.tv_dialog_download_line);
                    }
                })
                .backgroundColorRes(R.color.dialog_bg)
                .gravity(Gravity.CENTER)
                .onClick(R.id.tv_dialog_download_state, new LayerManager.OnLayerClickListener() {
                    @Override
                    public void onClick(AnyLayer anyLayer, View view) {
                        if (mApk == null) {
                            return;
                        }
                        if (!isForce) {
                            dismiss();
                        }
                        installApk();
                    }
                })
                .onClick(R.id.iv_dialog_download_close, new LayerManager.OnLayerClickListener() {
                    @Override
                    public void onClick(AnyLayer anyLayer, View view) {
                        if (!isForce) {
                            dismiss();
                        }
                    }
                });
        mAnyLayer.show();
    }

    private void installApk(){
        AnyPermission.with(mActivity)
                .install(mApk)
                .request(new RequestListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }

    private void preDownload() {
        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }
        if (tvApkSize != null) {
            tvApkSize.setText("");
        }
        if (tvProgress != null) {
            tvProgress.setText("");
        }
        if (tvState != null) {
            tvState.setText(R.string.basic_ui_dialog_download_state_downloading);
        }
    }

    private void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (tvProgress != null) {
            tvProgress.setText(progress + "%");
        }
        if (progress >= 100) {
            if (tvState != null) {
                tvState.setText(R.string.basic_ui_dialog_download_state_install);
                tvState.setTextColor(ResUtils.getColor(R.color.text_main));
            }
        }
    }

    private void dismiss() {
        if (mRxDownload != null) {
            mRxDownload.stop();
        }
        if (mAnyLayer != null) {
            mAnyLayer.dismiss();
        }
    }
}
