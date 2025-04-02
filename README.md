# CustomRatingBar

一個功能齊全的自定義Android評分條組件，解決原生RatingBar不方便調整大小、樣式和間距的問題。

## 特性

- 自定義星星圖標：支持通過XML或程式碼設置自定義的空星、半星和滿星圖標
- 保持原始圖標大小：使用wrap_content時保持圖標原始大小而不變形
- 自定義間距控制：可靈活調整星星之間的間距
- 自適應佈局：支持padding、margin和各種佈局參數
- 可自定義星星數量：預設為5顆星，但可通過XML或程式碼調整
- 支持互動模式和只讀模式
- 提供評分變更事件回調
- 星星大小可調整，不會變形或壓縮比例
- 在XML中可直接預覽樣式
- 自適應寬度變化

## 螢幕截圖

(此處可以放置應用程式的螢幕截圖)

## 使用方法

### 1. 在XML中使用

```xml
<!-- 基本用法 -->
<com.example.customratingbar.CustomRatingBar
    android:id="@+id/ratingBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:starCount="5"
    app:rating="3.5"
    app:isIndicator="false"
    app:starSize="24dp"
    app:starSpacing="4dp" />

<!-- 自定義圖標用法 -->
<com.example.customratingbar.CustomRatingBar
    android:id="@+id/customIconsRatingBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:starCount="5"
    app:rating="3.5"
    app:emptyStarDrawable="@drawable/ic_custom_empty"
    app:filledStarDrawable="@drawable/ic_custom_filled"
    app:halfStarDrawable="@drawable/ic_custom_half" />
```

### 2. 在程式碼中設置

```kotlin
// 獲取評分條引用
val ratingBar = findViewById<CustomRatingBar>(R.id.ratingBar)

// 設置評分值
ratingBar.setRating(4.5f)

// 獲取當前評分
val currentRating = ratingBar.getRating()

// 設置為只讀模式
ratingBar.setIsIndicator(true)

// 設置星星數量
ratingBar.setStarCount(7)

// 設置星星大小 (單位：dp)
ratingBar.setStarSize(32)

// 設置星星間距 (單位：dp)
ratingBar.setStarSpacing(8)

// 設置自定義圖標
val emptyIcon = ContextCompat.getDrawable(context, R.drawable.ic_custom_empty)
val filledIcon = ContextCompat.getDrawable(context, R.drawable.ic_custom_filled)
val halfIcon = ContextCompat.getDrawable(context, R.drawable.ic_custom_half)
ratingBar.setStarDrawables(emptyIcon, filledIcon, halfIcon)
```

### 3. 自定義圖標保持原始大小

當使用自定義圖標時，如果希望保持圖標的原始大小而不進行縮放，請確保：
1. 使用 `android:layout_width="wrap_content"` 和 `android:layout_height="wrap_content"`
2. 不要設置 `app:starSize` 屬性，或者將其設置為0

```xml
<!-- 自定義圖標保持原始大小 -->
<com.example.customratingbar.CustomRatingBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:emptyStarDrawable="@drawable/ic_custom_empty"
    app:filledStarDrawable="@drawable/ic_custom_filled"
    app:halfStarDrawable="@drawable/ic_custom_half" />
```

### 4. 監聽評分變化

```kotlin
// 實現評分變更監聽器接口
class YourActivity : AppCompatActivity(), CustomRatingBar.OnRatingChangeListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_layout)
        
        // 設置監聽器
        findViewById<CustomRatingBar>(R.id.ratingBar).setOnRatingChangeListener(this)
    }
    
    // 當評分變更時調用
    override fun onRatingChanged(ratingBar: CustomRatingBar, rating: Float) {
        // 處理評分變更事件
        Toast.makeText(this, "新評分: $rating", Toast.LENGTH_SHORT).show()
    }
}
```

## 自定義屬性

| 屬性 | 描述 | 預設值 |
|------|------|-------|
| starCount | 星星數量 | 5 |
| rating | 當前評分值 | 0.0 |
| isIndicator | 是否為只讀模式 | false |
| starSpacing | 星星間距 (dp) | 4dp |
| starSize | 星星大小 (dp) | 24dp |
| emptyStarDrawable | 空星圖標資源 | 內建圖標 |
| filledStarDrawable | 滿星圖標資源 | 內建圖標 |
| halfStarDrawable | 半星圖標資源 | 內建圖標 |

## 常見使用案例

### 案例1：自定義三種圖標並保持原始大小
最常見的使用場景是自定義三種圖標（空星、滿星、半星），並希望保持圖標的原始大小不被縮放：

```xml
<com.example.customratingbar.CustomRatingBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:starCount="5"
    app:rating="3.5"
    app:starSpacing="8dp"
    app:emptyStarDrawable="@drawable/ic_custom_empty"
    app:filledStarDrawable="@drawable/ic_custom_filled"
    app:halfStarDrawable="@drawable/ic_custom_half" />
```

### 案例2：調整星星間距
如果需要自定義星星之間的間距：

```xml
<com.example.customratingbar.CustomRatingBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:starCount="5"
    app:rating="4"
    app:starSpacing="16dp" />
```

### 案例3：只讀模式（沒有互動）
當只需顯示評分而不允許用戶更改時：

```xml
<com.example.customratingbar.CustomRatingBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:rating="4.5"
    app:isIndicator="true" />
```

## 導入到你的專案

### 方法：直接複製檔案

複製以下檔案到你的專案中：
   - `CustomRatingBar.kt`
   - 星星圖標資源：`ic_star_empty.xml`, `ic_star_filled.xml`, `ic_star_half.xml`
   - 添加相關顏色定義到你的 `colors.xml`：
     ```xml
     <color name="star_active">#FFFFCC00</color>
     <color name="star_inactive">#FFD3D3D3</color>
     ```
   - 添加自定義屬性到 `attrs.xml`：
     ```xml
     <declare-styleable name="CustomRatingBar">
         <attr name="starCount" format="integer" />
         <attr name="rating" format="float" />
         <attr name="isIndicator" format="boolean" />
         <attr name="starSpacing" format="dimension" />
         <attr name="starSize" format="dimension" />
         <attr name="emptyStarDrawable" format="reference" />
         <attr name="filledStarDrawable" format="reference" />
         <attr name="halfStarDrawable" format="reference" />
     </declare-styleable>
     ```