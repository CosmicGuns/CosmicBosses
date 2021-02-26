package net.velinquish.cosmicbosses.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.Common;
import net.velinquish.utils.PlayerCommand;

public class AddCommand extends PlayerCommand {

	CosmicBosses plugin = CosmicBosses.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());
		checkArgs(2, plugin.getLangManager().getNode("command-add-usage"));

		ItemStack item = ((Player) sender).getInventory().getItemInMainHand();

		if (item.getType().equals(Material.AIR))
			returnTell(plugin.getLangManager().getNode("invalid-material"));

		if (plugin.addToKit(args[1], item))
			plugin.getLangManager().getNode("item-added-to-kit").replace(Common.map("%kit%", args[1], "%item%", item.getItemMeta().getDisplayName())).execute(sender);
		else
			plugin.getLangManager().getNode("kit-created").replace(Common.map("%kit%", args[1])).execute(sender);
	}

}
