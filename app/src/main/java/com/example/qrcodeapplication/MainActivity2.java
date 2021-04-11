package com.example.qrcodeapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PermissionGroupInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author luoweili
 */
public class MainActivity2 extends AppCompatActivity implements PictureListAdapter.ItemClickListener{

//        private List<Integer> data = new ArrayList<Integer>(Arrays.asList(R.drawable.meinv1));
    private ArrayList<Integer> data = new ArrayList<Integer>(){{

        }};

    private PictureSelectViewGroup pictureSelectViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        pictureSelectViewGroup = findViewById(R.id.pictureSelectViewGroup);
    }

    @Override
    protected void onStart() {
        super.onStart();
        pictureSelectViewGroup.setMaxNumber(5);
        pictureSelectViewGroup.setTitle("自定义的画廊");
        PictureListAdapter pictureListAdapter = new PictureListAdapter(this, this, data);
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        pictureSelectViewGroup.setPictureListAdapter(layoutManager,pictureListAdapter);
    }

    /**
     * @Author:Wallace
     * @Description: item的单击事件，大图展示此图
     * @Date:Created in 16:21 2021/4/7
     * @Modified By:
     * @param data Adapter的数据源
     * @param position 单击的位置
     * @return: void
     */
    @Override
    public void onItemClick(ArrayList<Integer> data,int position) {
        Intent intent = new Intent(MainActivity2.this, ShowPictureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList("adapterData", (ArrayList<Integer>) data);
        bundle.putInt("position",position);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * @Author:Wallace
     * @Description: item的长按事件，弹出对话框，选择“是”则删除图片
     * @Date:Created in 16:21 2021/4/7
     * @Modified By:
     * @param position 单击的位置
     * @return: void
     */
    @Override
    public void onItemLongClick(PictureListAdapter pictureListAdapter, int position) {
        DialogUtil.showDialog(this,pictureListAdapter,position);
    }

    @Override
    protected void onDestroy() {
        pictureSelectViewGroup = null;
        super.onDestroy();
    }
}
