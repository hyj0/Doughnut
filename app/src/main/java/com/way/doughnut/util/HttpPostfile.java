package com.way.doughnut.util;

//import javafx.util.Pair;

import android.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by dell-pc on 2016/5/22.
 */
public class HttpPostfile {
    public String post(String url, String filePath) {
        try {
//            String url = "http://example.com/upload";
            MyLog.d("HttpPostfile", url + "  " + filePath);
            String charset = "UTF-8";
            String param = "value";
//            File textFile = new File("postfile.txt");
            File binaryFile = new File(filePath);
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try  {

                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

                // Send normal param.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).append(param).append(CRLF).flush();

//                // Send text file.
//                writer.append("--" + boundary).append(CRLF);
//                writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
//                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
//                writer.append(CRLF).flush();
//                Files.copy(textFile.toPath(), output);
//                output.flush(); // Important before continuing with writer!
//                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // Send binary file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
                writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
//                Files.copy(binaryFile.toPath(), output);
//                output.flush(); // Important before continuing with writer!

                DataInputStream dis = new DataInputStream(new FileInputStream(binaryFile));
                byte[] fileData = new byte[(int) binaryFile.length()];
                dis.readFully(fileData);
                output.write(fileData);


                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            } catch (Exception e) {

            }

// Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (responseCode != 200) {
                return "";
            }
            System.out.println(responseCode+":"); // Should be 200
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String retStr = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                //                System.out.println(inputLine);
                retStr += inputLine;
            }
            in.close();
            return retStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<Pair> parseSteps(String stepStr) {
        ArrayList<Pair> retLst = new ArrayList<>();
        String s = stepStr.replace("[", "").replace("]", "");
        String[] sp = s.split(",");
        for (int i = 0; i < sp.length; i += 2) {
            int x = Integer.parseInt(sp[i].trim());
            int y = Integer.parseInt(sp[i+1].trim());

            retLst.add(new Pair(x, y));
        }
        return retLst;
    }

//    public static void main(String[] argv) {
//        HttpPostfile httppost = new HttpPostfile();
////        String res = httppost.post("http://localhost:8888/play_game", "mmm1.png");
//        String res = "[[857, 857], [749, 749], [695, 695], [695, 695], [803, 803], [749, 749], [479, 479], [533, 533], [587, 587], [749, 749], [695, 695], [857, 857], [857, 857], [857, 857], [749, 749], [857, 857], [749, 749], [857, 857], [803, 803], [857, 857], [803, 803], [857, 857]]";
//        System.out.println(res);
//        ArrayList<Pair> lst = httppost.parseSteps(res);
//        for (Pair xy :
//                lst) {
//            int x = (int) xy.first;
//            int y = (int) xy.second;
//            System.out.println(x + " "+ y);
//        }
//    }
}
