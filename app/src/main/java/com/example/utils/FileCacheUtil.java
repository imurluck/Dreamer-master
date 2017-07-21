package com.example.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by 10405 on 2016/6/7.
 * This class is used to manage the file cache system.
 */

public class FileCacheUtil {

    private String path; //路径
    private static final File STORAGE = Environment.getExternalStorageDirectory();
    static final String JSONPATH = STORAGE + "/Dreamera-master/resource/data";       //缓存JSON数据、该目录下只存一个文件，时间一到就更新
    static final String PICTUREPATH = STORAGE + "/Dreamera-master/resource/picture"; //缓存图片
    public static final String CAMERAPATH = STORAGE +  "/Dreamera-master/photo/camera";     //存储拍摄后的照片
    public static final String EDITPATH = STORAGE +  "/Dreamera-master/photo/edit";         //存储编辑后的图片
    public static final String TEMPPATH = STORAGE + "/Dreamera-master/photo/temp";          //存储临时文件

    public String filename;

    public FileCacheUtil() {}

    public FileCacheUtil(String path){
        this.path = path;
    }

    /**
     * This function is used to create files
     * @param type file type 0:bitmap  1:json
     * @param picId picture ID
     * @return file
     */
    private File createFiles(int type, String picId){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = format.format(date);
        if(type == 0){ // 0: bitmap
            filename = picId + "+" + filename + ".jpg";//id + 时间
        }else if(type == 1){ //1: json
            filename += ".txt";
        }
        File fileFolder = new File(path);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个目录
            fileFolder.mkdirs(); // 创建多级目录，要用mkdirs
        }
        return new File(fileFolder, filename);
    }

    /**
     * This function is used to delete a file
     * @param file  the file wanted to delete
     */
    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete();
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (File file1 : files) { // 遍历目录下所有的文件
                    deleteFile(file1); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }


    /**
     * The code below is related to picture
     */

    /**
     * Clear JSON cache
     */
    public void jsonCacheClear() throws ParseException {
        File file = new File(FileCacheUtil.JSONPATH );
        if(file.exists()) {
            deleteFile(file);
        }
    }


    /**
     * Save JSON to file
     * @param string  the JSON string
     */
    void saveJSON(String string) throws IOException {
        File file = createFiles(1, "");
        FileOutputStream outputStream = new FileOutputStream(file);// 文件输出流
        outputStream.write(string.getBytes());
        outputStream.flush();
        outputStream.close(); // 关闭输出流
        file.setReadOnly();//设置文件只读
    }

    /**
     * Get JSON by path
     * @return String
     */
    public String getJsonFromFile() {
        try {
            File fileFolder = new File(JSONPATH);
            if(fileFolder.exists()){
                File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
                FileInputStream inputStream = null;
                if(files.length == 0) {//如果没有文件,可能是没有下载完
                    return "";
                } else if(files.length == 1) {//如果只有一个文件
                    inputStream = new FileInputStream(files[0]);
                }
                byte[] bytes = new byte[1024];
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                while (inputStream.read(bytes) != -1) {
                    arrayOutputStream.write(bytes, 0, bytes.length);
                }
                inputStream.close();
                arrayOutputStream.close();
                return new String(arrayOutputStream.toByteArray());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得JSON文件的文件名
     * @return string
     */
    String getFilename(String path) throws ParseException {
        File fileFolder = new File(path);
        String fileName = "";
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 目录下所有的文件 files[];
            if(files.length == 1){//如果文件存在
                return files[0].getName();
            }
        }
        return fileName;
    }


//-----------------------------------图片 部分 start----------------------------------------

    /**
     * 获得图片的文件名
     * @param id  picture ID
     * @return string
     */
    String getPicFilename(String id) throws ParseException {
        File fileFolder = new File(PICTUREPATH);
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 目录下所有的文件 files[];
            if(files.length > 0) {
                for (File file : files) {
                    String name = file.getName();
                    String idFromName = name.substring(0, name.indexOf('+'));
                    if (idFromName.equals(id)) {
                        return name;
                    }
                }
            }
        }
        return "";
    }


    /**
     * 按id从文件中读取图片
     * @param path the path of picture
     * @param picId picture ID
     * @return string
     */
    static Bitmap getPicFromFile(final String path, final String picId)
            throws IOException, ParseException {
        File fileFolder = new File(path);
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
            Bitmap bitmap = null;
            if(files.length == 0) {//如果没有文件,可能是没有下载完
                return null;
            }else{
                for (File file : files) {
                    String name = file.getName();
                    String idFromName = name.substring(0, name.indexOf('+'));
                    if (idFromName.equals(picId)) {
                        bitmap = BitmapFactory.decodeFile(path + '/' + name);
                        break;
                    }
                }
                return bitmap;
            }
        }else { //文件不存在
            return null;
        }
    }


    /**
     * 从文件中读取照片
     * @param path the path of the picture
     * @return bitmap
     */
    static Bitmap getPhotoFromFile(final String path)
            throws IOException, ParseException {
        File fileFolder = new File(path);
        Bitmap bitmap = null;
        if(fileFolder.exists()){
            File files[] = fileFolder.listFiles(); // 声明目录下所有的文件 files[];
            if(files.length == 1) { //如果没有文件,可能是没有下载完
                String name = files[0].getName();
                bitmap = BitmapFactory.decodeFile(path + '/'+name);
            }
        }
        return bitmap;
    }


    /**
     * 按id存储图片到文件中
     * @param bitmap the bitmap needed to be saved
     * @return void
     */
    public void savePicture(Bitmap bitmap, String id, boolean type) throws IOException {
        File file = createFiles(0, id);
        FileOutputStream outputStream = new FileOutputStream(file);// 文件输出流

        int compressLength = 900; // 压缩后的大小
        int maxLength = 1000; //最大长度
        int bigRadio = 10; //压缩90%
        int smallRadio = 100; //压缩90%
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float radio = 0.0f;

        if(type && (width >= maxLength || height >= maxLength)) {

            /**
             * 把最长的边缩放到900，另一条边等比例变换
             */
            boolean flag = (width > height);
            radio = flag ? ((float)width / compressLength) : ((float)height / compressLength);
            int newHeight = flag ? (int)(height / radio) : compressLength;
            int newWidth = flag ? compressLength : (int)(width / radio);

            bitmap = ImgToolKits.changeBitmapSize(bitmap, newWidth, newHeight);
            bitmap.compress(Bitmap.CompressFormat.JPEG, bigRadio, outputStream);//压缩图片90%
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, smallRadio, outputStream);//压缩图片90%
        }
        outputStream.flush();
        outputStream.close(); // 关闭输出流
    }

}
