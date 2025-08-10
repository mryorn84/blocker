# این فایل برای تنظیمات ProGuard و R8 هست
# چون اینجا کتابخانه خاصی نداریم، چیزی لازم نیست
# ولی اگه بخوای میتونی قوانین محافظت از کلاس‌ها رو اضافه کنی

# مثال: جلوگیری از حذف MainActivity
-keep class com.example.youtubeblocker.MainActivity { *; }

# جلوگیری از حذف کلاس‌های AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**
