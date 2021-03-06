package com.d.music.mvp.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.d.music.MainActivity;
import com.d.music.R;
import com.d.music.model.SingerModel;
import com.d.music.module.greendao.db.MusicDB;
import com.d.music.module.repeatclick.OnClickFastListener;
import com.d.music.mvp.fragment.SongFragment;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.lib.xrv.adapter.CommonHolder;

import java.util.List;


public class SingerAdapter extends CommonAdapter<SingerModel> {

    public SingerAdapter(Context context, List<SingerModel> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public void convert(int position, CommonHolder holder, final SingerModel item) {
        holder.setText(R.id.tv_singer, item.singer);
        holder.setText(R.id.tv_title, "" + item.count);
        holder.setViewOnClickListener(R.id.llyt_singer, new OnClickFastListener() {
            @Override
            public void onFastClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", item.singer);
                bundle.putInt("type", MusicDB.LOCAL_ALL_MUSIC);
                bundle.putInt("tab", 1);
                SongFragment songFragment = new SongFragment();
                songFragment.setArguments(bundle);

                MainActivity.replace(songFragment);
            }
        });
    }
}
