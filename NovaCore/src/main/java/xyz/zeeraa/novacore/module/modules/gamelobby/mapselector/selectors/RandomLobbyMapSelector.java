package xyz.zeeraa.novacore.module.modules.gamelobby.mapselector.selectors;

import java.util.Random;

import xyz.zeeraa.novacore.module.modules.game.mapselector.MapSelector;
import xyz.zeeraa.novacore.module.modules.gamelobby.map.GameLobbyMapData;
import xyz.zeeraa.novacore.module.modules.gamelobby.mapselector.LobbyMapSelector;

/**
 * This {@link MapSelector} selects a random map form the map list
 * 
 * @author Zeeraa
 */
public class RandomLobbyMapSelector extends LobbyMapSelector {
	@Override
	public GameLobbyMapData getMapToUse() {
		if (maps.size() == 0) {
			return null;
		}

		return maps.get(new Random().nextInt(maps.size()));
	}
}