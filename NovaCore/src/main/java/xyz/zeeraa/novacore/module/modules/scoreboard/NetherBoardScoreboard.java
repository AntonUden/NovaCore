package xyz.zeeraa.novacore.module.modules.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import xyz.zeeraa.novacore.NovaCore;
import xyz.zeeraa.novacore.module.NovaModule;

public class NetherBoardScoreboard extends NovaModule implements Listener {
	private static NetherBoardScoreboard instance;

	private int lineCount;

	private String defaultTitle;

	private HashMap<UUID, BPlayerBoard> boards;

	private HashMap<Integer, String> globalLines;
	private HashMap<UUID, HashMap<Integer, String>> playerLines;

	private HashMap<UUID, ChatColor> playerNameColor;

	private int taskId;

	public static NetherBoardScoreboard getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "NetherBoardScoreboard";
	}

	@Override
	public void onLoad() {
		NetherBoardScoreboard.instance = this;
		this.lineCount = 15;
		this.defaultTitle = "";
		this.boards = new HashMap<UUID, BPlayerBoard>();
		this.globalLines = new HashMap<Integer, String>();
		this.playerLines = new HashMap<UUID, HashMap<Integer, String>>();
		this.playerNameColor = new HashMap<UUID, ChatColor>();
		this.taskId = -1;
	}

	@Override
	public void onEnable() {
		for (int i = 0; i < lineCount; i++) {
			globalLines.put(i, "");
		}

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			createPlayerScoreboard(player);
		}

		if (taskId == -1) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(NovaCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (UUID uuid : boards.keySet()) {
						Player player = Bukkit.getServer().getPlayer(uuid);

						if (player != null) {
							update(player);
						}
					}
				}
			}, 5L, 5L);
		}
	}

	@Override
	public void onDisable() {
		while (boards.size() > 0) {
			UUID uuid = boards.keySet().iterator().next();

			BPlayerBoard board = boards.get(uuid);
			board.delete();
			
			boards.remove(uuid);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		createPlayerScoreboard(p);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		removeScoreboard(p);
	}

	private boolean createPlayerScoreboard(Player player) {
		if (!boards.containsKey(player.getUniqueId())) {
			BPlayerBoard board = Netherboard.instance().createBoard(player, defaultTitle);

			HashMap<Integer, String> pLines = new HashMap<Integer, String>();

			for (int i = 0; i < lineCount; i++) {
				pLines.put(i, "");
				board.set("", i);
			}

			boards.put(player.getUniqueId(), board);
			playerLines.put(player.getUniqueId(), pLines);

			for (ChatColor chatColor : ChatColor.values()) {
				Team team = board.getScoreboard().registerNewTeam("PLAYER_COLOR_" + chatColor.name());

				team.setPrefix(chatColor + "");
				team.setNameTagVisibility(NameTagVisibility.ALWAYS);
			}

			for (UUID uuid : playerNameColor.keySet()) {
				Team team = board.getScoreboard().getTeam("PLAYER_COLOR_" + playerNameColor.get(uuid).name());
				if (team != null) {
					OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(uuid);
					if (p != null) {
						if (p.getName() != null) {
							if (!team.getEntries().contains(p.getName())) {
								team.addEntry(p.getName());
							}
						}
					}
				}
			}

			update(player);

			return true;
		}
		return false;
	}

	private boolean removeScoreboard(Player player) {
		if (boards.containsKey(player.getUniqueId())) {
			boards.get(player.getUniqueId()).delete();
			boards.remove(player.getUniqueId());
			return true;
		}

		return false;
	}

	public void update(Player player) {
		if (boards.containsKey(player.getUniqueId())) {
			BPlayerBoard board = boards.get(player.getUniqueId());

			HashMap<Integer, String> pLines = playerLines.get(player.getUniqueId());

			ArrayList<String> existingLines = new ArrayList<String>();

			int lineNumber = lineCount;
			for (int i = 0; i < lineCount; i++) {
				String line = "";

				if (pLines.containsKey(i)) {
					String content = pLines.get(i);
					if (content.length() > 0) {
						line = content;
					}
				}

				if (line.length() == 0) {
					if (globalLines.containsKey(i)) {
						line = globalLines.get(i);
					}
				}

				if (line.length() == 0) {
					line = "";
				}

				while (existingLines.contains(line)) {
					line += ChatColor.RESET;
				}

				existingLines.add(line);

				board.set(line, lineNumber);

				lineNumber--;
			}
		}
	}

	public HashMap<UUID, BPlayerBoard> getBoards() {
		return boards;
	}

	public int getLineCount() {
		return lineCount;
	}

	public void setLineCount(int lineCount) {
		this.lineCount = lineCount;
	}

	public String getDefaultTitle() {
		return defaultTitle;
	}

	public void setDefaultTitle(String defaultTitle) {
		this.defaultTitle = defaultTitle;
	}

	public void setGlobalLine(int line, String content) {
		globalLines.put(line, content);
	}

	public void clearGlobalLine(int line) {
		this.setGlobalLine(line, "");
	}

	public void setPlayerLine(int line, Player player, String content) {
		if (playerLines.containsKey(player.getUniqueId())) {
			playerLines.get(player.getUniqueId()).put(line, content);
		}
	}

	public void clearPlayerLine(int line, Player player) {
		this.setPlayerLine(line, player, "");
	}

	public HashMap<Integer, String> getGlobalLines() {
		return globalLines;
	}

	public HashMap<UUID, HashMap<Integer, String>> getPlayerLines() {
		return playerLines;
	}
	
	public void resetPlayerNameColor(OfflinePlayer player) {
		setPlayerNameColor(player, null);
	}

	public void setPlayerNameColor(OfflinePlayer player, ChatColor newColor) {
		ChatColor oldColor = null;
		if (playerNameColor.containsKey(player.getUniqueId())) {
			oldColor = playerNameColor.get(player.getUniqueId());
		}

		if (oldColor != null) {
			for (UUID uuid : boards.keySet()) {
				BPlayerBoard board = boards.get(uuid);

				Team team = board.getScoreboard().getTeam("PLAYER_COLOR_" + oldColor.name());

				if (team != null) {
					if (player.getName() != null) {
						if (team.getEntries().contains(player.getName())) {
							team.removeEntry(player.getName());
						}
					}
				}
			}
		}

		if (newColor != null) {
			for (UUID uuid : boards.keySet()) {
				BPlayerBoard board = boards.get(uuid);

				Team team = board.getScoreboard().getTeam("PLAYER_COLOR_" + oldColor.name());

				if (team != null) {
					if (player.getName() != null) {
						if (!team.getEntries().contains(player.getName())) {
							team.addEntry(player.getName());
						}
					}
				}
			}
		}
	}
}