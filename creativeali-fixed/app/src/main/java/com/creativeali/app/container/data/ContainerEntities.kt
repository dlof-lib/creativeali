package com.creativeali.app.container.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.creativeali.app.container.DlofBadge

@Entity(tableName = "containers")
data class ContainerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconUri: String?,
    val badges: List<DlofBadge>, // via ContainerConverters
    val allowSetTxt: Boolean,
    val licenseText: String?,
    val blogLoopId: String,
    val diagramId: String,
    val createdAt: Long,
)

/**
 * يشفّر قائمة الأوسمة كنص واحد بدون الحاجة لجدول علاقي منفصل أو مكتبة
 * تسلسل خارجية: كل وسام = `name¶example¶svgRef` وكل الأوسمة مفصولة بـ `§`.
 */
class ContainerConverters {
    @TypeConverter
    fun badgesToString(badges: List<DlofBadge>): String =
        badges.joinToString("§") { "${it.id}¶${it.name}¶${it.example}¶${it.svgIconRef.orEmpty()}" }

    @TypeConverter
    fun stringToBadges(value: String): List<DlofBadge> {
        if (value.isBlank()) return emptyList()
        return value.split("§").mapNotNull { chunk ->
            val parts = chunk.split("¶")
            if (parts.size < 3) return@mapNotNull null
            DlofBadge(
                id = parts[0],
                name = parts[1],
                example = parts[2],
                svgIconRef = parts.getOrNull(3)?.ifBlank { null },
            )
        }
    }
}
