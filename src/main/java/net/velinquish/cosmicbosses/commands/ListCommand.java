package net.velinquish.cosmicbosses.commands;

import org.bukkit.command.CommandSender;

import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.AnyCommand;
import net.velinquish.utils.Common;

public class ListCommand extends AnyCommand {

	CosmicBosses plugin = CosmicBosses.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());

		if (plugin.getKits().isEmpty())
			returnTell(plugin.getLangManager().getNode("no-kits-exist"));
		tell(plugin.getLangManager().getNode("kit-list-heading"));
		for (String kit : plugin.getKits())
			tell(plugin.getLangManager().getNode("kit-list-item").replace(Common.map("%kit%", kit)));
	}

}
