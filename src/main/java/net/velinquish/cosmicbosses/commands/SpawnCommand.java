package net.velinquish.cosmicbosses.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.velinquish.cosmicbosses.CosmicBosses;
import net.velinquish.utils.AnyCommand;
import net.velinquish.utils.Common;

public class SpawnCommand extends AnyCommand {

	CosmicBosses plugin = CosmicBosses.getInstance();

	// /boss spawn <mythicmob> <instant drop kit> [<normal drop kit>] [<location>]
	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());

		checkArgs(3, plugin.getLangManager().getNode("command-spawn-usage"));

		if (!MythicMobs.inst().getMobManager().getMobNames().contains(args[1]))
			returnTell(plugin.getLangManager().getNode("invalid-mob").replace(Common.map("%boss%", args[1])));

		int locArg = 3;
		String dropKit = null;
		if (args.length > 3) //invalid dropkit
			if (plugin.getKit(args[3]) == null)
				try {
					Double.parseDouble(args[3]);
				} catch (NumberFormatException e) {
					returnTell(plugin.getLangManager().getNode("invalid-kit").replace(Common.map("%kit%", args[3]))); //invalid kit and invalid coordinate
				} //else, assumes no dropkit is specified
			else {
				locArg = 4;
				dropKit = args[3];
			}
		if (plugin.getKit(args[2]) == null)
			returnTell(plugin.getLangManager().getNode("invalid-kit").replace(Common.map("%kit%", args[2])));

		Location loc = getLocation(locArg, plugin.getLangManager().getNode("command-spawn-usage"), plugin.getLangManager().getNode("command-spawn-console-usage"));

		plugin.checkMaxBosses();
		ActiveMob boss = MythicMobs.inst().getMobManager().spawnMob(args[1], loc);

		CosmicBosses.debug("Dropkit used: " + dropKit);
		plugin.registerBoss(boss.getUniqueId(), args[1], args[2], dropKit);
	}

}
