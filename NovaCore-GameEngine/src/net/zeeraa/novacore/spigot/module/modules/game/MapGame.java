package net.zeeraa.novacore.spigot.module.modules.game;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.module.modules.game.events.GameStartEvent;
import net.zeeraa.novacore.spigot.module.modules.game.map.GameMap;
import net.zeeraa.novacore.spigot.module.modules.game.map.GameMapData;
import net.zeeraa.novacore.spigot.module.modules.game.map.mapmodule.MapModule;

/**
 * This is the same as {@link Game} but this type of game uses pre made maps for
 * games
 * <p>
 * If this extends {@link Listener} {@link GameManager#loadGame(Game)} will
 * register the listener when called
 * <p>
 * When creating games remember to call {@link Game#sendBeginEvent()} as soon as
 * the game begins and the count downs have finished
 * 
 * @author Zeeraa
 */
public abstract class MapGame extends Game {
	private GameMap activeMap;

	public MapGame() {
		super();

		this.activeMap = null;
	}

	/**
	 * Get the loaded map.
	 * <p>
	 * You can check if a map has been loaded using {@link MapGame#hasActiveMap()}
	 * 
	 * @return The loaded {@link GameMap} or null if no map has been loaded
	 */
	public GameMap getActiveMap() {
		return activeMap;
	}

	/**
	 * Check if a map has been loaded
	 * 
	 * @return <code>true</code> if a map has been loaded
	 */
	public boolean hasActiveMap() {
		return activeMap != null;
	}

	/**
	 * Load a map
	 * 
	 * @param mapData The {@link GameMapData} to load
	 * @return <code>true</code> on success. <code>false</code> the a map has
	 *         already been loaded
	 * @throws IOException if and {@link IOException} occurs while loading the world
	 */
	public boolean loadMap(GameMapData mapData) throws IOException {
		if (hasActiveMap()) {
			return false;
		}

		GameMap map = mapData.load();

		this.activeMap = map;
		this.world = map.getWorld();

		for (MapModule mapModule : map.getMapData().getMapModules()) {
			mapModule.onMapLoad(map);
		}

		return true;
	}

	/**
	 * Start the game
	 * 
	 * @return <code>false</code> if this has already been called
	 */
	@Override
	public boolean startGame() {
		if (startCalled) {
			return false;
		}
		startCalled = true;

		Bukkit.getServer().getPluginManager().callEvent(new GameStartEvent(this));

		for (MapModule module : activeMap.getMapData().getMapModules()) {
			Log.trace("MapGame", "Calling onGameStart() for map module " + module.getName());
			module.onGameStart(this);
		}

		if (autoEndGame()) {
			winCheckTask.start();
		}

		this.onStart();

		return true;
	}
}