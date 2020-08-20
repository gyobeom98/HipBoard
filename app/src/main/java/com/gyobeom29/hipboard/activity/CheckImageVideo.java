package com.gyobeom29.hipboard.activity;

import android.util.Log;

public class CheckImageVideo {

    //확장자가 비디오인지 이미지인지 확인
    static public Boolean isVideo(String path) {
        String extension = getExtension(path);
        Log.i("CheckImage", extension);
        if (extension.equals("mp4") || extension.equals("MP4") || extension.equals("MOV") || extension.equals("mov") || extension.equals("AVI") || extension.equals("avi") ||
                extension.equals("MKV") || extension.equals("mkv") || extension.equals("WMV") || extension.equals("wmv") || extension.equals("TS") || extension.equals("ts") ||
                extension.equals("TP") || extension.equals("tp") || extension.equals("FLV") || extension.equals("flv") || extension.equals("3GP") || extension.equals("3gp") ||
                extension.equals("MPG") || extension.equals("mpg") || extension.equals("MPEG") || extension.equals("mpeg") || extension.equals("MPE") || extension.equals("mpe") ||
                extension.equals("ASF") || extension.equals("asf") || extension.equals("ASX") || extension.equals("asx") || extension.equals("DAT") || extension.equals("dat") ||
                extension.equals("RM") || extension.equals("rm")) {
            return true;
        } else {
            return false;
        }
    }

    //확장자 나누기
    public static String getExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.indexOf('?'));
    }


}
