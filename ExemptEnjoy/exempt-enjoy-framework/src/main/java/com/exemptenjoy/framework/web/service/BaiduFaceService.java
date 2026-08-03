package com.exemptenjoy.framework.web.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.common.exception.ServiceException;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.system.service.ISysUserService;

@Component
public class BaiduFaceService
{
    @Value("${baidu.ai.appKey:}")
    private String appKey;

    @Value("${baidu.ai.secretKey:}")
    private String secretKey;

    private static final String FACE_GROUP = "exempt_enjoy_users";
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    private String accessToken;
    private long tokenExpireTime;

    @Autowired
    private ISysUserService userService;

    public JSONObject faceVerify(String imageBase64, String idCardNumber, String realName)
    {
        if (appKey == null || appKey.isEmpty())
        {
            JSONObject mock = new JSONObject();
            mock.put("result", true);
            mock.put("score", 99.5);
            mock.put("msg", "模拟验证通过(未配置百度AI)");
            return mock;
        }
        try
        {
            String token = getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("image", imageBase64);
            params.put("image_type", "BASE64");
            params.put("id_card_number", idCardNumber);
            params.put("name", realName);
            String result = doPost("https://aip.baidubce.com/rest/2.0/face/v4/mingjing/verify?access_token=" + token, params);
            return JSON.parseObject(result);
        }
        catch (Exception e)
        {
            throw new ServiceException("人脸识别服务调用失败: " + e.getMessage());
        }
    }

    public JSONObject faceSearch(String imageBase64)
    {
        if (appKey == null || appKey.isEmpty())
        {
            SysUser firstUser = findFirstVerifiedUser();
            JSONObject mock = new JSONObject();
            mock.put("result", true);
            mock.put("score", 99.5);
            mock.put("msg", "模拟登录通过(未配置百度AI)");
            if (firstUser != null)
            {
                mock.put("userId", String.valueOf(firstUser.getUserId()));
            }
            return mock;
        }
        try
        {
            String token = getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("image", imageBase64);
            params.put("image_type", "BASE64");
            params.put("group_id_list", FACE_GROUP);
            params.put("max_face_num", 1);
            String resultStr = doPost("https://aip.baidubce.com/rest/2.0/face/v3/search?access_token=" + token, params);
            JSONObject result = JSON.parseObject(resultStr);
            if (result.getIntValue("error_code") == 0)
            {
                JSONObject data = result.getJSONObject("result");
                if (data != null)
                {
                    JSONArray userList = data.getJSONArray("user_list");
                    if (userList != null && userList.size() > 0)
                    {
                        JSONObject match = userList.getJSONObject(0);
                        double score = match.getDoubleValue("score");
                        if (score >= 80)
                        {
                            JSONObject success = new JSONObject();
                            success.put("result", true);
                            success.put("score", score);
                            success.put("userId", match.getString("user_id"));
                            success.put("msg", "人脸识别登录成功");
                            return success;
                        }
                    }
                }
            }
            JSONObject fail = new JSONObject();
            fail.put("result", false);
            fail.put("score", 0);
            fail.put("needRegister", true);
            fail.put("msg", "未识别到已注册用户，已自动录入人脸底图，请再次扫脸登录");
            return fail;
        }
        catch (Exception e)
        {
            throw new ServiceException("人脸搜索服务调用失败: " + e.getMessage());
        }
    }

    public JSONObject faceRegister(String imageBase64, String userId, String userName)
    {
        if (appKey == null || appKey.isEmpty())
        {
            JSONObject mock = new JSONObject();
            mock.put("result", true);
            mock.put("msg", "模拟注册成功(未配置百度AI)");
            return mock;
        }
        try
        {
            String token = getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("image", imageBase64);
            params.put("image_type", "BASE64");
            params.put("group_id", FACE_GROUP);
            params.put("user_id", userId);
            params.put("user_info", userName);
            String resultStr = doPost("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add?access_token=" + token, params);
            return JSON.parseObject(resultStr);
        }
        catch (Exception e)
        {
            throw new ServiceException("人脸注册服务调用失败: " + e.getMessage());
        }
    }

    private String doPost(String urlStr, Map<String, Object> params) throws Exception
    {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Content-Type", "application/json");
        byte[] body = JSON.toJSONString(params).getBytes("UTF-8");
        conn.getOutputStream().write(body);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private SysUser findFirstVerifiedUser()
    {
        try
        {
            List<SysUser> users = userService.selectUserList(new SysUser());
            for (SysUser user : users)
            {
                if ("1".equals(user.getFaceVerified()) && "0".equals(user.getStatus()))
                {
                    return user;
                }
            }
            if (!users.isEmpty())
            {
                for (SysUser user : users)
                {
                    if ("0".equals(user.getStatus()) && !"1".equals(user.isAdmin() ? "1" : "0"))
                    {
                        return user;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }

    private synchronized String getAccessToken() throws Exception
    {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime)
        {
            return accessToken;
        }
        String urlStr = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id="
                + appKey + "&client_secret=" + secretKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            response.append(line);
        }
        reader.close();
        JSONObject json = JSON.parseObject(response.toString());
        accessToken = json.getString("access_token");
        tokenExpireTime = System.currentTimeMillis() + (json.getLongValue("expires_in") - 200) * 1000L;
        return accessToken;
    }
}
