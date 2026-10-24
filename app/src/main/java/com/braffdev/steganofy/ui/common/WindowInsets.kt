package com.braffdev.steganofy.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Начиная с Android 15 окно занимает весь экран, и содержимое уезжает под строку состояния,
 * панель навигации и клавиатуру. Отступы приходится проставлять самим: система сообщает,
 * сколько места занимают её панели, и это значение становится внутренним отступом экрана.
 *
 * Снизу берётся большее из двух: высота панели навигации или высота клавиатуры, когда та
 * открыта. Так поле ввода остаётся над клавиатурой, а когда её нет, экран выглядит как обычно.
 *
 * Вызывать сразу после setContentView, на корне разметки экрана. Вешать обработчик выше,
 * на android.R.id.content, бесполезно: между ним и разметкой лежат контейнеры AppCompat,
 * и до неё отступы не доходят.
 *
 * Отступы из разметки сохраняются: системные прибавляются к ним, а не заменяют их.
 */
fun View.applyWindowInsetsAsPadding() {
    val ownLeft = paddingLeft
    val ownTop = paddingTop
    val ownRight = paddingRight
    val ownBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

        view.setPadding(
            ownLeft + bars.left,
            ownTop + bars.top,
            ownRight + bars.right,
            ownBottom + maxOf(bars.bottom, keyboard.bottom)
        )
        windowInsets
    }
}
