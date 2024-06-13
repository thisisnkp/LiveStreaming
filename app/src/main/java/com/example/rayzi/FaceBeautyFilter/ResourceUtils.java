package com.example.rayzi.FaceBeautyFilter;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {
    /**
     * 递归拷贝Asset目录中的文件到rootDir中
     * <p>
     * Recursively copy the files in the Asset directory to rootDir
     */
    public static void initResources(AssetManager assets, String assetsName,
                                     String destPath) throws IOException {
        File destFile = new File(destPath, assetsName);
        if (isAssetsDir(assets, assetsName)) {
            if (!destFile.exists()) {
                destFile.mkdirs();
            }
            for (String name : assets.list(assetsName)) {
                initResources(assets, assetsName + File.separator + name, destPath);
            }
        } else {
            copyFile(assets.open(assetsName), destFile);
        }
    }

    private static List<String> getComposerNodeTags(AssetManager assets,
                                                    String assetsName)
            throws IOException {
        InputStream is = assets.open(assetsName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) >= 0) {
                baos.write(buffer, 0, bytesRead);
            }
            String content = baos.toString();
            JSONArray jsonArray = new JSONArray(content);
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                tags.add(jsonArray.getString(i));
            }
            return tags;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            is.close();
            baos.flush();
            baos.close();
        }
        return null;
    }

    private static boolean isAssetsDir(AssetManager assets, String path)
            throws IOException {
        String[] files = assets.list(path);
        return files != null && files.length > 0;
    }

    private static void copyFile(InputStream is, File destFile)
            throws IOException {
        if (destFile.exists()) {
            return;
        }
        File parentFile = destFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(destFile);
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) >= 0) {
                fos.write(buffer, 0, bytesRead);
            }
        } finally {
            is.close();
            fos.flush();
            try {
                fos.getFD().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fos.close();
        }
    }

    public static boolean clearDir(File dir) {
        if (!dir.exists()) {
            return true;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                clearDir(file);
                file.delete();
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        return true;
    }
}
