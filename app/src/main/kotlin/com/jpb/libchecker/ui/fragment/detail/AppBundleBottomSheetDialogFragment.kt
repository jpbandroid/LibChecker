package com.jpb.libchecker.ui.fragment.detail

import com.jpb.libchecker.base.BaseBottomSheetViewDialogFragment
import com.jpb.libchecker.bean.AppBundleItemBean
import com.jpb.libchecker.ui.detail.EXTRA_PACKAGE_NAME
import com.jpb.libchecker.utils.PackageUtils
import com.jpb.libchecker.view.app.BottomSheetHeaderView
import com.jpb.libchecker.view.detail.AppBundleBottomSheetView
import com.jpb.libchecker.view.detail.AppBundleItemView
import java.io.File
import java.util.Locale

class AppBundleBottomSheetDialogFragment :
  BaseBottomSheetViewDialogFragment<AppBundleBottomSheetView>() {

  private val packageName by lazy { arguments?.getString(EXTRA_PACKAGE_NAME) }

  override fun initRootView(): AppBundleBottomSheetView =
    AppBundleBottomSheetView(requireContext())

  override fun getHeaderView(): BottomSheetHeaderView = root.getHeaderView()

  override fun init() {
    packageName?.let {
      val packageInfo = PackageUtils.getPackageInfo(it)
      val list = packageInfo.applicationInfo.splitSourceDirs
      val localeList by lazy { Locale.getISOLanguages() }
      val bundleList = if (list.isNullOrEmpty()) {
        emptyList()
      } else {
        list.map { split ->
          val name = split.substringAfterLast("/")
          val middleName = name.removePrefix("split_config.").removeSuffix(".apk")
          val type = when {
            middleName.startsWith("arm") || middleName.startsWith("x86") -> AppBundleItemView.IconType.TYPE_NATIVE_LIBS
            middleName.endsWith("dpi") -> AppBundleItemView.IconType.TYPE_MATERIALS
            localeList.contains(middleName) -> AppBundleItemView.IconType.TYPE_STRINGS
            else -> AppBundleItemView.IconType.TYPE_OTHERS
          }
          AppBundleItemBean(name = name, size = File(split).length(), type = type)
        }
      }
      root.adapter.setList(bundleList)
    }
  }
}
