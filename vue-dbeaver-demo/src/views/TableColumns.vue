<template>
  <div class="table-columns-view">
    <div class="tab-bar">
      <div class="tab active">
        <i class="el-icon-menu"/>
        {{ tableName }} - 列
      </div>
      <el-button size="mini" icon="el-icon-edit-outline" @click="openInScript">在脚本中查询</el-button>
    </div>

    <div class="content" v-loading="loading">
      <el-table
          :data="columns"
          border
          stripe
          size="small"
          style="width: 100%"
      >
        <el-table-column prop="CID" label="#" width="60"/>
        <el-table-column prop="NAME" label="列名" min-width="140"/>
        <el-table-column prop="TYPE" label="类型" min-width="120"/>

        <el-table-column label="非空" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.NOTNULL ? 'danger' : 'info'" size="mini">
              {{ row.NOTNULL ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="DFLT_VALUE" label="默认值" min-width="100"/>

        <el-table-column label="主键" width="80">
          <template slot-scope="{ row }">
            <el-tag v-if="row.PK" type="warning" size="mini">PK</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <span slot="footer">
      <el-button @click="close">关闭</el-button>
    </span>
  </div>
</template>

<script>
import api from "../api";

export default {
  name: "TableColumns",

  props: {
    tableName: {
      type: String,
      required: true
    },
    visible: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      loading: false,
      columns: []
    };
  },

  watch: {
    tableName: {
      immediate: true,
      handler() {
        this.loadColumns();
      }
    },
    visible(val) {
    },
    activeSql:{

    }
  },

  methods: {
    openInScript() {
      let sql = `SELECT * FROM ${this.tableName}`;
      this.$emit("open-sql", sql)
      this.close();
    },
    close() {
      this.$emit("cancel");
    },
    async loadColumns() {
      if (!this.tableName) return;

      this.loading = true;
      try {
        const res = await api.getTableColumns(this.tableName);
        this.columns = res || [];
      } catch (e) {
        this.$message.error("加载列信息失败");
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.table-columns-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tab-bar {
  display: flex;
  background: #e8e8e8;
  border-bottom: 1px solid #c0c0c0;
  height: 28px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  font-size: 12px;
  background: #fff;
  border-right: 1px solid #c0c0c0;
  color: #333;
}

.content {
  flex: 1;
  overflow: auto;
  padding: 12px;
}
</style>
