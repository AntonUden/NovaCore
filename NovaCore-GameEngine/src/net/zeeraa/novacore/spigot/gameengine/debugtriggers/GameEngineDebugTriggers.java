package net.zeeraa.novacore.spigot.gameengine.debugtriggers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.debug.DebugCommandRegistrator;
import net.zeeraa.novacore.spigot.debug.DebugTrigger;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.game.MapGame;
import net.zeeraa.novacore.spigot.module.modules.game.map.GameMap;

public class GameEngineDebugTriggers {
	public static void init() {
		DebugCommandRegistrator.getInstance().addDebugTrigger(new DebugTrigger() {
			private Map<Player, Integer> index = new HashMap<Player, Integer>();

			private void next(Player player) {
				int i = 0;
				if (index.containsKey(player)) {
					i = index.get(player);
				}

				MapGame game = (MapGame) GameManager.getInstance().getActiveGame();
				GameMap map = game.getActiveMap();

				List<Location> starterLocations = map.getStarterLocations();

				if (i >= starterLocations.size()) {
					player.sendMessage(ChatColor.LIGHT_PURPLE + "All locations tested");
					return;
				}

				Location location = starterLocations.get(i);

				player.sendMessage(ChatColor.LIGHT_PURPLE + "Testing spawn location " + ChatColor.GREEN + i + ChatColor.LIGHT_PURPLE + " at " + ChatColor.GREEN + location.toString());
				player.teleport(location);

				i++;
				
				index.put(player, i);

				new BukkitRunnable() {
					@Override
					public void run() {
						next(player);
					}
				}.runTaskLater(NovaCore.getInstance(), 100L);
			}

			@Override
			public void onExecute(CommandSender sender, String commandLabel, String[] args) {
				if (GameManager.getInstance().isEnabled()) {
					if (GameManager.getInstance().hasGame()) {
						if (GameManager.getInstance().getActiveGame() instanceof MapGame) {
							MapGame game = (MapGame) GameManager.getInstance().getActiveGame();

							if (game.hasActiveMap()) {
								Player player = (Player) sender;
								
								if(index.containsKey(player)) {
									index.remove(player);
								}
								
								next(player);
							} else {
								sender.sendMessage("The active game does not have a map right now");
							}
						} else {
							sender.sendMessage("The active game is not a MapGame");
						}
					} else {
						sender.sendMessage("GameManager dest not have an active game");
					}
				} else {
					sender.sendMessage("GameManager not enabled");
				}
			}

			@Override
			public PermissionDefault getPermissionDefault() {
				return PermissionDefault.OP;
			}

			@Override
			public String getPermission() {
				return "novacore.debug.testgamespawnlocations";
			}

			@Override
			public String getName() {
				return "testgamespawnlocations";
			}

			@Override
			public AllowedSenders getAllowedSenders() {
				return AllowedSenders.PLAYERS;
			}
		});
	}
}
