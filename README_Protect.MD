# rwmodProtect

+ rwmodProtect 是一个专为Rusted Warfare（铁锈战争）设计的mod混淆器。它以较低的性能代价提供较好的防护效果和兼容性。

## 功能
- [x] 封装加固
- [x] 伪造文件夹<br>将文件伪装为文件夹（这不能对音乐文件使用），使常规程序，无法正常浏览文件。如果仅剔除伪文件夹，依然需要封包或修正。
- [x] 重构文件树<br>移除明显的关系树，并使用特殊字符干扰阅读，这可能对性能有所改善。
- [x] 精简文件<br>移除无用文件和代码，并使用最大压缩，这将改善占用和性能，不过由于混淆的屎山实现，这可能生成一些可以避免的代码。
- [ ] 更多暂不支持

## 兼容性问题
* 加固的文件。如果文件已经加壳，这将出错。
* 循环引用。当发生一个loop，会卡死。
* ${section.key}。访问变更的内容，这将引发问题。
* all-units.template<br>（移除兼容）当复制另一个.ini并指于自己向不同的all-units.template时出错<br>all-units.template优先于copyFrom。使用copyFrom复制ini，不会被追加ini持有的all-units.template。
* 请避免使用all-units.template。诺要完全兼容all-units.template，将会大幅降低混淆效果。复制多个目标的all-units.template相同，会有格外的性能代价，除此之外这可能引起未跳过节，这是因为插入循序导致的。
* 故意绕过的空组。使用分割符号不要空元素，如：“,”，“a,”等，此外元素不应该包含“ ”，文件除外。
* 非法符号。文件名称不应该与混淆字符相同，此外值不能包含[.]所排除的特殊unicode。
* @copyFrom_skipThisSection<br>（移除兼容）此值不能为 false,0,IGNORE。<br>@copyFrom_skipThisSection定义为false或0时拿回之前的所有键，反之丢弃。
* 不规范的持有<br>持有 mod-info.txt、auto、..、. 等特殊名称等文件，还有背景音乐，地图和地图图像，使打包出错或造成多次复制。
* dont_load。不加载的内容，如果这个ini没有被使用这将导致错误。
* 虚引用。在一些模板文件或节引用从未使用的资源，这将导致这些资源被打包。
* 错误的数学表达式。如果与游戏静态运算不兼容，这将导致栈溢出。
* 非法路径<br>使用"/"或"\\"作为路径头或尾部，以及连续使用这都是不支持的。
* 重排序<br>由于屎山代码，对多态值进行强制衍射以及重复值移除，这可能引起重排序，特别是action的显示循序、autoTrigger和autoTriggerOnEvent事件链的执行循序，此外也会造成rwsave的兼容问题。

请避免多态，对宏以及多态资源的实现基于暴力衍射，这可能引起一些兼容问题和格外的性能代价。

## 配置
配置在/sdcard/Android/data/rust.rwTool/files，避免卸载残留。
* .ini<br>默认的加载配置，这将覆盖安装包内的加载配置。
* lib.zip<br>默认的加载依赖，用于copyFrom:CORE:实现。
