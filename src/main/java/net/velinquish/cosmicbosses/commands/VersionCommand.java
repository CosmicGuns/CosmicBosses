package net.velinquish.cosmicbosses.commands;

import org.bukkit.command.CommandSender;

import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.AnyCommand;

public class VersionCommand extends AnyCommand {

	CosmicBosses plugin = CosmicBosses.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		tellRaw("&8&m= = = = = = = = = = = = = = = = = = = = = = =");
		tellRaw("&3Plugin: &7CosmicBosses");
		tellRaw("&3Version: &7" + plugin.getDescription().getVersion());
		tellRaw("&3Author: &bVelinquish");
		tellRaw("&8&m= = = = = = = = = = = = = = = = = = = = = = =");

	}

}
