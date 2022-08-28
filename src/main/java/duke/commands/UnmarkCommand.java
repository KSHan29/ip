package duke.commands;

import duke.*;

import java.io.IOException;

/**
 * Command for Unmarking Tasks.
 */
public class UnmarkCommand extends Command {
    private String remainingCommand;

    /**
     * UnmarkCommand constructor.
     *
     * @param remainingCommand task number.
     */
    public UnmarkCommand(String remainingCommand) {
        this.remainingCommand = remainingCommand;
    }

    /**
     * Executes a unmark command.
     *
     * @param tasks TaskList that stores Tasks objects.
     * @param ui Ui that handles user interaction.
     * @param storage Storage that handles storing information on memory files.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        try {
            int taskNumber = Integer.parseInt(remainingCommand);
            tasks.setTaskStatus(taskNumber - 1, false);
            storage.setTaskStatusOnDisk(taskNumber, false);
            ui.printUnmarkTask(tasks.getTaskToString(taskNumber - 1));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("     OOPS!!! Please enter a valid task number.");
        } catch (IOException e) {
            System.out.println("     " + e.getMessage());
        }
    }

    /**
     * Checks if program should exit.
     *
     * @return false as program should not exit.
     */
    @Override
    public boolean isExit() {
        return false;
    }
}
