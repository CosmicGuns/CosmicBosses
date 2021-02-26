package net.velinquish.cosmicbosses.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.AnyCommand;

public class CommandManager extends Command {

	private CosmicBosses plugin = CosmicBosses.getInstance();

	public CommandManager(String name) {
		super(name);
		setAliases(plugin.getConfig().getStringList("plugin-aliases"));
		setDescription("Main command for CosmicBosses");
	}

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length > 0)
			if ("reload".equalsIgnoreCase(args[0])) {
				handle(new ReloadCommand(), sender, args);
				return true;
			} else if ("ver".equalsIgnoreCase(args[0]) || "version".equalsIgnoreCase(args[0]) || "about".equalsIgnoreCase(args[0])) {
				new VersionCommand().execute(sender, args, false);
				return true;
			} else if ("list".equalsIgnoreCase(args[0])) {
				new ListCommand().execute(sender, args, false);
				return true;
			} else if ("add".equalsIgnoreCase(args[0])) {
				handle(new AddCommand(), sender, args);
				return true;
			} else if ("set".equalsIgnoreCase(args[0])) {
				handle(new SetCommand(), sender, args);
				return true;
			} else if ("spawn".equalsIgnoreCase(args[0])) {
				handle(new SpawnCommand(), sender, args);
				return true;
			}
		plugin.getLangManager().getNode("command-message").execute(sender);

		return false;
	}

	public void handle(AnyCommand cmd, CommandSender sender, String[] args) {
		cmd.execute(sender, args, Arrays.asList(args).contains("-s"));
	}
}
