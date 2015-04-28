package com.example.alexander.robotop.ThreadControll;

/**
 * Created by Alexander on 28/04/2015.
 */
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Executer<T>{
    private ExecutorService executer =  Executors.newSingleThreadExecutor();
    private Queue<Future<T>> result = new LinkedList<Future<T>>();

    public void execute(Callable<T> call){
        result.add(executer.submit(call));
    }
    //ToDo check for queue empty
    public T getResultWait(long millis) throws InterruptedException, ExecutionException {
        T calced;
        if(result.isEmpty()){
            return null;
        }
        try{
            calced = result.peek().get(millis, TimeUnit.MILLISECONDS);
        }catch(TimeoutException e){
            return null;
        }
        result.poll();
        return calced;
    }

    public T getResult() throws InterruptedException, ExecutionException{
        return result.poll().get();
    }


}

