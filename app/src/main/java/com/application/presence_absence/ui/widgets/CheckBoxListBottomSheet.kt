package com.application.presence_absence.ui.widgets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.application.presence_absence.core.extensions.createDialog
import com.application.presence_absence.databinding.BottomSheetCheckBoxListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class CheckBoxListBottomSheet(@StringRes val title: Int) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCheckBoxListBinding
    protected lateinit var listAdapter: CheckBoxListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog.createDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCheckBoxListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title = getString(title)
        setupAdapter()
        setupRecyclerview()

        initFilteredOptions()
    }

    abstract fun initFilteredOptions()

    private fun setupAdapter() {
        listAdapter = CheckBoxListAdapter(
            onItemChecked = {
                onItemChecked(it)
            },
            onItemUnChecked = {
                onItemUnChecked(it)
            }
        )
    }

    private fun setupRecyclerview() {
        with(binding.rvItemList) {
            setHasFixedSize(true)
            adapter = listAdapter
        }
        initValues()
    }

    abstract fun initValues()

    abstract fun onItemChecked(c: CheckBoxItemView)

    abstract fun onItemUnChecked(c: CheckBoxItemView)

}