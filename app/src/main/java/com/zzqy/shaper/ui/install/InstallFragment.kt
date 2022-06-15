package com.zzqy.shaper.ui.install

import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseFragment
import com.zzqy.shaper.databinding.FragmentInstallMd2Binding
import com.zzqy.shaper.di.viewModel

class InstallFragment : BaseFragment<FragmentInstallMd2Binding>() {

    override val layoutRes = R.layout.fragment_install_md2
    override val viewModel by viewModel<InstallViewModel>()

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.install)
    }
}
