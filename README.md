# ContractBoard

Готовый к запуску Paper-плагин (1.20.6, Java 21), который добавляет ежедневные контракты фракций с GUI, NPC и наградами.

## Возможности
- Три фракции: Рыбаки, Шахтеры, Охотники.
- Ротация контрактов по майнкрафтовским дням (по умолчанию) с детерминированным сидом на игрока/день.
- Хранение данных в SQLite через HikariCP.
- Инвентарное GUI (Adventure/MiniMessage).
- Опциональная интеграция экономики через Vault.

## Установка
1. Соберите плагин: `./gradlew build`.
2. Скопируйте jar из `build/libs/` в папку `plugins/` сервера.
3. Запустите сервер, чтобы сгенерировались конфиги и шаблоны.

## Команды
- `/contracts` (алиас `/cb`) — открыть GUI контрактов.
- `/contractsadmin` (алиас `/cba`) — админ-команды:
  - `reload`
  - `setrep <player> <faction> <value>`
  - `addrep <player> <faction> <delta>`
  - `resetday <player>`
  - `npc create <faction>`
  - `npc bind <faction>`
  - `debug <player>`

## Использование NPC
1. Выполните `/contractsadmin npc create <faction>`, чтобы создать NPC на вашей позиции.
2. Или смотрите на существующего жителя и выполните `/contractsadmin npc bind <faction>`.
3. ПКМ по NPC открывает меню фракции.

## Шаблоны контрактов
Шаблоны лежат в `plugins/ContractBoard/templates/*.yml`.
Каждый шаблон задаёт фракцию, цель, требования и награды.

Пример:
```yaml
id: mine_iron
faction: MINERS
weight: 8
displayName: "<gray>Добыть железную руду</gray>"
description:
  - "<gray>Добывайте железную руду для шахтеров.</gray>"
requirements:
  minRep: 0
objective:
  type: MINE_BLOCKS
  target: 20
  types:
    - IRON_ORE
    - DEEPSLATE_IRON_ORE
rewards:
  rep: 30
  money: 60.0
```

## Примечания
- Контракты обновляются по циклу `rotationMode` из `config.yml`. Для режима `minecraft` используется игровой день, для `real` — время `dailyResetTime`.
- По умолчанию генерируется 5 контрактов на цикл, но награду можно забрать только один раз (настраивается через `maxClaimsPerDay`).
- Прогресс отслеживается в реальном времени и сохраняется в SQLite.
