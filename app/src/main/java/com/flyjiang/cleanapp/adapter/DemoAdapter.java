package com.flyjiang.cleanapp.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.flyjiang.cleanapp.R;

import java.util.List;

/**
 * Created by ${flyjiang} on 2019/10/17.
 * 文件说明：demo
 */
public class DemoAdapter extends BaseQuickAdapter<Object, BaseViewHolder> {

    public DemoAdapter(@Nullable List<Object> data) {
        super(R.layout.activity_splash, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Object item) {

    }
}
