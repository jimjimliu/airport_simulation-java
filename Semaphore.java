class Semaphore {
    private int count;

    public Semaphore(int count) {
        this.count = count;
    }

    synchronized public void waitSem() throws InterruptedException {
        count--;
        if (count<0) {
            // then place this thread in waiting queue
            wait();
        }
    }


    synchronized public void signalSem() {
        count++;
        // remove a thread in waiting queue
        // and place it in entry queue
        notify();
        //has no effect if there are no thread in waiting queue
    }

}//Semaphore
