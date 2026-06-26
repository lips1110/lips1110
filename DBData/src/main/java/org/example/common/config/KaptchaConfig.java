package org.example.common.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer producer() {

        Properties props = new Properties();

        props.put("kaptcha.border", "no");
        props.put("kaptcha.textproducer.char.length", "4");
        props.put("kaptcha.image.width", "130");
        props.put("kaptcha.image.height", "48");
        props.put("kaptcha.textproducer.font.size", "32");
        props.put("kaptcha.noise.color", "black");

        Config config = new Config(props);

        return config.getProducerImpl();
    }
}
