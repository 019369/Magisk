package com.zzqy.shaper.ui.theme

import com.zzqy.shaper.arch.BaseViewModel
import com.zzqy.shaper.events.RecreateEvent
import com.zzqy.shaper.events.dialog.DarkThemeDialog
import com.zzqy.shaper.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> darkModePressed()
        else -> Unit
    }

    fun saveTheme(theme: Theme) {
        theme.select()
        RecreateEvent().publish()
    }

    private fun darkModePressed() = DarkThemeDialog().publish()

}
