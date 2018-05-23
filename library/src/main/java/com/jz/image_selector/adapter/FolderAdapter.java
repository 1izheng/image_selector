package com.jz.image_selector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jz.image_selector.R;
import com.jz.image_selector.bean.Folder;
import com.jz.image_selector.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹Adapter
 */
public class FolderAdapter extends BaseAdapter {

    private int mImageSize;
    private static int lastSelected = 0;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Folder> mFolders;

    public FolderAdapter(Context context, List<Folder> mDatas) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = ScreenUtils.getImageItemWidth(context);
        if (mDatas != null && mDatas.size() > 0) {
            mFolders = mDatas;
        } else {
            mFolders = new ArrayList<>();
        }
    }

    /**
     * 设置数据集
     *
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size();
    }

    @Override
    public Folder getItem(int position) {
        return mFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }


        Folder folder = getItem(position);
        holder.name.setText(folder.name);
        holder.size.setText(folder.images.size() + "张");
        // 显示图片
        Glide.with(mContext)
                .load(new File(folder.cover.path))
                .placeholder(R.drawable.default_error)
                .override(mImageSize, mImageSize)
                .centerCrop()
                .into(holder.cover);

        if (lastSelected == position) {
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }
    }

}
