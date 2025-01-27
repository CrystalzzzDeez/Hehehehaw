package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class HeavyLaserProjectile(
    starship: ActiveStarship?,
	name: Component,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int,
    sound: String
) : TrackingLaserProjectile(starship, name, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.heavyLaser ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.heavyLaser
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val maxDegrees: Double = balancing.maxDegrees
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val color: Color = Color.RED
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val soundName: String = sound
}
