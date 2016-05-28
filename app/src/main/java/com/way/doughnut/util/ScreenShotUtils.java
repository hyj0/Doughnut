package com.way.doughnut.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.io.*;
import java.net.Socket;

/**
 * Created by dell-pc on 2016/5/22.
 */
public class ScreenShotUtils {
    /**
     * 进行截取屏幕
     * @param pActivity
     * @return bitmap
     */
    public static Bitmap takeScreenShot(Activity pActivity)
    {
        Bitmap bitmap=null;
        View view=pActivity.getWindow().getDecorView();
        // 设置是否可以进行绘图缓存
        view.setDrawingCacheEnabled(true);
        // 如果绘图缓存无法，强制构建绘图缓存
        view.buildDrawingCache();
        // 返回这个缓存视图
        bitmap=view.getDrawingCache();

        // 获取状态栏高度
        Rect frame=new Rect();
        // 测量屏幕宽和高
        view.getWindowVisibleDisplayFrame(frame);
        int stautsHeight=frame.top;
        Log.d("jiangqq", "状态栏的高度为:"+stautsHeight);

        int width=pActivity.getWindowManager().getDefaultDisplay().getWidth();
        int height=pActivity.getWindowManager().getDefaultDisplay().getHeight();
        // 根据坐标点和需要的宽和高创建bitmap
        bitmap=Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height-stautsHeight);
        return bitmap;
    }


    /**
     * 保存图片到sdcard中
     * @param pBitmap
     */
    private static boolean savePic(Bitmap pBitmap,String strName)
    {
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(strName);
            if(null!=fos)
            {
                pBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 截图
     * @param pActivity
     * @return 截图并且保存sdcard成功返回true，否则返回false
     */
    public static boolean shotBitmap(Activity pActivity)
    {

        return  ScreenShotUtils.savePic(takeScreenShot(pActivity), "sdcard/"+System.currentTimeMillis()+".png");
    }

    public static void capScreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = "screencap";
                    Log.d("click", cmd);
                    Socket socket = new Socket("localhost", 8080);
                    //向服务器发送消息
                    PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
                    out.println(cmd);

                    //接收来自服务器的消息
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String resp = br.readLine();
                    Log.d("EEE", "click resp:"+resp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static boolean capScreen1() {
        try {
            String cmd = "screencap";
            Log.d("click", cmd);
            Socket socket = new Socket("localhost", 8080);
            //向服务器发送消息
            PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(cmd);

            //接收来自服务器的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resp = br.readLine();
            Log.d("EEE", "click resp:"+resp);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
