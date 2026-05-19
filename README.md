# Weeping Creeper

Creepers that freeze when you look at them. Move faster when you don't. Only damage *you*, not your house, when they explode.

A Weeping Angel × Creeper crossbreed for Minecraft 26.1.x on Fabric and NeoForge.

## Requirements

- Minecraft **26.1.x**
- Java **25**
- Fabric Loader **0.18.4+** with **Fabric API**, *or* NeoForge **26.1+**

## Downloads

Coming soon to Modrinth and CurseForge.

## What it does

- **Freezes when observed.** If any nearby player has the weeping creeper within their 120° front cone AND line-of-sight, it stops moving completely. Look away (or break line-of-sight) and it resumes.
- **Faster than vanilla.** Movement speed bumped from `0.25` to `0.35` by default — noticeably quicker, not zombie-fast.
- **Player-only explosion.** No terrain damage, no damage to mobs or other entities. Only players in radius take damage. Vanilla creeper damage scaling preserved.
- **Replaces vanilla creepers** by default. Every vanilla creeper spawn becomes a weeping creeper. Tunable from 0–100% in config.

## How it works under the hood

- Custom entity (`WeepingCreeperEntity extends Creeper`) — inherits the swell timer, AI goals, attack logic, and charged-creeper handling. Vanilla creeper texture is reused (so installed texture packs apply automatically).
- Look detection: dot product between player look vector and player→creeper vector vs. `cos(arc / 2)`, plus a vanilla `hasLineOfSight()` check.
- The freeze: custom server AI step skips `super.customServerAiStep()` while observed and zeros horizontal velocity.
- Explosion: a tiny mixin (`CreeperTickMixin`) intercepts the private `Creeper.explodeCreeper()` call inside `Creeper.tick()` and redirects to our custom `explodeWeeping()` for weeping creepers — vanilla creepers are unaffected. Custom explosion uses `Level.ExplosionInteraction.NONE` (no block damage) plus a manual damage pulse to nearby players only.
- Spawn replacement: `EntityJoinLevelEvent` (NeoForge) / `ServerEntityEvents.ENTITY_LOAD` (Fabric) — when a vanilla creeper spawns naturally, swap it for a weeping creeper.
- Tears overlay: a `RenderLayer` draws our `tears.png` on top of the vanilla creeper model. The base creeper texture comes from `minecraft:textures/entity/creeper/creeper.png`, so installed resource packs (Faithful, Sphax, etc.) apply naturally. Toggle live in config — no resource reload needed.

## Configuration

Config file at `config/weepingcreeperlite.json` (created on first launch):

```json
{
  "replacementChance": 1.0,
  "movementSpeed": 0.35,
  "lookArcDegrees": 120.0,
  "tearsEnabled": true,
  "explosionRadius": 3.0,
  "chargedExplosionRadius": 6.0
}
```

| Key | Default | Range | Notes |
|---|---|---|---|
| `replacementChance` | `1.0` | `0.0` – `1.0` | Fraction of vanilla creeper spawns that become weeping creepers. `0.0` = never replace; `1.0` = always. |
| `movementSpeed` | `0.35` | `0.01` – `2.0` | Vanilla creeper is `0.25`. Higher = faster when not observed. |
| `lookArcDegrees` | `120.0` | `1.0` – `360.0` | Front cone width (degrees) that counts as "looking at". |
| `tearsEnabled` | `true` | bool | Cosmetic tears overlay on top of the creeper texture. |
| `explosionRadius` | `3.0` | `0.5` – `20.0` | Base explosion radius. Vanilla creeper is `3.0`. |
| `chargedExplosionRadius` | `6.0` | `0.5` – `40.0` | Explosion radius when struck by lightning. Vanilla charged is `6.0`. |

Edit the file and restart Minecraft (or the dedicated server) for changes to take effect. The `tearsEnabled` toggle takes effect live without restart.

## Building

```bash
./gradlew buildAll
```

Produces:
- `fabric/build/libs/weepingcreeperlite-fabric-<version>.jar`
- `neoforge/build/libs/weepingcreeperlite-neoforge-<version>.jar`

## License

CC0-1.0.
