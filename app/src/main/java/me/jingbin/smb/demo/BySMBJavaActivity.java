package me.jingbin.smb.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.List;

import me.jingbin.smb.BySMB;
import me.jingbin.smb.OnOperationFileCallback;
import me.jingbin.smb.OnReadFileListNameCallback;
import me.jingbin.smb.demo.databinding.ActivityMainBinding;

/**
 * Java版本
 */
public class BySMBJavaActivity extends AppCompatActivity {

    private MyHandle handle;
    public ProgressDialog progressDialog = null;
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // 可直接在Activity里初始化
        BySMB.initProperty("6000", "3000");

        setTitle("BySMB Java版");
        binding.etIp.setText(SpUtil.getString("ip"));
        binding.etUsername.setText(SpUtil.getString("username"));
        binding.etPassword.setText(SpUtil.getString("password"));
        binding.etFoldName.setText(SpUtil.getString("foldName"));
        binding.etContent.setText(SpUtil.getString("content"));
        binding.etFileName.setText(SpUtil.getString("contentFileName"));

        handle = new MyHandle(this);
        binding.tvSend.setOnClickListener(v -> {
            operation(1);
            saveEditValue();
        });
        binding.tvRead.setOnClickListener(v -> {
            operation(2);
            saveEditValue();
        });
        binding.tvDelete.setOnClickListener(v -> {
            operation(3);
            saveEditValue();
        });
    }


    private static class MyHandle extends Handler {

        private final WeakReference<BySMBJavaActivity> mWeakReference;

        public MyHandle(BySMBJavaActivity activity) {
            mWeakReference = new WeakReference<BySMBJavaActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BySMBJavaActivity activity = null;
            if (mWeakReference != null && mWeakReference.get() != null) {
                activity = mWeakReference.get();
            }
            if (activity != null) {
                if (activity.progressDialog != null) {
                    activity.progressDialog.hide();
                }
                switch (msg.what) {
                    case 1:
                        // 读取文件列表成功
                        List<String> list = (List<String>) msg.obj;
                        activity.binding.tvFileList.setText(list.toString());
                        break;
                    case 2:
                        // 写入或删除成功后再次读取
                        activity.binding.tvLog.setText(msg.obj.toString());
                        activity.operation(2);
                        break;
                    default:
                        // 失败
                        String obj = (String) msg.obj;
                        activity.binding.tvLog.setText(obj);
                        Toast.makeText(activity, obj, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

    }

    /**
     * 增加 查看 删除
     *
     * @param state 1写入 2查看 3删除
     */
    private void operation(int state) {
        showProgress(state);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BySMB bySMB = BySMB.with()
                            .setConfig(binding.etIp.getText().toString(),
                                    binding.etUsername.getText().toString(),
                                    binding.etPassword.getText().toString(),
                                    binding.etFoldName.getText().toString())
                            .setReadTimeOut(60L)
                            .setSoTimeOut(180)
                            .build();

                    switch (state) {
                        case 1:
                            // 写入
                            File file = writeStringToFile(BySMBJavaActivity.this,
                                    binding.etContent.getText().toString(),
                                    binding.etFileName.getText().toString());
                            bySMB.writeToFile(file, new OnOperationFileCallback() {
                                @Override
                                public void onSuccess() {
                                    // 成功
                                    Message msg = Message.obtain();
                                    msg.obj = "写入成功";
                                    msg.what = 2;
                                    handle.sendMessage(msg);
                                }

                                @Override
                                public void onFailure(@NotNull String message) {
                                    Message msg = Message.obtain();
                                    msg.obj = message;
                                    handle.sendMessage(msg);
                                }
                            });
                            break;
                        case 2:
                            // 读取 ("", "*.txt", callback)
                            bySMB.listShareFileName(new OnReadFileListNameCallback() {
                                @Override
                                public void onSuccess(@NotNull List<String> fileNameList) {
                                    // 成功
                                    Message msg = Message.obtain();
                                    msg.obj = fileNameList;
                                    msg.what = 1;
                                    handle.sendMessage(msg);
                                }

                                @Override
                                public void onFailure(@NotNull String message) {
                                    Message msg = Message.obtain();
                                    msg.obj = message;
                                    handle.sendMessage(msg);
                                }
                            });
                            break;
                        case 3:
                            // 删除
                            bySMB.deleteFile(binding.etFileName.getText().toString(), new OnOperationFileCallback() {
                                @Override
                                public void onSuccess() {
                                    // 成功
                                    Message msg = Message.obtain();
                                    msg.obj = "删除成功";
                                    msg.what = 2;
                                    handle.sendMessage(msg);
                                }

                                @Override
                                public void onFailure(@NotNull String message) {
                                    Message msg = Message.obtain();
                                    msg.obj = message;
                                    handle.sendMessage(msg);
                                }
                            });
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.obj = "连接失败： " + e.getMessage();
                    handle.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showProgress(int state) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        switch (state) {
            case 1:
                progressDialog.setMessage("上传中...");
                break;
            case 2:
                progressDialog.setMessage("读取中...");
                break;
            case 3:
                progressDialog.setMessage("删除中...");
                break;
        }
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveEditValue();
    }

    private void saveEditValue() {
        SpUtil.putString("ip", binding.etIp.getText().toString());
        SpUtil.putString("username", binding.etUsername.getText().toString());
        SpUtil.putString("password", binding.etPassword.getText().toString());
        SpUtil.putString("foldName", binding.etFoldName.getText().toString());
        SpUtil.putString("content", binding.etContent.getText().toString());
        SpUtil.putString("contentFileName", binding.etFileName.getText().toString());
    }

    /**
     * 在本地生成文件
     */
    private File writeStringToFile(Activity context, String content, String writeFileName) {
        File file = null;
        try {
            file = new File(context.getFilesDir(), writeFileName);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
            }
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            printStream.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}