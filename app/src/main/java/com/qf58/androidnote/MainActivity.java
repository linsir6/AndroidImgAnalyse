package com.qf58.androidnote;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(30000L, TimeUnit.MILLISECONDS)
                .readTimeout(30000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Album.image(MainActivity.this) // 选择图片。
                        .multipleChoice()
                        .requestCode(200)
                        .camera(true)
                        .columnCount(3)
                        .selectCount(1)
                        .onResult(new Action<ArrayList<AlbumFile>>() {
                            @Override
                            public void onAction(int requestCode, @NonNull ArrayList<AlbumFile> result) {

                                ToastUtils.showShort("图片已选择完毕，准备开始压缩...");

                                File file = new File(result.get(0).getPath());

                                Luban.with(MainActivity.this)
                                        .load(file)                                   // 传入要压缩的图片列表
                                        .ignoreBy(100)                                  // 忽略不压缩图片的大小
                                        .setTargetDir(Environment.getExternalStorageDirectory().getAbsolutePath())                        // 设置压缩后文件存储位置
                                        .setCompressListener(new OnCompressListener() { //设置回调
                                            @Override
                                            public void onStart() {


                                            }

                                            @Override
                                            public void onSuccess(File file) {
                                                ToastUtils.showShort("图片已压缩完毕，准备发送给后台...");

                                                OkHttpUtils.post()//
                                                        .addFile("file", "messenger_01.png", file)//
                                                        .url("http://119.28.180.234:3000")
                                                        .build()//
                                                        .execute(new StringCallback() {
                                                            @Override
                                                            public void onError(Call call, Exception e, int id) {
                                                                LogUtils.e("lin", "---lin---> " + e.toString());
                                                                ToastUtils.showShort("网络出现问题...");

                                                            }

                                                            @Override
                                                            public void onResponse(String response, int id) {
                                                                LogUtils.e("lin", "---lin---> " + response);
                                                                try {
                                                                    JSONObject jsonObject = new JSONObject(response);
                                                                    JSONObject jsonObject2 = (JSONObject) jsonObject.getJSONArray("result").get(0);
                                                                    String name = jsonObject2.getString("name");

                                                                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                                                                    intent.putExtra("key", name);
                                                                    startActivity(intent);
                                                                    ToastUtils.showShort("图片已分析完成...");


                                                                } catch (Exception e) {
                                                                    ToastUtils.showShort("图片分析失败...");

                                                                }
                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e("lin", "---lin---> 压缩异常 " + e.toString());
                                            }
                                        })
                                        .launch();    //启动压缩


                                LogUtils.e("---lin--->" + file.isFile() + file.length());


                            }
                        })
                        .onCancel(new Action<String>() {
                            @Override
                            public void onAction(int requestCode, @NonNull String result) {

                            }
                        })
                        .start();


            }
        });

    }



}
