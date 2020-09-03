package net.zeeraa.novacore.spigot.abstraction.commons;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.zeeraa.novacore.commons.log.AbstractPlayerMessageSender;

public class AbstractBukkitPlayerMessageSender implements AbstractPlayerMessageSender {

	@Override
	public boolean trySendMessage(UUID uuid, String message) {
		Player player = Bukkit.getServer().getPlayer(uuid);

		if (player != null) {
			if (player.isOnline()) {
				player.sendMessage(message);
			}
		}

		return false;
	}
}