package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.ArrayList;
import java.util.List;

public class TpaCommand extends Command {

    public MoreVanilla loader;

    public TpaCommand(String name, MoreVanilla loader) {
            super(name, "Dịch chuyển người chơi", "");

        this.usage = "§7/tpa <player> : Gửi yêu cầu cho người chơi\n" +
                "§7/tpa <a|accept> : Chấp nhận yêu cầu dịch chuyển tới\n" +
                "§7/tpa <d|denny> : Từ chối yêu cầu";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player|accept|deny", false)
        });

        this.setPermission(loader.configFile.getString("permission-tpa"));
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

        if (args.length < 1){

                FormWindowSimple form = new FormWindowSimple("Dịch chuyển người chơi", "");
            List<Player> players = new ArrayList<>(this.loader.plugin.getServer().getOnlinePlayers().values());
            players.remove(player);

            if (players.isEmpty()){
                form.setContent("§7Tất cả người chơi đã offline!");
            }else {
                for (Player pplayer : players)
                    form.addButton(new ElementButton("§5"+pplayer.getName()+"\n§7»Nhấn vào để dịch chuyển"));
            }

            player.showFormWindow(form);
            return true;
        }

        switch (args[0]){
            case "a":
            case "accept":
                loader.tpaAccept(player);
                break;
            case "d":
            case "denny":
                loader.tpaDenny(player);
                break;
            default:
                loader.tpa(player, args[0]);
        }
        return true;
    }
}
