# Dreadpasta — Fabric 1.21.4

Крипипаста-мод для Minecraft **1.21.4 Fabric**.

## Что есть

- частые скримеры через S2C-пакет и HUD-оверлей;
- сущность `dreadpasta:stalker`: игрокоподобный преследователь без ника;
- при касании Stalker телепортирует игрока в измерение `dreadpasta:dread`;
- жуткое измерение с ареной, коридором и порталом выхода;
- портал охраняется Stalker;
- сбои в чате;
- молнии рядом с игроком;
- удаление случайных блоков рядом с игроком;
- «сломанные чанки»: случайные дыры/блоки/скалк/crying obsidian вокруг игрока;
- команды управления.

## Команды

```mcfunction
/dreadpasta on
/dreadpasta off
/dreadpasta intensity 0..5
/dreadpasta spawn
/dreadpasta dread
```

## Сборка

Нужны Java 21 и Gradle.

```bash
gradle build
```

Готовый `.jar` будет в:

```text
build/libs/dreadpasta-1.0.0.jar
```

## Важно

Мод реально меняет блоки в мире. Перед тестом сделай копию сохранения.

Если хочешь мягкий режим:

```mcfunction
/dreadpasta intensity 1
```

Если хочешь отключить разрушения полностью — оставь `/dreadpasta off` или убери вызовы `deleteBlocksNear` / `breakChunkPatch` в `DreadpastaMod.java`.
