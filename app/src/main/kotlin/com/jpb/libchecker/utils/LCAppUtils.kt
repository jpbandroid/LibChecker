package com.jpb.libchecker.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity

import com.jpb.libchecker.SystemServices
import com.jpb.libchecker.annotation.AUTUMN
import com.jpb.libchecker.annotation.LibType
import com.jpb.libchecker.annotation.NATIVE
import com.jpb.libchecker.annotation.SPRING
import com.jpb.libchecker.annotation.SUMMER
import com.jpb.libchecker.annotation.WINTER
import com.jpb.libchecker.bean.DetailExtraBean
import com.jpb.libchecker.bean.LibStringItem
import com.jpb.libchecker.constant.Constants
import com.jpb.libchecker.constant.Constants.OVERLAY
import com.jpb.libchecker.database.AppItemRepository
import com.jpb.libchecker.database.Repositories
import com.jpb.libchecker.database.entity.LCItem
import com.jpb.libchecker.database.entity.RuleEntity
import com.jpb.libchecker.ui.detail.AppDetailActivity
import com.jpb.libchecker.ui.detail.EXTRA_DETAIL_BEAN
import com.jpb.libchecker.ui.detail.EXTRA_PACKAGE_NAME
import com.jpb.libchecker.ui.fragment.detail.EXTRA_LC_ITEM
import com.jpb.libchecker.ui.fragment.detail.OverlayDetailBottomSheetDialogFragment
import com.jpb.libchecker.ui.main.EXTRA_REF_NAME
import com.jpb.libchecker.ui.main.EXTRA_REF_TYPE
import com.jpb.libchecker.utils.extensions.dp
import com.jpb.libchecker.utils.extensions.isTempApk
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.jpb.libchecker.R
import rikka.material.app.DayNightDelegate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object LCAppUtils {

  fun getCurrentSeason(): Int {
    return when (Calendar.getInstance(Locale.getDefault()).get(Calendar.MONTH) + 1) {
      3, 4, 5 -> SPRING
      6, 7, 8 -> SUMMER
      9, 10, 11 -> AUTUMN
      12, 1, 2 -> WINTER
      else -> -1
    }
  }

  fun setTitle(context: Context): String {
    val sb = StringBuilder(context.getString(R.string.app_name))
    val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    when {
      date.endsWith("1225") -> {
        sb.append("\uD83C\uDF84")
      }
      date == "20220131" -> {
        sb.append("\uD83C\uDFEE")
      }
      date == "20220201" -> {
        sb.append("\uD83D\uDC2F")
      }
    }
    return sb.toString()
  }

  fun getAppIcon(packageName: String): Drawable {
    return try {
      val pi = SystemServices.packageManager.getPackageInfo(packageName, 0)
      pi?.applicationInfo?.loadIcon(SystemServices.packageManager)!!
    } catch (e: Exception) {
      ColorDrawable(Color.TRANSPARENT)
    }
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
  fun atLeastS(): Boolean {
    return Build.VERSION.SDK_INT >= 31
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
  fun atLeastR(): Boolean {
    return Build.VERSION.SDK_INT >= 30
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
  fun atLeastQ(): Boolean {
    return Build.VERSION.SDK_INT >= 29
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
  fun atLeastP(): Boolean {
    return Build.VERSION.SDK_INT >= 28
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
  fun atLeastO(): Boolean {
    return Build.VERSION.SDK_INT >= 26
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N_MR1)
  fun atLeastNMR1(): Boolean {
    return Build.VERSION.SDK_INT >= 25
  }

  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
  fun atLeastN(): Boolean {
    return Build.VERSION.SDK_INT >= 24
  }

  fun findRuleRegex(string: String, @LibType type: Int): RuleEntity? {
    val iterator = AppItemRepository.rulesRegexList.entries.iterator()
    while (iterator.hasNext()) {
      val entry = iterator.next()
      if (entry.key.matcher(string).matches() && entry.value.type == type) {
        return entry.value
      }
    }
    return null
  }

  private suspend fun getRuleWithDexChecking(
    name: String,
    packageName: String? = null,
    nativeLibs: List<LibStringItem>? = null
  ): RuleEntity? {
    val ruleEntity = Repositories.ruleRepository.getRule(name) ?: return null
    if (ruleEntity.type == NATIVE) {
      if (packageName == null) {
        return ruleEntity
      }
      val isApk = packageName.isTempApk()
      when (ruleEntity.name) {
        "libjiagu.so", "libjiagu_a64.so", "libjiagu_x86.so", "libjiagu_x64.so" -> {
          return if (PackageUtils.hasDexClass(packageName, "com.qihoo.util.QHClassLoader", isApk)) {
            ruleEntity
          } else {
            null
          }
        }
        "libapp.so" -> {
          return if (nativeLibs?.any { it.name == "libflutter.so" } == true || PackageUtils.hasDexClass(
              packageName,
              "io.flutter.FlutterInjector",
              isApk
            )
          ) {
            ruleEntity
          } else {
            null
          }
        }
        else -> return ruleEntity
      }
    } else {
      return ruleEntity
    }
  }

  suspend fun getRuleWithRegex(
    name: String,
    @LibType type: Int,
    packageName: String? = null,
    nativeLibs: List<LibStringItem>? = null
  ): RuleEntity? {
    return getRuleWithDexChecking(name, packageName, nativeLibs) ?: findRuleRegex(name, type)
  }

  fun checkNativeLibValidation(packageName: String, nativeLib: String): Boolean {
    return when (nativeLib) {
      "libjiagu.so" -> {
        PackageUtils.hasDexClass(packageName, "com.qihoo.util.QHClassLoader", false)
      }
      "libapp.so" -> {
        PackageUtils.hasDexClass(packageName, "io.flutter.FlutterInjector", false)
      }
      else -> true
    }
  }

  fun getNightMode(nightModeString: String): Int {
    return when (nightModeString) {
      Constants.DARK_MODE_OFF -> DayNightDelegate.MODE_NIGHT_NO
      Constants.DARK_MODE_ON -> DayNightDelegate.MODE_NIGHT_YES
      Constants.DARK_MODE_FOLLOW_SYSTEM -> DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      else -> DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
  }

  fun launchDetailPage(
    context: FragmentActivity,
    item: LCItem,
    refName: String? = null,
    refType: Int = NATIVE
  ) {
    if (item.abi.toInt() == OVERLAY) {
      OverlayDetailBottomSheetDialogFragment().apply {
        arguments = bundleOf(
          EXTRA_LC_ITEM to item
        )
        show(context.supportFragmentManager, tag)
      }
    } else {
      val intent = Intent(context, AppDetailActivity::class.java)
        .putExtras(
          bundleOf(
            EXTRA_PACKAGE_NAME to item.packageName,
            EXTRA_REF_NAME to refName,
            EXTRA_REF_TYPE to refType,
            EXTRA_DETAIL_BEAN to DetailExtraBean(
              item.isSplitApk,
              item.isKotlinUsed,
              item.variant
            )
          )
        )
      context.startActivity(intent)
    }
  }

  fun createLoadingDialog(context: ContextThemeWrapper): AlertDialog {
    return MaterialAlertDialogBuilder(context)
      .setView(
        LinearProgressIndicator(context).apply {
          layoutParams = ViewGroup.LayoutParams(200.dp, ViewGroup.LayoutParams.WRAP_CONTENT).also {
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
          }
          trackCornerRadius = 3.dp
          isIndeterminate = true
        }
      )
      .setCancelable(false)
      .create()
  }
}

/**
 * From drakeet
 */
fun doOnMainThreadIdle(action: () -> Unit) {
  val handler = Handler(Looper.getMainLooper())

  val idleHandler = MessageQueue.IdleHandler {
    handler.removeCallbacksAndMessages(null)
    action()
    return@IdleHandler false
  }

  fun setupIdleHandler(queue: MessageQueue) {
    queue.addIdleHandler(idleHandler)
  }

  if (Looper.getMainLooper() == Looper.myLooper()) {
    setupIdleHandler(Looper.myQueue())
  } else {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      setupIdleHandler(Looper.getMainLooper().queue)
    } else {
      handler.post { setupIdleHandler(Looper.myQueue()) }
    }
  }
}
