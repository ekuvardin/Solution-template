package executor.executionStrategy;

import java.util.Queue;

/**
 * Strategy stops any tasks execution
 */
public class EndExecution implements IExecutionStrategy {

    @Override
    public boolean process(Queue<Runnable> tasks) {
        return false;
    }

    @Override
    public void stopExecution() {
    }

    @Override
    public void onTrySubmitTask() {
        throw new RuntimeException("Submit new tasks are forbidden");
    }

    @Override
    public boolean canSubmitTask() {
        return false;
    }
}
