# Travel Finder - 旅游规划 App 设计文档

## 1. 项目概述

### 1.1 项目简介
一款基于 Android 平台的旅游规划应用，核心功能包括：
- 爬取小红书等平台的旅行帖子，提取景点/兴趣点（POI）
- 在地图上展示 POI 位置
- 用户基于评价/评分规划出行计划

### 1.2 技术选型

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| **语言** | Kotlin | Android 首选，支持扩展函数 |
| **架构** | Clean Architecture + MVVM | 清晰分层，职责分离 |
| **DI** | Hilt | Google 官方依赖注入框架 |
| **数据库** | Room | 本地缓存，支持离线访问 |
| **网络** | Retrofit + OkHttp | REST API 调用 |
| **异步** | Coroutines + Flow | 协程 + 响应式数据流 |
| **地图** | 高德地图 SDK (AMap) | 中国区 POI 数据更精准 |
| **图片** | Coil | Kotlin 首选图片加载库 |
| **序列化** | Kotlin Serialization | JSON 解析 |

### 1.3 项目结构

```
com.travelfinder/
├── data/                           # 数据层
│   ├── local/                       # 本地数据存储
│   │   ├── dao/                    # Data Access Objects
│   │   ├── db/                      # Room Database
│   │   └── entity/                  # 数据库实体
│   ├── remote/                      # 远程数据源
│   │   ├── api/                    # Retrofit API 接口
│   │   └── crawler/                 # 爬虫实现
│   └── repository/                  # Repository 实现
│
├── domain/                          # 领域层（核心业务逻辑）
│   ├── model/                       # 领域模型
│   ├── repository/                  # Repository 接口
│   └── usecase/                     # 用例（Interactor）
│
├── presentation/                    # 展示层
│   ├── ui/                          # Activity/Fragment
│   ├── viewmodel/                   # ViewModel
│   ├── state/                       # UI 状态类
│   └── adapter/                     # RecyclerView Adapter
│
├── di/                              # 依赖注入模块
│
└── util/                            # 工具类
```

---

## 2. OOP 设计原则应用

### 2.1  SOLID 原则

| 原则 | 应用场景 |
|------|----------|
| **单一职责 (SRP)** | `CrawlerStrategy` 只负责爬取，`PostRepository` 只负责数据访问 |
| **开闭原则 (OCP)** | 新增 `DouyinCrawler` 不需修改现有 `CrawlerFactory` 代码 |
| **里氏替换 (LSP)** | 任何 `CrawlerStrategy` 实现都可被 `CrawlerFactory` 使用 |
| **接口隔离 (ISP)** | 小接口如 `CrawlerStrategy`，避免庞大接口 |
| **依赖倒置 (DIP)** | `PostRepository` 是接口，`PostRepositoryImpl` 实现它 |

### 2.2 设计模式

| 模式 | 实现 | 说明 |
|------|------|------|
| **策略模式** | `CrawlerStrategy` 接口 + `XiaohongshuCrawler` / `AmapPoiCrawler` | 支持多种爬虫平台，可动态切换 |
| **工厂模式** | `CrawlerFactory` | 根据数据源类型创建对应爬虫实例 |
| **仓库模式** | `PostRepository` / `TripRepository` | 统一数据访问，支持本地缓存 fallback |
| **建造者模式** | `TripPlanBuilder` | 构建复杂 `TripPlan` 对象 |
| **观察者模式** | `Flow` / `StateFlow` | 响应式数据流，UI 自动更新 |
| **单例模式** | `@Singleton` Hilt Scope | 数据库实例等全局单例 |
| **模板方法模式** | `BaseCrawler` + 子类特化 | 定义爬虫通用流程，子类实现差异化部分 |

---

## 3. 核心类设计

### 3.1 领域模型 (Domain Models)

```kotlin
// 地理位置值对象
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)

// 所有可定位对象的抽象基类
abstract class Locatable(
    open val id: String,
    open val name: String,
    open val location: Location,
    open val rating: Float
)

// 兴趣点
data class POI(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float,
    val description: String,
    val images: List<String>,
    val tags: List<String>,
    val reviews: List<Review>,
    val visitOrder: Int? = null,        // 规划顺序
    val stayDuration: Int? = null,       // 预计停留时间（分钟）
    val likes: Int = 0,
    val source: String = ""
) : Locatable(id, name, location, rating)

// 用户评价
data class Review(
    val id: String,
    val author: String,
    val content: String,
    val rating: Float,
    val images: List<String>,
    val date: Long
)

// 小红书帖子
data class Post(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float,
    val author: String,
    val content: String,
    val images: List<String>,
    val tags: List<String>,
    val likes: Int,
    val publishDate: Long
) : Locatable(id, name, location, rating) {

    fun toPOI(): POI = POI(
        id = id, name = name, location = location, rating = rating,
        description = content, images = images, tags = tags,
        reviews = emptyList(), source = "小红书"
    )
}

// 行程规划
data class TripPlan(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float = 0f,
    val pois: MutableList<POI> = mutableListOf(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Locatable(id, name, location, rating) {

    fun calculateOverallRating(): Float = ...
    fun totalStayDuration(): Int = ...
    fun addPOI(poi: POI, order: Int? = null): TripPlan = ...
    fun removePOI(poiId: String): TripPlan = ...
}

// 行程建造者
class TripPlanBuilder {
    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun location(location: Location) = apply { this.location = location }
    fun addPOI(poi: POI) = apply { pois.add(poi) }
    fun build(): TripPlan = ...
}
```

### 3.2 类继承层次图

```
Locatable (抽象基类)
├── Post (小红书帖子)
├── POI (兴趣点)
└── TripPlan (行程规划)

Post --toPOI()--> POI
TripPlan --contains--> POI
POI --hasMany--> Review
```

### 3.3 Repository 接口设计

```kotlin
interface PostRepository {
    suspend fun searchPosts(keyword: String): Result<List<Post>>
    suspend fun getPostsByLocation(lat: Double, lng: Double, radius: Int): Result<List<Post>>
    suspend fun getPostById(id: String): Result<Post>
    suspend fun savePosts(posts: List<Post>)
    fun getSavedPosts(): Flow<List<Post>>
    fun getSupportedSources(): List<String>
}

interface TripRepository {
    suspend fun createTripPlan(tripPlan: TripPlan): Result<TripPlan>
    fun getAllTripPlans(): Flow<List<TripPlan>>
    suspend fun getTripPlanById(id: String): Result<TripPlan>
    suspend fun updateTripPlan(tripPlan: TripPlan): Result<TripPlan>
    suspend fun deleteTripPlan(id: String): Result<Unit>
}
```

### 3.4 爬虫策略模式

```kotlin
// 爬虫策略接口
interface CrawlerStrategy {
    suspend fun crawl(keyword: String): Result<List<Post>>
    suspend fun crawlByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 5000
    ): Result<List<Post>>
    fun getSourceName(): String
    fun getSourceId(): String
}

// 小红书爬虫
class XiaohongshuCrawler : CrawlerStrategy { ... }

// 高德地图爬虫
class AmapPoiCrawler : CrawlerStrategy { ... }

// 爬虫工厂
class CrawlerFactory(
    private val crawlers: Map<String, CrawlerStrategy>
) {
    fun getCrawler(sourceId: String): CrawlerStrategy? = crawlers[sourceId]
    fun getSupportedSources(): List<Pair<String, String>> = ...
}

enum class DataSource(val id: String, val displayName: String) {
    XIAOHONGSHU("xiaohongshu", "小红书"),
    AMAP("amap", "高德地图")
}
```

### 3.5 用例设计

```kotlin
// 搜索 POI
class SearchPOIsUseCase(private val postRepository: PostRepository) {
    suspend operator fun invoke(keyword: String): Result<List<POI>> {
        return postRepository.searchPosts(keyword).map { posts ->
            posts.map { it.toPOI() }
        }
    }
}

// 创建行程
class CreateTripPlanUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(
        name: String,
        pois: List<POI>,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<TripPlan> = ...
}

// 获取所有行程
class GetAllTripPlansUseCase(private val tripRepository: TripRepository) {
    operator fun invoke() = tripRepository.getAllTripPlans()
}

// 添加 POI 到行程
class AddPOIToTripUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(tripId: String, poi: POI, order: Int? = null): Result<TripPlan> = ...
}
```

---

## 4. 数据库设计

### 4.1 Room Entities

```kotlin
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float,
    val author: String,
    val content: String,
    val images: String,        // JSON string
    val tags: String,          // JSON string
    val likes: Int,
    val publishDate: Long,
    val cachedAt: Long
)

@Entity(tableName = "trip_plans")
data class TripPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val centerAddress: String,
    val rating: Float,
    val poisJson: String,      // JSON array
    val startDate: Long?,
    val endDate: Long?,
    val createdAt: Long
)

@Entity(tableName = "pois")
data class POIEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float,
    val description: String,
    val images: String,
    val tags: String,
    val likes: Int,
    val source: String
)

@Entity(
    tableName = "trip_pois",
    primaryKeys = ["tripId", "poiId"]
)
data class TripPOIEntity(
    val tripId: String,
    val poiId: String,
    val visitOrder: Int,
    val stayDuration: Int?
)
```

### 4.2 数据库关系图

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│ TripPlan    │────<│  TripPOI     │>────│    POI      │
│   Entity    │     │  (关联表)     │     │   Entity    │
└─────────────┘     └──────────────┘     └─────────────┘
                                               │
                                               │
                                        ┌──────┴──────┐
                                        │   Post      │
                                        │   Entity    │
                                        └─────────────┘
```

---

## 5. UI 架构 (MVVM)

### 5.1 UI State 设计

```kotlin
sealed class MapUiState {
    object Idle : MapUiState()
    object Loading : MapUiState()
    data class Success(val pois: List<POI>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

sealed class TripListUiState {
    object Loading : TripListUiState()
    data class Success(val trips: List<TripPlan>) : TripListUiState()
    data class Empty(val message: String = "暂无行程规划") : TripListUiState()
    data class Error(val message: String) : TripListUiState()
}

sealed class TripPlanningUiState {
    object Idle : TripPlanningUiState()
    object Loading : TripPlanningUiState()
    data class Success(val tripName: String, val pois: List<POI>) : TripPlanningUiState()
    data class Error(val message: String) : TripPlanningUiState()
}
```

### 5.2 ViewModel 设计

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchPOIsUseCase: SearchPOIsUseCase,
    private val searchPOIsByLocationUseCase: SearchPOIsByLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _selectedPOI = MutableStateFlow<POI?>(null)
    val selectedPOI: StateFlow<POI?> = _selectedPOI.asStateFlow()

    fun searchPOIs(keyword: String) { ... }
    fun searchPOIsNearby(latitude: Double, longitude: Double, radiusMeters: Int = 5000) { ... }
    fun selectPOI(poi: POI) { ... }
}

@HiltViewModel
class TripViewModel @Inject constructor(
    private val createTripPlanUseCase: CreateTripPlanUseCase,
    private val getAllTripPlansUseCase: GetAllTripPlansUseCase,
    private val getTripPlanDetailUseCase: GetTripPlanDetailUseCase,
    private val addPOIToTripUseCase: AddPOIToTripUseCase,
    private val removePOIFromTripUseCase: RemovePOIFromTripUseCase,
    private val deleteTripPlanUseCase: DeleteTripPlanUseCase
) : ViewModel() {

    private val _tripListState = MutableStateFlow<TripListUiState>(TripListUiState.Loading)
    val tripListState: StateFlow<TripListUiState> = _tripListState.asStateFlow()

    private val _selectedPOIs = MutableStateFlow<List<POI>>(emptyList())
    val selectedPOIs: StateFlow<List<POI>> = _selectedPOIs.asStateFlow()

    fun createTripPlan(name: String, startDate: Long? = null, endDate: Long? = null) { ... }
    fun addPOIToCurrentTrip(poi: POI) { ... }
    fun removePOIFromSelection(poiId: String) { ... }
}
```

### 5.3 Fragment 与布局

| Fragment | 布局 | 功能 |
|----------|------|------|
| `MainActivity` | `activity_main.xml` | 导航容器 + 底部导航 |
| `HomeFragment` | `fragment_home.xml` | 行程列表展示 |
| `MapFragment` | `fragment_map.xml` | 地图 + POI 搜索/展示 |
| `PlanningFragment` | `fragment_planning.xml` | 行程创建/编辑 |

### 5.4 页面导航流程

```
┌─────────────────┐
│  MainActivity   │
│  (导航容器)      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│           BottomNavigation              │
├─────────────┬─────────────┬──────────────┤
│   首页      │    地图     │     规划     │
│ HomeFragment│ MapFragment │PlanningFrag │
└─────────────┴─────────────┴──────────────┘
       │              │              │
       │              ▼              │
       │    ┌─────────────────┐      │
       │    │  POI 详情底部栏  │      │
       │    │  (点击标记显示)  │      │
       │    └─────────────────┘      │
       │              │              │
       │              ▼              │
       │    ┌─────────────────┐      │
       └────│ 添加到当前行程   │◄─────┘
            └─────────────────┘
```

---

## 6. 依赖注入 (Hilt)

### 6.1 Module 设计

```kotlin
// 数据库模块
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = ...

    @Provides @Singleton
    fun providePostDao(database: AppDatabase): PostDao = database.postDao()

    @Provides @Singleton
    fun provideTripDao(database: AppDatabase): TripDao = database.tripDao()
}

// 网络模块
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
}

// 仓储模块
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository
}

// 爬虫模块
@Module @InstallIn(SingletonComponent::class)
abstract class CrawlerModule {
    @Binds @Singleton
    abstract fun bindXiaohongshuCrawler(impl: XiaohongshuCrawler): CrawlerStrategy

    @Binds @Singleton
    abstract fun bindAmapPoiCrawler(impl: AmapPoiCrawler): CrawlerStrategy
}
```

---

## 7. 地图集成

### 7.1 高德地图集成

```kotlin
// MapFragment 中的集成
class MapFragment : Fragment(), LocationSource {

    private var aMap: AMap? = null
    private val markers = mutableListOf<Marker>()

    private fun setupMap() {
        binding.mapView.onCreate(savedInstanceState)
        aMap = binding.mapView.map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isCompassEnabled = true
            setLocationSource(this@MapFragment)
        }
    }

    private fun showPOIs(pois: List<POI>) {
        clearMarkers()
        pois.forEach { poi ->
            val marker = aMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(poi.location.latitude, poi.location.longitude))
                    .title(poi.name)
                    .snippet(poi.description.take(50))
            )
            marker?.tag = poi
            marker?.let { markers.add(it) }
        }
    }
}
```

---

## 8. 文件清单

### 8.1 Kotlin 源文件 (20 个)

```
app/src/main/java/com/travelfinder/
├── TravelFinderApp.kt                    # Application 入口
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── PostDao.kt               # 帖子 DAO
│   │   │   └── TripDao.kt               # 行程 DAO
│   │   ├── db/
│   │   │   └── AppDatabase.kt           # Room 数据库
│   │   └── entity/
│   │       ├── PostEntity.kt           # 帖子实体
│   │       └── TripPlanEntity.kt       # 行程实体
│   ├── remote/
│   │   └── crawler/
│   │       ├── CrawlerStrategy.kt      # 爬虫策略接口
│   │       ├── CrawlerFactory.kt       # 爬虫工厂
│   │       ├── XiaohongshuCrawler.kt   # 小红书爬虫
│   │       └── AmapPoiCrawler.kt       # 高德爬虫
│   └── repository/
│       ├── PostRepositoryImpl.kt       # 帖子仓储实现
│       └── TripRepositoryImpl.kt       # 行程仓储实现
│
├── domain/
│   ├── model/
│   │   ├── Location.kt                 # 地理位置
│   │   ├── Locatable.kt                # 可定位抽象类
│   │   ├── POI.kt                      # 兴趣点
│   │   ├── Post.kt                     # 帖子
│   │   ├── Review.kt                   # 评价
│   │   └── TripPlan.kt                 # 行程规划
│   ├── repository/
│   │   ├── PostRepository.kt           # 帖子仓储接口
│   │   └── TripRepository.kt           # 行程仓储接口
│   └── usecase/
│       ├── SearchPOIsUseCase.kt        # 搜索 POI 用例
│       └── CreateTripPlanUseCase.kt    # 创建行程用例
│
├── presentation/
│   ├── adapter/
│   │   ├── POIAdapter.kt               # POI 列表适配器
│   │   └── TripPlanAdapter.kt         # 行程列表适配器
│   ├── state/
│   │   └── MapUiState.kt               # UI 状态类
│   ├── ui/
│   │   ├── MainActivity.kt            # 主 Activity
│   │   ├── home/
│   │   │   └── HomeFragment.kt        # 首页
│   │   ├── map/
│   │   │   └── MapFragment.kt         # 地图页
│   │   └── planning/
│   │       └── PlanningFragment.kt    # 规划页
│   └── viewmodel/
│       ├── MapViewModel.kt             # 地图 ViewModel
│       └── TripViewModel.kt            # 行程 ViewModel
│
└── di/
    ├── CrawlerModule.kt               # 爬虫 DI 模块
    ├── DatabaseModule.kt              # 数据库 DI 模块
    ├── NetworkModule.kt                # 网络 DI 模块
    └── RepositoryModule.kt            # 仓储 DI 模块
```

### 8.2 资源文件 (15 个)

```
app/src/main/res/
├── layout/
│   ├── activity_main.xml              # 主布局
│   ├── fragment_home.xml              # 首页布局
│   ├── fragment_map.xml               # 地图布局
│   ├── fragment_planning.xml           # 规划布局
│   ├── item_poi.xml                   # POI 列表项
│   └── item_trip_plan.xml             # 行程列表项
├── navigation/
│   └── nav_graph.xml                  # 导航图
├── menu/
│   └── bottom_nav_menu.xml            # 底部导航菜单
├── drawable/
│   ├── circle_background.xml          # 圆形背景
│   └── placeholder_image.xml          # 占位图
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml                # 应用图标
│   └── ic_launcher_round.xml          # 圆形图标
└── values/
    ├── colors.xml                     # 颜色定义
    ├── strings.xml                   # 字符串资源
    └── themes.xml                     # 主题定义
```

### 8.3 配置文件

```
travel_finder/
├── build.gradle.kts                   # 根构建配置
├── settings.gradle.kts                # 项目设置
├── gradle.properties                  # Gradle 属性
└── app/
    ├── build.gradle.kts               # App 模块配置
    └── proguard-rules.pro            # ProGuard 规则
```

---

## 9. 构建说明

### 9.1 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

### 9.2 高德地图 API Key 配置

1. 在 [高德开放平台](https://lbs.amap.com/) 注册账号
2. 创建应用，获取 Web API Key 和 Android SDK Key
3. 在 `AndroidManifest.xml` 中添加：

```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="YOUR_API_KEY" />
```

### 9.3 构建命令

```bash
# 下载 Gradle Wrapper
gradle wrapper --gradle-version 8.2

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### 9.4 APK 输出位置

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 10. 扩展计划

### 10.1 短期扩展

- [ ] 接入真实的小红书 API（需申请）
- [ ] 实现路线优化算法（TSP 问题）
- [ ] 添加行程分享功能
- [ ] 支持 Google Maps（海外版）

### 10.2 长期扩展

- [ ] 接入抖音、美团等更多数据源
- [ ] 添加离线地图功能
- [ ] 实现 AI 智能推荐
- [ ] 支持多语言（国际化）

---

## 11. 注意事项

1. **爬虫合规性**: 小红书等平台对爬虫有严格限制，实际使用需要官方 API 或获得授权
2. **用户隐私**: 位置信息需妥善处理，遵守相关法律法规
3. **API 配额**: 高德地图 API 有每日调用配额限制，需合理使用
