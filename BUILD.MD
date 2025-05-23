# 构建指南

### 配置环境
- 至少openjdk8
- gradle
- android sdk
### 依赖库环境
- android ndk
- rustc-nightly rustc-src
- build-essential

### 手动添加gradle wrapper
请复制gradle warpper，它被忽略上传了。

## Android
### 签名
准备你的签名文件，并适当修改配置。

### 导入依赖库
- [ZipParallel](https://github.com/n9tank/ZipParallel)
- [pngquant](https://github.com/n9tank/pngquant-android)

我提供了一份armv8_64的构建文件，这可能是过时的，强烈建议自行编译，将编译文件放在app/libs下。

### 构建依靠库
导入这些库的依赖文件。
- ZipParallel:[libdeflate-android](https://github.com/n9tank/libdeflate-android)
- pngquant:libspng,libImagequant

gitmodules拉取不可用，尚未完成，请手动下载依赖。

* 增加架构目标

```
abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
```

* pngquant

对于静态库，使用gradle构建需要配置[targets](https://blog.csdn.net/qingfeng812/article/details/132674778)。

构建libImagequant，请先修改两处Cargo.toml

``` 配置
[profile.release]
- opt-level = 3
- lto = true 
- codegen-units = 1
- panic = "abort"
- strip = true
```

在libImagequant_sys路径构建

``` 命令
cargo +nightly build --target $TARGET_ARCH --release -Z build-std=std,panic_abort -Z build-std-features=panic_immediate_abort
```
在termux上构建，请在proot（卡成翔）容器下工作，否则不支持rustup会导致嵌入的std无法重新构建而膨胀。

rust环境并不需要android ndk，我还尚未编写cargo一键构建，请导出静态库作为链接。

推荐使用github actions，这样就不需要折腾ArchLinux，以及rust工具链了。
[android-aarch64-ci](https://github.com/n9tank/libimagequant-android-aarch64-ci)

对于其他平台请使用cmake构建。

如果同时构建多个目标，需要修改camkeList.txt，使其适配。


### 编译

./gradlew apk:assembleRelease

## jar程序

jar程序尚未适配完成，这仅仅作为实验。

### 导入依赖库

jar程序不支持直接导入AAR，你需要依赖库jar导入在libs。

如果使用jar内加载动态库，需要先格外解压，为了兼容性，动态库请只在打包时加入。

### 编译

./gradlew jar:assemble

### 打包所有文件

把jar，以态库还有配置依赖压缩在一起，通过bat启用就可以加载在jre上了。
当然一些程序可以直接打包成exe。