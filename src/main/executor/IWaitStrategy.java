package main.executor;

public interface IWaitStrategy {

   void wait(int milSeconds);

   void notifyWaiter();

}
