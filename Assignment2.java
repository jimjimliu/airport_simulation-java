/*
 * CSI 3131
 * Assignment 2
 * Junhan Liu
 * 7228243
 */

import java.util.Random;

/***************************************************************************************/
//  Provide code for the methods in the classes Aeroplane and Airport, and in one place
//  in the main() method. Look for "your code here" comments.


/* the main class of assignment 2, launching the simulation */
public class Assignment2 {
    // Configuration
    final static int DESTINATIONS = 4;
    final static int AEROPLANES = 6;
    final static int PLANE_SIZE = 3;
    final static int PASSENGERS = 20;
    final static String[] destName = {"Toronto", "New York", "New Delhi", "Beijing"};

    public static void main(String args[]){
        int i;
        Aeroplane[] plane = new Aeroplane[6];
        Passenger[] passengers = new Passenger[20];

        // create the airport
        Airport sp = new Airport();

        /* create aeroplanes and passengers*/
        for (i=0; i<6; i++)
         plane[i] = new Aeroplane(sp, i);
        for (i=0; i<20; i++)
            passengers[i] = new Passenger(sp, i);

        /* now launch them */
        for (i=0; i<6; i++)
         plane[i].start();
        for (i=0; i<20; i++)
            passengers[i].start();

        // let them enjoy for 20 seconds
        try { Thread.sleep(20000);} catch (InterruptedException e) { }

        /* now stop them */
        // note how we are using deferred cancellation
        for (i=0; i<6; i++)
            try {plane[i].interrupt();} catch (Exception e) { }
        for (i=0; i<20; i++)
            try {passengers[i].interrupt();} catch (Exception e) { }

        // Wait until everybody else is finished
        // your code here
        
        for(int k=0; k<6; k++){
         try{
          plane[k].join();
         }catch(Exception e){
          e.printStackTrace();
         }
        }
        
        for(int h=0; h<20; h++){
         try{
          passengers[h].join();
         }catch(Exception e){
          e.printStackTrace();
         }
        }
 

        // This should be the last thing done by this program:
        System.out.println("Simulation finished.");
    }
}



/* The class implementing a passenger. */
// This class is completely provided to you, you don't have to change
// anything, just have a look and understand what the passenger wants from
// the airport and from the aeroplanes
class Passenger extends Thread {
    private boolean enjoy;
    private int id;
    private Airport sp;

    // constructor
    public Passenger(Airport sp, int id) {
        this.sp = sp;
        this.id = id;
        enjoy = true;
    }

    // this is the passenger's thread
    public void run() {
        int         stime;
        int         dest;
        Aeroplane   sh;

        while (enjoy) {
            try {
                // Wait and arrive to the port
                stime = (int) (700*Math.random());
                sleep(stime);

                // Choose the destination
                dest = (int) (((double) Assignment2.DESTINATIONS)*Math.random());
                System.out.println("Passenger " + id + " wants to go to " + Assignment2.destName[dest]);

                // come to the airport and board a aeroplane to my destination
                // (might wait if there is no such aeroplane ready)
                sh = sp.wait4Ship(dest);
                
                // Should be executed after the aeroplane is on the pad and taking passengers
                System.out.println("Passenger " + id + " has boarded aeroplane " + sh.id + ", destination: "+Assignment2.destName[dest]);

                // wait for launch
                sh.wait4launch();

                // Enjoy the ride
                // Should be executed after the aeroplane has launched.
                System.out.println("Passenger "+id+" enjoying the ride to "+Assignment2.destName[dest]+ ": Woohooooo!");

                // wait for landing
                sh.wait4landing();

                // Should be executed after the aeroplane has landed
                System.out.println("Passenger " + id + " leaving the aeroplane " + sh.id);

                // Leave the aeroplane
                sh.leave();
            } catch (InterruptedException e) {
                enjoy = false; // have been interrupted, probably by the main program, terminate
            }
       }
       System.out.println("Passenger "+id+" has finished its rides.");
    }
}



/* The class simulating an aeroplane */
// Now, here you will have to implement several methods
class Aeroplane extends Thread {
    public int          id;
    private Airport    airport;
    private boolean enjoy;
    // your code here (other local variables and semaphores)
    
    public int numPassenger;
    public Semaphore semBoard; 
    public Semaphore semPads; 
    public Semaphore semLaunch;
    public Semaphore passengerReady;
    public Semaphore semDisembark;
    public Semaphore semHasLeft;
 
    
    // constructor
    public Aeroplane(Airport sp, int id) {
        this.airport = sp;
        this.id = id;
        enjoy = true;

        // your code here (local variable and semaphore initializations)
        numPassenger = 0;
        this.semBoard = new Semaphore(0);
        this.semLaunch = new Semaphore(0);
        this.semPads = sp.semPads;
        this.passengerReady = new Semaphore(0);
        this.semDisembark = new Semaphore(0);
        this.semHasLeft = new Semaphore(1);
    }

    // the aeroplane thread executes this
    public void run() {
        int     stime;
        int     destination;

        while (enjoy) {
            try {
                // Wait until there an empty landing pad, then land
                destination = airport.wait4landing(this);

                System.out.println("Aeroplane " + id + " landing on pad " + destination);

                // Tell the passengers that we have landed
                if(getLoad() == 3){
                  this.semDisembark.signalSem();
                }

                // Wait until all passengers leave
                
                
                if(this.getLoad() != 0){
                  this.semHasLeft.waitSem();
                  this.semHasLeft.waitSem();
                  this.semHasLeft.waitSem();
                }

                System.out.println("Aeroplane " + id + " boarding to "+Assignment2.destName[destination]+" now!");

                // the passengers can start to board now
                airport.boarding(destination);
                this.semBoard.signalSem();

                // Wait until full of passengers
                this.semLaunch.waitSem();

                // 4, 3, 2, 1, Launch!
                System.out.println("Aeroplane " + id + " launches towards "+Assignment2.destName[destination]+"!");

                // tell the passengers we have launched, so they can enjoy now ;-)
                this.passengerReady.signalSem();
                this.passengerReady.signalSem();
                this.passengerReady.signalSem();
                
                airport.launch(destination);

                // Fly in the air
                stime = 500+(int) (1500*Math.random());
                sleep(stime);
                
                
            } catch (InterruptedException e) {
                enjoy = false; // have been interrupted, probably by the main program, terminate
            }
        }
        System.out.println("Aeroplane "+id+" has finished its flights.");
    }

    public int getLoad(){
      return this.numPassenger;
    }
    
    public void addLoad(){
      this.numPassenger = numPassenger+1;
    }
    
    public void reduceLoad(){
      this.numPassenger = numPassenger-1;
    }

    // service functions to passengers
    // called by the passengers leaving the aeroplane
    public void leave()  throws InterruptedException  {
        // your code here
      this.reduceLoad();
      if(this.getLoad() != 0){
       this.semDisembark.signalSem();
      }
      this.semHasLeft.signalSem();
    }

    // called by the passengers sitting in the aeroplane, to wait
    // until the launch
    public void wait4launch()  throws InterruptedException {
        // your code here
      
      this.semBoard.waitSem();
      
      this.addLoad();
      
      if(this.getLoad() == 3){
       this.semLaunch.signalSem();
      }
      else{
       this.semBoard.signalSem();
      }
      this.passengerReady.waitSem();
    }

    // called by the bored passengers sitting in the aeroplane, to wait
    // until landing
    public void wait4landing()  throws InterruptedException {
        // your code here
     
      this.semDisembark.waitSem();
    }
}



/* The class implementing the Airport. */
/* This might be convenient place to put lots of the synchronization code into */
class Airport {
    Aeroplane[]    pads; // what is sitting on a given pad
    // your code here (other local variables and semaphores)

    public Semaphore semPads;
    public Semaphore[] semPassenger = new Semaphore[4];
    public Semaphore semWaitList;
    public int[] waitList;
    
    // constructor
    public Airport() {
        int i;

        pads = new Aeroplane[4];
        this.waitList = new int[] {0,0,0,0};

        // pads[] is an array containing the aeroplanes sitting on corresponding pads
        // Value null means the pad is empty
        for(i=0; i<4; i++) {
            pads[i] = null;
        }

        // your code here (local variable and semaphore initializations)
        this.semWaitList = new Semaphore(1);
        this.semPads = new Semaphore(4);
        
        for(i=0; i<4; i++) {
            this.semPassenger[i] = new Semaphore(0);
        }
   
    }

    // called by a passenger wanting to go to the given destination
    // returns the aeroplane he/she boarded
    // Careful here, as the pad might be empty at this moment
    public Aeroplane wait4Ship(int dest) throws InterruptedException{
        // your code here
      
      this.semWaitList.waitSem();
      
      if(pads[dest] == null){
      
       this.waitList[dest]++;
       this.semWaitList.signalSem();
       System.out.println("The flight has taken off, please take the next flight.");
       this.semPassenger[dest].waitSem();
      }
      else{    
       this.semWaitList.signalSem();
      }
   
      return pads[dest];
    }

    // called by an aeroplane to tell the airport that it is accepting passengers now to destination dest
    public void boarding(int dest) {
        // your code here

      if(waitList[dest] != 0){
       int temp = this.waitList[dest];
       
       if(temp >= 3){
        for(int i=0; i< 3; i++){
         this.semPassenger[dest].signalSem();
         this.waitList[dest]--;
        }
       }
       else{
        for(int i=0; i< temp; i++){
         this.semPassenger[dest].signalSem();
         this.waitList[dest]--;
        }
       }
      }
    }

    // Called by an aeroplane returning from a trip
    // Returns the number of the empty pad where to land (might wait
    // until there is an empty pad).
    // Try to rotate the pads so that no destination is starved
    public int wait4landing(Aeroplane sh)  throws InterruptedException  {
        // your code here
    
      int padAvailable = -1;
      
      //initially there are 4 pads available
      //if all occupied, the wait, 
      //if no, then return an empty pad;
      this.semPads.waitSem();
      
      for(int i=0; i<pads.length; i++){
       if(pads[i] == null){
        padAvailable = i;
        pads[i] = sh;
        return i;
       }
      }
      return padAvailable;
    }

    // called by an aeroplane when it launches, to inform the
    // airport that the pad has been emptied
    public void launch(int dest) {
        // your code here
      pads[dest] = null;
      this.semPads.signalSem();
    }
}