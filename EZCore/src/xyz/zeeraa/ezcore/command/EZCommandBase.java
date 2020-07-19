package xyz.zeeraa.ezcore.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

public abstract class EZCommandBase {
	private EZCommandBase parentCommand;
	private List<EZSubCommand> subCommands;
	
	private String permission;
	private String permissionDescription;
	private PermissionDefault permissionDefaultValue;
	
	private CommandPermissionMode permissionMode;
	
	private String description;

	private String name;
	private List<String> aliases;

	public EZCommandBase(String name) {
		this.name = name;
		this.description = "";
		this.permission = null;
		this.permissionMode = CommandPermissionMode.DEFAULT;
		this.permissionDescription = "";
		this.permissionDefaultValue = PermissionDefault.FALSE;
		this.subCommands = new ArrayList<EZSubCommand>();
		this.parentCommand = null;

		this.aliases = new ArrayList<String>();
	}

	public List<EZSubCommand> getSubCommands() {
		return subCommands;
	}

	public List<String> getAliases() {
		return aliases;
	}

	protected void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	
	public String getDescription() {
		return description;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}
	
	public String getPermission() {
		return permission;
	}
	
	protected void setPermission(String permission) {
		this.permission = permission;
	}

	public boolean hasPermission() {
		return permission != null;
	}
	
	public String getPermissionDescription() {
		return permissionDescription;
	}
	
	protected void setPermissionDescription(String permissionDescription) {
		this.permissionDescription = permissionDescription;
	}
	
	public PermissionDefault getPermissionDefaultValue() {
		return permissionDefaultValue;
	}
	
	protected void setPermissionDefaultValue(PermissionDefault permissionDefaultValue) {
		this.permissionDefaultValue = permissionDefaultValue;
	}
	
	public CommandPermissionMode getPermissionMode() {
		return permissionMode;
	}
	
	protected void setPermissionMode(CommandPermissionMode permissionMode) {
		this.permissionMode = permissionMode;
	}
	
	/**
	 * Add the help sub command to this command
	 */
	protected void addHelpSubCommand() {
		addSubCommand(new HelpSubCommand());
	}
	
	protected void addHelpSubCommand(String permission) {
		this.addHelpSubCommand(permission, PermissionDefault.FALSE);
	}
	
	
	protected void addHelpSubCommand(String permission, PermissionDefault permissionDefaultValue) {
		this.addHelpSubCommand(permission, "Show help", permissionDefaultValue);
	}
	
	
	protected void addHelpSubCommand(String permission, String permissionDescription) {
		this.addHelpSubCommand(permission, permissionDescription, PermissionDefault.FALSE);
	}
	
	protected void addHelpSubCommand(String permission, String permissionDescription, PermissionDefault permissionDefaultValue) {
		addSubCommand(new HelpSubCommand(permission, permissionDescription, permissionDefaultValue));
	}
	
	/**
	 * Executes the command, returning its success
	 *
	 * @param sender       Source object which is executing this command
	 * @param commandLabel The alias of the command used
	 * @param args         All arguments passed to the command, split via ' '
	 * @return true if the command was successful, otherwise false
	 */
	public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);

	/**
	 * Executed on tab completion for this command, returning a list of options the
	 * player can tab through.
	 *
	 * @param sender Source object which is executing this command
	 * @param alias  the alias being used
	 * @param args   All arguments passed to the command, split via ' '
	 * @return a list of tab-completions for the specified arguments. This will
	 *         never be null. List may be immutable.
	 * @throws IllegalArgumentException if sender, alias, or args is null
	 */
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		Validate.notNull(sender, "Sender cannot be null");
		Validate.notNull(args, "Arguments cannot be null");
		Validate.notNull(alias, "Alias cannot be null");

		if (args.length == 0) {
			return ImmutableList.of();
		}

		String lastWord = args[args.length - 1];

		Player senderPlayer = sender instanceof Player ? (Player) sender : null;

		ArrayList<String> matchedPlayers = new ArrayList<String>();
		for (Player player : sender.getServer().getOnlinePlayers()) {
			String name = player.getName();
			if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, lastWord)) {
				matchedPlayers.add(name);
			}
		}

		Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
		return matchedPlayers;
	}

	protected void addSubCommand(EZSubCommand subCommand) {
		subCommands.add(subCommand);
		subCommand.setParentCommand(this);
	}

	public boolean hasParentCommand() {
		return parentCommand != null;
	}

	/**
	 * Set the parent command. After the parent command has been set all other calls
	 * to this function will be ignored
	 * 
	 * @param parentCommand {@link EZCommandBase} instance of the parent command
	 */
	protected void setParentCommand(EZCommandBase parentCommand) {
		if (this.parentCommand != null) {
			return;
		}
		
		this.parentCommand = parentCommand;
	}

	public EZCommandBase getParentCommand() {
		return parentCommand;
	}

	public String getName() {
		return name;
	}
	
	public static List<String> generateAliasList(String... aliases) {
		List<String> result = new ArrayList<>();
		
		for(String alias : aliases) {
			result.add(alias);
		}
		
		return result;
	}
	
	public String getNoPermissionMessage() {
		return ChatColor.RED + "You dont have permission to use this command";
	}
}