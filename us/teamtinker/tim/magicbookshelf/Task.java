package us.teamtinker.tim.magicbookshelf;

/**
 * @author Tim Clancy
 * @version 1.15.14
 */
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Task implements Runnable {

    private final int taskID;

    public Task(Plugin plugin, long initialDelay, long delay) {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, initialDelay, delay);
    }

    public final boolean isAlive() {
        return Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID);
    }

    public final void cancel() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}