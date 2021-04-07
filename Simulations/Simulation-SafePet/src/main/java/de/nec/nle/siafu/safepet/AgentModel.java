/*
 * Copyright NEC Europe Ltd. 2006-2007
 * 
 * This file is part of the context simulator called Siafu.
 * 
 * Siafu is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * Siafu is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.nec.nle.siafu.safepet;

import static de.nec.nle.siafu.safepet.Constants.POPULATION;
import static de.nec.nle.siafu.safepet.Constants.Fields.ACTIVITY;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Random;

import de.nec.nle.siafu.behaviormodels.BaseAgentModel;
import de.nec.nle.siafu.exceptions.PlaceTypeUndefinedException;
import de.nec.nle.siafu.model.Agent;
import de.nec.nle.siafu.model.Place;
import de.nec.nle.siafu.model.World;
import de.nec.nle.siafu.safepet.Constants.Activity;
import de.nec.nle.siafu.types.EasyTime;
import de.nec.nle.siafu.types.Text;

/**
 * This Agent Model defines the behavior of users in this test simulation.
 * Essentially, most users will wander around in a zombie like state, except
 * for Pietro and Teresa, who will stay put, and the postman, who will spend a
 * simulation life time running between the two ends of the map.
 * 
 * @author Miquel Martin
 * 
 */
public class AgentModel extends BaseAgentModel {

	/**
	 * The time at which the psotman moves fastest.
	 */
	private static final int POSTMAN_PEAK = 12;

	/**
	 * A random number generator.
	 */
	private static final Random RAND = new Random();

	/**
	 * The top speed at which agents will move.
	 */
	private static final int TOP_SPEED = 10;

	/**
	 * A special user that plays a courrier of the Czar.
	 */
	private Agent dog;

	/**
	 * The current time.
	 */
	private EasyTime now;

	/** Place one in the simulation. */
	private Place placeHome;
	private Place placeOne;
	private Place placeTwo;
	private Place placeThree;

	/** Place two in the simulation. */

	/**
	 * Constructor for the agent model.
	 * 
	 * @param world the simulation's world
	 */
	public AgentModel(final World world) {
		super(world);
	}

	/**
	 * Create a bunch of agents that will wander around aimlessly. Tweak them
	 * for testing purposes as needed. Two agents, Pietro and Teresa, are
	 * singled out and left under the control of the user. A third agent,
	 * Postman, is set to run errands between the two places int he map.
	 * 
	 * @return the created agents
	 */
	public ArrayList<Agent> createAgents() {
		world.stopSpinning(true);
		System.out.println("Creating " + POPULATION + " pet.");
		ArrayList<Agent> population = AgentGenerator.createRandomPopulation(POPULATION, world);

		try {
			placeHome = world.getPlacesOfType("Home").iterator().next();
			placeOne = world.getPlacesOfType("PointOne").iterator().next();
			placeTwo = world.getPlacesOfType("PointTwo").iterator().next();
			placeThree = world.getPlacesOfType("PointThree").iterator().next();
		} catch (PlaceTypeUndefinedException e) {
			throw new RuntimeException(e);
		}

		dog = population.get(0);

		dog.setName("Pet Dog - ID 001");
		dog.setImage("Dog");
		dog.setPos(placeHome.getPos());
		dog.setVisible(true);
		dog.setSpeed(2);
		dog.getControl();
		dog.set("Language", new Text("Portuguese"));

		return population;
	}

	/**
	 * Make all the normal agents wander around, and the postman, run errands
	 * from one place to another. His speed depends on the time, slowing down
	 * at night.
	 * 
	 * @param agents the list of agents
	 */
	public void doIteration(final Collection<Agent> agents) {
		Calendar time = world.getTime();
		now = new EasyTime(time.get(Calendar.HOUR_OF_DAY), time
						.get(Calendar.MINUTE));
		handleDog();
		for (Agent a : agents) {
			if (!a.isOnAuto()) {
				continue; // This guy's being managed by the user interface
			}
			if (a.equals(dog)) {
				continue;
			}
			handleDog(a);
		}
	}

	/**
	 * Move the postman from one place to the next, increasing the speed the
	 * closer to noon it is.
	 * 
	 */
	private void handleDog() {
		dog.setSpeed(POSTMAN_PEAK
				- Math.abs(POSTMAN_PEAK - now.getHour()));
		if (dog.isAtDestination()) {
			if (dog.getPos().equals(placeHome.getPos())) {
				dog.setDestination(placeOne);
			} else if(dog.getPos().equals(placeOne.getPos())) {
				dog.setDestination(placeTwo);
			} else if(dog.getPos().equals(placeTwo.getPos())) {
				dog.setDestination(placeThree);
			} else if(dog.getPos().equals(placeThree.getPos())) {
				world.stopSpinning(true);
				dog.setDestination(placeHome);
			}
		}
	}

	/**
	 * Keep the agent wandering around zombie style.
	 * 
	 * @param a the agent to zombiefy
	 */
	private void handleDog(final Agent a) {
		switch ((Activity) a.get(ACTIVITY)) {
		case WAITING:
			break;

		case WALKING:
			a.wander();
			break;
		default:
			throw new RuntimeException("Unable to handle activity "
					+ (Activity) a.get(ACTIVITY));
		}

	}
}
