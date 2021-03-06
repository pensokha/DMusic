package com.d.music.mvp.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.d.commen.base.BaseFragment;
import com.d.commen.mvp.MvpBasePresenter;
import com.d.commen.mvp.MvpView;
import com.d.music.R;
import com.d.music.commen.AlertDialogFactory;
import com.d.music.commen.Preferences;
import com.d.music.module.events.MusicModelEvent;
import com.d.music.module.events.RefreshEvent;
import com.d.music.module.events.SortTypeEvent;
import com.d.music.module.greendao.db.MusicDB;
import com.d.music.module.greendao.music.base.MusicModel;
import com.d.music.module.greendao.util.MusicDBUtil;
import com.d.music.module.media.MusicFactory;
import com.d.music.module.repeatclick.ClickUtil;
import com.d.music.mvp.activity.ScanActivity;
import com.d.music.utils.Util;
import com.d.music.utils.fileutil.FileUtil;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 扫描首页
 * Created by D on 2017/4/29.
 */
public class ScanFragment extends BaseFragment<MvpBasePresenter> implements MvpView {
    @Bind(R.id.btn_full_scan)
    Button btnFullScan;
    @Bind(R.id.btn_custom_scan)
    Button btnCustomScan;

    private Context context;
    private int type;
    private CustomScanFragment customScanFragment;
    private AlertDialog dialog;//进度提示dialog
    private int[] cornerDrawable = new int[]{R.drawable.bg_corner_main
            , R.drawable.bg_corner_main0
            , R.drawable.bg_corner_main1
            , R.drawable.bg_corner_main2
            , R.drawable.bg_corner_main3
            , R.drawable.bg_corner_main4
            , R.drawable.bg_corner_main5
            , R.drawable.bg_corner_main6
            , R.drawable.bg_corner_main7
            , R.drawable.bg_corner_main8
            , R.drawable.bg_corner_main9
            , R.drawable.bg_corner_main10
            , R.drawable.bg_corner_main11
            , R.drawable.bg_corner_main12
            , R.drawable.bg_corner_main13
            , R.drawable.bg_corner_main14
            , R.drawable.bg_corner_main15
            , R.drawable.bg_corner_main16
            , R.drawable.bg_corner_main17
    };

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_scan;
    }

    @Override
    public MvpBasePresenter getPresenter() {
        return new MvpBasePresenter(this.getActivity().getApplicationContext());
    }

    @Override
    protected MvpView getMvpView() {
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt("type");
        }
    }

    @Override
    protected void init() {

    }

    @Override
    public void onResume() {
        int cur = Preferences.getInstance(context.getApplicationContext()).getSkin() + 1;
        if (cur >= 0 && cur < cornerDrawable.length) {
            btnFullScan.setBackgroundResource(cornerDrawable[cur]);
            btnCustomScan.setBackgroundResource(cornerDrawable[cur]);
        }
        super.onResume();
    }

    @OnClick({R.id.btn_full_scan, R.id.btn_custom_scan})
    public void OnClickLister(final View view) {
        if (ClickUtil.isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.btn_full_scan:
            case R.id.btn_custom_scan:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    RxPermissions rxPermissions = new RxPermissions((Activity) context);
                    rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Permission>() {
                                @Override
                                public void accept(@NonNull Permission permission) throws Exception {
                                    if (context == null || getActivity() == null || getActivity().isFinishing()) {
                                        return;
                                    }
                                    if (permission.granted) {
                                        // `permission.name` is granted !
                                        sw(view.getId());
                                    } else if (permission.shouldShowRequestPermissionRationale) {
                                        // Denied permission without ask never again
                                        Util.toast(context, "Denied permission!");
                                    } else {
                                        // Denied permission with ask never again
                                        // Need to go to the settings
                                        Util.toast(context, "Denied permission with ask never again!");
                                    }
                                }
                            });
                } else {
                    sw(view.getId());
                }
                break;
        }
    }

    private void sw(int viewId) {
        switch (viewId) {
            case R.id.btn_full_scan:
                scanAll();
                return;
            case R.id.btn_custom_scan:
                goCustomScan();
                break;
        }
    }

    private void goCustomScan() {
        if (customScanFragment == null) {
            customScanFragment = new CustomScanFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            customScanFragment.setArguments(bundle);
        }
        ScanActivity activity = (ScanActivity) getActivity();
        activity.replaceFragment(customScanFragment);
    }

    private void scanAll() {
        dialog = AlertDialogFactory.createFactory(context).getLoadingDialog();
        Observable.create(new ObservableOnSubscribe<List<MusicModel>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<MusicModel>> e) throws Exception {
                List<String> paths = new ArrayList<>();
                paths.add(FileUtil.getRootPath());
                List<MusicModel> list = (List<MusicModel>) MusicFactory.createFactory(context, type).getMusic(paths);
                MusicDBUtil.getInstance(context).deleteAll(type);
                MusicDBUtil.getInstance(context).insertOrReplaceMusicInTx(list, type);
                MusicDBUtil.getInstance(context).updateCusListCount(type, list != null ? list.size() : 0);
                MusicDBUtil.getInstance(context).updateCusListSoryByType(type, MusicDB.ORDER_TYPE_TIME);//默认按时间排序
                EventBus.getDefault().post(new SortTypeEvent(type, MusicDB.ORDER_TYPE_TIME));//按时间排序

                //更新收藏字段
                List<MusicModel> c = (List<MusicModel>) MusicDBUtil.getInstance(context).queryAllMusic(MusicDB.COLLECTION_MUSIC);
                MusicDBUtil.getInstance(context).insertOrReplaceMusicInTx(MusicModel.clone(c, type), type);

                //更新首页自定义列表
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_INVALID, RefreshEvent.SYNC_CUSTOM_LIST));

                if (list == null) {
                    list = new ArrayList<>();
                }
                e.onNext(list);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<MusicModel>>() {
                    @Override
                    public void accept(@NonNull List<MusicModel> list) throws Exception {
                        MusicModelEvent event = new MusicModelEvent(type, list);
                        EventBus.getDefault().post(event);

                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }
                });
    }
}
