package org.example.db.modules.dbmain.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BatchRows {
    private String type;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private Integer rowCount;
    
    private Integer total;
}
