
package com.jiec.contact.db;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jiec.contact.utils.LogUtil;

public class RecordHelper {
    public static JSONObject getRecords(JSONObject requestObject) {
        JSONObject object = new JSONObject();

        String owner = requestObject.getString("user_id");

        SqlHelper sh = new SqlHelper();
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new java.util.Date());
        String sql = "SELECT * FROM Link_Email WHERE Czz = '" + owner + "' and SendTime like '%"
                + date + "%' ORDER BY SendTime DESC;";
        ResultSet rs = sh.queryExecute(sql);

        if (rs == null) {
            return null;
        }

        String selfNum = requestObject.getString("selfNum");

        JSONArray jsonArray = new JSONArray();
        try {
            while (rs.next()) {
                JSONObject o = new JSONObject();
                o.put("id", rs.getInt(1));
                o.put("name", myTrim(rs.getString(9)));
                o.put("num", myTrim(rs.getString(3)).equals(selfNum) ? myTrim(rs.getString(2))
                        : myTrim(rs.getString(3)));
                o.put("date", myTrim(rs.getString(21)));
                o.put("time", myTrim(rs.getString(8)));
                o.put("info", myTrim(rs.getString(20)));
                o.put("state", myTrim(rs.getString(11)).equals("Y") ? 1 : 0);
                o.put("msg", myTrim(rs.getString(5)));
                o.put("type", myTrim(rs.getString(15)).equals("S") ? 1 : 0);
                o.put("system_id", myTrim(rs.getString(21)));

                jsonArray.add(o);
            }

            object.put("records", jsonArray);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return object;
    }

    private static String myTrim(String str) {
        if (str == null) {
            return "";
        } else {
            return str.trim();
        }
    }

    public static boolean insertRecord(JSONObject object) {
        boolean result = true;
        JSONArray recordsArray = object.getJSONArray("records");
        for (int i = 0; i < recordsArray.size(); i++) {
            object = recordsArray.getJSONObject(i);

            String stateStr = object.getInt("state") == 1 ? "Y" : "N";
            String typeStr = object.getInt("type") == 1 ? "S" : "P";

            String fromNum, toNum;
            if (object.getInt("state") == 2) {
                fromNum = object.getString("selfNum");
                toNum = object.getString("num");
            } else {
                toNum = object.getString("selfNum");
                fromNum = object.getString("num");
            }

            String attachment = object.getString("date") + "_" + object.getString("time") + "_"
                    + object.getString("num") + ".arm";

            String subjectStr = object.getInt("type") == 1 ? "短信" : "电话";
            String sql = "INSERT INTO Link_Email (Czz, BH, FromNum, ToNum, Subject, SendTime, Attachment, IsSelfRead, Msg, Type, SystemId) values('"
                    + object.getString("owner")
                    + "', '"
                    + object.optString("numBH")
                    + "', '"
                    + fromNum
                    + "', '"
                    + toNum
                    + "', '"
                    + subjectStr
                    + "', '"
                    + object.getString("date")
                    + " "
                    + object.getString("time")
                    + "', '"
                    + attachment
                    + "', '"
                    + stateStr
                    + "', '"
                    + object.getString("msg")
                    + "', '"
                    + typeStr + "', '" + object.getString("system_id") + "');";
            LogUtil.d(sql);
            SqlHelper sh = new SqlHelper();
            result = result & sh.upExecute(sql);
        }

        return result;

    }

    public static boolean updateRecord(JSONObject object) {
        String sql = "UPDATE Link_Email SET Info='" + object.getString("info") + "' WHERE ID="
                + object.getInt("id") + ";";
        LogUtil.d(sql);
        SqlHelper sh = new SqlHelper();
        return sh.upExecute(sql);
    }
}
