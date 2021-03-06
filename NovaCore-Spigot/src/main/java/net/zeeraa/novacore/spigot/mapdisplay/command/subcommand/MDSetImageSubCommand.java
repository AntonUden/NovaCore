package net.zeeraa.novacore.spigot.mapdisplay.command.subcommand;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaSubCommand;
import net.zeeraa.novacore.spigot.mapdisplay.MapDisplay;
import net.zeeraa.novacore.spigot.mapdisplay.MapDisplayManager;

public class MDSetImageSubCommand extends NovaSubCommand {

	public MDSetImageSubCommand() {
		super("setimage");

		setPermission("novacore.command.mapdisplay.setimage");
		setPermissionDefaultValue(PermissionDefault.OP);
		setDescription("Set the image of a map display");
		setFilterAutocomplete(true);
		setAllowedSenders(AllowedSenders.ALL);
		setUseage("/mapdisplay setimage <name> <url>");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!MapDisplayManager.getInstance().isEnabled()) {
			sender.sendMessage(ChatColor.DARK_RED + "MapDisplayManager is not enabled");
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please provide a name of a display");
			return false;
		}

		if (args.length == 1) {
			sender.sendMessage(ChatColor.RED + "Please provide a url containing an image");
			return false;
		}

		for (MapDisplay display : MapDisplayManager.getInstance().getMapDisplays()) {
			if (display.getName().equalsIgnoreCase(args[0])) {
				BufferedImage image = null;

				try {
					URL url = new URL(args[1]);
					image = ImageIO.read(url);
					Log.trace("Image loaded from url");
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Could not read image from url. " + e.getClass().getName() + " " + e.getMessage());
					e.printStackTrace();
				}

				if (image != null) {
					try {
						display.setImage(image);
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Failed to set the display image. " + e.getClass().getName() + " " + e.getMessage());
						e.printStackTrace();
					}
				}

				return true;
			}
		}

		sender.sendMessage(ChatColor.RED + "Could not find map display named " + args[0]);

		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<String>();

		for (MapDisplay display : MapDisplayManager.getInstance().getMapDisplays()) {
			result.add(display.getName());
		}

		return result;
	}
}