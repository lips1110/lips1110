package org.example.db.modules.dbmain.controller;

import com.google.code.kaptcha.Producer;
import org.example.common.utils.LocalCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/db/captcha")
public class CapController {
    @Autowired
    private Producer producer;
    @GetMapping
    public Map<String,Object> captcha(HttpServletResponse response)            throws IOException {
        String text = producer.createText();
        String uuid = UUID.randomUUID().toString();
        // 5分钟过期
        LocalCacheUtil.put(uuid, text.toLowerCase(), 300);
        BufferedImage image = producer.createImage(text);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        String base64 = Base64.getEncoder()                .encodeToString(baos.toByteArray());
        Map<String,Object> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("img","data:image/jpeg;base64," + base64);
        return map;
    }
}
