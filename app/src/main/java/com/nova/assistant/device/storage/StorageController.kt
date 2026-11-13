package com.nova.assistant.device.storage

import android.os.Environment
import android.os.StatFs
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class StorageInfo(
    val availableBytes: Long,
    val totalBytes: Long,
    val availableGb: Double,
    val totalGb: Double,
    val percentageFree: Int
)

@Singleton
class StorageController @Inject constructor() {

    fun getInternalStorageInfo(): StorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong

            val availableBytes = availableBlocks * blockSize
            val totalBytes = totalBlocks * blockSize

            val availableGb = availableBytes / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val pct = if (totalBytes > 0) ((availableBytes.toFloat() / totalBytes.toFloat()) * 100).toInt() else 0

            StorageInfo(
                availableBytes = availableBytes,
                totalBytes = totalBytes,
                availableGb = String.format(Locale.US, "%.1f", availableGb).toDouble(),
                totalGb = String.format(Locale.US, "%.1f", totalGb).toDouble(),
                percentageFree = pct
            )
        } catch (_: Exception) {
            StorageInfo(0, 0, 0.0, 0.0, 0)
        }
    }
}
