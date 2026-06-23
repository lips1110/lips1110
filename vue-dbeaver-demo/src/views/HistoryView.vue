<template>
<div class="sql-editor">
  <div class="editor-toolbar" >
    <el-tooltip content="保存到历史记录" style="margin-left: 0" placement="right">
      <el-button
          type="warning"
          size="mini"
          icon="el-icon-document"
          circle
          plain
          @click="save"
      />
    </el-tooltip>
    <el-tooltip content="清空编辑器" style="margin-left: 0" placement="right">
      <el-button
          type="info"
          size="mini"
          icon="el-icon-delete"
          circle
          plain
          @click="clear"
      />
    </el-tooltip>

    <el-tooltip content="格式化" style="margin-left: 0" placement="right">
      <el-button
          size="mini"
          icon="el-icon-magic-stick"
          circle
          plain
          @click="format"
      />
    </el-tooltip>
  </div>

  <codemirror
      ref="cm"
      v-model="innerValue"
      class="codemirror-wrap"
      :options="cmOptions"
  />
</div>
</template>

<script>
import {codemirror} from "vue-codemirror";

function getLocalStorageSize() {
  let total = 0

  for (let key in localStorage) {
    if (!localStorage.hasOwnProperty(key)) continue

    const value = localStorage.getItem(key)
    total += (key.length + (value ? value.length : 0)) * 2
  }

  // 转 KB
  return (total / 1024).toFixed(2)
}

export default {
  name: "HistoryView",
  components: { codemirror },
  props: {
    historySql: {
      type: String,
      required: true
    },},
  data() {
    return {
      innerValue: '',
      cmInstance: null,
      cmOptions: {
        tabSize: 2,
        mode: 'text/x-sql',
        theme: 'eclipse',
        lineNumbers: true,
        line: true,
        styleActiveLine: true,
        lineWrapping: true,
        autofocus: true
      }
    };
  },
  methods:{
    clear() {
      this.$confirm(
          `确定全部清空吗`,
          '提示',
          {
            confirmButtonText: '保存',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        localStorage.removeItem('sqlContent')
        this.innerValue = '';
        this.$emit('input', '');
        this.$message.success('已清空')
      })

    },
    save(){
      const size = getLocalStorageSize()
      this.$confirm(
          `缓存已占用 ${size}KB，继续保存吗`,
          '提示',
          {
            confirmButtonText: '保存',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        localStorage.setItem('sqlContent', this.innerValue);
        this.$message.success('已保存')
      })
    },
    format() {
      this.innerValue = this.innerValue
          .replace(/\s+/g, ' ')
          .replace(/\s*,\s*/g, ', ')
          .replace(/\bSELECT\b/gi, 'SELECT\n  ')
          .replace(/\bFROM\b/gi, '\nFROM')
          .replace(/\bWHERE\b/gi, '\nWHERE')
          .replace(/\bORDER BY\b/gi, '\nORDER BY')
          .replace(/\bGROUP BY\b/gi, '\nGROUP BY')
          .trim();
      this.$emit('input', this.innerValue);
    },
  },
  mounted() {
    const size = getLocalStorageSize()
    if (size > 500) { // 500KB阈值
      this.$confirm(
          `缓存已占用 ${size}KB，建议清理`,
          '提示',
          {
            confirmButtonText: '清理',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        localStorage.removeItem('sqlContent')
        this.$message.success('已清理缓存')
      })
    }
    this.innerValue = localStorage.getItem('sqlContent')
  },
  watch: {
    historySql: {
      immediate: true,
      handler() {
        this.innerValue = localStorage.getItem('sqlContent')
      }
    },
  },
}
</script>

<style scoped>
.sql-editor {
  display: flex;
  flex: 1;
  min-height: 0;
}

.editor-toolbar {
  width: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  background: #fff;
  border-right: 1px solid #ddd;
}

.codemirror-wrap {
  flex: 1;
  overflow: hidden;
}

.codemirror-wrap >>> .CodeMirror {
  height: 100%;
  font-size: 14px;
  font-family: Consolas, 'Courier New', monospace;
}
</style>
