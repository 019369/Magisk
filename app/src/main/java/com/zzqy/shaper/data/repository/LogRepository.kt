package com.zzqy.shaper.data.repository

import com.zzqy.shaper.core.Const
import com.zzqy.shaper.core.Info
import com.zzqy.shaper.core.model.su.SuLog
import com.zzqy.shaper.data.database.SuLogDao
import com.zzqy.shaper.ktx.await
import com.topjohnwu.superuser.Shell


class LogRepository(
    private val logDao: SuLogDao
) {

    suspend fun fetchSuLogs() = logDao.fetchAll()

    suspend fun fetchMagiskLogs(): String {
        val list = object : AbstractMutableList<String>() {
            val buf = StringBuilder()
            override val size get() = 0
            override fun get(index: Int): String = ""
            override fun removeAt(index: Int): String = ""
            override fun set(index: Int, element: String): String = ""
            override fun add(index: Int, element: String) {
                if (element.isNotEmpty()) {
                    buf.append(element)
                    buf.append('\n')
                }
            }
        }
        if (Info.env.isActive) {
            Shell.cmd("cat ${Const.SHAPER_LOG} || logcat -d -s Shaper").to(list).await()
        } else {
            Shell.cmd("logcat -d").to(list).await()
        }
        return list.buf.toString()
    }

    suspend fun clearLogs() = logDao.deleteAll()

    fun clearMagiskLogs(cb: (Shell.Result) -> Unit) =
        Shell.cmd("echo -n > ${Const.SHAPER_LOG}").submit(cb)

    suspend fun insert(log: SuLog) = logDao.insert(log)

}
