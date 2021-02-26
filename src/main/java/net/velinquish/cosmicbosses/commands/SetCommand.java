package net.velinquish.cosmicbosses.commands;

import java.util.ArrayList;
import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.Common;
import net.velinquish.utils.PlayerCommand;

public class SetCommand extends PlayerCommand {

	CosmicBosses plugin = CosmicBosses.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());
		checkArgs(2, plugin.getLangManager().getNode("command-set-usage"));

		ArrayList<ItemStack> kit = new ArrayList<>();
		ItemStack item;
		for (int i = 0; i < 36; i++) {
			item = ((Player) sender).getInventory().getItem(i);
			if (Objects.nonNull(item)) kit.add(item);
		}

		if (plugin.setKit(args[1], kit, true))
			plugin.getLangManager().getNode("kit-set").replace(Common.map("%kit%", args[1])).execute(sender);
		else
			plugin.getLangManager().getNode("kit-created").replace(Common.map("%kit%", args[1])).execute(sender);
	}

}
