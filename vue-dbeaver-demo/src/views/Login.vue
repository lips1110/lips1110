<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header">
        <span>用户登录</span>
      </div>

      <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="rules"
          label-width="80px"
          @keyup.enter.native="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              style="width:100%"
              :loading="loading"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'Login',

  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          {required: true, message: '请输入用户名', trigger: 'blur'}
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'}
        ]
      }
    }
  },

  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) {
          return
        }

        this.loading = true
        const res = await api.login(this.loginForm);
        if (res.code === 200) {
          localStorage.setItem( 'token',res.data )
          this.$message.success('登录成功')
          await this.$router.push('/DB')
        } else {
          this.loading = false
          this.$message.error(res.message || '登录失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  /* 背景图 */
  background: url("~@/assets/back.jpg") no-repeat center center;
  width: 100%;
  height: 98vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
}

html,
body,
#app {
  height: 100%;
}
</style>
