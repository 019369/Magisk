package com.zzqy.shaper.ui.home

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.zzqy.shaper.R
import com.zzqy.shaper.arch.BaseFragment
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.download.DownloadService
import com.zzqy.shaper.databinding.FragmentHomeMd2Binding
import com.zzqy.shaper.di.viewModel
import com.zzqy.shaper.events.RebootEvent

class HomeFragment : BaseFragment<FragmentHomeMd2Binding>() {

    override val layoutRes = R.layout.fragment_home_md2
    override val viewModel by viewModel<HomeViewModel>()

    override fun onStart() {
        super.onStart()
        activity?.title = resources.getString(R.string.section_home)
        setHasOptionsMenu(true)
        DownloadService.observeProgress(this, viewModel::onProgressUpdate)
    }

    private fun checkTitle(text: TextView, icon: ImageView) {
        text.post {
            if (text.layout?.getEllipsisCount(0) != 0) {
                with (icon) {
                    layoutParams.width = 0
                    layoutParams.height = 0
                    requestLayout()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // If titles are squished, hide icons
        with(binding.homeMagiskWrapper) {
            checkTitle(homeMagiskTitle, homeMagiskIcon)
        }
        with(binding.homeManagerWrapper) {
            checkTitle(homeManagerTitle, homeManagerIcon)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home_md2, menu)
        if (!Info.isRooted)
            menu.removeItem(R.id.action_reboot)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->
                HomeFragmentDirections.actionHomeFragmentToSettingsFragment().navigate()
            R.id.action_reboot -> activity?.let { RebootEvent.inflateMenu(it).show() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.stateManagerProgress = 0
    }
}
