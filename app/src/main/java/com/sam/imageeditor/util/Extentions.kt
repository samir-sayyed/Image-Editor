package com.sam.imageeditor.util

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showDialog(
    showDialogParams: ShowDialogParams
): AlertDialog? {
    return MaterialAlertDialogBuilder(context ?: return null).apply {
        setTitle(showDialogParams.title)
        setMessage(showDialogParams.message)
        setPositiveButton(showDialogParams.textPositive) { _, _ ->
            showDialogParams.positiveListener?.invoke()
        }
        setNegativeButton(showDialogParams.textNegative) { _, _ ->
            showDialogParams.negativeListener?.invoke()
        }
        setCancelable(showDialogParams.cancelable)
    }.create().let { dialog ->
        dialog.setCanceledOnTouchOutside(showDialogParams.canceledOnTouchOutside)
        dialog.show()
        dialog
    }
}

data class ShowDialogParams(
    val title: String? = null,
    val message: String? = null,
    val textPositive: String? = null,
    val positiveListener: (() -> Unit)? = null,
    val textNegative: String? = null,
    val negativeListener: (() -> Unit)? = null,
    val cancelable: Boolean = false,
    val canceledOnTouchOutside: Boolean = false,
    val showMessage: Boolean = false,
    val checkBoxText: String? = null,
    val showCheckBox: Boolean = false,
    val checkBoxListener: ((Boolean) -> Unit)? = null,
    val titleCenter: Boolean = false
)