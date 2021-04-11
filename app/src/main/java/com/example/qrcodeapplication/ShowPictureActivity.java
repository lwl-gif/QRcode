package com.example.qrcodeapplication;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
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
public class ShowPictureActivity extends Activity implements ShowPictureAdapter.ItemClickListener{

    private ImageButton imageButton;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ArrayList<Integer> data;
    RequestOptions requestOptions = new RequestOptions().centerCrop();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_picture);
        imageButton = findViewById(R.id.imageButton_back);
        imageButton.setOnClickListener(view -> {
            Intent intent = new Intent(ShowPictureActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        });
        imageView = findViewById(R.id.imageView);
        recyclerView = findViewById(R.id.recyclerView2);
        Bundle bundle = getIntent().getExtras();
        //获取数据源
        data = bundle.getIntegerArrayList("adapterData");
        //获取点击位置
        int position = bundle.getInt("position");
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(data.get(position)).into(imageView);
        ShowPictureAdapter showPictureAdapter = new ShowPictureAdapter(this,this,data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(showPictureAdapter);
    }

    @Override
    public void onItemClick(ArrayList<Integer> data, int position) {
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(data.get(position)).into(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageButton = null;
        imageView = null;
        recyclerView = null;
        data.clear();
    }
}