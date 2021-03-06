package net.zeeraa.novacore.spigot.module.modules.game.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.zeeraa.novacore.spigot.module.modules.game.elimination.PlayerEliminationReason;

/**
 * Called when a player is eliminated
 * 
 * @author Zeeraa
 */
public class PlayerEliminatedEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private OfflinePlayer player;
	private Entity killer;
	private PlayerEliminationReason reason;
	private int placement;

	private boolean cancel;

	public PlayerEliminatedEvent(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
		this.player = player;
		this.killer = killer;
		this.reason = reason;
		this.placement = placement;

		this.cancel = false;
	}

	/**
	 * Get the player that was eliminated
	 * 
	 * @return {@link OfflinePlayer}
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * Get the entity that killed the player.
	 * <p>
	 * this will always be <code>null</code> unless the
	 * {@link PlayerEliminationReason} is {@link PlayerEliminationReason#KILLED}
	 * 
	 * @return {@link Entity}
	 */
	public Entity getKiller() {
		return killer;
	}

	/**
	 * Get the reason why the player was eliminated
	 * 
	 * @return {@link PlayerEliminationReason}
	 */
	public PlayerEliminationReason getReason() {
		return reason;
	}

	/**
	 * Get the placement of the eliminated player
	 * 
	 * @return Placement number
	 */
	public int getPlacement() {
		return placement;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;

	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}