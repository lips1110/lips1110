<template>
  <div class="sql-editor">
    <div class="editor-toolbar" >
      <el-tooltip   content="执行当前 SQL" placement="right">
        <el-button
          type="primary"
          size="mini"
          icon="el-icon-video-play"
          circle
          @click="$emit('execute-current')"
        />
      </el-tooltip>

      <el-tooltip style="margin-left: 0" content="执行全部 SQL" placement="right">
        <el-button
          type="success"
          size="mini"
          icon="el-icon-caret-right"
          circle
          @click="$emit('execute-all')"
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
      @ready="onReady"
    />
  </div>
</template>

<script>
import { codemirror } from 'vue-codemirror';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/eclipse.css';
import 'codemirror/mode/sql/sql.js';
import 'codemirror/addon/selection/active-line.js';

export default {
  name: 'SqlEditor',
  components: { codemirror },
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      innerValue: this.value,
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
  watch: {
    value(val) {
      if (val !== this.innerValue) {
        this.innerValue = val;
      }
    },
    innerValue(val) {
      this.$emit('input', val);
    }
  },
  created() {
  },
  methods: {
    onReady(cm) {
      this.cmInstance = cm;
    },
    getCurrentStatement() {
      if (!this.cmInstance) return this.innerValue.trim();

      const doc = this.cmInstance.getDoc();
      const selection = doc.getSelection();
      if (selection && selection.trim()) {
        return selection.trim();
      }

      const cursor = doc.getCursor();
      const line = cursor.line;
      const totalLines = doc.lineCount();

      let start = line;
      let end = line;

      while (start > 0) {
        const prev = doc.getLine(start - 1).trim();
        if (!prev || prev.endsWith(';')) break;
        start--;
      }

      while (end < totalLines - 1) {
        const next = doc.getLine(end + 1).trim();
        if (!next) break;
        end++;
        if (next.endsWith(';')) break;
      }

      const lines = [];
      for (let i = start; i <= end; i++) {
        lines.push(doc.getLine(i));
      }

      return lines.join('\n').trim();
    },
    clear() {
      this.innerValue = '';
      this.$emit('input', '');
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
    refresh() {
      if (this.cmInstance) {
        this.cmInstance.refresh();
      }
    },
    //定时保存函数
    saveSql() {
      const cacheSql = localStorage.getItem('sqlContent');
      console.log(cacheSql)
      if (cacheSql){
        if (!cacheSql.includes( this.innerValue)){
          localStorage.setItem('sqlContent', this.innerValue.concat(" \n",cacheSql));
        }
      }

    }
  }
};
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
