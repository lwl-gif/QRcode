package com.example.sendtalk.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.sendtalk.R;
import com.example.sendtalk.adapter.ImagesAdapter;

/**
 * @author luoweili
 */
public class DialogUtil {

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

    /**定义一个显示指定内容的对话框*/
    public static void showDialog(Context context, String tag, Bundle data){
        String code = data.getString("code");
        String message = data.getString("message");
        String tip = data.getString("tip");
        View view = View.inflate(context, R.layout.dialog_view,null);
        TextView tvFrom = view.findViewById(R.id.dialog_from);
        tvFrom.setText(tag);
        TextView tvCode = view.findViewById(R.id.dialog_code);
        tvCode.setText(code);
        TextView tvMessage = view.findViewById(R.id.dialog_message);
        tvMessage.setText(message);
        TextView tvTip = view.findViewById(R.id.dialog_tip);
        tvTip.setText(tip);
        //创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);
        builder.setPositiveButton("确定", null);
        builder.create().show();
    }
}