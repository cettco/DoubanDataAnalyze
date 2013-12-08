package limbo;

import org.json.JSONArray;
import org.json.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Limbo
 * Date: 13-12-5
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */

/*
*  Data model for all info.
*  Member variable name is also json key.
* */
class Model {
    private Integer id;
    private String title;
    private String location;
    private String startDate;
    private String endDate;
    private String type;
//    private String fee;
    private Integer peopleJoin;
    private Integer peopleInterested;
}

public class DoubanParser {
    private String content;
    private String filename;
    // A list of Shanghai districts
    private final ArrayList<String> DISTRICTS = new ArrayList<String>(Arrays.asList(
            "黄浦区", "徐汇区", "静安区", "闸北区", "杨浦区", "宝山区", "浦东新区", "松江区", "奉贤区",
            "卢湾区", "长宁区", "普陀区", "虹口区", "闵行区", "嘉定区", "金山区", "青浦区", "崇明县"
    ));
    private final ArrayList<String> ACTIVITY_TYPE = new ArrayList<String>(Arrays.asList(
            "music", "drama", "salon", "party", "film", "exhibition", "commonweal",
            "travel", "sports", "others"
    ));

    public DoubanParser(String content, String filename) {
        this.content = content;
        this.filename = filename;
    }

    public JSONArray parse() {
        JSONArray jsonArray = new JSONArray();
        Document doc = Jsoup.parse(content);
        Elements items = doc.getElementsByClass("list-entry");
        for( Element item : items) {
            String url = item.select("[itemprop=url]").attr("href");
            Integer length = url.length();
            Integer id = Integer.parseInt(url.substring(url.lastIndexOf('/', length - 2) + 1, length -1));
            String title = item.select("[itemprop=summary]").html();
            String address = item.select("[itemprop=location]").attr("content");
            String location = getDistrict(address);
            String startDate = item.select("[itemprop=startDate]").attr("datetime");
            String endDate = item.select("[itemprop=endDate]").attr("datetime");
//            String fee = item.select("li.fee").select("strong").html();
            Integer peopleJoin = Integer.parseInt(item.select("p[class=counts]").select("span").first().text()
                    .replace("人参加", ""));
            Integer peopleInterested = Integer.parseInt(item.select("p[class=counts]").select("span").get(2).text()
                    .replace("人感兴趣", ""));
            String type = getType(filename);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", id);
                jsonObject.put("title", title);
                jsonObject.put("location", location);
                jsonObject.put("startDate", startDate);
                jsonObject.put("endDate", endDate);
                jsonObject.put("type", type);
//                jsonObject.put("fee", fee);
                jsonObject.put("peopleJoin", peopleJoin);
                jsonObject.put("peopleInterested", peopleInterested);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return jsonArray;
    }

    private String getDistrict(String addr) {
        for (String district : DISTRICTS) {
            if (addr.contains(district))
                return district;
        }
        try {
            final URL url = new URL("http://api.map.baidu.com/place/v2/search?ak=y2FokqkoaD1pfNnXDcEsFK5w&output=json" +
                    "&page_size=1&page_num=0&scope=1&region=" + URLEncoder.encode("上海", "utf-8") + "&query="
                    + URLEncoder.encode(addr, "utf-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String result = "", content;
                while((content = bufferedReader.readLine()) != null) {
                    result += content;
                }
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getInt("total") > 0) {
                    String address = ((JSONObject) jsonObject.getJSONArray("results").get(0)).getString("address");
                    for (String district : DISTRICTS) {
                        if (address.contains(district))
                            return district;
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "未知";
    }

    private String getType(String in) {
        for (String type : ACTIVITY_TYPE) {
            if (in.contains(type))
                return type;
        }
        return "unknown type";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
