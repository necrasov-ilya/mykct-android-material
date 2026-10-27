package ru.dzhaparidze.mykct.ui.theme

import androidx.compose.ui.graphics.Color

// Токены сняты с крипто-референса: near-black фон, фиолетовый акцент.
// Фирменный градиент убран: акцент плоский - #8635BC, все заливки идут им.
val Violet = Color(0xFF8635BC)       // акцент: активный пункт, кольцо, ссылки, карточки пар
val VioletSoft = Color(0xFFE8E5FF)   // светлая заливка под акцент в светлой теме

// Статусы взяты светлее и насыщеннее, чем обычная веб-палитра: на near-black фоне
// #22C55E / #F5A623 / #E0575B выглядели тусклыми, будто выключенные.
val Green = Color(0xFF30D97C)         // кольцо прогресса, "в плюсе", "был на паре"
val Warning = Color(0xFFFFAE1A)       // пропуск по уважительной
val Danger = Color(0xFFFF5A63)        // ошибка, пропуск без уважительной

// Те же три статуса для светлой темы: тёмные тона Radix (шаг 11 шкал grass/amber/red).
// Светлые дают на белом контраст 1.9 / 1.9 / 3.0 - число в ячейке календаря не читается.
val GreenDeep = Color(0xFF2A7E3B)
val WarningDeep = Color(0xFFAB6400)
val DangerDeep = Color(0xFFCE2C31)

// Заливка ячейки календаря - одна на обе темы (Radix, шаг 9), текст поверх неё Ink.
// Белого текста на жёлтой заливке не бывает: любой оттенок, который ещё читается как
// жёлтый, даёт с белым 1.6:1. С Ink все три дают 6.4 / 12.4 / 5.0.

val GreenFill = Color(0xFF46A758)
val WarningFill = Color(0xFFFFC53D)
val DangerFill = Color(0xFFE5484D)

// Светлая тема
val LightBackground = Color(0xFFF4F4F8)
val GreyFill = Color(0xFFE7E7EE)      // невыбранные круги дней
// Оттенок с референса, но затемнён: исходный #9A9AA5 давал на белом 2.8:1 при норме
// WCAG AA 4.5:1 - подписи дней и время в таймлайне читались плохо. Этот даёт 5.0:1.
val GreyText = Color(0xFF6E6E7A)
val Ink = Color(0xFF16161D)

// Тёмная тема (основная в референсе)
val DarkBackground = Color(0xFF0C0C11)
val DarkSurface = Color(0xFF16161D)   // карточки, навбар
val DarkGreyFill = Color(0xFF23232D)  // круглые кнопки, невыбранные круги
val DarkGreyText = Color(0xFF8A8A99)
val DarkSettingsFill = Color(0xFF141413)  // карточки настроек в тёмной теме: от фона отделяются заливкой, не обводкой

// Чёрная тема: фон гасит пиксели OLED, карточки берут фон тёмной темы
val OledBackground = Color.Black
val OledSurface = DarkBackground
