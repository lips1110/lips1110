<template>
  <div class="table-columns-view">
    <div class="tab-bar">
      <div class="tab active">
        <i class="el-icon-menu" />
        {{ tableName }} - 列
      </div>
    </div>

    <div class="content" v-loading="loading">
      <el-table :data="columns" border stripe size="small" style="width: 100%">
        <el-table-column prop="CID" label="#" width="60" />
        <el-table-column prop="NAME" label="列名" min-width="140" />
        <el-table-column prop="TYPE" label="类型" min-width="120" />
        <el-table-column prop="NOTNULL" label="非空" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.notnull ? 'danger' : 'info'" size="mini">
              {{ row.notnull ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="DFLT_VALUE" label="默认值" min-width="100" />
        <el-table-column prop="PK" label="主键" width="80">
          <template slot-scope="{ row }">
            <el-tag v-if="row.PK" type="warning" size="mini">PK</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'TableColumns',
  data() {
    return {
      loading: false,
      columns: []
    };
  },
  computed: {
    tableName() {
      return this.$route.params.tableName;
    }
  },
  watch: {
    tableName: {
      immediate: true,
      handler() {
        this.loadColumns();
      }
    }
  },
  methods: {
    async loadColumns() {
      this.loading = true;
      try {
        this.columns = await api.getTableColumns(this.tableName);
      } catch (e) {
        this.$message.error('加载列信息失败');
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
