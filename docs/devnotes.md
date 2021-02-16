# Getting Started with Development
- Install the JDK
- Install IntelliJ IDEA
- Clone the repo
- Run gradlew genIntellijRuns
- Run gradlew openIdea
- Copy build\libs\BazaarNotifier-<version>.jar into the Minecraft mods directory
- Make sure everything still works by launching Minecraft


# How Things Work
## BazaarNotifier
This is the entry point that class that handles mod initialization by calling into other classes and contains static references to other top level mod data like the module list, mod configuration, etc..

## Modules
Modules represent on-screen information displayed by the mod. The current modules are:
- NotificationModules - Shows orders, including price and if they are best, matched, or outbid
- SuggestionModule - Displays suggestions for items to flip
- BankModule - Wired up but stubbed out currently.

## ModuleList
The ModuleList handles proxying operations to each active module, including initial configuration, drawing on the screen, resetting, generating configuration to be persisted.

## BazaarNotifierConfig
Handles all configuration file operations, including:
- Loading the configuration during the FMLPreInitializationEvent event.
- Generating a new config when a new mod version is detected while preserving the private API key if it is present.

## HypixelApiWrapped
Handles all calls to the Hypixel API, including:
- Getting Bazaar data
- Checking if the private API key is valid

## ChestTickHandler
- 

## EventHandler
### Chat Events
### Menu Opened Events
- Determines if the current menu  is a chest from the Bazaar, currently doesn't detect the product type selection or buy/sell screen
### disconnectEvent
- Sets the inBazaar global to false
### renderBazaarEvent (BackgroundDrawnEvent)
- Calls drawAllModules on the static modules ModuleList on the BazaarNotifier object if the BazaarNotifier static inBazaar value is true.
- Not sure how this is referenced - maybe Forge automatically hooks it up due to the SubscribeEvent property on the method?
### renderOutlines (RenderGameOverlayEvent)
- Calls drawAllOutlines on the static modules ModuleList on the BazaarNotifier object if the BazaarNotifier static inBazaar value is true.
- Not sure how this is referenced - maybe Forge automatically hooks it up due to the SubscribeEvent property on the method?

# Things left to do
- Wire up logging properly to the logger.
- Abstract away player / chat logging.
