package xyz.zeeraa.novacore.version.v1_8_R3;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public class VersionIndependentUtils implements xyz.zeeraa.novacore.abstraction.VersionIndependantUtils {
	@SuppressWarnings("deprecation")
	@Override
	public void setBlockAsPlayerSkull(Block block) {
		block.setType(Material.SKULL);
		Skull skull = (Skull) block.getState();
		skull.setSkullType(SkullType.PLAYER);

		block.getState().update(true);

		block.setData((byte) 1);
	}

	@Override
	public ItemStack getItemInMainHand(Player player) {
		return player.getItemInHand();
	}

	@Override
	public ItemStack getItemInOffHand(Player player) {
		return null; // 1.8 does not have a player off hand
	}

	@Override
	public double getEntityMaxHealth(LivingEntity livingEntity) {
		return livingEntity.getMaxHealth();
	}

	@Override
	public void setEntityMaxHealth(LivingEntity livingEntity) {
		livingEntity.getMaxHealth();
	}

	@Override
	public void resetEntityMaxHealth(LivingEntity livingEntity) {
		livingEntity.resetMaxHealth();
	}

	@Override
	public double[] getRecentTps() {
		return MinecraftServer.getServer().recentTps;
	}

	@Override
	public int getPlayerPing(Player player) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		
		return entityPlayer.ping;
	}
}