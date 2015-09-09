package com.amigo.navi.keyguard.haokan;


import android.content.Context;
import android.text.TextUtils;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.haokan.db.DataConstant;
import com.amigo.navi.keyguard.haokan.entity.Client;
import com.amigo.navi.keyguard.haokan.entity.EventLogger;
import com.amigo.navi.keyguard.haokan.entity.WallpaperList;
import com.amigo.navi.keyguard.haokan.entity.Wallpaper;
import com.amigo.navi.keyguard.haokan.entity.Category;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.amigo.navi.keyguard.haokan.entity.Music;


public class JsonUtil {
	private static final String TAG = "JsonUtil";
    private static final String DATA_VERSION = "dv";
    
    public static String parseJsonToUserId(String jsonString) {
        
        String userId = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            userId = jsonObject.optString("uid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userId;
    }
    
    
    //10.  故事锁屏——用户基本信息提交
    public static String clientInfoToJsonString(Client client) {

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("dn", client.getDeviceName());
            data.put("imei", client.getImei());
            data.put("iccid", client.getIccid());
            data.put("mb", client.getMobileNumber());
            data.put("ss", client.getScreenSize());
            data.put("mac", client.getMac());
            data.put("os", client.getOs());
            data.put("sex", client.getSex());
            data.put("bd", client.getBirthday());
            data.put("p", client.getProvince());
            data.put("c", client.getCity());
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
        
    }
    
    
    public static List<Category> parseJsonToCategory(String jsonString) {

        DebugLog.d(TAG, "parseJsonToCategory jsonString = " + jsonString);
        List<Category> list = new ArrayList<Category>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            
            JSONArray jsonArray = jsonObject.optJSONArray("data");
            if(jsonArray == null){
            	return list;
            }
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject2 = jsonArray.optJSONObject(i);
                Category wallpaperType = new Category();
                wallpaperType.setTypeId(jsonObject2.optInt("i"));
                wallpaperType.setTypeName(jsonObject2.optString("n"));
                wallpaperType.setTypeIconUrl(jsonObject2.optString("u"));
                wallpaperType.setTypeNameEn(jsonObject2.optString("en"));
                wallpaperType.setFavorite(true);
                wallpaperType.setSort(i);
                list.add(wallpaperType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static void setCategoryDataVersion(Context context,String jsonString){
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            int dateVersion = jsonObject.optInt(DATA_VERSION);
            Common.setDataVersion(context, dateVersion);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
  
    
    public static WallpaperList parseJsonToWallpaperList(String jsonString) {

        DebugLog.d(TAG, "parseJsonToWallpaperList jsonString = " + jsonString);
        WallpaperList list = new WallpaperList();
        
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.optJSONArray("data");
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject2 = jsonArray.optJSONObject(i);
                String date = jsonObject2.optString("date");
                String festival = jsonObject2.optString("f");
                JSONArray jsonArray3 = jsonObject2.optJSONArray("ts");
                for (int j = 0; j < jsonArray3.length(); j++) {
                    JSONObject jsonObject4 = jsonArray3.optJSONObject(j);
                    Category category = new Category();
                    category.setTypeId(jsonObject4.optInt("ti"));
                    category.setTypeName(jsonObject4.optString("tn"));
                    JSONArray jsonArray4 = jsonObject4.optJSONArray("imgs");
                    for (int k = 0; k < jsonArray4.length(); k++) {

                        JSONObject jsonObject5 = jsonArray4.optJSONObject(k);
                        Wallpaper wallpaper = new Wallpaper();
                        wallpaper.setCategory(category);
                        wallpaper.setDate(date);
                        wallpaper.setFestival(festival);
                        wallpaper.setImgId(jsonObject5.optInt("i"));
                        wallpaper.setImgName(jsonObject5.optString("n"));
                        wallpaper.setImgContent(jsonObject5.optString("c"));
                        wallpaper.setImgSource(jsonObject5.optString("s"));
                        wallpaper.setImgUrl(jsonObject5.optString("iu"));
                        wallpaper.setUrlClick(jsonObject5.optString("uc"));
                        wallpaper.setStartTime(jsonObject5.optString("st"));
                        wallpaper.setEndTime(jsonObject5.optString("et"));
                        wallpaper.setUrlPv(jsonObject5.optString("up"));
                        wallpaper.setIsAdvert(jsonObject5.optInt("ia"));
                        wallpaper.setBackgroundColor(jsonObject5.optString("bc"));
                        String showTime = jsonObject5.optString("t");
                        
//                        if(!TextUtils.isEmpty(showTime)){
//                            String[] times = showTime.split("-");
//                            if(times != null && times.length > 1){
//                                wallpaper.setShowTimeBegin(times[0]);
//                                wallpaper.setShowTimeEnd(times[1]);
//                            }else{
//                                wallpaper.setShowTimeBegin("");
//                                wallpaper.setShowTimeEnd("");
//                            }
//                        }
                        
                        wallpaper.setShowTimeBegin("NA");
                        wallpaper.setShowTimeEnd("NA");

                        if (!TextUtils.isEmpty(showTime)) {
                            String[] times = showTime.split("-");
                            if (times != null && times.length > 1) {
                                wallpaper.setShowTimeBegin(times[0]);
                                wallpaper.setShowTimeEnd(times[1]);
                            }
                        }
                        
                        wallpaper.setSort(jsonObject5.optInt("st"));
                        wallpaper.setDetailLinkText(jsonObject5.optString("ud"));
                        Music music = new Music();
 
                        music.setmMusicName(jsonObject5.optString("mn"));
                        music.setmArtist(jsonObject5.optString("ms"));
                        music.setPlayerUrl(jsonObject5.optString("mu"));
                        music.setMusicId(jsonObject5.optString("mi"));
                        wallpaper.setMusic(music);

                        list.add(wallpaper);
                    }
                }
            }
            
            list.setHasMore(jsonObject.optInt("hm") == 1);
          
        } catch (JSONException e) {
            DebugLog.d(TAG,"parseJsonToWallpaperList error e:" + e.getStackTrace());
            e.printStackTrace();
        }
        return list;
    }
    
    public static String logToJsonString(HashMap<String, List<EventLogger>> map,List<EventLogger> slogs, String userId) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("ui", userId);
            
            
            JSONArray jsonArrayILogs = new JSONArray();
            for (Entry<String, List<EventLogger>> entry : map.entrySet()) {
                JSONObject jsonObjectLog = new JSONObject();
                String dataHour = entry.getKey();
                jsonObjectLog.put("dh", dataHour);
                List<EventLogger> listLog = entry.getValue();
                JSONArray jsonArrayImgs = new JSONArray();
                for (int j = 0; j < listLog.size(); j++) {
                    EventLogger userLog = listLog.get(j);
                    JSONObject jsonObjectImag = new JSONObject();

                    jsonObjectImag.put("ii", userLog.getImgId());
                    jsonObjectImag.put("ti", userLog.getTypeId());
                    jsonObjectImag.put("e", userLog.getEvent());
                    jsonObjectImag.put("c", userLog.getCount());
                    jsonArrayImgs.put(jsonObjectImag);
                }
                jsonObjectLog.put("imgs", jsonArrayImgs);
                jsonArrayILogs.put(jsonObjectLog);
            }

            data.put("ilogs", jsonArrayILogs);
            
            
            JSONArray jsonArraySLogs = new JSONArray();
            for (int j = 0; j < slogs.size(); j++) {
                EventLogger userLog = slogs.get(j);
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem.put("e", userLog.getEvent());
                jsonObjectItem.put("dt", userLog.getDateTime());
                jsonObjectItem.put("v", userLog.getValue());
                jsonArraySLogs.put(jsonObjectItem);
            }
            data.put("slogs", jsonArraySLogs);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jsonObject.toString();
        
    }
    
    
     public static String userLogToJsonString(HashMap<String, List<EventLogger>> map, String userId) {
        
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("ui", userId);
            JSONArray jsonArrayLogs = new JSONArray();
            for (Entry<String, List<EventLogger>> entry : map.entrySet()) {
                JSONObject jsonObjectLog = new JSONObject();
                String dataHour = entry.getKey();
                jsonObjectLog.put("dh", dataHour);
                List<EventLogger> listLog = entry.getValue();
                JSONArray jsonArrayImgs = new JSONArray();
                for (int j = 0; j < listLog.size(); j++) {
                    EventLogger userLog = listLog.get(j);
                    JSONObject jsonObjectImag = new JSONObject();

                    jsonObjectImag.put("ii", userLog.getImgId());
                    jsonObjectImag.put("ti", userLog.getTypeId());
                    jsonObjectImag.put("e", userLog.getEvent());
                    jsonObjectImag.put("c", userLog.getCount());
                    jsonArrayImgs.put(jsonObjectImag);
                }
                jsonObjectLog.put("imgs", jsonArrayImgs);
                jsonArrayLogs.put(jsonObjectLog);
            }

            data.put("ilogs", jsonArrayLogs);
            data.put("slogs", new JSONArray());
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }
     
     
    public static String userLogToJsonString(List<EventLogger> slogs, String userId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("ui", userId);
            JSONArray jsonArrayLogs = new JSONArray();
            for (int j = 0; j < slogs.size(); j++) {
                EventLogger userLog = slogs.get(j);
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem.put("e", userLog.getEvent());
                jsonObjectItem.put("dt", userLog.getDateTime());
                jsonObjectItem.put("v", userLog.getValue());
                jsonArrayLogs.put(jsonObjectItem);
            }
            data.put("slogs", jsonArrayLogs);
            data.put("ilogs", new JSONArray());
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }
    
    
    public static WallpaperList getDefaultWallpaperList() {
        
        String jsonString = null;
        WallpaperList list = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(FileUtil.WALLPAPER_XML_LOCATION);
//            is = context.getAssets().open("wallpaper.xml");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            jsonString = new String(buffer, "utf-8");
            DebugLog.d("haokan", "jsonString = " + jsonString);
            if (jsonString != null) {
                
                list = new WallpaperList();
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.optJSONArray("imgs");
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jsonObjectImg = jsonArray.optJSONObject(i);
                    
                    Wallpaper wallpaper = new Wallpaper();
                    String imageName = jsonObjectImg.optString("ImageName");
                    String imageTitle = jsonObjectImg.optString("ImageTitle");
                    String imageContent = jsonObjectImg.optString("ImageContent");
                    String imageSource = jsonObjectImg.optString("ImageSource");
                    String background = jsonObjectImg.optString("Background");
                    String showTimeBegin = jsonObjectImg.optString("ShowTimeBegin");
                    String showTimeEnd = jsonObjectImg.optString("ShowTimeEnd");
                    
                    String MusicName = jsonObjectImg.optString("MusicName");
                    String MusicSinger = jsonObjectImg.optString("MusicSinger");
                    String MusicURL = jsonObjectImg.optString("MusicURL");
                    
                    String lock = jsonObjectImg.optString("Lock");
                    int id = i + 1;
                    
                    Music music = null;
                    if (!TextUtils.isEmpty(MusicURL)) {
                        music = new Music();
                        music.setMusicId(String.valueOf(id));
                        music.setmMusicName(MusicName);
                        music.setmArtist(MusicSinger);
                        music.setDownLoadUrl(MusicURL);
                        music.setPlayerUrl(MusicURL);
                    }
                    wallpaper.setMusic(music);
 
                    Category category = new Category();
                    category.setTypeId(0);
                    category.setTypeName("inlay");
                    wallpaper.setCategory(category);
                    wallpaper.setImgId(id);
                    wallpaper.setDisplayName(imageName);
                    wallpaper.setImgName(imageTitle);
                    wallpaper.setImgContent(imageContent);
                    wallpaper.setImgSource(imageSource);
                    
                    wallpaper.setBackgroundColor(background);

                    wallpaper.setShowTimeBegin("NA");
                    wallpaper.setShowTimeEnd("NA");

                    if (!TextUtils.isEmpty(showTimeBegin)) {
                        wallpaper.setShowTimeBegin(showTimeBegin);
                    }
                    if (!TextUtils.isEmpty(showTimeEnd)) {
                        wallpaper.setShowTimeEnd(showTimeEnd);
                    }
                    
                    wallpaper.setImgUrl(imageName);
                    wallpaper.setType(Wallpaper.WALLPAPER_FROM_FIXED_FOLDER);
                    wallpaper.setRealOrder(i);
                    wallpaper.setShowOrder(i); 
                    wallpaper.setTodayImage(true);
                    if("1".equals(lock)){
                        wallpaper.setLocked(true);
                    }else{
                        wallpaper.setLocked(false);
                    }
                    wallpaper.setSort(i);
                    wallpaper.setDownloadFinish(DataConstant.DOWNLOAD_FINISH);
                    list.add(wallpaper);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
    

}
