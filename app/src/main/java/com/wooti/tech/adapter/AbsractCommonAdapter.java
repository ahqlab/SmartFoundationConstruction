package com.wooti.tech.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import java.io.Serializable;
import java.util.List;

public abstract class AbsractCommonAdapter<D extends Serializable> extends BaseAdapter {

    Context context;
    public LayoutInflater inflater;
    public List<D> data;

    int position;

    public AbsractCommonAdapter(Context context, List<D> data) {
        super();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    protected abstract View getUserEditView(int position, View convertView, ViewGroup parent);

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public D getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        return getUserEditView(position, convertView, parent);
    }


}
