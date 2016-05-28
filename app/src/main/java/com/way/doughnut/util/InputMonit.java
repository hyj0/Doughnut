package com.way.doughnut.util;

import android.util.Log;

import java.io.*;
import java.net.Socket;

/**
 * Created by dell-pc on 2016/5/21.
 */
public class InputMonit {
    void runCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream stderr = process.getErrorStream ();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stderr));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("EEEE", "runCmd err:"+line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void click(final int x, final int y) throws IOException {
//        String cmd = String.format("input tap %d %d", x, y);
//        runCmd(cmd);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = String.format("input %d %d", x, y);
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
    public  void click1(int x, int y) {
        try {
            String cmd = String.format("input %d %d", x, y);
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

    public static void stopmonit() {
        try {
            String cmd = "exit";
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}