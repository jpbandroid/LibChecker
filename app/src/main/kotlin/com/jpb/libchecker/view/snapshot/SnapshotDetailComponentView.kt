package com.jpb.libchecker.view.snapshot

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.marginStart
import androidx.core.view.marginTop

import com.jpb.libchecker.constant.GlobalValues
import com.jpb.libchecker.constant.librarymap.IconResMap
import com.jpb.libchecker.database.entity.RuleEntity
import com.jpb.libchecker.utils.extensions.getDimensionPixelSize
import com.jpb.libchecker.utils.extensions.toColorStateList
import com.jpb.libchecker.utils.extensions.valueUnsafe
import com.jpb.libchecker.view.AViewGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.jpb.libchecker.R

class SnapshotDetailComponentView(context: Context) : MaterialCardView(context) {

  val container = SnapshotDetailComponentContainerView(context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
  }

  init {
    addView(container)
  }

  class SnapshotDetailComponentContainerView(context: Context) : AViewGroup(context) {

    init {
      clipToPadding = false
      val padding = context.getDimensionPixelSize(R.dimen.main_card_padding)
      setPadding(padding, padding, padding, padding)
    }

    val typeIcon = AppCompatImageView(context).apply {
      layoutParams = LayoutParams(16.dp, 16.dp)
      imageTintList = R.color.material_blue_grey_700.toColorStateList(context)
      addView(this)
    }

    val name = AppCompatTextView(
      ContextThemeWrapper(
        context,
        R.style.TextView_SansSerifMedium
      )
    ).apply {
      layoutParams = LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      ).also {
        it.marginStart = 8.dp
      }
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
      setTextColor(Color.BLACK)
      addView(this)
    }

    private var chip: Chip? = null

    fun setChipOnClickListener(listener: OnClickListener?) {
      chip?.setOnClickListener(listener)
    }

    fun setChip(entity: RuleEntity?, colorRes: Int) {
      if (entity == null) {
        if (chip != null) {
          removeView(chip)
          chip = null
        }
      } else {
        if (chip == null) {
          chip = Chip(ContextThemeWrapper(context, R.style.App_LibChip)).apply {
            layoutParams = LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
              it.topMargin = 4.dp
            }
            setTextColor(Color.BLACK)
            chipStrokeColor = ColorStateList.valueOf((Color.parseColor("#20000000")))
            chipStrokeWidth = 1.dp.toFloat()
            chipStartPadding = 10.dp.toFloat()
            setPadding(paddingStart, 2.dp, paddingEnd, 2.dp)
            addView(this)
          }
        }
        chip!!.apply {
          setChipIconResource(IconResMap.getIconRes(entity.iconIndex))
          text = entity.label
          chipBackgroundColor = colorRes.toColorStateList(context)

          if (!GlobalValues.isColorfulIcon.valueUnsafe && !IconResMap.isSingleColorIcon(
              entity.iconIndex
            )
          ) {
            val icon = chipIcon
            icon?.let {
              it.mutate().colorFilter =
                ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
              chipIcon = it
            }
          } else if (IconResMap.isSingleColorIcon(entity.iconIndex)) {
            chipIcon?.mutate()?.setTint(Color.BLACK)
          } else {
            setChipIconResource(IconResMap.getIconRes(entity.iconIndex))
          }
        }
      }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
      typeIcon.autoMeasure()
      name.measure(
        (measuredWidth - paddingStart - typeIcon.measuredWidth - name.marginStart - paddingEnd).toExactlyMeasureSpec(),
        name.defaultHeightMeasureSpec(this)
      )
      val chipHeight = chip?.let {
        it.autoMeasure()
        it.measuredHeight + it.marginTop
      } ?: 0
      setMeasuredDimension(
        measuredWidth,
        paddingTop + name.measuredHeight.coerceAtLeast(typeIcon.measuredHeight) + chipHeight + paddingBottom
      )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
      typeIcon.layout(paddingStart, typeIcon.toVerticalCenter(this))
      name.layout(typeIcon.right + name.marginStart, paddingTop)
      chip?.layout(name.left, name.bottom + chip!!.marginTop)
    }
  }
}
