package com.jpb.libchecker.view.snapshot

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jpb.libchecker.R

import com.jpb.libchecker.recyclerview.VerticalSpacesItemDecoration
import com.jpb.libchecker.recyclerview.adapter.snapshot.TimeNodeAdapter
import com.jpb.libchecker.utils.extensions.dp
import com.jpb.libchecker.utils.extensions.unsafeLazy
import com.jpb.libchecker.view.app.BottomSheetHeaderView
import com.jpb.libchecker.view.app.IHeaderView

class TimeNodeBottomSheetView(context: Context) : LinearLayout(context), IHeaderView {

  val adapter by unsafeLazy { TimeNodeAdapter() }

  private val header = BottomSheetHeaderView(context).apply {
    layoutParams =
      LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    title.text = context.getString(R.string.dialog_title_change_timestamp)
  }

  private val list = RecyclerView(context).apply {
    layoutParams = LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    ).also {
      it.topMargin = 24.dp
    }
    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    isVerticalScrollBarEnabled = false
    adapter = this@TimeNodeBottomSheetView.adapter
    layoutManager = LinearLayoutManager(context)
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
