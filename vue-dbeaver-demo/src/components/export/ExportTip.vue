<template>
  <div class="box">
    <div v-for="(item, idx) in exportTipSql" :key="idx" class="index-box">
        <el-tooltip placement="top" :content="item">
          <div class="left"> {{ item }}  </div>
        </el-tooltip>

      <div class="right"><el-button size="small"   @click="exportCurrentSql(item)">导出</el-button></div>
    </div>

  </div>
</template>

<script>
export default {
  name: "exportTip",
  data() {
    return {
    }
  },
  props: {
    //绑定传值
    exportTipSql: {
      type: [],
      required: true
    },
  },

  watch:{
    exportTipSql: {
      immediate: true,
      handler(val) {
        console.log(val)
      }
    },
  },
  methods:{
    exportCurrentSql(val){
      this.$emit("export-current",null,null,val )
    }
  }
}
</script>

<style scoped>
.box {
  flex-direction: column;
}
.left{
  font-family: Consolas, monospace;
  font-size: 12px;
  line-height: 26px;
  color: #606266;
  background: #f0f0f0;

  margin-bottom: 2px;
  padding: 2px 4px;
  border-radius: 3px;

  float: left;
  width: 600px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.right{
  width: 80px;
  float: right;
  margin-left: auto;
}

.index-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

</style>
