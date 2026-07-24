# Creative Ali

تطبيق أندرويد (Kotlin + Jetpack Compose) بقسمين:

1. **التدوين (Blogging)** — كتابة مذكرات بصيغة DLoF:
   - `.dlof` — مذكرة واحدة (عنوان + نص + وسائط)
   - `.b.dlof` — سلسلة/حلقة من المذكرات مترابطة ذاتيًا (loop)
   - `.dlofpkg` — حزمة مضغوطة تحوي الحلقة + الوسائط + `set.txt`
   - الكود: `app/src/main/java/com/creativeali/app/blogging/`

2. **المخططات (Diagrams)** — لوحة سحب وإسقاط لإنشاء مخططات مشاريع/تطبيقات:
   - أشكال هندسية (مستطيل، دائرة، خط)، نصوص، صور، فيديوهات
   - تحكم كامل بلون التعبئة، لون الحافة، سُمك الحافة، استدارة الزوايا، لون النص
   - مكتبة خطوط وأيقونات قابلة للتوسّع من `assets/fonts/` و `assets/icons/`
   - الكود: `app/src/main/java/com/creativeali/app/diagrams/`

أيقونة التطبيق مأخوذة من الشعار المرفق ومولّدة بكل الأحجام (`mipmap-*`).

## البنية

```
CreativeAli/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/creativeali/app/
│       │   ├── MainActivity.kt          # تنقّل بقسمين (تبويب سفلي)
│       │   ├── CreativeAliApp.kt
│       │   ├── ui/theme/                # ألوان مستوحاة من الشعار
│       │   ├── blogging/                # نموذج DLoF + القراءة/الكتابة + التصدير
│       │   ├── diagrams/                # عناصر المخطط + اللوحة + الشاشة
│       │   └── library/                 # قارئ مكتبة الخطوط والأيقونات
│       ├── res/                          # الأيقونة، الألوان، النصوص (عربي RTL)
│       └── assets/
│           ├── fonts/                   # ضع ملفات الخطوط هنا
│           ├── icons/                   # ضع ملفات الأيقونات هنا
│           └── dlofpkg_template/set.txt.template
├── .github/workflows/build-apk.yml       # بناء APK تلقائيًا عبر GitHub Actions
└── settings.gradle.kts / build.gradle.kts / gradle.properties
```

## حالة المشروع — مهم

هذا **مشروع Android Studio حقيقي وقابل للفتح والبناء**، بواجهتين تعملان
فعليًا (تنقّل، حفظ مذكرة، إضافة عناصر للمخطط، سحبها، تلوينها، واختيار صور
وفيديوهات من الجهاز). لكنه **نقطة انطلاق (scaffold) وليس نسخة نهائية طويلة
الأمد** مثل تطبيق DLoF Reader (`dlof-go-main`) الذي بُني على مدى فترة طويلة
بآلاف الأسطر. الأشياء التالية لم تُبنَ بعد ويُفترض إكمالها تدريجيًا:

- تخزين دائم للمذكرات والمخططات (Room/DataStore) — حاليًا كل شيء في الذاكرة فقط
- تنفيذ زر "تصدير .dlofpkg" فعليًا داخل الواجهة (المنطق جاهز في `DlofPackage.kt`، يحتاج فقط ربطه بزر الحفظ وجمع روابط الوسائط)
- تشغيل فعلي للفيديو داخل عنصر المخطط (حاليًا صورة مصغّرة فقط)
- تكبير/تدوير العناصر في المخطط (السحب للتحريك جاهز، التحجيم/الدوران لا)
- تعبئة `assets/fonts/` و `assets/icons/` بمكتبة ضخمة فعلية من الخطوط والأيقونات — المُحمّل (`FontLibrary` / `IconLibrary`) جاهز ويكتشف أي ملف تضيفه تلقائيًا
- `gradle/wrapper/gradle-wrapper.jar` غير موجود (ملف ثنائي) — افتح المشروع في Android Studio مرة واحدة وسيُنشئه تلقائيًا، أو نفّذ `gradle wrapper` إذا كان Gradle مثبتًا لديك

## البناء

- **محليًا**: افتح المجلد في Android Studio (Hedgehog أو أحدث) → Sync → Run.
- **GitHub**: ادفع المشروع إلى مستودع جديد، وسيعمل `build-apk.yml` تلقائيًا
  وينتج APK تجريبي كـ artifact في كل push إلى `main`.
