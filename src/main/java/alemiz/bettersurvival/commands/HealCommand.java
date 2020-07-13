package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import cn.nukkit.Server;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class HealCommand extends Command {

    public MoreVanilla loader;

    public HealCommand(String name, MoreVanilla loader) {
        super(name, "Hồi máu", "");

        this.usage ="§7/heal <player(tuỳ ý)> : Hồi máu cho người chơi khác hoặc bản thân";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });

        this.setPermission(loader.configFile.getString("permission-heal"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player) && args.length < 1){
            sender.sendMessage("§cLệnh này chỉ được thử hiện trong game!");
            return true;
        }

        if (!(sender instanceof Player)){
            if (args.length < 1){
                sender.sendMessage(getUsageMessage());
                return true;
            }

            Player player = Server.getInstance().getPlayer(args[0]);
            this.loader.heal(player, "console");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1){
            this.loader.heal(player, player.getName());
            return true;
        }

        player = Server.getInstance().getPlayer(args[0]);
        this.loader.heal(player, ((Player) sender).getName());
        return true;
    }
}
