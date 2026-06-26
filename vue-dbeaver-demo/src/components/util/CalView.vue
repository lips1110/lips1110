<template>
  <div class="calculator">
    <div class="screen">
      <div class="expression">{{ expression || '0' }}</div>
      <div class="result">{{ result }}</div>
    </div>

    <div class="buttons">
      <button @click="clearAll">C</button>
      <button @click="backspace">⌫</button>
      <button @click="append('(')">(</button>
      <button @click="append(')')">)</button>

      <button @click="append('7')">7</button>
      <button @click="append('8')">8</button>
      <button @click="append('9')">9</button>
      <button class="op" @click="append('/')">÷</button>

      <button @click="append('4')">4</button>
      <button @click="append('5')">5</button>
      <button @click="append('6')">6</button>
      <button class="op" @click="append('*')">×</button>

      <button @click="append('1')">1</button>
      <button @click="append('2')">2</button>
      <button @click="append('3')">3</button>
      <button class="op" @click="append('-')">−</button>

      <button @click="append('0')">0</button>
      <button @click="append('.')">.</button>
      <button class="equal" @click="calculate">=</button>
      <button class="op" @click="append('+')">+</button>
    </div>
  </div>
</template>

<script>
import { evaluate } from 'mathjs'

export default {
  name: 'Calculator',

  data() {
    return {
      expression: '',
      result: '0'
    }
  },

  mounted() {
    window.addEventListener('keydown', this.handleKeydown)
  },

  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeydown)
  },

  methods: {
    append(value) {
      this.expression += value
      this.preview()
    },

    clearAll() {
      this.expression = ''
      this.result = '0'
    },

    backspace() {
      this.expression = this.expression.slice(0, -1)
      this.preview()
    },

    calculate() {
      if (!this.expression) return

      try {
        const value = evaluate(this.expression)
        this.result = String(value)
        this.expression = String(value)
      } catch (e) {
        this.result = 'Error'
      }
    },

    preview() {
      if (!this.expression) {
        this.result = '0'
        return
      }

      try {
        this.result = String(
            evaluate(this.expression)
        )
      } catch (e) {
        // 输入过程中允许表达式不完整
      }
    },

    handleKeydown(e) {
      const key = e.key

      if (/^[0-9]$/.test(key)) {
        this.append(key)
        return
      }

      if (['+', '-', '*', '/', '.', '(', ')'].includes(key)) {
        this.append(key)
        return
      }

      if (key === 'Backspace') {
        this.backspace()
        return
      }

      if (key === 'Delete') {
        this.clearAll()
        return
      }

      if (key === 'Enter') {
        this.calculate()
      }
    }
  }
}
</script>

<style scoped>
.calculator {
  width: 360px;
  height: 520px;
  background: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 12px;
  padding: 14px;
  box-sizing: border-box;
  user-select: none;
}

.screen {
  height: 110px;
  border-radius: 10px;
  background: #f5f7fa;
  padding: 12px;
  box-sizing: border-box;
  margin-bottom: 14px;
}

.expression {
  height: 45px;
  font-size: 26px;
  text-align: right;
  overflow: hidden;
  word-break: break-all;
}

.result {
  height: 35px;
  line-height: 35px;
  text-align: right;
  color: #909399;
  font-size: 18px;
}

.buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.buttons button {
  height: 68px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 22px;
  background: #f2f3f5;
  transition: all .15s;
}

.buttons button:hover {
  transform: translateY(-1px);
}

.op {
  font-weight: bold;
}

.equal {
  font-weight: bold;
}
</style>
