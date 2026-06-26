import axios from 'axios';
import router from "@/router";
import {Message} from "element-ui";

const http = axios.create({
    baseURL: '/db', timeout: 1000 * 60
});

http.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['token'] = token;
    }
    return config;
}, error => {
    console.log("拦截器错误", error.response);
    return Promise.reject(error.response?.data || error);
});
let isRedirecting = false;
// 响应拦截器
http.interceptors.response.use((response) => {
    const res = response.data;
    // 👇 关键：业务码判断
    if (res.code === 401) {
        localStorage.removeItem("token");
        router.push("/login");
        if (!isRedirecting) {
            isRedirecting = true;
            Message.error("登录已失效，请重新登录");
            localStorage.removeItem("token");
            router.replace("/login").catch(() => {
            });
            setTimeout(() => {
                isRedirecting = false;
            }, 1000);
        }
        return Promise.reject(new Error("未登录"));
    }
    return response;
}, (error) => {
    return Promise.reject(error);
});


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
        return http.post('/execute', para);
    }, executeAll(para) {
        return http.post('/execute-all', para);
    }, exportAllApi(para) {
        return http.post('/exportAllApi', para, {
            responseType: 'blob'
        }).then((r) => r.data);
    }, login(para) {
        return http.post('/login/submit', para).then((r) => r.data);
    }, getCaptcha() {
        return http.get('/captcha').then((r) => r.data);
    }
};
