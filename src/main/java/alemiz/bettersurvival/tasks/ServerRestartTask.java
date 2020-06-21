package alemiz.bettersurvival.tasks;

import alemiz.bettersurvival.BetterSurvival;

public class ServerRestartTask implements Runnable{

    private final BetterSurvival plugin;

    public ServerRestartTask(BetterSurvival plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int restartIn = this.plugin.getRestartTime();

        if (restartIn > 0){
            this.plugin.setRestartTime(restartIn - 10);
            return;
        }
        this.plugin.getServer().shutdown();
    }
}
