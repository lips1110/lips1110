package org.example.db.modules.dbmain.bean;

import lombok.Data;

@Data
public class ExecuteRequest {

    private String sql;

    private Integer pageNum;

    private Integer pageSize;
    
    private Integer index;
}
