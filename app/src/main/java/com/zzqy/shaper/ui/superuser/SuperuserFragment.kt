package com.zzqy.shaper.ui.superuser

import android.os.Bundle
import android.view.View
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseFragment
import com.zzqy.shaper.databinding.AnyDiffRvItem
import com.zzqy.shaper.databinding.FragmentSuperuserMd2Binding
import com.zzqy.shaper.databinding.adapterOf
import com.zzqy.shaper.di.viewModel
import rikka.recyclerview.addEdgeSpacing
import rikka.recyclerview.addItemSpacing
import rikka.recyclerview.fixEdgeEffect

class SuperuserFragment : BaseFragment<FragmentSuperuserMd2Binding>() {

    override val layoutRes = R.layout.fragment_superuser_md2
    override val viewModel by viewModel<SuperuserViewModel>()

    override fun onStart() {
        super.onStart()
        activity?.title = resources.getString(R.string.superuser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.superuserList.apply {
            adapter = adapterOf<AnyDiffRvItem>()
            addEdgeSpacing(top = R.dimen.l_50, bottom = R.dimen.l1)
            addItemSpacing(R.dimen.l1, R.dimen.l_50, R.dimen.l1)
            fixEdgeEffect()
        }
    }

    override fun onPreBind(binding: FragmentSuperuserMd2Binding) {}

}
