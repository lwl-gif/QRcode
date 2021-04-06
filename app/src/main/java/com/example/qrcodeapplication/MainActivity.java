package com.example.qrcodeapplication;


import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

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
 * @author luoweili
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    //所选相册图片的路径(原图/压缩后/剪裁后)
    String albumPath = "";
    //用来转换相机路径用的
    LocalMedia localMedia = new LocalMedia();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * picSelector的相册相机界面
     *
     * // 例如 LocalMedia 里面返回三种 path
     *                     // 1.media.getPath(); 为原图 path
     *                     // 2.media.getCutPath();为裁剪后 path，需判断 media.isCut();是否为 true
     *                     // 3.media.getCompressPath();为压缩后 path，需判断 media.isCompressed();是否为 true
     *                     // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
     *
     */
    protected void showPictureSelectDialog() {
        // 进入相册 以下是例子：不需要的api可以不写
        PictureSelector.create(MainActivity.this)
                .openGallery(ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(R.style.picture_default_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(1)// 最大图片选择数量
                .selectionMode( PictureConfig.SINGLE)// 多选 or 单选
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .compress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .compressSavePath(getCompressPath())//压缩图片自定义保存地址
                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 如果返回码是可以用的
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    if(PictureSelector.obtainMultipleResult(data).get(0) != null)
                    {
                        // 图片选择结果回调
                        localMedia = PictureSelector.obtainMultipleResult(data).get(0);
                        if(localMedia.isCompressed())
                        {
                            albumPath = localMedia.getCompressPath();
                            Log.e(TAG, "onActivityResult: "+ albumPath);

                            //设置图片圆角角度
                            RoundedCorners roundedCorners = new RoundedCorners(30);
                            //通过RequestOptions扩展功能
                            RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(300, 300)
                                    //圆形
                                    .circleCrop();
//                            Glide.with(this)
//                                    .load(albumPath).apply(options).into(img_face);
                        }
                    }
                    break;
                default:
            }
        }
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

    @Override
    public void onClick(View view) {

    }
}
