# CommandAPI

Un framework annotation-based per la creazione di comandi su piattaforme Minecraft: **Bukkit/Spigot**, **BungeeCord** e **Velocity**.

---

## Moduli

| Modulo | Piattaforma | Java |
|---|---|---|
| `commandapi-common` | Core condiviso | 8+ |
| `commandapi-bukkit` | Bukkit / Spigot | 8+ |
| `commandapi-bungee` | BungeeCord | 8+ |
| `commandapi-velocity` | Velocity | 17+ |

---

## Installazione

### Gradle

Aggiungi il repository e la dipendenza per la piattaforma desiderata:

**Bukkit / Spigot**
```groovy
repositories{
    maven {
    url = uri("https://repo.lorenzzzz.xyz/releases")
}
dependencies {
    implementation "dev.lorenzz:commandapi-bukkit:2.0.1"
}
```

**BungeeCord**
```groovy
repositories{
    maven {
    url = uri("https://repo.lorenzzzz.xyz/releases")
}
dependencies {
    implementation "dev.lorenzz:commandapi-bungee:2.0.1"
}
```

**Velocity**
```groovy
repositories{
    maven {
    url = uri("https://repo.lorenzzzz.xyz/releases")
}
dependencies {
    implementation "dev.lorenzz:commandapi-velocity:2.0.1"
}
```

---

## Quick Start

### Bukkit / Spigot

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.register(new MyCommands());
    }
}
```

### BungeeCord

```java
public class MyPlugin extends Plugin {

    @Override
    public void onEnable() {
        BungeeCommandManager manager = new BungeeCommandManager(this);
        manager.register(new MyCommands());
    }
}
```

### Velocity

```java
@Plugin(id = "myplugin")
public class MyPlugin {

    @Inject
    public MyPlugin(ProxyServer server) {
        VelocityCommandManager manager = new VelocityCommandManager(server, this);
        manager.register(new MyCommands());
    }
}
```

---

## Creare Comandi

### Comando semplice

```java
public class MyCommands {

    @Command(name = "hello", description = "Saluta il giocatore")
    public void hello(@Sender Player player) {
        player.sendMessage("Ciao!");
    }
}
```

Risultato: `/hello` → `Ciao!`

### Comando con argomenti

```java
public class MyCommands {

    @Command(name = "greet", description = "Saluta qualcuno")
    public void greet(@Sender Player player, @Named("target") Player target) {
        player.sendMessage("Hai salutato " + target.getName() + "!");
    }
}
```

Risultato: `/greet Steve` → `Hai salutato Steve!`

### Comando con sottocomandi

```java
public class GamemodeCommands {

    @Command(name = "gm", aliases = {"gamemode"}, description = "Cambia gamemode")
    public void root(@Sender Player player) {
        // Viene mostrata automaticamente la pagina di help
    }

    @Subcommand(name = "creative", description = "Modalità creativa")
    @Permission("gm.creative")
    public void creative(@Sender Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage("Gamemode impostato a Creative!");
    }

    @Subcommand(name = "survival", description = "Modalità sopravvivenza")
    @Permission("gm.survival")
    public void survival(@Sender Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("Gamemode impostato a Survival!");
    }
}
```

Risultato: `/gm` mostra l'help, `/gm creative` cambia gamemode.

---

## Annotazioni

### Metodo

| Annotazione | Target | Descrizione |
|---|---|---|
| `@Command` | Metodo | Dichiara un comando root |
| `@Subcommand` | Metodo | Dichiara un sottocomando |
| `@Permission` | Metodo | Richiede un permesso per eseguire il comando |

### Parametro

| Annotazione | Target | Descrizione |
|---|---|---|
| `@Sender` | Parametro | Identifica il parametro come il sender del comando |
| `@Named` | Parametro | Assegna un nome personalizzato all'argomento (visibile in usage/help) |
| `@Optional` | Parametro | Rende l'argomento opzionale, con valore di default |
| `@Flag` | Parametro | Dichiara un flag booleano (es. `-silent`) |
| `@Combined` | Parametro | Consuma tutto l'input rimanente come stringa unica |
| `@Hidden` | Parametro | Nasconde l'argomento dall'help e dall'usage |
| `@Range` | Parametro | Impone vincoli numerici min/max |

---

## Dettaglio Annotazioni

### `@Command`

```java
@Command(
    name = "test",           // nome del comando (obbligatorio)
    aliases = {"t", "tst"},  // alias
    async = true,            // esecuzione asincrona (default: true)
    description = "Un test", // descrizione per l'help
    usage = ""               // usage personalizzato (auto-generato se vuoto)
)
```

### `@Subcommand`

```java
@Subcommand(
    name = "sub",            // nome del sottocomando (obbligatorio)
    async = false,           // esecuzione asincrona (default: false)
    description = "Un sub"   // descrizione per l'help
)
```

### `@Permission`

```java
@Permission("myplugin.admin")
```

Se il sender non ha il permesso, riceve un messaggio di errore automatico.

### `@Sender`

```java
public void cmd(@Sender Player player) { ... }           // solo giocatori
public void cmd(@Sender CommandSender sender) { ... }     // qualsiasi sender
public void cmd(@Sender ProxiedPlayer player) { ... }     // BungeeCord
public void cmd(@Sender CommandSource source) { ... }     // Velocity
```

### `@Named`

```java
public void cmd(@Sender Player player, @Named("messaggio") String msg) { ... }
```

Usage generato: `/cmd <messaggio>`

### `@Optional`

```java
public void cmd(@Sender Player player, @Optional("default") String value) { ... }
```

Se l'argomento non viene fornito, viene usato `"default"`. Se `@Optional("")` il valore sarà `null` per oggetti, `0` per numeri, `false` per booleani.

### `@Flag`

```java
public void cmd(@Sender Player player, @Flag("-silent") boolean silent) { ... }
```

Usage: `/cmd [-silent]` — se il flag è presente, `silent = true`.

### `@Combined`

```java
public void cmd(@Sender Player player, @Combined @Named("messaggio") String message) { ... }
```

Cattura tutto il testo rimanente: `/cmd ciao a tutti` → `message = "ciao a tutti"`

### `@Range`

```java
public void cmd(@Sender Player player, @Range(min = 1, max = 100) int amount) { ... }
```

Se il valore è fuori range, viene inviato un errore automatico.

---

## Provider personalizzati

Puoi registrare provider per tipi personalizzati:

```java
BukkitCommandManager manager = new BukkitCommandManager(plugin);

manager.registerProvider(GameMode.class, new Provider<GameMode>() {
    @Override
    public GameMode provide(String input) throws CommandExitException {
        try {
            return GameMode.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandExitException("&cGamemode non valida: " + input);
        }
    }

    @Override
    public List<String> tabComplete(CommandActor actor, String arg) {
        return Arrays.stream(GameMode.values())
                .map(gm -> gm.name().toLowerCase())
                .filter(name -> name.startsWith(arg.toLowerCase()))
                .collect(Collectors.toList());
    }
});
```

### Provider già inclusi

| Tipo | Piattaforma |
|---|---|
| `String` | Tutte |
| `int`, `Integer` | Tutte |
| `long`, `Long` | Tutte |
| `double`, `Double` | Tutte |
| `float`, `Float` | Tutte |
| `boolean`, `Boolean` | Tutte |
| Qualsiasi `Enum` | Tutte (auto-generato) |
| `Player` | Bukkit |
| `OfflinePlayer` | Bukkit |
| `ProxiedPlayer` | BungeeCord |
| `Player` (Velocity) | Velocity |

---

## Esempio completo

```java
public class EconomyCommands {

    @Command(name = "economy", aliases = {"eco"}, description = "Gestione economia")
    public void root(@Sender Player player) {}

    @Subcommand(name = "give", description = "Dai soldi a un giocatore")
    @Permission("economy.give")
    public void give(
            @Sender Player sender,
            @Named("giocatore") Player target,
            @Range(min = 1, max = 1000000) @Named("importo") int amount,
            @Flag("-silent") boolean silent
    ) {
        // logica per dare soldi
        sender.sendMessage("&aHai dato &e" + amount + " &aa &e" + target.getName());
        if (!silent) {
            target.sendMessage("&aHai ricevuto &e" + amount + " &ada &e" + sender.getName());
        }
    }

    @Subcommand(name = "balance", description = "Controlla il saldo")
    public void balance(
            @Sender Player player,
            @Optional @Named("giocatore") Player target
    ) {
        Player check = target != null ? target : player;
        // logica per mostrare il saldo
        player.sendMessage("&eSaldo di " + check.getName() + ": &a1000");
    }
}
```

**Comandi generati:**
- `/economy` → mostra help con sottocomandi
- `/economy give <giocatore> <importo> [-silent]`
- `/economy balance [giocatore]`

---

## Struttura del progetto

```
commandapi/
├── commandapi-common/    Core condiviso (annotazioni, dispatcher, provider)
├── commandapi-bukkit/    Implementazione Bukkit/Spigot
├── commandapi-bungee/    Implementazione BungeeCord
└── commandapi-velocity/  Implementazione Velocity
```

---

## Build

```bash
./gradlew clean build
```

I JAR vengono generati in `<modulo>/build/libs/`.

---

## Licenza

Questo progetto è open source.
