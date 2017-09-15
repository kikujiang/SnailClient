package solo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class FileUtil {


	private static final String TAG = "FileUtil";
	/**
     * 读文件
     * 
     * @param filePath 文件路径
     * @return 若文件路径所在文件不存在返回null，否则返回文件内容
     */
    public static String readFile(String filepath) {
        File file = new File(filepath);
        String fileContent = "";
        if (file != null && file.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    fileContent += line;
                }
                reader.close();
                return fileContent;
            } catch (IOException e) {
                throw new RuntimeException("IOException occurred. ", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException("IOException occurred. ", e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断文件夹是否存在，如果不存在,就创建
     * @param folderPath
     * @return
     */
    public static boolean checkFolderExists(String folderPath) throws Exception{
    	boolean isFolderExists = false;
    	File folderFile = new File(folderPath);
    	if(folderFile.exists() && folderFile.isDirectory()){
    		deleteAllFiles(folderFile);
    		isFolderExists = true;
    	}else{
    		isFolderExists = createFolder(folderPath);
    	}
    	return isFolderExists;
    }
    
    private static void deleteAllFiles(File root) {  
        File files[] = root.listFiles();  
        if (files != null)  
            for (File f : files) {  
                if (f.isDirectory()) { // 判断是否为文件夹  
                    deleteAllFiles(f);  
                    try {  
                        f.delete();  
                    } catch (Exception e) {  
                    }  
                } else {  
                    if (f.exists()) { // 判断是否存在  
                        deleteAllFiles(f);  
                        try {  
                            f.delete();  
                        } catch (Exception e) {  
                        }  
                    }  
                }  
            }  
    }  
    
    /**
     * 根据指定路径创建文件夹
     * @param folderPath
     * @return 创建文件夹是否成功
     */
    private static boolean createFolder(String folderPath){
    	Log.d(TAG, "folder path is:" + folderPath);
    	File folderFile = new File(folderPath);
    	return folderFile.mkdirs();
    }
    
    /**
     * 写文件
     * 
     * @param filePath 文件路径
     * @param content 内容
     * @return
     */
    public static boolean writeFile(String filePath,String content) {
    	Log.d(TAG, "file path is:" + filePath+ ", content is:" + content);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath, false);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }
    
}
