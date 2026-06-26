package org.example.db.modules.dbmain.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResultBean {
    /**
     * 类型
     */
    private String type;
    /**
     * 列名
     */
    private List<String> columns;
    /**
     * 返回行数
     */
    private List<Map<String, Object>> rows;
    /**
     * 返回行数值
     */
    private Integer rowCount;
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 更新个数
     */
    private Integer changes;
}
