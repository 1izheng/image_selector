package com.jz.image_selector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.jz.image_selector.adapter.FolderAdapter;
import com.jz.image_selector.adapter.ImageGridAdapter;
import com.jz.image_selector.bean.Folder;
import com.jz.image_selector.bean.Image;
import com.jz.image_selector.utils.FileUtils;
import com.jz.image_selector.utils.ImageDataSource;
import com.jz.image_selector.utils.ScreenUtils;
import com.jz.image_selector.view.FolderPopUpWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 多图选择
 */
public class ImageSelectorActivity extends FragmentActivity implements View.OnClickListener, ImageDataSource.OnImagesLoadedListener, AdapterView.OnItemClickListener {

    /**
     * 选择结果，返回为 ArrayList 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 必须选择的数量
     */
    public static final String MUST_COUNT = "must_count";
    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

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
     * 是否裁剪
     */
    public static final String EXTRA_DEFAULT_CROP = "isCrop";

    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;
    /**
     * 是否需要裁剪
     */
    public static boolean isCrop = true;

    private int mustCount;

    private int mDefaultCount;  //最大选择数量

    //是否直接拍照
    public static final String DEFAULT_START_CAMERA = "default_start_camera";
    private boolean defaultStartCamera = false; //是否直接拍照

    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100; //拍照返回
    private static final int REQUEST_PREVIEW = 101; //预览页
    private static final int REQUEST_CROP = 103; //裁剪成功返回

    // 图片Grid
    private GridView mGridView;
    // 类别
    private TextView mCategoryText;
    // 预览按钮
    private Button mPreviewBtn;
    // 底部View
    private View mPopupAnchorView;
    // 确定
    private Button mSubmitButton;

    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();

    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    private ImageGridAdapter mImageAdapter;

    private FolderAdapter mFolderAdapter;
    private FolderPopUpWindow mFolderPopupWindow;

    private boolean mIsShowCamera = false;

    private File camearFile; //拍照保存的临时文件

    private int currentMode;

    /**
     * @param activity
     * @param requestCode
     * @param maxNum
     * @param selectedMode
     * @param defaultStartCamera
     * @param isCrop
     * @param showCamera
     * @param resultList
     */
    public static void startSelect(Activity activity, int requestCode, int maxNum, int selectedMode, boolean defaultStartCamera, boolean isCrop, boolean showCamera, ArrayList<String> resultList) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        // 最大可选择图片数量
        intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
        // 选择模式
        intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_MODE, selectedMode);
        //是否直接开始拍照
        intent.putExtra(DEFAULT_START_CAMERA, defaultStartCamera);
        //是否裁剪
        intent.putExtra(EXTRA_DEFAULT_CROP, isCrop);
        //是否显示相机
        intent.putExtra(EXTRA_SHOW_CAMERA, showCamera);
        //已经选择的图片
        intent.putStringArrayListExtra(ImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, resultList);
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * @param activity     activity
     * @param requestCode  requestCode
     * @param maxNum       最多选择图片数
     * @param selectedMode ImageSelectorActivity.MODE_SINGLE , ImageSelectorActivity.MODE_MULTI
     */
    public static void startSelect(Activity activity, int requestCode, int maxNum, int selectedMode, boolean defaultStartCamera, ArrayList<String> resultList) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        // 最大可选择图片数量
        intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_COUNT, maxNum);
        // 选择模式
        intent.putExtra(ImageSelectorActivity.EXTRA_SELECT_MODE, selectedMode);
        //是否直接开始拍照
        intent.putExtra(DEFAULT_START_CAMERA, defaultStartCamera);
        //已经选择的图片
        intent.putStringArrayListExtra(ImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, resultList);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector_z);
        initIntent();
        //直接拍照
        defaultStartCamera = getIntent().getBooleanExtra(DEFAULT_START_CAMERA, false);
        if (defaultStartCamera) {
            if (!checkPermission(Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            } else {
                showCameraAction();
            }
        }
        initView();
        initData();
    }

    private void initIntent() {
        Intent intent = getIntent();
        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, 0);
        mustCount = intent.getIntExtra(MUST_COUNT, 0);
        currentMode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        mIsShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);//默认显示照相机
        isCrop = intent.getBooleanExtra(EXTRA_DEFAULT_CROP, true); //默认裁剪
        if (currentMode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
        }
    }

    private void initData() {
        // 首次加载所有图片
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(ImageSelectorActivity.this, this);
            } else {
                ActivityCompat.requestPermissions(ImageSelectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        } else {
            new ImageDataSource(ImageSelectorActivity.this, this);
        }
    }

    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, this);
            } else {
                Toast.makeText(getApplicationContext(), "权限被禁止，无法选择本地图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //调用相机
                showCameraAction();
            } else {
                Toast.makeText(getApplicationContext(), "权限被禁止，无法打开相机", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(this);
        // 完成按钮
        mSubmitButton = (Button) findViewById(R.id.btn_commit);
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText("完成");
            mSubmitButton.setEnabled(false);
        } else {
            mSubmitButton.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setOnClickListener(this);
        mImageAdapter = new ImageGridAdapter(this, mIsShowCamera);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(currentMode == MODE_MULTI);
        mPopupAnchorView = findViewById(R.id.footer);
        mCategoryText = (TextView) findViewById(R.id.category_btn);
        // 初始化，加载所有图片
        mCategoryText.setText(R.string.folder_all);
        mCategoryText.setOnClickListener(this);

        mPreviewBtn = (Button) findViewById(R.id.preview);
        // 初始化，按钮状态初始化
        if (resultList == null || resultList.size() <= 0) {
            mPreviewBtn.setText(R.string.preview);
        } else {
            mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
        }
        mPreviewBtn.setOnClickListener(this);

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setNumColumns(ScreenUtils.getColumn(this));
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);

        mFolderAdapter = new FolderAdapter(this, null);
    }


    /**
     * 创建弹出的ListView popup
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mFolderAdapter.setSelectIndex(position);
                mFolderPopupWindow.dismiss();
                Folder folder = (Folder) adapterView.getAdapter().getItem(position);
                if (null != folder) {
                    mImageAdapter.setData(folder.images);
                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }
                    mCategoryText.setText(folder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(mPopupAnchorView.getHeight());
    }

    public void finishSelect() {
        Intent data = new Intent();
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            mSubmitButton.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
            if (mustCount != 0) {
                if (resultList.size() < mustCount) {
                    mSubmitButton.setEnabled(false);
                } else {
                    mSubmitButton.setEnabled(true);
                }
            } else {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
            mSubmitButton.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
        } else {
            mSubmitButton.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
        }
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            mSubmitButton.setText("完成");
            mSubmitButton.setEnabled(false);
        }
    }

    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));// 刷新系统相册

            Intent data = new Intent();
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                //是否裁剪
                if (isCrop) {
                    //去裁剪
                    Intent intent = new Intent(this, ImageCropActivity.class);
                    intent.putExtra(ImageCropActivity.IMAGE_PATH, camearFile.getAbsolutePath());
                    startActivityForResult(intent, REQUEST_CROP);
                } else {
                    if (camearFile != null) {
                        onCameraShot(camearFile);
                    }
                }
            } else {
                if (camearFile != null && camearFile.exists()) {
                    camearFile.delete();
                }
                if (defaultStartCamera) {
                    finish();
                }
            }
        }

        //裁剪完成
        if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            String path = data.getStringExtra(EXTRA_RESULT);
            Intent intent = new Intent();
            resultList.add(path);
            intent.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, intent);
            finish();
        }

        //预览
        if (requestCode == REQUEST_PREVIEW) {
            if (resultCode == RESULT_OK) {
                finishSelect();
            }
        }

    }

    /**
     * 调用相机拍照
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            camearFile = FileUtils.createTmpFile(this);

            if (camearFile != null) {
                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    uri = Uri.fromFile(camearFile);
                } else {
                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                     */
                    uri = FileProvider.getUriForFile(this, FileUtils.getFileProviderName(this), camearFile);
                    //加入uri权限 要不三星手机不能拍照
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA);

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
                    onImageUnselected(image.path);
                } else {
                    // 判断选择数量问题
                    if (mDefaultCount == resultList.size()) {
                        Toast.makeText(this, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.path);
                    mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
                    onImageSelected(image.path);
                }
                mImageAdapter.select(image);
            } else if (mode == MODE_SINGLE) {
                // 单选模式
                //判断是否需要裁剪
                if (isCrop) {
                    //去裁剪
                    Intent intent = new Intent(this, ImageCropActivity.class);
                    intent.putExtra(ImageCropActivity.IMAGE_PATH, image.path);
                    startActivityForResult(intent, REQUEST_CROP);
                } else {
                    //不裁剪
                    onSingleImageSelected(image.path);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //简单处理旋转屏幕..
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) { //返回
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_commit) { //确定
            if (resultList != null && resultList.size() > 0) {
                // 返回已选择的图片数据
                finishSelect();
            }
        } else if (id == R.id.preview) { //预览
            if (resultList.size() != 0) {
                Intent intent = new Intent(this, PreviewImagesActivity.class);
                intent.putExtra("pics", resultList);
                startActivityForResult(intent, REQUEST_PREVIEW);
            } else {
                Toast.makeText(this, "请选择图片", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.category_btn) {
            //创建popup
            createPopupFolderList();

            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mPopupAnchorView, Gravity.NO_GRAVITY, 0, 0);
                int index = mFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }

        }
    }

    @Override
    public void onImagesLoaded(ArrayList<Folder> imageFolders, ArrayList<Image> images) {
        this.mResultFolder = imageFolders;
        mImageAdapter.setData(images);

        // 设定默认选择
        if (resultList != null && resultList.size() > 0) {
            mImageAdapter.setDefaultSelected(resultList);
        }

        mFolderAdapter.setData(mResultFolder);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mImageAdapter.isShowCamera()) {
            // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
            if (position == 0) {
                if (!checkPermission(Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    showCameraAction();
                }
            } else {
                // 正常操作
                Image image = (Image) parent.getAdapter().getItem(position);
                selectImageFromGrid(image, currentMode);
            }
        } else {
            // 正常操作
            Image image = (Image) parent.getAdapter().getItem(position);
            selectImageFromGrid(image, currentMode);
        }
    }
}
