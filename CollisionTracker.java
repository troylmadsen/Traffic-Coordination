
package src;

/**
 * A tracker to notify vehicles of collisions occurring between one another and
 * allow interfacing from the simulator.
 * 
 * @author Troy Madsen
 */
public class CollisionTracker {
	/**
	 * Whether a vehicle has been part of an collision.
	 */
	private boolean hadCollision = false;
	
	/**
	 * The VIN of the owner of this object.
	 */
	private int myVIN;
	
	/**
	 * The VIN of the other vehicle involved in the collision.
	 */
	private int otherVIN;
	
	/**
	 * Time collision occurred.
	 */
	//TODO
	private double time;
	
	/**
	 * Speed of this vehicle at time of collision.
	 */
	//TODO
	private double mySpeed;
	
	/**
	 * Speed of other vehicle at time of collision.
	 */
	//TODO
	private double incidentSpeed;
	
	/**
	 * Default constructor for a working tracker.
	 */
	public CollisionTracker() {}
	
	/**
	 * Signals that this vehicle was part of an accident and should update its
	 * state.
	 * 
	 * @param myVIN The VIN of the owner of this object.
	 * @param otherVIN The VIN of the other vehicle involved in the collision.
	 */
	public void notifyCollision(int myVIN, int otherVIN) {
		this.myVIN = myVIN;
		this.otherVIN = otherVIN;
		setHadCollision();
		
		//FIXME Debugging
		System.out.println("Collision: " + Integer.toString(myVIN)
				+ " " + Integer.toString(otherVIN));
	}
	
	/**
	 * Sets the state of hadCollision to true.
	 * 
	 * @return If the state was changed.
	 */
	private boolean setHadCollision() {
		boolean b = hadCollision;
		
		hadCollision = true;
		return !b;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
