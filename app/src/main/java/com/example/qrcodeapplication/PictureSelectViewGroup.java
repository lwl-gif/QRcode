package com.example.qrcodeapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;

import static com.luck.picture.lib.config.PictureMimeType.ofImage;

/**
 * @Author:Wallace
 * @Description:自定义一个带有标题和一个展示图片的recyclerView
 * 包含的组件：
 * 1.标题（TextView）
 * 2.当前图片数量（TextView）
 * 3.符号“/”（TextView）
 * 4.图片最大数量（TextView）
 * 5.多选按钮(ImageButton)
 * @Date:2021/4/6 13:55
 * @Modified By:
 */
public class PictureSelectViewGroup extends ConstraintLayout {

    private TextView textViewTitle;
    private TextView textViewCurrentNumber;
    private TextView textViewSymbol;
    private TextView textViewMaxNumber;
    private ImageButton addButton;
    private RecyclerView pictureList;
    private PictureListAdapter pictureListAdapter;
    private int currentNumber;
    private int maxNumber;
    private Context context;

    public int getCurrentNumber() {
        return currentNumber;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    /**设置最大图片数*/
    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
        this.textViewMaxNumber.setText(String.valueOf(this.maxNumber));
    }

    public void setTitle(String title) {
        this.textViewTitle.setText(title);
    }

    public void setTextViewMaxNumber(TextView textViewMaxNumber) {
        this.textViewMaxNumber = textViewMaxNumber;
    }

    public PictureListAdapter getPictureListAdapter() {
        return pictureListAdapter;
    }

    public void setPictureListAdapter(PictureListAdapter pictureListAdapter) {
        this.pictureListAdapter = pictureListAdapter;
        pictureList.setAdapter(this.pictureListAdapter);
    }

    public PictureSelectViewGroup(@NonNull Context context) {
        super(context);
        this.context = context;
        initView(this.context);
    }

    public PictureSelectViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(this.context);
    }

    public PictureSelectViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(this.context);
    }

    public PictureSelectViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView(this.context);
    }

    /**初始化UI，根据业务需求设置默认值。*/
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.picture_select_view, this, true);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewTitle.setText(R.string.images);
        textViewCurrentNumber = (TextView) findViewById(R.id.textViewCurrentNumber);
        currentNumber = 0;
        textViewCurrentNumber.setText(String.valueOf(currentNumber));
        textViewSymbol = (TextView) findViewById(R.id.textViewSymbol);
        textViewSymbol.setText(R.string.symbol);
        textViewMaxNumber = (TextView) findViewById(R.id.textViewMaxNumber);
        setMaxNumber(0);
        addButton = (ImageButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            addPictureListener();
        });
        pictureList = (RecyclerView) findViewById(R.id.pictureList);
    }


    /**多选按钮点击事件*/
    private void addPictureListener() {
        //进入相册
        PictureSelector.create((Activity) context)
                // 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .openGallery(ofImage())
                // 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .theme(R.style.picture_default_style)
                // 最大图片选择数量
                .maxSelectNum(maxNumber-currentNumber)
                // 多选 or 单选
                .selectionMode( PictureConfig.MULTIPLE)
                // 是否显示拍照按钮
                .isCamera(false)
                // 图片列表点击 缩放效果 默认true
                .isZoomAnim(true)
                // 是否压缩
                .compress(true)
                // 同步true或异步false 压缩 默认同步
                .synOrAsy(true)
                // 压缩图片自定义保存地址
                .compressSavePath(getCompressPath())
                // glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .sizeMultiplier(0.5f)
                // glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                // .glideOverride(160, 160)
                //结果回调onActivityResult code
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    /**压缩后图片文件存储位置*/
    private String getCompressPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PictureSelector/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }
}
