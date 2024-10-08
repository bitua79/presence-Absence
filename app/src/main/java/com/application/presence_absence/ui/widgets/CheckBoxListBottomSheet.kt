package com.application.presence_absence.ui.widgets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.application.presence_absence.databinding.BottomSheetCheckBoxListBinding
import com.application.presence_absence.ui.utils.createDialog
import com.application.presence_absence.ui.utils.gone
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class CheckBoxListBottomSheet(
    @StringRes val title: Int, private val hasSearchBar: Boolean
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCheckBoxListBinding
    protected lateinit var listAdapter: CheckBoxListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog.createDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCheckBoxListBinding.inflate(inflater, container, false)
        return binding.root
    }

    abstract var itemList: List<CheckBoxItemView>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerview()

        initViews()
        initFilteredOptions()
    }

    private fun initViews() {
        binding.title = getString(title)
        if (!hasSearchBar) binding.etSearch.gone()

        binding.etSearch.doAfterTextChanged {
            val query = it?.toString()?.trim().orEmpty()
            if (query.isNotEmpty()) {
                val filteredList = itemList.filter { item ->
                    item.text.contains(query)
                }
                listAdapter.submitList(filteredList)
            }
        }
    }

    abstract fun initFilteredOptions()

    private fun setupAdapter() {
        listAdapter = CheckBoxListAdapter(onItemChecked = {
            onItemChecked(it)
        }, onItemUnChecked = {
            onItemUnChecked(it)
        })
    }

    private fun setupRecyclerview() {
        with(binding.rvItemList) {
            setHasFixedSize(true)
            adapter = listAdapter
        }
        initValues()
    }

    open fun initValues() {
        listAdapter.submitList(itemList)
    }

    abstract fun onItemChecked(c: CheckBoxItemView)

    abstract fun onItemUnChecked(c: CheckBoxItemView)

}