package com.jpb.libchecker.view.statistics

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

import com.jpb.libchecker.annotation.AUTUMN
import com.jpb.libchecker.annotation.SPRING
import com.jpb.libchecker.annotation.SUMMER
import com.jpb.libchecker.annotation.WINTER
import com.jpb.libchecker.constant.GlobalValues
import com.jpb.libchecker.utils.extensions.getDimensionPixelSize
import com.jpb.libchecker.view.AViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.jpb.libchecker.R

class LibReferenceLoadingView(context: Context, attributeSet: AttributeSet? = null) :
  AViewGroup(context, attributeSet) {

  val loadingView = LottieAnimationView(context).apply {
    val size = context.getDimensionPixelSize(R.dimen.lottie_anim_size)
    layoutParams = FrameLayout.LayoutParams(size, size).also {
      it.gravity = Gravity.CENTER
    }
    imageAssetsFolder = "/"
    repeatCount = LottieDrawable.INFINITE
    val assetName = when (GlobalValues.season) {
      SPRING -> "anim/lib_reference_spring.json"
      SUMMER -> "anim/lib_reference_summer.json"
      AUTUMN -> "anim/lib_reference_autumn.json"
      WINTER -> "anim/lib_reference_winter.json"
      else -> throw IllegalArgumentException("Are you living on earth?")
    }

    enableMergePathsForKitKatAndAbove(true)
    setAnimation(assetName)
    addView(this)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    loadingView.autoMeasure()
    setMeasuredDimension(
      measuredWidth,
      measuredHeight
    )
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    loadingView.layout(loadingView.toHorizontalCenter(this), loadingView.toVerticalCenter(this))
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    loadingView.playAnimation()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    loadingView.pauseAnimation()
  }
}
