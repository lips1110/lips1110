<template>
  <div class="result-table">
    <el-table
        v-if="data.columns && data.columns.length"
        :data="data.rows"
        border
        stripe
        size="mini"
        height="300px"
        style="width:100%"
        label="序号"
    >
      <el-table-column
          v-for="col in data.columns"
          :key="col"
          :prop="col"
          :label="col"
          min-width="100"
          :table-layout="'fixed'"
          show-overflow-tooltip
      />
    </el-table>
    <div v-else class="no-rows">
      查询成功，无数据返回
    </div>
    <div class="pagination-wrapper">
      <el-pagination
          background
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10,100,300,1000]"
          :total="data.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
  </div>

</template>

<script>
export default {
  name: 'ResultTable',

  props: {
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    },
    data: {
      type: Object,
      required: true
    }
  },
  mounted() {
  },

  computed: {
  },
  methods:{
    handleCurrentChange(page) {
      this.$emit('page-change', {
        page: page,
        size: this.pageSize
      });
    },

    handleSizeChange(size) {
      this.$emit('page-change', {
        page: 1,
        size: size
      });
    }

  }
};
</script>

<style scoped>
.no-rows {
  color: #909399;
  font-size: 13px;
  padding: 8px;
}
.result-table {
  overflow: auto;
  min-height: 100px;
  border: 1px solid #dcdfe6;
}

.pagination-wrapper {
  height: 50px;
  padding: 8px 20px;
  box-sizing: border-box;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1px solid #dcdfe6;
}
</style>
