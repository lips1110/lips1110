package org.example.db.modules.dbmain.mapper;

import java.util.HashMap;

public interface SqlExecuteMapper {
    
    HashMap<String,Object> select(String sql);
}
