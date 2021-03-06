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
package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.math.Matrix3X3;
import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.util.IObserverRegistry;
import io.github.jevaengine.util.NullObservers;
import io.github.jevaengine.util.Observers;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.physics.PhysicsBodyShape;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface IAnimationSceneModel extends ISceneModel {
	IAnimationSceneModel clone();

	IAnimationSceneModelAnimation getAnimation(String name);

	String[] getAnimations();

	boolean hasAnimation(String name);

	public enum AnimationSceneModelAnimationState implements Serializable {
		Play,
		PlayWrap,
		Stop,
		PlayToEnd,
	}

	public interface IAnimationSceneModelAnimation {
		AnimationSceneModelAnimationState getState();

		void setState(AnimationSceneModelAnimationState state);

		IObserverRegistry getObservers();
	}

	public interface IAnimationSceneModelAnimationObserver {
		void event(String name);

		void stateChanged(AnimationSceneModelAnimationState state);
	}

	public static final class NullAnimationSceneModelAnimation implements IAnimationSceneModelAnimation {
		@Override
		public AnimationSceneModelAnimationState getState() {
			return AnimationSceneModelAnimationState.Stop;
		}

		@Override
		public void setState(AnimationSceneModelAnimationState state) {
		}

		@Override
		public IObserverRegistry getObservers() {
			return new NullObservers();
		}
	}

	public static final class NullAnimationSceneModel implements IAnimationSceneModel {
		private Direction direction;

		@Override
		public void dispose() {
		}

		@Override
		public void update(int deltaTime) {
		}

		@Override
		public Direction getDirection() {
			return direction;
		}

		@Override
		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		@Override
		public PhysicsBodyShape getBodyShape() {
			return new PhysicsBodyShape();
		}

		@Override
		public List<ISceneModelComponent> getComponents(Matrix3X3 projection) {
			return new ArrayList<>();
		}

		@Override
		public Rect3F getAABB() {
			return new Rect3F();
		}

		@Override
		public IAnimationSceneModel clone() {
			return new NullAnimationSceneModel();
		}

		@Override
		public IAnimationSceneModelAnimation getAnimation(String name) {
			return new NullAnimationSceneModelAnimation();
		}

		@Override
		public String[] getAnimations() {
			return new String[0];
		}

		@Override
		public boolean hasAnimation(String name) {
			return false;
		}

		@Override
		public IObserverRegistry getObservers() {
			return new Observers();
		}
	}
}
