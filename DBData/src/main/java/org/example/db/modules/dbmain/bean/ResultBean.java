package org.example.db.modules.dbmain.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResultBean {
    private List<Map<String, Object>> rows;
    
    private Long total;
    
    private int code;
    
    private String message;
}
