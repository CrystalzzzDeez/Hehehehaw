package net.horizonsend.ion.server.data.migrator

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.data.migrator.types.item.migrator.IdentifierUpdate
import net.horizonsend.ion.server.data.migrator.types.item.migrator.LegacyNameFixer
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType

object DataMigrators : IonServerComponent() {
	override fun onEnable() {
		registerDataVersions()
	}

	private val dataVersions = mutableListOf<DataVersion>()
	private val lastDataVersion get() = dataVersions.lastIndex

	private fun registerDataVersions() {
		registerDataVersion(DataVersion.builder(0).build()) // To align to index

		registerDataVersion(DataVersion
			.builder(1)
			.addMigrator(LegacyNameFixer(
				"DETONATOR", "SMOKE_GRENADE", "PUMPKIN_GRENADE", "GUN_BARREL", "CIRCUITRY", "PISTOL_RECEIVER", "RIFLE_RECEIVER",
				"SMB_RECEIVER", "SNIPER_RECEIVER", "SHOTGUN_RECEIVER", "CANNON_RECEIVER", "ALUMINUM_INGOT", "ALUMINUM_BLOCK", "RAW_ALUMINUM_BLOCK",
				"CHETHERITE", "CHETHERITE_BLOCK", "TITANIUM_INGOT", "TITANIUM_BLOCK", "RAW_TITANIUM_BLOCK", "URANIUM", "URANIUM_BLOCK",
				"RAW_URANIUM_BLOCK", "NETHERITE_CASING", "ENRICHED_URANIUM", "ENRICHED_URANIUM_BLOCK", "URANIUM_CORE", "URANIUM_ROD", "FUEL_ROD_CORE",
				"FUEL_CELL", "FUEL_CONTROL", "REACTIVE_COMPONENT", "REACTIVE_HOUSING", "REACTIVE_PLATING", "REACTIVE_CHASSIS", "REACTIVE_MEMBRANE",
				"REACTIVE_ASSEMBLY", "FABRICATED_ASSEMBLY", "CIRCUIT_BOARD", "MOTHERBOARD", "REACTOR_CONTROL", "SUPERCONDUCTOR", "SUPERCONDUCTOR_BLOCK",
				"SUPERCONDUCTOR_CORE", "STEEL_INGOT", "STEEL_BLOCK", "STEEL_PLATE", "STEEL_CHASSIS", "STEEL_MODULE", "STEEL_ASSEMBLY",
				"REINFORCED_FRAME", "REACTOR_FRAME", "PROGRESS_HOLDER", "BATTLECRUISER_REACTOR_CORE", "BARGE_REACTOR_CORE", "CRUISER_REACTOR_CORE", "UNLOADED_SHELL",
				"LOADED_SHELL", "UNCHARGED_SHELL", "CHARGED_SHELL", "ARSENAL_MISSILE", "PUMPKIN_GRENADE", "UNLOADED_ARSENAL_MISSILE", "ACTIVATED_ARSENAL_MISSILE",
				"GAS_CANISTER_EMPTY",
			))
			.addMigrator(IdentifierUpdate("PISTOL", "BLASTER_PISTOL"))
			.addMigrator(IdentifierUpdate("RIFLE", "BLASTER_RIFLE"))
			.addMigrator(IdentifierUpdate("SHOTGUN", "BLASTER_SHOTGUN"))
			.addMigrator(IdentifierUpdate("SNIPER", "BLASTER_SNIPER"))
			.addMigrator(IdentifierUpdate("CANNON", "BLASTER_CANNON"))
			.build()
		)

		registerDataVersion(DataVersion
			.builder(2)
			.build()
		)
	}

	private fun registerDataVersion(dataVersion: DataVersion) {
		dataVersions.add(dataVersion)
	}

	fun migrate(chunk: Chunk) {
		val chunkVersion = chunk.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (chunkVersion == lastDataVersion) return

		val nextVersion = minOf(chunkVersion + 1, lastDataVersion)
		val toApply = dataVersions.subList(nextVersion, lastDataVersion)

		val snapshot = chunk.chunkSnapshot

		for (x in 0..15) for (y in chunk.world.minHeight..chunk.world.maxHeight) for (z in 0..15) {
			val type = snapshot.getBlockType(x, y, z)
			if (Pipes.isPipedInventory(type)) {
				val state = chunk.getBlock(x, y, z).state as InventoryHolder
				migrateInventory(state.inventory, toApply)
			}
		}
	}

	fun migrate(player: Player) {
		val playerVersion = player.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (playerVersion == lastDataVersion) return

		val nextVersion = minOf(playerVersion + 1, lastDataVersion)
		val toApply = dataVersions.subList(nextVersion, lastDataVersion)

		migrateInventory(player.inventory, toApply)
	}

	private fun migrateInventory(inventory: Inventory, versions: List<DataVersion>) {
		for (dataVersion in versions) {
			dataVersion.migrateInventory(inventory, dataVersion.versionNumber)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerLogin(event: PlayerJoinEvent) {
		migrate(event.player)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		migrate(event.chunk)
	}
}