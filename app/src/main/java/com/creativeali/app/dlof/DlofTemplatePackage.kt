package com.creativeali.app.dlof

import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * دعم حزمة القالب القابلة للاسترداد `.dlofTemplate`: ملف مضغوط يحتوي
 * `template.xml` (وصف رسمي حسب dlof-template.xsd) بالإضافة لنسخة مرجعية
 * اختيارية بصيغة نصية (مثلاً Kotlin) لإعادة الاستخدام في مشاريع أخرى.
 *
 * في الشاشة: "تصميم القالب" تسمح باستيراد `.dlofTemplate`، استرداد قالب
 * محفوظ محليًا، أو تصدير التصميم الحالي كحزمة جديدة للمشاركة.
 */
object DlofTemplatePackage {

    data class NamedTemplate(val id: String, val name: String, val template: DlofTemplate)

    fun export(named: NamedTemplate, outStream: OutputStream, referenceSourceCode: String? = null) {
        ZipOutputStream(outStream).use { zip ->
            zip.putNextEntry(ZipEntry("template.xml"))
            zip.write(writeTemplateXml(named).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            if (referenceSourceCode != null) {
                zip.putNextEntry(ZipEntry("Template.kt"))
                zip.write(referenceSourceCode.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
    }

    fun import(file: File): NamedTemplate {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry("template.xml") ?: error("template.xml مفقود داخل حزمة .dlofTemplate")
            val xml = zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).readText()
            return parseTemplateXml(xml)
        }
    }

    private fun writeTemplateXml(named: NamedTemplate): String {
        val t = named.template
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<dlofTemplate xmlns=\"https://dlof.org/schema/1.0/template\" id=\"${esc(named.id)}\" name=\"${esc(named.name)}\">\n")
            append("  <template")
            t.ref?.let { append(" ref=\"${esc(it)}\"") }
            t.primaryColor?.let { append(" primaryColor=\"${esc(it)}\"") }
            t.secondaryColor?.let { append(" secondaryColor=\"${esc(it)}\"") }
            t.backgroundColor?.let { append(" backgroundColor=\"${esc(it)}\"") }
            t.textColor?.let { append(" textColor=\"${esc(it)}\"") }
            t.fontFamily?.let { append(" fontFamily=\"${esc(it)}\"") }
            append(" layout=\"${t.layout.wire}\"")
            t.headerAttachmentRef?.let { append(" headerAttachmentRef=\"${esc(it)}\"") }
            append("/>\n")
            append("</dlofTemplate>\n")
        }
    }

    private fun parseTemplateXml(xml: String): NamedTemplate {
        fun attr(tag: String, name: String): String? =
            Regex("<$tag\\b[^>]*\\b$name=\"([^\"]*)\"").find(xml)?.groupValues?.get(1)

        val id = attr("dlofTemplate", "id") ?: java.util.UUID.randomUUID().toString()
        val name = attr("dlofTemplate", "name") ?: "قالب مستورد"
        val template = DlofTemplate(
            ref = attr("template", "ref"),
            primaryColor = attr("template", "primaryColor"),
            secondaryColor = attr("template", "secondaryColor"),
            backgroundColor = attr("template", "backgroundColor"),
            textColor = attr("template", "textColor"),
            fontFamily = attr("template", "fontFamily"),
            layout = DlofTemplateLayout.fromWire(attr("template", "layout")),
            headerAttachmentRef = attr("template", "headerAttachmentRef"),
        )
        return NamedTemplate(id, name, template)
    }

    private fun esc(s: String): String = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
