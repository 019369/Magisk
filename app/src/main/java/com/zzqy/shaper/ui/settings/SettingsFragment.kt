package com.zzqy.shaper.ui.settings

import android.os.Bundle
import android.view.View
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseFragment
import com.zzqy.shaper.databinding.FragmentSettingsMd2Binding
import com.zzqy.shaper.di.viewModel
import rikka.recyclerview.addEdgeSpacing
import rikka.recyclerview.addItemSpacing
import rikka.recyclerview.fixEdgeEffect

class SettingsFragment : BaseFragment<FragmentSettingsMd2Binding>() {

    override val layoutRes = R.layout.fragment_settings_md2
    override val viewModel by viewModel<SettingsViewModel>()

    override fun onStart() {
        super.onStart()

        activity?.title = resources.getString(R.string.settings)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsList.apply {
            addEdgeSpacing(bottom = R.dimen.l1)
            addItemSpacing(R.dimen.l1, R.dimen.l_50, R.dimen.l1)
            fixEdgeEffect()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.items.forEach { it.refresh() }
    }

}
