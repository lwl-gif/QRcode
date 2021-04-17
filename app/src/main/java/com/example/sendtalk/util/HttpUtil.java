package com.example.sendtalk.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.example.sendtalk.config.ThreadPoolConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author luoweili
 */
public class HttpUtil {
    public static final String BASE_URL = "http://192.168.1.100:8080/ul/api/";
    /**
     * 创建线程池
     */
    public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            ThreadPoolConfig.CORE_POOL_SIZE,
            ThreadPoolConfig.MAXIMUM_POOL_SIZE,
            ThreadPoolConfig.KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            ThreadPoolConfig.WORK_QUEUE,
            ThreadPoolConfig.threadFactory,
            ThreadPoolConfig.rejectHandler
    );
    /**
     * 创建默认的OkHttpClient对象
     */
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                private final Map<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
                    cookieStore.put(httpUrl, list);
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
                    List<Cookie> cookies = cookieStore.get(httpUrl);
                    return cookies == null ? new ArrayList<>() : cookies;
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String newUrl(String url, HashMap<String, String> hashMap) {
        /**
         * @Author:Wallace
         * @Description:发送GET和DELETE请求时，url拼接参数形成新的url
         * @Date:Created in 22:27 2021/4/1
         * @Modified By:
         * @param url  请求地址
         * @param hashMap   请求原本要携带的参数
         * @return: java.lang.String    新的url
         */
        // 拼接请求参数
        StringBuffer buffer = new StringBuffer(url);
        buffer.append('?');
        for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append('=');
            buffer.append(entry.getValue());
            buffer.append('&');
        }
        buffer.deleteCharAt(buffer.length() - 1);
        url = buffer.toString();
        return url;
    }


    /**
     * GET方法
     */
    public static void getRequest(String authorization, String url, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            //创建请求对象
            Request request;
            Request.Builder builder = new Request.Builder();
            builder.method("GET", null);
            if (authorization != null && authorization.length() > 0) {
                request = builder.addHeader("Authorization", authorization).url(url).build();
            } else {
                request = builder.url(url).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.failed(e, code);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    callback.success(response, code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * POST 方法
     */
    public static void postRequest(String authorization, String url, HashMap<String, String> params, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            MediaType multiPartFormData = MediaType.parse("multipart/form-data; charset=utf-8");
            MultipartBody.Builder multiBuilder = new MultipartBody.Builder();
            assert multiPartFormData != null;
            multiBuilder.setType(multiPartFormData);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multiBuilder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(params.get(entry.getKey()), null)
                );
            }
            RequestBody multiBody = multiBuilder.build();
            Request request;
            Request.Builder rBuilder = new Request.Builder();
            if (authorization != null && authorization.length() > 0) {
                request = rBuilder.addHeader("Authorization", authorization).url(url).post(multiBody).build();
            } else {
                request = rBuilder.url(url).post(multiBody).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.failed(e, code);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    callback.success(response, code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param token      所携带的token
     * @param url        请求地址
     * @param params     请求参数（键值对）
     * @param imagesPath 所携带的图片全路径
     * @param callback   回调接口
     * @param code       请求代码
     * @Author: Wallace
     * @Description: POST方法, 可传图片
     * 先添加请求参数，后添加图片
     * @Date: Created in 21:56 2021/4/12
     * @Modified By:
     * @return: void
     */
    public static void postRequest(String token, String url, HashMap<String, String> params, List<String> imagesPath, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            // {我的理解:每一对键值对都是一个分区，因为后端通过键名(@RequestParam(value = "key"))就可以取到对于的值了，
            // 所以没必要特意指明这块分区的name;
            // 但是上传文件时，即可以把所有文件放在同一个分区，也可以把每一个文件都放在独立的不同的分区，
            // 存放文件的每个分区都要指明分区的name}
            // 创建表单类型的分区请求体
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            //先把键值对放入表单
            for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
                String key = (String) stringStringEntry.getKey();
                String value = (String) stringStringEntry.getValue();
                //把键值对添加到表单
                builder.addFormDataPart(key, value);
            }
            //遍历传入的文件地址，获取文件
            for (String s : imagesPath) {
                File file = new File(s);
                /*把所有的文件都添加到“images”这块区域里面。
                  第一个参数：在请求体中该分区的名称，后端可根据这个名称(@RequestParam(value = "images"))获取这块区域中的所有文件
                  第二个参数：文件名（不知道有什么意义）
                  第三个参数：指定文件的类型（image/*表示为图片类型,image/.png则是进一步指定为png格式的图片）
                 */
                builder.addFormDataPart("images", file.getName(), RequestBody.Companion.create(file, MediaType.parse("image/*")));
            }
            //构建请求体
            RequestBody requestBody = builder.build();
            //声明http请求
            Request request;
            //构建http请求
            Request.Builder rBuilder = new Request.Builder();
            if (token != null && token.length() > 0) {
                request = rBuilder.addHeader("Authorization", token).url(url).post(requestBody).build();
            } else {
                request = rBuilder.url(url).post(requestBody).build();
            }
            //异步请求
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.failed(e, code);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    callback.success(response, code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * DELETE方法
     */
    public static void deleteRequest(String Authorization, String url, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            //创建请求对象
            Request request;
            Request.Builder builder = new Request.Builder();
            builder.method("DELETE", null);
            if (Authorization != null && Authorization.length() > 0) {
                request = builder.addHeader("Authorization", Authorization).url(url).build();
            } else {
                request = builder.url(url).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e, code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response, code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * PUT 方法
     */
    public static void putRequest(String Authorization, String url, HashMap<String, String> params, MyCallback callback, int code) {
        FutureTask<String> task = new FutureTask<>(() -> {
            MediaType MultiPart_Form_Data = MediaType.parse("multipart/form-data; charset=utf-8");
            MultipartBody.Builder multiBuilder = new MultipartBody.Builder();
            assert MultiPart_Form_Data != null;
            multiBuilder.setType(MultiPart_Form_Data);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multiBuilder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(params.get(entry.getKey()), null)
                );
            }
            RequestBody multiBody = multiBuilder.build();
            Request request;
            Request.Builder rBuilder = new Request.Builder();
            if (Authorization != null && Authorization.length() > 0) {
                request = rBuilder.addHeader("Authorization", Authorization).url(url).put(multiBody).build();
            } else {
                request = rBuilder.url(url).put(multiBody).build();
            }
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failed(e, code);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.success(response, code);
                }
            });
            return null;
        });
        //提交任务
        threadPool.submit(task);
        try {
            task.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface MyCallback {
        void success(Response response, int code) throws IOException;

        void failed(IOException e, int code);
    }

    /**
     * @param context 网络图片加载的上下文
     * @param url     网络图片的url
     * @Author: Wallace
     * @Description: 获取图片的缓存路径，有.submit()会引起阻塞，需要在自线程中使用该方法
     * @Date: Created 10:20 2021/4/16
     * @Modified: by who yyyy-MM-dd
     * @return: java.lang.String 返回图片的缓存路径
     */
    public static String getImgCachePath(Context context, String url) {
        try {
            FutureTarget target = Glide.with(context)
                    .downloadOnly()
                    .load(url)
                    .submit();
            File file = (File) target.get(8, TimeUnit.SECONDS);
            String path = file.getAbsolutePath();
            return path;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return null;
        }
    }

    /**
     * 判断请求是否被服务器拦截
     */
    public static boolean requestIsIntercepted(JSONObject jsonObject) {
        try {
            String tip = jsonObject.getString("tip");
            String r = "请求被拦截！";
            if (r.equals(tip)) {
                return true;
            }
        } catch (JSONException e) {
            return false;
        }
        return false;
    }
}

