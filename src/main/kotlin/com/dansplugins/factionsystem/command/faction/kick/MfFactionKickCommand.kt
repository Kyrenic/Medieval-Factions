package com.dansplugins.factionsystem.command.faction.kick

import com.dansplugins.factionsystem.MedievalFactions
import com.dansplugins.factionsystem.faction.permission.MfFactionPermission.Companion.KICK
import com.dansplugins.factionsystem.faction.permission.MfFactionPermission.Companion.SET_MEMBER_ROLE
import com.dansplugins.factionsystem.player.MfPlayer
import dev.forkhandles.result4k.onFailure
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.logging.Level.SEVERE

class MfFactionKickCommand(private val plugin: MedievalFactions) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("mf.kick")) {
            sender.sendMessage("$RED${plugin.language["CommandFactionKickNoPermission"]}")
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("$RED${plugin.language["CommandFactionKickNotAPlayer"]}")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("$RED${plugin.language["CommandFactionKickUsage"]}")
            return true
        }
        val target = try {
            plugin.server.getPlayer(UUID.fromString(args[0])) ?: plugin.server.getOfflinePlayer(args[0])
        } catch (exception: IllegalArgumentException) {
            plugin.server.getOfflinePlayer(args[0])
        }
        if (!target.hasPlayedBefore()) {
            sender.sendMessage("$RED${plugin.language["CommandFactionKickInvalidTarget"]}")
            return true
        }
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val playerService = plugin.services.playerService
            val mfPlayer = playerService.getPlayer(sender)
                ?: playerService.save(MfPlayer.fromBukkit(sender)).onFailure {
                    sender.sendMessage("$RED${plugin.language["CommandFactionKickFailedToSavePlayer"]}")
                    plugin.logger.log(SEVERE, "Failed to save player: ${it.reason.message}", it.reason.cause)
                    return@Runnable
                }
            val targetMfPlayer = playerService.getPlayer(target)
                ?: playerService.save(MfPlayer.fromBukkit(target)).onFailure {
                    sender.sendMessage("$RED${plugin.language["CommandFactionKickFailedToSaveTargetPlayer"]}")
                    plugin.logger.log(SEVERE, "Failed to save player: ${it.reason.message}", it.reason.cause)
                    return@Runnable
                }
            val factionService = plugin.services.factionService
            val faction = factionService.getFaction(mfPlayer.id)
            if (faction == null) {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickMustBeInAFaction"]}")
                return@Runnable
            }
            val role = faction.getRole(mfPlayer.id)
            if (role == null || !role.hasPermission(faction, KICK)) {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickNoFactionPermission"]}")
                return@Runnable
            }
            if (mfPlayer.id.value == targetMfPlayer.id.value) {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickCannotKickSelf"]}")
                return@Runnable
            }
            val targetRole = faction.getRole(targetMfPlayer.id)
            if (targetRole != null && faction.members.filter { it.player.id.value != targetMfPlayer.id.value }.none {
                val memberRole = faction.getRole(it.player.id)
                memberRole?.hasPermission(faction, SET_MEMBER_ROLE(targetRole.id)) == true
            }) {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickNoOneCanSetTheirRole"]}")
                return@Runnable
            }
            if (faction.members.none { it.player.id.value == targetMfPlayer.id.value }) {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickTargetNotInFaction"]}")
                return@Runnable
            }
            factionService.save(
                faction.copy(members = faction.members.filter { it.player.id.value != targetMfPlayer.id.value })
            ).onFailure {
                sender.sendMessage("$RED${plugin.language["CommandFactionKickFailedToSaveFaction"]}")
                plugin.logger.log(SEVERE, "Failed to save faction: ${it.reason.message}", it.reason.cause)
                return@Runnable
            }
            sender.sendMessage("$GREEN${plugin.language["CommandFactionKickSuccess", target.name ?: "unknown player", faction.name]}")
        })
        return true
    }
}