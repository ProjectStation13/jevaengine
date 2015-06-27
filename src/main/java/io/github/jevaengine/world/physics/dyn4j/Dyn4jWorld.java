/* 
 * Copyright (C) 2015 Jeremy Wildsmith.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.jevaengine.world.physics.dyn4j;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsWorld;
import io.github.jevaengine.world.physics.NonparticipantPhysicsBody;
import io.github.jevaengine.world.physics.PhysicsBodyDescription;
import io.github.jevaengine.world.physics.PhysicsBodyShape;
import static io.github.jevaengine.world.physics.PhysicsBodyShape.PhysicsBodyShapeType.Box;
import static io.github.jevaengine.world.physics.PhysicsBodyShape.PhysicsBodyShapeType.Circle;
import java.util.LinkedList;
import java.util.Queue;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

public final class Dyn4jWorld implements IPhysicsWorld
{
	private final World m_physicsWorld = new World();
	
	private final float m_maxSurfaceFrictionForceNewtonMeters;
	
	private final PhysicsContactListener m_contactListener = new PhysicsContactListener();
	
	public Dyn4jWorld(float maxSurfaceFrictionForceNewtonMeters)
	{
		m_maxSurfaceFrictionForceNewtonMeters = maxSurfaceFrictionForceNewtonMeters;
		
		m_physicsWorld.setGravity(new Vector2());
		m_physicsWorld.addListener(m_contactListener);
	}
	
	@Nullable
	private Convex constructShape(PhysicsBodyShape shape)
	{
		Rect3F aabb = shape.aabb;
		
		if(!aabb.hasVolume())
			return null;
		
		switch(shape.type)
		{
		default:
			assert false: "Unrecognized shape type.";
		case Box:
		case Circle:
			Vector2 points[] = new Vector2[4];
			points[0] = new Vector2(shape.aabb.x, shape.aabb.y);
			points[1] = new Vector2(shape.aabb.x + shape.aabb.width, shape.aabb.y);
			points[2] = new Vector2(shape.aabb.x + shape.aabb.width, shape.aabb.y + shape.aabb.height);
			points[3] = new Vector2(shape.aabb.x, shape.aabb.y + shape.aabb.height);
			return new Polygon(points);
		//	return new Circle(Math.min(aabb.width, aabb.height) / 2.0F);
		}
	}
	
	World getWorld()
	{
		return m_physicsWorld;
	}
	
	@Override
	public float getMaxFrictionForce()
	{
		return m_maxSurfaceFrictionForceNewtonMeters;
	}
	
	@Override
	public void update(int deltaTime)
	{
		m_physicsWorld.update(deltaTime);
		m_contactListener.relay();
	}
	
	@Override
	public void setGravity(Vector2F gravity)
	{
		m_physicsWorld.setGravity(Dyn4jUtil.unwrap(gravity));
	}
	
	@Override
	public IPhysicsBody createBody(@Nullable IEntity owner, PhysicsBodyDescription bodyDescription)
	{
		Convex shape = constructShape(bodyDescription.shape);
		
		if(shape == null)
			return new NonparticipantPhysicsBody(owner);
		
		Body body = new Body();
		
		if(bodyDescription.type == PhysicsBodyDescription.PhysicsBodyType.Static || bodyDescription.isSensor)
			body.setMass(Mass.Type.INFINITE);
		else
			body.setMass(shape.createMass(bodyDescription.density));
			
		BodyFixture fixture = new BodyFixture(shape);
		fixture.setSensor(bodyDescription.isSensor);
		fixture.setDensity(bodyDescription.density);
		fixture.setUserData(owner);
		fixture.setFriction(bodyDescription.friction);
		body.addFixture(fixture);
		m_physicsWorld.addBody(body);
		
		return new Dyn4jBody(this, body, fixture, bodyDescription.shape.aabb.getXy(), owner, bodyDescription.collisionExceptions);
	}
	
	@Override
	public IPhysicsBody createBody(PhysicsBodyDescription bodyDescription)
	{
		return createBody(null, bodyDescription);
	}
	
	/*
	 * The contact listener is notified during a world step routine. During a step routine, the world is locked,
	 * preventing bodies from being created in the physics world. If the contactListener notifies physics bodies of contacts
	 * during a step cycle, the body observers could react by mutating the world (which is not possible when it is locked in a step cycle.)
	 * 
	 * Thus, contact callbacks are queued and then executed after the world step routine has completed via the relay method.
	 */
	private class PhysicsContactListener implements ContactListener
	{
		public Queue<Runnable> m_contactProcesses = new LinkedList<>();
		
		public void relay()
		{
			for(Runnable r; (r = m_contactProcesses.poll()) != null;)
				r.run();
		}
		
		@Override
		public boolean begin(ContactPoint contact)
		{
			final Object oAPhysicsBody = contact.getBody1().getUserData();
			final Object oBPhysicsBody = contact.getBody2().getUserData();
				
			if(oAPhysicsBody instanceof Dyn4jBody && oBPhysicsBody instanceof Dyn4jBody)
			{
				m_contactProcesses.add(new Runnable() {

					@Override
					public void run() {
						((Dyn4jBody)oAPhysicsBody).beginContact((Dyn4jBody)oBPhysicsBody);
						((Dyn4jBody)oBPhysicsBody).beginContact((Dyn4jBody)oAPhysicsBody);					
					}
				});
			}
			
			return true;
		}

		@Override
		public void end(ContactPoint contact)
		{
			final Object oAPhysicsBody = contact.getBody1().getUserData();
			final Object oBPhysicsBody = contact.getBody2().getUserData();
				
			if(!(oAPhysicsBody instanceof Dyn4jBody && oBPhysicsBody instanceof Dyn4jBody))
				return;

			m_contactProcesses.add(new Runnable() {
				
				@Override
				public void run() {
					((Dyn4jBody)oAPhysicsBody).endContact((Dyn4jBody)oBPhysicsBody);
					((Dyn4jBody)oBPhysicsBody).endContact((Dyn4jBody)oAPhysicsBody);					
				}
			});
		}

		@Override
		public boolean preSolve(ContactPoint contact)
		{
			final Object oAPhysicsBody = contact.getBody1().getUserData();
			final Object oBPhysicsBody = contact.getBody2().getUserData();
		
			if((oAPhysicsBody instanceof Dyn4jBody && oBPhysicsBody instanceof Dyn4jBody))
			{
				Dyn4jBody a = (Dyn4jBody)oAPhysicsBody;
				Dyn4jBody b = (Dyn4jBody)oBPhysicsBody;

				if(!(a.collidesWith(b) && b.collidesWith(a)))
					return false;
			}
			
			return true;
		}

		@Override
		public void sensed(ContactPoint contact) { }

		@Override
		public boolean persist(PersistedContactPoint point) { return true; }

		@Override
		public void postSolve(SolvedContactPoint point) { }
	}

}
