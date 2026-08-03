# 免申即享政务便民系统

## 平台简介

免申即享政务便民系统是一套基于Spring Boot + Vue3前后端分离的政务便民服务平台，核心功能是实现政策与企业的智能匹配和自动兑付。

## 技术栈

* 后端：Spring Boot 4.0.3 + Spring Security + MyBatis + Redis + JWT
* 前端：Vue 3 + Element Plus + Vite + Pinia
* 数据库：MySQL 8.0 + Redis
* 其他：Druid连接池、PageHelper分页、ECharts图表、智谱AI大模型

## 模块说明

| 模块 | 说明 |
| :--- | :--- |
| exempt-enjoy-admin | Web服务入口，Controller层 |
| exempt-enjoy-framework | 框架核心，安全/配置/AOP |
| exempt-enjoy-system | 系统+业务Service/Mapper/Domain |
| exempt-enjoy-common | 通用工具/注解/异常/常量 |

## 核心功能

* 政策管理：政策发布、规则配置、条件表达式管理
* 智能匹配：规则比对引擎自动匹配政策与企业
* 匹配兑现：匹配→推送→确认→兑付→归档全流程闭环
* 风控评估：信用分+税务状态自动判定风控等级
* AI助手：智谱AI大模型智能问答和PDF规则提取
* 定时任务：自动匹配推送+政策到期预警

## 运行环境

* JDK 17+
* MySQL 8.0+
* Redis
* Node.js 16+

## 启动方式

### 后端
```bash
mvn clean package
java -jar exempt-enjoy-admin/target/exempt-enjoy-admin.jar
```

### 前端
```bash
npm install
npm run dev
```
