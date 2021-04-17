package com.example.sendtalk.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sendtalk.R;
import com.example.sendtalk.adapter.ImagesAdapter;
import com.example.sendtalk.util.DialogUtil;
import com.example.sendtalk.util.HttpUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import org.json.JSONArray;
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
public class MainActivity2 extends Activity implements ImagesAdapter.ItemListener, HttpUtil.MyCallback{

    private static final String TAG = "MainActivity2";
    //自定义请求代码
    /**未知错误*/
    private static final int UNKNOWN_REQUEST_ERROR = 100;
    /**请求失败*/
    private static final int REQUEST_FAIL = 1000;
    /**请求成功，但子线程解析数据失败*/
    private static final int REQUEST_BUT_FAIL_READ_DATA = 1001;
    /**更新说说*/
    private static final int UPDATE_TALK = 110;
    /**获取说说*/
    private static final int GET_TALK = 120;
    /**获取说说失败*/
    private static final int GET_TALK_FAIL = 1200;
    /**获取说说成功*/
    private static final int GET_TALK_SUCCEED = 1201;

    static class MyHandler extends Handler {
        private WeakReference<MainActivity2> mainActivityWeakReference;

        public MyHandler(WeakReference<MainActivity2> mainActivityWeakReference) {
            this.mainActivityWeakReference = mainActivityWeakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Bundle bundle = msg.getData();
            MainActivity2 myActivity = mainActivityWeakReference.get();
            if (what == REQUEST_FAIL) {
                Toast.makeText(myActivity, bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (what == REQUEST_BUT_FAIL_READ_DATA) {
                Toast.makeText(myActivity, "子线程解析数据异常！", Toast.LENGTH_SHORT).show();
            } else if(what == GET_TALK_FAIL){
                DialogUtil.showDialog(myActivity,TAG,bundle);
            } else if(what == GET_TALK_SUCCEED){
                //调用方法初始化数据
                myActivity.initData();
                Toast.makeText(myActivity,  "数据获取成功", Toast.LENGTH_SHORT).show();
            } else  {
                Toast.makeText(myActivity,  bundle.getString("reason"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MyHandler myHandler = new MyHandler(new WeakReference(this));

    /**url+图片名构成的访问服务器获取图片的GlideUrl*/
    private ArrayList<String> imageNameUrlList = new ArrayList<>();
    private String text = null;
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
                    btnSubmit.setTextColor(ContextCompat.getColor(MainActivity2.this,R.color.gray));
                }else {
                    btnSubmit.setClickable(true);
                    btnSubmit.setTextColor(ContextCompat.getColor(MainActivity2.this,R.color.blue));
                }
                MainActivity2.this.text = MainActivity2.this.editText.getText().toString().trim();
            }
        });
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setClickable(false);
        btnSubmit.setTextColor(ContextCompat.getColor(this,R.color.gray));
        btnSubmit.setOnClickListener(view -> {
            String url = HttpUtil.BASE_URL + "reader/" + "test";
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("text",this.text);
            List<String> list = this.imagesAdapter.getImagesPath();
            List<String> tempList = list.subList(0,list.size()-1);
            HttpUtil.postRequest(null,url,hashMap,tempList,this, UPDATE_TALK);
            Toast.makeText(this,"正在更新您的说说...",Toast.LENGTH_LONG).show();
        });
        Button btnLast = findViewById(R.id.btn_last);
        btnLast.setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //初始化
        imagesAdapter = new ImagesAdapter(this,this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3 );
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(this.text == null && this.imageNameUrlList.size() == 0){
            //发一次获取数据的请求
            String url = HttpUtil.BASE_URL + "reader/" + "test/getData";
            HttpUtil.getRequest(null, url, this, GET_TALK);
        }
    }

    /**
     * @Author: Wallace
     * @Description: 从服务器端获取数据成功后，给组件赋值，显示数据
     * @Date: Created 20:59 2021/4/13
     * @Modified: by who yyyy-MM-dd
     * @return: void
     */
    private void initData() {
        this.editText.setText(this.text);
        this.imagesAdapter.setImageNameUrlList(this.imageNameUrlList);
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
                Intent intent = new Intent(MainActivity2.this,ShowPictureActivity.class);
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
                    ArrayList<LocalMedia> selectList = (ArrayList<LocalMedia>) PictureSelector.obtainMultipleResult(data);
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
            case UPDATE_TALK:
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
                    message.what = UPDATE_TALK;
                    myHandler.sendMessage(message);
                } catch (JSONException e) {
                    message.setData(bundle);
                    message.what = REQUEST_BUT_FAIL_READ_DATA;
                    myHandler.sendMessage(message);
                }
                break;
            case GET_TALK:
                try {
                    jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    String tip = jsonObject.getString("tip");
                    String cod = jsonObject.getString("code");
                    bundle.putString("code",cod);
                    bundle.putString("tip",tip);
                    bundle.putString("message",msg);
                    message.setData(bundle);
                    message.what = GET_TALK_FAIL;
                    //如果获取成功，则解析服务器端传来的数据
                    if("获取成功！".equals(msg)){
                        JSONObject jsonData =  jsonObject.getJSONObject("dataObject");
                        this.text = jsonData.getString("text");
                        //所有图片的名称
                        JSONArray jsonArray = jsonData.getJSONArray("filenames");
                        String url = HttpUtil.BASE_URL + "reader" + "/test/getImage/";
                        //将图片名称变成url装进列表里
                        this.imageNameUrlList.clear();
                        for(int i = 0 ; i < jsonArray.length() ; i++){
                            this.imageNameUrlList.add(url+jsonArray.getString(i));
                        }
                        message.what = GET_TALK_SUCCEED;
                    }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.text = null;
        this.editText.setText(text);
        this.imageNameUrlList.clear();
        Glide.get(this).clearMemory();
    }
}
