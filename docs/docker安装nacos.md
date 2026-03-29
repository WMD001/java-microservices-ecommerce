---
title: docker安装nacos
---

## 拉取镜像

```bash
docker pull nacos/nacos-server:v3.2.0
```

## 启动容器

```bash
docker run --name nacos-standalone-derby \
    -e MODE=standalone \
    -e NACOS_AUTH_TOKEN=MTIzNDU2Nzg5MHF3ZXJ0eXVpb3Bhc2RmZ2hqa2w7W10= \
    -e NACOS_AUTH_IDENTITY_KEY=nacos \
    -e NACOS_AUTH_IDENTITY_VALUE=nacos \
    -p 8080:8080 \
    -p 8848:8848 \
    -p 9848:9848 \
    -d nacos/nacos-server:v3.2.0
```

### 参数说明

- **--name nacos-standalone-derby** 指定容器名称
- **-e MODE=standalone** 以单机模式启动
- **-e NACOS_AUTH_TOKEN=${your_nacos_auth_secret_token}** 指定JWT生成的密钥，需要是32位及以上字符的Base64编码形式
- **-e NACOS_AUTH_IDENTITY_KEY=nacos** 和 **-e NACOS_AUTH_IDENTITY_VALUE=nacos** 配置nacos服务端之间身份识别的key 和value
- **-p 8080:8080**Nacos控制台端口，访问Nacos控制台及Nacos控制台的API
- **-p 8848:8848**Nacos HTTP API 端口，用于Nacos AdminAPI及HTTP OpenAPI的访问
- **-p 9848:9848**客户端gRPC请求服务端端口，用于客户端向服务端发起连接和请求
- **-d**表示后台运行
- **nacos/nacos-server:v3.2.0**指定要使用的容器镜像，选择上一步中拉取的镜像名称

### 查看启动日志

```bash
docker logs -f 容器id
```

启动后浏览器访问 localhost:8080 ，初次访问提示设置控制台登录密码，登录后进入到控制台页面。
