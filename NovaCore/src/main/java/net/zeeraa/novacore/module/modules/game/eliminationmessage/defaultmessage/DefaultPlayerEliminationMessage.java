package net.zeeraa.novacore.module.modules.game.eliminationmessage.defaultmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import net.zeeraa.novacore.NovaCore;
import net.zeeraa.novacore.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.module.modules.game.eliminationmessage.PlayerEliminationMessage;
import net.zeeraa.novacore.teams.Team;

/**
 * This is the default player elimination message for NovaCore
 * 
 * @author Zeeraa
 */
public class DefaultPlayerEliminationMessage implements PlayerEliminationMessage {
	@Override
	public void showPlayerEliminatedMessage(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
		ChatColor playerColor = ChatColor.AQUA;

		String extra = "";

		if (NovaCore.getInstance().getTeamManager() != null) {
			Team playerTeam = NovaCore.getInstance().getTeamManager().getPlayerTeam(player);
			if (playerTeam != null) {
				playerColor = playerTeam.getTeamColor();
			}
		}

		switch (reason) {
		case DEATH:
			extra += "died";
			break;

		case DID_NOT_RECONNECT:
			extra += "did not reconnect in time";
			break;

		case QUIT:
			extra += "quit";
			break;

		case KILLED:
			String killerName = "";
			if (killer != null) {
				if (killer instanceof Projectile) {
					Entity theBoiWhoFirered = (Entity) ((Projectile) killer).getShooter();

					if (theBoiWhoFirered != null) {
						killerName = "by " + theBoiWhoFirered.getName();
					} else {
						killerName = "by " + killer.getName();
					}
				} else {
					killerName = "by " + killer.getName();
				}
			}

			extra += "was killed " + killerName;
			break;

		case OTHER:
			extra += "went out to buy some milk but never returned";
			break;
		default:
			extra += "went out to buy some milk but never returned";
			break;
		}

		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Player Eliminated> " + playerColor + ChatColor.BOLD + player.getName() + " " + ChatColor.GOLD + ChatColor.BOLD + extra);
	}
}