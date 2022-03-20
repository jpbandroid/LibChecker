package com.jpb.libchecker.recyclerview.diff

import androidx.recyclerview.widget.DiffUtil
import com.jpb.libchecker.bean.LibReference

class RefListDiffUtil : DiffUtil.ItemCallback<LibReference>() {

  override fun areItemsTheSame(oldItem: LibReference, newItem: LibReference): Boolean {
    return oldItem.hashCode() == newItem.hashCode()
  }

  override fun areContentsTheSame(oldItem: LibReference, newItem: LibReference): Boolean {
    return oldItem.libName == newItem.libName &&
      oldItem.referredList.size == newItem.referredList.size &&
      oldItem.chip == newItem.chip &&
      oldItem.type == newItem.type
  }
}
