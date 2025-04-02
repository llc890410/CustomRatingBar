package com.example.customratingbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

/**
 * 自定義評分條組件 (Custom Rating Bar)
 * 
 * 一個功能完整的星級評分控件，解決原生 RatingBar 不方便調整大小、樣式和間距的問題。
 * 可以顯示滿星、半星和空星，支持使用者互動或純展示模式。
 * 
 * 主要功能與特點：
 * - 自定義星星圖標：支持通過 XML 或代碼設置自定義的空星、半星和滿星圖標
 * - 保持原始圖標大小：使用 wrap_content 時保持傳入圖標的原始大小而不縮放
 * - 自定義間距控制：可靈活調整星星之間的間距
 * - 自適應佈局：支持 padding、margin 和各種佈局參數
 * - 可調整星星數量：預設五顆星，但可自由設置任意數量
 * - 互動/只讀模式：可設置為使用者可互動或純展示模式
 * - 提供評分變更回調：方便監聽評分變化
 * - 狀態保存：在螢幕旋轉等配置變更時保持評分狀態
 * - 自適應寬度：當使用固定寬度時自動調整星星大小
 *
 * XML 屬性說明：
 * - starCount：星星數量，預設為 5
 * - rating：當前評分值，範圍從 0 到 starCount
 * - isIndicator：是否為只讀模式，為 true 時使用者無法點擊修改評分
 * - starSpacing：星星之間的間距（單位：dp 或其他尺寸單位）
 * - starSize：星星的大小（單位：dp 或其他尺寸單位），設為 0 則使用圖標原始大小
 * - emptyStarDrawable：自定義空星圖標資源引用
 * - filledStarDrawable：自定義滿星圖標資源引用
 * - halfStarDrawable：自定義半星圖標資源引用
 */
class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * 常量和預設值
     */
    companion object {
        private const val DEFAULT_STAR_COUNT = 5           // 預設星星數量
        private const val DEFAULT_RATING = 0.0f            // 預設評分
        private const val DEFAULT_IS_INDICATOR = false     // 預設是否為只讀模式
        private const val DEFAULT_STAR_SPACING_DP = 4      // 預設星星間距（dp）
        private const val DEFAULT_STAR_SIZE_DP = 24        // 預設星星大小（dp）
    }

    // 基本屬性
    private var starCount: Int = DEFAULT_STAR_COUNT
    private var rating: Float = DEFAULT_RATING
    private var isIndicator: Boolean = DEFAULT_IS_INDICATOR
    private var starSpacing: Int = dpToPx(DEFAULT_STAR_SPACING_DP)
    private var starSize: Int = dpToPx(DEFAULT_STAR_SIZE_DP)

    // 評分變更監聽器
    private var onRatingChangeListener: OnRatingChangeListener? = null
    // 儲存所有星星視圖的列表
    private var starViews = mutableListOf<ImageView>()
    // 預先分配佈局參數列表，避免在onMeasure中創建新物件
    private var starLayoutParams = mutableListOf<LayoutParams>()

    // 星星圖標資源（懶加載）
    private val starEmptyDrawable: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_star_empty)
    }

    private val starFilledDrawable: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_star_filled)
    }

    private val starHalfDrawable: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_star_half)
    }

    // 自定義圖標
    private var customEmptyDrawable: Drawable? = null
    private var customFilledDrawable: Drawable? = null
    private var customHalfDrawable: Drawable? = null

    /**
     * 初始化
     */
    init {
        initAttributes(attrs)          // 初始化自定義屬性
        orientation = HORIZONTAL       // 設置水平方向佈局
        gravity = Gravity.CENTER_VERTICAL  // 垂直居中
        clipToPadding = false          // 允許子視圖繪製在 padding 區域
        setupStars()                   // 設置星星視圖
    }

    /**
     * 初始化自定義屬性
     * 
     * @param attrs XML 屬性集
     */
    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar)
            try {
                // 從 XML 屬性中讀取配置值
                starCount = typedArray.getInteger(R.styleable.CustomRatingBar_starCount, DEFAULT_STAR_COUNT)
                rating = typedArray.getFloat(R.styleable.CustomRatingBar_rating, DEFAULT_RATING)
                isIndicator = typedArray.getBoolean(R.styleable.CustomRatingBar_isIndicator, DEFAULT_IS_INDICATOR)
                
                // 讀取自定義圖標
                val emptyDrawableResId = typedArray.getResourceId(R.styleable.CustomRatingBar_emptyStarDrawable, 0)
                val filledDrawableResId = typedArray.getResourceId(R.styleable.CustomRatingBar_filledStarDrawable, 0)
                val halfDrawableResId = typedArray.getResourceId(R.styleable.CustomRatingBar_halfStarDrawable, 0)
                
                // 檢查是否有從XML設置starSize的值
                val hasCustomStarSize = typedArray.hasValue(R.styleable.CustomRatingBar_starSize)
                
                // 如果設置了自定義starSize，則使用該值
                if (hasCustomStarSize) {
                    starSize = typedArray.getDimensionPixelSize(R.styleable.CustomRatingBar_starSize, starSize)
                }
                
                // 讀取間距值
                starSpacing = typedArray.getDimensionPixelSize(R.styleable.CustomRatingBar_starSpacing, starSpacing)
                
                // 設置自定義圖標
                if (emptyDrawableResId != 0) {
                    customEmptyDrawable = ContextCompat.getDrawable(context, emptyDrawableResId)
                }
                if (filledDrawableResId != 0) {
                    customFilledDrawable = ContextCompat.getDrawable(context, filledDrawableResId)
                }
                if (halfDrawableResId != 0) {
                    customHalfDrawable = ContextCompat.getDrawable(context, halfDrawableResId)
                }
                
                // 如果設置了自定義圖標但沒有設置自定義starSize，則從圖標獲取大小
                if (!hasCustomStarSize && (customEmptyDrawable != null || customFilledDrawable != null || customHalfDrawable != null)) {
                    // 使用第一個可用的drawable來設置大小
                    when {
                        customEmptyDrawable != null -> updateStarSizeFromDrawable(customEmptyDrawable!!)
                        customFilledDrawable != null -> updateStarSizeFromDrawable(customFilledDrawable!!)
                        customHalfDrawable != null -> updateStarSizeFromDrawable(customHalfDrawable!!)
                    }
                }
            } finally {
                typedArray.recycle()   // 釋放 TypedArray 資源
            }
        }
    }

    /**
     * 設置星星視圖
     * 移除所有現有星星並重新建立
     */
    private fun setupStars() {
        removeAllViews()       // 清除所有現有視圖
        starViews.clear()      // 清空列表
        starLayoutParams.clear() // 清空佈局參數列表

        // 建立每顆星星
        for (i in 0 until starCount) {
            // 預先創建佈局參數
            val params = LayoutParams(starSize, starSize).apply {
                if (i < starCount - 1) {
                    marginEnd = starSpacing  // 設置除了最後一顆外的星星右邊距
                }
            }
            starLayoutParams.add(params) // 儲存佈局參數以便後續重用
            
            val imageView = ImageView(context).apply {
                id = View.generateViewId()  // 生成唯一ID
                layoutParams = params // 設置佈局參數
                scaleType = ImageView.ScaleType.FIT_CENTER  // 縮放類型：適應中心
                // 如果不是只讀模式，設置點擊事件
                if (!isIndicator) {
                    setOnClickListener { onStarClicked(i + 1) }
                }
            }
            starViews.add(imageView)    // 添加到列表
            addView(imageView)          // 添加到佈局
        }

        updateStars()  // 更新星星狀態
    }

    /**
     * 更新所有星星的顯示狀態
     * 根據當前評分值設置每顆星星是滿星、半星還是空星
     */
    private fun updateStars() {
        val fullStars = rating.toInt()  // 滿星數量
        val hasHalfStar = rating - fullStars >= 0.5f  // 是否有半星

        // 為每顆星星設置相應的圖標
        for (i in 0 until starCount) {
            val starView = starViews[i]
            when {
                i < fullStars -> {
                    val drawable = customFilledDrawable ?: starFilledDrawable
                    starView.setImageDrawable(drawable)  // 滿星
                }
                i == fullStars && hasHalfStar -> {
                    val drawable = customHalfDrawable ?: starHalfDrawable
                    starView.setImageDrawable(drawable)  // 半星
                }
                else -> {
                    val drawable = customEmptyDrawable ?: starEmptyDrawable
                    starView.setImageDrawable(drawable)  // 空星
                }
            }
        }
    }

    /**
     * 處理星星點擊事件
     * 
     * @param position 點擊的星星位置（1-based）
     */
    private fun onStarClicked(position: Int) {
        if (!isIndicator) {  // 只有在互動模式下才處理點擊
            setRating(position.toFloat())  // 設置評分
            onRatingChangeListener?.onRatingChanged(this, rating)  // 通知監聽器
        }
    }

    /**
     * 設置評分值
     * 
     * @param rating 評分值，範圍從 0 到 starCount
     */
    fun setRating(rating: Float) {
        val newRating = rating.coerceIn(0f, starCount.toFloat())  // 限制評分範圍
        if (this.rating != newRating) {  // 只在評分變化時更新
            this.rating = newRating
            updateStars()  // 更新星星狀態
        }
    }

    /**
     * 獲取當前評分值
     * 
     * @return 當前評分值
     */
    fun getRating(): Float = rating

    /**
     * 設置星星數量
     * 
     * @param count 星星數量，必須大於 0
     * @throws IllegalArgumentException 如果 count 小於或等於 0
     */
    fun setStarCount(count: Int) {
        if (count <= 0) {
            throw IllegalArgumentException("星星數量必須大於 0，目前值: $count")
        }
        
        if (starCount != count) {  // 只在數量變化時更新
            starCount = count
            rating = rating.coerceAtMost(count.toFloat())  // 確保評分不超過星星數量
            setupStars()  // 重新設置星星
        }
    }

    /**
     * 獲取星星數量
     * 
     * @return 星星數量
     */
    fun getStarCount(): Int = starCount

    /**
     * 設置是否為只讀模式
     * 
     * @param indicator true 表示只讀模式，false 表示允許互動
     */
    fun setIsIndicator(indicator: Boolean) {
        if (isIndicator != indicator) {  // 只在模式變化時更新
            isIndicator = indicator
            setupStars()  // 重新設置星星以更新點擊事件
        }
    }

    /**
     * 判斷是否為只讀模式
     * 
     * @return 是否為只讀模式
     */
    fun isIndicator(): Boolean = isIndicator

    /**
     * 設置星星間距
     * 
     * @param spacingDp 間距大小，單位 dp
     */
    fun setStarSpacing(spacingDp: Int) {
        val spacingPx = dpToPx(spacingDp)
        if (starSpacing != spacingPx) {  // 只在間距變化時更新
            starSpacing = spacingPx
            setupStars()  // 重新設置星星
        }
    }

    /**
     * 設置星星大小
     * 
     * @param sizeDp 星星大小，單位 dp
     */
    fun setStarSize(sizeDp: Int) {
        val sizePx = dpToPx(sizeDp)
        if (starSize != sizePx) {  // 只在大小變化時更新
            starSize = sizePx
            setupStars()  // 重新設置星星
        }
    }

    /**
     * 設置評分變更監聽器
     * 
     * @param listener 評分變更監聽器
     */
    fun setOnRatingChangeListener(listener: OnRatingChangeListener) {
        onRatingChangeListener = listener
    }

    /**
     * 將 dp 轉換為 px
     * 
     * @param dp 密度無關像素值
     * @return 轉換後的像素值
     */
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    /**
     * 評分變更監聽器接口
     * 用於通知外部評分變化事件
     */
    interface OnRatingChangeListener {
        /**
         * 當評分變更時調用
         * 
         * @param ratingBar 評分條實例
         * @param rating 新的評分值
         */
        fun onRatingChanged(ratingBar: CustomRatingBar, rating: Float)
    }

    /**
     * 測量視圖尺寸
     * 重寫此方法以實現自適應寬度
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // 獲取寬度和測量模式
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        
        // 只在EXACTLY模式下調整星星大小，AT_MOST（wrap_content）模式保持原始大小
        if (mode == MeasureSpec.EXACTLY && width > 0 && starCount > 0) {
            // 確保不會發生除零錯誤
            if (starCount <= 0) return
            
            // 計算總填充和間距
            val totalPadding = paddingLeft + paddingRight
            val totalSpacing = (starCount - 1) * starSpacing
            
            // 確保總填充和間距不會超過可用寬度
            if (totalPadding + totalSpacing >= width) {
                // 如果填充和間距已經超過可用寬度，設置最小星星大小
                val minStarSize = 1
                for (i in 0 until starCount) {
                    val layoutParams = starLayoutParams[i]
                    layoutParams.width = minStarSize
                    layoutParams.height = minStarSize
                    starViews[i].layoutParams = layoutParams
                }
                return
            }
            
            // 計算可用寬度
            val availableWidth = width - totalPadding - totalSpacing
            
            // 計算每顆星星的新大小，確保至少為1像素
            val newStarSize = (availableWidth / starCount).coerceAtLeast(1)
            
            // 如果星星大小需要調整
            if (newStarSize != starSize) {
                // 更新每顆星星的大小，重用已有的LayoutParams物件
                for (i in 0 until starCount) {
                    // 獲取LayoutParams並更新其屬性
                    val layoutParams = starLayoutParams[i]
                    layoutParams.width = newStarSize
                    layoutParams.height = newStarSize
                    // 間距保持不變
                    starViews[i].layoutParams = layoutParams
                }
            }
        } else if (mode == MeasureSpec.AT_MOST) {
            // 在AT_MOST模式下（wrap_content），計算整個視圖所需的總寬度
            val totalWidth = paddingLeft + paddingRight + (starCount * starSize) + ((starCount - 1) * starSpacing)
            
            // 確保不超過可用的最大寬度
            val finalWidth = totalWidth.coerceAtMost(width)
            
            // 設置測量尺寸
            setMeasuredDimension(finalWidth, measuredHeight)
        }
    }

    /**
     * 設置空星圖標
     * 
     * @param drawable 空星圖標
     */
    fun setEmptyStarDrawable(drawable: Drawable?) {
        customEmptyDrawable = drawable
        // 如果設置了新的drawable且未手動設置starSize，則根據drawable大小更新starSize
        drawable?.let {
            if (customEmptyDrawable != null && customFilledDrawable == null && customHalfDrawable == null) {
                updateStarSizeFromDrawable(it)
            }
        }
        updateStars()
    }

    /**
     * 設置滿星圖標
     * 
     * @param drawable 滿星圖標
     */
    fun setFilledStarDrawable(drawable: Drawable?) {
        customFilledDrawable = drawable
        // 如果設置了新的drawable且未手動設置starSize，則根據drawable大小更新starSize
        drawable?.let {
            if (customEmptyDrawable == null && customFilledDrawable != null && customHalfDrawable == null) {
                updateStarSizeFromDrawable(it)
            }
        }
        updateStars()
    }

    /**
     * 設置半星圖標
     * 
     * @param drawable 半星圖標
     */
    fun setHalfStarDrawable(drawable: Drawable?) {
        customHalfDrawable = drawable
        // 如果設置了新的drawable且未手動設置starSize，則根據drawable大小更新starSize
        drawable?.let {
            if (customEmptyDrawable == null && customFilledDrawable == null && customHalfDrawable != null) {
                updateStarSizeFromDrawable(it)
            }
        }
        updateStars()
    }

    /**
     * 設置所有星星圖標
     * 
     * @param emptyDrawable 空星圖標
     * @param filledDrawable 滿星圖標
     * @param halfDrawable 半星圖標
     */
    fun setStarDrawables(emptyDrawable: Drawable?, filledDrawable: Drawable?, halfDrawable: Drawable?) {
        customEmptyDrawable = emptyDrawable
        customFilledDrawable = filledDrawable
        customHalfDrawable = halfDrawable
        
        // 使用第一個非空的drawable來更新starSize
        when {
            emptyDrawable != null -> updateStarSizeFromDrawable(emptyDrawable)
            filledDrawable != null -> updateStarSizeFromDrawable(filledDrawable)
            halfDrawable != null -> updateStarSizeFromDrawable(halfDrawable)
        }
        
        updateStars()
    }
    
    /**
     * 從drawable獲取大小並更新starSize
     */
    private fun updateStarSizeFromDrawable(drawable: Drawable) {
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        
        // 使用drawable的實際大小作為starSize（取寬高的最大值）
        if (drawableWidth > 0 && drawableHeight > 0) {
            val newSize = maxOf(drawableWidth, drawableHeight)
            if (starSize != newSize) {
                starSize = newSize
                setupStars()
            }
        }
    }

    /**
     * 保存視圖狀態
     */
    override fun onSaveInstanceState(): Parcelable {
        // 保存基類的狀態
        val superState = super.onSaveInstanceState() ?: Bundle()
        
        // 創建保存狀態物件
        val savedState = SavedState(superState)
        
        // 保存當前評分值
        savedState.rating = rating
        savedState.starCount = starCount
        savedState.isIndicator = isIndicator
        savedState.starSpacing = starSpacing
        savedState.starSize = starSize
        
        return savedState
    }
    
    /**
     * 恢復視圖狀態
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        // 如果狀態是SavedState類型，恢復保存的值
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            rating = state.rating
            starCount = state.starCount
            isIndicator = state.isIndicator
            starSpacing = state.starSpacing
            starSize = state.starSize
            
            // 重新設置星星並更新顯示
            setupStars()
        } else {
            // 如果不是SavedState類型，由基類處理
            super.onRestoreInstanceState(state)
        }
    }
    
    /**
     * 自定義保存狀態類
     */
    internal class SavedState : BaseSavedState {
        // 保存的評分值
        var rating: Float = 0f
        var starCount: Int = 0
        var isIndicator: Boolean = false
        var starSpacing: Int = 0
        var starSize: Int = 0
        
        constructor(superState: Parcelable) : super(superState)
        
        constructor(parcel: Parcel) : super(parcel) {
            rating = parcel.readFloat()
            starCount = parcel.readInt()
            isIndicator = parcel.readInt() == 1
            starSpacing = parcel.readInt()
            starSize = parcel.readInt()
        }
        
        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(rating)
            out.writeInt(starCount)
            out.writeInt(if (isIndicator) 1 else 0)
            out.writeInt(starSpacing)
            out.writeInt(starSize)
        }
        
        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }
                
                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    /**
     * 當組件從父視圖中分離時被調用
     * 釋放資源以避免內存洩漏
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清除回調引用以避免內存洩漏
        onRatingChangeListener = null
    }
} 