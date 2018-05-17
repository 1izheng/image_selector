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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹Adapter
 */
public class FolderAdapter extends BaseAdapter {

    int mImageSize;
    private static int lastSelected ;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Folder> mFolders = new ArrayList<>();

    public FolderAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }
    
    public FolderAdapter(Context context, List<Folder> mDatas) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
        this.mFolders=mDatas;
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
        return mFolders.size() + 1;
    }

    @Override
    public Folder getItem(int i) {
        if (i == 0) return null;
        return mFolders.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
            if (i == 0) {
                holder.name.setText("所有图片");
                holder.size.setText(getTotalImageSize() + "张");
                if (mFolders.size() > 0) {
                    Folder f = mFolders.get(0);

                    //显示图片
                    Glide.with(mContext)
                            .load(new File(f.cover.path))
                            .error(R.drawable.default_error)
                            .override(mImageSize, mImageSize)
                            .centerCrop()
                            .into(holder.cover);
                }
            } else {
                holder.bindData(getItem(i));
            }
            if (lastSelected == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            } else {
                holder.indicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    private int getTotalImageSize() {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0) {
            for (Folder f : mFolders) {
                result += f.images.size();
            }
        }
        return result;
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

        void bindData(Folder data) {
            name.setText(data.name);
            size.setText(data.images.size() + "张");
            // 显示图片
            Glide.with(mContext)
                    .load(new File(data.cover.path))
                    .placeholder(R.drawable.default_error)
                    .override(mImageSize, mImageSize)
                    .centerCrop()
                    .into(cover);
        }
    }

}