package com.zzqy.shaper.core.base

import android.app.job.JobService
import android.content.Context
import com.zzqy.shaper.core.wrap

abstract class BaseJobService : JobService() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrap())
    }
}
