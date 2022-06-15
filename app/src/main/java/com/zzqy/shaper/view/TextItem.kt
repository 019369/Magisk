package com.zzqy.shaper.view

import com.zzqy.shaper.R
import com.zzqy.shaper.databinding.DiffRvItem

class TextItem(val text: Int) : DiffRvItem<TextItem>() {
    override val layoutRes = R.layout.item_text

    override fun contentSameAs(other: TextItem) = text == other.text
}
