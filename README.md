# ContractBoard

Production-ready Paper plugin (1.20.6, Java 21) that adds daily faction contracts with GUI, NPCs, and rewards.

## Features
- Three factions: Fishers, Miners, Hunters.
- Daily contract rotation with deterministic seed per player/day.
- SQLite persistence with HikariCP.
- Inventory GUI (Adventure/MiniMessage).
- Optional Vault economy integration.

## Installation
1. Build the plugin: `./gradlew build`.
2. Copy the jar from `build/libs/` to your server `plugins/` folder.
3. Start the server to generate config and templates.

## Commands
- `/contracts` (alias `/cb`) — open contracts GUI.
- `/contractsadmin` (alias `/cba`) — admin commands:
  - `reload`
  - `setrep <player> <faction> <value>`
  - `addrep <player> <faction> <delta>`
  - `resetday <player>`
  - `npc create <faction>`
  - `npc bind <faction>`
  - `debug <player>`

## NPC Usage
1. Run `/contractsadmin npc create <faction>` to spawn an NPC at your location.
2. Or look at an existing villager and run `/contractsadmin npc bind <faction>`.
3. Right-click the NPC to open the faction menu.

## Templates
Templates are stored in `plugins/ContractBoard/templates/*.yml`.
Each template defines faction, objective, requirements, and rewards.

Example:
```yaml
id: mine_iron
faction: MINERS
weight: 8
displayName: "<gray>Mine Iron Ore</gray>"
description:
  - "<gray>Mine iron ore for the miners."
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

## Notes
- Contracts rotate daily at `dailyResetTime` from `config.yml`.
- Progress is tracked in real time and saved to SQLite.
