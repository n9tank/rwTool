[![GitHub stars](https://img.shields.io/github/stars/n9tank/rwmodProtect)](https://githubfast.com/n9tank/rwmodProtect/stargazers) [![GitHub forks](https://img.shields.io/github/forks/n9tank/rwmodProtect)](https://githubfast.com/n9tank/rwmodProtect/network) [![GitHub issues](https://img.shields.io/github/issues/n9tank/rwmodProtect)](https://githubfast.com/n9tank/rwmodProtect/issues)

# 介绍
 rwTool 是一个专为Rusted Warfare（铁锈战争）设计的工具集。提供混淆，封包拆包，提取地图文件，地图文件精简等功能。

# 功能

- [Protect](README_Protect.MD)
混淆器功能，详细见页面。

- pack/unpack
尽可能将文件伪装成文件夹，拆包则是还原，用于规避常规文件浏览器。

- 提取地图文件
尝试从rwsave/replay文件提取tmx，并写出。

对于编码使用2字节以及4字节变体的难以处理，因此仅提取可能的最大文。
这将无法运行，需要手动修复。
这是为了避免字节扫描的漏洞，否则需要提供每个编码的输出文件。

- [地图精简](https://github.com/Timeree/RwMapCompressor)
基于RwMapCompressor修改的地图压缩器，提供强力的压缩效果。
（建议使用v1模板）

- png压缩
基于pngquant，压缩效果不会太差。

# 开关

错误日志会保存在在Android/data/rust.rwTool/cache。（未提供Debug版本）

- lib 启用加载依赖，这将使用内置的lib.zip作为加载依赖。

- raw 启用原始流，不解压条目操作更快，部分场景不可用。

# 公开配置 

- head 决定zip文件头，为空禁用，只有一个条目则只写出头，用“,”分割，格式：最小大小，随机大小，…。

- end 用于伪装Zip64尾部，在低版本jdk不兼容。

- split 文件路径“/”分割计算，为空禁用，格式：NN。(0-9)，最大分割数-1，最大分割长度-1。

- dumpMax 在解压时超过该长度中断条目。

# [构建](BUILD.MD)
- [下载](https://gitee.com/n9tank/rwTool/releases)