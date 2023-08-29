package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot

abstract class PlayerController(val player: Player, starship: Starship, name: String) : Controller(starship, name) {
	override val pilotName: Component = player.displayName()

	override var yaw: Float = 0F
		get() = player.location.yaw

	override var pitch: Float = 0F
		get() = player.location.pitch

	override val color: Color
		 get() = PlayerCache[player].nationOid?.let { Color.fromRGB( NationCache[it].color ) } ?: super.color

	override fun canDestroyBlock(block: Block): Boolean = BlockBreakEvent(block, player).callEvent()

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block) =
		BlockPlaceEvent(block, block.state, placedAgainst, player.activeItem, player, true, EquipmentSlot.HAND).callEvent()

	override fun audience(): Audience = player
}
