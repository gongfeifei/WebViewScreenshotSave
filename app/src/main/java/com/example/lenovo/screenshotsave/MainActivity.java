package com.example.lenovo.screenshotsave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.webkit.WebView.enableSlowWholeDocumentDraw;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private static final String baiduUrl = "https://www.baidu.com/s?cl=3&tn=baidutop10&fr=top1000&wd=%E5%A7%9A%E6%98%8E%E5%BD%93%E9%80%89%E7%AF%AE%E5%8D%8F%E4%B8%BB%E5%B8%AD&rsv_idx=2";
    private Bitmap bitmap;
    private Activity context = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableSlowWholeDocumentDraw();
        setContentView(R.layout.activity_main);
        findView();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.screen:
                //截取webview整个页面
                    bitmap = captureScreen(mWebView);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(new String[]{"保存图片"}, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick (DialogInterface dialog, int which){
                        if(bitmap != null){
                            saveImageToGallery(MainActivity.this, bitmap);
                        }
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //指定保存路径
    public static void saveImageToGallery (Context context, Bitmap bmp){
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "webview");
        if(!appDir.exists()){
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);

        try{
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try{
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
    }

    private Bitmap captureScreen(WebView webView) {
        //获取webview的缩放率
        float scale = mWebView.getScale();
        //得到缩放后webview内容的高度
        int webViewHeight = (int) (mWebView.getContentHeight() * scale);
        //创建位图
        Bitmap bitmap = Bitmap.createBitmap(mWebView.getWidth(), webViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //绘制（会调用native方法，完成图形绘制）
        mWebView.draw(canvas);
        return bitmap;
    }

    private void init() {
        mWebView.loadUrl(baiduUrl);
        mWebView.setDrawingCacheEnabled(true);
        mWebView.setWebViewClient(new WebViewClient()); //主要帮助WebView处理各种通知、请求事件的
        mWebView.setWebChromeClient(new WebChromeClient()); //主要辅助WebView处理JavaScript的对话框、网站图标、网站title、加载进度等
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);  //对WebView设置JS可执行
        settings.setDomStorageEnabled(true);  //使DOM storage API可用
        settings.setUseWideViewPort(true);  //设置webview推荐使用的窗口
        settings.setLoadWithOverviewMode(true); //设置webview加载的页面的模式，
        // 这样可以让你的页面适应手机屏幕的分辨率，完整的显示在屏幕上，可以放大缩小

        //获取当前WebView的UA
        String ua = settings.getUserAgentString();
        //在当前UA字符的末尾增加app的标识和版本号等信息
        settings.setUserAgentString(ua + "APP_TAG/6.0");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void findView() {
        mWebView = (WebView) findViewById(R.id.webview);
    }
}
