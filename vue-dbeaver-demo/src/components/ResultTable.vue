<template>
  <div class="result-table">
    <el-table
        v-if="data.columns && data.columns.length"
        :data="data.rows"
        border
        stripe
        size="mini"
        height="100%"
        style="width:100%"
    >
      <el-table-column
          v-for="col in data.columns"
          :key="col"
          :prop="col"
          :label="col"
          min-width="100"
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
          :page-sizes="[10,100,500,1000]"
          :total="data.rowCount"
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
    currentPage: Number,
    pageSize: Number,
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
        pageNum: page,
        pageSize: this.pageSize
      });
    },

    handleSizeChange(size) {
      this.$emit('page-change', {
        pageNum: 1,
        pageSize: size
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
  max-height:600px;
  border: 1px solid #dcdfe6;
}

.pagination-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;

  height: 50px;
  padding: 8px 20px;
  box-sizing: border-box;

  background: #fff;
  //border-top: 1px solid #dcdfe6;

  display: flex;
  justify-content: flex-end;
  align-items: center;

  z-index: 999;
}
</style>
