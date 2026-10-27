package ru.dzhaparidze.mykct.feature

import androidx.compose.animation.AnimatedContent
import ru.dzhaparidze.mykct.feature.schedule.drawAmbientGlow
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.dzhaparidze.mykct.R
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.dzhaparidze.mykct.data.auth.AuthService
import ru.dzhaparidze.mykct.feature.profile.AccountButton
import ru.dzhaparidze.mykct.feature.profile.ProfileSheet
import ru.dzhaparidze.mykct.data.ThemeMode
import ru.dzhaparidze.mykct.feature.auth.LoginScreen
import ru.dzhaparidze.mykct.feature.home.HomeScreen
import ru.dzhaparidze.mykct.feature.schedule.ScheduleScreen
import ru.dzhaparidze.mykct.feature.schedule.ScheduleViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import ru.dzhaparidze.mykct.ui.theme.OledBackground
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import ru.dzhaparidze.mykct.ui.MechanicalSelector
import ru.dzhaparidze.mykct.feature.settings.SettingsScreen

enum class Screen { SCHEDULE, HOME, SETTINGS }

/** Shared content clearance: 50-dp visible mechanism, breathing room and the system navigation area. */
@Composable
fun navBarInset(): Dp = 64.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/**
 * Оболочка приложения: экран + механический селектор, утопленный в нижнюю границу.
 * Навигации как таковой нет - три экрана и переключатель, роутер тут не за что.
 */
@Composable
fun AppShell(
    openWeek: LocalDate?,
    onWeekOpened: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    // Старт на расписании: "Главная" пока пустая, открывать приложение на заглушке незачем.
    var screen by rememberSaveable { mutableStateOf(Screen.SCHEDULE) }
    // Форма входа живёт здесь, а не на экранах: навбар плавает поверх контента,
    // и открытая изнутри экрана форма оказывалась под ним.
    var loginOpen by rememberSaveable { mutableStateOf(false) }
    var profileOpen by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = remember { AuthService.get(context) }
    val session by auth.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // Кнопка аккаунта одна на три шапки: у вошедшего - профиль, иначе - форма входа.
    val account: @Composable () -> Unit = {
        // Сохранённая, но не подтверждённая без сети сессия - тоже "вошёл": форма входа ей не нужна
        val signedIn = session.user != null || session.hasStoredSession
        AccountButton(
            signedIn = signedIn,
            onClick = { if (signedIn) profileOpen = true else loginOpen = true },
        )
    }

    // Тап по пушу: закрыть листы и открыть его неделю в расписании
    val scheduleViewModel: ScheduleViewModel = viewModel()
    LaunchedEffect(openWeek) {
        val week = openWeek ?: return@LaunchedEffect
        profileOpen = false
        loginOpen = false
        screen = Screen.SCHEDULE
        scheduleViewModel.show(week)
        onWeekOpened()
    }


    val accent = MaterialTheme.colorScheme.primary
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Чёрная тема - без света в фоне. Свет дрейфует с периодом 90 с, 8 кадров
    // в секунду хватает: за кадр пятно сдвигается меньше чем на пиксель.
    val glow = MaterialTheme.colorScheme.background != OledBackground
    val drift by produceState(0f, glow) {
        while (glow) {
            value = ((1 - cos(2 * PI * System.currentTimeMillis() / 90_000)) / 2).toFloat()
            delay(125)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фон и свет по кромкам - общие для всех экранов и живут здесь,
        // а не внутри каждого: при смене экрана подложка не участвует в переходе,
        // меняется только содержимое. Раньше фон рисовал каждый экран сам, поэтому
        // гас вместе с ним - и в середине перехода экран целиком уходил в чёрный.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind { if (glow) drawAmbientGlow(accent, darkTheme, drift) },
        ) {
        // Содержимое сменяется по очереди: старое гаснет 120 мс, новое проявляется 180 мс.
        // Одновременный наплыв кладёт текст на текст, а разнести по времени можно только
        // потому, что подложка не анимируется - в паузе видно фон со светом, а не пустоту.
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                fadeIn(tween(180, delayMillis = 120)) togetherWith fadeOut(tween(120))
            },
            label = "screen",
            modifier = Modifier.fillMaxSize(),
        ) { current ->
            when (current) {
                Screen.SCHEDULE -> ScheduleScreen(account = account)
                Screen.HOME -> HomeScreen(onLogin = { loginOpen = true }, account = account)

                Screen.SETTINGS -> SettingsScreen(
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    account = account,
                )
            }
        }
        }

        // Paint through the system navigation inset: the housing's lower edge dissolves into
        // the same background instead of ending above the gesture handle over timetable text.
        val bottomColor = MaterialTheme.colorScheme.background
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navBarInset() + 16.dp)
                .background(
                    Brush.verticalGradient(
                        0f to bottomColor.copy(alpha = 0f),
                        0.45f to bottomColor.copy(alpha = 0.92f),
                        0.72f to bottomColor,
                        1f to bottomColor,
                    ),
                ),
        )

        MechanicalSelector(
            icons = listOf(R.drawable.ic_calendar, R.drawable.ic_home, R.drawable.ic_settings),
            labels = listOf("Расписание", "Главная", "Настройки"),
            selectedIndex = screen.ordinal,
            onSelect = { screen = Screen.entries[it] },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (profileOpen && (session.user != null || session.hasStoredSession)) {
            ProfileSheet(
                user = session.user,
                onSignOut = {
                    profileOpen = false
                    scope.launch { auth.signOut() }
                },
                onDismiss = { profileOpen = false },
            )
        }

        if (loginOpen) {
            LoginScreen(onBack = { loginOpen = false }, onSuccess = { loginOpen = false })
        }
    }
}

