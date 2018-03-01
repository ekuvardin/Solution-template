package executor.executionStrategy;

import java.util.Queue;

/**
 * Execution strategy to run tasks
 */
public interface IExecutionStrategy {

    /**
     * Poll Runnable from tasks and executing them until get Interrupted or manually stop
     *
     * @param tasks queue of tasks to executing
     * @return true if execution processes normally or false - was thrown InterruptedException
     */
    boolean process(Queue<Runnable> tasks);

    /**
     * Stop executing current strategy
     */
    void stopExecution();

    /**
     * Some action after submitting task to queue
     */
    void onTrySubmitTask();

    /**
     * Can submit task to current strategy
     *
     * @return can submit task
     */
    boolean canSubmitTask();
}
