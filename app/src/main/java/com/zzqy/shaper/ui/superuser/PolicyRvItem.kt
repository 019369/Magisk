package com.zzqy.shaper.ui.superuser

import android.graphics.drawable.Drawable
import androidx.databinding.Bindable
import com.zzqy.shaper.BR
import com.zzqy.shaper.R
import com.zzqy.shaper.core.model.su.SuPolicy
import com.zzqy.shaper.databinding.ObservableDiffRvItem
import com.zzqy.shaper.databinding.RvContainer
import com.zzqy.shaper.databinding.set

class PolicyRvItem(
    private val viewModel: SuperuserViewModel,
    override val item: SuPolicy,
    val packageName: String,
    private val isSharedUid: Boolean,
    val icon: Drawable,
    val appName: String
) : ObservableDiffRvItem<PolicyRvItem>(), RvContainer<SuPolicy> {

    override val layoutRes = R.layout.item_policy_md2

    val title get() = if (isSharedUid) "[SharedUID] $appName" else appName

    private inline fun <reified T> setImpl(new: T, old: T, setter: (T) -> Unit) {
        if (old != new) {
            setter(new)
        }
    }

    @get:Bindable
    var isExpanded = false
        set(value) = set(value, field, { field = it }, BR.expanded)

    @get:Bindable
    var isEnabled
        get() = item.policy == SuPolicy.ALLOW
        set(value) = setImpl(value, isEnabled) {
            viewModel.togglePolicy(this, value)
        }

    @get:Bindable
    var shouldNotify
        get() = item.notification
        private set(value) = setImpl(value, shouldNotify) {
            item.notification = it
            viewModel.updateNotify(this)
        }

    @get:Bindable
    var shouldLog
        get() = item.logging
        private set(value) = setImpl(value, shouldLog) {
            item.logging = it
            viewModel.updateLogging(this)
        }

    fun toggleExpand() {
        isExpanded = !isExpanded
    }

    fun toggleNotify() {
        shouldNotify = !shouldNotify
    }

    fun toggleLog() {
        shouldLog = !shouldLog
    }

    fun revoke() {
        viewModel.deletePressed(this)
    }

    override fun itemSameAs(other: PolicyRvItem) = item.uid == other.item.uid

}
