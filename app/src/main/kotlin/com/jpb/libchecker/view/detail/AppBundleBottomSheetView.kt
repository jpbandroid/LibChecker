package com.jpb.libchecker.view.detail

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jpb.libchecker.R

import com.jpb.libchecker.recyclerview.VerticalSpacesItemDecoration
import com.jpb.libchecker.recyclerview.adapter.detail.AppBundleAdapter
import com.jpb.libchecker.utils.extensions.dp
import com.jpb.libchecker.utils.extensions.unsafeLazy
import com.jpb.libchecker.view.app.BottomSheetHeaderView
import com.jpb.libchecker.view.app.IHeaderView

class AppBundleBottomSheetView(context: Context) : LinearLayout(context), IHeaderView {

  val adapter by unsafeLazy { AppBundleAdapter() }

  private val header = BottomSheetHeaderView(context).apply {
    layoutParams =
      LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    title.text = context.getString(R.string.app_bundle)
  }

  private val list = RecyclerView(context).apply {
    layoutParams = LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    ).also {
      it.topMargin = 24.dp
    }
    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    adapter = this@AppBundleBottomSheetView.adapter
    layoutManager = LinearLayoutManager(context)
    isVerticalScrollBarEnabled = false
    clipToPadding = false
    clipChildren = false
    isNestedScrollingEnabled = false
    setHasFixedSize(true)
    addItemDecoration(VerticalSpacesItemDecoration(4.dp))
  }

  init {
    orientation = VERTICAL
    setPadding(24.dp, 16.dp, 24.dp, 0)
    addView(header)
    addView(list)
  }

  override fun getHeaderView(): BottomSheetHeaderView {
    return header
  }
}
