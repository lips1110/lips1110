package org.example.db.modules.dbmain.controller;

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
        if (s.equalsIgnoreCase("1qazxsw2") && s1.equalsIgnoreCase("admin")) {
            return BaseResult.success("查询成功",JwtTokenUtil.createJWT(UUID.randomUUID().toString()));
        }else {
            return BaseResult.failure("查询失败");
        }
       
    }
}
