module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/db': {
        target: 'http://localhost:18000',
        changeOrigin: true
      }
    },
    client:{
      overlay:false
    }
  },
  lintOnSave: false
};
