# دعم Creative Ali لصيغة DLoF (Document Loop Format)

هذا الملف يوثّق مستوى توافق تطبيق **Creative Ali** مع مواصفة DLoF الرسمية
(`https://dlof.org/schema/1.0`، انظر `schema/dlof.xsd` و`schema/dlof-template.xsd`
في هذا المجلد لنسخة كاملة من المخطط الرسمي).

## ما هو مدعوم بالكامل الآن

| الجزء | الحالة | أين في الكود |
|---|---|---|
| `<metadata>` (title, domain, author, dates, language, tags, signature) | ✅ كامل | `dlof/DlofModel.kt`, `dlof/DlofXmlCodec.kt` |
| كل قيم `domain` الـ 19 (education, book, series, comic, manga, podcast...) | ✅ كامل | `DlofDomain` |
| `<loopLinks>` (previous/next/loopRoot) | ✅ كامل | `DlofLoopLinks` |
| أنواع المحتوى الستة: genericItem, qaItem, bookChapter, termDefinition, infoExplain, episodeItem | ✅ كامل قراءة وكتابة | `DlofContent` + `DlofXmlCodec` |
| `<attachments>` (صور/فيديو/صوت/ترجمة/ملفات، base64 أو مرجع خارجي) | ✅ كامل | `DlofAttachment` |
| `<mediaFolder>` (فهرس ملفات media/ المجاورة) | ✅ كامل | `DlofMediaFile` |
| `<template>` (ألوان، خط، تخطيط standard/card/magazine/minimal) | ✅ كامل | `DlofTemplate` |
| حزمة القالب القابلة للاسترداد `.dlofTemplate` | ✅ استيراد وتصدير | `DlofTemplatePackage.kt` |
| تشفير AES-256-GCM متوافق مع صيغة Best64 (v2 وv3 المعزّزة بـ HMAC) | ✅ كامل | `DlofCrypto.kt` |
| فحص هيكل حزمة `.dlofpkg` (set.txt، المجلدات، الوسائط، التشفير) | ✅ كامل | `DlofPackageValidator.kt` |
| نظام الحلقات/المسلسلات (ترقيم، ربط تلقائي، تحقق من التسلسل) | ✅ كامل | `DlofEpisodes.kt` |
| النسخ الاحتياطي الكامل للتطبيق (`.caibak`، مع تشفير اختياري) | ✅ كامل | `backup/BackupManager.kt` |
| الإعلانات (بانر + بيني) عبر AdMob | ✅ كامل (يتطلب استبدال معرّفات الإنتاج) | `ads/` |

## غير مدعوم بعد (مخطط له لاحقًا)

- `<remoteSync>` — النموذج موجود بالكامل في `DlofRemoteSync` والقراءة/الكتابة تعمل،
  لكن **لا يوجد بعد** منطق فعلي يجلب البيانات من الشبكة أو يقارن البصمات تلقائيًا.
- `<webPublish>` — نشر ملف dlof كصفحة ويب (SFTP/GitHub Pages/Netlify) — غير مُطبَّق بعد.
- تطبيق admin منفصل (تسجيل دخول، إدارة تحديثات) — تم استبعاده بطلب صريح؛ Creative Ali
  تطبيق واحد فقط.
- Argon2id الحقيقي: نستخدم PBKDF2 بضعف عدد التكرارات كبديل مؤقت (نفس أسلوب المرجع
  عندما لا تتوفر مكتبة BouncyCastle).

## التوافق الخلفي

الشكل المبسّط القديم (`DlofEntry` / `BDlofLoop` في `blogging/DlofDocument.kt`)
ما زال يعمل لمذكرات "التدوين" الموجودة مسبقًا ولم يُحذف؛ الملفات الجديدة أو
المستوردة من مصادر DLoF خارجية تُقرأ وتُكتب عبر `DlofDocumentV2` الكامل في هذا
المجلد.
