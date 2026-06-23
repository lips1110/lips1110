import axios from 'axios';

const http = axios.create({
    baseURL: '/db', timeout: 1000*60
});

http.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['token'] = token;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器
http.interceptors.response.use(
    (response) => {
        const res = response.data;

        // 👇 关键：业务码判断
        if (res.code === 401) {
            localStorage.removeItem("token");
            window.location.href = "/login";
            this.$message.success('登录失效，请重新登录')
            return Promise.reject(new Error("未登录"));
        }

        return response;
    },
    (error) => {
        // 这里只处理真正 HTTP 错误（如 500 / 网络错误）
        return Promise.reject(error);
    }
);

export default {
    health() {
        return http.get('/health').then((r) => r.data);
    }, getTree() {
        return http.get('/tables').then((r) => r.data);
    }, getTableColumns(tableName) {
        return http.get(`/tables/${tableName}/columns`).then((r) => r.data);
    }, getTableData(tableName, param) {
        return http.post(`/tables/data`, param).then((r) => r.data);
    }, executeSql(para) {
        return http.post('/execute', para).then((r) => r.data);
    }, executeAll(para) {
        return http.post('/execute-all', para).then((r) => r.data);
    }, exportAllApi(para) {
        return http.post('/exportAllApi', para, {
            responseType: 'blob'
        }).then((r) => r.data);
    }, login(para) {
        return http.post('/login/submit', para).then((r) => r.data);
    },
};
