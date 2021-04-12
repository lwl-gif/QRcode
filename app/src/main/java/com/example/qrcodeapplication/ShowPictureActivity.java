package com.example.qrcodeapplication;


import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Wallace
 * @Description: 展示图片大图的活动
 * @Date:Created in 16:25 2021/4/7
 * @Modified By:
 * @return:
 */
public class ShowPictureActivity extends Activity{

    private ImageButton imageButton;
    private TextView tvNow;
    private TextView tvAll;
    private ImageView imageView;
    private ArrayList<String> imagesPath;
    /**当前是第几张图片*/
    private int position;
    /**图片总数*/
    private int total;
    private RequestOptions requestOptions = new RequestOptions().error(R.mipmap.error2);
    /**定义手势监听对象*/
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_picture);
        imageButton = findViewById(R.id.imageButton_back);
        imageButton.setOnClickListener(view -> {
            finish();
        });
        imageView = findViewById(R.id.imageView);
        tvNow = findViewById(R.id.tv_now);
        tvAll = findViewById(R.id.tv_all);
        Bundle bundle = getIntent().getExtras();
        //获取数据
        ImagesAdapter imagesAdapter = (ImagesAdapter) bundle.getParcelable("imagesAdapter");
        imagesPath = imagesAdapter.getImagesPath();

        //获取点击位置
        position = bundle.getInt("position") + 1;
        tvNow.setText(String.valueOf(position));
        total = imagesPath.size() - 1;
        tvAll.setText(String.valueOf(total));
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position -1)).into(imageView);
        //设置手势监听由SimpleOnGestureListener处理
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            //当识别的手势是滑动手势时回调onFinger方法
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //得到手触碰位置的起始点和结束点坐标 x , y ，并进行计算
                float x = e2.getX() - e1.getX();
                float y = e2.getY() - e1.getY();
                //通过计算判断是向左还是向右滑动
                if (x < 0) {
                    //向右
                    if (position < total) {
                        position++;
                        tvNow.setText(String.valueOf(position));
                        Glide.with(ShowPictureActivity.this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position - 1)).into(imageView);
                    } else {
                        Toast.makeText(ShowPictureActivity.this, "已经是最后一张了！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //向左
                    if (position > 1) {
                        position--;
                        tvNow.setText(String.valueOf(position));
                        Glide.with(ShowPictureActivity.this).applyDefaultRequestOptions(requestOptions).load(imagesPath.get(position - 1)).into(imageView);
                    } else {
                        Toast.makeText(ShowPictureActivity.this, "已经是第一张了！", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    /**当Activity被触摸时回调*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageButton = null;
        tvNow = null;
        tvAll = null;
        imageView = null;
        imagesPath.clear();
        position = 0;
        requestOptions = null;
    }
}