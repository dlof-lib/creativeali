package com.creativeali.app.container

import java.util.UUID

/**
 * وسام DLoF (Badge_name / Example / svg_icon) — وحدة عرض صغيرة تُلصق على
 * الحاوية لوصف حالتها أو دورها (مثال: "مدير"، "فعال"، "إدارة").
 * الصيغة النصية المرجعية: `Badge_name: name; Example: text; %svg_icon[ref];`
 */
data class DlofBadge(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var example: String = "",
    var svgIconRef: String? = null,
) {
    /** يُصدَّر بنفس صيغة "DLoF v.m" النصية المطلوبة عند التوليد/التصدير. */
    fun toDlofTag(): String =
        "[Badge_name: $name; Example: ${example.ifBlank { "بدون وصف" }}; %svg_icon[${svgIconRef.orEmpty()}];]"
}

/**
 * حاوية DLoF: مجلد/مشروع واحد يجمع مذكرة (.b.dlof) ومخطط واحد تحت اسم
 * ووصف وأيقونة وأوسمة موحّدة. [blogLoopId] و [diagramId] يشيران إلى
 * سجلّات [com.creativeali.app.blogging.BDlofLoop] و
 * [com.creativeali.app.diagrams.Diagram] الخاصة بهذه الحاوية تحديدًا.
 */
data class DlofContainer(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "حاوية بدون اسم",
    var description: String = "",
    var iconUri: String? = null, // null = الأيقونة الافتراضية
    var badges: MutableList<DlofBadge> = mutableListOf(),
    var allowSetTxt: Boolean = false, // السماح بملف ضبط تشغيل set.txt خاص بالحاوية
    var licenseText: String? = null, // ترخيص اختياري يُرفق عند تصدير .dlof / .dlofpkg
    val blogLoopId: String,
    val diagramId: String,
    var createdAt: Long = System.currentTimeMillis(),
)
