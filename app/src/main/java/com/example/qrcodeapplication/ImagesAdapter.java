package com.example.qrcodeapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/4/11 19:20
 * @Modified By:
 */
public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    /**添加按钮的图片路径*/
    private String path;
    private List<LocalMedia> selectList;
    private ArrayList<String> imagesPath = new ArrayList<>();
    private ItemListener itemListener;
    private final RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.placeholder).centerCrop().error(R.drawable.ic_camera);
    /**当前是否处于删除状态*/
    private boolean deleting = false;
    /**当前是否第一次删除图片*/
    private boolean firstDelete = true;

    public ImagesAdapter(Context context,ItemListener itemListener,List<LocalMedia> selectList){
        this.context = context;
        this.itemListener = itemListener;
        this.selectList = selectList;
        Resources resources = context.getResources();
        path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.drawable.add_image) + "/"
                + resources.getResourceTypeName(R.drawable.add_image) + "/"
                + resources.getResourceEntryName(R.drawable.add_image);
        setSelectList(selectList);
    }

    public List<LocalMedia> getSelectList() {
        return selectList;
    }

    public void setSelectList(List<LocalMedia> selectList) {
        this.selectList = selectList;
        imagesPath.clear();
        for(LocalMedia localMedia:selectList){
            imagesPath.add(localMedia.getPath());
        }
        imagesPath.add(path);
        notifyDataSetChanged();
    }

    public ArrayList<String> getImagesPath() {
        return imagesPath;
    }

    public boolean isDeleting() {
        return deleting;
    }

    public void setDeleting(boolean deleting) {
        this.deleting = deleting;
    }

    public boolean isFirstDelete() {
        return firstDelete;
    }

    public void setFirstDelete(boolean firstDelete) {
        this.firstDelete = firstDelete;
    }

    @NonNull
    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.image_tiem, null);
        return new ImagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesAdapter.ViewHolder holder, int position) {

        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .load(imagesPath.get(position))
                .into(holder.imageButton);
    }

    @Override
    public int getItemCount() {
        return this.imagesPath.size();
    }

/*    public void addItem(int position, String imagePath){
        imagesPath.add(position,imagePath);
        notifyItemInserted(position);
    }*/

    public void removeItem(int position){
        selectList.remove(position);
        imagesPath.remove(position);
        notifyItemRemoved(position);
    }

    /**列表项的监听器*/
    public interface ItemListener{
        /**单击展示图片时*/
        public void onClickToShow(int position);
        /**单击删除图片时*/
        public void onClickToDelete(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton imageButton;
        private ImageButton imageDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            imageButton.setOnClickListener(view -> {
                itemListener.onClickToShow(getLayoutPosition());
            });
            imageButton.setOnLongClickListener(view -> {
                ImagesAdapter.this.deleting = !ImagesAdapter.this.deleting;
                ImagesAdapter.this.notifyItemRangeChanged(0,ImagesAdapter.this.selectList.size());
                return false;
            });
            imageDelete = itemView.findViewById(R.id.image_delete);
            //如果不是最后一个item
            if(getLayoutPosition() != ImagesAdapter.this.getItemCount()-1){
                //当前处于删除图片状态，显示删除的图标
                if(ImagesAdapter.this.deleting){
                    imageDelete.setVisibility(View.VISIBLE);
                }else {
                    imageDelete.setVisibility(View.GONE);
                }
                imageDelete.setOnClickListener(view -> {
                    itemListener.onClickToDelete(getLayoutPosition());
                });
            }
            //如果是最后一个item，删除图标永不显示
            else {
                imageDelete.setVisibility(View.GONE);
            }
        }
    }
}
