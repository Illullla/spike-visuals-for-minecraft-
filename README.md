# Spike Visuals (Fabric 1.21.4)

Watermark, ArrayList, TargetHUD, TotemBar, Fullbright, частицы при ударе, Hand Customizer + меню на Right Shift.

## Клавиши (Настройки -> Управление -> Visuals)
Right Shift - меню | H ArrayList | K TargetHUD | B TotemBar | J Fullbright | L частицы | G Hand

Hand Customizer: позиция, поворот, размер руки/предмета и скорость взмаха.
Все настройки сохраняются при закрытии меню (config/visuals.properties и visuals-hand.properties).

## Получить .jar без установки чего-либо
1. Создайте репозиторий на github.com и загрузите ВСЕ файлы из этой папки (включая скрытую папку .github).
2. Вкладка Actions -> дождитесь зелёной галочки у workflow "build" (1-3 минуты).
3. Откройте запуск -> внизу Artifacts -> скачайте visuals-jar -> внутри spike-visuals-1.0.0.jar.
4. Положите jar в .minecraft/mods (нужны Fabric Loader и Fabric API для 1.21.4).

## Локальная сборка
JDK 21 + Gradle 8.12: `gradle build` -> build/libs/spike-visuals-1.0.0.jar

## Главное меню
Мод заменяет стартовое меню на Spike-экран (Singleplayer / Multiplayer / Options / Quit).
Отключить: Right Shift -> модуль TitleScreen -> выключить.
