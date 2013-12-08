package limbo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Limbo
 * Date: 13-12-5
 * Time: 下午4:39
 * To change this template use File | Settings | File Templates.
 */

/*
* This is an example for DoubanParser
* Need jar: json, jsoup
* */
public class Main {

    static public String getFileString(String filename) {
        String content = "";
        try {
            StringBuffer contentBuffer = new StringBuffer();
            File file = new File(filename);
            FileInputStream inputStream = new FileInputStream(file);
            char[] buffer = new char[1024];
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            while (inputStreamReader.read(buffer) != -1) {
                contentBuffer.append(new String(buffer));
                buffer = new char[1024];
            }
            inputStream.close();
            inputStreamReader.close();
            content = contentBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return content;
    }

    public static void handleFile(String srcDir, String outputDir, Integer count) {
        File dir = new File(srcDir);
        File[] files = dir.listFiles();

        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            String absolutePath = files[i].getAbsolutePath();
            if (files[i].isDirectory()) {
                handleFile(absolutePath, outputDir, count);
            } else {
                String content = getFileString(absolutePath);
                DoubanParser parser = new DoubanParser(content, absolutePath);
                JSONArray jsonArray = parser.parse();
                for (int j = 0; j < jsonArray.length(); j++) {
                    try {
                        count++;
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        String result = jsonObject.toString();
                        String newFileName = "output/" + count.toString() + ".json";
                        FileWriter fileWriter = new FileWriter(newFileName);
                        fileWriter.write(result, 0, result.length());
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (JSONException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
    	if (args.length < 2) {
    		System.out.println("Error: Incomplete arguments");
    		return ;
    	}
        String srcDir = args[0];
        String outputDir = args[1];
        File file = new File(outputDir);
        boolean ret = true;
        if (!file.exists())
            ret = file.mkdirs();
        if (ret)
            Main.handleFile(srcDir, outputDir, 0);
        else
            System.out.println("Error: Cannot make directory");
    }
}
