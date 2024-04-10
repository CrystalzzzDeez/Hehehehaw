package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterType
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.helixAroundVector
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.minecraft.core.BlockPos
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class IonTurretProjectile(
		ship: ActiveStarship?,
		loc: Location,
		dir: Vector,
		override val speed: Double,
		override val color: Color,
		override val range: Double,
		override val particleThickness: Double,
		override val explosionPower: Float,
		override val starshipShieldDamageMultiplier: Double,
		override val areaShieldDamageMultiplier: Double,
		override val soundName: String,
		override val balancing: StarshipWeapons.ProjectileBalancing?,
		shooter: Damager

): LaserProjectile(ship, loc, dir, shooter) {

	override val volume: Int = (range / 16).toInt()

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = dir.clone().normalize().multiply(travel)
		val particle = Particle.REDSTONE
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)
		for (location in helixAroundVector(oldLocation, vector, 0.3, 150, wavelength = 1.0)) {
			loc.world.spawnParticle(
					Particle.SOUL_FIRE_FLAME,
					location,
					0,
					0.5,
					0.5,
					0.5,
					0.0,
					null,
					true
			)
		}
		loc.world.spawnParticle(particle, loc.x, loc.y, loc.z, 1, 0.0, 0.0, 0.0, 0.5, dustOptions, true)
	}

	override fun onImpactStarship(starship: ActiveStarship) {
		val impactLocation = this.loc
		val shipsThrusters = starship.thrusters
		val shipsWeapons = starship.weapons
		for(thruster in shipsThrusters){
			if (impactLocation.distance(thruster.pos.toLocation(starship.world)) <= 5) {
				shipsThrusters.remove(thruster)
				Tasks.syncDelay(100L) {shipsThrusters.add(thruster)}
			}
		}
		for(weapon in shipsWeapons) {
			if (impactLocation.distance(weapon.pos.toLocation(starship.world)) <= 5) {
				weapon.fireCooldownNanos += 1000
				Tasks.syncDelay(50L) {weapon.fireCooldownNanos -= 1000}
			}
		}
	}
}


