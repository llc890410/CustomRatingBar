package com.example.customratingbar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customratingbar.databinding.ActivityMainBinding

/**
 * 主界面活動
 * 
 * 此活動展示如何使用CustomRatingBar组件的各種功能和配置
 * 包括不同大小、間距的星星評分條，以及自定義圖標、互動和只讀模式等
 */
class MainActivity : AppCompatActivity(), CustomRatingBar.OnRatingChangeListener {
    
    // 使用ViewBinding訪問佈局元素
    private lateinit var binding: ActivityMainBinding
    
    // 追蹤當前間距，用於調整按鈕
    private var currentSpacing = 8
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 設置系統UI相關的內邊距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupRatingBars()
        setupClickListeners()
        setupCustomIconsExample()
    }
    
    /**
     * 設置評分條相關配置
     */
    private fun setupRatingBars() {
        // 設置第一個評分條的點擊監聽器
        binding.ratingBar1.setOnRatingChangeListener(this)
        
        // 設置星星間距示例
        binding.spacingRatingBar.setStarSpacing(20) // 20dp間距
        
        // 更新當前選擇的評分顯示
        updateSelectedRatingText(binding.ratingBar1.getRating())
    }
    
    /**
     * 設置點擊事件監聽器
     */
    private fun setupClickListeners() {
        // 重置按鈕點擊事件
        binding.resetButton.setOnClickListener {
            binding.ratingBar1.setRating(0f)
            updateSelectedRatingText(0f)
            Toast.makeText(this, "評分已重置", Toast.LENGTH_SHORT).show()
        }
        
        // 調整間距按鈕
        binding.increaseSpacingButton.setOnClickListener {
            currentSpacing += 4
            binding.customIconsRatingBar.setStarSpacing(currentSpacing)
            Toast.makeText(this, "星星間距已增加到 ${currentSpacing}dp", Toast.LENGTH_SHORT).show()
        }
        
        binding.decreaseSpacingButton.setOnClickListener {
            currentSpacing = (currentSpacing - 4).coerceAtLeast(0)
            binding.customIconsRatingBar.setStarSpacing(currentSpacing)
            Toast.makeText(this, "星星間距已減少到 ${currentSpacing}dp", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 設置自定義圖標評分條範例
     */
    private fun setupCustomIconsExample() {
        // 初始設置間距
        binding.customIconsRatingBar.setStarSpacing(currentSpacing)
        
        // 設置評分變更監聽器
        binding.customIconsRatingBar.setOnRatingChangeListener(object : CustomRatingBar.OnRatingChangeListener {
            override fun onRatingChanged(ratingBar: CustomRatingBar, rating: Float) {
                Toast.makeText(
                    this@MainActivity, 
                    "自定義圖標評分條: $rating", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    /**
     * 更新評分文本顯示
     * 
     * @param rating 當前評分值
     */
    private fun updateSelectedRatingText(rating: Float) {
        binding.selectedRatingText.text = "選擇的評分: $rating"
    }
    
    /**
     * 評分變更監聽器回調方法
     * 
     * @param ratingBar 發生變更的評分條
     * @param rating 新的評分值
     */
    override fun onRatingChanged(ratingBar: CustomRatingBar, rating: Float) {
        // 當評分條的評分變更時更新文本
        if (ratingBar.id == R.id.ratingBar1) {
            updateSelectedRatingText(rating)
        }
    }
}