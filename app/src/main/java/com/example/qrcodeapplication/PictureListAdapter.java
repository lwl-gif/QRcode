package com.example.qrcodeapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/4/6 14:25
 * @Modified By:
 */
public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> {

    private final String TAG = "PictureListAdapter";
    private String url = "http://192.168.1.101:8080/ul/api/reader/selectPictureByPictureName";
    private Context context;
//    /**图片uri*/
//    private ArrayList<String> pictureUri;
//    /**定义列表项包含的组件*/

    private LayoutInflater inflater;

    public ArrayList<Integer> getData() {
        return data;
    }

    private ArrayList<Integer> data;
    RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.load_default);
    /**对外提供一个接口，让Activity实现对item的单击和长按事件*/
    public interface  ItemClickListener{
        /**
         * @Author:Wallace
         * @Description:单击事件
         * @Date:Created in 16:06 2021/4/7
         * @Modified By:
         * @param position 点击的位置
         * @return:
         */
        void onItemClick(ArrayList<Integer> data, int position);
        /**
         * @Author:Wallace
         * @Description:长按事件
         * @Date:Created in 16:06 2021/4/7
         * @Modified By:
         * @param position 长按的位置
         * @return:
         */
        void  onItemLongClick(PictureListAdapter pictureListAdapter, int position);
    }

    private ItemClickListener itemClickListener;

    public PictureListAdapter(Context context, ItemClickListener itemClickListener, ArrayList<Integer> data) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public PictureListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureListAdapter.ViewHolder holder, int position) {
        GlideUrl glideUrl = new GlideUrl(url);
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(glideUrl)
                .into(holder.imageButton);
        String s = context.getResources().getResourceName(data.get(position));
        holder.name.setText(s);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addItem(int position, int drawable){
        data.add(position,drawable);
        notifyItemInserted(position);
    }

    public void removeItem(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton imageButton;
        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            name = itemView.findViewById(R.id.textView);
            imageButton.setOnLongClickListener(view -> {
                itemClickListener.onItemLongClick(PictureListAdapter.this,getLayoutPosition());
                return false;
            });
            imageButton.setOnClickListener(view -> {
                itemClickListener.onItemClick(data, getLayoutPosition());
            });
        }
    }
}
