package me.autobot.lib.systems.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An action that can be called and performed by the robot.
 * */
public class Action {
    private static List<Action> actions = new ArrayList<>();

    /**
     * Activates the given action to be executed and run to completion.
     * @param action The action to activate.
     * */
    public static void activate(Action action) {
        action.init();
        actions.add(action);
    }

    /**
     * Updates all actions.
     * */
    public static void update() {
        for (Action action : actions) {
            if (action.hasFinished() || action.isCancelled()) {
                actions.remove(action);
            } else {
                action.execute();
            }
        }
    }


    private UUID uuid = UUID.randomUUID();
    private boolean cancelled = false;

    /**
     * Creates a new Action.
     * */
    public Action() {

    }

    /**
     * Called when the action is initialized.
     * */
    public void init() {

    }

    /**
     * Executes the action.
     * */
    public void execute() {

    }

    /**
     * Cancels and effectively stops executing the action.
     * */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Returns whether the action has finished.
     * @return Whether the action has finished.
     * */
    public boolean hasFinished() {
        return true;
    }

    /**
     * Returns whether the action has been cancelled.
     * @return Whether the action has been cancelled.
     * */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the UUID of the action.
     * @return The UUID of the action.
     * */
    public UUID getUUID() {
        return uuid;
    }
}
