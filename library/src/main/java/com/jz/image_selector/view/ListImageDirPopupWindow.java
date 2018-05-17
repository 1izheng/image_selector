package com.jz.image_selector.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jz.image_selector.R;
import com.jz.image_selector.adapter.FolderAdapter;
import com.jz.image_selector.bean.Folder;

import java.util.List;

public class ListImageDirPopupWindow extends BasePopupWindowForListView<Folder>
{
	private ListView mListDir;

	public ListImageDirPopupWindow(int width, int height,
								   List<Folder> datas, View convertView)
	{
		super(convertView, width, height, true, datas);
	}

	public ListView getListView(){
		return mListDir;
	}
	
	@Override
	public void initViews()
	{
		mListDir = (ListView) findViewById(R.id.id_list_dir);
		mListDir.setAdapter(new FolderAdapter(context,mDatas));
	}
	
	public void setAdapter(FolderAdapter adapter){
		mListDir.setAdapter(adapter);
	}

	public interface OnImageDirSelected
	{
		void selected(Folder floder, int position);
	}

	private OnImageDirSelected mImageDirSelected;

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected)
	{
		this.mImageDirSelected = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		mListDir.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
			{

				if (mImageDirSelected != null)
				{
					Folder folder=(Folder) parent.getAdapter().getItem(position);
					mImageDirSelected.selected(folder,position);
				}
			}
		});
	}

	@Override
	public void init()
	{

	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
	}

}
