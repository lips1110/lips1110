package org.example.db.modules.dbmain.controller;

import com.anji.captcha.util.MD5Util;
import org.example.common.utils.LocalCacheUtil;
import org.example.db.modules.dbmain.bean.BaseResult;
import org.example.db.modules.dbmain.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/db/login")
public class LoginController {
    
    @PostMapping("/submit")
    public BaseResult SubmitLogin(@RequestBody HashMap<String,String> hashMap){
        String s = hashMap.get("password");
        String s1 = hashMap.get("username");
        String cacheCode =
                LocalCacheUtil.get(hashMap.get("uuid"));
        if (cacheCode == null) {
            return BaseResult.failure("验证码已过期");
        }
    
        if (!cacheCode.equalsIgnoreCase(hashMap.get("captcha"))) {
            return BaseResult.failure("验证码错误");
        }
    
        if (s.equalsIgnoreCase(MD5Util.md5("1qazxsw2")) && s1.equalsIgnoreCase("admin")) {
            String jwt = JwtTokenUtil.createJWT(UUID.randomUUID().toString(), s1);
            LocalCacheUtil.put("token:"+s1,jwt,60*60*8);
            return BaseResult.success("登录成功",jwt);
        }else {
            return BaseResult.failure("登录失败");
        }
       
    }
    
    
}
