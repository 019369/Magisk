package com.zzqy.shaper.view

import com.zzqy.shaper.R
import com.zzqy.shaper.databinding.DiffRvItem

sealed class TappableHeadlineItem : DiffRvItem<TappableHeadlineItem>() {

    abstract val title: Int
    abstract val icon: Int

    override val layoutRes = R.layout.item_tappable_headline

    // --- listener

    interface Listener {

        fun onItemPressed(item: TappableHeadlineItem)

    }

    // --- objects

    object ThemeMode : TappableHeadlineItem() {
        override val title = R.string.settings_dark_mode_title
        override val icon = R.drawable.ic_day_night
    }

}
