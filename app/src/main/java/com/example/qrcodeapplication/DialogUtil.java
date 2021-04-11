package com.example.qrcodeapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luoweili
 */
public class DialogUtil {

    /**定义一个图片删除的对话框*/
    public static void showDialog(Context context , PictureListAdapter pictureListAdapter , int position) {
        String title = "您确定要删除此图吗？";
        StringBuilder buffer = new StringBuilder("图片：");
        String name = context.getResources().getResourceName(pictureListAdapter.getData().get(position));
        String msg = buffer.append(name).toString();
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setMessage(msg).setCancelable(false);
        builder.setPositiveButton("确定", (dialog, which) -> {
            pictureListAdapter.removeItem(position);
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }

    /**定义一个图片删除的对话框*/
    public static void showDialog(Context context , ImagesAdapter imagesAdapter, int position) {
        //先得到构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //设置标题
        builder.setTitle("删除提示");
        //设置内容
        String msg = "您确定要删除它：" + imagesAdapter.getImagesPath().get(position);
        builder.setMessage(msg);
        //设置图标，图片id即可
        builder.setIcon(R.mipmap.ic_launcher);
        //设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imagesAdapter.removeItem(position);
                imagesAdapter.setFirstDelete(false);
                dialog.dismiss(); //关闭dialog
            }
        });
        //设置取消按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imagesAdapter.setFirstDelete(false);
                dialog.dismiss();
            }
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }
}