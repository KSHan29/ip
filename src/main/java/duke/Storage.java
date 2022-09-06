package duke;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import duke.tasks.Deadline;
import duke.tasks.Event;
import duke.tasks.Task;
import duke.tasks.ToDo;

/**
 * Contains methods that deal with storing and loading Tasks from memory.
 */
public class Storage {
    private String filePath;
    private String tempFilePath;

    /**
     * Constructor for creating Storage.
     *
     * @param filePath The filePath to the file that stores Tasks in a .txt file.
     * @param tempFilePath The filePath for a temporary file for use when main file has to be rewritten.
     */
    public Storage(String filePath, String tempFilePath) {
        this.filePath = filePath;
        this.tempFilePath = tempFilePath;
    }

    /**
     * Creates files for storage of Tasks if they do not exist. If they exist, returns list of Tasks.
     *
     * @return An ArrayList of Tasks added previously.
     */
    public ArrayList<Task> loadTaskList() {
        // Ensure file exists
        File tasksFile = new File(filePath);
        File tempFile = new File(tempFilePath);
        try {
            tasksFile.createNewFile();
            tempFile.createNewFile();
        } catch (IOException | SecurityException e) {
            System.out.println(e.getMessage());
        }

        // hardDiskTasks and tempTasks files should exist here
        assert new File(filePath).exists() : "File exists is supposed to return true";
        assert new File(tempFilePath).exists() : "Temp file exists is supposed to return true";

        // Add disk info to taskList
        ArrayList<Task> pastTasks = readTaskMemoFromDisk(tasksFile);
        return pastTasks;
    }

    public HashMap<String, Task> generateTaskListQuickFind(ArrayList<Task> taskList) {
        HashMap<String, Task> pastTasks = new HashMap<>();
        for (Task task : taskList) {
            String taskDescription = task.getDescription();
            Task putReturnValue = pastTasks.put(taskDescription, task);

            assert putReturnValue == null : "There should not be duplicate Task descriptions";
        }
        return pastTasks;
    }

    /**
     * Loads Tasks previously added from memory and adds into an ArrayList.
     *
     * @param file File which contains Tasks added previously.
     * @return ArrayList of Tasks which were added previously.
     */
    public ArrayList<Task> readTaskMemoFromDisk(File file) {
        ArrayList<Task> pastTasks = new ArrayList<>();
        try {
            Scanner s = new Scanner(file);
            while (s.hasNext()) {
                String memo = s.nextLine();

                Task currentTask = parseTaskMemo(memo);
                pastTasks.add(currentTask);
            }
            s.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        } catch (IndexOutOfBoundsException e) {
            // input file is invalid, just start from empty list;
            return new ArrayList<>();
        }
        return pastTasks;
    }

    /**
     * Parse TaskMemo to create Task list.
     *
     * @param memo The input read from Task memory file.
     * @return Task created using info from memory.
     */
    public Task parseTaskMemo(String memo) {
        // first letter to identify task
        String task = memo.substring(0, 1);

        // Retrieve task status
        String separator = "|";
        String taskMarked = "1";
        int indexOfFirstBreak = memo.indexOf(separator);
        String status = memo.substring(indexOfFirstBreak + 2, indexOfFirstBreak + 3);
        boolean statusIsDone = status.equals(taskMarked);

        // skip "| x | " to get task description
        String descriptionAndTime = memo.substring(indexOfFirstBreak + 6);
        int indexOfThirdBreak = descriptionAndTime.indexOf("|");
        String description = descriptionAndTime;
        String time = "";

        // if time exists, retrieve time and update task description
        if (indexOfThirdBreak != -1) {
            description = descriptionAndTime.substring(0, indexOfThirdBreak - 1);
            time = descriptionAndTime.substring(indexOfThirdBreak + 2);
        }

        Task currentTask = createTask(task, description, time, statusIsDone);
        return currentTask;
    }

    /**
     * Creates a Task object using information retrieved from memory.
     *
     * @param task Task identifier.
     * @param description Task description.
     * @param time Time associated with Task.
     * @param isDone Status of Task.
     * @return Task object.
     */
    public Task createTask(String task, String description, String time, boolean isDone) {
        Task newTask = null;
        LocalDate date = null;
        if (time != "") {
            date = LocalDate.parse(time);
        }
        switch (task) {
        case "T":
            newTask = new ToDo(description);
            break;
        case "D":
            newTask = new Deadline(description, date);
            break;
        case "E":
            newTask = new Event(description, date);
            break;
        default:
            throw new DukeException("OOPS!!! The Disk memory is invalid");
        }
        newTask.setTaskStatus(isDone);
        return newTask;
    }

    /**
     * Stores Task information onto memory.
     *
     * @param task Task information.
     * @throws IOException if unable to write to file.
     */
    public void addTaskToDisk(String task) throws IOException {
        appendToFile(filePath, task);
    }

    /**
     * Rewrites the .txt file that contains all the tasks added in order to reflect changes in a task status.
     *
     * @param taskNumber the row of Task which changed status.
     * @param isDone the new status of the task.
     * @throws IOException if unable to write to file.
     */
    public void setTaskStatusOnDisk(int taskNumber, boolean isDone) throws IOException {
        File inputFile = new File(filePath);
        File tempFile = new File(tempFilePath);
        Scanner s = new Scanner(inputFile);
        while (s.hasNext()) {
            String currentTaskMemo = s.nextLine();
            String newTaskMemo = changeStatusOfTaskMemo(taskNumber, isDone, currentTaskMemo);
            appendToFile(tempFilePath, newTaskMemo + System.lineSeparator());
            taskNumber -= 1;
        }
        s.close();
        boolean successful = tempFile.renameTo(inputFile);
        assert successful : "Temp file should be successfully renamed as main Memory file keep track of Tasks";
    }

    private String changeStatusOfTaskMemo(int taskNumber, boolean isDone, String currentTaskMemo) {
        if (taskNumber != 1) {
            return currentTaskMemo;
        }
        // before status "x | description"
        int indexOfFirstBreak = currentTaskMemo.indexOf("|");
        String beforeStatus = currentTaskMemo.substring(0, indexOfFirstBreak + 2);

        // after " X | x"
        String afterStatus = currentTaskMemo.substring(indexOfFirstBreak + 3);
        String status = isDone ? "1" : "0";
        currentTaskMemo = beforeStatus + status + afterStatus;
        return currentTaskMemo;
    }

    /**
     * Removes Task information from memory.
     *
     * @param taskNumber Row of Task to delete from memory.
     * @throws IOException if unable to write to file.
     */
    public void deleteTaskFromDisk(int taskNumber) throws IOException {
        File inputFile = new File(filePath);
        File tempFile = new File(tempFilePath);
        Scanner s = new Scanner(inputFile);
        while (s.hasNext()) {
            String currentLine = s.nextLine();
            if (taskNumber != 1) {
                appendToFile(tempFilePath, currentLine + System.lineSeparator());
            }
            taskNumber -= 1;
        }
        s.close();
        boolean successful = tempFile.renameTo(inputFile);
        assert successful : "Temp file should be successfully renamed as main Memory file keep track of Tasks";

    }

    /**
     * Appends String to a file.
     *
     * @param file The filepath of file to be appended.
     * @param textToAdd The String to be appended to file.
     * @throws IOException if unable to write to file.
     */
    public void appendToFile(String file, String textToAdd) throws IOException {
        FileWriter fw = new FileWriter(file, true);
        fw.write(textToAdd);
        fw.close();
    }
}
