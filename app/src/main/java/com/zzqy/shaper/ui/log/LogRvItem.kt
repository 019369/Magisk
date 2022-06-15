package com.zzqy.shaper.ui.log

import androidx.databinding.Bindable
import com.zzqy.shaper.BR
import com.zzqy.shaper.R
import com.zzqy.shaper.core.model.su.SuLog
import com.zzqy.shaper.databinding.ObservableDiffRvItem
import com.zzqy.shaper.databinding.RvContainer
import com.zzqy.shaper.databinding.set
import com.zzqy.shaper.ktx.timeDateFormat
import com.zzqy.shaper.ktx.toTime

class LogRvItem(
    override val item: SuLog
) : ObservableDiffRvItem<LogRvItem>(), RvContainer<SuLog> {

    override val layoutRes = R.layout.item_log_access_md2

    val date = item.time.toTime(timeDateFormat)

    @get:Bindable
    var isTop = false
        set(value) = set(value, field, { field = it }, BR.top)

    @get:Bindable
    var isBottom = false
        set(value) = set(value, field, { field = it }, BR.bottom)

    override fun itemSameAs(other: LogRvItem) = item.appName == other.item.appName
}
