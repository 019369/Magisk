package com.zzqy.shaper.core.base

import android.app.Service
import android.content.Context
import com.zzqy.shaper.core.wrap

abstract class BaseService : Service() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrap())
    }
}
