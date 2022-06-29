package com.tvs.vitalsign.repository.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VoiceFileUtils {

    public static File getVoiceFile(Context context, String fileName) {
        String dirPath = context.getCacheDir() + File.separator + "voice";
        File voiceDir = new File(dirPath);
        if (!voiceDir.exists()) voiceDir.mkdirs();
        String filePath = voiceDir + File.separator + fileName + ".wav";
        return new File(filePath);
    }

    public static boolean writeWavFileIntoCacheDir(File file, InputStream byteStream, long _fileSize) {
        try {
            if (file.exists()) {
                if (_fileSize == file.length()) {
                    return true;
                } else {
                    file.delete();
                }
            }
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];

                long fileSize = _fileSize;
                long fileSizeDownloaded = 0;

                inputStream = byteStream;
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d("VoiceFileUtils", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
