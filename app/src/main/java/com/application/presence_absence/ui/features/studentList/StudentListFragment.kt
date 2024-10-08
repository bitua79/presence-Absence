package com.application.presence_absence.ui.features.studentList

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.application.presence_absence.R
import com.application.presence_absence.core.utils.FirebaseAnalyticsHelper
import com.application.presence_absence.databinding.FragmentStudentListBinding
import com.application.presence_absence.ui.core.UiError
import com.application.presence_absence.ui.core.UiLoading
import com.application.presence_absence.ui.core.UiSuccess
import com.application.presence_absence.ui.core.UnAuthorizedError
import com.application.presence_absence.ui.features.examList.entities.ExamStatus
import com.application.presence_absence.ui.features.examList.entities.ExamView
import com.application.presence_absence.ui.features.studentList.entities.StudentStatus
import com.application.presence_absence.ui.features.studentList.entities.StudentView
import com.application.presence_absence.ui.utils.collectOnFragment
import com.application.presence_absence.ui.utils.createSnackbar
import com.application.presence_absence.ui.utils.gone
import com.application.presence_absence.ui.utils.visible
import com.application.presence_absence.ui.widgets.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class StudentListFragment : Fragment() {

    private lateinit var binding: FragmentStudentListBinding
    private lateinit var listAdapter: StudentListAdapter

    private val args by navArgs<StudentListFragmentArgs>()
    lateinit var examArg: ExamView
    var listIsInvoked = false

    private val viewModel: StudentListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVariables()
        initViewModel()
        initViews()
        initControllers()
    }

    private fun initVariables() {
        examArg = args.exam
        listIsInvoked = examArg.isInvoked || examArg.status == ExamStatus.CANCELLED
    }

    private fun initViewModel() {
        viewModel.getAllStudents(examArg.id.toString())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViews() {
        with(binding) {
            exam = args.exam
            listInvoked = listIsInvoked
            incShowPresents.title = getString(R.string.label_show_presents)
            incShowAbsents.title = getString(R.string.label_show_absents)

            incShowPresents.let {
                it.root.setOnClickListener {
                    viewModel.setShowPresents(true)
                    viewModel.setShowAbsents(null)
                }

                // Reset exam place filter value
                it.ivClose.setOnClickListener {
                    viewModel.setShowPresents(null)
                }
            }

            incShowAbsents.let {
                it.root.setOnClickListener {
                    viewModel.setShowAbsents(true)
                    viewModel.setShowPresents(null)
                }

                // Reset exam place filter value
                it.ivClose.setOnClickListener {
                    viewModel.setShowAbsents(null)
                }
            }

            tbStudentListToolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            etSearch.doAfterTextChanged {
                viewModel.setStudentQuery(etSearch.text?.toString()?.trim().orEmpty())
            }

            btnSubmit.setOnClickListener {
                // Log submit button select content
                FirebaseAnalyticsHelper().logInvokeExamSelectContent()

                viewModel.invokeExamStatus(examArg.id.toString())
                binding.listInvoked = true
                listAdapter.setListInvoked(true)
                listAdapter.notifyDataSetChanged()
            }

            srlRefresh.setOnRefreshListener {
                viewModel.getAllStudents(examArg.id.toString())
            }

            srlRefresh.setColorSchemeResources(
                R.color.color_primary,
                R.color.color_secondary
            )
        }

        setupAdapter()
        setupRecyclerview()
    }

    private fun setupAdapter() {
        listAdapter = StudentListAdapter(
            onSetPresence = {
                onStudentSetPresence(it)
            },
            onSetAbsence = {
                onStudentSetAbsence(it)
            },
            onRemoveAttendance = {
                onRemoveAttendance(it)
            },
            listInvoked = listIsInvoked
        )
    }

    private fun onStudentSetPresence(student: StudentView) {
        viewModel.setStudentAttendanceStatus(
            examId = examArg.id.toString(),
            studentId = student.id.toString(),
            state = StudentStatus.PRESENCE
        )
    }

    private fun onStudentSetAbsence(student: StudentView) {
        viewModel.setStudentAttendanceStatus(
            examId = examArg.id.toString(),
            studentId = student.id.toString(),
            state = StudentStatus.ABSENCE
        )
    }

    private fun onRemoveAttendance(student: StudentView) {
        viewModel.setStudentAttendanceStatus(
            examId = examArg.id.toString(),
            studentId = student.id.toString(),
            state = StudentStatus.NOT_SET
        )
    }

    private fun setupRecyclerview() {
        with(binding.rvStudentList) {
            setHasFixedSize(true)
            adapter = listAdapter
        }
    }

    private fun initControllers() {
        viewModel.uiViewState.collectOnFragment(this) {
            binding.isLoading = it is UiLoading
            binding.srlRefresh.isRefreshing = it is UiLoading
            if (it is UiLoading) {
                binding.tvNoStudent.gone()
            }

            if (it is UiError) {
                if (it is UnAuthorizedError) {
                    setList(emptyList())
                    AlertDialog(
                        title = getString(R.string.msg_unauthorized),
                        description = getString(R.string.msg_navigate_to_login),
                        buttonText = getString(R.string.label_got_it),
                        onOkClick = {
                            val action =
                                StudentListFragmentDirections.actionStudentListFragmentToLoginFragment()
                            findNavController().navigate(action)
                        }
                    ).show(requireActivity().supportFragmentManager, "UnAuthorized")
                    viewModel.clearState()

                } else {
                    createSnackbar(it.errorStringId, binding.dividerSnackBarView).show()
                }

            } else if (it is UiSuccess) {
                viewModel.clearState()
            }
        }

        viewModel.dataViewState.collectOnFragment(this) {
            setList(it.filteredList.orEmpty())
        }

        lifecycleScope.launch {
            viewModel.filter.collect { stdFilter ->
                binding.filterState = stdFilter
            }
        }
    }

    private fun setList(list: List<StudentView>) {
        listAdapter.submitList(list)

        val isLoading = viewModel.uiViewState.value is UiLoading
        with(binding.btnSubmit) {
            if (list.isEmpty()) gone()
            else if (!isLoading && !listIsInvoked) visible()
        }

        with(binding.tvNoStudent) {
            if (!isLoading && list.isEmpty()) visible() else gone()
        }
    }
}