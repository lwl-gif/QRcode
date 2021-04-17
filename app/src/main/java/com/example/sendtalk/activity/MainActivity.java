package com.example.sendtalk.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendtalk.R;
import com.example.sendtalk.adapter.ImagesAdapter;
import com.example.sendtalk.util.DialogUtil;
import com.example.sendtalk.util.HttpUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

/**
 * @author luoweili
 */
public class MainActivity extends Activity implements ImagesAdapter.ItemListener, HttpUtil.MyCallback{

    private static final String TAG = "MainActivity";
    //自定义请求代码
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 100;
    /**请求失败*/
    private static final int REQUEST_FAIL = 1000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 1001;
    /**发说说*/
    private static final int SEND_TALK = 110;

    static class MyHandler extends Handler {
        private WeakReference<MainActivity> mainActivityWeakReference;

        public MyHandler(WeakReference<MainActivity> mainActivityWeakReference) {
            this.mainActivityWeakReference = mainActivityWeakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Bundle bundle = msg.getData();
            MainActivity myActivity = mainActivityWeakReference.get();
             if (what == REQUEST_FAIL) {
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            } else if(what == SEND_TALK){
                 DialogUtil.showDialog(myActivity,TAG,bundle);
            } else  {
                Toast.makeText(myActivity,  bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MyHandler myHandler = new MyHandler(new WeakReference(this));

    /**每次已选的图片*/
    private ArrayList<LocalMedia> selectList = new ArrayList<>();
    private Button btnSubmit;
    private EditText editText;
    private ImagesAdapter imagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(view -> {
            finish();
        });
        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0){
                    btnSubmit.setClickable(false);
                    btnSubmit.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.gray));
                }else {
                    btnSubmit.setClickable(true);
                    btnSubmit.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.blue));
                }
            }
        });
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setClickable(false);
        btnSubmit.setTextColor(ContextCompat.getColor(this,R.color.gray));
        btnSubmit.setOnClickListener(view -> {
            String url = HttpUtil.BASE_URL + "reader/" + "test";
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("text",editText.getText().toString().trim());
            List<String> list = this.imagesAdapter.getImagesPath();
            List<String> tempList = list.subList(0,list.size()-1);
            HttpUtil.postRequest(null,url,hashMap,tempList,this,SEND_TALK);
            Toast.makeText(this,"正在发表您的说说...",Toast.LENGTH_LONG).show();
        });
        Button btnLast = findViewById(R.id.btn_last);
        btnLast.setOnClickListener(view -> {
            //跳转到新的活动
            Intent intent = new Intent(MainActivity.this,MainActivity2.class);
            startActivity(intent);
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        imagesAdapter = new ImagesAdapter(this,this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3 );
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(this.imagesAdapter.getSelectList().size() == 0){
            imagesAdapter.setSelectList(this.selectList);
        }
    }

    @Override
    public void onClickToShow(int position) {
        /**
         * @Author:Wallace
         * @Description: 先判断是不是最后一个item
         * 1.不是最后一个item,则开启一个Activity，用大图来展示当前的图片
         * 2.是最后一个item，则选择图片添加
         * @Date:Created in 21:41 2021/4/11
         * @Modified By:
         * @param position item的位置
         * @return: void
         */
        // 当前不处于删除状态
        if(!imagesAdapter.getDeleting()){
            if(position == imagesAdapter.getItemCount()-1) {
                //进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(this)
                        //全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .openGallery(PictureMimeType.ofImage())
                        //每行显示个数 int
                        .imageSpanCount(3)
                        .maxSelectNum(30)
                        //多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .selectionMode(PictureConfig.MULTIPLE)
                        //是否可预览图片
                        .previewImage(true)
                        //是否显示拍照按钮 true or false
                        .isCamera(false)
                        //拍照保存图片格式后缀,默认jpeg
                        .imageFormat(PictureMimeType.JPEG)
                        //图片列表点击 缩放效果 默认true
                        .isZoomAnim(true)
                        //int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .withAspectRatio(1, 1)
                        //是否显示uCrop工具栏，默认不显示 true or false
                        .hideBottomControls(false)
                        //裁剪框是否可拖拽 true or false
                        .freeStyleCropEnabled(false)
                        //是否圆形裁剪 true or false
                        .circleDimmedLayer(false)
                        //是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
                        .showCropFrame(false)
                        //是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
                        .showCropGrid(false)
                        //是否开启点击声音 true or false
                        .openClickSound(true)
                        //是否传入已选图片 List<LocalMedia> list
//                    .selectionMedia(this.imagesAdapter.getSelectList())
                        //同步true或异步false 压缩 默认同步
                        .synOrAsy(true)
                        //裁剪是否可旋转图片 true or false
                        .rotateEnabled(false)
                        //裁剪是否可放大缩小图片 true or false
                        .scaleEnabled(true)
                        //是否可拖动裁剪框(固定)
                        .isDragFrame(false)
                        //结果回调onActivityResult requestCode
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
            else {
                Intent intent = new Intent(MainActivity.this,ShowPictureActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("imagesAdapter", imagesAdapter);
                bundle.putInt("position",position);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 结果回调
                    selectList.clear();
                    selectList = (ArrayList<LocalMedia>) PictureSelector.obtainMultipleResult(data);
                    // 重新设置数据源，刷新item
                    imagesAdapter.setSelectList(selectList);
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onClickToDelete(int position) {
        // 当前处于删除状态
        if(imagesAdapter.getDeleting()){
            // 如果当前是第一次删除图片，弹出提示框
            if(imagesAdapter.isFirstDelete()){
                DialogUtil.showDialog(this,this.imagesAdapter,position);
            }else {
                imagesAdapter.removeItem(position);
            }
        }
    }

    @Override
    public void success(Response response, int code) throws IOException {
        //获取服务器响应字符串
        String result = response.body().string().trim();
        Message message = new Message();
        Bundle bundle = new Bundle();
        switch (code) {
            case SEND_TALK:
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    String tip = jsonObject.getString("tip");
                    String cod = jsonObject.getString("code");
                    bundle.putString("code",cod);
                    bundle.putString("tip",tip);
                    bundle.putString("message",msg);
                    message.setData(bundle);
                    message.what = SEND_TALK;
                    myHandler.sendMessage(message);
                } catch (JSONException e) {
                    message.setData(bundle);
                    message.what = REQUEST_BUT_FAIL_READ_DATA;
                    myHandler.sendMessage(message);
                }
                break;
            default:
                myHandler.sendEmptyMessage(UNKNOWN_REQUEST_ERROR);
        }
    }

    @Override
    public void failed(IOException e, int code) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        String reason = null;
        if (e instanceof SocketTimeoutException) {
            reason = "连接超时";
            message.what = REQUEST_FAIL;
        } else if (e instanceof ConnectException) {
            reason = "连接服务器失败";
            message.what = REQUEST_FAIL;
        } else if (e instanceof UnknownHostException) {
            reason = "网络异常";
            message.what = REQUEST_FAIL;
        } else {
            reason = "未知错误";
            message.what = UNKNOWN_REQUEST_ERROR;
        }
        bundle.putString("reason",reason);
        message.setData(bundle);
        myHandler.sendMessage(message);
    }
}
