# ToDo List

## 完成

- [x] 完成 ao/roughness/metallic 的支持
- [x] 颜色管理，sRGB -> Linear in shader -> output sRGB
- [x] 将name从attribute和uniform中拆出来，方便复用
- [x] 处理缺失attribute/uniform(texture)的情况, 当前没有处理(例如TANGENT),
  也许可以自动添加宏定义 `#define HAS_XXX_XXX`,
  例如: `HAS_UNIFORM_BASE_COLOR_TEXTURE`, `HAS_ATTRIBUTE_TANGENT`
- [x] BRDF材质系统
- [x] 灯光系统

## 高优先级

- [ ] Dirty/EventBus/Dispose系统改进和实现
- [ ] Animation
- [ ] Morph Target
- [ ] GLTF Loader支持路径加载(文件夹模式), 目前只支持二进制glb
- [ ] Shader GLSL预处理: 支持Renderer添加宏定义
- [ ] Shadow Map / Multi Pass / Post Processing

## 低优先级

- [ ] 内置形状(Cube, Plane, etc), 内置Primitive(Mesh, Line, Point)
- [ ] 内存优化，尽可能减少copy，支持调用`Primitive#gpuOnly`，将数据从CPU拷贝到GPU后自动释放CPU内存
- [ ] 优化渲染流程, 尽可能减少状态切换