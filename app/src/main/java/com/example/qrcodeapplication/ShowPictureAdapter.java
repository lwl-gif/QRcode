package com.example.qrcodeapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/4/7 17:28
 * @Modified By:
 */
public class ShowPictureAdapter extends RecyclerView.Adapter<ShowPictureAdapter.ViewHolder>{
    
    private final String TAG = "ShowPictureAdapter";
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Integer> data;
    RequestOptions requestOptions = new RequestOptions().centerCrop();
    
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
    }

    private ItemClickListener itemClickListener;

    public ShowPictureAdapter(Context context, ItemClickListener itemClickListener, ArrayList<Integer> data) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ShowPictureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item, parent, false);
        return new ShowPictureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowPictureAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(data.get(position))
                .into(holder.imageButton);
        String s = context.getResources().getResourceName(data.get(position));

        holder.imageButton.setOnClickListener(view -> {
            itemClickListener.onItemClick(data, position);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton imageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
        }
    }
}
