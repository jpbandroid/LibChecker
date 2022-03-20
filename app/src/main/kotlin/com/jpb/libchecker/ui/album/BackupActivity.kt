package com.jpb.libchecker.ui.album

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.jpb.libchecker.R

import com.jpb.libchecker.base.BaseActivity
import com.jpb.libchecker.constant.Constants
import com.jpb.libchecker.databinding.ActivityBackupBinding
import com.jpb.libchecker.utils.LCAppUtils
import com.jpb.libchecker.utils.StorageUtils
import com.jpb.libchecker.utils.showToast
import com.jpb.libchecker.viewmodel.SnapshotViewModel
import rikka.recyclerview.fixEdgeEffect
import rikka.widget.borderview.BorderRecyclerView
import rikka.widget.borderview.BorderView
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : BaseActivity<ActivityBackupBinding>() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setAppBar(binding.appbar, binding.toolbar)
    (binding.root as ViewGroup).bringChildToFront(binding.appbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupFragment())
        .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  class BackupFragment : PreferenceFragmentCompat() {

    private val viewModel: SnapshotViewModel by viewModels()

    private lateinit var backupResultLauncher: ActivityResultLauncher<String>
    private lateinit var restoreResultLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
      super.onAttach(context)
      backupResultLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
          it?.let {
            try {
              val dialog = LCAppUtils.createLoadingDialog(requireActivity())
              dialog.show()
              requireActivity().contentResolver.openOutputStream(it)?.let { os ->
                viewModel.backup(os) {
                  dialog.dismiss()
                }
              }
            } catch (e: IOException) {
              Timber.e(e)
            }
          }
        }
      restoreResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
          it?.let {
            try {
              requireActivity().contentResolver.openInputStream(it)
                ?.let { inputStream ->
                  val dialog = LCAppUtils.createLoadingDialog(requireActivity())
                  dialog.show()
                  viewModel.restore(inputStream) { success ->
                    if (!success) {
                      context.showToast("Backup file error")
                    }
                    dialog.dismiss()
                  }
                }
            } catch (e: IOException) {
              Timber.e(e)
            }
          }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.album_backup, rootKey)

      findPreference<Preference>(Constants.PREF_LOCAL_BACKUP)?.apply {
        setOnPreferenceClickListener {
          val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
          val date = Date()
          val formatted = simpleDateFormat.format(date)

          if (StorageUtils.isExternalStorageWritable) {
            runCatching {
              backupResultLauncher.launch("LibChecker-Snapshot-Backups-$formatted.lcss")
            }.onFailure {
              Timber.e(it)
              context.showToast("Document API not working")
            }
          } else {
            context.showToast("External storage is not writable")
          }
          true
        }
      }
      findPreference<Preference>(Constants.PREF_LOCAL_RESTORE)?.apply {
        setOnPreferenceClickListener {
          runCatching {
            restoreResultLauncher.launch("*/*")
          }.onFailure {
            Timber.e(it)
            context.showToast("Document API not working")
          }
          true
        }
      }
    }

    override fun onCreateRecyclerView(
      inflater: LayoutInflater,
      parent: ViewGroup,
      savedInstanceState: Bundle?
    ): RecyclerView {
      val recyclerView = super.onCreateRecyclerView(
        inflater,
        parent,
        savedInstanceState
      ) as BorderRecyclerView
      recyclerView.fixEdgeEffect()
      recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

      val lp = recyclerView.layoutParams
      if (lp is FrameLayout.LayoutParams) {
        lp.rightMargin =
          recyclerView.context.resources.getDimension(rikka.material.R.dimen.rd_activity_horizontal_margin)
            .toInt()
        lp.leftMargin = lp.rightMargin
      }

      recyclerView.borderViewDelegate.borderVisibilityChangedListener =
        BorderView.OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean ->
          (activity as? BaseActivity<*>)?.appBar?.setRaised(!top)
        }
      return recyclerView
    }
  }
}
