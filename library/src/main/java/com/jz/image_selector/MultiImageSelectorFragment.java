package com.jz.image_selector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jz.image_selector.adapter.FolderAdapter;
import com.jz.image_selector.adapter.ImageGridAdapter;
import com.jz.image_selector.bean.Folder;
import com.jz.image_selector.bean.Image;
import com.jz.image_selector.utils.FileUtils;
import com.jz.image_selector.view.ListImageDirPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择Fragment
 * Created by Nereo on 2015/4/7.
 */
public class MultiImageSelectorFragment extends Fragment {

    /**
     * 最大图片选择次数，int类型
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 图片选择模式，int类型
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 是否显示相机，boolean类型
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 默认选择的数据集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";
    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;
    private static final String TAG = "MultiImageSelector";
    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_PREVIEW = 101;
    private static final int REQUEST_SINGLE = 102;


    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();
    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    // 图片Grid
    private GridView mGridView;
    private Callback mCallback;

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    // 类别
    private TextView mCategoryText;
    // 预览按钮
    private Button mPreviewBtn;
    // 底部View
    private View mPopupAnchorView;

    private int mDesireImageCount;

    private boolean hasFolderGened = false;
    private boolean mIsShowCamera = false;

    private int mGridWidth, mGridHeight;

    private File mTmpFile;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        private boolean fileExist(String path) {
            if (!TextUtils.isEmpty(path)) {
                return new File(path).exists();
            }
            return false;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<Image> images = new ArrayList<>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if (!fileExist(path)) {
                            continue;
                        }
                        Image image = null;
                        if (!TextUtils.isEmpty(name)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }

                    } while (data.moveToNext());

                    mImageAdapter.setData(images);

                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 选择图片数量
        mDesireImageCount = getArguments().getInt(EXTRA_SELECT_COUNT);

        // 图片选择模式
        final int mode = getArguments().getInt(EXTRA_SELECT_MODE);

        // 默认选择
        if (mode == MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(EXTRA_DEFAULT_SELECTED_LIST);
            if (tmp != null && tmp.size() > 0) {
                resultList = tmp;
            }
        }

        // 是否显示照相机
        mIsShowCamera = getArguments().getBoolean(EXTRA_SHOW_CAMERA, true);
        mImageAdapter = new ImageGridAdapter(getActivity(), mIsShowCamera);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == MODE_MULTI);

        mPopupAnchorView = view.findViewById(R.id.footer);

        mCategoryText = (TextView) view.findViewById(R.id.category_btn);
        // 初始化，加载所有图片
        mCategoryText.setText(R.string.folder_all);
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mListImageDirPopupWindow == null) {
                    createPopList(mGridWidth, mGridHeight);
                }
                if (mListImageDirPopupWindow.isShowing()) {
                    mListImageDirPopupWindow.dismiss();
                } else {
                    mListImageDirPopupWindow.showAsDropDown(mPopupAnchorView, 0, 0);
                    // 设置背景颜色变暗
                    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                    lp.alpha = .3f;
                    getActivity().getWindow().setAttributes(lp);
                }
            }
        });

        mPreviewBtn = (Button) view.findViewById(R.id.preview);
        // 初始化，按钮状态初始化
        if (resultList == null || resultList.size() <= 0) {
            mPreviewBtn.setText(R.string.preview);
        } else {
            mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
        }
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList.size() != 0) {
                    Intent intent = new Intent(getActivity(), PreviewPicturesActivity.class);
                    intent.putExtra("pics", resultList);
                    startActivityForResult(intent, REQUEST_PREVIEW);
                } else {
                    Toast.makeText(getActivity(), "请选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGridView = (GridView) view.findViewById(R.id.grid);
        /*mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {

                final Picasso picasso = Picasso.with(getActivity());
                if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
                    picasso.resumeTag(getActivity());
                } else {
                    picasso.pauseTag(getActivity());
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });*/
        mGridView.setAdapter(mImageAdapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int width = mGridView.getWidth();
                final int height = mGridView.getHeight();

                mGridWidth = width;
                mGridHeight = height;

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                mImageAdapter.setItemSize(columnWidth);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mImageAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        showCameraAction();
                    } else {
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mFolderAdapter = new FolderAdapter(getActivity());
    }

    /**
     * 显示文件夹的popupwindow
     */
    private ListImageDirPopupWindow mListImageDirPopupWindow;

    private void createPopList(int width, int height) {
        mListImageDirPopupWindow = new ListImageDirPopupWindow(
                width, (int) (height * 0.7),
                mResultFolder, LayoutInflater.from(getActivity()).inflate(R.layout.list_dir, null));

        mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1.0f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        // 设置选择文件夹的回调
        mListImageDirPopupWindow.setOnImageDirSelected(new ListImageDirPopupWindow.OnImageDirSelected() {

            @Override
            public void selected(final Folder floder, int position) {
                mFolderAdapter.setSelectIndex(position);

                final int index = position;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListImageDirPopupWindow.dismiss();

                        if (index == 0) {
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.folder_all);
                            if (mIsShowCamera) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            if (null != floder) {
                                mImageAdapter.setData(floder.images);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                                mCategoryText.setText(floder.name);
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首次加载所有图片
        //new LoadImageTask().execute();
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(mTmpFile);
                    }
                }
            } else {
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
            }
        }

        if (requestCode == REQUEST_PREVIEW) {
            if (resultCode == Activity.RESULT_OK) {
                ((MultiImageSelectorActivity) getActivity()).finishSelect();
            }
        }

        Log.i("requestCode", requestCode + "");
        Log.i("resultCode", resultCode + "");
        if (requestCode == REQUEST_SINGLE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("data", data.getStringArrayListExtra("pics").get(0));
                if (mCallback != null && data != null)
                    mCallback.onSingleImageSelected(data.getStringArrayListExtra("pics").get(0));
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "on change");

        if (mListImageDirPopupWindow != null) {
            if (mListImageDirPopupWindow.isShowing()) {
                mListImageDirPopupWindow.dismiss();
            }
        }


        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int height = mGridView.getHeight();

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                Log.d(TAG, "Desire Size = " + desireSize);
                final int numCount = mGridView.getWidth() / desireSize;
                Log.d(TAG, "Grid Size = " + mGridView.getWidth());
                Log.d(TAG, "num count = " + numCount);
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (mGridView.getWidth() - columnSpace * (numCount - 1)) / numCount;
                mImageAdapter.setItemSize(columnWidth);

                if (mListImageDirPopupWindow != null) {
                    mListImageDirPopupWindow.setHeight(height * 5 / 8);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        super.onConfigurationChanged(newConfig);

    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = FileUtils.createTmpFile(getActivity());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getActivity(), R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if (resultList.size() != 0) {
                        mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
                    } else {
                        mPreviewBtn.setText(R.string.preview);
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    // 判断选择数量问题
                    if (mDesireImageCount == resultList.size()) {
                        Toast.makeText(getActivity(), R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.path);
                    mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);
            } else if (mode == MODE_SINGLE) {
                // 单选模式
                if (mCallback != null) {
//                    Intent intent = new Intent(getActivity(), PreviewPicturesActivity.class);
//                    ArrayList<String> pic = new ArrayList<>();
//                    pic.add(image.path);
//                    intent.putExtra("pics", pic);
//                    startActivityForResult(intent, REQUEST_SINGLE);

                    //单选选取图片直接回掉,不进行展示
                    mCallback.onSingleImageSelected(image.path);
                }
            }
        }
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onSingleImageSelected(String path);

        void onImageSelected(String path);

        void onImageUnselected(String path);

        void onCameraShot(File imageFile);
    }
}
