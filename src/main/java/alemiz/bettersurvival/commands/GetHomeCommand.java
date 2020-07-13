package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.Set;

public class GetHomeCommand extends Command {

    public Home loader;

    public GetHomeCommand(String name, Home loader) {
        super(name, "Hiện home của bạn", "", new String[]{"listhome"});
        this.commandParameters.clear();

        this.usage = "§7/gethome: Hiện home của bạn";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });

        this.loader = loader;
    }


    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cLệnh này chỉ được sử dụng trong game!");
            return true;
        }

        Player player = (Player) sender;

        Set<String> homes = loader.getHomes(player);
        if (homes.isEmpty()){
            player.sendMessage("§6»§7Bạn chưa có home!");
            return true;
        }

        StringBuilder format = new StringBuilder();
        for (String home : homes){
            format.append(home).append(", ");
        }

        player.sendMessage("§6»§7Homes của bạn: "+format.substring(0, format.length()-2));
        return true;
    }
}
