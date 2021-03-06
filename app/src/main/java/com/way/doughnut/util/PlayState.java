package com.way.doughnut.util;

import android.content.Context;

import java.io.*;

/**
 * Created by dell-pc on 2016/5/23.
 */
public class PlayState {
    private static final String TAG = "PlayState";
    /* 0--未运行， 1--正在运行*/
    static int State = 0;
    public static int  getState() {
        return State;
    }
    public static void setState(int newState) {
        State = newState;
    }

    public static boolean isCmdlineRuning(String cmdline) {
        boolean state = false;
        try {
            Process proc = Runtime.getRuntime().exec("ps");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            String oneline;
            while ((oneline = br.readLine()) != null) {
                line += oneline;
            }
            if (line.length() == 0) {
                state = false;
            } else {
                if (line.contains(cmdline)) {
                    state = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return state;
    }

    public static void copy(final InputStream source, String destFile) {

        try {
            File daemservBinFile = new File(destFile);
            daemservBinFile.getParentFile().mkdirs();
            OutputStream destination = null;
            destination = new FileOutputStream(daemservBinFile);

            byte[] buffer = new byte[1024];
            int nread;

            while ((nread = source.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = source.read();
                    if (nread < 0)
                        break;
                    destination.write(nread);
                    continue;
                }
                destination.write(buffer, 0, nread);
            }
            destination.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void runDaemServer(final Context mAppContext, final InputStream doroot, final InputStream httpcmdserv) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                            /*start daem*/
                String daemserv = "doroot";

                try {
                    String httpcmdservFilePath = mAppContext.getFilesDir() + "/" + "httpcmdserv";
                    String daemservFilePath = mAppContext.getFilesDir() + "/" +  daemserv;
                    if (PlayState.isCmdlineRuning(daemservFilePath)) {
                        MyLog.d(TAG, daemservFilePath + " runing ...");
                        return;
                    } else {
                        MyLog.d(TAG, daemservFilePath + " not  runing ..." );
                    }

                    copy(doroot, daemservFilePath);
                    copy(httpcmdserv, httpcmdservFilePath);

//                    File daemservBinFile = new File(daemservFilePath);
//                    daemservBinFile.getParentFile().mkdirs();
//                    OutputStream destination = new FileOutputStream(daemservBinFile);
//
//                    byte[] buffer = new byte[1024];
//                    int nread;
//
//                    while ((nread = source.read(buffer)) != -1) {
//                        if (nread == 0) {
//                            nread = source.read();
//                            if (nread < 0)
//                                break;
//                            destination.write(nread);
//                            continue;
//                        }
//                        destination.write(buffer, 0, nread);
//                    }
//                    destination.close();
//
//                    File httpcmdservBinFile = new File(httpcmdservFilePath);
//                    destination = new FileOutputStream(httpcmdservBinFile);
//
//                    while ((nread = httpcmdserv.read(buffer)) != -1) {
//                        if (nread == 0) {
//                            nread = httpcmdserv.read();
//                            if (nread < 0)
//                                break;
//                            destination.write(nread);
//                            continue;
//                        }
//                        destination.write(buffer, 0, nread);
//                    }
//                    destination.close();
//
//


                    Runtime.getRuntime().exec("chmod 777 " + daemservFilePath);
                    Runtime.getRuntime().exec("chmod 777 " + httpcmdservFilePath);
                    MyLog.d(TAG, "runing "+ daemservFilePath);
                    Runtime.getRuntime().exec(daemservFilePath + " " +  httpcmdservFilePath, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                            /*exit daem*/
                MyLog.d(TAG, "daemserv exit !!!");
//                            PlayState.setDaemState(0);
            }
        }).start();

    }
}
