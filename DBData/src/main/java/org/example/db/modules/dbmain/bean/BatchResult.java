package org.example.db.modules.dbmain.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BatchResult {
    private String sql;
    private BatchRows result;
    private String type;
}
