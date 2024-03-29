package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.LevelVote;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;

public class LevelVoteCommand extends Command {

    public LevelVote loader;

    public LevelVoteCommand(String name, LevelVote loader, String usage) {
        super(name, "Vote về thời tiết và thời gian", "");

        this.usage = usage;
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-vote"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cBạn chỉ được sử dụng lệnh này trong game!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2){
            FormWindowCustom form = new FormWindowCustom("Vote");
            form.addElement(new ElementDropdown("§7Chọn chủ đề", this.loader.voteTopics, 0));
            form.addElement(new ElementInput("§7Chọn giá trị"));
            player.showFormWindow(form);
            return true;
        }

        this.loader.vote(player, args[0], args[1]);
        return true;
    }
}
