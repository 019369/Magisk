package com.zzqy.shaper.di

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.LinkMovementMethod
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import com.zzqy.shaper.core.Const
import com.zzqy.shaper.core.magiskdb.PolicyDao
import com.zzqy.shaper.core.magiskdb.SettingsDao
import com.zzqy.shaper.core.magiskdb.StringDao
import com.zzqy.shaper.data.database.SuLogDatabase
import com.zzqy.shaper.data.repository.LogRepository
import com.zzqy.shaper.data.repository.NetworkService
import com.zzqy.shaper.ktx.deviceProtectedContext
import com.zzqy.shaper.ui.home.HomeViewModel
import com.zzqy.shaper.ui.install.InstallViewModel
import com.zzqy.shaper.ui.log.LogViewModel
import com.zzqy.shaper.ui.superuser.SuperuserViewModel
import com.zzqy.shaper.ui.surequest.SuRequestViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.utils.NoCopySpannableFactory

val AppContext: Context inline get() = ServiceLocator.context

@SuppressLint("StaticFieldLeak")
object ServiceLocator {

    lateinit var context: Context
    val deContext by lazy { context.deviceProtectedContext }
    val timeoutPrefs by lazy { deContext.getSharedPreferences("su_timeout", 0) }

    // Database
    val policyDB = PolicyDao()
    val settingsDB = SettingsDao()
    val stringDB = StringDao()
    val sulogDB by lazy { createSuLogDatabase(deContext).suLogDao() }
    val logRepo by lazy { LogRepository(sulogDB) }

    // Networking
    val okhttp by lazy { createOkHttpClient(context) }
    val retrofit by lazy { createRetrofit(okhttp) }
    val markwon by lazy { createMarkwon(context) }
    val networkService by lazy {
        NetworkService(
            createApiService(retrofit, Const.Url.GITHUB_PAGE_URL),
            createApiService(retrofit, Const.Url.GITHUB_RAW_URL),
            createApiService(retrofit, Const.Url.GITHUB_API_URL)
        )
    }

    object VMFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                HomeViewModel::class.java -> HomeViewModel(networkService)
                LogViewModel::class.java -> LogViewModel(logRepo)
                SuperuserViewModel::class.java -> SuperuserViewModel(policyDB)
                InstallViewModel::class.java -> InstallViewModel(networkService)
                SuRequestViewModel::class.java -> SuRequestViewModel(policyDB, timeoutPrefs)
                else -> modelClass.newInstance()
            } as T
        }
    }
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModel() =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ServiceLocator.VMFactory)[VM::class.java]
    }

private fun createSuLogDatabase(context: Context) =
    Room.databaseBuilder(context, SuLogDatabase::class.java, "sulogs.db")
        .fallbackToDestructiveMigration()
        .build()

private fun createMarkwon(context: Context) =
    Markwon.builder(context).textSetter { textView, spanned, bufferType, onComplete ->
        textView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            setSpannableFactory(NoCopySpannableFactory.getInstance())
            setText(spanned, bufferType)
            onComplete.run()
        }
    }.build()
